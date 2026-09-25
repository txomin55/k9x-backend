import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("org.springframework.boot")
    id("org.graalvm.buildtools.native")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-tx")
    implementation("org.flywaydb:flyway-core")
    // Metrics, traces and logs over OTLP, to New Relic in production (the native image cannot carry its Java
    // agent). The JDK HTTP sender replaces the default OkHttp one, which drags the Kotlin stdlib into the image.
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry") {
        exclude(group = "io.opentelemetry", module = "opentelemetry-exporter-sender-okhttp")
    }
    implementation("io.opentelemetry:opentelemetry-exporter-sender-jdk")
    // The span exporter is declared in OpenTelemetryExportConfiguration, so its class is needed to compile.
    implementation("io.opentelemetry:opentelemetry-exporter-otlp") {
        exclude(group = "io.opentelemetry", module = "opentelemetry-exporter-sender-okhttp")
    }
    // Pinned to the release built on the same OpenTelemetry SDK (1.62) as the one Spring Boot manages.
    implementation("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.28.0-alpha")
    implementation(project(":k9x-backend-application"))
    implementation(project(":k9x-backend-infrastructure"))
    // Swagger UI for local runs (bootRun and the IDE); developmentOnly never reaches the bootJar, so staging
    // and production do not carry it. Same version as the one the OAS stubs are generated against.
    developmentOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")
}

springBoot {
    mainClass.set("com.k9x.K9xBackendApplication")
}

// Deployed environments run a GraalVM native image (`nativeCompile`, see Dockerfile): it boots in well under a
// second and fits the 512MB boxes with room to spare. Local runs (bootRun, the IDE) stay on the JVM.
graalvmNative {
    binaries.named("main") {
        imageName.set("k9x-backend")
        // The root build applies `java-library` to every module, which makes the plugin default to a shared library.
        sharedLibrary.set(false)
        // The image is built on a GitHub runner and runs on Render and Fly hosts: target the baseline x86-64
        // instruction set instead of the build machine's, or a host with an older CPU refuses to start it.
        buildArgs.add("-march=compatibility")
        // The image only carries the standard charsets by default; OpenPDF encodes its fonts in Cp1252.
        buildArgs.add("-H:+AddAllCharsets")
    }
}

// Only build metadata is baked into application.yml. The active profile is chosen at runtime via
// SPRING_PROFILES_ACTIVE, and secrets (Google, JWT, VAPID) come from the environment — see
// .env.example — so nothing sensitive ends up inside the jar.
val projectArtifactId: String = project.name
val projectVersion = project.version.toString()

tasks.processResources {
    inputs.property("project.artifactId", projectArtifactId)
    inputs.property("project.version", projectVersion)
    filesMatching("application.yml") {
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "project.artifactId" to projectArtifactId,
                "project.version" to projectVersion,
            ),
            "beginToken" to "@",
            "endToken" to "@",
        )
    }
}

// Spring's AOT processing (the first half of nativeCompile) freezes into the image the profiles active while it
// runs and the outcome of every @ConditionalOn... check. Only deployed environments run the native image, so it
// is processed as one: without this it took the `local` default, and production booted with `local` and
// `deployed` both active. OTLP export is forced on so its beans exist in every image; each environment then turns
// it on or off at runtime (OpenTelemetryExportConfiguration).
tasks.named<org.springframework.boot.gradle.tasks.aot.ProcessAot>("processAot") {
    environment("SPRING_PROFILES_ACTIVE", "deployed")
    environment("OTLP_EXPORT_ENABLED", "true")
}

// `./gradlew :k9x-backend-loader:bootRun` runs from the repository root, so the local profiles find
// the .env files that live there.
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    workingDir = rootProject.projectDir
}
