plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.firebase.crashlytics")
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

    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material) // Keep this, assuming it’s a newer version (e.g., 1.9.0+)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)
        implementation(libs.androidx.annotation)
        implementation(libs.androidx.lifecycle.livedata.ktx.v261)
        implementation(libs.androidx.lifecycle.viewmodel.ktx.v261)
        implementation(libs.androidx.activity)
        implementation(libs.journeyapps.zxing.android.embedded)
        implementation(libs.androidx.datastore.preferences)
        implementation(libs.retrofit)
        implementation(libs.converter.gson)
        implementation(libs.kotlinx.coroutines.android)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.retrofit2.kotlinx.serialization.converter)
        implementation(libs.logging.interceptor)
        implementation(libs.kotlinx.datetime)
        implementation(libs.otp.view)
        implementation(libs.imagepicker)
        implementation(platform(libs.firebase.bom))
        implementation(libs.firebase.analytics)
        implementation(libs.firebase.messaging.ktx)
        implementation(libs.firebase.crashlytics.ndk)
        implementation(libs.androidx.ui.text.android)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        implementation(libs.glide)
        kapt("com.github.bumptech.glide:compiler:4.15.1")
        implementation(libs.materialdatetimepicker)
        implementation("androidx.core:core-splashscreen:1.0.1")
    }}
dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
}
