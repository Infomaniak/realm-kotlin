# Infomaniak's fork of [Realm Kotlin](https://github.com/realm/realm-kotlin)

This is a fork of the deprecated [Realm Kotlin](https://github.com/realm/realm-kotlin).
It has been made compatible with Kotlin 2.2.10 thanks to the work of @XilinJia
on their [krdb](https://github.com/XilinJia/krdb) fork (we cherry-picked some of their commits).

We have reversed the rebranding so it can be used as a drop-in replacement with builds published
to a local or private maven repository.

We have checked the diff between our revision and Realm's original project to be exempt from any
suspicious code or reference to unchecked binaries.

Unlike the forked repo, this one can be built from source on Linux.

Project structure is changed to make Intellij IDE work.
gradlew needs to be run from the root directory rather than packages.

## Version compatibility

Version 3.2.9 <==> Kotlin 2.2.10
Version 3.2.8-2 <==> Kotlin 2.2.0

## How to use:

Replace the `io.realm.kotlin` maven group with `com.infomaniak.realm.kotlin`, and use the `3.2.9` version.

It's all published on Maven Central.
You might need to add this at the top of your `settings.gradle[.kts]`:

```.gradle.kts
pluginManagement {
    repositories {
        mavenCentral() // Our Realm fork is published here
        gradlePluginPortal() // To keep access to other plugins
    }
}
```

### Build the project

See the [Releasing guide](RELEASING.md), or the [contributing guide](CONTRIBUTING.md).

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
        classpath("com.infomaniak.realm.kotlin:gradle-plugin:y.y.y")
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
