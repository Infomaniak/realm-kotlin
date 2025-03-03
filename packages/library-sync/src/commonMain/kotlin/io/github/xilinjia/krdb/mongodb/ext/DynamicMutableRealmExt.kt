package io.github.xilinjia.krdb.mongodb.ext

import io.github.xilinjia.krdb.UpdatePolicy
import io.github.xilinjia.krdb.dynamic.DynamicMutableRealm
import io.github.xilinjia.krdb.dynamic.DynamicMutableRealmObject
import io.github.xilinjia.krdb.dynamic.DynamicRealmObject
import io.github.xilinjia.krdb.internal.dynamic.DynamicMutableRealmImpl
import io.github.xilinjia.krdb.mongodb.annotations.ExperimentalAsymmetricSyncApi
import io.github.xilinjia.krdb.schema.RealmClassKind
import io.github.xilinjia.krdb.types.AsymmetricRealmObject

/**
 * Insert a dynamic version of a [AsymmetricRealmObject] into a realm. Since asymmetric objects are
 * "write-only", it is not possible to access the managed data after it has been inserted.
 *
 * @param obj the asymmetric object to insert.
 * @throws IllegalArgumentException if the object is not an asymmetric object, the object graph
 * of [obj] either contains an object with a primary key value that already exists or an object from
 * a previous version, or if a property does not match the underlying schema.
 */
@ExperimentalAsymmetricSyncApi
public fun DynamicMutableRealm.insert(obj: DynamicRealmObject) {
    val kind: RealmClassKind? = (this as DynamicMutableRealmImpl).realmReference.owner.schema()[obj.type]?.kind
    if (kind != RealmClassKind.ASYMMETRIC) {
        throw IllegalArgumentException("Only asymmetric objects are supported, ${obj.type} is a $kind")
    }
    @Suppress("invisible_member", "invisible_reference")
    val obj = io.github.xilinjia.krdb.internal.copyToRealm(configuration.mediator, realmReference, obj, UpdatePolicy.ERROR, mutableMapOf()) as DynamicMutableRealmObject
    @Suppress("invisible_member", "invisible_reference")
    ((obj as io.github.xilinjia.krdb.internal.dynamic.DynamicMutableRealmObjectImpl).io_realm_kotlin_objectReference!!.objectPointer).release()
}
