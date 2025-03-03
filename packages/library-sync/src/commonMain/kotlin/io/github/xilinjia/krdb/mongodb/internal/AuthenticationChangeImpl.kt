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
package io.github.xilinjia.krdb.mongodb.internal

import io.github.xilinjia.krdb.mongodb.LoggedIn
import io.github.xilinjia.krdb.mongodb.LoggedOut
import io.github.xilinjia.krdb.mongodb.Removed
import io.github.xilinjia.krdb.mongodb.User

internal class LoggedInImpl(override val user: User) : LoggedIn
internal class LoggedOutImpl(override val user: User) : LoggedOut
internal class RemovedImpl(override val user: User) : Removed
