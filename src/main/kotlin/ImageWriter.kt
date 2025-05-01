package me.chriss99

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

object ImageWriter {
    @JvmStatic
    fun main(args: Array<String>) {
        writeImageHeightMap(
            Float2DBufferWrapper(arrayOf<FloatArray>(floatArrayOf(1f, 0.5f), floatArrayOf(0f, .75f))),
            "test",
            true
        )
    }

    fun writeImageHeightMap(heightMap: Float2DBufferWrapper, filename: String, negative: Boolean) {
        val image = BufferedImage(heightMap.size.x, heightMap.size.y, BufferedImage.TYPE_INT_RGB)

        for (i in 0..<heightMap.size.x) for (j in 0..<heightMap.size.y) {
            val heightValue: Float = heightMap.getFloat(i, j) / 250 + (if (negative) .5f else 0f)
            val myRGB = Color(heightValue, heightValue, heightValue)
            val rgb = myRGB.rgb
            image.setRGB(i, j, rgb)
        }

        val file = File("images/$filename.png")
        file.mkdirs()
        try {
            ImageIO.write(image, "png", file)
        } catch (e: IOException) {
            println("Couldn't save image!")
        }
    }
}