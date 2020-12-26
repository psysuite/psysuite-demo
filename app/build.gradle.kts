plugins {
    id(Plugins.androidApplication)
    kotlin(Plugins.kotlinAndroid)
    kotlin(Plugins.kotlinExtensions)
    id("name.remal.check-dependency-updates") version "1.0.211"
}

android {

    compileSdkVersion(Configs.compileSdkVersion)

    defaultConfig {
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
            proguardFiles(getDefaultProguardFile(ProGuards.proguardTxt), ProGuards.androidDefault)        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
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

    implementation("com.intentfilter:android-permissions:2.0.54")

    implementation(project(":core"))
    implementation(project(":psysuitecore"))
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    testImplementation("junit:junit:4.13")
    testImplementation("org.robolectric:robolectric:4.2")
    testImplementation("androidx.test:core:1.3.0")
    testImplementation("androidx.test:runner:1.3.0")
    testImplementation("androidx.test.ext:junit:1.1.2")
    testImplementation("androidx.test.ext:truth:1.3.0")
    testImplementation("androidx.test.espresso:espresso-core:3.3.0")
    testImplementation("androidx.test.espresso:espresso-intents:3.3.0")

//    debugImplementation("androidx.fragment:fragment-testing:${Versions.fragment}")
//    androidTestImplementation ("androidx.fragment:fragment-testing:${Versions.fragment}")

    androidTestImplementation("androidx.navigation:navigation-testing:${Versions.navVersion}")

    implementation("androidx.fragment:fragment:1.1.0")
    debugImplementation ("androidx.fragment:fragment-testing:1.1.0"){
        exclude("androidx.test", "core-ktx")
    }
    debugImplementation("androidx.test:core-ktx:1.1.0")

    testImplementation("org.mockito:mockito-core:2.24.5")
    androidTestImplementation("org.mockito:mockito-android:2.24.5")

    // Core library
    androidTestImplementation("androidx.test:core:1.1.0")

    // AndroidJUnitRunner and JUnit Rules
    androidTestImplementation("androidx.test:runner:1.1.0")
    androidTestImplementation("androidx.test:rules:1.1.0")

    
    // Assertions
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.ext:truth:1.1.0")
    androidTestImplementation("com.google.truth:truth:1.0")

    // Espresso dependencies
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.1.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.1.0")
    androidTestImplementation("androidx.test.espresso.idling:idling-concurrent:3.1.0")

    // The following Espresso dependency can be either "implementation" or "androidTestImplementation", depending on whether you want the
    // dependency to appear on your APK's compile classpath or the test APK classpath.
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.1.0")

    androidTestImplementation("org.robolectric:annotations:4.4")
}