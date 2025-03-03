/*
 * Copyright 2020 Realm Inc.
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

package io.github.xilinjia.krdb.test.darwin

// FIXME API-CLEANUP Do we actually want to expose this. Test should probably just be reeavluated
//  or moved.
import io.github.xilinjia.krdb.RealmConfiguration
import io.github.xilinjia.krdb.entities.Sample
import io.github.xilinjia.krdb.internal.BaseRealmImpl
import io.github.xilinjia.krdb.internal.Mediator
import io.github.xilinjia.krdb.internal.RealmObjectCompanion
import io.github.xilinjia.krdb.internal.RealmObjectInternal
import io.github.xilinjia.krdb.internal.RealmObjectReference
import io.github.xilinjia.krdb.internal.RealmReference
import io.github.xilinjia.krdb.internal.interop.CapiT
import io.github.xilinjia.krdb.internal.interop.ClassKey
import io.github.xilinjia.krdb.internal.interop.NativePointer
import io.github.xilinjia.krdb.internal.interop.PropertyKey
import io.github.xilinjia.krdb.internal.interop.RealmPointer
import io.github.xilinjia.krdb.internal.schema.ClassMetadata
import io.github.xilinjia.krdb.internal.schema.PropertyMetadata
import io.github.xilinjia.krdb.internal.schema.SchemaMetadata
import io.github.xilinjia.krdb.types.BaseRealmObject
import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.TypedRealmObject
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toLong
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class InstrumentedTests {

    // FIXME API-CLEANUP Do we actually want to expose this. Test should probably just be reeavluated
    //  or moved. Local implementation of pointer wrapper to support test. Using the internal one would
    //  require the native wrapper to be api dependency from cinterop/library. Don't know if the
    //  test is needed at all at this level
    class CPointerWrapper<T : CapiT>(val ptr: CPointer<out CPointed>?, managed: Boolean = true) : NativePointer<T> {
        override fun release() {
            // Do nothing
        }

        override fun isReleased(): Boolean = false
    }

    @Test
    @Suppress("invisible_reference", "invisible_member")
    fun testRealmObjectInternalPropertiesGenerated() {
        val p = Sample()

        @Suppress("CAST_NEVER_SUCCEEDS")
        val realmModel: io.github.xilinjia.krdb.internal.RealmObjectInternal = p as? io.github.xilinjia.krdb.internal.RealmObjectInternal
            ?: error("Supertype RealmObjectInternal was not added to Sample class")

        memScoped {
            val ptr1: COpaquePointerVar = alloc()
            val ptr2: COpaquePointerVar = alloc()

            // Accessing getters/setters
            realmModel.`io_realm_kotlin_objectReference` = RealmObjectReference(
                type = RealmObject::class,
                objectPointer = CPointerWrapper(ptr1.ptr),
                className = "Sample",
                owner = MockRealmReference(),
                mediator = MockMediator()
            )

            val realmPointer: RealmPointer = CPointerWrapper(ptr2.ptr)
            val configuration = RealmConfiguration.create(schema = setOf(Sample::class))

            realmModel.`io_realm_kotlin_objectReference`?.run {
                assertNotNull(this)
                assertEquals(ptr1.rawPtr.toLong(), (objectPointer as CPointerWrapper).ptr.toLong())
                assertEquals("Sample", className)
            }
        }
    }

    class MockRealmReference : RealmReference {
        override val dbPointer: RealmPointer
            get() = TODO("Not yet implemented")
        override val owner: BaseRealmImpl
            get() = TODO("Not yet implemented")
        override val schemaMetadata: SchemaMetadata
            get() = object : SchemaMetadata {
                override fun get(className: String): ClassMetadata = object : ClassMetadata {
                    override val classKey: ClassKey
                        get() = TODO("Not yet implemented")
                    override val properties: List<PropertyMetadata>
                        get() = TODO("Not yet implemented")
                    override val clazz: KClass<out TypedRealmObject>
                        get() = TODO("Not yet implemented")
                    override val className: String
                        get() = TODO("Not yet implemented")
                    override val primaryKeyProperty: PropertyMetadata
                        get() = TODO("Not yet implemented")
                    override val isEmbeddedRealmObject: Boolean
                        get() = TODO("Not yet implemented")

                    override fun get(propertyKey: PropertyKey): PropertyMetadata? {
                        TODO("Not yet implemented")
                    }
                    override fun get(property: KProperty<*>): PropertyMetadata? {
                        TODO("Not yet implemented")
                    }
                    override fun get(propertyName: String): PropertyMetadata? {
                        TODO("Not yet implemented")
                    }
                }

                override fun get(classKey: ClassKey): ClassMetadata? {
                    TODO("Not yet implemented")
                }
            }
    }
    class MockMediator : Mediator {
        override fun companionOf(clazz: KClass<out BaseRealmObject>): RealmObjectCompanion {
            TODO("Not yet implemented")
        }
        override fun createInstanceOf(clazz: KClass<out BaseRealmObject>): RealmObjectInternal {
            TODO("Not yet implemented")
        }
    }
}
