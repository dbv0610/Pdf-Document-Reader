package com.ui.baselib.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding


class ModelDiffCallback<T : Any>(
    private val areItemsTheSameCallback: (oldItem: T, newItem: T) -> Boolean,
    private val areContentsTheSameCallback: (oldItem: T, newItem: T) -> Boolean = { o, n -> o == n },
    private val payloadProvider: ((oldItem: T, newItem: T) -> Any?)? = null
) : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
        areItemsTheSameCallback(oldItem, newItem)

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
        areContentsTheSameCallback(oldItem, newItem)

    override fun getChangePayload(oldItem: T, newItem: T): Any? =
        payloadProvider?.invoke(oldItem, newItem)
}

abstract class DiffAdapter<T : Any, VB : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, DiffAdapter<T, VB>.ViewHolder>(diffCallback), LifecycleOwner {

    constructor(
        areItemsTheSame: (old: T, new: T) -> Boolean,
        areContentsTheSame: (old: T, new: T) -> Boolean = { o, n -> o == n },
        payloadProvider: ((old: T, new: T) -> Any?)? = null
    ) : this(
        ModelDiffCallback(
            areItemsTheSameCallback = areItemsTheSame,
            areContentsTheSameCallback = areContentsTheSame,
            payloadProvider = payloadProvider
        )
    )

    var currentPosition = MutableLiveData(RecyclerView.NO_POSITION)
        private set

    var onPositionChanged : (Int)-> Unit = {}
    private var lifecycleRegistry = LifecycleRegistry(this)
    private var attachedRecyclerView: RecyclerView? = null
    private var attachedViewLifecycleOwner: LifecycleOwner? = null

    private val viewLifecycleObserver = LifecycleEventObserver { source, _ ->
        moveLifecycleTo(source.lifecycle.currentState)
    }

    private val recyclerViewAttachStateListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) {
            attachToViewLifecycle(view.findViewTreeLifecycleOwner())
        }

        override fun onViewDetachedFromWindow(view: View) = Unit
    }

    protected var context: Context? = null
        private set

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
    protected val viewLifecycleOwner: LifecycleOwner
        get() = attachedViewLifecycleOwner ?: this

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (lifecycleRegistry.currentState == Lifecycle.State.DESTROYED) {
            lifecycleRegistry = LifecycleRegistry(this)
        }

        attachedRecyclerView
            ?.takeIf { it !== recyclerView }
            ?.removeOnAttachStateChangeListener(recyclerViewAttachStateListener)

        attachedRecyclerView = recyclerView
        context = recyclerView.context
        recyclerView.addOnAttachStateChangeListener(recyclerViewAttachStateListener)
        attachToViewLifecycle(recyclerView.findViewTreeLifecycleOwner())
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        recyclerView.removeOnAttachStateChangeListener(recyclerViewAttachStateListener)
        attachedViewLifecycleOwner?.lifecycle?.removeObserver(viewLifecycleObserver)
        attachedViewLifecycleOwner = null
        attachedRecyclerView = null

        moveLifecycleTo(Lifecycle.State.DESTROYED)

        super.onDetachedFromRecyclerView(recyclerView)
        context = null
    }

    private fun attachToViewLifecycle(owner: LifecycleOwner?) {
        if (owner == null || owner === attachedViewLifecycleOwner) return

        attachedViewLifecycleOwner?.lifecycle?.removeObserver(viewLifecycleObserver)
        attachedViewLifecycleOwner = owner

        if (lifecycleRegistry.currentState == Lifecycle.State.DESTROYED) {
            lifecycleRegistry = LifecycleRegistry(this)
        }

        moveLifecycleTo(owner.lifecycle.currentState)
        if (owner.lifecycle.currentState != Lifecycle.State.DESTROYED) {
            owner.lifecycle.addObserver(viewLifecycleObserver)
        }
    }

    /**
     * LifecycleRegistry rejects INITIALIZED -> DESTROYED (e.g. the adapter is detached while the
     * activity is still in onCreate) and any move out of DESTROYED, so route around both.
     */
    private fun moveLifecycleTo(state: Lifecycle.State) {
        val current = lifecycleRegistry.currentState
        if (current == state || current == Lifecycle.State.DESTROYED) return
        if (state == Lifecycle.State.DESTROYED && current == Lifecycle.State.INITIALIZED) {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
        lifecycleRegistry.currentState = state
    }

    fun stringRes(@StringRes res: Int): String = context?.getString(res) ?: ""

    fun stringRes(@StringRes res: Int, vararg args: Any): String =
        context?.getString(res, *args) ?: ""

    public override fun getItem(position: Int): T = super.getItem(position)

    fun getItemOrNull(position: Int): T? = currentList.getOrNull(position)

    fun getCurrentItem(): T? = getItemOrNull(getCurrentPos())

    val currentItems: List<T>
        get() = currentList.toList()

    fun getListItem(): List<T> = currentList.toList()

    abstract fun createBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VB
    abstract fun VB.bind(item: T, position: Int)

    open fun VB.bind(item: T, position: Int, payloads: List<Any>) {
        bind(item, position)
    }

    inner class ViewHolder(val binding: VB) : RecyclerView.ViewHolder(binding.root) {

        val context: Context
            get() = binding.root.context

        fun stringRes(@StringRes res: Int): String = context.getString(res)

        fun stringRes(@StringRes res: Int, vararg args: Any): String = context.getString(res, *args)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val vb = createBinding(inflater, parent, viewType)
        return ViewHolder(vb)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.bind(item, position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = getItem(position)
        if (payloads.isEmpty()) {
            holder.binding.bind(item, position)
        } else {
            holder.binding.bind(item, position, payloads)
        }
    }

    fun submitListCustom(newList: List<T>?, commitCallback: (() -> Unit)? = null) {
        submitList(newList?.toList(), commitCallback)
    }

    fun removeItem(position: Int) {
        val current = currentList.toMutableList()
        if (position in current.indices) {
            current.removeAt(position)
            submitListCustom(current) { normalizeCurrentPosition() }
        }
    }

    fun removeItem(item: T) {
        val index = currentList.indexOf(item)
        if (index != -1) removeItem(index)
    }

    fun removeItems(predicate: (T) -> Boolean) {
        val newItems = currentList.filterNot(predicate)
        if (newItems.size == currentList.size) return
        submitListCustom(newItems) { normalizeCurrentPosition() }
    }

    fun addItem(item: T, index: Int = currentList.size) {
        val current = currentList.toMutableList()
        val safeIndex = index.coerceIn(0, current.size)
        current.add(safeIndex, item)
        submitListCustom(current)
    }

    fun addItems(items: List<T>) {
        if (items.isEmpty()) return
        val current = currentList.toMutableList()
        current.addAll(items)
        submitListCustom(current)
    }

    fun clearAll() {
        if (currentList.isEmpty()) return
        submitListCustom(emptyList()) {
            currentPosition.value = RecyclerView.NO_POSITION
            onPositionChanged(RecyclerView.NO_POSITION)
        }
    }

    fun sortWith(comparator: Comparator<in T>) {
        if (currentList.size < 2) return
        submitListCustom(currentList.sortedWith(comparator))
    }

    fun <R : Comparable<R>> sortBy(selector: (T) -> R?) {
        sortWith(compareBy(selector))
    }

    fun <R : Comparable<R>> sortByDescending(selector: (T) -> R?) {
        sortWith(compareByDescending(selector))
    }

    fun changeItemWithPos(index: Int, newItem: T) {
        val current = currentList.toMutableList()
        if (index !in current.indices) return
        current[index] = newItem
        submitListCustom(current)
    }

    fun updateItem(index: Int, newItem: T) {
        changeItemWithPos(index, newItem)
    }

    fun updateItem(predicate: (T) -> Boolean, transform: (T) -> T) {
        val index = currentList.indexOfFirst(predicate)
        if (index == -1) return
        changeItemWithPos(index, transform(currentList[index]))
    }

    fun replaceAll(transform: (T) -> T) {
        if (currentList.isEmpty()) return
        submitListCustom(currentList.map(transform))
    }

    fun setCurrentPos(position: Int) {
        if (position != RecyclerView.NO_POSITION && position !in 0 until itemCount) return
        val prev = currentPosition.value ?: RecyclerView.NO_POSITION
        if (prev == position) return

        currentPosition.value = position

        if (prev != RecyclerView.NO_POSITION && prev < itemCount) {
            notifyItemChanged(prev)
        }
        if (position != RecyclerView.NO_POSITION && position < itemCount) {
            notifyItemChanged(position)
        }
        onPositionChanged(position)
    }

    fun getCurrentPos(): Int = currentPosition.value ?: RecyclerView.NO_POSITION

    private fun normalizeCurrentPosition() {
        val current = getCurrentPos()
        if (current == RecyclerView.NO_POSITION) return
        if (current !in currentList.indices) {
            currentPosition.value = RecyclerView.NO_POSITION
            onPositionChanged(RecyclerView.NO_POSITION)
        }
    }
}
