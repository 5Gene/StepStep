plugins {
    alias(vcl.plugins.android.application)
    alias(vcl.plugins.gene.compose)
    alias(vcl.plugins.gene.android)
}

android {
    namespace = "com.spark.stepstep"
}

dependencies {
    implementation(vcl.google.material)
}