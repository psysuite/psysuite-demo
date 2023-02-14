import java.io.FileInputStream
import java.util.*

plugins {
    id(Plugins.androidApplication)
    kotlin(Plugins.kotlinAndroid)
    kotlin(Plugins.kotlinExtensions)
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
    compileSdkVersion(Configs.compileSdkVersion)

    defaultConfig {


        configurations.all {
            resolutionStrategy { force("androidx.core:core-ktx:1.7.0") }
        }


        applicationId = Configs.applicationId
        minSdkVersion(Configs.minSdkVersion)
        targetSdkVersion(Configs.targetSdkVersion)
        versionCode = Configs.versionCode
        versionName = Configs.versionName
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    packagingOptions {
        pickFirst("META-INF/NOTICE*")
        pickFirst("META-INF/LICENSE*")
    }

    testOptions {
        // include the android resources in local tests. See http://robolectric.org/migrating/#migrating-to-40
        unitTests.isIncludeAndroidResources = true
    }
}

androidExtensions {
    isExperimental = true
}

dependencies {

    implementation(project(":core"))
    implementation(project(":psysuitecore"))

    implementation("com.intentfilter:android-permissions:2.0.54")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.fragment:fragment:1.5.3")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.preference:preference:1.2.0")

    // added to prevent double class definition 19/10/2022
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")


    debugImplementation ("androidx.fragment:fragment-testing:1.1.0"){
        exclude("androidx.test", "core-ktx")
    }
    debugImplementation("androidx.test:core-ktx:1.4.0")

//    testImplementation("junit:junit:4.13.2")
//    testImplementation("org.robolectric:robolectric:4.6.1")
//    testImplementation("androidx.test:core:1.4.0")
//    testImplementation("androidx.test:runner:1.4.0")
//    testImplementation("androidx.test.ext:junit:1.1.3")
//    testImplementation("androidx.test.ext:truth:1.4.0")
//    testImplementation("androidx.test.espresso:espresso-core:3.4.0")
//    testImplementation("androidx.test.espresso:espresso-intents:3.4.0")
//    testImplementation("org.mockito:mockito-core:4.0.0")
//    androidTestImplementation("androidx.navigation:navigation-testing:${Versions.navVersion}")
//    androidTestImplementation("org.mockito:mockito-android:4.0.0")
//    androidTestImplementation("androidx.test:core:1.4.0")
//    androidTestImplementation("androidx.test:runner:1.4.0")
//    androidTestImplementation("androidx.test:rules:1.4.0")
//    androidTestImplementation("androidx.test.ext:junit:1.1.3")
//    androidTestImplementation("androidx.test.ext:truth:1.4.0")
//    androidTestImplementation("com.google.truth:truth:1.1.3")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
//    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.4.0")
//    androidTestImplementation("androidx.test.espresso:espresso-intents:3.4.0")
//    androidTestImplementation("androidx.test.espresso.idling:idling-concurrent:3.4.0")
//    androidTestImplementation("org.robolectric:annotations:4.6.1")

    // The following Espresso dependency can be either "implementation" or "androidTestImplementation", depending on whether you want the
    // dependency to appear on your APK's compile classpath or the test APK classpath.
//    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.4.0")
}