/*
 * Copyright 2021 Realm Inc.
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
package io.github.xilinjia.krdb.test.common

import io.github.xilinjia.krdb.Realm
import io.github.xilinjia.krdb.RealmConfiguration
import io.github.xilinjia.krdb.entities.primarykey.NoPrimaryKey
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyBsonObjectId
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyBsonObjectIdNullable
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyByte
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyByteNullable
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyChar
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyCharNullable
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyInt
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyIntNullable
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyLong
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyLongNullable
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyRealmUUID
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyRealmUUIDNullable
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyShort
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyShortNullable
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyString
import io.github.xilinjia.krdb.entities.primarykey.PrimaryKeyStringNullable
import io.github.xilinjia.krdb.ext.query
import io.github.xilinjia.krdb.query.find
import io.github.xilinjia.krdb.test.common.utils.assertFailsWithMessage
import io.github.xilinjia.krdb.test.platform.PlatformUtils
import io.github.xilinjia.krdb.test.util.TypeDescriptor.allPrimaryKeyFieldTypes
import io.github.xilinjia.krdb.test.util.TypeDescriptor.rType
import io.github.xilinjia.krdb.test.util.use
import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.RealmUUID
import org.mongodb.kbson.BsonObjectId
import kotlin.reflect.typeOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val PRIMARY_KEY = "PRIMARY_KEY"

class PrimaryKeyTests {

    private lateinit var tmpDir: String
    private lateinit var configuration: RealmConfiguration
    private lateinit var realm: Realm

    @BeforeTest
    fun setup() {
        tmpDir = PlatformUtils.createTempDir()
        configuration =
            RealmConfiguration.Builder(
                setOf(
                    PrimaryKeyString::class,
                    PrimaryKeyStringNullable::class,
                    NoPrimaryKey::class

                )
            )
                .directory(tmpDir)
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
    fun string() {
        realm.writeBlocking {
            copyToRealm(PrimaryKeyString().apply { primaryKey = PRIMARY_KEY })
        }

        realm.query<PrimaryKeyString>()
            .find { results ->
                assertEquals(PRIMARY_KEY, results[0].primaryKey)
            }
    }

    @Test
    fun nullPrimaryKey() {
        realm.writeBlocking {
            copyToRealm(PrimaryKeyStringNullable().apply { primaryKey = null })
        }

        realm.query<PrimaryKeyStringNullable>()
            .find { results ->
                assertNull(results[0].primaryKey)
            }
    }

    @Test
    fun duplicatePrimaryKeyThrows() {
        realm.writeBlocking {
            val obj = PrimaryKeyString().apply { primaryKey = PRIMARY_KEY }
            copyToRealm(obj)
            assertFailsWith<IllegalArgumentException> {
                copyToRealm(obj)
            }
        }

        realm.query<PrimaryKeyString>()
            .find { results ->
                assertEquals(PRIMARY_KEY, results[0].primaryKey)
            }
    }

    @Test
    fun duplicateNullPrimaryKeyThrows() {
        realm.writeBlocking {
            val obj = PrimaryKeyStringNullable().apply { primaryKey = null }
            copyToRealm(obj)
            assertFailsWith<IllegalArgumentException> {
                copyToRealm(obj)
            }
        }

        realm.query<PrimaryKeyStringNullable>()
            .find { results ->
                assertEquals(1, results.size)
                assertNull(results[0].primaryKey)
            }
    }

    @Test
    fun updateWithDuplicatePrimaryKeyThrows() {
        realm.writeBlocking {
            copyToRealm(PrimaryKeyString().apply { primaryKey = PRIMARY_KEY }).run {
                assertFailsWithMessage<IllegalArgumentException>("Cannot update primary key property 'PrimaryKeyString.primaryKey'") {
                    primaryKey = PRIMARY_KEY
                }
            }
        }
    }

    @Test
    @OptIn(ExperimentalStdlibApi::class)
    fun verifyPrimaryKeyTypeSupport() {
        val expectedTypes = setOf(
            typeOf<Byte>(),
            typeOf<Byte?>(),
            typeOf<Char>(),
            typeOf<Char?>(),
            typeOf<Short>(),
            typeOf<Short?>(),
            typeOf<Int>(),
            typeOf<Int?>(),
            typeOf<Long>(),
            typeOf<Long?>(),
            typeOf<String>(),
            typeOf<String?>(),
            typeOf<BsonObjectId>(),
            typeOf<BsonObjectId?>(),
            typeOf<RealmUUID>(),
            typeOf<RealmUUID?>(),
        ).map { it.rType() }.toMutableSet()

        assertTrue(expectedTypes.containsAll(allPrimaryKeyFieldTypes))
        expectedTypes.removeAll(allPrimaryKeyFieldTypes)
        assertTrue(expectedTypes.isEmpty(), "$expectedTypes")
    }

    @Test
    @Suppress("invisible_reference", "invisible_member")
    fun testPrimaryKeyForAllSupportedTypes() {

        // TODO Maybe we would only need to iterate underlying Realm types?
        val classes = arrayOf(
            PrimaryKeyByte::class,
            PrimaryKeyByteNullable::class,
            PrimaryKeyChar::class,
            PrimaryKeyCharNullable::class,
            PrimaryKeyShort::class,
            PrimaryKeyShortNullable::class,
            PrimaryKeyInt::class,
            PrimaryKeyIntNullable::class,
            PrimaryKeyLong::class,
            PrimaryKeyLongNullable::class,
            PrimaryKeyString::class,
            PrimaryKeyStringNullable::class,
            PrimaryKeyBsonObjectId::class,
            PrimaryKeyBsonObjectIdNullable::class,
            PrimaryKeyRealmUUID::class,
            PrimaryKeyRealmUUIDNullable::class,
        )

        val configuration = RealmConfiguration.Builder(
            setOf(
                PrimaryKeyByte::class,
                PrimaryKeyByteNullable::class,
                PrimaryKeyChar::class,
                PrimaryKeyCharNullable::class,
                PrimaryKeyShort::class,
                PrimaryKeyShortNullable::class,
                PrimaryKeyInt::class,
                PrimaryKeyIntNullable::class,
                PrimaryKeyLong::class,
                PrimaryKeyLongNullable::class,
                PrimaryKeyString::class,
                PrimaryKeyStringNullable::class,
                PrimaryKeyBsonObjectId::class,
                PrimaryKeyBsonObjectIdNullable::class,
                PrimaryKeyRealmUUID::class,
                PrimaryKeyRealmUUIDNullable::class,
            )
        )
            .directory(tmpDir)
            .build()

//        @Suppress("invisible_reference", "invisible_member")
        val mediator = (configuration as io.github.xilinjia.krdb.internal.RealmConfigurationImpl).mediator

        Realm.open(configuration).use { realm ->
            realm.writeBlocking {
                val types = allPrimaryKeyFieldTypes.toMutableSet()
                for (c in classes) {
                    // We could expose this through the test model definitions instead if that is better to avoid the internals
                    val realmObjectCompanion = mediator.companionOf(c)
                    copyToRealm(realmObjectCompanion.`io_realm_kotlin_newInstance`() as RealmObject)
                    val type = realmObjectCompanion.`io_realm_kotlin_primaryKey`!!.rType()
                    assertTrue(types.remove(type), type.toString())
                }
                assertTrue(types.toTypedArray().isEmpty(), "Untested primary keys: $types")
            }
        }
    }
}
