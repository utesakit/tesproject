// Gradle build configuration for the server.
plugins {
    // Kotlin plugin for compiling Kotlin code
    kotlin("jvm") version "2.2.21"                      // aktuellste Version

    // Kotlin plugin that enables @Serializable and JSON (de-)serialization support.
    kotlin("plugin.serialization") version "2.2.21"     // identische Version wie oben (muss)

    // Gradle application plugin: lets us define an entry point (main class) and run the server via "gradle run".
    application
}

repositories {
    // Central Maven repository where most JVM libraries are published.
    // Gradle will download all declared dependencies from here.
    mavenCentral()
}

// Centralized version definitions to keep dependency versions in one place.
val ktorVersion = "2.3.13"                      // letzte 2.x-Version
val ktormVersion = "4.1.1"                      // aktuellste Version
val postgresDriverVersion = "42.7.8"            // aktuellste Version
val kotlinxSerializationJsonVersion = "1.9.0"   // aktuellste Version
val logbackVersion = "1.5.21"                   // aktuellste Version
val javaJwtVersion = "4.5.0"                    // aktuellste Version
val jbcryptVersion = "0.4"                      // aktuellste Version

// All external libraries (dependencies) used by the server.
dependencies {
    // Core Ktor server APIs (routing, pipeline, plugins, ...).
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    // Netty-based HTTP server engine used to actually listen on a port.
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    // Integration between Ktor and kotlinx.serialization for JSON handling.
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")

    // Core JSON library for Kotlin serialization (explicit version).
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinxSerializationJsonVersion}")

    // Logback implementation for logging (INFO, DEBUG, ERROR, ...).
    implementation("ch.qos.logback:logback-classic:${logbackVersion}")

    // Ktorm core: lightweight ORM/SQL mapping for working with the database.
    implementation("org.ktorm:ktorm-core:${ktormVersion}")
    // PostgreSQL JDBC driver: enables Java/Kotlin to talk to a PostgreSQL database.
    implementation("org.postgresql:postgresql:${postgresDriverVersion}")

    // Basic Ktor authentication plugin (used as foundation for JWT auth).
    implementation("io.ktor:ktor-server-auth-jvm:${ktorVersion}")
    // Ktor plugin for JWT-based authentication.
    implementation("io.ktor:ktor-server-auth-jwt-jvm:${ktorVersion}")
    // Java JWT library from Auth0: used by TokenService to create/verify tokens.
    implementation("com.auth0:java-jwt:${javaJwtVersion}")

    // BCrypt library for secure password hashing.
    implementation("org.mindrot:jbcrypt:${jbcryptVersion}")

    // Testing
    // testImplementation(kotlin("test"))
    // testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    // testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

application {
    // Gradle uses this as the entry point when running the application.
    mainClass.set("com.tes.MainKt")
}

tasks.test {
    // Use the modern JUnit 5 platform for running tests.
    // TODO: do!
    useJUnitPlatform()
}

kotlin {
    // Configure Kotlin to target Java 21.
    // This must match the JDK version used to build and run the project!
    jvmToolchain(21)                 // beste TLS Version für Kotlin 2.2.21
}