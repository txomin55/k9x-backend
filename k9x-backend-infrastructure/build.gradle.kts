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
val fastexcelVersion = "0.20.2"
val poiVersion = "5.4.1"
val openPdfVersion = "3.0.5"
val springdocVersion = "3.0.1"

dependencies {
    implementation(project(":k9x-backend-application"))
    implementation(project(":k9x-backend-domain"))
    // The stubs publish springdoc (Swagger UI, swagger-core) as a runtime dependency. It only serves the API
    // docs, which the deployed profile never exposed, yet ~1,500 of its classes loaded at every start of the
    // 512 MB box. It is kept out of the jar and only reaches local runs, see the loader's developmentOnly.
    implementation("com.k9x:oas-definition-stubs:$k9xStubsVersion") {
        exclude(group = "org.springdoc")
    }
    // Bean Validation used to come through springdoc; the stubs' @Valid / @NotNull still need it to be enforced.
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // Only for OpenApiConfiguration to compile; it is skipped wherever springdoc is not on the classpath.
    compileOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.google.api-client:google-api-client:2.7.2")
    implementation("com.google.http-client:google-http-client-gson:1.44.2")
    // fastexcel writes the event export workbook. It replaced Apache POI, whose ~16 MB of classes stayed in the
    // metaspace of the 512 MB box after the first export; POI is kept for the tests only, to read it back.
    implementation("org.dhatim:fastexcel:$fastexcelVersion")
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
    unitTestImplementation("org.apache.poi:poi-ooxml:$poiVersion")
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
