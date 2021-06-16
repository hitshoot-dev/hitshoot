plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.31"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    application
}

repositories {
    // Use Maven Central for resolving dependencies
    mavenCentral()
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // DB dependencies
    implementation("org.flywaydb:flyway-core:7.10.0")

    // Vert.x dependencies
    implementation("io.vertx:vertx-lang-kotlin:4.1.0")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.1.0")
    implementation("io.vertx:vertx-core:4.1.0")
    implementation("io.vertx:vertx-web:4.1.0")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.1.0")
    implementation("io.vertx:vertx-web-client:4.1.0")

    // Database support might be added in the future
    //implementation("io.vertx:vertx-pg-client:4.1.0")
    //implementation("io.vertx:vertx-sql-client-templates:4.1.0")
    //implementation("io.vertx:vertx-codegen:4.1.0")
    //annotationProcessor("io.vertx:vertx-codegen:4.1.0:processor")

    // Misc dependencies
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation("de.mkammerer:argon2-jvm:2.10.1")
    implementation("org.yaml:snakeyaml:1.29")
    implementation("net.sourceforge.argparse4j:argparse4j:0.9.0")
    implementation("net.termer.vertx.kotlin.validation:vertx-web-validator-kotlin:1.0.1")
    implementation("com.github.jknack:handlebars:4.2.0")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")

}

/* APPLICATION CONFIGURATION */
application {
    // Define the main class for the application
    mainClass.set("hitshoot.AppKt")
}
tasks.named("build") {
    dependsOn("shadowJar")
}