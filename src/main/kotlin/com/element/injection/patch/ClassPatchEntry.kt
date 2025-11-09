package com.element.injection.patch

data class ClassPatchEntry(
    val name: String,
    val cleanName: String,
    val modifiedName: String,
    val patchBytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassPatchEntry

        if (name != other.name) return false
        if (cleanName != other.cleanName) return false
        if (modifiedName != other.modifiedName) return false
        if (!patchBytes.contentEquals(other.patchBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + cleanName.hashCode()
        result = 31 * result + modifiedName.hashCode()
        result = 31 * result + patchBytes.contentHashCode()
        return result
    }
}