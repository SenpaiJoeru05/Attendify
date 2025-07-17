plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // Add this line
}

android {
    namespace = "com.example.attendacesystem"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.attendacesystem"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11 // Change this line
        targetCompatibility = JavaVersion.VERSION_11 // And this line
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
    ndkVersion = "26.1.10909125"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.core.ktx.v1120)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0") // For ViewModel
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0") // For LiveData
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7") // For Navigation Component
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.airbnb.android:lottie:6.1.0")
    implementation("androidx.camera:camera-camera2:1.3.2")
    implementation("androidx.camera:camera-lifecycle:1.3.2")
    implementation("androidx.camera:camera-view:1.3.2")
    implementation("com.google.mlkit:face-detection:16.1.7")

    // TFLite
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0") // optional

    // Room DB
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

}