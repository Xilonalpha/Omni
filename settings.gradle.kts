pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://dl.google.com/dl/android/maven2/") }
    }
}

rootProject.name = "OmniscientScanner"
include(":app")
include(":planet_scanner_core")
include(":scios_marrow") // NEW: THE ELITE INTELLECTUAL PROPERTY MODULE
