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

# Build-time credentials to resolve dependencies from GitHub Packages (a GitHub PAT with the
# `read:packages` scope). They are needed only here, never at runtime, and they are NOT the
# platform's runtime secrets: `fly secrets` are injected into the machine at boot, and the builder
# never sees them. GPR_USER is a plain arg either way — a GitHub username is not a secret.
#
# The two deployment targets feed GPR_KEY in differently, so the RUN below accepts both:
#
#   Fly (production)  a build secret, mounted as a file only for that RUN and recorded in no layer:
#                     fly deploy --build-secret GPR_KEY=<PAT>
#   Render (staging)  a build arg, because Render has no build-secret mechanism: it forwards the
#                     service's environment variables to the build, so GPR_USER and GPR_KEY are
#                     set in the Render dashboard. There the PAT does end up in the build layer's
#                     metadata, which is the reason production does not use this path.
#
# gradle.properties is git-ignored and excluded by .dockerignore, so it never reaches the builder.
ARG GPR_USER
ARG GPR_KEY

# The image is profile-agnostic: the environment picks the profile at runtime through
# SPRING_PROFILES_ACTIVE (set it to `deployed`), so the same jar serves staging and production.
# Also downloads and unzips the New Relic Java agent into ./newrelic/.
RUN --mount=type=secret,id=GPR_KEY,required=false \
    ./gradlew :k9x-backend-loader:bootJar unzipNewrelic \
    -Pgpr.user="$GPR_USER" \
    -Pgpr.key="$(cat /run/secrets/GPR_KEY 2>/dev/null || echo "$GPR_KEY")" \
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
