package com.balex.common.domain.entity

import android.content.Context
import com.balex.common.R
import com.google.gson.annotations.SerializedName

data class MenuItems(
    @SerializedName("items")
    val items: Map<String, String>
) {
    companion object {
        const val MENU_ITEM_RULES = "Rules"
        const val MENU_ITEM_ABOUT = "About"

        fun fromResources(context: Context): MenuItems {
            return MenuItems(
                items = mapOf(
                    MENU_ITEM_RULES to context.getString(R.string.rules_app),
                    MENU_ITEM_ABOUT to context.getString(R.string.about_app)
                )
            )
        }

    }
    fun getItem(key: String): String {
        val itemName= when (key) {
            MENU_ITEM_RULES -> items[MENU_ITEM_RULES]
            MENU_ITEM_ABOUT -> items[MENU_ITEM_ABOUT]
            else -> "null"
        }
        return itemName.toString()
    }
}

