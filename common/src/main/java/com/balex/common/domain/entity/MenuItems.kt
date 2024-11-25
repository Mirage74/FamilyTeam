package com.balex.common.domain.entity

import com.google.gson.annotations.SerializedName

data class MenuItems(
    @SerializedName("items")
    val items: List<String> = listOf(MENU_ITEM_ABOUT,MENU_ITEM_ABOUT2,MENU_ITEM_ABOUT3,MENU_ITEM_ABOUT4 )
) {
    companion object {
        const val MENU_ITEM_ABOUT = "About"
        const val MENU_ITEM_ABOUT2 = "About2"
        const val MENU_ITEM_ABOUT3 = "About3"
        const val MENU_ITEM_ABOUT4 = "About4"
    }
}

