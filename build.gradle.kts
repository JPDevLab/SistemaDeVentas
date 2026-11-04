plugins {
    id("java")
    id("jacoco")
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "6.0.1.5171"
    id("info.solidsoft.pitest") version "1.19.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

val cucumberVersion = "7.18.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    pitest("org.pitest:pitest-junit5-plugin:1.2.1")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

val sonarToken: String? = providers.environmentVariable("SONAR_TOKEN").orNull
    ?: providers.gradleProperty("sonarToken").orNull

sonar {
    properties {
        property("sonar.projectKey", "SistemaDeVentas")
        property("sonar.projectName", "Proyecto Demo Calidad")
        property("sonar.host.url", "http://localhost:9000")
        if (sonarToken != null) {
            property("sonar.token", sonarToken)
        }
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.exclusions", "src/main/resources/static/**")
    }
}

tasks.sonar {
    dependsOn(tasks.test, tasks.jacocoTestReport)
}

tasks.register<Test>("acceptanceTest") {
    description = "Runs Cucumber acceptance tests."
    group = "verification"
    useJUnitPlatform()
    filter {
        includeTestsMatching("runners.RunCucumberTest")
    }
}

pitest { pitestVersion.set("1.19.0")
    junit5PluginVersion.set("1.2.1")
    targetClasses.set(listOf("org.example.*"))
    targetTests.set(listOf("org.example.model.*", "org.example.service.impl.*"))
    threads.set(4)
    useClasspathFile.set(true)
    outputFormats.set(listOf("HTML"))
    timestampedReports.set(false)
}