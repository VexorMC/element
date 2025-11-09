package com.element.configuration

import com.element.configuration.game.GameProvider
import com.element.configuration.game.impl.CustomGameProvider
import com.element.configuration.model.GameEnvironment

object Configuration {
    val gameProvider: GameProvider by lazy {
        System.getProperty("element.game.provider")?.let { providerName ->
            if (providerName == "custom") {
                val identifier = System.getProperty("element.game.provider.identifier")
                val mainClass = System.getProperty("element.game.provider.mainClass")

                if (identifier.isNullOrBlank() || mainClass.isNullOrBlank()) {
                    error("Custom game provider requires both 'element.game.provider.identifier' and 'element.game.provider.mainClass' properties to be set.")
                }

                CustomGameProvider(identifier, mainClass)
            }

            GameProvider.services.find { it.identifier == providerName } ?: error("Unknown game provider '$providerName'. Please ensure it is correctly defined in your classpath.")
        } ?: GameProvider.get()
    }

    val environment: GameEnvironment by lazy {
        GameEnvironment.fromIdentifier(System.getProperty("element.environment") ?: "prod")
    }
}