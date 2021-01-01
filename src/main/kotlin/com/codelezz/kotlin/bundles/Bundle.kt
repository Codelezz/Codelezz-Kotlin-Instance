package com.codelezz.kotlin.bundles

import com.codelezz.kotlin.nodes.CallableNode
import com.codelezz.kotlin.nodes.Node

abstract class Bundle {

	abstract val registeredNodes: List<String>
	abstract val nodes: MutableMap<String, CallableNode>

	abstract fun createCallableNode(node: Node): CallableNode?
	abstract fun callNode(node: Node, objects: Map<String, Any>)
}