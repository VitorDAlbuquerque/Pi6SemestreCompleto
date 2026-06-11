package com.example.demo.dto

data class LoginResponse(
    val message: String,
    val userId: String,
    val name: String,
    val email: String,
    val role: String,
    val apartmentId: String? = null,
    val block: String? = null
)