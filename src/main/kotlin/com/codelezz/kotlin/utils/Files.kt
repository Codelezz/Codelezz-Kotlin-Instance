@file:Suppress("unused")
package com.codelezz.kotlin.utils

import java.io.File

/**
 * Get a file from a directory.
 */
operator fun File.get(path: String) = File(this, path)

/**
 * Check if a file exits in a directory.
 */
operator fun File.contains(name: String) = name in list().orEmpty()
