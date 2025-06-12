package com.element.launch

import com.element.configuration.Configuration
import com.element.configuration.game.GameProvider
import com.element.configuration.model.GameEnvironment
import com.element.injection.TransformingClassLoader
import com.element.injection.patch.ClassPatcher
import com.element.injection.patch.impl.ClassPatchTransformer
import org.apache.logging.log4j.LogManager

object Main {
    private val LOGGER = LogManager.getLogger("Element/Bootstrap")

    @JvmStatic
    fun main(args: Array<String>) {
        LOGGER.info("Element is bootstrapping...")

        var classLoader: ClassLoader

        when (Configuration.environment) {
            GameEnvironment.Development -> {
                LOGGER.info("Running in a development environment")

                classLoader = this.javaClass.classLoader // just use parent classloader
            }
            GameEnvironment.Production -> {
                LOGGER.info("Running in production environment")
                ClassPatcher.discoverPatches()

                val transformingClassLoader = TransformingClassLoader().apply {
                    transformers += ClassPatchTransformer()
                }

                Thread.currentThread().setContextClassLoader(transformingClassLoader)

                classLoader = transformingClassLoader
            }
        }

        val gameProvider = Configuration.gameProvider

        LOGGER.info("Game provider: ${gameProvider.identifier}")
        LOGGER.info("Launching Minecraft with main class ${gameProvider.mainClass}")

        classLoader.loadClass(gameProvider.mainClass).getMethod("main", Array<String>::class.java)
            .invoke(null, args) // no need to modify args
    }
}