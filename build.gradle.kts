import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.2"
    id("org.graalvm.buildtools.native") version "0.9.27"
    kotlin("jvm") version "1.9.20-RC"
    kotlin("plugin.spring") version "1.9.20-RC"
}

group = "dev.lutergs"
version = "0.2.4"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

repositories {
    mavenCentral()
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // jackson datatype
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // mongoDB reactive
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    // oracle reactive
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("com.oracle.database.r2dbc:oracle-r2dbc:1.1.1")
    implementation("io.r2dbc:r2dbc-pool:1.0.1.RELEASE")
    implementation("io.r2dbc:r2dbc-spi:1.0.0.RELEASE")

    // MD5 hashing
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")

    // AWS SDK
    implementation(platform("software.amazon.awssdk:bom:2.20.154"))
    implementation("software.amazon.awssdk:s3")

    // JWT
    implementation("com.nimbusds:nimbus-jose-jwt:9.31")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.75")
    // TODO : jjwt 1.0 부터는 JWE 를 사용하므로, 확인 후 변경할 것

    // Web-push
    implementation("nl.martijndwars:web-push:5.1.1")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val graalvmReflectionTest: TaskProvider<Test> = tasks.register("graalvm-reflection-test", Test::class.java) {
    description = "GraalVM reflection test. Run this test to collect metadata"
    useJUnitPlatform()
    filter {
        includeTestsMatching("dev.lutergs.lutergsbackend.graalVM.*")
    }
}

graalvmNative {
    binaries.all {
        resources.autodetect()
    }
    binaries {
        named("main") {
            // this class should be initialized at build time to avoid error
            buildArgs.add("--initialize-at-build-time=org.apache.commons.logging.LogFactoryService")

            // java -agentlib:native-image-agent=config-output-dir=./src/main/resources/META-INF/native-image -jar build/libs/lutergs-backend-0.1.0.jar
            // 위 명령어를 통해, reflection 등의 설정을 자동으로 기록할 수 있음.
        }
    }
    toolchainDetection.set(false)
}
