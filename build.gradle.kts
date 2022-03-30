import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    // ktlint
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("org.jlleitschuh.gradle.ktlint-idea") version "10.0.0"

    kotlin("jvm") version "1.5.21"
    kotlin("plugin.spring") version "1.5.21"
    kotlin("plugin.jpa") version "1.5.21"
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    ignoreFailures.set(true)
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

noArg {
    annotation("javax.persistence.Entity")
}

group = "com.yapp"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.springfox:springfox-boot-starter:3.0.0")

    // Spring Boot actuator, admin
    implementation("org.springframework.boot:spring-boot-starter-actuator:2.3.1.RELEASE")
    implementation("de.codecentric:spring-boot-admin-client:2.3.1")

    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.2")
    runtimeOnly("org.postgresql:postgresql")

    implementation("com.google.firebase:firebase-admin:6.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.2.2")

    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.11.228"))
    implementation("com.amazonaws:aws-java-sdk-s3")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito")
    }
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("com.ninja-squad:springmockk:2.0.3")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

/**
 * Gradle에서는 기본적으로 하나의 source 디렉토리만 지원(src/main/java)
 * 따라서 sourceSets 설정을 통해 여러 source 디렉토리 지원
 */
sourceSets {
    main {
        resources {
            srcDirs(listOf("src/main/resources", "src/main/resources/profiles"))
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("com.yapp.web2.BookmarkersApplicationKt")
}