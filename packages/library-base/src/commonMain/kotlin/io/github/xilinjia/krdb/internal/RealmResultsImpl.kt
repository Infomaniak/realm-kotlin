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

package io.github.xilinjia.krdb.internal

import io.github.xilinjia.krdb.internal.RealmValueArgumentConverter.convertToQueryArgs
import io.github.xilinjia.krdb.internal.interop.Callback
import io.github.xilinjia.krdb.internal.interop.ClassKey
import io.github.xilinjia.krdb.internal.interop.RealmChangesPointer
import io.github.xilinjia.krdb.internal.interop.RealmInterop
import io.github.xilinjia.krdb.internal.interop.RealmInterop.realm_results_get
import io.github.xilinjia.krdb.internal.interop.RealmKeyPathArrayPointer
import io.github.xilinjia.krdb.internal.interop.RealmNotificationTokenPointer
import io.github.xilinjia.krdb.internal.interop.RealmResultsPointer
import io.github.xilinjia.krdb.internal.interop.getterScope
import io.github.xilinjia.krdb.internal.interop.inputScope
import io.github.xilinjia.krdb.internal.query.ObjectQuery
import io.github.xilinjia.krdb.internal.util.Validation.sdkError
import io.github.xilinjia.krdb.notifications.ResultsChange
import io.github.xilinjia.krdb.notifications.internal.InitialResultsImpl
import io.github.xilinjia.krdb.notifications.internal.UpdatedResultsImpl
import io.github.xilinjia.krdb.query.RealmQuery
import io.github.xilinjia.krdb.query.RealmResults
import io.github.xilinjia.krdb.query.TRUE_PREDICATE
import io.github.xilinjia.krdb.types.BaseRealmObject
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * Primitive results are not exposed through the public API but might be needed when implementing
 * `RealmDictionary.values` as Core returns those as results.
 */
// TODO OPTIMIZE Perhaps we should map the output of dictionary.values to a RealmList so that
//  primitive typed results are never ever exposed publicly.
// TODO OPTIMIZE We create the same type every time, so don't have to perform map/distinction every time
internal class RealmResultsImpl<E : BaseRealmObject> constructor(
    internal val realm: RealmReference,
    internal val nativePointer: RealmResultsPointer,
    private val classKey: ClassKey,
    private val clazz: KClass<E>,
    private val mediator: Mediator,
    @Suppress("UnusedPrivateMember")
    private val mode: Mode = Mode.RESULTS,
) : AbstractList<E>(), RealmResults<E>, InternalDeleteable, CoreNotifiable<RealmResultsImpl<E>, ResultsChange<E>>, RealmStateHolder {

    internal enum class Mode {
        // FIXME Needed to make working with @LinkingObjects easier.
        EMPTY, // RealmResults that is always empty.
        RESULTS // RealmResults wrapping a Realm Core Results.
    }

    override val size: Int
        get() = RealmInterop.realm_results_count(nativePointer).toInt()

    override fun get(index: Int): E = getterScope {
        realmValueToRealmObject(
            realm_results_get(nativePointer, index.toLong()),
            clazz,
            mediator,
            realm
        ) as E
    }

    override fun query(query: String, vararg args: Any?): RealmQuery<E> = inputScope {
        // If an empty query is passed in, reconstruct the original query backing this RealmResults
        val queryPointer = if (query.trim().compareTo(TRUE_PREDICATE, ignoreCase = true) == 0 && args.isEmpty()) {
            RealmInterop.realm_results_get_query(nativePointer)
        } else {
            try {
                RealmInterop.realm_query_parse_for_results(
                    nativePointer,
                    query,
                    convertToQueryArgs(args)
                )
            } catch (e: IndexOutOfBoundsException) {
                throw IllegalArgumentException(e.message, e.cause)
            }
        }
        ObjectQuery(
            realm,
            classKey,
            clazz,
            mediator,
            queryPointer,
        )
    }

    override fun asFlow(keyPaths: List<String>?): Flow<ResultsChange<E>> {
        realm.checkClosed()
        val keyPathInfo = keyPaths?.let {
            Pair(classKey, keyPaths)
        }
        return realm.owner.registerObserver(this, keyPathInfo)
    }

    override fun delete() {
        // TODO OPTIMIZE Are there more efficient ways to do this? realm_query_delete_all is not
        //  available in C-API yet
        RealmInterop.realm_results_delete_all(nativePointer)
    }

    /**
     * Returns a frozen copy of this query result. If it is already frozen, the same instance
     * is returned.
     */
    override fun freeze(frozenRealm: RealmReference): RealmResultsImpl<E> {
        val frozenDbPointer = frozenRealm.dbPointer
        val frozenResults = RealmInterop.realm_results_resolve_in(nativePointer, frozenDbPointer)
        return RealmResultsImpl(frozenRealm, frozenResults, classKey, clazz, mediator)
    }

    /**
     * Thaw the frozen query result, turning it back into a live, thread-confined RealmResults.
     */
    override fun thaw(liveRealm: RealmReference): RealmResultsImpl<E> {
        val liveDbPointer = liveRealm.dbPointer
        val liveResultPtr = RealmInterop.realm_results_resolve_in(nativePointer, liveDbPointer)
        return RealmResultsImpl(liveRealm, liveResultPtr, classKey, clazz, mediator)
    }

    override fun registerForNotification(
        keyPaths: RealmKeyPathArrayPointer?,
        callback: Callback<RealmChangesPointer>
    ): RealmNotificationTokenPointer {
        return RealmInterop.realm_results_add_notification_callback(nativePointer, keyPaths, callback)
    }

    override fun changeFlow(scope: ProducerScope<ResultsChange<E>>): ChangeFlow<RealmResultsImpl<E>, ResultsChange<E>> =
        ResultChangeFlow(scope)

    override fun realmState(): RealmState = realm

    override fun isValid(): Boolean {
        return !nativePointer.isReleased() && !realm.isClosed()
    }
}

internal class ResultChangeFlow<E : BaseRealmObject>(scope: ProducerScope<ResultsChange<E>>) :
    ChangeFlow<RealmResultsImpl<E>, ResultsChange<E>>(scope) {

    override fun initial(frozenRef: RealmResultsImpl<E>): ResultsChange<E> =
        InitialResultsImpl(frozenRef)

    override fun update(
        frozenRef: RealmResultsImpl<E>,
        change: RealmChangesPointer
    ): ResultsChange<E> {
        val listChangeSetBuilderImpl = ListChangeSetBuilderImpl(change)
        return UpdatedResultsImpl(frozenRef, listChangeSetBuilderImpl.build())
    }

    override fun delete(): ResultsChange<E> =
        sdkError("Results should never have been deleted")
}
