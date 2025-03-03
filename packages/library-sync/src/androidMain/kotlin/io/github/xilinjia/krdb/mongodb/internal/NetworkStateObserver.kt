package io.github.xilinjia.krdb.mongodb.internal

internal actual fun registerSystemNetworkObserver() {
    // Registering network state listeners are done in io.github.xilinjia.krdb.mongodb.RealmSyncInitializer
    // so we do not have to store the Android Context.
}
