package io.github.xilinjia.krdb.test.mongodb.jvm

import io.github.xilinjia.krdb.Realm
import io.github.xilinjia.krdb.entities.sync.SyncObjectWithAllTypes
import io.github.xilinjia.krdb.ext.query
import io.github.xilinjia.krdb.internal.platform.runBlocking
import io.github.xilinjia.krdb.log.LogCategory
import io.github.xilinjia.krdb.log.LogLevel
import io.github.xilinjia.krdb.log.RealmLog
import io.github.xilinjia.krdb.mongodb.User
import io.github.xilinjia.krdb.mongodb.sync.SyncConfiguration
import io.github.xilinjia.krdb.mongodb.syncSession
import io.github.xilinjia.krdb.test.mongodb.TestApp
import io.github.xilinjia.krdb.test.mongodb.common.FLEXIBLE_SYNC_SCHEMA
import io.github.xilinjia.krdb.test.mongodb.common.utils.CustomLogCollector
import io.github.xilinjia.krdb.test.mongodb.createUserAndLogIn
import io.github.xilinjia.krdb.test.mongodb.use
import io.github.xilinjia.krdb.test.mongodb.util.DefaultFlexibleSyncAppInitializer
import io.github.xilinjia.krdb.test.util.use
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class PlatformNetworkingTests {

    private val TIMEOUT = 10.seconds

    @Test
    @Ignore // https://github.com/realm/realm-kotlin/issues/1819
    fun syncRoundTrip_coreNetworking() = runBlocking {
        roundTrip(platformNetworking = false)
    }

    @Test
    fun syncRoundTrip_platformNetworking() = runBlocking {
        roundTrip(platformNetworking = true)
    }

    private suspend fun roundTrip(platformNetworking: Boolean) {
        TestApp(this::class.simpleName, DefaultFlexibleSyncAppInitializer, builder = {
            it.usePlatformNetworking(platformNetworking)
        }).use { app ->
            val selector = org.mongodb.kbson.ObjectId().toString()

            // Setup logger to capture WebSocketClient log messages
            val logger = CustomLogCollector()
            RealmLog.add(logger)
            RealmLog.setLevel(LogLevel.DEBUG, LogCategory.Realm.Sdk)

            Realm.open(createSyncConfig(app.createUserAndLogIn(), selector))
                .use { uploadRealm ->
                    Realm.open(createSyncConfig(app.createUserAndLogIn(), selector))
                        .use { realm ->
                            uploadRealm.write {
                                copyToRealm(
                                    SyncObjectWithAllTypes().apply {
                                        stringField = selector
                                    }
                                )
                            }
                            uploadRealm.syncSession.uploadAllLocalChanges(TIMEOUT)
                            withTimeout(TIMEOUT) {
                                realm.query<SyncObjectWithAllTypes>().asFlow().first {
                                    it.list.size == 1
                                }.list.first().also {
                                    assertEquals(selector, it.stringField)
                                }
                            }
                        }
                }
            assertTrue(
                if (platformNetworking) {
                    logger.logs.any { it.contains("\\[Websocket.*\\] onOpen".toRegex()) }
                } else {
                    logger.logs.none { it.contains("\\[Websocket.*\\] onOpen".toRegex()) }
                },
                "Failed to verify log statements for : platformNetworking=$platformNetworking"
            )
        }
    }

    private fun createSyncConfig(
        user: User,
        selector: String
    ): SyncConfiguration {
        return SyncConfiguration.Builder(user, FLEXIBLE_SYNC_SCHEMA).initialSubscriptions {
            add(it.query<SyncObjectWithAllTypes>("stringField = $0", selector))
        }.build()
    }
}
