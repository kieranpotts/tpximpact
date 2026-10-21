plugins {
    application
    alias(libs.plugins.kotlin.jvm)
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

val ktlint = configurations.create("ktlint")

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    runtimeOnly(libs.logback.classic)

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
                implementation(libs.testcontainers.junit.jupiter)
                implementation(libs.testcontainers.postgresql)
                runtimeOnly(libs.postgresql)
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
