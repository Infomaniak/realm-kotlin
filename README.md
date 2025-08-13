# Infomaniak's fork of [Realm Kotlin](https://github.com/realm/realm-kotlin)

This is a fork of the deprecated [Realm Kotlin](https://github.com/realm/realm-kotlin).
It has been made compatible with Kotlin 2.2.0 thanks to the work of @XilinJia
on their [krdb](https://github.com/XilinJia/krdb) fork (we cherry-picked some of their commits).

We have reversed the rebranding so it can be used as a drop-in replacement with builds published
to a local or private maven repository.

We have checked the diff between our revision and Realm's original project to be exempt from any
suspicious code or reference to unchecked binaries.

Unlike the forked repo, this one can be built from source on Linux.

Project structure is changed to make Intellij IDE work.
gradlew needs to be run from the root directory rather than packages.

## Version compatibility

Version 3.2.8 <==> Kotlin 2.2.0

## How to use:

This project is NOT YET published to a maven repo.

### Build the project

#### Prerequisites

- Swig 4.2.0 or above. On Mac this can be installed using Homebrew: `brew install swig`.
- Ccache. On Mac this can be installed using Homebrew: `brew install ccache`.
- CMake 3.18.1 or above. Can be installed through the Android SDK Manager, or homebrew.
- Java 17 or above.
- Define environment variables:
    - `ANDROID_HOME`
    - `JAVA_HOME`
    - `NDK_HOME`
- clone this project **with submodules** (don't skip the `--recursive` option for that):
  ```git clone --recursive https://github.com/Infomaniak/realm-kotlin.git ```

#### Gradle commands

```
./gradlew clean
./gradlew jvmTest
./gradlew publishToMavenLocal 
```

### Setup the repository

* in project's `settings.gradle[.kts]`, add:
```
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        // Other repos...
        mavenLocal() // <--- 👈 Add this.
    }
}

dependencyResolutionManagement {
    // repositoriesMode...
    repositories {
        // Other repos...
        mavenLocal() // <--- 👈 Add this.
    }
}
```

* in project's `build.gradle[.kts]`, add:
```
buildscript {
    dependencies {
        classpath("io.realm.kotlin:gradle-plugin:y.y.y")
    }
}
```
* remove the version in the declarations in the `plugins` blocks
* and of course, change your Kotlin to 2.x.y (refer to Version compatibility)

------------------------------------

Original Readme of Realm-Kotlin can be found [here](https://github.com/realm/realm-kotlin)

# Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details!


# License

Realm Kotlin is published under the [Apache 2.0 license](LICENSE).
