plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-parcelize") // Keep if you are using Parcelize
}

android {
    namespace = "com.ran.refeed"
    compileSdk = 35 // Stick to 34 for now.

    defaultConfig {
        applicationId = "com.ran.refeed"
        minSdk = 26
        targetSdk = 30  // targetSdk should match compileSdk
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
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Use Java 17
        targetCompatibility = JavaVersion.VERSION_17 // Use Java 17
    }

    kotlinOptions {
        jvmTarget = "17"  // Target JVM 17
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1" // Use 1.5.10 with Kotlin 1.9.22
    }

    packagingOptions { // Corrected: Use packagingOptions
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:19.1.0")
    // Use a central place for versions (libs.versions.toml is BEST, but this works)
    val composeBomVersion = "2024.02.01" //  Latest *STABLE* BOM.  Check for updates!
    val lifecycleVersion = "2.7.0"
    val coroutinesVersion = "1.7.3" // Use a consistent version

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:$composeBomVersion"))

    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")  // Consider updating
    implementation("androidx.activity:activity-compose:1.8.2") // Consider updating

    // Compose (NO versions needed because of the BOM)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7") // Consider updating

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutinesVersion")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.2")) // Use the Firebase BOM
    implementation("com.google.firebase:firebase-auth-ktx") // No versions needed
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Coil
    implementation("io.coil-kt:coil-compose:2.5.0")  // Check for updates

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBomVersion")) // BOM here too
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("com.google.maps.android:maps-compose:6.4.2") // Replace x.y.z!
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    implementation("com.airbnb.android:lottie-compose:6.0.0")
    implementation("androidx.compose.animation:animation:1.5.0")
    // If you want to use Material Motion
    implementation("com.google.accompanist:accompanist-navigation-animation:0.30.1")
    implementation("io.appwrite:sdk-for-android:6.1.0")

}