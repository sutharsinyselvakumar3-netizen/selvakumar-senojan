package com.example.data.model

enum class ArmState {
    IDLE,
    SEARCHING,
    MOVING,
    TARGETING,
    DRILLING,
    RETURNING,
    HOME,
    SAFE,
    FAULT;

    companion object {
        fun fromString(value: String?): ArmState {
            return try {
                if (value.isNullOrBlank()) IDLE else valueOf(value.trim().uppercase())
            } catch (e: Exception) {
                IDLE
            }
        }
    }
}
