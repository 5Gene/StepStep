plugins {
    alias(vcl.plugins.android.application)
    alias(vcl.plugins.gene.compose)
    alias(vcl.plugins.gene.android)
}

android {
    namespace = "com.spark.stepstep"
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(vcl.google.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(project(":stepstep"))
}