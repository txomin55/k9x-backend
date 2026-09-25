# syntax=docker/dockerfile:1.4
#
# Build stage: a GraalVM native image of the loader.
#
FROM ghcr.io/graalvm/native-image-community:25 AS build
# The Gradle wrapper script needs xargs, which the Oracle Linux base leaves out.
RUN microdnf install -y findutils && microdnf clean all
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
# Only the deploy workflows (.github/workflows/deploy*.yml) build this image — Render and Fly deploy
# the artifact they publish — so the credentials come from there, where GPR_KEY is the job's ephemeral
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
# SPRING_PROFILES_ACTIVE (set it to `deployed`), so the same binary serves staging and production.
# nativeCompile runs Spring's AOT processing and then native-image, which takes several minutes and
# a few GB of memory; a GitHub runner has enough of both.
RUN ./gradlew :k9x-backend-loader:nativeCompile \
    -Pgpr.user="$GPR_USER" -Pgpr.key="$GPR_KEY" \
    -x test

#
# Package stage: only the executable, no JVM. Debian slim rather than distroless keeps a shell for
# `fly ssh console`; its glibc is newer than the build stage's, which is what the binary links against.
#
FROM debian:bookworm-slim
LABEL maintainer="txomin.sirera@gmail.com"
LABEL version="1.0"
VOLUME /tmp/k9x-backend
COPY --from=build /home/k9x-backend/k9x-backend-loader/build/native/nativeCompile/ /usr/local/lib/k9x-backend/
EXPOSE 4000

# Runtime options of the native image (overridable by setting the platform's start command):
#   -Xmx300m                    the whole box is 512MB, and without a JIT, metaspace or code cache the
#                               process outside the heap is only a few tens of MB. Capping the heap keeps
#                               a leak from pushing the box into swap, which is what took it down on the
#                               JVM (2026-09-24 14:50).
#   -XX:+ExitOnOutOfMemoryError after an OutOfMemoryError the process stayed alive but answered nothing,
#                               and Fly does not restart a machine whose process is still running.
#                               Dying instead lets the platform restart it.
# Exec form, so the binary is PID 1 and receives the platform's SIGTERM.
ENTRYPOINT ["/usr/local/lib/k9x-backend/k9x-backend"]
CMD ["-Xmx300m", "-XX:+ExitOnOutOfMemoryError"]
