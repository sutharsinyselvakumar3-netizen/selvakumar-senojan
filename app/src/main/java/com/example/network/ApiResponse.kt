package com.example.network

data class ApiResponse(
    val success: Boolean,
    val message: String? = null,
    val reason: String? = null
)
