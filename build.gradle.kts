plugins {
    kotlin("jvm") version "1.8.0"
}

group = "com.miya10kei"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("io.micrometer:micrometer-tracing-bom:latest.release"))

    implementation("io.micrometer:micrometer-tracing")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-zipkin:1.22.0")
    implementation("io.zipkin.reporter2:zipkin-sender-urlconnection:2.16.3")

    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("ch.qos.logback:logback-classic:1.4.5")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}