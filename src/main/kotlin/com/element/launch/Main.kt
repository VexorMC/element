package com.element.launch

import com.element.configuration.Configuration
import com.element.injection.TransformingClassLoader
import com.element.injection.patch.impl.*
import org.apache.logging.log4j.LogManager

object Main {
    private val LOGGER = LogManager.getLogger("Element/Bootstrap")
    val classLoader = TransformingClassLoader()

    @JvmStatic
    fun main(args: Array<String>) {
        LOGGER.info("Element is bootstrapping...")

        val allTransformers = listOf(
            ClassPatchTransformer(), OpenALTransformer()
        )

        classLoader.apply {
            transformers += allTransformers.filter { it.environments.any { it == Configuration.environment } }
        }

        LOGGER.info("Running in a '${Configuration.environment.name}' (${Configuration.environment.identifier}) environment")

        val gameProvider = Configuration.gameProvider

        LOGGER.info("Game provider: ${gameProvider.identifier}")
        LOGGER.info("Launching Minecraft with main class ${gameProvider.mainClass}")

        Thread.currentThread().setContextClassLoader(classLoader)
        classLoader.loadClass(gameProvider.mainClass).getMethod("main", Array<String>::class.java)
            .invoke(null, args) // no need to modify args
    }
}