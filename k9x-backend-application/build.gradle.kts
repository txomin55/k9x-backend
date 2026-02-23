dependencies {
    implementation(project(":k9x-backend-domain"))
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")

    compileOnly("org.springframework:spring-context")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testng:testng:7.12.0")
    testImplementation("junit:junit")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}
