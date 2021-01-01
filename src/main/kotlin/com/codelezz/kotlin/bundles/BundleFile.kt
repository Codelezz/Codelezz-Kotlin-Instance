package com.codelezz.kotlin.bundles

import com.codelezz.kotlin.CodelezzBase.gson
import com.google.gson.JsonObject
import java.io.File
import java.io.FileNotFoundException
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * An information holder for the module startup.
 * Here all actions for loading in modules have been defined.
 */
class BundleFile(val file: File) {
    lateinit var id: String
    lateinit var name: String
    lateinit var version: String
    private lateinit var mainClass: String

    /**
     * Load the json inside the jar.
     */
    fun loadJson() {
        val jar = JarFile(file)
        val entry = jar.getJarEntry("bundle.json") ?: throw FileNotFoundException("Jar does not contain bundle.json")
        val stream = jar.getInputStream(entry)
        val fileString = stream.bufferedReader().lineSequence().joinToString(separator = "")
        val json = gson.fromJson(fileString, JsonObject::class.java) // TODO: Transform this to a separate data class
        if(!json.isJsonObject) incomplete("json object")
        val obj = json.asJsonObject

        id = obj["id"]?.asString ?: incomplete("id")
        name = obj["name"]?.asString ?: incomplete("name")
        version =
            obj["version"]?.asString ?: incomplete("version")
        mainClass =
            obj["main"]?.asString ?: incomplete("main class")
    }

    private fun incomplete(type: String): Nothing =
        throw BundleJsonIncomplete("The bundle.json of ${file.name} does not contain a $type.")

    /**
     * Unload the jar file.
     */
    fun unload() {
        val jar = JarFile(file)
        jar.close()
    }

    /**
     * Check if the current version is the correct one.
     * This is used to determine which bundles need to be download.
     * Therefore, if the module should not be in the server this will return true.
     */
    fun shouldUpdate(): Boolean = false

    /**
     * Check if the bundle should be in the server.
     */
    fun needToDelete() = false

    /**
     * Can this bundle be updated.
     */
    fun canUpdate(): Boolean = !needToDelete()

    /**
     * Load the bundle in.
     */
    fun loadBundle(loader: URLClassLoader): Bundle {
        val classToLoad = Class.forName(mainClass, true, loader)
        if (!Bundle::class.java.isAssignableFrom(classToLoad)) {
            throw BundleFileNotFound(
                "$mainClass is not a Bundle. It needs to " +
                        "extend the Bundle class to work as a bundle"
            )
        }
        val cl = classToLoad as Class<Bundle>
        val klass: KClass<Bundle> = cl.kotlin
        val module = when {
            klass.objectInstance != null -> klass.objectInstance!!
            klass.primaryConstructor != null -> klass.primaryConstructor!!.call()
            else -> throw BundleConstructorNotFound("$mainClass has no primary constructor")
        }
        loadMuleFiles(loader)
        return module
    }

    /**
     * Loads the module files inside the jar.
     */
    private fun loadMuleFiles(loader: URLClassLoader) {
        BundleClassLoader.loadJar(file, loader)
    }

    /**
     * Deletes the module file.
     */
    fun delete() {
        if (file.exists()) file.delete()
    }

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        return when {
            this === other       -> true
            other !is BundleFile -> false
            else                 -> other.id == id
        }
    }
}

/**
 * If a module class could not been found.
 */
class BundleFileNotFound(message: String) : Exception(message)

/**
 * If a module class could not been found.
 */
class BundleJsonIncomplete(message: String) : Exception(message)

/**
 * If a module class has no constructor.
 */
class BundleConstructorNotFound(message: String) : Exception(message)
