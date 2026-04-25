plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.gradle.git.properties)
    alias(libs.plugins.sonarqube)
    jacoco
}

group = "com.kubyshkin.hr-support"
version = "0.0.2"

springBoot {
    buildInfo()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

sonar {
    properties {
        property("sonar.projectKey", "FreeWind6_hr_support")
        property("sonar.organization", "freewind6")
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.integration:spring-integration-jdbc")
    implementation("org.springframework.integration:spring-integration-jpa")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation(libs.hypersistence.utils.hibernate)
    implementation(libs.telegrambots.client)
    implementation(libs.telegrambots.springboot.longpolling.starter)

    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.integration:spring-integration-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
