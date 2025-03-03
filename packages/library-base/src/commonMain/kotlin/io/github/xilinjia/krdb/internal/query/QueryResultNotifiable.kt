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

package io.github.xilinjia.krdb.internal.query

import io.github.xilinjia.krdb.internal.ChangeFlow
import io.github.xilinjia.krdb.internal.CoreNotifiable
import io.github.xilinjia.krdb.internal.LiveRealm
import io.github.xilinjia.krdb.internal.Mediator
import io.github.xilinjia.krdb.internal.Notifiable
import io.github.xilinjia.krdb.internal.RealmResultsImpl
import io.github.xilinjia.krdb.internal.ResultChangeFlow
import io.github.xilinjia.krdb.internal.interop.ClassKey
import io.github.xilinjia.krdb.internal.interop.NativePointer
import io.github.xilinjia.krdb.internal.interop.RealmResultsT
import io.github.xilinjia.krdb.notifications.ResultsChange
import io.github.xilinjia.krdb.types.BaseRealmObject
import kotlinx.coroutines.channels.ProducerScope
import kotlin.reflect.KClass

internal class QueryResultNotifiable<E : BaseRealmObject>(
    val results: NativePointer<RealmResultsT>,
    val classKey: ClassKey,
    val clazz: KClass<E>,
    val mediator: Mediator
) : Notifiable<RealmResultsImpl<E>, ResultsChange<E>> {
    override fun coreObservable(liveRealm: LiveRealm): CoreNotifiable<RealmResultsImpl<E>, ResultsChange<E>>? {
        return thawResults(liveRealm.realmReference, results, classKey, clazz, mediator)
    }

    override fun changeFlow(scope: ProducerScope<ResultsChange<E>>): ChangeFlow<RealmResultsImpl<E>, ResultsChange<E>> {
        return ResultChangeFlow(scope)
    }
}
