plugins {
    id("com.android.application") version "9.1.1" apply false
    // Kotlin of the app (AGP 9 compiles Kotlin itself; this sets its version). DocSDK needs 2.3.20 or later.
    id("org.jetbrains.kotlin.android") version "2.3.20" apply false
}
