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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.xilinjia.krdb.entities.backlink

import io.github.xilinjia.krdb.ext.backlinks
import io.github.xilinjia.krdb.ext.realmDictionaryOf
import io.github.xilinjia.krdb.ext.realmListOf
import io.github.xilinjia.krdb.ext.realmSetOf
import io.github.xilinjia.krdb.types.EmbeddedRealmObject
import io.github.xilinjia.krdb.types.RealmDictionary
import io.github.xilinjia.krdb.types.RealmList
import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.RealmSet
import io.github.xilinjia.krdb.types.RealmUUID
import io.github.xilinjia.krdb.types.annotations.Ignore
import org.mongodb.kbson.ObjectId

class Child : RealmObject {
    val parents by backlinks(Parent::child)
    val parentsByList by backlinks(Parent::childList)
    val parentsBySet by backlinks(Parent::childSet)
    val parentsByDictionary by backlinks(Parent::childDictionary)
}

class EmbeddedChild : EmbeddedRealmObject {
    var id = ObjectId()
    var parent: Parent? = null
    val parentViaBacklinks: Parent by backlinks(Parent::embeddedChild)
    val parent2ViaBacklinks: Parent2 by backlinks(Parent2::embeddedChild)
}

class Parent(var id: Int) : RealmObject {
    constructor() : this(0)

    var child: Child? = null
    var childList: RealmList<Child> = realmListOf()
    var childSet: RealmSet<Child> = realmSetOf()
    var childDictionary: RealmDictionary<Child?> = realmDictionaryOf()

    var embeddedChild: EmbeddedChild? = EmbeddedChild()
    val embeddedChildren by backlinks(EmbeddedChild::parent)
}

class Parent2(var id: Int) : RealmObject {
    constructor() : this(0)
    var embeddedChild: EmbeddedChild? = EmbeddedChild()
}

class Recursive : RealmObject {
    var name: RealmUUID = RealmUUID.random()
    var uuidSet: RealmSet<RealmUUID> = realmSetOf()
    var uuidList: RealmList<RealmUUID> = realmListOf()
    var uuidDictionary: RealmDictionary<RealmUUID> = realmDictionaryOf()

    var recursiveField: Recursive? = null
    val references by backlinks(Recursive::recursiveField)
}

class MissingSourceProperty : RealmObject {
    @Ignore
    var reference: MissingSourceProperty? = null
    val references by backlinks(MissingSourceProperty::reference)
}
