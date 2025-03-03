/*
 * Copyright 2022 Realm Inc.
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

package io.github.xilinjia.krdb.internal.dynamic

import io.github.xilinjia.krdb.dynamic.DynamicRealm
import io.github.xilinjia.krdb.dynamic.DynamicRealmObject
import io.github.xilinjia.krdb.internal.BaseRealmImpl
import io.github.xilinjia.krdb.internal.FrozenRealmReferenceImpl
import io.github.xilinjia.krdb.internal.InternalConfiguration
import io.github.xilinjia.krdb.internal.RealmReference
import io.github.xilinjia.krdb.internal.interop.FrozenRealmPointer
import io.github.xilinjia.krdb.internal.query.ObjectQuery
import io.github.xilinjia.krdb.internal.schema.RealmSchemaImpl
import io.github.xilinjia.krdb.query.RealmQuery
import io.github.xilinjia.krdb.schema.RealmClassKind
import io.github.xilinjia.krdb.schema.RealmSchema

internal open class DynamicRealmImpl(
    configuration: InternalConfiguration,
    dbPointer: FrozenRealmPointer
) : BaseRealmImpl(configuration), DynamicRealm {

    override val realmReference: RealmReference = FrozenRealmReferenceImpl(this, dbPointer)

    override fun query(
        className: String,
        query: String,
        vararg args: Any?
    ): RealmQuery<DynamicRealmObject> {
        if (realmReference.owner.schema()[className]?.kind == RealmClassKind.ASYMMETRIC) {
            throw IllegalArgumentException("Queries on asymmetric objects are not allowed: $className")
        }
        return ObjectQuery(
            realmReference,
            realmReference.schemaMetadata.getOrThrow(className).classKey,
            DynamicRealmObject::class,
            configuration.mediator,
            query,
            args
        )
    }

    // FIXME Currently constructs a new instance on each invocation. We could cache this pr. schema
    //  update, but requires that we initialize it all on the actual schema update to allow freezing
    //  it. If we make the schema backed by the actual realm_class_info_t/realm_property_info_t
    //  initialization it would probably be acceptable to initialize on schema updates
    override fun schema(): RealmSchema {
        return RealmSchemaImpl.fromDynamicRealm(realmReference.dbPointer)
    }
}
