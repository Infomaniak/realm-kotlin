package io.github.xilinjia.krdb.test.mongodb.common

import io.github.xilinjia.krdb.Realm
import io.github.xilinjia.krdb.internal.platform.runBlocking
import io.github.xilinjia.krdb.mongodb.App
import io.github.xilinjia.krdb.mongodb.User
import io.github.xilinjia.krdb.mongodb.sync.SyncConfiguration
import io.github.xilinjia.krdb.test.mongodb.TestApp
import io.github.xilinjia.krdb.test.mongodb.asTestApp
import io.github.xilinjia.krdb.test.mongodb.createUserAndLogIn
import io.github.xilinjia.krdb.test.mongodb.util.DefaultFlexibleSyncAppInitializer
import io.github.xilinjia.krdb.test.util.TestHelper
import io.github.xilinjia.krdb.test.util.use
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for [io.github.xilinjia.krdb.mongodb.sync.Sync] that is accessed through
 * [io.github.xilinjia.krdb.mongodb.App.sync].
 */
class SyncClientTests {

    private lateinit var user: User
    private lateinit var app: App

    @BeforeTest
    fun setup() {
        app = TestApp(this::class.simpleName, DefaultFlexibleSyncAppInitializer)
        val (email, password) = TestHelper.randomEmail() to "password1234"
        user = runBlocking {
            app.createUserAndLogIn(email, password)
        }
    }

    @AfterTest
    fun tearDown() {
        if (this::app.isInitialized) {
            app.asTestApp.close()
        }
    }

    @Test
    fun sync() {
        assertNotNull(app.sync)
    }

    // There is no way to test reconnect automatically, so just verify that code path does not crash.
    @Test
    fun reconnect_noRealms() {
        app.sync.reconnect()
    }

    // There is no way to test reconnect automatically, so just verify that code path does not crash.
    @Test
    fun reconnect() {
        val config = SyncConfiguration.create(user, schema = FLEXIBLE_SYNC_SCHEMA)
        Realm.open(config).use {
            app.sync.reconnect()
        }
    }

    @Test
    fun hasSyncSessions_noRealms() {
        assertFalse(app.sync.hasSyncSessions)
    }

    @Test
    fun hasSyncSessions() {
        val config = SyncConfiguration.create(user, schema = FLEXIBLE_SYNC_SCHEMA)
        Realm.open(config).use {
            assertTrue(app.sync.hasSyncSessions)
        }
    }

    @Test
    fun waitForSessionsToTerminate_noRealms() {
        app.sync.waitForSessionsToTerminate()
    }

    @Test
    fun waitForSessionsToTerminate() {
        val config1 = SyncConfiguration.Builder(user, schema = FLEXIBLE_SYNC_SCHEMA).build()
        val config2 = SyncConfiguration.Builder(user, schema = FLEXIBLE_SYNC_SCHEMA).name("other.realm").build()

        Realm.open(config1).use {
            assertTrue(app.sync.hasSyncSessions)
            Realm.open(config2).use { /* do nothing */ }
            assertTrue(app.sync.hasSyncSessions)
        }
        app.sync.waitForSessionsToTerminate()
        assertFalse(app.sync.hasSyncSessions)
    }
}
