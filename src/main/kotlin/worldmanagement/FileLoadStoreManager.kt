package me.chriss99.worldmanagement

import glm_.vec2.Vec2i
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FileLoadStoreManager<F>(
    folderPath: String, fileExtension: String,
    private val fileFromBytes: (bytes: ByteArray, pos: Vec2i) -> F,
    private val fileToBytes: (file: F) -> ByteArray
) {
    private val folderPath = "$folderPath/"
    private val fileExtension = ".$fileExtension"

    init {
        File(folderPath).mkdirs()
    }

    fun loadFile(coord: Vec2i): F = fileFromBytes(getFile(coord).readBytes(), coord)
    fun saveFile(file: F, coord: Vec2i) = getFile(coord).writeBytes(fileToBytes(file))

    private fun getFile(coord: Vec2i): File {
        val file = File(folderPath + coord.x + ";" + coord.y + fileExtension)

        if (!file.exists())
            file.createNewFile()

        return file
    }
}