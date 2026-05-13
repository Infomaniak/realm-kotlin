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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.gitlab.arturbosch.detekt.Detekt
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

private fun locateConfigDir(current: File): File {
    val configDir = Paths.get(current.path, "config")
    return if (Files.exists(configDir) && File(configDir.toUri()).isDirectory) {
        configDir.toFile()
    } else {
        val parent = current.parentFile ?: error("Couldn't locate config folder upwards in the file tree")
        locateConfigDir(parent)
    }
}

class RealmLintPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val configDir = locateConfigDir(target.rootDir.absoluteFile).path

        target.allprojects {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            val ktlint = configurations.create("ktlint")

            dependencies.add("ktlint", "com.pinterest:ktlint:${Versions.ktlint}")

            val outputDir = "${project.buildDir}/reports/ktlint/"
            val inputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))

            tasks.register("ktlintCheck", JavaExec::class.java) {
                inputs.files(inputFiles)
                outputs.dir(outputDir)

                description = "Check Kotlin code style."
                classpath = ktlint
                jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
                mainClass.set("com.pinterest.ktlint.Main")
                args(
                    "src/**/*.kt",
                    "!src/**/generated/**",
                    "!src/**/resources/**",
                    "--reporter=plain",
                    "--reporter=html,output=${project.buildDir}/reports/ktlint/ktlint.html",
                    "--reporter=checkstyle,output=${project.buildDir}/reports/ktlint/ktlint.xml",
                    "--editorconfig=${configDir}/ktlint/.editorconfig"
                )
            }

            tasks.register("ktlintFormat", JavaExec::class.java) {
                inputs.files(inputFiles)
                outputs.dir(outputDir)

                description = "Fix Kotlin code style deviations."
                classpath = ktlint
                jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
                mainClass.set("com.pinterest.ktlint.Main")
                args(
                    "-F",
                    "src/**/*.kt",
                    "!src/**/resources/**"
                )
            }

            extensions.configure(io.gitlab.arturbosch.detekt.extensions.DetektExtension::class.java) {
                buildUponDefaultConfig = true
                config.from(files("$configDir/detekt/detekt.yml"))
                baseline = file("$configDir/detekt/baseline.xml")
                source.setFrom(
                    files(
                        file("src/androidMain/kotlin"),
                        file("src/androidAndroidTest/kotlin"),
                        file("src/androidTest/kotlin"),
                        file("src/commonMain/kotlin"),
                        file("src/commonTest/kotlin"),
                        file("src/darwin/kotlin"),
                        file("src/ios/kotlin"),
                        file("src/iosMain/kotlin"),
                        file("src/iosTest/kotlin"),
                        file("src/jvm/kotlin"),
                        file("src/jvmMain/kotlin"),
                        file("src/main/kotlin"),
                        file("src/macosMain/kotlin"),
                        file("src/macosTest/kotlin"),
                        file("src/test/kotlin")
                    )
                )
            }

            tasks.withType(Detekt::class.java).configureEach {
                reports {
                    xml.required.set(true)
                    html.required.set(true)
                    txt.required.set(true)
                    sarif.required.set(true)
                    md.required.set(true)
                }
            }
        }
    }
}
