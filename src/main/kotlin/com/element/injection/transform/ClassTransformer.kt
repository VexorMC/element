package com.element.injection.transform

import com.element.configuration.model.GameEnvironment

abstract class ClassTransformer(
    vararg val environments: GameEnvironment = arrayOf(GameEnvironment.Production, GameEnvironment.Development)
) {
    /**
     * Transforms the provided class bytes.
     */
    abstract fun transform(name: String, transformedName: String, source: ByteArray): ByteArray
}