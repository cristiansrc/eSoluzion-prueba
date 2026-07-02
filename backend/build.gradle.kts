plugins {
    java
    id("org.springframework.boot") version "3.3.+"
    id("io.spring.dependency-management") version "1.1.5"
    id("org.openapi.generator") version "7.6.0"
    jacoco
}

group = "com.esoluzion"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // H2 Database
    runtimeOnly("com.h2database:h2")

    // Flyway
    implementation("org.flywaydb:flyway-core")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    // OpenAPI / Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

// OpenAPI Generator
openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("${rootDir}/../docs/api/pricing-api.yaml")
    apiPackage.set("com.esoluzion.pricing.infrastructure.adapter.in.web.api")
    modelPackage.set("com.esoluzion.pricing.infrastructure.adapter.in.web.dto")
    invokerPackage.set("com.esoluzion.pricing.infrastructure.adapter.in.web")
    outputDir.set("${projectDir}/build/generated")
    configOptions.set(mapOf(
        "interfaceOnly" to "true",
        "useSpringBoot3" to "true",
        "useJakartaEe" to "true",
        "dateLibrary" to "java8-localdatetime",
        "skipDefaultInterface" to "true",
        "useTags" to "true",
        "generateBuilders" to "true",
        "library" to "spring-boot"
    ))
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}

sourceSets {
    main {
        java {
            srcDir("${projectDir}/build/generated/src/main/java")
        }
    }
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

val jacocoExclude = listOf(
    "**/dto/**",
    "**/entity/**",
    "**/model/**",
    "**/config/**",
    "**/exception/**",
    "**/exceptions/**",
    "**/*MapperImpl*",
    "**/*Application*",
    "**/*ApiUtil*",
    "**/*PricesApi*",
    "**/*Handler*",
    "**/*ApiError*"
)

tasks.jacocoTestReport {
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(jacocoExclude)
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.85".toBigDecimal()
            }
        }
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(jacocoExclude)
            }
        })
    )
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
