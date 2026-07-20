// examples/blog-app: a realistic Spring Boot app whose tests use factory_bot_java as the test-data factory.
// It depends on the library via project(...) references (the library is not yet on Maven Central).
//
// Run locally on H2:      ../../gradlew :examples:blog-app:bootRun
// Tests on Testcontainers: ../../gradlew :examples:blog-app:test   (requires Docker)

plugins {
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    runtimeOnly("com.h2database:h2")            // local `bootRun`
    runtimeOnly("org.postgresql:postgresql")    // tests (Testcontainers) / production

    // The library under demonstration — pulled in as a real dependency, from the consumer's perspective.
    testImplementation(project(":factory-bot-spring-data-jpa"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}
