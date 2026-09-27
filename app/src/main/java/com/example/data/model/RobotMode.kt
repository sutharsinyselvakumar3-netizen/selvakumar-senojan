package com.example.data.model

enum class RobotMode {
    MANUAL,
    AUTO,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): RobotMode {
            return when (value?.trim()?.uppercase()) {
                "MANUAL" -> MANUAL
                "AUTO" -> AUTO
                else -> UNKNOWN
            }
        }
    }
}
