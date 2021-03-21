import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j", "log4j-api", properties["version.log4j"].toString())
    implementation("org.apache.logging.log4j", "log4j-core", properties["version.log4j"].toString())
    implementation("org.slf4j", "slf4j-simple", properties["version.slf4j"].toString())

    implementation("org.springframework", "spring-webflux", properties["version.spring.web"].toString()) {
        exclude(module = "spring-bens")
        exclude(module = "spring-aop")
    }
    testImplementation("org.springframework", "spring-test", properties["version.spring.web"].toString())

    implementation("io.projectreactor", "reactor-core", properties["version.reactor"].toString())
    testImplementation("io.projectreactor", "reactor-test", properties["version.reactor"].toString())
    implementation("io.projectreactor.netty", "reactor-netty", properties["version.netty"].toString())

    implementation("org.springframework.data", "spring-data-mongodb", properties["version.spring.data"].toString())
    implementation("org.mongodb", "mongodb-driver-reactivestreams", properties["version.mongodb"].toString())

    implementation("com.fasterxml.jackson.core", "jackson-databind", properties["version.jackson"].toString())

    testImplementation("org.junit.jupiter", "junit-jupiter", properties["version.junit"].toString())
    testImplementation("org.testcontainers", "testcontainers", properties["version.testcontainers"].toString())
    testImplementation("org.testcontainers", "junit-jupiter", properties["version.testcontainers"].toString())
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

configure<ApplicationPluginConvention> {
    mainClassName = "com.example.spring.Main"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }
}