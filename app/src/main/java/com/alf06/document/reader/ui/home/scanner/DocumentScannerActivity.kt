package com.alf06.document.reader.ui.home.scanner

import androidx.annotation.OptIn
import androidx.camera.view.TransformExperimental
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.ActivtiDocumentScannerBinding
import com.alf06.document.reader.ui.home.scanner.fragment.DocumentScannerFragment
import com.alf06.document.reader.ui.home.scanner.fragment.ScanCropFragment
import com.alf06.document.reader.ui.home.scanner.fragment.ScanFilterFragment
import com.alf06.document.reader.ui.home.scanner.fragment.ScanPagesFragment
import com.ui.baselib.base.BaseActivity
import com.ui.baselib.base.NavGraph
import com.ui.baselib.extensions.newInstanceWithArgs

@OptIn(TransformExperimental::class)
class DocumentScannerActivity : BaseActivity<ActivtiDocumentScannerBinding>(ActivtiDocumentScannerBinding::inflate) {
    override val fullStatus: Boolean
        get() = true

    override fun backPressed() {
        if (!goBackFragment()) {
            finish()
        }
    }

    override val fragmentContainerId: Int
        get() = R.id.documentScannerContainer
    override val navGraph: NavGraph
        get() = NavGraph().apply {
            addDestination(ScannerNavigator.Camera.route, isStartDestination = true) { newInstanceWithArgs<DocumentScannerFragment>() }
            addDestination(ScannerNavigator.Crop.route) { newInstanceWithArgs<ScanCropFragment>() }
            addDestination(ScannerNavigator.Pages.route) { newInstanceWithArgs<ScanPagesFragment>() }
            addDestination(ScannerNavigator.Filter.route) { newInstanceWithArgs<ScanFilterFragment>() }
        }

    override fun initialize() {
    }

    override fun ActivtiDocumentScannerBinding.setData() {
    }

    override fun ActivtiDocumentScannerBinding.onClick() {
    }
}
