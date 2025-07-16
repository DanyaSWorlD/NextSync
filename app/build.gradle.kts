plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("io.objectbox")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.next.sync"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.next.sync"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "0.0.2"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
}

kapt {
    correctErrorTypes = true
    useBuildCache = false
    javacOptions {
        option("-source", "17")
        option("-target", "17")
    }
}

val workVersion = "2.10.2"
val hiltVersion = "2.56.2"
val lifecycleVersion = "2.9.1"
val navigationVersion = "2.9.1"
dependencies {
    // AndroidX Core & AppCompat (Foundation)
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1") // For older view-based components if any

    // UI - Google Material Components (for XML views, if used) & ConstraintLayout
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // UI - Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2025.06.01")) // BOM for consistent versions
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")                 // Material Design 3
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Lifecycle & ViewModel (Android Architecture Components)
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion") // For Fragment-based navigation
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")       // For Fragment-based navigation UI helpers
    implementation("androidx.navigation:navigation-compose:$navigationVersion")      // For Jetpack Compose Navigation

    // Dependency Injection - Hilt
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0") // Hilt integration for Compose Navigation
    implementation("androidx.hilt:hilt-work:1.2.0")               // Hilt integration for WorkManager

    // Background Processing - WorkManager
    implementation("androidx.work:work-runtime-ktx:$workVersion")
    implementation("androidx.work:work-rxjava2:$workVersion")     // If you use RxJava with WorkManager
    implementation("androidx.work:work-gcm:$workVersion")         // For GCMNetworkManager compatibility (less common now)
    implementation("androidx.work:work-multiprocess:$workVersion")

    // Data Persistence - DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Networking & External Libraries
    implementation("io.coil-kt:coil-compose:2.7.0") // Image Loading
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0") // JSON Serialization

    // Nextcloud Integration
    implementation("com.github.nextcloud:android-library:2.18.0") {
        exclude(group = "org.ogce", module = "xpp3")
    }
    implementation("commons-httpclient:commons-httpclient:3.1@jar") // Consider if this can be replaced or updated

    // Permissions
    implementation("com.github.getActivity:XXPermissions:25.0")

    // Google Play Services
    implementation("com.google.android.play:review-ktx:2.0.2") // In-App Review

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.work:work-testing:$workVersion")
}