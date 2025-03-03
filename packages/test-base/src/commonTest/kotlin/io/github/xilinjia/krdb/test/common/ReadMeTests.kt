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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.xilinjia.krdb.test.common

import io.github.xilinjia.krdb.Realm
import io.github.xilinjia.krdb.RealmConfiguration
import io.github.xilinjia.krdb.ext.asFlow
import io.github.xilinjia.krdb.ext.query
import io.github.xilinjia.krdb.ext.realmListOf
import io.github.xilinjia.krdb.internal.platform.singleThreadDispatcher
import io.github.xilinjia.krdb.notifications.DeletedList
import io.github.xilinjia.krdb.notifications.DeletedObject
import io.github.xilinjia.krdb.notifications.InitialList
import io.github.xilinjia.krdb.notifications.InitialObject
import io.github.xilinjia.krdb.notifications.InitialRealm
import io.github.xilinjia.krdb.notifications.InitialResults
import io.github.xilinjia.krdb.notifications.ListChange
import io.github.xilinjia.krdb.notifications.ObjectChange
import io.github.xilinjia.krdb.notifications.PendingObject
import io.github.xilinjia.krdb.notifications.RealmChange
import io.github.xilinjia.krdb.notifications.ResultsChange
import io.github.xilinjia.krdb.notifications.SingleQueryChange
import io.github.xilinjia.krdb.notifications.UpdatedList
import io.github.xilinjia.krdb.notifications.UpdatedObject
import io.github.xilinjia.krdb.notifications.UpdatedRealm
import io.github.xilinjia.krdb.notifications.UpdatedResults
import io.github.xilinjia.krdb.test.platform.PlatformUtils
import io.github.xilinjia.krdb.types.RealmList
import io.github.xilinjia.krdb.types.RealmObject
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Class for source code snippets that are part of our README.
 *
 * NOTE: If changing tests in this file, you would also have to update the corresponding snippets
 * in the README.
 */
class ReadMeTests {
    private lateinit var scope: CoroutineScope
    private lateinit var context: CloseableCoroutineDispatcher
    lateinit var tmpDir: String
    lateinit var realm: Realm

    @BeforeTest
    fun setup() {
        context = singleThreadDispatcher("test-dispatcher")
        scope = CoroutineScope(context)

        tmpDir = PlatformUtils.createTempDir()
        val configuration =
            RealmConfiguration.Builder(schema = setOf(Person::class, Dog::class))
                .directory(tmpDir)
                .build()
        realm = Realm.open(configuration)
    }

    @AfterTest
    fun tearDown() {
        scope.cancel()
        context.cancel()
        realm.close()
        context.close()
        PlatformUtils.deleteTempDir(tmpDir)
    }

    class Dog : RealmObject {
        var name: String = "NN"
        var age: Int = 0
    }

    class Person : RealmObject {
        var name: String = ""
        var dog: Dog? = null
        var addresses: RealmList<String> = realmListOf()
    }

    @Test
    fun query() {
        // ## Query example begin
        val all = realm.query<Person>().find()

        // Person named 'Carlo'
        val personsByNameQuery = realm.query<Person>("name = $0", "Carlo")
        val filteredByName = personsByNameQuery.find()

        // Person having a dog aged more than 7 with a name starting with 'Fi'
        val filteredByDog =
            realm.query<Person>("dog.age > $0 AND dog.name BEGINSWITH $1", 7, "Fi").find()

        // Observing for changes with Kotlin Coroutine Flows
        scope.async {
            personsByNameQuery.asFlow().collect { result ->
                println("Realm updated: Number of persons is ${result.list.size}")
            }
        }
        // ## Query example end
    }

    @Test
    fun delete() {
        // ## Delete example begin
        realm.writeBlocking {
            // Selected by a query
            val query = this.query<Dog>()
            delete(query)

            // From a results
            val results = query.find()
            delete(results)

            // From individual objects
            results.forEach { delete(it) }
        }
        // ## Delete example end
    }

    @Test
    fun notifications_realm() {
        // Subscribe for change notifications on a Realm instance
        scope.async {
            // ### Realm example begin
            realm.asFlow()
                .collect { realmChange: RealmChange<Realm> ->
                    when (realmChange) {
                        is InitialRealm<*> -> println("Initial Realm")
                        is UpdatedRealm<*> -> println("Realm updated")
                    }
                }
            // ### Realm example end
        }
        // out: "Initial Realm"

        // Write data
        realm.writeBlocking {
            copyToRealm(Person())
        }
        // out: "Realm updated"
    }

    @Test
    fun notifications_realmObject() {
        // Person named Carlo
        val person = realm.writeBlocking {
            copyToRealm(Person().apply { name = "Carlo" })
        }

        // Subscribe for change notifications on person
        scope.async {
            // ### RealmObject example begin
            person.asFlow().collect { objectChange: ObjectChange<Person> ->
                when (objectChange) {
                    is InitialObject -> println("Initial object: ${objectChange.obj.name}")
                    is UpdatedObject ->
                        println("Updated object: ${objectChange.obj.name}, changed fields: ${objectChange.changedFields.size}")
                    is DeletedObject -> println("Deleted object")
                }
            }
            // ### RealmObject example end
        }
        // out: "Initial object: Carlo"

        // Change person field `dog`
        realm.writeBlocking {
            findLatest(person)?.dog = Dog()
        }
        // out: "Updated object: Carlo, changed fields: 1"

        // Delete person
        realm.writeBlocking {
            findLatest(person)?.let { delete(it) }
        }
        // out: "Deleted object"
    }

    @Test
    fun notifications_realmList() {
        // Person named Carlo
        val person = realm.writeBlocking {
            copyToRealm(Person().apply { name = "Carlo" })
        }

        // Subscribe for RealmList change notifications
        scope.async {
            // ### RealmLists example begin
            person.addresses.asFlow()
                .collect { listChange: ListChange<String> ->
                    when (listChange) {
                        is InitialList -> println("Initial list size: ${listChange.list.size}")
                        is UpdatedList -> println("Updated list size: ${listChange.list.size} insertions ${listChange.insertions.size}")
                        is DeletedList -> println("Deleted list")
                    }
                }
            // ### RealmLists example end
        }
        // out: "Initial list size: 0"

        // Add an element to the list
        realm.writeBlocking {
            findLatest(person)?.addresses?.add("123 Fake Street")
        }
        // out: Updated list size: 0 insertions 1"

        // Remove the object that holds the list
        realm.writeBlocking {
            findLatest(person)?.let { delete(it) }
        }
        // out: "Deleted list"
    }

    @Test
    fun notifications_realmQuery() {
        // Subscribe for change notifications on a query
        scope.async {
            // ### RealmQuery example begin
            realm.query<Person>().asFlow()
                .collect { resultsChange: ResultsChange<Person> ->
                    when (resultsChange) {
                        is InitialResults -> println("Initial results size: ${resultsChange.list.size}")
                        is UpdatedResults -> println("Updated results size: ${resultsChange.list.size} insertions ${resultsChange.insertions.size}")
                    }
                }
            // ### RealmQuery example end
        }
        // out: "Initial results size: 0"

        // Add an element that matches the query filter
        realm.writeBlocking {
            copyToRealm(Person().apply { name = "Carlo" })
        }
        // out: Updated results size: 0 insertions 1"
    }

    @Test
    fun notifications_realmSingleQuery() {
        // Subscribe for a single object query change notifications
        scope.async {
            // ### RealmSingleQuery example begin
            realm.query<Person>("name = $0", "Carlo").first().asFlow()
                .collect { objectChange: SingleQueryChange<Person> ->
                    when (objectChange) {
                        is PendingObject -> println("Pending object")
                        is InitialObject -> println("Initial object: ${objectChange.obj.name}")
                        is UpdatedObject -> println("Updated object: ${objectChange.obj.name}, changed fields: ${objectChange.changedFields.size}")
                        is DeletedObject -> println("Deleted object")
                    }
                }
            // ### RealmSingleQuery example end
        }
        // out: "Pending object"

        // Insert an element that matches the query filter
        val person = realm.writeBlocking {
            copyToRealm(Person().apply { name = "Carlo" })
        }
        // out: "Initial object: Carlo"

        // Update one field of the inserted element
        realm.writeBlocking {
            findLatest(person)?.dog = Dog()
        }
        // out: "Updated object: Carlo, changed fields: 1"

        // Delete the element
        realm.writeBlocking {
            findLatest(person)?.let { delete(it) }
        }
        // out: "Deleted object"
    }
}
