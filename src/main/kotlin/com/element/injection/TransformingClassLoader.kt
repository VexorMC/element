package com.element.injection

import com.element.injection.transform.ClassTransformer
import java.util.concurrent.CopyOnWriteArrayList

class TransformingClassLoader(
    parent: ClassLoader = getSystemClassLoader()
) : ClassLoader(parent) {
    val transformers: MutableList<ClassTransformer> = CopyOnWriteArrayList()

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun.") || name.startsWith("jdk.") || name.startsWith("org.apache.logging.log4j.") || name.startsWith("com.google.common.") || findLoadedClass(name) != null) {
            return super.loadClass(name, resolve)
        }

        findLoadedClass(name)?.let { return it }

        return try {
            val clazz = findClass(name)
            if (resolve) {
                resolveClass(clazz)
            }
            clazz
        } catch (_: ClassNotFoundException) {
            super.loadClass(name, resolve)
        }
    }

    override fun findClass(name: String): Class<*> {
        val path = name.replace('.', '/').plus(".class")

        val classBytes = getResourceAsStream(path)?.use { it.readBytes() }
            ?: throw ClassNotFoundException("Class $name not found")

        val transformedBytes = applyTransformers(name, classBytes)

        return defineClass(name, transformedBytes, 0, transformedBytes.size)
    }

    private fun applyTransformers(name: String, bytes: ByteArray): ByteArray {
        var transformedBytes = bytes
        for (transformer in transformers) {
            transformedBytes = transformer.transform(name, name, transformedBytes)
        }
        return transformedBytes
    }
}
