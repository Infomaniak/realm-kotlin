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

import io.github.xilinjia.krdb.notifications.DeletedSet
import io.github.xilinjia.krdb.notifications.InitialSet
import io.github.xilinjia.krdb.notifications.SetChangeSet
import io.github.xilinjia.krdb.notifications.UpdatedSet
import io.github.xilinjia.krdb.types.RealmSet

internal class InitialSetImpl<T>(override val set: RealmSet<T>) : InitialSet<T>

internal class UpdatedSetImpl<T>(
    override val set: RealmSet<T>,
    setChangeSet: SetChangeSet
) : UpdatedSet<T>, SetChangeSet by setChangeSet

internal class DeletedSetImpl<T>(override val set: RealmSet<T>) : DeletedSet<T>
