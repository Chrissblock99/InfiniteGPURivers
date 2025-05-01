package me.chriss99.worldmanagement;

import org.joml.Vector2i;

import java.io.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FileLoadStoreManager<T> {
    private final String folderPath;
    private final String fileExtension;
    private final BiFunction<byte[], Vector2i, T> fileFromBytes;
    private final Function<T, byte[]> fileToBytes;

    public FileLoadStoreManager(String folderPath, String fileExtension, BiFunction<byte[], Vector2i, T> fileFromBytes, Function<T, byte[]> fileToBytes) {
        this.folderPath = folderPath + "/";
        this.fileExtension = "." + fileExtension;
        this.fileFromBytes = fileFromBytes;
        this.fileToBytes = fileToBytes;

        new File(folderPath).mkdirs();
    }

    public T loadFile(Vector2i coord) {
        try (FileInputStream inputStream = new FileInputStream(getFile(coord))) {
            return fileFromBytes.apply(inputStream.readAllBytes(), coord);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveFile(T type, Vector2i coord) {
        try (FileOutputStream outputStream = new FileOutputStream(getFile(coord))) {
            outputStream.write(fileToBytes.apply(type));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFile(Vector2i coord) {
        File file = new File(folderPath + coord.x + ";" + coord.y + fileExtension);

        if (!file.exists())
            try {
                file.createNewFile();
            } catch (IOException e1) {
                throw new RuntimeException("Could not write to folder!");
            }

        return file;
    }
}
