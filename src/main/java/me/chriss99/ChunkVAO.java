package me.chriss99;

import me.chriss99.glabstractions.VAO;
import org.joml.Vector2i;

public interface ChunkVAO extends VAO {
    Vector2i getSrcPos();
    int getWidth();
}