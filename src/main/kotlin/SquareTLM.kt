package me.chriss99;

import me.chriss99.worldmanagement.TileLoadManager;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Collection;

public class SquareTLM<T> implements TileLoadManager<T> {
    protected int renderDistance;
    protected Vector2f position;

    public SquareTLM(int renderDistance, Vector2f initPos) {
        this.renderDistance = renderDistance;
        this.position = initPos;
    }

    @Override
    public boolean loadPolicy(Vector2i tilePos, T tile) {
        Vector2f distance = new Vector2f(tilePos).sub(position).absolute();
        return distance.x < renderDistance && distance.y < renderDistance;
    }

    @Override
    public Collection<Vector2i> loadCommander() {
        ArrayList<Vector2i> toLoad = new ArrayList<>();

        for (int x = -renderDistance; x < renderDistance+1; x++)
            for (int y = -renderDistance; y < renderDistance+1; y++)
                toLoad.add(new Vector2i((int) position.x + x, (int) position.y + y));

        return toLoad;
    }

    public int getRenderDistance() {
        return renderDistance;
    }

    public void setRenderDistance(int renderDistance) {
        this.renderDistance = renderDistance;
    }

    public void setPosition(Vector2f position) {
        this.position = new Vector2f(position);
    }
}
