package me.chriss99.glabstractions

import org.lwjgl.opengl.GL30.*

open class VAOImpl : VAO {
    private val vao: Int
    private val vbos: Array<VBO<*>>
    private val ibo: IntGLBuffer?
    final override var indexLength: Int = 0
        private set

    constructor(index: IntArray?, vertexSize: Int, vararg vbos: DoubleArray) {
        vao = glGenVertexArrays()
        bind()

        this.vbos = Array(vbos.size) {
            val vbo: VBO<*> = VBO(DoubleGLBuffer(GL_ARRAY_BUFFER).updateData(vbos[it], GL_STATIC_DRAW), vertexSize)

            glVertexAttribPointer(it, vbo.vertexSize, GL_DOUBLE, false, 0, 0)
            glEnableVertexAttribArray(it)

            return@Array vbo
        }

        ibo = if (index != null) IntGLBuffer(GL_ELEMENT_ARRAY_BUFFER).updateData(index, GL_STATIC_DRAW) else null
        indexLength = index?.size ?: (vbos[0].size / vertexSize)
    }

    constructor(index: IntArray?, vertexSize: Int, vararg vbos: FloatArray) {
        vao = glGenVertexArrays()
        bind()

        this.vbos = Array(vbos.size) {
            val vbo: VBO<*> = VBO(FloatGLBuffer(GL_ARRAY_BUFFER).updateData(vbos[it], GL_STATIC_DRAW), vertexSize)

            glVertexAttribPointer(it, vbo.vertexSize, GL_FLOAT, false, 0, 0)
            glEnableVertexAttribArray(it)

            return@Array vbo
        }

        ibo = if (index != null) IntGLBuffer(GL_ELEMENT_ARRAY_BUFFER).updateData(index, GL_STATIC_DRAW) else null
        indexLength = index?.size ?: vbos[0].size / vertexSize
    }

    fun updateVertices(index: Int, vertices: DoubleArray) {
        (vbos[index].buffer as DoubleGLBuffer).updateData(vertices, GL_STREAM_DRAW)
    }

    fun updateVertices(index: Int, vertices: FloatArray) {
        (vbos[index].buffer as FloatGLBuffer).updateData(vertices, GL_STREAM_DRAW)
    }

    fun updateIndices(indices: IntArray) {
        ibo?.updateData(indices, GL_STREAM_DRAW) ?: throw IllegalArgumentException("This doesn't have an IBO!")
        indexLength = indices.size
    }

    override fun bind() = glBindVertexArray(vao)

    override fun delete() {
        vbos.forEach { it.delete() }
        ibo?.delete()
        glDeleteVertexArrays(vao)
    }
}