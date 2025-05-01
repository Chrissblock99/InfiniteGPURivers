package me.chriss99.program

import org.lwjgl.opengl.GL45.*
import java.io.File
import java.io.FileNotFoundException
import java.util.*

open class GLProgram {
    val program: Int
    private val shaders = ArrayList<Int>()

    init {
        this.program = glCreateProgram()
    }

    fun addShader(name: String, type: Int) {
        val shader = loadShader(File("src/main/glsl/$name"), type)
        if (glGetShaderi(shader, GL_COMPILE_STATUS) !== 1) println("Shader $name did not compile!")

        shaders.add(shader)
        glAttachShader(program, shader)
    }

    fun bindAttribute(index: Int, name: String) {
        glBindAttribLocation(program, index, name)
    }

    fun validate() {
        glLinkProgram(program)
        glValidateProgram(program)

        val link: Int = glGetProgrami(program, GL_LINK_STATUS)
        val validate: Int = glGetProgrami(program, GL_VALIDATE_STATUS)

        if (link == 1 && validate == 1) return

        println("Program Linked: $link")
        println("Program Validated: $validate")
    }

    fun use() {
        glUseProgram(program)
    }

    fun getUniform(name: String): Int {
        return glGetUniformLocation(program, name)
    }

    open fun delete() {
        //detach the shaders from the program object
        for (shader in shaders) {
            glDetachShader(program, shader)
            glDeleteShader(shader)
        }

        //stop using the shader program
        glUseProgram(0)

        //delete the program now that the shaders are detached and the program isn't being used
        glDeleteProgram(program)
    }

    companion object {
        fun loadShader(file: File, type: Int): Int {
            try {
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
                glCompileShader(id)
                return id
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return -1
            }
        }
    }
}