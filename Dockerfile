# syntax=docker/dockerfile:1.4
#
# Build stage
#
FROM eclipse-temurin:25-jdk AS build
WORKDIR /home/k9x-backend
COPY gradlew gradlew
COPY gradle/ gradle/
COPY settings.gradle.kts build.gradle.kts ./
COPY k9x-backend-domain/ k9x-backend-domain/
COPY k9x-backend-application/ k9x-backend-application/
COPY k9x-backend-infrastructure/ k9x-backend-infrastructure/
COPY k9x-backend-loader/ k9x-backend-loader/

# Build-time credentials to resolve dependencies from GitHub Packages. They are needed only here,
# never at runtime, and they are NOT the same thing as the platform's runtime secrets: on Fly,
# `fly secrets` are injected into the machine at boot and the builder never sees them.
#
#   GPR_USER  a plain build arg — a GitHub username is not a secret.
#   GPR_KEY   a build secret, mounted as a file for the duration of the RUN below. Unlike an ARG
#             it is not recorded in the image metadata or in any layer.
#
#   fly deploy --build-arg GPR_USER=<user> --build-secret GPR_KEY=<github PAT, read:packages>
#
# gradle.properties is git-ignored and excluded by .dockerignore, so it never reaches the builder.
ARG GPR_USER

# The image is profile-agnostic: the environment picks the profile at runtime through
# SPRING_PROFILES_ACTIVE (set it to `deployed`), so the same jar serves staging and production.
# Also downloads and unzips the New Relic Java agent into ./newrelic/.
RUN --mount=type=secret,id=GPR_KEY \
    ./gradlew :k9x-backend-loader:bootJar unzipNewrelic \
    -Pgpr.user="$GPR_USER" -Pgpr.key="$(cat /run/secrets/GPR_KEY)" \
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

ENTRYPOINT ["java", "-javaagent:/usr/local/lib/newrelic/newrelic.jar", "-jar", "/usr/local/lib/k9x-backend.jar"]
