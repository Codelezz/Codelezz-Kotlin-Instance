package com.codelezz.kotlin.nodes

import com.codelezz.kotlin.CodelezzBase.gson
import com.codelezz.kotlin.bundles.Bundles
import com.codelezz.kotlin.utils.*
import com.google.gson.annotations.SerializedName

object Nodes {
	private val nodes: MutableMap<String, Node> = mutableMapOf()
	private val links: MutableList<Link> = mutableListOf()

	val nodesSize: Int
		get() = nodes.size
	val linksSize: Int
		get() = links.size

	fun initializeNodes() {
		val finished = nodes.filterValues {
			it.selectors.all { sel ->
				!sel.connectorIn || !sel.needed || sel.defaultValue != null
			}
		}.map {
			Bundles.initializeNode(it.value) ifFailedPrint "Could not initialize ${it.value.nodeId}"
		}

		val completed = finished.all { it }

		if(!completed) {
			println("Node initialization could not be completed correctly.")
			println("Some bundles were not installed correctly.")
			println("If you reloaded, try restarting.")
		}
	}

	/**
	 * Finds the links from a selector.
	 *
	 * @param id is the id of the Selector.
	 */
	fun findLinks(id: String): List<Link> =
		links.filter { it.baseId == id }

	/**
	 * Find all the nodes from a link between selectors.
	 *
	 * @param id is the id of the Selector.
	 * @return A map with the selector id from the link and the node that it is from.
	 */
	fun findLinkedNodes(id: String): Map<String, Node> =
		findLinks(id).map { it.hookId to nodes[it.hookNodeId] }.toMap().filterValuesNotNull()

	fun parse(nodes: String, links: String) {
		parseNodes(nodes)
		parseLinks(links)
	}

	private fun parseNodes(nodes: String) {
		val ns = gson.fromJson<List<Node>>(nodes, genericTypeInline<List<Node>>())
		this.nodes.putAll(ns.associateBy { it.id })
	}

	private fun parseLinks(links: String) {
		val ls = gson.fromJson<List<Link>>(links, genericTypeInline<List<Link>>())
		this.links.addAll(ls)
	}

	fun clearNodes() {
		nodes.forEach { Bundles.disposeNode(it.value) }
		nodes.clear()
		links.clear()
	}

}

data class Node(
	val id: String,
	val nodeId: String,
	val annotations: Map<String, Any>,
	val selectors: List<Selector>
)

data class Selector(
	val id: String,
	val nodeId: String,
	val selectorId: String,
	val type: String,
	val cacheStrategy: CacheStrategy,
	val defaultValue: Any?,
	val needed: Boolean,
	val connectorIn: Boolean,
	val connectorOut: Boolean,
)

/**
 * Caching strategy of a selector.
 * This is only necessary for a connector going in.
 */
enum class CacheStrategy {
	/**
	 * Keep the value cached between runs.
	 * This can be handy for static messages and other static variables.
	 * Dispose the value after the node has run.
	 */
	@SerializedName("keep")
	KEEP,

	/**
	 * This can be handy for values that should be different every run.
	 * Or values that should be the initiator of a node.
	 */
	@SerializedName("disposeOnRun")
	DISPOSE_ON_RUN,
}

data class Link(
	val id: String,
	val baseNodeId: String,
	val hookNodeId: String,
	val baseId: String,
	val hookId: String,
)