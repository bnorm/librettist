import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

group = "dev.bnorm.storyboard"

kotlin {
    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        all {
            languageSettings {
                optIn("androidx.compose.animation.core.ExperimentalTransitionApi")
                optIn("androidx.compose.animation.ExperimentalAnimationApi")
                optIn("androidx.compose.animation.ExperimentalSharedTransitionApi")
            }
        }

        commonMain {
            dependencies {
                api(project(":storyboard-core"))
                api(compose.ui)
                api(compose.material)

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
            }
        }

        jvmMain {
            dependencies {
                implementation("org.apache.pdfbox:pdfbox:3.0.1")
            }
        }
    }
}
