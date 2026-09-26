package com.ui.baselib.base

import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseBottomSheet<V : ViewBinding>(
    private val activity: FragmentActivity,
    private val bindingFactory: (LayoutInflater) -> V,
) : BottomSheetDialog(activity) {

    companion object {
        const val TAG = "BaseBottomSheet"
    }

    private val showingGuard = AtomicBoolean(false)
    private var _binding: V? = null

    protected val binding: V
        get() = _binding ?: throw IllegalStateException("Binding accessed before show()/after dismiss()")

    var onDismissListener: () -> Unit = {}

    abstract fun V.onBind()

    init {
        setOnShowListener { trySetupBottomSheet() }

        setOnDismissListener {
            showingGuard.set(false)
            _binding = null
            onDismissListener()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        hideSystemBars()
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }
    private fun hideSystemBars() {
        val win = window ?: return
        WindowCompat.setDecorFitsSystemWindows(win, false)
        win.navigationBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(win, win.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    private fun ensureContentView() {
        if (_binding != null) return
        val vb = bindingFactory(LayoutInflater.from(context))
        _binding = vb
        vb.onBind()
        vb.root.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        vb.root.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom != oldBottom) {
                val sheet = findViewById<View>(R.id.design_bottom_sheet)
                    ?: return@addOnLayoutChangeListener
                BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        setContentView(vb.root)
    }

    private fun trySetupBottomSheet() {
        window?.let { win ->
            win.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            win.setBackgroundDrawableResource(android.R.color.transparent)
        }
        hideSystemBars()

        val sheet = findViewById<View>(R.id.design_bottom_sheet) ?: return

        sheet.setBackgroundColor(Color.TRANSPARENT)
        sheet.fitsSystemWindows = false
        sheet.setPadding(0, 0, 0, 0)
        sheet.layoutParams = sheet.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }

        var parent = sheet.parent
        while (parent is View) {
            val view = parent
            view.fitsSystemWindows = false
            view.setPadding(0, 0, 0, 0)
            ViewCompat.setOnApplyWindowInsetsListener(view) { _, _ ->
                WindowInsetsCompat.CONSUMED
            }
            parent = view.parent
        }
        ViewCompat.setOnApplyWindowInsetsListener(sheet) { _, _ ->
            WindowInsetsCompat.CONSUMED
        }
        BottomSheetBehavior.from(sheet).apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
            (sheet.parent as? ViewGroup)?.layoutTransition = null
        }
    }

    override fun show() {
        if (activity.isFinishing || activity.isDestroyed) return

        if (Looper.myLooper() != Looper.getMainLooper()) {
            activity.runOnUiThread { show() }
            return
        }

        if (!showingGuard.compareAndSet(false, true)) return
        if (isShowing) return

        try {
            ensureContentView()
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
            super.show()
            hideSystemBars()
            window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        } catch (_: Throwable) {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            showingGuard.set(false)
        }
    }

    fun dismissSafe() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            activity.runOnUiThread { dismissSafe() }
            return
        }

        if (!isShowing) {
            showingGuard.set(false)
            return
        }

        try {
            dismiss()
        } finally {
            showingGuard.set(false)
        }
    }
}