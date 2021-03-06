@file:JvmName("Token")
@file:Suppress("unused")

package com.codelezz.kotlin.utils

import com.codelezz.kotlin.CodelezzBase.gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Merge two maps to one map.
 */
fun <K, V> Map<K, V>.mergeReduce(other: Map<K, V>, reduce: (V, V) -> V = { _, b -> b }): Map<K, V> {
	val result = LinkedHashMap<K, V>(this.size + other.size)
	result.putAll(this)
	other.forEach { e -> result[e.key] = result[e.key]?.let { reduce(e.value, it) } ?: e.value }
	return result
}

/**
 * Parse a string to a json map.
 */
fun String.json(): Map<String, Any> = gson.fromJson(this, genericTypeInline<Map<String, Any>>())

/**
 * Get a generic type for a type token.
 */
fun <T> genericType(): Type = object : TypeToken<T>() {}.type

/**
 * Inline a generic type token.
 */
inline fun <reified T> genericTypeInline(): Type = object : TypeToken<T>() {}.type

/**
 * Filters the values so that there are no null values left.
 */
fun <K, V: Any> Map<out K, V?>.filterValuesNotNull(): Map<K, V> {
	val result = LinkedHashMap<K, V>()
	for (entry in this) {
		entry.value?.let {
			result[entry.key] = it
		}
	}
	return result
}

/**
 * Prints a message if the value of the boolean is false.
 */
infix fun Boolean.ifFailedPrint(message: Any): Boolean {
	if(!this) println(message)
	return this
}