package com.codelezz.kotlin.bundles

import java.io.File
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Load a jar file in to memory.
 */
object BundleClassLoader : ClassLoader() {
    private const val CLASS_SUFFIX = ".class"

    /**
     * Load jar.
     */
    fun loadJar(file: File, loader: ClassLoader) {
        val jar = JarFile(file)
        val jarContents: Enumeration<JarEntry> = jar.entries()
        val cls = jarContents.asSequence()
            .filter { !it.isDirectory && it.name.endsWith(CLASS_SUFFIX) }
            .map { it.name.substring(0, it.name.length - CLASS_SUFFIX.length).replace('/', '.') }
            .filter { findLoadedClass(it) == null }
            .toList()
        for (it in cls) {
            loader.loadClass(it)
        }
    }
}
