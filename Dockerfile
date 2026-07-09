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

RUN ./gradlew :k9x-backend-loader:bootJar -PspringProfilesActive=production \
    -Pgpr.user="$GPR_USER" -Pgpr.key="$GPR_KEY" \
    -x test

#
# Package stage
#
FROM eclipse-temurin:25-jre
LABEL maintainer="txomin.sirera@gmail.com"
LABEL version="1.0"
VOLUME /tmp/k9x-backend
COPY --from=build /home/k9x-backend/k9x-backend-loader/build/libs/*.jar /usr/local/lib/k9x-backend.jar
EXPOSE 4000
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=production", "/usr/local/lib/k9x-backend.jar"]
