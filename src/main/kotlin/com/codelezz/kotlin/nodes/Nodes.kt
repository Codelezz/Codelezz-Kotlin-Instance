package com.codelezz.kotlin.nodes

import com.sun.org.apache.xpath.internal.operations.Bool

object Nodes {
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
	val defaultType: Any,
	val required: Bool,
	val connectorIn: Bool,
	val connectorOut: Bool,
)

data class Link(
	val id: String,
	val baseNodeId: String,
	val hookNodeId: String,
	val baseId: String,
	val hookId: String,
)