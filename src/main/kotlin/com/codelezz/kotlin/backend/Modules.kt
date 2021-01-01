package com.codelezz.kotlin.backend

import com.codelezz.kotlin.CodelezzBase.gson
import com.codelezz.kotlin.nodes.Nodes
import com.codelezz.kotlin.utils.genericTypeInline
import com.codelezz.kotlin.utils.json
import java.io.ByteArrayInputStream
import java.util.*
import java.util.zip.GZIPInputStream

object Modules {

	/**
	 * Starts the connection between the backend and this instance.
	 * It will try to authenticate with the backend.
	 *
	 * If this instance has never be ran it will create a new user instance,
	 * and it will need to be linked with the backend.
	 * Guidance will be given to the players that can connect this instance.
	 */
	internal fun init() {
		val modules = fetchModules()
		println("Loaded ${modules.size} modules.")
		loadModules(modules)
		println("Parsed ${Nodes.nodesSize} nodes, and ${Nodes.linksSize} links.")
	}

	/**
	 * This will reinitialize the backend modules.
	 * Though this will not update the installed bundles.
	 */
	internal fun reInit() {
		val modules = fetchModules()
		loadModules(modules)
	}

	private fun fetchModules(): List<Module> {
		val responds = postAuthorized { url = "https://api.codelezz.com/fetch-modules" }.json()
		val links = responds["links"]
		if (links !is List<*>) return emptyList()

		return links.mapNotNull {
			if (it !is String) return@mapNotNull null
			val res = get {
				url = it
				contentType = " "
			}
			gson.fromJson<Module>(res, genericTypeInline<Module>())
		}
	}

	private fun loadModules(modules: List<Module>) {
		modules.forEach {
			Nodes.parse(it.nodes.decompress(), it.links.decompress())
		}
	}

	private fun String.decompress(): String {
		val bytes = Base64.getDecoder().decode(this)
		val gis = GZIPInputStream(ByteArrayInputStream(bytes))
		return gis.bufferedReader().lineSequence().joinToString(separator = "")
	}
}

private data class Module(
	val version: String,
	val publisher: String,
	val nodes: String,
	val links: String,
)