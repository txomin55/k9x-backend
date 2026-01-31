pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://repo.spring.io/milestone")
        }
    }
}

rootProject.name = "k9x-backend"

include(
    "k9x-backend-domain",
    "k9x-backend-application",
    "k9x-backend-infrastructure",
    "k9x-backend-loader",
)
