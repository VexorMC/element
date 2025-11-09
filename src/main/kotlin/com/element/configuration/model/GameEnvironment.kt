package com.element.configuration.model

enum class GameEnvironment(val identifier: String) {
    Development("dev"),
    Production("prod");

    companion object {
        fun fromIdentifier(identifier: String): GameEnvironment {
            return entries.firstOrNull { it.identifier == identifier }
                ?: error("Unknown game environment identifier: $identifier")
        }
    }
}