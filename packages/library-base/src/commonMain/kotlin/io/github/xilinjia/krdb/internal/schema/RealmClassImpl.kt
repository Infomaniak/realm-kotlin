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

import io.github.xilinjia.krdb.internal.interop.ClassInfo
import io.github.xilinjia.krdb.internal.interop.PropertyInfo
import io.github.xilinjia.krdb.schema.RealmClass
import io.github.xilinjia.krdb.schema.RealmClassKind
import io.github.xilinjia.krdb.schema.RealmProperty
import io.github.xilinjia.krdb.schema.ValuePropertyType

// TODO Public due to being a transitive dependency to RealmObjectCompanion
public data class RealmClassImpl(
    // Optimization: Store the schema in the C-API alike structure directly from compiler plugin to
    // avoid unnecessary repeated initializations for realm_schema_new
    val cinteropClass: ClassInfo,
    val cinteropProperties: List<PropertyInfo>
) : RealmClass {

    override val name: String = cinteropClass.name
    override val properties: Collection<RealmProperty> = cinteropProperties.map {
        RealmPropertyImpl.fromCoreProperty(it)
    }
    override val primaryKey: RealmProperty? = properties.firstOrNull {
        it.type.run { this is ValuePropertyType && isPrimaryKey }
    }

    override val kind: RealmClassKind
        get() = when {
            cinteropClass.isEmbedded -> RealmClassKind.EMBEDDED
            cinteropClass.isAsymmetric -> RealmClassKind.ASYMMETRIC
            else -> RealmClassKind.STANDARD
        }

    override fun get(key: String): RealmProperty? = properties.firstOrNull { it.name == key }
}
