# Releasing

_The original publishing process of Realm was very non-standard, and relied on uploading files
to AWS and other stuff._

For this updated fork, we're trying to keep things simple, without compromising on security.
Despite that goal, we still need a guide for releases.

## **IMPORTANT WARNING:**

DO NOT RUN THE COMMANDS BELOW USING THE DOUBLE RUN ARROW.
It would lead to previous commands to be run, quite a pitfall…

COPY THE COMMANDS into the terminal instead.

## Prerequisites

- Swig 4.2.0 or above. On Mac this can be installed using Homebrew: `brew install swig`.
- Ccache. On Mac this can be installed using Homebrew: `brew install ccache`.
- CMake 3.18.1 or above. Can be installed through Homebrew too: `brew install cmake`.
- Java 17 or above.
- Properly defined environment variables:
    - `ANDROID_HOME`
    - `JAVA_HOME`
    - `NDK_HOME`

## (1/5) Make sure no Gradle daemon is alive

**Why?** Because for some reason, the Gradle daemons started by Android Studio
(and possibly IntelliJ IDEA too) are likely to not pick up the proper environment variables that
are needed to have CMake work (which this project relies on).

### Quit all Gradle daemons

On macOS (which is required to publish this KMP project for all targets, including iOS and macOS),
you can open the **Activity Monitor**, type "java" in the search bar, select them all, and quit
them, or force quit them if after more than 3 seconds, they are still alive, not responding to the
signal.

## (2/5) Start a Gradle build from CLI

_Make sure you haven't opened a Gradle project with the IDE since the previous step, to ensure no
problematic Gradle daemon is spawned._

From a terminal, in the project's root dir, run `./gradlew`.
_This will start a new Gradle daemon, with the right environment variables (especially for CMake)._

From now on, you can start a Gradle sync, and run tasks from the Gradle tool window too, until
the Gradle daemon dies (happens after about 3h of non-use, by default), at which point you'd need
to redo the first 2 steps.

## (3/5) Injecting the environment 

_To avoid leaking build traceability secrets, it's advised to disable shell history for commands
starting with a leading space, and to **not forget the leading space** when injecting the environment
variables in the current shell session. That is the `histignorespace` option from below._

You can, one-by-one, copy each of those commands (filling with the right data) and run them.
These are required for signing and Maven Central publishing.

```shell
setopt histignorespace
 export GPG_private_key="-----BEGIN PGP PRIVATE KEY BLOCK-----???-----END PGP PRIVATE KEY BLOCK-----"
 export GPG_key_id="????????"
 export GPG_private_password="???"
 export ossrhUsername="???"
 export ossrhPassword="???"
```

## (4/5) Build and test locally, in a real app

Publishing is done with [nmcp](https://github.com/GradleUp/nmcp), which provides a way to check
what will be sent to Maven Central.

Instead of doing a `publishToMavenLocal`, at least for the last test,
we recommend to do the following **to check the publication content**:

### 1. Clean Maven Local from any leftover version

```shell
rm -rf ~/.m2/repository/com/infomaniak/realm/kotlin
```

### 2. Set the version to something unique

To **avoid inadvertently testing against an already published version**, change the version in both
the consumer project and this project to something that has never been published before.
For example if you want to test against for an upcoming release named `3.2.9-1`, you can set the
version to `3.2.9-1-local` or alike.

For this project, set the version in the [`Config.kt`](buildSrc/src/main/kotlin/Config.kt) file,
in the `version` property of the `Realm` object` (should be at line 65).

### 3. Ensure `mavenLocal()` is added for plugins and regular dependencies

_We will use Maven Local, even if we're not using the `publishToMavenLocal` Gradle task._

### 4. Check the test consumer project misses the unique version

When trying to run a debug app build of the consumer project, it should fail to resolve the
Realm dependencies, since they haven't been built yet.

### 5. Build it into an aggregated zip

Now, run the the `nmcpZipAggregation` task:

```shell
./gradlew nmcpZipAggregation
```

_It will run the build of all publishable modules, and put them into a zip, the same zip that
would be sent to Maven Central._

### 6. Unzip it to Maven Local

```shell
unzip build/nmcp/zip/aggregation.zip -d ~/.m2/repository/
```

### 7. Test the test consumer project

Now, Gradle sync, building, running the app, and its tests, should work fine.

Ensure everything is right and ready for publication.

#### Troubleshooting

If you get C/C++ compilation issues, make sure the Gradle daemon was started from CLI,
as specified above, make sure you have the correct prerequisites, and that the
right revisions of the git submodules are checked out.

If you have a runtime crash with something like the following one:

```stacktrace
java.lang.UnsatisfiedLinkError: No implementation found for long io.realm.kotlin.internal.interop.realmcJNI.RLM_INVALID_CLASS_KEY_get() (tried Java_io_realm_kotlin_internal_interop_realmcJNI_RLM_1INVALID_1CLASS_1KEY_1get and Java_io_realm_kotlin_internal_interop_realmcJNI_RLM_1INVALID_1CLASS_1KEY_1get__) - is the library loaded, e.g. System.loadLibrary?
	at io.realm.kotlin.internal.interop.realmcJNI.RLM_INVALID_CLASS_KEY_get(Native Method)
	at io.realm.kotlin.internal.interop.realmc.getRLM_INVALID_CLASS_KEY(realmc.java:193)
	at io.realm.kotlin.internal.interop.RealmInteropKt.INVALID_CLASS_KEY_delegate$lambda$0(RealmInterop.kt:43)
	at io.realm.kotlin.internal.interop.RealmInteropKt.$r8$lambda$LJwi4SNclSxwkee1Ov--MrCpyik(Unknown Source:0)
	at io.realm.kotlin.internal.interop.RealmInteropKt$$ExternalSyntheticLambda0.invoke(D8$$SyntheticClass:0)
	at kotlin.SynchronizedLazyImpl.getValue(LazyJVM.kt:86)
	at io.realm.kotlin.internal.interop.RealmInteropKt.getINVALID_CLASS_KEY(RealmInterop.kt:43)
	at io.realm.kotlin.internal.interop.ClassInfo.<init>(ClassInfo.kt:27)
	at io.realm.kotlin.internal.interop.ClassInfo$Companion.create(ClassInfo.kt:48)
```

Delete the `packages/cinterop/.cxx` directory and try again:

```shell
rm -rf packages/cinterop/.cxx
```

### 8. Extra publication setup checks with SNAPSHOTS

If you want to test all the Maven Central required metadata is properly set, for example because
the Gradle config was edited somehow, you can use snapshots.

**In that case, make sure to add the `-SNAPSHOT` suffix to the version.**

Also ensure that SNAPSHOTS are enabled for the namespace: https://central.sonatype.org/publish/publish-portal-snapshots/#enabling-snapshot-releases-for-your-namespace

Then, you can run this command:

```shell
./gradlew publishAggregationToCentralPortalSnapshots
```

You can then add this repo to consume it https://central.sonatype.com/repository/maven-snapshots/

## (5/5) Publish to Maven Central

### 1. Update the version

Remove any suffix added previously, having the exact version you want to release instead.
For the example we used before, that would be `3.2.9-1`.

### 2. Build and publish

This is it. This command will run the build, build the zip aggregate, and send it to Maven Central,
checking for status repeatedly, until validation.

Once this command completes successfully, expect a delay typically between 10 to 30 minutes,
but potentially of several hours sometimes, before final availability.

```shell
./gradlew publishAggregationToCentralPortal
```

You can refresh the page linked below to see if the new version is available:

https://repo1.maven.org/maven2/com/infomaniak/realm/kotlin/gradle-plugin/

### 3. Announce the new version

Create a GitHub Release with human readable updates.
Example: https://github.com/Infomaniak/realm-kotlin/releases/tag/3.2.8-2
