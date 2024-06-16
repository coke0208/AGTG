package com.example.test.productinfo

data class GroupDB(
    val groupId: String = "",
    val name: String = "",
    val members: MutableMap<String, Boolean> = mutableMapOf()
)
