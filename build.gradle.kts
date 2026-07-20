import de.undercouch.gradle.tasks.download.Download

plugins {
    id("org.springframework.boot") version "4.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("de.undercouch.download") version "5.3.0"
}

val downloadNewrelic by tasks.registering(Download::class) {
    mkdir("newrelic")
    src("https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip")
    dest(file("newrelic"))
}

tasks.register<Copy>("unzipNewrelic") {
    dependsOn(downloadNewrelic)
    from(zipTree(file("newrelic/newrelic-java.zip")))
    into(rootDir)
}

allprojects {
    group = "com.tmanager.k9x-backend"
    version = "0.0.1"

    // SNAPSHOT dependencies (e.g. com.k9x:oas-definition-stubs) are republished in place on
    // GitHub Packages, which deletes older timestamped jars. Gradle caches SNAPSHOT metadata for
    // 24h by default, so it can stay pinned to a timestamp whose jar no longer exists. Re-check the
    // latest snapshot on every build so we always resolve the most recent published artifact.
    configurations.all {
        resolutionStrategy {
            cacheChangingModulesFor(0, "seconds")
        }
    }

    repositories {
        mavenCentral()
        maven {
            url = uri("https://repo.spring.io/milestone")
        }
        maven {
            url = uri("https://maven.pkg.github.com/txomin55/k9x-oas-definition")
            credentials {
                username = (findProperty("gpr.user") as String?) ?: System.getenv("GITHUB_ACTOR")
                password = (findProperty("gpr.key") as String?) ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    configurations.named("compileOnly") {
        extendsFrom(configurations.named("annotationProcessor").get())
    }

    extensions.configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.0")
        }
    }

    apply(plugin = "jvm-test-suite")

    extensions.configure<TestingExtension> {
        suites {
            register<JvmTestSuite>("unitTest") {
                useJUnitJupiter()
                dependencies {
                    implementation(project())
                }
            }
        }
    }

    configurations.named("unitTestImplementation") {
        extendsFrom(configurations.getByName("implementation"))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    // The `unitTest` JvmTestSuite is not bound to `check` by default, so `build`/`check` would skip it.
    // Wire it in so CI and `./gradlew build` run the unit tests.
    tasks.named("check") {
        dependsOn(tasks.named("unitTest"))
    }
}
