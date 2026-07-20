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

# Build-time credentials to resolve dependencies from GitHub Packages.
# Set these as env vars in Render (forwarded to the build as args).
# gradle.properties is git-ignored, so it is never present in the build context.
ARG GPR_USER
ARG GPR_KEY
ARG SPRING_PROFILES_ACTIVE

# Also downloads and unzips the New Relic Java agent into ./newrelic/.
RUN ./gradlew :k9x-backend-loader:bootJar unzipNewrelic -PspringProfilesActive="$SPRING_PROFILES_ACTIVE" \
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

ENTRYPOINT ["java", "-javaagent:/usr/local/lib/newrelic/newrelic.jar", "-jar", "/usr/local/lib/k9x-backend.jar"]
