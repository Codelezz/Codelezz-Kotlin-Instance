@file:Suppress("unused")
package com.codelezz.kotlin.utils

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.codelezz.kotlin.utils.Exclude.During.*
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

@Retention(value = AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
/**
 * Excludes field(s) from json serialization and/or deserialization depending on [during]
 * @param during When to exclude field from serialization and/or deserialization
 */
annotation class Exclude(val during: During = BOTH) {
    /**
     * @see SERIALIZATION Exclude field ONLY from json serialization
     * @see DESERIALIZATION Exclude field ONLY from json deserialization
     * @see BOTH Exclude field from json serialization and deserialization
     */
    enum class During {
        /**
         * Exclude field ONLY from json serialization
         */
        SERIALIZATION,

        /**
         * Exclude field ONLY from json deserialization
         */
        DESERIALIZATION,

        /**
         * Exclude field from json serialization and deserialization
         */
        BOTH
    }
}

internal fun getExclusionStrategy(during: Exclude.During = BOTH): ExclusionStrategy {
    return object : ExclusionStrategy {
        override fun shouldSkipClass(clazz: Class<*>?): Boolean {
            return false
        }

        override fun shouldSkipField(f: FieldAttributes?): Boolean {
            return if (f == null) true else {
                val annotation = f.getAnnotation(Exclude::class.java)
                if (annotation == null) {
                    false
                } else {
                    if (during == BOTH || annotation.during == BOTH) return true
                    annotation.during == during
                }
            }
        }
    }
}

fun KProperty<*>.excluded(during: Exclude.During = BOTH): Boolean {
    val ex = this.findAnnotation<Exclude>() ?: return false
    if (during == BOTH || ex.during == BOTH) return true
    return during == ex.during
}

fun Collection<KProperty<*>>.filterExcludeAnnotation(during: Exclude.During = BOTH): List<KProperty<*>> =
    filter { !it.excluded(during) }