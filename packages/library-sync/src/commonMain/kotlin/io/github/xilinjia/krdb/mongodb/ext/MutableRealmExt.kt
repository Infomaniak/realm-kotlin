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

package io.github.xilinjia.krdb.mongodb.ext

import io.github.xilinjia.krdb.MutableRealm
import io.github.xilinjia.krdb.UpdatePolicy
import io.github.xilinjia.krdb.internal.RealmObjectInternal
import io.github.xilinjia.krdb.mongodb.annotations.ExperimentalAsymmetricSyncApi
import io.github.xilinjia.krdb.types.AsymmetricRealmObject

/**
 * Insert an [AsymmetricRealmObject] into Realm. Since asymmetric objects are "write-only", it is
 * not possible to access the managed data after it has been inserted.
 *
 * @param obj the object to insert.
 * @throws IllegalArgumentException if the object graph of [obj] either contains an object
 * with a primary key value that already exists or an object from a previous version.
 */
@ExperimentalAsymmetricSyncApi
public fun <T : AsymmetricRealmObject> MutableRealm.insert(obj: T) {
    @Suppress("invisible_member", "invisible_reference")
    if (this is io.github.xilinjia.krdb.internal.InternalMutableRealm) {
        val obj = io.github.xilinjia.krdb.internal.copyToRealm(
            configuration.mediator,
            realmReference,
            obj,
            UpdatePolicy.ERROR
        )
        (obj as RealmObjectInternal).io_realm_kotlin_objectReference!!.objectPointer.release()
    } else {
        throw IllegalStateException("Calling insert() on $this is not supported.")
    }
}
