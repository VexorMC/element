package com.element.injection.patch

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteStreams
import com.nothome.delta.GDiffPatcher
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ClassPatcher {
    val patches = mutableMapOf<String, ClassPatchEntry>()
    val patchCache = mutableMapOf<String, ByteArray>()

    private val patcher = GDiffPatcher()

    private val logger = LogManager.getLogger("ClassPatcher")

    fun discoverPatches() {
        val jis = ZipInputStream(
            javaClass.getResourceAsStream("/patches.zip") ?: error("Patches archive not found"))

        logger.info("Reading entries")
        do {
            try {
                val entry = jis.nextEntry ?: break
                val cp = readPatch(entry, jis)
                cp.let {
                    patches.put(it.cleanName.replace(".class", ""), it)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } while (true)

        logger.info("Discovered ${patches.size} patches")
    }

    fun readPatch(patchEntry: ZipEntry, jis: ZipInputStream): ClassPatchEntry {
        val input: ByteArrayDataInput
        try {
            input = ByteStreams.newDataInput(jis.readAllBytes())
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("Unable to read binary patch file ${patchEntry.name}")
        }

        val name = input.readUTF()
        val cleanName = input.readUTF()
        val modifiedName = input.readUTF()

        val patchLength = input.readInt()
        val patchBytes = ByteArray(patchLength)

        input.readFully(patchBytes)

        return ClassPatchEntry(name, cleanName, modifiedName, patchBytes)
    }

    fun applyPatch(name: String, mappedName: String, inputData: ByteArray): ByteArray {
        if (patchCache.containsKey(name)) {
            return patchCache[name]!!
        }

        println("$name - $mappedName")

        val patch = patches[mappedName] ?: return inputData

        var transformed: ByteArray = inputData

        synchronized(patcher) {
            try {
                transformed = patcher.patch(inputData, patch.patchBytes)
            } catch (e: IOException) {
                logger.error("Failed to patch $name", e)
            }
        }

        patchCache[name] = transformed

        return transformed
    }
}
