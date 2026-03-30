import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)

    //Google services plugin
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

val tmdbApiKey = (
    localProperties.getProperty("TMDB_API_KEY")
        ?: System.getenv("TMDB_API_KEY")
        ?: ""
    )
    .trim()
    .removeSurrounding("\"")
    .replace("\"", "\\\"")

android {
    namespace = "com.piggylabs.nexscene"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.piggylabs.nexscene"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    //Navigation and Material-Design
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.6")

    // Ktor Client Dependencies
    implementation("io.ktor:ktor-client-core:3.4.1")
    implementation("io.ktor:ktor-client-android:3.4.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.1")
    implementation("io.ktor:ktor-client-logging:3.4.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.1")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    annotationProcessor("androidx.room:room-compiler:2.7.0")

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))

    // Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.4.0")


    //Gson
    implementation("com.google.http-client:google-http-client-gson:1.43.3")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
