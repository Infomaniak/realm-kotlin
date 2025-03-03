package io.github.xilinjia.krdb.mongodb.internal

import io.github.xilinjia.krdb.internal.interop.RealmAppPointer
import io.github.xilinjia.krdb.internal.interop.RealmInterop
import io.github.xilinjia.krdb.mongodb.sync.Sync

internal class SyncImpl(private val app: RealmAppPointer) : Sync {

    override val hasSyncSessions: Boolean
        get() = RealmInterop.realm_app_sync_client_has_sessions(app)

    override fun reconnect() {
        RealmInterop.realm_app_sync_client_reconnect(app)
    }

    override fun waitForSessionsToTerminate() {
        RealmInterop.realm_app_sync_client_wait_for_sessions_to_terminate(app)
    }
}
