plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.tpximpact.urlshortener"

// Test code lives outside this build's directory, so it never reaches the
// image. See: docs > development > structure.
val testsDir = layout.projectDirectory.dir("../../tests/api-service")

kotlin {
    jvmToolchain(
        libs.versions.java
            .get()
            .toInt(),
    )
    compilerOptions {
        allWarningsAsErrors = true
    }
}

application {
    mainClass = "com.tpximpact.urlshortener.ApplicationKt"
}

// The contract is bundled into the service, and served at `GET /openapi.yaml`.
// See: docs > decisions > 0013.
val contract = layout.projectDirectory.file("contract/openapi.yaml")

tasks.processResources {
    from(contract) { into("contract") }
}

val ktlint = configurations.create("ktlint")

dependencies {
    implementation(libs.flyway.core)
    implementation(libs.hikaricp)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.status.pages)
    runtimeOnly(libs.flyway.database.postgresql)
    runtimeOnly(libs.logback.classic)
    runtimeOnly(libs.postgresql)

    ktlint(libs.ktlint.cli) {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

testing {
    suites {
        // Fast, in-memory tests. No Docker needed.
        val test = named<JvmTestSuite>("test")
        test {
            useJUnitJupiter(libs.versions.junit)
            sources {
                kotlin.setSrcDirs(listOf(testsDir.dir("unit/kotlin")))
                resources.setSrcDirs(listOf(testsDir.dir("unit/resources")))
            }
            dependencies {
                implementation(libs.ktor.server.test.host)
                implementation(libs.openapi.validator)
            }
            targets.all {
                testTask.configure {
                    // HTTP tests validate responses against the source file,
                    // not the copy bundled into the service.
                    systemProperty("contract.path", contract.asFile.absolutePath)
                }
            }
        }

        // Tests against real PostgreSQL, in Testcontainers. Needs Docker.
        register<JvmTestSuite>("integrationTest") {
            useJUnitJupiter(libs.versions.junit)
            sources {
                kotlin.setSrcDirs(listOf(testsDir.dir("integration/kotlin")))
                resources.setSrcDirs(listOf(testsDir.dir("integration/resources")))
            }
            dependencies {
                implementation(project())
                implementation(libs.flyway.core)
                implementation(libs.hikaricp)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.testcontainers.junit.jupiter)
                implementation(libs.testcontainers.postgresql)
            }
            targets.all {
                testTask.configure {
                    shouldRunAfter(test)
                    // Testcontainers loads a native library, via JNA.
                    jvmArgs("--enable-native-access=ALL-UNNAMED")
                }
            }
        }
    }
}

// Gradle prints nothing for passing tests by default. Show each one, so the
// output of `make test` proves what ran.
tasks.withType<Test>().configureEach {
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
}

// ktlint runs from its CLI, not a plugin, because the plugins only format
// files inside this build's directory, and the tests are outside it. Style
// rules are configured in the root `.editorconfig`.
val ktlintPatterns =
    listOf(
        "src/**/*.kt",
        "*.kts",
        testsDir.asFile.absolutePath + "/**/*.kt",
    )

tasks.withType<JavaExec>().matching { it.name.startsWith("ktlint") }.configureEach {
    classpath = ktlint
    mainClass = "com.pinterest.ktlint.Main"
    // ktlint embeds a Kotlin compiler that still uses `sun.misc.Unsafe`.
    jvmArgs("--sun-misc-unsafe-memory-access=allow")
}

tasks.register<JavaExec>("ktlintCheck") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Checks Kotlin code style."
    args(ktlintPatterns)
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Fixes Kotlin code style."
    args(listOf("--format") + ktlintPatterns)
}
