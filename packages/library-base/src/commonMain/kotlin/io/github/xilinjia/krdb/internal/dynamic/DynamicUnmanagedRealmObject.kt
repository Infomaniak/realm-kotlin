@file:Suppress("UNCHECKED_CAST")

package io.github.xilinjia.krdb.internal.dynamic

import io.github.xilinjia.krdb.dynamic.DynamicMutableRealmObject
import io.github.xilinjia.krdb.dynamic.DynamicRealmObject
import io.github.xilinjia.krdb.ext.realmDictionaryOf
import io.github.xilinjia.krdb.ext.realmListOf
import io.github.xilinjia.krdb.ext.realmSetOf
import io.github.xilinjia.krdb.internal.RealmObjectInternal
import io.github.xilinjia.krdb.internal.RealmObjectReference
import io.github.xilinjia.krdb.query.RealmResults
import io.github.xilinjia.krdb.types.BaseRealmObject
import io.github.xilinjia.krdb.types.RealmDictionary
import io.github.xilinjia.krdb.types.RealmList
import io.github.xilinjia.krdb.types.RealmSet
import kotlin.reflect.KClass

internal class DynamicUnmanagedRealmObject(
    override val type: String,
    properties: Map<String, Any?>
) : DynamicMutableRealmObject, RealmObjectInternal {

    @Suppress("SpreadOperator")
    constructor(type: String, vararg properties: Pair<String, Any?>) : this(
        type,
        mapOf(*properties)
    )

    val properties: MutableMap<String, Any?> = properties.toMutableMap()

    override fun <T : Any> getValue(propertyName: String, clazz: KClass<T>): T =
        properties[propertyName] as T

    override fun <T : Any> getNullableValue(propertyName: String, clazz: KClass<T>): T? =
        properties[propertyName] as T?

    override fun getObject(propertyName: String): DynamicMutableRealmObject? =
        properties[propertyName] as DynamicMutableRealmObject?

    override fun <T : Any> getValueList(propertyName: String, clazz: KClass<T>): RealmList<T> =
        properties.getOrPut(propertyName) { realmListOf<T>() } as RealmList<T>

    override fun <T : Any> getNullableValueList(
        propertyName: String,
        clazz: KClass<T>
    ): RealmList<T?> = properties.getOrPut(propertyName) { realmListOf<T?>() } as RealmList<T?>

    override fun getObjectList(propertyName: String): RealmList<DynamicMutableRealmObject> =
        properties.getOrPut(propertyName) { realmListOf<DynamicMutableRealmObject>() }
            as RealmList<DynamicMutableRealmObject>

    override fun <T : Any> getValueSet(propertyName: String, clazz: KClass<T>): RealmSet<T> =
        properties.getOrPut(propertyName) { realmSetOf<T>() } as RealmSet<T>

    override fun <T : Any> getNullableValueSet(
        propertyName: String,
        clazz: KClass<T>
    ): RealmSet<T?> = properties.getOrPut(propertyName) { realmSetOf<T?>() } as RealmSet<T?>

    override fun <T : Any> getValueDictionary(
        propertyName: String,
        clazz: KClass<T>
    ): RealmDictionary<T> =
        properties.getOrPut(propertyName) { realmDictionaryOf<T?>() } as RealmDictionary<T>

    override fun <T : Any> getNullableValueDictionary(
        propertyName: String,
        clazz: KClass<T>
    ): RealmDictionary<T?> =
        properties.getOrPut(propertyName) { realmDictionaryOf<T?>() } as RealmDictionary<T?>

    override fun getBacklinks(propertyName: String): RealmResults<out DynamicRealmObject> =
        throw IllegalStateException("Unmanaged dynamic realm objects do not support backlinks.")

    override fun getObjectSet(propertyName: String): RealmSet<DynamicMutableRealmObject> =
        properties.getOrPut(propertyName) { realmSetOf<DynamicMutableRealmObject>() }
            as RealmSet<DynamicMutableRealmObject>

    override fun getObjectDictionary(
        propertyName: String
    ): RealmDictionary<DynamicMutableRealmObject?> =
        properties.getOrPut(propertyName) { realmDictionaryOf<DynamicMutableRealmObject>() }
            as RealmDictionary<DynamicMutableRealmObject?>

    override fun <T> set(propertyName: String, value: T): DynamicMutableRealmObject {
        properties[propertyName] = value as Any
        return this
    }

    override var io_realm_kotlin_objectReference: RealmObjectReference<out BaseRealmObject>? = null
}
