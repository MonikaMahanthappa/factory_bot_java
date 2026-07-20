// factory-bot-spring-data-jpa: Spring Data JPA persistence adapter + auto-configuration.
// Wires the `create` strategy to a real repository/EntityManager — see ADR-0004.

dependencies {
    api(project(":factory-bot-core"))

    // Compile against Spring Data JPA / Spring context; consumers bring their own versions.
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:3.3.5")
    compileOnly("org.springframework.data:spring-data-jpa:3.3.5")
    compileOnly("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // Integration tests run a real Spring Boot + H2 slice.
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.5")
    testRuntimeOnly("com.h2database:h2:2.3.232")
}
