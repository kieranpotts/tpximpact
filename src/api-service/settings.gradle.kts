plugins {
    // Downloads the JDK named by the build's toolchain, when the machine
    // running Gradle doesn't already have it.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "api-service"
