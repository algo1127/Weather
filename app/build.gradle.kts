plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.algo1127.weather"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.algo1127.weather"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "2026-06-13A"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // === WEATHER APP BACKEND DEPENDENCIES ===

    // 1. Retrofit + OkHttp (Network calls to AEMET)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // 2. Moshi (JSON parsing - converts AEMET JSON to Kotlin objects)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.retrofit.converter.moshi)

    // 3. Room (Local database for caching weather data)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)


    // 4. Coroutines (Async operations - fetching data without freezing UI)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation(libs.play.services.location)

    // 5. ViewModel + LiveData (UI data holders - keeps data alive during rotation)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2")

    // Testing (keep these)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")


    ksp(libs.moshi.kotlin.codegen) // <--- ADD THIS LINE!
    ksp(libs.androidx.room.compiler) // <--- ADD THIS LINE RIGHT HERE!
    implementation("com.airbnb.android:lottie:6.4.0")
}