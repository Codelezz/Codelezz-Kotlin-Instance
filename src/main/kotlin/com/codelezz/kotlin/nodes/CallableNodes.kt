package com.codelezz.kotlin.nodes

import com.codelezz.kotlin.bundles.Bundles
import com.codelezz.kotlin.bundles.Bundles.callable
import com.codelezz.kotlin.utils.filterValuesNotNull
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Base class for any node implementation.
 *
 * It manages the state of an implemented node and it's selector states.
 */
open class CallableNode(private val node: Node) {

	private val selectors: Map<String, CallableSelector> = node.selectors
		.filter { it.connectorIn || it.defaultValue != null }
		.map {
			when (it.cacheStrategy) {
				CacheStrategy.KEEP -> it.id to LatestStrategySelector(it)
				CacheStrategy.DISPOSE_ON_RUN -> it.id to QueueStrategySelector(it)
			}
		}
		.toMap()

	open fun initialize() {
		fireWhenReady()
	}

	open fun dispose() {}

	fun completeSelector(selectorId: String, value: Any) {
		val selector = node.selectors.firstOrNull { it.selectorId == selectorId } ?: return

		Nodes.findLinkedNodes(selector.id)
			.mapValues { it.value.callable }
			.filterValuesNotNull()
			.forEach { it.value.provideSelector(it.key, value) }
	}

	fun provideSelector(id: String, value: Any) {
		selectors[id]?.provide(value)
		print("Got selector: ${selectors[id]}")
		fireWhenReady()
	}

	/**
	 * Fire this node if all values are ready!
	 */
	fun fireWhenReady() {
		if (selectors.all { it.value.isReady }) {
			val objects = selectors
				.filterValues { it.hasValue }
				.map { it.value.selectorId to it.value.next() }
				.toMap()
			print("Everything is ready: $node -> $objects")
			Bundles.callNode(node, objects)
		}
	}
}

/**
 * A way to implement cache strategies for different selectors.
 */
sealed class CallableSelector(private val selector: Selector) {
	abstract val hasValue: Boolean

	val id: String
		get() = selector.id
	val selectorId: String
		get() = selector.selectorId
	val isReady: Boolean
		get() = !selector.needed || hasValue

	abstract fun next(): Any
	abstract fun provide(value: Any)
}

/**
 * Always uses the latest value.
 * It is ready once a value has been passed or if the selector has a default value.
 */
data class LatestStrategySelector(val selector: Selector) : CallableSelector(selector) {
	private val lock = ReentrantLock()
	lateinit var value: Any

	override val hasValue: Boolean
		get() = lock.withLock { ::value.isInitialized }

	init {
		selector.defaultValue?.let {
			lock.withLock { value = it }
		}
	}

	override fun next(): Any {
		if (!hasValue) throw NullPointerException()
		return lock.withLock { value }
	}

	override fun provide(value: Any) {
		lock.withLock {
			this.value = value
		}
	}
}

/**
 * Queues up values.
 * Every time next is called the latest value is grabbed from the queue and is passed on.
 * Once the queue is empty it waits until a new value is provided.
 */
data class QueueStrategySelector(val selector: Selector) : CallableSelector(selector) {

	private val queue = ConcurrentLinkedQueue<Any>()

	override val hasValue: Boolean
		get() = queue.isNotEmpty()

	init {
		selector.defaultValue?.let {
			queue.offer(it)
		}
	}

	override fun next(): Any {
		if (!hasValue) throw NullPointerException()
		return queue.poll()
	}

	override fun provide(value: Any) {
		queue.offer(value)
	}
}