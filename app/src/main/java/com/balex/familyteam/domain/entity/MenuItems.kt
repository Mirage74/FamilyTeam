package com.balex.familyteam.domain.entity

data class MenuItems(
    val items: List<String> = listOf(MENU_ITEM_ABOUT)
) {
    companion object {
        const val MENU_ITEM_ABOUT = "About"
    }
}

