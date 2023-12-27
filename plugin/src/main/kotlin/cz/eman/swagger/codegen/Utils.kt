package cz.eman.swagger.codegen

import org.gradle.api.Project
import org.gradle.api.tasks.TaskCollection
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

internal fun Project.kotlinCompileTasks(): TaskCollection<KotlinCompile<*>> =
    tasks.withType(KotlinCompile::class.java)
