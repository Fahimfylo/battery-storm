plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    id("application")
}

group = "com.ghost.batterystorm"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

sourceSets {
    val main by getting {
        kotlin.srcDir("src/jvmMain/kotlin")
        resources.srcDir("src/jvmMain/resources")
    }
    val test by getting {
        kotlin.srcDir("src/jvmTest/kotlin")
        resources.srcDir("src/jvmTest/resources")
    }
}

dependencies {
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.23")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.ghost.batterystorm.MainKt")
}

repositories {
    mavenCentral()
}