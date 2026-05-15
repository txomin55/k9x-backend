dependencies {
    implementation(project(":k9x-backend-domain"))

    compileOnly("org.springframework:spring-context")

    unitTestImplementation("org.springframework.boot:spring-boot-starter-test")
    unitTestRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
