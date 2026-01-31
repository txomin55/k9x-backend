val jjwtVersion = "0.13.0"
val restAssuredVersion = "6.0.0"
val k9xStubsVersion = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":k9x-backend-application"))
    implementation(project(":k9x-backend-domain"))
    implementation("com.k9x:oas-definition-stubs:$k9xStubsVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")

    compileOnly("org.springframework:spring-context")

    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("junit:junit")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")

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
