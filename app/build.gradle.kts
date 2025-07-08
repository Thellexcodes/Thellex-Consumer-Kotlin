plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
    id("com.google.gms.google-services") version "4.4.3" apply false
}

android {
    namespace = "com.thellex.payments"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thellex.payments"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
//            isMinifyEnabled = true
//            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.material.v1110)
    implementation(libs.journeyapps.zxing.android.embedded)
    implementation(libs.material.vlatestversion)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.lifecycle.livedata.ktx.v261)
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v261)
    implementation(libs.kotlinx.datetime)
    implementation (libs.otp.view)
    implementation (libs.imagepicker)
    implementation (libs.journeyapps.zxing.android.embedded)
    implementation(libs.firebase.analytics)

    implementation ("io.socket:socket.io-client:2.0.0") {
        exclude(group = "org.json", module = "json")
    }

    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.ui.text.android)
    implementation(libs.firebase.messaging.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}