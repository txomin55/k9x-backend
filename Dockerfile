# syntax=docker/dockerfile:1.4
#
# Build stage
#
FROM eclipse-temurin:25-jdk AS build
WORKDIR /home/k9x-backend
COPY gradlew gradlew
COPY gradle/ gradle/
# Fetch the Gradle distribution in a layer of its own. It only depends on the wrapper files, so the
# layer cache (exported to GitHub Actions) reuses it across builds and services.gradle.org is only
# hit when the Gradle version changes. That host answers CI runners with an occasional 403, and a
# download in the same step as the build meant one of those failed the whole deploy.
RUN ./gradlew --version
COPY settings.gradle.kts build.gradle.kts ./
COPY k9x-backend-domain/ k9x-backend-domain/
COPY k9x-backend-application/ k9x-backend-application/
COPY k9x-backend-infrastructure/ k9x-backend-infrastructure/
COPY k9x-backend-loader/ k9x-backend-loader/

# Build-time credentials to resolve com.k9x:oas-definition-stubs from GitHub Packages. They are
# needed here only, never at runtime, and they are NOT the platforms' runtime secrets: on Fly a
# secret is injected into the machine at boot and a builder never sees it.
#
# Only .github/workflows/deploy.yml builds this image — Render and Fly deploy the artifact it
# publishes — so the credentials come from that workflow, where GPR_KEY is the job's ephemeral
# GITHUB_TOKEN. Nothing here has to be configured on either platform.
#
# Building by hand needs them passed explicitly, taking the values from gradle.properties:
#
#   docker build --build-arg GPR_USER=<user> --build-arg GPR_KEY=<github PAT, read:packages> .
#
# gradle.properties is git-ignored and excluded by .dockerignore, so it never reaches the builder.
ARG GPR_USER
ARG GPR_KEY

# The image is profile-agnostic: the environment picks the profile at runtime through
# SPRING_PROFILES_ACTIVE (set it to `deployed`), so the same jar serves staging and production.
# Also downloads and unzips the New Relic Java agent into ./newrelic/.
RUN ./gradlew :k9x-backend-loader:bootJar unzipNewrelic \
    -Pgpr.user="$GPR_USER" -Pgpr.key="$GPR_KEY" \
    -x test

# Override the agent's bundled default config with the project's custom one.
COPY newrelic/newrelic.yml newrelic/newrelic.yml

#
# Package stage
#
FROM eclipse-temurin:25-jre
LABEL maintainer="txomin.sirera@gmail.com"
LABEL version="1.0"
VOLUME /tmp/k9x-backend
COPY --from=build /home/k9x-backend/k9x-backend-loader/build/libs/*.jar /usr/local/lib/k9x-backend.jar
COPY --from=build /home/k9x-backend/newrelic/newrelic.jar /usr/local/lib/newrelic/newrelic.jar
COPY --from=build /home/k9x-backend/newrelic/newrelic.yml /usr/local/lib/newrelic/newrelic.yml
EXPOSE 4000

# JVM flags tuned for a small, slow box (Render gives staging 0.1 CPU, Fly 1 shared CPU, 512MB both):
#   -XX:TieredStopAtLevel=1  only the C1 JIT: compiles faster and cheaper, peak speed a bit lower.
#                            The backend is I/O-bound, so startup time matters more than peak speed.
#   -XX:+UseSerialGC         single-threaded GC: no GC worker threads competing for one CPU and less
#                            memory overhead than G1, which is what the JVM would otherwise pick.
#   -Xss512k                 half the default stack for platform threads (virtual threads are unaffected).
#   -XX:MaxRAMPercentage=70  without it the heap tops out at 25% of the box, ~128MB of the 512MB, and a
#                            single heavy request threw OutOfMemoryError in production (2026-09-24 04:44 UTC).
#                            The other 30% is left for metaspace, thread stacks and the New Relic agent.
#                            Not higher: at 90% the heap plus all that outgrew the 512MB box and the kernel
#                            killed the process (2026-09-24 10:40 UTC), with no Java log at all. For more
#                            heap, give the machine more memory in fly.toml instead.
#   -XX:+ExitOnOutOfMemoryError  after that OutOfMemoryError the JVM stayed alive but answered nothing, and
#                            Fly does not restart a machine whose process is still running. Dying instead
#                            lets the platform restart it.
# Overridable per environment by setting JAVA_OPTS on the platform.
ENV JAVA_OPTS="-XX:TieredStopAtLevel=1 -XX:+UseSerialGC -Xss512k -XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"

# The New Relic agent is only attached where NEW_RELIC_ENABLED=true (production, see fly.toml).
# Instrumenting every class as it loads is most of the startup cost on staging's 0.1 CPU, and it
# disables the JVM's class data sharing (the "Sharing is only supported for boot loader classes"
# warning). `exec` keeps java as PID 1 so it receives the platform's SIGTERM.
ENTRYPOINT ["sh", "-c", "if [ \"$NEW_RELIC_ENABLED\" = true ]; then AGENT=-javaagent:/usr/local/lib/newrelic/newrelic.jar; fi; exec java $JAVA_OPTS $AGENT -jar /usr/local/lib/k9x-backend.jar"]
