package com.wxiwei.office.fc.util

import java.util.AbstractMap
import java.util.AbstractSet
import java.util.AbstractCollection
import java.util.Collection
import java.util.Comparator
import java.util.Iterator
import java.util.Set
import java.util.TreeMap

/** A bidirectionally indexed map whose keys and values are naturally ordered. */
class BinaryTree : AbstractMap<Any?, Any?> {
    private val byKey = TreeMap<Any, Node>(Comparator { a, b -> (a as Comparable<Any>).compareTo(b) })
    private val byValue = TreeMap<Any, Node>(Comparator { a, b -> (a as Comparable<Any>).compareTo(b) })
    private var modifications = 0

    constructor()

    constructor(map: kotlin.collections.Map<*, *>) : this() {
        putAll(map)
    }

    fun getKeyForValue(value: Any?): Any? = findNode(value)?.key

    fun removeValue(value: Any?): Any? {
        val node = findNode(value) ?: return null
        removeNode(node)
        return node.key
    }

    fun entrySetByValue(): MutableSet<MutableMap.MutableEntry<Any?, Any?>> = entryView(false)
    fun keySetByValue(): MutableSet<Any?> = keyView(false)
    fun valuesByValue(): MutableCollection<Any?> = valueView(false)

    override val size: Int get() = byKey.size

    override fun containsKey(key: Any?): Boolean = key != null && byKey.containsKey(key)
    override fun containsValue(value: Any?): Boolean = value != null && byValue.containsKey(value)
    override fun get(key: Any?): Any? = if (key == null) null else byKey[key]?.value

    override fun put(key: Any?, value: Any?): Any? {
        checkKey(key)
        checkValue(value)
        if (byKey.containsKey(key)) throw IllegalArgumentException("key already exists: $key")
        if (byValue.containsKey(value)) throw IllegalArgumentException("value already exists: $value")
        val node = Node(key, value)
        byKey[key!!] = node
        byValue[value!!] = node
        modifications++
        return null
    }

    override fun remove(key: Any?): Any? {
        if (key == null) return null
        val node = byKey[key] ?: return null
        val old = node.value
        removeNode(node)
        return old
    }

    override fun clear() {
        if (byKey.isNotEmpty()) modifications++
        byKey.clear()
        byValue.clear()
    }

    override val entries: MutableSet<MutableMap.MutableEntry<Any?, Any?>> get() = entryView(true)

    private fun find(value: Any?): Any? = findNode(value)?.key

    private fun findNode(value: Any?): Node? {
        checkValue(value)
        return byValue[value]
    }

    private fun removeNode(node: Node) {
        byKey.remove(node.key)
        byValue.remove(node.value)
        modifications++
    }

    private fun checkKey(key: Any?) {
        if (key == null) throw NullPointerException("key")
        if (key !is Comparable<*>) throw ClassCastException("key is not Comparable")
    }

    private fun checkValue(value: Any?) {
        if (value == null) throw NullPointerException("value")
        if (value !is Comparable<*>) throw ClassCastException("value is not Comparable")
    }

    private fun nodes(byKeyOrder: Boolean): List<Node> = if (byKeyOrder) byKey.values.toList() else byValue.values.toList()

    private fun entryView(byKeyOrder: Boolean): MutableSet<MutableMap.MutableEntry<Any?, Any?>> = object : AbstractSet<MutableMap.MutableEntry<Any?, Any?>>() {
        override val size: Int get() = this@BinaryTree.size
        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Any?, Any?>> = nodeIterator(byKeyOrder).mapIterator { it }
        override fun contains(element: MutableMap.MutableEntry<Any?, Any?>): Boolean = element.let { this@BinaryTree.get(it.key) == it.value && this@BinaryTree.containsKey(it.key) }
        override fun remove(element: MutableMap.MutableEntry<Any?, Any?>): Boolean { if (!contains(element)) return false; this@BinaryTree.remove(element.key); return true }
        override fun clear() = this@BinaryTree.clear()
    }

    private fun keyView(byKeyOrder: Boolean): MutableSet<Any?> = object : AbstractSet<Any?>() {
        override val size: Int get() = this@BinaryTree.size
        override fun iterator(): MutableIterator<Any?> = nodeIterator(byKeyOrder).mapIterator { it.key }
        override fun contains(element: Any?): Boolean = this@BinaryTree.containsKey(element)
        override fun remove(element: Any?): Boolean { val present = contains(element); this@BinaryTree.remove(element); return present }
        override fun clear() = this@BinaryTree.clear()
    }

    private fun valueView(byKeyOrder: Boolean): MutableCollection<Any?> = object : AbstractCollection<Any?>() {
        override val size: Int get() = this@BinaryTree.size
        override fun iterator(): MutableIterator<Any?> = nodeIterator(byKeyOrder).mapIterator { it.value }
        override fun contains(element: Any?): Boolean = this@BinaryTree.containsValue(element)
        override fun remove(element: Any?): Boolean { val present = contains(element); this@BinaryTree.removeValue(element); return present }
        override fun clear() = this@BinaryTree.clear()
    }

    private fun nodeIterator(byKeyOrder: Boolean): MutableIterator<Node> {
        val expected = modifications
        val iterator = nodes(byKeyOrder).iterator()
        var last: Node? = null
        return object : MutableIterator<Node> {
            override fun hasNext(): Boolean = iterator.hasNext()
            override fun next(): Node { if (expected != modifications) throw ConcurrentModificationException(); last = iterator.next(); return last!! }
            override fun remove() { if (expected != modifications) throw ConcurrentModificationException(); val node = last ?: throw IllegalStateException(); removeNode(node); last = null }
        }
    }

    private fun <T> MutableIterator<Node>.mapIterator(mapper: (Node) -> T): MutableIterator<T> = object : MutableIterator<T> {
        override fun hasNext() = this@mapIterator.hasNext()
        override fun next() = mapper(this@mapIterator.next())
        override fun remove() = this@mapIterator.remove()
    }

    private class Node(override val key: Any?, override val value: Any?) : MutableMap.MutableEntry<Any?, Any?> {
        override fun setValue(newValue: Any?): Any? = throw UnsupportedOperationException()
        override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && key == other.key && value == other.value
        override fun hashCode(): Int = (key?.hashCode() ?: 0) xor (value?.hashCode() ?: 0)
    }
}
