package io.github.xilinjia.krdb.internal.query

import io.github.xilinjia.krdb.internal.Mediator
import io.github.xilinjia.krdb.internal.RealmReference
import io.github.xilinjia.krdb.internal.RealmResultsImpl
import io.github.xilinjia.krdb.internal.interop.ClassKey
import io.github.xilinjia.krdb.internal.interop.RealmInterop
import io.github.xilinjia.krdb.internal.interop.RealmResultsPointer
import io.github.xilinjia.krdb.types.BaseRealmObject
import kotlin.reflect.KClass

internal fun <T : BaseRealmObject> thawResults(
    liveRealm: RealmReference,
    resultsPointer: RealmResultsPointer,
    classKey: ClassKey,
    clazz: KClass<T>,
    mediator: Mediator
): RealmResultsImpl<T> {
    val liveResultPtr = RealmInterop.realm_results_resolve_in(resultsPointer, liveRealm.dbPointer)
    return RealmResultsImpl(liveRealm, liveResultPtr, classKey, clazz, mediator)
}
