## Keep Companion classes and class.Companion member of all classes that can be used in our API to
#  allow calling realmObjectCompanionOrThrow and realmObjectCompanionOrNull on the classes
-keep class io.github.xilinjia.krdb.types.RealmInstant$Companion
-keepclassmembers class io.github.xilinjia.krdb.types.RealmInstant {
    io.github.xilinjia.krdb.types.RealmInstant$Companion Companion;
}
-keep class org.mongodb.kbson.BsonObjectId$Companion
-keepclassmembers class org.mongodb.kbson.BsonObjectId {
    org.mongodb.kbson.BsonObjectId$Companion Companion;
}
-keep class io.github.xilinjia.krdb.dynamic.DynamicRealmObject$Companion, io.github.xilinjia.krdb.dynamic.DynamicMutableRealmObject$Companion
-keepclassmembers class io.github.xilinjia.krdb.dynamic.DynamicRealmObject, io.github.xilinjia.krdb.dynamic.DynamicMutableRealmObject {
    **$Companion Companion;
}
-keep,allowobfuscation class ** implements io.github.xilinjia.krdb.types.BaseRealmObject
-keep class ** implements io.github.xilinjia.krdb.internal.RealmObjectCompanion
-keepclassmembers class ** implements io.github.xilinjia.krdb.types.BaseRealmObject {
    **$Companion Companion;
}

## Preserve all native method names and the names of their classes.
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

## Preserve all classes that are looked up from native code
# Notification callback
-keep class io.github.xilinjia.krdb.internal.interop.NotificationCallback {
    *;
}
# Utils to convert core errors into Kotlin exceptions
-keep class io.github.xilinjia.krdb.internal.interop.CoreErrorConverter {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.JVMScheduler {
    *;
}
# Interop, sync-specific classes
-keep class io.github.xilinjia.krdb.internal.interop.sync.NetworkTransport {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.sync.Response {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.LongPointerWrapper {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.sync.AppError {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.sync.CoreConnectionState {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.sync.SyncError {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.LogCallback {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.SyncErrorCallback {
    # TODO OPTIMIZE Only keep actually required symbols
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.sync.JVMSyncSessionTransferCompletionCallback {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.sync.ResponseCallback {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.sync.ResponseCallbackImpl {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.AppCallback {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.CompactOnLaunchCallback {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.MigrationCallback {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.DataInitializationCallback {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.SubscriptionSetCallback {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.SyncBeforeClientResetHandler {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.SyncAfterClientResetHandler {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.AsyncOpenCallback {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.NativePointer {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.ProgressCallback {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.sync.ApiKeyWrapper {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.ConnectionStateChangeCallback {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.SyncThreadObserver {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.sync.CoreCompensatingWriteInfo {
    *;
}
# Preserve Function<X> methods as they back various functional interfaces called from JNI
-keep class kotlin.jvm.functions.Function* {
    *;
}
-keep class kotlin.Unit {
    *;
}

# Platform networking callback
-keep class io.github.xilinjia.krdb.internal.interop.sync.WebSocketTransport {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.sync.CancellableTimer {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.sync.WebSocketClient {
    *;
}
-keep class io.github.xilinjia.krdb.internal.interop.sync.WebSocketObserver {
    *;
}

# Un-comment for debugging
#-printconfiguration /tmp/full-r8-config.txt
#-keepattributes LineNumberTable,SourceFile
#-printusage /tmp/removed_entries.txt
#-printseeds /tmp/kept_entries.txt
