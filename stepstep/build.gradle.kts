plugins {
    alias(vcl.plugins.android.library)
    alias(vcl.plugins.gene.compose)
    alias(vcl.plugins.gene.android)
}

android {
    namespace = "org.spark.stepstep"
}

dependencies {
    implementation(vcl.google.material)
    implementation(vcl.kotlinx.coroutines.android)
    implementation(vcl.androidx.annotation)
}