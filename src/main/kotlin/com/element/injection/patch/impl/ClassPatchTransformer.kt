package com.element.injection.patch.impl

import com.element.configuration.model.GameEnvironment
import com.element.configuration.onlyIn
import com.element.injection.patch.ClassPatcher
import com.element.injection.transform.ClassTransformer

class ClassPatchTransformer : ClassTransformer(GameEnvironment.Production) {
    init {
        onlyIn(GameEnvironment.Production) {
            ClassPatcher.discoverPatches()
        }
    }

    override fun transform(
        name: String,
        transformedName: String,
        source: ByteArray
    ): ByteArray {
        return ClassPatcher.applyPatch(name, transformedName, source)
    }
}