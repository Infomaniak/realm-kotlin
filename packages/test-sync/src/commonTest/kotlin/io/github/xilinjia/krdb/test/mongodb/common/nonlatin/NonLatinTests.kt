package io.github.xilinjia.krdb.test.mongodb.common.nonlatin

import io.github.xilinjia.krdb.Realm
import io.github.xilinjia.krdb.entities.sync.ObjectIdPk
import io.github.xilinjia.krdb.ext.query
import io.github.xilinjia.krdb.internal.platform.runBlocking
import io.github.xilinjia.krdb.mongodb.User
import io.github.xilinjia.krdb.mongodb.sync.SyncConfiguration
import io.github.xilinjia.krdb.test.mongodb.TestApp
import io.github.xilinjia.krdb.test.mongodb.asTestApp
import io.github.xilinjia.krdb.test.mongodb.common.PARTITION_BASED_SCHEMA
import io.github.xilinjia.krdb.test.mongodb.createUserAndLogIn
import io.github.xilinjia.krdb.test.mongodb.util.DefaultPartitionBasedAppInitializer
import io.github.xilinjia.krdb.test.util.TestChannel
import io.github.xilinjia.krdb.test.util.TestHelper
import io.github.xilinjia.krdb.test.util.receiveOrFail
import io.github.xilinjia.krdb.test.util.use
import kotlinx.coroutines.async
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ExperimentalKBsonSerializerApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NonLatinTests {

    private lateinit var partitionValue: String
    private lateinit var user: User
    private lateinit var app: TestApp

    @BeforeTest
    fun setup() {
        partitionValue = TestHelper.randomPartitionValue()
        @OptIn(ExperimentalKBsonSerializerApi::class)
        app = TestApp(this::class.simpleName, DefaultPartitionBasedAppInitializer)
        val (email, password) = TestHelper.randomEmail() to "password1234"
        user = runBlocking {
            app.createUserAndLogIn(email, password)
        }
    }

    @AfterTest
    fun tearDown() {
        if (this::app.isInitialized) {
            app.close()
        }
    }

    /**
     * - Insert a string with the null character in MongoDB using the command server
     */
    @Test
    fun readNullCharacterFromMongoDB() = runBlocking {
        val adminApi = app.asTestApp
        val config = SyncConfiguration.Builder(user, partitionValue, schema = PARTITION_BASED_SCHEMA).build()
        Realm.open(config).use { realm ->
            val json: JsonObject = adminApi.insertDocument(
                ObjectIdPk::class.simpleName!!,
                """
                {
                    "name": "foo\u0000bar",
                    "realm_id" : "$partitionValue"
                }
                """.trimIndent()
            )!!
            val oid = json["insertedId"]!!.jsonObject["${'$'}oid"]!!.jsonPrimitive.content
            assertNotNull(oid)

            val channel = TestChannel<ObjectIdPk>()
            val job = async {
                realm.query<ObjectIdPk>("_id = $0", BsonObjectId(oid)).first()
                    .asFlow().collect {
                        if (it.obj != null) {
                            channel.send(it.obj!!)
                        }
                    }
            }

            val insertedObject = channel.receiveOrFail()
            assertEquals(oid, insertedObject._id.toHexString())
            val char1 = "foo\u0000bar".toCharArray()
            val char2 = insertedObject.name.toCharArray()
            assertEquals("foo\u0000bar", insertedObject.name)
            assertContentEquals(char1, char2)
            job.cancel()
        }
    }
}
