package io.github.xilinjia.krdb.test.common

import io.github.xilinjia.krdb.Realm
import io.github.xilinjia.krdb.RealmConfiguration
import io.github.xilinjia.krdb.entities.FqNameImportEmbeddedChild
import io.github.xilinjia.krdb.entities.FqNameImportParent
import io.github.xilinjia.krdb.ext.query
import io.github.xilinjia.krdb.test.platform.PlatformUtils
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class FqNameImportTests {

    lateinit var tmpDir: String
    lateinit var realm: Realm

    @BeforeTest
    fun setup() {
        tmpDir = PlatformUtils.createTempDir()
        val configuration =
            RealmConfiguration.Builder(schema = setOf(FqNameImportParent::class, FqNameImportEmbeddedChild::class))
                .directory(tmpDir)
                .build()
        realm = Realm.open(configuration)
    }

    @AfterTest
    fun tearDown() {
        if (this::realm.isInitialized) {
            realm.close()
        }
        PlatformUtils.deleteTempDir(tmpDir)
    }

    @Test
    fun import() {
        realm.writeBlocking {
            copyToRealm(FqNameImportParent().apply { child = FqNameImportEmbeddedChild() })
        }

        realm.query<FqNameImportParent>().find().single().run {
            assertNotNull(child)
        }
    }
}
