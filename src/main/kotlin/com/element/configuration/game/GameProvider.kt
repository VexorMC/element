package com.element.configuration.game

import com.element.configuration.game.impl.CustomGameProvider
import com.element.launch.Main
import java.util.ServiceLoader

interface GameProvider {
    val identifier: String
    val mainClass: String

    companion object {
        val services: ServiceLoader<GameProvider> = ServiceLoader.load(GameProvider::class.java)

        /**
         * Returns the game provider for the current environment.
         * If no specific provider is found, it defaults to a custom provider.
         */
        fun get(): GameProvider {
            return services.firstOrNull()
                ?: error("We couldn't find a game provider. Please ensure you have a valid game provider in your classpath.")
        }
    }
}