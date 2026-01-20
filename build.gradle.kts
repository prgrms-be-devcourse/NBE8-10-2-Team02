plugins {
	java
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "1.9.0"
	kotlin("plugin.spring") version "1.9.0"
	kotlin("plugin.jpa") version "1.9.0"
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

//    implementation("org.springframework.boot:spring-boot-starter-security")
//    testImplementation("org.springframework.boot:spring-boot-starter-security-test")

    implementation("org.springframework.boot:spring-boot-starter-restclient")

	// QueryDSL
	implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
	annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")

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


