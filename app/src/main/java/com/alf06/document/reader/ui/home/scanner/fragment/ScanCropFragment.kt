package com.alf06.document.reader.ui.home.scanner.fragment

import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentScanCropBinding
import com.alf06.document.reader.ui.home.scanner.PendingCrop
import com.alf06.document.reader.ui.home.scanner.ScanPage
import com.alf06.document.reader.ui.home.scanner.ScanSessionViewModel
import com.alf06.document.reader.ui.home.scanner.ScannerNavigator
import com.alf06.document.reader.ui.home.scanner.detection.DocumentImageProcessor
import com.ui.baselib.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.Point

class ScanCropFragment : BaseFragment<FragmentScanCropBinding>(
    FragmentScanCropBinding::inflate
) {
    private val viewModel: ScanSessionViewModel by activityViewModel()
    private val processor = DocumentImageProcessor()
    private var busy = false

    override fun FragmentScanCropBinding.initView() {
        root.updatePadding(bottom = root.paddingBottom + navigationBarHeight)
        val pending = viewModel.pendingCrop
        if (pending == null) {
            navHost.goBackFragment()
            return
        }
        show(pending)
    }

    override fun FragmentScanCropBinding.onClick() {
        btnBack.setOnClickListener { retake() }
        btnRetake.setOnClickListener { retake() }
        btnRotate.setOnClickListener { rotate() }
        btnApply.setOnClickListener { apply() }
    }

    override fun onBackPressed(): Boolean {
        retake()
        return true
    }

    private fun show(pending: PendingCrop) {
        binding.cornerView.setImage(pending.original, pending.width, pending.height, pending.corners)
    }

    private fun retake() {
        if (busy) return
        viewModel.discardPending()
        navHost.goBackFragment()
    }

    private fun rotate() {
        val pending = viewModel.pendingCrop ?: return

        val corners = binding.cornerView.corners().map { Point(1.0 - it.y, it.x) }
        runBusy {
            val rotated = withContext(Dispatchers.Default) { processor.rotateClockwise(pending.original) }
            if (rotated == null) {
                showError()
                return@runBusy
            }
            pending.original.delete()
            val next = PendingCrop(rotated, pending.height, pending.width, corners)
            viewModel.pendingCrop = next
            show(next)
        }
    }

    private fun apply() {
        val pending = viewModel.pendingCrop ?: return
        val corners = binding.cornerView.corners()
        runBusy {
            val scan = withContext(Dispatchers.Default) {
                runCatching { processor.process(pending.original, corners) }
                    .onFailure { Log.e(TAG, "Unable to crop scan", it) }
                    .getOrNull()
            }
            if (scan == null) {
                showError()
                return@runBusy
            }
            viewModel.pendingCrop = null
            viewModel.addOrReplace(ScanPage(scan.original, cropped = scan.output))

            navHost.navigatePopUpTo(
                routeName = ScannerNavigator.Pages.route,
                popUpTo = ScannerNavigator.Crop.route,
                popUpToInclusive = true,
            )
        }
    }

    private fun runBusy(block: suspend () -> Unit) {
        if (busy) return
        setBusy(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                block()
            } finally {
                bindingOrNull?.let { setBusy(false) }
            }
        }
    }

    private fun setBusy(value: Boolean) {
        busy = value
        binding.progress.isVisible = value
        binding.btnApply.isEnabled = !value
        binding.btnRotate.isEnabled = !value
        binding.btnRetake.isEnabled = !value
    }

    private fun showError() {
        Toast.makeText(appContext(), R.string.scanner_capture_failed, Toast.LENGTH_SHORT).show()
    }

    private companion object {
        const val TAG = "ScanCrop"
    }
}
