package com.euxcet.viturering.home

data class CardInfo(
    val key: String,
    val title: String?= null,
    val description: String? = null,
    val icon: Int = 0,
    val backgroundColor: Int = 0,
    val backgroundRes: Int = 0,
    val hCells: Int = 1,
    val vCells: Int = 1,
    val hPosition: Int = 0,
    val vPosition: Int = 0
)
