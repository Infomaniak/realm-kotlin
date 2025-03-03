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

package io.github.xilinjia.krdb.notifications.internal

import io.github.xilinjia.krdb.notifications.InitialResults
import io.github.xilinjia.krdb.notifications.ListChangeSet
import io.github.xilinjia.krdb.notifications.UpdatedResults
import io.github.xilinjia.krdb.query.RealmResults
import io.github.xilinjia.krdb.types.BaseRealmObject

internal class InitialResultsImpl<T : BaseRealmObject>(
    override val list: RealmResults<T>
) : InitialResults<T>

internal class UpdatedResultsImpl<T : BaseRealmObject>(
    override val list: RealmResults<T>,
    listChangeSet: ListChangeSet
) : UpdatedResults<T>, ListChangeSet by listChangeSet
