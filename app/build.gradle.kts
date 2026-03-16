import java.io.FileInputStream
import java.util.Properties

// Read from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

// Helper function to get properties with fallbacks
fun getLocalProperty(key: String, defaultValue: String = ""): String {
    return localProperties.getProperty(key) ?: System.getenv(key) ?: defaultValue
}

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
            keyAlias        = props.getProperty("keyAlias")
            storePassword   = props.getProperty("storePassword")
            keyPassword     = props.getProperty("keyPassword")
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
            buildConfigField("String", "API_URL", "\"${getLocalProperty("PSYSUITE_API_URL_RELEASE", "https://your-server.com/api")}\"")
            buildConfigField("String", "API_KEY", "\"${getLocalProperty("PSYSUITE_API_KEY_RELEASE", "release-key-not-configured")}\"")

        }

        getByName("debug") {
            isDebuggable = true
            buildConfigField("String", "API_URL", "\"${getLocalProperty("PSYSUITE_API_URL_DEBUG", "http://localhost:5000/api")}\"")
            buildConfigField("String", "API_KEY", "\"${getLocalProperty("PSYSUITE_API_KEY_DEBUG", "debug-key-not-configured")}\"")
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

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    
    // Exclude problematic dependencies
    configurations.all {
        exclude(group = "androidx.profileinstaller", module = "profileinstaller")
    }
}