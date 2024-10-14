package com.balex.logged_user

import kotlinx.serialization.Serializable

@Serializable
enum class PagesNames {
    TodoList,
    ShopList,
    MyTasksForOtherUsers,
    AdminPanel,
    Logout
}