plugins {
	kotlin("jvm") version "2.0.0"
	kotlin("plugin.spring") version "2.0.0"
	kotlin("plugin.jpa") version "2.0.0"
	id("org.springframework.boot") version "3.5.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.vaadin") version "24.8.2"
}

group = "com"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenCentral()
}

extra["vaadinVersion"] = "24.8.2"

dependencies {
	// Kotlin + coroutines
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.0")

	// Spring Boot + MVC + Security + JPA
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// Jackson Kotlin module
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	// Vaadin
	implementation("com.vaadin:vaadin-spring-boot-starter")

	// MyBatis + PostgreSQL
	implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3")
	runtimeOnly("org.postgresql:postgresql")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.security:spring-security-test")

	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")

	implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10")
	implementation("org.xhtmlrenderer:flying-saucer-pdf-openpdf:9.1.22")

}

dependencyManagement {
	imports {
		mavenBom("com.vaadin:vaadin-bom:${property("vaadinVersion")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}