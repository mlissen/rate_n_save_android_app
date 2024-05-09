plugins {
    id("com.android.application") // Use 'id' to apply plugins
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.ratensaveandroidapp"
    compileSdk = 34

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    defaultConfig {
        applicationId = "com.example.ratensaveandroidapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }


    buildTypes {

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation (libs.logging.interceptor)
    implementation ("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06") // Or the latest stable version
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation(libs.androidx.media3.common)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.media3:media3-exoplayer:1.3.1")
    implementation ("androidx.media3:media3-ui:1.3.1")
    implementation ("androidx.media3:media3-exoplayer-dash:1.3.1")
    implementation(libs.material.components)
    implementation ("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation ("androidx.window:window:1.0.0")
    implementation(libs.zxing)
    implementation(libs.appcompatactivity)
    implementation(libs.lifecyclelivedataktx)
    implementation(libs.retrofit2)
    implementation(libs.retrofit2GsonConverter)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.security:security-crypto:1.1.0-alpha03")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.media3:media3-exoplayer:1.3.0")
    implementation("androidx.media3:media3-ui:1.3.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.0")
}