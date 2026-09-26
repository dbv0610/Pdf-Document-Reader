package com.alf06.document.reader.ui.home.tools.translate.fragment

import androidx.lifecycle.lifecycleScope
import com.alf06.document.reader.R
import com.alf06.document.reader.databinding.FragmentSplitSelectPagesBinding
import com.alf06.document.reader.ui.adapter.SplitPageAdapter
import com.alf06.document.reader.ui.home.tools.translate.TranslateNavigator
import com.alf06.document.reader.ui.home.tools.translate.TranslatePdfViewModel
import com.ui.baselib.base.BaseFragment
import com.ui.baselib.extensions.click
import com.ui.baselib.lifecycle.collectFlow
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Locale

class TranslateSelectPagesFragment :
    BaseFragment<FragmentSplitSelectPagesBinding>(FragmentSplitSelectPagesBinding::inflate) {
    private val viewModel: TranslatePdfViewModel by activityViewModel()

    override fun FragmentSplitSelectPagesBinding.initView() {
        val thumbnailWidth = (resources.displayMetrics.widthPixels / 3).coerceIn(1, MAX_THUMBNAIL_WIDTH)
        val adapter = SplitPageAdapter(
            scope = viewLifecycleOwner.lifecycleScope,
            loadThumbnail = { index -> viewModel.thumbnail(index, thumbnailWidth) },
            onToggle = viewModel::togglePage,
        )
        rcvPages.adapter = adapter
        btnContinue.setText(R.string.select)

        collectFlow(viewModel.pageCount) { count -> adapter.submitList(List(count) { it }) }
        collectFlow(viewModel.selectedPages) { selected ->
            adapter.setSelectedPages(selected)
            tvTitle.text = getString(R.string.split_select_pages, String.format(Locale.US, "%02d", selected.size))
            btnContinue.isEnabled = selected.isNotEmpty()
        }
    }

    override fun FragmentSplitSelectPagesBinding.onClick() {
        icBack.click { navHost.goBackFragment() }
        btnContinue.click {
            if (viewModel.selectedPages.value.isNotEmpty()) navigateTo(TranslateNavigator.Language.route)
        }
    }

    private companion object {
        const val MAX_THUMBNAIL_WIDTH = 400
    }
}
