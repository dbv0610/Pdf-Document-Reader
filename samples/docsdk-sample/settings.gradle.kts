pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // DocSDK: the folder published by ./gradlew :android_document:publishReleasePublicationToSdkRepository.
        // An integrator puts the URL of the repository they were given here instead.
        maven(url = uri("../../sdk-repo"))
    }
}
rootProject.name = "DocSDK Sample"
include(":app")
