package com.codelezz.kotlin.bundles

import com.codelezz.kotlin.CodelezzBase.baseFolder
import com.codelezz.kotlin.nodes.CallableNode
import com.codelezz.kotlin.nodes.Node
import com.codelezz.kotlin.utils.get
import com.codelezz.kotlin.utils.ifFailedPrint
import java.net.URLClassLoader

object Bundles {

	private val bundles: MutableList<Bundle> = mutableListOf()

	fun loadBundles() {
		bundles.clear()
		loadBundleJars()
	}

	fun loadBundleJars() {
		val dir = baseFolder["bundles"]
		if(!dir.exists()) {
			dir.mkdirs()
			return
		}

		val files = dir.listFiles()?.filter { !it.isDirectory && it.name.endsWith(".jar") } ?: return

		val loader =
			URLClassLoader(files.map { it.toURI().toURL() }.toTypedArray(), this.javaClass.classLoader)

		val bundles = files
			.map { BundleFile(it) }
			.map {
				it.loadJson()
				it.loadBundle(loader)
			}

		this.bundles.addAll(bundles)
		println("Initialized ${bundles.size} bundles.")
	}

	private val Node.bundle: Bundle?
		get() = bundles.firstOrNull { it.registeredNodes.contains(nodeId) }

	val Node.callable: CallableNode?
		get() {
			val b = bundle ?: return null
			if(!b.nodes.containsKey(id)) initializeNode(this) ifFailedPrint "Could not initialize $nodeId"
			return b.nodes[id]
		}

	fun initializeNode(node: Node): Boolean {
		val bundle = node.bundle ?: return false
		if(!bundle.nodes.containsKey(node.id)) {
			val call = bundle.createCallableNode(node) ?: return false
			bundle.nodes[node.id] = call
		}
		val call = bundle.nodes[node.id] ?: return false
		call.initialize()
		return true
	}

	fun callNode(node: Node, objects: Map<String, Any>) {
		val bundle = node.bundle ?: return
		if(!bundle.nodes.containsKey(node.id)) {
			initializeNode(node) ifFailedPrint "Could not initialize ${node.nodeId}"
		}
		bundle.callNode(node, objects)
	}

	fun disposeNode(node: Node) {
		bundles
			.filter { it.registeredNodes.contains(node.nodeId) }
			.forEach {
				val call = it.nodes.remove(node.id) ?: return@forEach
				call.dispose()
			}
	}
}