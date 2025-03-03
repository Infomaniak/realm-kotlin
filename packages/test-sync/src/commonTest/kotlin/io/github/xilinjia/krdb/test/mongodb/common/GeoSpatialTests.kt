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

package io.github.xilinjia.krdb.test.mongodb.common

import io.github.xilinjia.krdb.Realm
import io.github.xilinjia.krdb.annotations.ExperimentalGeoSpatialApi
import io.github.xilinjia.krdb.entities.Location
import io.github.xilinjia.krdb.entities.sync.SyncRestaurant
import io.github.xilinjia.krdb.ext.query
import io.github.xilinjia.krdb.internal.platform.runBlocking
import io.github.xilinjia.krdb.mongodb.User
import io.github.xilinjia.krdb.mongodb.sync.SyncConfiguration
import io.github.xilinjia.krdb.mongodb.syncSession
import io.github.xilinjia.krdb.test.mongodb.TestApp
import io.github.xilinjia.krdb.test.mongodb.createUserAndLogIn
import io.github.xilinjia.krdb.test.mongodb.util.DefaultFlexibleSyncAppInitializer
import io.github.xilinjia.krdb.test.util.TestHelper
import io.github.xilinjia.krdb.test.util.use
import io.github.xilinjia.krdb.types.geo.Distance
import io.github.xilinjia.krdb.types.geo.GeoBox
import io.github.xilinjia.krdb.types.geo.GeoCircle
import io.github.xilinjia.krdb.types.geo.GeoPoint
import io.github.xilinjia.krdb.types.geo.GeoPolygon
import org.mongodb.kbson.ObjectId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalGeoSpatialApi::class)
class GeoSpatialTests {
    private lateinit var app: TestApp

    @BeforeTest
    fun setup() {
        app = TestApp(this::class.simpleName, DefaultFlexibleSyncAppInitializer)
    }

    @AfterTest
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    private suspend fun createRandomUser(): User =
        app.createUserAndLogIn(
            email = TestHelper.randomEmail(),
            password = "password1234"
        )

    @Test
    fun write() {
        runBlocking {
            val user = createRandomUser()

            val config =
                SyncConfiguration.Builder(
                    user = user,
                    schema = FLEXIBLE_SYNC_SCHEMA
                ).initialSubscriptions {
                    add(it.query<SyncRestaurant>())
                }.build()

            Realm.open(config).use { realm ->
                realm.write {
                    copyToRealm(SyncRestaurant())
                }
            }
        }
    }

    @Test
    fun write_outsideSubscriptionsFail() {
        runBlocking {
            val user = createRandomUser()

            val config =
                SyncConfiguration.Builder(
                    user = user,
                    schema = FLEXIBLE_SYNC_SCHEMA
                ).build()

            Realm.open(config).use { realm ->
                realm.write {
                    assertFailsWith<IllegalArgumentException>(
                        message = "[RLM_ERR_NO_SUBSCRIPTION_FOR_WRITE]: Cannot write to class SyncRestaurant when no flexible sync subscription has been created."
                    ) {
                        copyToRealm(SyncRestaurant())
                    }
                }
            }
        }
    }

    @Test
    fun geoBox_tests() {
        runBlocking {
            generic_geo_test(
                bounds = GeoBox.create(
                    top = 1.0, left = 1.0,
                    bottom = -1.0, right = -1.0,
                ),
                validLocation = Location(0.0, 0.0),
                invalidLocation = Location(40.0, 40.0),
            )
        }
    }

    @Test
    fun geoCircle_tests() {
        runBlocking {
            generic_geo_test(
                bounds = GeoCircle.create(
                    GeoPoint.create(0.0, 0.0), Distance.fromKilometers(.01)
                ),
                validLocation = Location(0.0, 0.0),
                invalidLocation = Location(40.0, 40.0),
            )
        }
    }

    @Test
    fun geoPolygon_tests() {
        runBlocking {
            generic_geo_test(
                bounds = GeoPolygon.create(
                    outerRing = listOf(
                        GeoPoint.create(-5.0, -5.0),
                        GeoPoint.create(5.0, -5.0),
                        GeoPoint.create(5.0, 5.0),
                        GeoPoint.create(-5.0, 5.0),
                        GeoPoint.create(-5.0, -5.0)
                    ),
                    holes = arrayOf(
                        listOf(
                            GeoPoint.create(-4.0, -4.0),
                            GeoPoint.create(4.0, -4.0),
                            GeoPoint.create(4.0, 4.0),
                            GeoPoint.create(-4.0, 4.0),
                            GeoPoint.create(-4.0, -4.0)
                        )
                    )
                ),
                validLocation = Location(4.5, 4.5), // Outside the hole and withing the ring
                invalidLocation = Location(0.0, 0.0), // Inside the hole
            )
        }
    }

    private suspend fun generic_geo_test(
        bounds: Any,
        validLocation: Location,
        invalidLocation: Location,
    ) {
        val section = ObjectId()

        // User #1 will try to write some data and assert some failure conditions.
        val user1 = createRandomUser()
        val config =
            SyncConfiguration.Builder(
                user = user1,
                schema = FLEXIBLE_SYNC_SCHEMA
            ).initialSubscriptions {
                add(
                    it.query<SyncRestaurant>(
                        "section = $0 AND location GEOWITHIN $1",
                        section,
                        bounds
                    )
                )
            }.waitForInitialRemoteData().build()

        Realm.open(config).use { realm ->
            val restaurant = realm.write {
                // Fail: write outside subscription bounds, compensating write
                copyToRealm(
                    SyncRestaurant().apply {
                        this.section = section
                        this.location = invalidLocation
                    }
                )

                // Ok: Write within subscription bounds
                copyToRealm(
                    SyncRestaurant().apply {
                        this.section = section
                        this.location = validLocation
                    }
                )

                // Ok: Write within subscription bounds, this one will be moved outside of bounds in the next step.
                copyToRealm(
                    SyncRestaurant().apply {
                        this.section = section
                        this.location = validLocation
                    }
                )
            }

            realm.syncSession.uploadAllLocalChanges(timeout = 30.seconds)

            realm.write {
                // Ok. The object will be updated and moved outside of its view.
                // It is a valid operation, it would be removed from the local Realm but still be
                // accessible at the mongo instance.
                findLatest(restaurant)!!.location = invalidLocation
            }

            realm.syncSession.uploadAllLocalChanges(timeout = 30.seconds)
        }

        // Download data on user #2
        val user2 = createRandomUser()
        val config2 =
            SyncConfiguration.Builder(
                user = user2,
                schema = FLEXIBLE_SYNC_SCHEMA
            ).initialSubscriptions {
                add(
                    it.query<SyncRestaurant>(
                        "section = $0 AND location GEOWITHIN $1",
                        section,
                        bounds
                    )
                )
            }.waitForInitialRemoteData(
                timeout = 30.seconds
            ).build()

        Realm.open(config2).use { realm ->
            assertEquals(1, realm.query<SyncRestaurant>().count().find())
        }
    }
}
