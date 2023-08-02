pluginManagement {
    repositories {
        google()
        jcenter()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}


include(":nativeaudio")

include(":psysuitepython")
project(":psysuitepython").projectDir = File(settingsDir, "../modules/psysuitepython/psysuitepython")

include(":psysuitecore")
project(":psysuitecore").projectDir = File(settingsDir, "../modules/psysuitecore/psysuitecore")

include(":core")
project(":core").projectDir = File(settingsDir, "../modules/core/core")

include(":app")
