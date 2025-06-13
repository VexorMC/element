package com.element.injection.transform.impl

import com.element.configuration.model.GameEnvironment
import com.element.injection.transform.ClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.tree.*

/**
 * Hacky patches to get OpenAL working properly without having to binary patch SoundSystem.
 *
 * A lot of inspiration was taken from [Spice](https://github.com/Polyfrost/Spice); thanks to them for their work on this!
 */
class OpenALTransformer : ClassTransformer() {
    override fun transform(name: String, transformedName: String, source: ByteArray): ByteArray {
        if (name != "org.lwjgl.openal.AL" && name != "org.lwjgl.openal.AL10") return source

        val classReader = ClassReader(source)
        val classNode = ClassNode()
        classReader.accept(classNode, 0)

        val methodsToInject = getInjectionTargets(name)

        classNode.methods.removeIf { method ->
            methodsToInject.any { it.name == method.name && it.desc == method.desc }
        }

        methodsToInject.forEach { injected ->
            val methodNode = MethodNode(injected.access, injected.name, injected.desc, null, null)
            injected.instructions.forEach { methodNode.instructions.add(it) }
            classNode.methods.add(methodNode)
        }

        val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        classNode.accept(writer)
        return writer.toByteArray()
    }

    private fun getInjectionTargets(className: String): List<Injection> {
        return when (className) {
            "org.lwjgl.openal.AL" -> listOf(
                Injection(
                    "create",
                    "()V",
                    Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_SYNTHETIC,
                    listOf(
                        MethodInsnNode(Opcodes.INVOKESTATIC, "com/element/injection/patch/al/OpenALPatches", "create", "()V", false),
                        InsnNode(Opcodes.RETURN)
                    )
                ),
                Injection(
                    "isCreated",
                    "()Z",
                    Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_SYNTHETIC,
                    listOf(
                        MethodInsnNode(Opcodes.INVOKESTATIC, "com/element/injection/patch/al/OpenALPatches", "isCreated", "()Z", false),
                        InsnNode(Opcodes.IRETURN)
                    )
                ),
                Injection(
                    "destroy",
                    "()V",
                    Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_SYNTHETIC,
                    listOf(
                        MethodInsnNode(Opcodes.INVOKESTATIC, "com/element/injection/patch/al/OpenALPatches", "destroy", "()V", false),
                        InsnNode(Opcodes.RETURN)
                    )
                )
            )
            "org.lwjgl.openal.AL10" -> listOf(
                Injection(
                    name = "alListener",
                    desc = "(ILjava/nio/FloatBuffer;)V",
                    access = Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_SYNTHETIC,
                    instructions = listOf(
                        VarInsnNode(Opcodes.ILOAD, 0),
                        VarInsnNode(Opcodes.ALOAD, 1),
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "org/lwjgl/openal/AL10",
                            "alListenerfv",
                            "(ILjava/nio/FloatBuffer;)V",
                            false
                        ),
                        InsnNode(Opcodes.RETURN)
                    )
                ),
                Injection(
                    name = "alSource",
                    desc = "(IILjava/nio/FloatBuffer;)V",
                    access = Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_SYNTHETIC,
                    instructions = listOf(
                        VarInsnNode(Opcodes.ILOAD, 0),
                        VarInsnNode(Opcodes.ILOAD, 1),
                        VarInsnNode(Opcodes.ALOAD, 2),
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "org/lwjgl/openal/AL10",
                            "alSourcefv",
                            "(IILjava/nio/FloatBuffer;)V",
                            false
                        ),
                        InsnNode(Opcodes.RETURN)
                    )
                )
            )
            else -> emptyList()
        }
    }

    private data class Injection(
        val name: String,
        val desc: String,
        val access: Int,
        val instructions: List<AbstractInsnNode>
    )
}
