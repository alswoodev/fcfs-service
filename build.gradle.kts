val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks

bootJar.enabled = false

plugins {
	java
	id("org.springframework.boot") version "3.5.11"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.fcfs"
version = "0.0.1-SNAPSHOT"
description = "FCFS coupon system"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations{
	compileOnly{
		extendsFrom(configurations.annotationProcessor.get())
	}
}


repositories {
	mavenCentral()
}

subprojects{
	apply(plugin="java")
	apply(plugin="io.spring.dependency-management")
	apply(plugin="org.springframework.boot")

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
		implementation("org.springframework.boot:spring-boot-starter-data-redis")
		compileOnly("org.projectlombok:lombok")
		runtimeOnly("com.h2database:h2")
		runtimeOnly("com.mysql:mysql-connector-j")
		implementation("org.springframework.boot:spring-boot-starter")
		
		testImplementation("org.springframework.boot:spring-boot-starter-test")
		testAnnotationProcessor ("org.projectlombok:lombok")
		testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
