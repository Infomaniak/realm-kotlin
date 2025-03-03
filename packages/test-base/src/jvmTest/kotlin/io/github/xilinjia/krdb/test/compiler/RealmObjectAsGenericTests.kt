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

@file:OptIn(ExperimentalCompilerApi::class)

package io.github.xilinjia.krdb.test.compiler

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.github.xilinjia.krdb.test.util.Compiler.compileFromSource
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import kotlin.test.assertEquals

class RealmObjectAsGenericTests {

    @Test
    fun `object as generic`() {
        val result = compileFromSource(
            source = SourceFile.kotlin(
                "object_as_generic.kt",
                """
                    import io.github.xilinjia.krdb.types.BaseRealmObject
                    import io.github.xilinjia.krdb.types.EmbeddedRealmObject
                    import io.github.xilinjia.krdb.types.RealmObject

                    open class BaseClass<T : BaseRealmObject>
                    class Foo : BaseClass<RealmObject>()
                    class Bar : BaseClass<io.github.xilinjia.krdb.types.RealmObject>()
                    class RealmObjectAsGenericsFoo : BaseClass<RealmObject>()
                    class RealmObjectAsGenericsBar : BaseClass<io.github.xilinjia.krdb.types.RealmObject>()
                    class RealmObjectFoo : RealmObject, BaseClass<RealmObject>()
                    class RealmObjectBar : io.github.xilinjia.krdb.types.RealmObject, BaseClass<io.github.xilinjia.krdb.types.RealmObject>()
                    class EmbeddedRealmObjectAsGenericsFoo : BaseClass<EmbeddedRealmObject>()
                    class EmbeddedRealmObjectAsGenericsBar : BaseClass<io.github.xilinjia.krdb.types.EmbeddedRealmObject>()
                    class EmbeddedObjectFoo : EmbeddedRealmObject, BaseClass<EmbeddedRealmObject>()
                    class EmbeddedObjectBar : io.github.xilinjia.krdb.types.EmbeddedRealmObject, BaseClass<io.github.xilinjia.krdb.types.EmbeddedRealmObject>()
                """.trimIndent()
            )
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
