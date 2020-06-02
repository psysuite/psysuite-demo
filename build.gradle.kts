// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlin_version by extra("1.3.72")
    repositories {
        google()
        jcenter()
        maven {
            url = uri("https://dl.bintray.com/kyonifer/maven")
        }
        
    }
    // NOTE: Do not place your application dependencies here; they belong in the individual module build.gradle files
    dependencies {
        classpath(ClassPaths.gradlePlugin)
        classpath(ClassPaths.kotlinPlugin)
        classpath(ClassPaths.navSafeArgsGradlePlugin)
        "classpath"("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url = uri("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}
