package me.chriss99.worldmanagement;

public interface FileStorage<K, V> {
    boolean hasFile(K key);
    V loadFile(K key);
    void saveFile(V file);
}
