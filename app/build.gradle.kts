import java.io.FileInputStream
import java.util.*

plugins {
    id(Plugins.androidApplication)
    id(Plugins.kotlinAndroid)
    id("kotlin-parcelize")
}

android {

    signingConfigs {

        val folder = ".signing"
        val props = Properties()
        try {
            props.load(FileInputStream(rootProject.file("${folder}/password.properties")))
        } catch (e: java.io.IOException) {
            println("File con password non esiste. non puoi continuare, $e")
        }

        create("release") {
            storeFile       = file("../${folder}/psysuite_keystore.jks")
            keyAlias        = props.getProperty("keyAlias") //"psysuite_key"
            storePassword   = props.getProperty("storePassword") //"qwas_QWAS_72"
            keyPassword     = props.getProperty("keyPassword") //"qwas_QWAS_72"
        }
    }

    namespace = Configs.applicationId
    compileSdkVersion(Configs.compileSdkVersion)
    defaultConfig {

        applicationId = Configs.applicationId
        versionCode = Configs.versionCode
        versionName = Configs.versionName

        minSdkVersion(Configs.minSdkVersion)
        targetSdkVersion(Configs.targetSdkVersion)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(getDefaultProguardFile(ProGuards.proguardTxt), ProGuards.androidDefault)
            signingConfig = signingConfigs.getByName("release")
        }

        getByName("debug") {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(project(":core"))
    implementation(project(":psysuitepython"))
    implementation(project(":psysuitecore"))

    implementation(Dependencies.permissions)
    implementation(Dependencies.AndroidX.legacy_support)
    implementation(Dependencies.AndroidX.fragment)
    implementation(Dependencies.AndroidX.livecycleviewmodel)
    implementation("androidx.test:monitor:1.7.1")
    implementation("androidx.test.ext:junit-ktx:1.2.1")
    implementation("androidx.navigation:navigation-testing:2.7.7")
}