plugins {
    id(Plugins.androidApplication)
    kotlin(Plugins.kotlinAndroid)
    kotlin(Plugins.kotlinExtensions)

    id("name.remal.check-dependency-updates") version "1.0.192"
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


}

androidExtensions {
    isExperimental = true
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(Dependencies.Kotlin.stdLib)

    implementation(Dependencies.AndroidX.ktxCore)
    implementation(Dependencies.AndroidX.navFragment)
    implementation(Dependencies.AndroidX.navUi)

    implementation("androidx.appcompat:appcompat:1.3.0-alpha01")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("com.google.android.material:material:1.1.0")

    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("com.jakewharton.rxrelay2:rxrelay:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")

    implementation("org.ejml:ejml-kotlin:0.39")
    implementation(files( "jvm/koma-core-api-jvm-0.12.jar", "jvm/koma-core-ejml-0.12.jar"))

    implementation(Dependencies.Moshi.moshi)
    implementation(Dependencies.Moshi.moshiKt)

    implementation("com.intentfilter:android-permissions:2.0.54")
    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}