package com.thellex.pos.ui.login

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val isLoggedIn: Boolean = false
)
