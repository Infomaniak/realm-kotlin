/*
 * Copyright 2023 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.xilinjia.krdb.test.common.notifications

import io.github.xilinjia.krdb.Realm
import io.github.xilinjia.krdb.RealmConfiguration
import io.github.xilinjia.krdb.entities.JsonStyleRealmObject
import io.github.xilinjia.krdb.ext.realmAnyDictionaryOf
import io.github.xilinjia.krdb.ext.realmAnyListOf
import io.github.xilinjia.krdb.ext.realmAnyOf
import io.github.xilinjia.krdb.internal.platform.runBlocking
import io.github.xilinjia.krdb.notifications.DeletedMap
import io.github.xilinjia.krdb.notifications.InitialMap
import io.github.xilinjia.krdb.notifications.MapChange
import io.github.xilinjia.krdb.notifications.UpdatedMap
import io.github.xilinjia.krdb.test.common.utils.DeletableEntityNotificationTests
import io.github.xilinjia.krdb.test.common.utils.FlowableTests
import io.github.xilinjia.krdb.test.platform.PlatformUtils
import io.github.xilinjia.krdb.test.util.receiveOrFail
import io.github.xilinjia.krdb.types.RealmAny
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class RealmAnyNestedDictionaryNotificationTest : FlowableTests, DeletableEntityNotificationTests {

    lateinit var tmpDir: String
    lateinit var configuration: RealmConfiguration
    lateinit var realm: Realm

    @BeforeTest
    fun setup() {
        tmpDir = PlatformUtils.createTempDir()
        configuration = RealmConfiguration.Builder(
            schema = setOf(JsonStyleRealmObject::class)
        ).directory(tmpDir)
            .build()
        realm = Realm.open(configuration)
    }

    @AfterTest
    fun tearDown() {
        if (this::realm.isInitialized && !realm.isClosed()) {
            realm.close()
        }
        PlatformUtils.deleteTempDir(tmpDir)
    }

    @Test
    @Ignore // Initial element events are verified as part of the asFlow tests
    override fun initialElement() {}

    @Test
    override fun asFlow() = runBlocking<Unit> {
        val channel = Channel<MapChange<String, RealmAny?>>()

        val o: JsonStyleRealmObject = realm.write {
            copyToRealm(
                JsonStyleRealmObject().apply {
                    value = realmAnyDictionaryOf(
                        "root" to realmAnyDictionaryOf(
                            "key1" to 1,
                            "key2" to 2,
                            "key3" to 3
                        )
                    )
                }
            )
        }

        val dict = o.value!!.asDictionary()["root"]!!.asDictionary()
        assertEquals(3, dict.size)
        val listener = async {
            dict.asFlow().collect {
                channel.send(it)
            }
        }

        channel.receiveOrFail(1.seconds).run {
            assertIs<InitialMap<String, RealmAny?>>(this)
            assertEquals(
                mapOf("key1" to 1, "key2" to 2, "key3" to 3),
                this.map.mapValues { it.value!!.asInt() }
            )
        }

        realm.write {
            val liveList = findLatest(o)!!.value!!.asDictionary()["root"]!!.asDictionary()
            liveList.put("key4", RealmAny.create(4))
        }

        channel.receiveOrFail(1.seconds).run {
            assertIs<UpdatedMap<String, RealmAny?>>(this)
            assertEquals(mapOf("key1" to 1, "key2" to 2, "key3" to 3, "key4" to 4), this.map.mapValues { it.value!!.asInt() })
        }

        realm.write {
            findLatest(o)!!.value = realmAnyOf(5)
        }

        // Fails due to missing deletion events
        channel.receiveOrFail(1.seconds).run {
            assertIs<DeletedMap<String, RealmAny?>>(this)
        }
        listener.cancel()
        channel.close()
    }

    @Test
    override fun cancelAsFlow() {
        kotlinx.coroutines.runBlocking {
            val container = realm.write {
                copyToRealm(
                    JsonStyleRealmObject().apply {
                        value = realmAnyDictionaryOf("root" to realmAnyDictionaryOf())
                    }
                )
            }
            val channel1 = Channel<MapChange<String, *>>(1)
            val channel2 = Channel<MapChange<String, *>>(1)
            val observedDict = container.value!!.asDictionary()["root"]!!.asDictionary()
            val observer1 = async {
                observedDict.asFlow()
                    .collect { change ->
                        channel1.trySend(change)
                    }
            }
            val observer2 = async {
                observedDict.asFlow()
                    .collect { change ->
                        channel2.trySend(change)
                    }
            }

            // Ignore first emission with empty sets
            assertTrue { channel1.receiveOrFail(1.seconds).map.isEmpty() }
            assertTrue { channel2.receiveOrFail(1.seconds).map.isEmpty() }

            // Trigger an update
            realm.write {
                val queriedContainer = findLatest(container)
                queriedContainer!!.value!!.asDictionary()["root"]!!.asDictionary().put("key1", RealmAny.create(1))
            }
            assertEquals(1, channel1.receiveOrFail().map.size)
            assertEquals(1, channel2.receiveOrFail().map.size)

            // Cancel observer 1
            observer1.cancel()

            // Trigger another update
            realm.write {
                val queriedContainer = findLatest(container)
                queriedContainer!!.value!!.asDictionary()["root"]!!.asDictionary().put("key2", RealmAny.create(2))
            }

            // Check channel 1 didn't receive the update
            assertTrue(channel1.isEmpty)
            // But channel 2 did
            assertEquals(2, channel2.receiveOrFail().map.size)

            observer2.cancel()
            channel1.close()
            channel2.close()
        }
    }

    @Test
    override fun deleteEntity() = runBlocking<Unit> {
        val container = realm.write {
            copyToRealm(
                JsonStyleRealmObject().apply {
                    value = realmAnyDictionaryOf("root" to realmAnyDictionaryOf())
                }
            )
        }
        val mutex = Mutex(true)
        val flow = async {
            container.value!!.asDictionary()["root"]!!.asDictionary().asFlow().first {
                mutex.unlock()
                it is DeletedMap<String, *>
            }
        }

        // Await that flow is actually running
        mutex.lock()
        // Update mixed value to overwrite and delete set
        realm.write {
            findLatest(container)!!.value = realmAnyListOf()
        }

        // Await that notifier has signalled the deletion so we are certain that the entity
        // has been deleted
        withTimeout(10.seconds) {
            flow.await()
        }
    }

    @Test
    override fun asFlowOnDeletedEntity() = runBlocking<Unit> {
        val container = realm.write {
            copyToRealm(
                JsonStyleRealmObject().apply {
                    value = realmAnyDictionaryOf("root" to realmAnyDictionaryOf())
                }
            )
        }
        val mutex = Mutex(true)
        val flow = async {
            container.value!!.asDictionary()["root"]!!.asDictionary().asFlow().first {
                mutex.unlock()
                it is DeletedMap<String, *>
            }
        }

        // Await that flow is actually running
        mutex.lock()
        // And delete containing entity
        realm.write { delete(findLatest(container)!!) }

        // Await that notifier has signalled the deletion so we are certain that the entity
        // has been deleted
        withTimeout(10.seconds) {
            flow.await()
        }

        // Verify that a flow on the deleted entity will signal a deletion and complete gracefully
        withTimeout(10.seconds) {
            container.value!!.asDictionary()["root"]!!.asDictionary().asFlow().collect {
                assertIs<DeletedMap<String, *>>(it)
            }
        }
    }

    @Test
    @Ignore
    override fun closingRealmDoesNotCancelFlows() {
        TODO("Not yet implemented")
    }

    @Test
    @Ignore
    override fun closeRealmInsideFlowThrows() {
        TODO("Not yet implemented")
    }
}
