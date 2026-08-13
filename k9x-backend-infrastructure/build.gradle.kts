import org.jooq.meta.jaxb.Property

plugins {
    id("org.jooq.jooq-codegen-gradle") version "3.19.23"
}

val jjwtVersion = "0.13.0"
val restAssuredVersion = "6.0.0"
val k9xStubsVersion = "0.0.1-SNAPSHOT"
val jooqVersion = "3.19.23"
val postgresqlVersion = "42.7.11"
val flywayDatabasePostgresqlVersion = "12.4.0"
val poiVersion = "5.4.1"
val commonsIoVersion = "2.18.0"
val openPdfVersion = "3.0.5"

dependencies {
    implementation(project(":k9x-backend-application"))
    implementation(project(":k9x-backend-domain"))
    implementation("com.k9x:oas-definition-stubs:$k9xStubsVersion")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.google.api-client:google-api-client:2.7.2")
    implementation("com.google.http-client:google-http-client-gson:1.44.2")
    implementation("nl.martijndwars:web-push:5.1.1")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("org.apache.poi:poi-ooxml:$poiVersion")
    // POI needs commons-io at runtime (SXSSFWorkbook -> UnsynchronizedByteArrayOutputStream). It comes in
    // transitively through poi, but the IDE's Gradle import drops it and the app then dies with a
    // NoClassDefFoundError on the first export. Declared explicitly so no toolchain can lose it.
    implementation("commons-io:commons-io:$commonsIoVersion")
    // OpenPDF renders the printable event proof. Note the coordinate changed meaning in 3.0.0: 3.x lives in
    // org.openpdf.* while everything under the old com.lowagie.* 2.x package is deprecated.
    implementation("com.github.librepdf:openpdf:$openPdfVersion")

    runtimeOnly("org.postgresql:postgresql:$postgresqlVersion")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:$flywayDatabasePostgresqlVersion")

    compileOnly("org.springframework:spring-context")

    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    unitTestImplementation("org.springframework.boot:spring-boot-starter-test")
    unitTestImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    unitTestRuntimeOnly("org.junit.platform:junit-platform-launcher")

    jooqCodegen("org.jooq:jooq-meta-extensions:$jooqVersion")
}

jooq {
    configuration {
        generator {
            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                withProperties(
                    Property().withKey("scripts").withValue("src/main/resources/db/schema"),
                    Property().withKey("sort").withValue("flyway"),
                    Property().withKey("defaultNameCase").withValue("lower")
                )
            }
            generate {
                isDeprecated = false
                isRecords = true
            }
            target {
                packageName = "com.k9x.infrastructure.out.postgres.jooq.generated"
                directory = "src/main/generated"
            }
        }
    }
}

sourceSets {
    main {
        java.srcDir("src/main/generated")
    }
}

tasks.named<ProcessResources>("processResources") {
    from("src/main/java") {
        include("**/*.json")
    }
}

tasks.named("compileJava") {
    dependsOn("jooqCodegen")
}

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/txomin55/k9x-oas-definition")
        credentials {
            username = (project.findProperty("gpr.user") as String?) ?: System.getenv("GITHUB_ACTOR")
            password = (project.findProperty("gpr.key") as String?) ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
