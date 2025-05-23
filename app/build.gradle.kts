plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}



android {
    namespace = "com.example.plife"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.plife"
        minSdk = 24
        targetSdk = 33
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
    buildFeatures {
        viewBinding = true
    }
}



dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database-ktx:20.2.2")
    implementation("com.google.firebase:firebase-auth:22.1.2")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-auth-ktx:22.1.2")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.firebase:firebase-storage-ktx:20.2.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.3.1")
    implementation("com.google.firebase:firebase-database:20.2.2")

    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.firebase:firebase-messaging:23.4.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    implementation ("com.sun.mail:android-mail:1.6.5")
    implementation ("com.sun.mail:android-activation:1.6.5")

    implementation ("com.squareup.picasso:picasso:2.8")



    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")




}