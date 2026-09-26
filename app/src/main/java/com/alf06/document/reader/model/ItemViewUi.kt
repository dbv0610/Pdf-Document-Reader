package com.alf06.document.reader.model

data class FavoriteUi(
  val document: FavoriteDocument,
  val isFavorite: Boolean
)
data class RecentUi(
  val document: RecentDocument,
  val isFavorite: Boolean
)