plugins {
	java
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "2.1.0"
	kotlin("plugin.spring") version "2.1.0"
	kotlin("plugin.jpa") version "2.1.0"
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
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
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")

	developmentOnly("org.springframework.boot:spring-boot-devtools")

	runtimeOnly("com.h2database:h2")
    developmentOnly("org.springframework.boot:spring-boot-h2console")

    compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")

	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")

	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")

    implementation("org.springframework.boot:spring-boot-starter-restclient")

	// 1. QueryDSL 라이브러리
	implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")

	// 2. QClass 생성을 위한 핵심 엔진 (이 3개가 세트입니다)
	annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
	annotationProcessor("jakarta.persistence:jakarta.persistence-api")
	annotationProcessor("jakarta.annotation:jakarta.annotation-api")

    //caffeine
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

}

tasks.withType<Test> {
	useJUnitPlatform()
}


sourceSets {
	main {
		java {
			srcDirs("build/generated/sources/annotationProcessor/java/main")
		}
	}
}


