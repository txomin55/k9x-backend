import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-tx")
    implementation("org.flywaydb:flyway-core")
    implementation(project(":k9x-backend-application"))
    implementation(project(":k9x-backend-infrastructure"))
}

springBoot {
    mainClass.set("com.k9x.K9xBackendApplication")
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

// `./gradlew :k9x-backend-loader:bootRun` runs from the repository root, so the local profiles find
// the .env files that live there.
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    workingDir = rootProject.projectDir
}
