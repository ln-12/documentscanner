plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "io.github.ln_12.documentscanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.ln_12.documentscanner"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.6.2")
    implementation("androidx.compose.ui:ui-graphics:1.6.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.2")
    implementation("androidx.compose.material3:material3:1.2.0")

    // ML Kit
    // https://developers.google.com/ml-kit/vision/doc-scanner/android
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1")

    // Glide
    // https://github.com/bumptech/glide
    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")

    debugImplementation("androidx.compose.ui:ui-tooling:1.6.2")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.2")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.2")
}