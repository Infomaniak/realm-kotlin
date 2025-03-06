### This is a fork and rebranding of [Realm Kotlin](https://github.com/XilinJia/realm-kotlin) which is a fork of the deprecated [Realm Kotlin](https://github.com/realm/realm-kotlin).  This has been made compatible with Kotlin 2.1.x while updating various dependencies

JVM has been tested to work the same as with builds with Kotlin 2.0.x.  Android is being tested.  Testing iOS and MacOS will depend on other contributors.

Unlike the forked repo, this one can be built from source on Linux.

Project structure is changed to make Intellij IDE work.  gradlew needs to be run from the root directory rather than packages.

Testing are performed on the dev versions. Maven artifacts can be published (tested locally), but can not be tested upon yet.

### Published to Maven Central, being tested

### To use this with a local build (3.2.4 is being tested, use 3.2.3 instead)

* clone this project with: 
```git clone --recursive https://github.com/XilinJia/krdb.git ```
* build in the project root directory with: ```./gradlew publishToMavenLocal ```
* in Android project, in settings.gradle at the project level, add in the beginning:
```
pluginManagement {
    repositories {
        mavenLocal() // 👈 Add this line
        google()
        mavenCentral()
    }
}
```
* in project build.gradle, add:
```
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath "io.github.xilinjia.krdb:gradle-plugin:3.2.4"
    }
}
allprojects {
    repositories {
        mavenLocal()
    }
}
```
and remove:
```
id 'io.realm.kotlin' version 'x.x.x' apply false
```
* in the app build.gradle

in the plugins block, replace:
```id 'io.realm.kotlin' ```
with
```    id 'io.github.xilinjia.krdb' ```

replace:
```implementation "io.realm.kotlin:library-base:x.x.x" ```
with:
```implementation "io.github.xilinjia.krdb:library-base:3.2.4" ```

and replace:
```apply plugin: "io.realm.kotlin" ```
with
```apply plugin: "io.github.xilinjia.krdb" ```

* in all kotlin files, replace "io.realm.kotlin" with "io.github.xilinjia.krdb"
* and of course, change your Kotlin to 2.1.10

------------------------------------

Original Readme of Realm-Kotlin can be referenced [here](https://github.com/realm/realm-kotlin)

# Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details!


# License

Realm Kotlin is published under the [Apache 2.0 license](LICENSE).

This product is not being made available to any person located in Cuba, Iran, North Korea, Sudan, Syria or the Crimea region, or to any other person that is not eligible to receive the product under U.S. law.

<img style="width: 0px; height: 0px;" src="https://3eaz4mshcd.execute-api.us-east-1.amazonaws.com/prod?s=https://github.com/realm/realm-kotlin#README.md">
