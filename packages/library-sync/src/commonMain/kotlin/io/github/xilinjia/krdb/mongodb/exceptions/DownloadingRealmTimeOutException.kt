package io.github.xilinjia.krdb.mongodb.exceptions

import io.github.xilinjia.krdb.exceptions.RealmException
import io.github.xilinjia.krdb.mongodb.sync.SyncConfiguration

/**
 * Thrown when opening a Realm and it didn't finish download server data in the allocated timeframe.
 *
 * This can only happen if [SyncConfiguration.Builder.waitForInitialRemoteData] is set.
 */
public class DownloadingRealmTimeOutException : RealmException {
    internal constructor(syncConfig: SyncConfiguration) : super(
        "Realm did not manage to download all initial data in time: ${syncConfig.path}, " +
            "timeout: ${syncConfig.initialRemoteData!!.timeout}."
    )
}
