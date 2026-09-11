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
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")

    testImplementation("io.cucumber:cucumber-java:7.18.0")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.18.0")

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

sonar {
    properties {
        property("sonar.projectKey", "SistemaDeVentas")
        property("sonar.projectName", "Proyecto Demo Calidad")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.token", "sqp_d421ca303eef46eafbabb0d9a580a448ede8402f")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
    }



}

tasks.register<Test>("acceptanceTest") { useJUnitPlatform()
    description = "Runs Cucumber acceptance tests."
    group = "verification"
}

pitest { pitestVersion.set("1.19.0")
    junit5PluginVersion.set("1.2.1")
    targetClasses.set(listOf("org.example.*"))
    targetTests.set(listOf("org.example.model.*"))
    threads.set(4)
    useClasspathFile.set(true)
    outputFormats.set(listOf("HTML"))
    timestampedReports.set(false)
}