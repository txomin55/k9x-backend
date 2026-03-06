import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation(project(":k9x-backend-application"))
    implementation(project(":k9x-backend-infrastructure"))
}

springBoot {
    mainClass.set("com.k9x.K9xBackendApplication")
}

val springProfilesActive = (findProperty("springProfilesActive") as String?) ?: "develop"
val projectArtifactId: String = project.name
val projectVersion = project.version.toString()
val googleClientId = (findProperty("google.client_id") as String?) ?: ""
val googleClientSecret = (findProperty("google.client_secret") as String?) ?: ""
val googleRedirectUrl = (findProperty("google.redirect_url") as String?) ?: ""

tasks.processResources {
    filesMatching("application.yml") {
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "spring.profiles.active" to springProfilesActive,
                "project.artifactId" to projectArtifactId,
                "project.version" to projectVersion,
                "google.client_id" to googleClientId,
                "google.client_secret" to googleClientSecret,
                "google.redirect_url" to googleRedirectUrl,
            ),
            "beginToken" to "@",
            "endToken" to "@",
        )
    }
}
