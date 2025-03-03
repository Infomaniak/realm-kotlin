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

package io.github.xilinjia.krdb.internal.schema

import io.github.xilinjia.krdb.internal.interop.CollectionType
import io.github.xilinjia.krdb.internal.interop.PropertyInfo
import io.github.xilinjia.krdb.schema.ListPropertyType
import io.github.xilinjia.krdb.schema.MapPropertyType
import io.github.xilinjia.krdb.schema.RealmProperty
import io.github.xilinjia.krdb.schema.RealmPropertyType
import io.github.xilinjia.krdb.schema.SetPropertyType
import io.github.xilinjia.krdb.schema.ValuePropertyType

internal data class RealmPropertyImpl(
    override var name: String,
    override var type: RealmPropertyType,
) : RealmProperty {

    override val isNullable: Boolean = when (type) {
        is ValuePropertyType -> type.isNullable
        is ListPropertyType -> false
        is SetPropertyType -> false
        is MapPropertyType -> false
    }

    companion object {
        fun fromCoreProperty(corePropertyImpl: PropertyInfo): RealmPropertyImpl {
            return with(corePropertyImpl) {
                val storageType = RealmStorageTypeImpl.fromCorePropertyType(type)
                val type = when (collectionType) {
                    CollectionType.RLM_COLLECTION_TYPE_NONE -> ValuePropertyType(
                        storageType,
                        isNullable,
                        isPrimaryKey,
                        isIndexed,
                        isFullTextIndexed
                    )
                    CollectionType.RLM_COLLECTION_TYPE_LIST -> ListPropertyType(
                        storageType,
                        isNullable,
                        isComputed
                    )
                    CollectionType.RLM_COLLECTION_TYPE_SET -> SetPropertyType(
                        storageType,
                        isNullable
                    )
                    CollectionType.RLM_COLLECTION_TYPE_DICTIONARY -> MapPropertyType(
                        storageType,
                        isNullable
                    )
                    else -> error("Unsupported type $collectionType")
                }
                RealmPropertyImpl(name, type)
            }
        }
    }
}
