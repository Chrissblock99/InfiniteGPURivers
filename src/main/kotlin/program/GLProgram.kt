package me.chriss99.program

import org.lwjgl.opengl.GL45.*
import java.io.File
import java.io.FileNotFoundException

open class GLProgram(folder: String = "", vararg fileNames: String) {
    val program: Int = glCreateProgram()
    init {
        fileNames.map { File("src/main/glsl/${ if (folder == "") "" else "$folder/" }$it") }.map {
            if (!it.exists())
                throw FileNotFoundException(it.path)

            val shader = glCreateShader(typeFromFileExtension(it.name.split(".").last()))
            glAttachShader(program, shader)
            glShaderSource(shader, it.readText())
            glCompileShader(shader)

            return@map shader
        }.forEach {
            if (glGetShaderi(it, GL_COMPILE_STATUS) == GL_TRUE)
                return@forEach

            System.err.println(glGetShaderInfoLog(it))
            throw IllegalStateException("A shader did not compile!")
        }


        glLinkProgram(program)
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println(glGetProgramInfoLog(program))
            throw IllegalStateException("A program did not link!")
        }

        glValidateProgram(program)
        if (glGetProgrami(program, GL_VALIDATE_STATUS) == GL_FALSE) {
            System.err.println(glGetProgramInfoLog(program))
            throw IllegalStateException("A program did not validate!")
        }
    }

    fun bindAttribute(index: Int, name: String) = glBindAttribLocation(program, index, name)
    fun use() = glUseProgram(program)
    fun getUniform(name: String) = glGetUniformLocation(program, name)

    open fun delete() {
        val shadersMax = glGetProgrami(program, GL_ATTACHED_SHADERS)

        if (shadersMax > 0) {
            val shaders = IntArray(shadersMax)
            glGetAttachedShaders(program, intArrayOf(shadersMax), shaders)
            shaders.forEach {
                glDetachShader(program, it)
                glDeleteShader(it)
            }
        }

        glDeleteProgram(program)
    }

    companion object {
        fun typeFromFileExtension(ext: String) = when(ext) {
            "vert" -> GL_VERTEX_SHADER
            "frag" -> GL_FRAGMENT_SHADER
            "geom" -> GL_GEOMETRY_SHADER
            "tesc" -> GL_TESS_CONTROL_SHADER
            "tese" -> GL_TESS_EVALUATION_SHADER
            "comp" -> GL_COMPUTE_SHADER
            else -> throw IllegalArgumentException("$ext is not a valid shader file extension!")
        }
    }
}