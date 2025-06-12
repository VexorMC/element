package com.element.injection.transform

interface ClassTransformer {
    /**
     * Transforms the provided class bytes.
     */
    fun transform(name: String, transformedName: String, source: ByteArray): ByteArray
}