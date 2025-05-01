package me.chriss99.worldmanagement

interface FileStorage<K, V> {
    fun hasFile(key: K): Boolean
    fun loadFile(key: K): V
    fun saveFile(key: K, file: V)
}