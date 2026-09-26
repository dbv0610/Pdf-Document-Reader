package com.wxiwei.office.officereader

import android.content.SearchRecentSuggestionsProvider

class SearchSuggestionsProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        private const val AUTHORITY = "searchprovider"
        private const val MODE = DATABASE_MODE_QUERIES
    }
}
