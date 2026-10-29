package com.example.skillbridge.config

/**
 * SkillBridge Application Environment Configuration
 * Provides a clean switch between MOCK (offline/preview) and STAGING/PROD (backend-connected).
 */
enum class EnvironmentMode {
    MOCK,
    STAGING,
    PRODUCTION
}

object AppConfig {
    var currentMode: EnvironmentMode = EnvironmentMode.MOCK

    val apiBaseUrl: String
        get() = when (currentMode) {
            EnvironmentMode.MOCK -> "http://localhost:8080/v1"
            EnvironmentMode.STAGING -> "https://staging-api.skillbridge.in/v1"
            EnvironmentMode.PRODUCTION -> "https://api.skillbridge.in/v1"
        }

    val isMock: Boolean
        get() = currentMode == EnvironmentMode.MOCK

    fun setEnvironment(mode: EnvironmentMode) {
        currentMode = mode
    }
}
