plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.israel.planpilot"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.israel.planpilot"
        minSdk = 30
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.window:window:1.3.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.asynclayoutinflater:asynclayoutinflater:1.0.0")
    implementation("com.jakewharton.threetenabp:threetenabp:1.3.1")
    implementation("androidx.paging:paging-runtime-ktx:3.3.0")
    implementation("com.wdullaer:materialdatetimepicker:4.2.3")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.firebase:firebase-firestore-ktx:25.0.0")
    implementation("androidx.compose.ui:ui:1.6.8")
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("com.jakewharton.threetenabp:threetenabp:1.3.1")
    implementation("androidx.paging:paging-runtime-ktx:3.3.0")
    implementation("androidx.compose.material:material:1.6.8")
    implementation("androidx.compose.runtime:runtime:1.6.8")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    implementation("com.google.android.libraries.places:places:3.5.0")
    implementation("androidx.room:room-ktx:2.6.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}