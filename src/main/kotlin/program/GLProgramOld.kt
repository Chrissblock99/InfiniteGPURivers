package me.chriss99.program

import org.lwjgl.opengl.GL45.*
import java.io.File
import java.util.*

open class GLProgramOld {
    val program: Int = glCreateProgram()
    private val shaders = ArrayList<Pair<Int, String>>()

    fun addShader(name: String, type: Int) {
        val shader = loadShader(File("src/main/glsl/$name"), type)

        shaders.add(shader to name)
        glAttachShader(program, shader)
    }

    fun validate() {
        for ((shader, name) in shaders) {
            glCompileShader(shader)
            if (glGetShaderi(shader, GL_COMPILE_STATUS) != 1)
                System.err.println("Shader $name did not compile!")
        }
        glLinkProgram(program)
        glValidateProgram(program)

        val link: Int = glGetProgrami(program, GL_LINK_STATUS)
        val validate: Int = glGetProgrami(program, GL_VALIDATE_STATUS)

        if (link == 1 && validate == 1)
            return

        System.err.println("Program Linked: $link")
        System.err.println("Program Validated: $validate")

        throw IllegalStateException("A program broke!")
    }

    fun bindAttribute(index: Int, name: String) = glBindAttribLocation(program, index, name)
    fun use() = glUseProgram(program)
    fun getUniform(name: String) = glGetUniformLocation(program, name)

    open fun delete() {
        for ((shader, _) in shaders) {
            glDetachShader(program, shader)
            glDeleteShader(shader)
        }

        glUseProgram(0)
        glDeleteProgram(program)
    }

    companion object {
        fun loadShader(file: File, type: Int): Int {
            val sc = Scanner(file)
            val data = StringBuilder()

            if (file.exists()) {
                while (sc.hasNextLine()) {
                    data.append(sc.nextLine()).append("\n")
                }

                sc.close()
            }
            val id: Int = glCreateShader(type)
            glShaderSource(id, data)
            return id
        }
    }
}