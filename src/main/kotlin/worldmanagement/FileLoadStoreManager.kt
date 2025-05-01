package me.chriss99.worldmanagement

import org.joml.Vector2i
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.function.BiFunction
import java.util.function.Function

class FileLoadStoreManager<T>(
    folderPath: String, fileExtension: String, fileFromBytes: BiFunction<ByteArray, Vector2i, T>,
    private val fileToBytes: Function<T, ByteArray>
) {
    private val folderPath = "$folderPath/"
    private val fileExtension = ".$fileExtension"
    private val fileFromBytes: BiFunction<ByteArray, Vector2i, T> = fileFromBytes

    init {
        File(folderPath).mkdirs()
    }

    fun loadFile(coord: Vector2i): T {
        try {
            FileInputStream(getFile(coord)).use { inputStream ->
                return fileFromBytes.apply(inputStream.readAllBytes(), coord)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun saveFile(type: T, coord: Vector2i) {
        try {
            FileOutputStream(getFile(coord)).use { outputStream ->
                outputStream.write(fileToBytes.apply(type))
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun getFile(coord: Vector2i): File {
        val file = File(folderPath + coord.x + ";" + coord.y + fileExtension)

        if (!file.exists()) try {
            file.createNewFile()
        } catch (e1: IOException) {
            throw RuntimeException("Could not write to folder!")
        }

        return file
    }
}