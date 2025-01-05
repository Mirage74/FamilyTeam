package com.balex.common.domain.entity

import com.google.gson.annotations.SerializedName

data class MenuItems(
    @SerializedName("items")
    val items: List<String> = listOf(MENU_ITEM_ABOUT)
) {
    companion object {
        const val MENU_ITEM_ABOUT = "About"
    }
}

