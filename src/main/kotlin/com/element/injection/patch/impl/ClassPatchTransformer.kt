package com.element.injection.patch.impl

import com.element.injection.patch.ClassPatcher
import com.element.injection.transform.ClassTransformer

class ClassPatchTransformer : ClassTransformer {
    override fun transform(
        name: String,
        transformedName: String,
        source: ByteArray
    ): ByteArray {
        return ClassPatcher.applyPatch(name, transformedName, source)
    }
}