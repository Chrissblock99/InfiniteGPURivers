package me.chriss99.erosion;

import me.chriss99.Area;
import me.chriss99.IterationSurfaceType;
import me.chriss99.worldmanagement.iteration.IterableWorld;
import org.joml.Vector2i;
import org.joml.Vector4i;

import java.util.*;

public class ErosionManager {
    private final GPUTerrainEroder eroder;
    private final IterableWorld data;

    private final Vector2i maxChunks;

    public ErosionManager(GPUTerrainEroder eroder, IterableWorld data) {
        this.eroder = eroder;
        this.data = data;

        maxChunks = new Vector2i(eroder.getMaxTextureSize()).div(data.chunkSize);
    }

    public boolean findIterate(Area area, int maxIteration) {
        HashMap<Integer, LinkedHashSet<Vector2i>> tilesAtIteration = new LinkedHashMap<>();

        area.forAllPoints(pos -> {
            int iteration = data.getTile(pos).iteration;
            tilesAtIteration.computeIfAbsent(iteration, k -> new LinkedHashSet<>()).add(pos);
        });

        List<Integer> sortedIterations = tilesAtIteration.keySet().stream().sorted(Comparator.naturalOrder()).toList();

        HashSet<Vector2i> candidates = null;
        for (int lowestIteration : sortedIterations) {
            if (lowestIteration > maxIteration)
                return false;

            LinkedHashSet<Vector2i> currentCandidates = tilesAtIteration.get(lowestIteration);
            if (hasIterable2x2Area(currentCandidates)) {
                candidates = currentCandidates;
                break;
            }
        }
        if (candidates == null)
            return false;


        Area bestArea = new Area();

        for (Vector2i currentPos : candidates) {
            Area betterArea = betterAreaFrom(currentPos, bestArea);
            if (betterArea != null)
                bestArea = betterArea;

            if (bestArea.getSize().equals(maxChunks))
                break;
        }

        if (bestArea.equals(new Area()))
            return false;

        iterate(bestArea);
        return true;
    }

    private boolean hasIterable2x2Area(LinkedHashSet<Vector2i> tiles) {
        for (Vector2i pos : tiles)
            if (tiles.contains(new Vector2i(pos).add(1, 0)) && tiles.contains(new Vector2i(pos).add(0, 1)) && tiles.contains(new Vector2i(pos).add(1, 1)) && iterable(new Area(pos, 2)))
                return true;

        return false;
    }

    private static final Vector4i[] changes = new Vector4i[]{new Vector4i(0, 0, 1, 0), new Vector4i(0, 0, 0, 1), new Vector4i(1, 0, 0, 0), new Vector4i(0, 1, 0, 0)};
    private Area betterAreaFrom(Vector2i startPos, Area bestArea) {
        Area betterArea = new Area(startPos, 2);

        if (!iterable(betterArea))
            return null;

        boolean[] directions = new boolean[]{true, true, true, true};

        for (int i = 0; directions[0] || directions[1] || directions[2] || directions[3]; i = (i+1) % 4) {
            if (betterArea.getWidth() == maxChunks.x) {
                directions[0] = false;
                directions[2] = false;
            }
            if (betterArea.getHeight() == maxChunks.y) {
                directions[1] = false;
                directions[3] = false;
            }


            if (!directions[i])
                continue;

            Area betterAreaTry = betterArea.increase(changes[i].x, changes[i].y, changes[i].z, changes[i].w);

            if (iterable(betterAreaTry))
                betterArea = betterAreaTry;
            else
                directions[i] = false;
        }


        if (betterArea.getArea() > bestArea.getArea())
            return betterArea;
        return null;
    }

    private boolean iterable(Area area) {
        if (area.getWidth() < 2 || area.getHeight() < 2)
            return false;

        Vector2i length = area.getSize().sub(1, 1);

        int l = getEdgesEqual(area.srcPos(), length.y, true);
        int r = getEdgesEqual(area.srcPos().add(length.x,0), length.y, true);
        int f = getEdgesEqual(area.srcPos().add(0,length.y), length.x, false);
        int b = getEdgesEqual(area.srcPos(), length.x, false);

        if (l > 1 || r > 1 || f > 1 || b > 1)
            return false;

        if (l == -1 || r == 1 || f == 1 || b == -1)
            return false;

        IterationSurfaceType.SurfaceType allowed = IterationSurfaceType.SurfaceType.FLAT;
        boolean tl = allowed.equals(data.getIterationSurfaceType(area.srcPos().add(0, length.y)).getSurfaceType());
        boolean tr = allowed.equals(data.getIterationSurfaceType(area.srcPos().add(length)).getSurfaceType());
        boolean dl = allowed.equals(data.getIterationSurfaceType(area.srcPos()).getSurfaceType());
        boolean dr = allowed.equals(data.getIterationSurfaceType(area.srcPos().add(length.x, 0)).getSurfaceType());

        if (l == 0 && f == 0 && !tl || f == 0 && r == 0 && !tr || l == 0 && b == 0 && !dl || b == 0 && r == 0 && !dr)
            return false;

        return iterationsAreSame(area);
    }

    private void iterate(Area area) {
        Vector2i length = area.getSize().sub(1, 1);

        int l = getEdgesEqual(area.srcPos(), length.y, true);
        int r = getEdgesEqual(area.srcPos().add(length.x,0), length.y, true);
        int f = getEdgesEqual(area.srcPos().add(0,length.y), length.x, false);
        int b = getEdgesEqual(area.srcPos(), length.x, false);

        Area erosionArea = area.mul(data.chunkSize);
        eroder.changeArea(erosionArea);
        ErosionTask task = new ErosionTask(eroder, erosionArea, data.chunkSize, l == 0, r == 0, f == 0, b == 0);
        while (!task.erosionStep());

        l--;
        r++;
        f++;
        b--;

        setEdges(area.srcPos(), length.y, true, l);
        setEdges(area.srcPos().add(length.x,0), length.y, true, r);
        setEdges(area.srcPos().add(0,length.y), length.x, false, f);
        setEdges(area.srcPos(), length.x, false, b);

        increaseIteration(area, l, r, f, b);
    }

    private void increaseIteration(Area area, int l, int r, int f, int b) {
        Area inner = area.outset(-1);
        Vector2i size = area.getSize().sub(1, 1);

        inner.forAllPoints(v -> data.getTile(v).iteration += data.chunkSize);

        if (l == 0)
            for (int y = 1; y < size.y; y++)
                data.getTile(area.srcPos().add(0, y)).iteration += data.chunkSize;
        if (r == 0)
            for (int y = 1; y < size.y; y++)
                data.getTile(area.srcPos().add(size.x, y)).iteration += data.chunkSize;
        if (f == 0)
            for (int x = 1; x < size.x; x++)
                data.getTile(area.srcPos().add(x, size.y)).iteration += data.chunkSize;
        if (b == 0)
            for (int x = 1; x < size.x; x++)
                data.getTile(area.srcPos().add(x, 0)).iteration += data.chunkSize;

        if (l == 0 && b == 0)
            data.getTile(area.srcPos()).iteration += data.chunkSize;
        if (l == 0 && f == 0)
            data.getTile(area.srcPos().add(0, size.y)).iteration += data.chunkSize;
        if (r == 0 && b == 0)
            data.getTile(area.srcPos().add(size.x, 0)).iteration += data.chunkSize;
        if (r == 0 && f == 0)
            data.getTile(inner.endPos()).iteration += data.chunkSize;
    }

    private boolean iterationsAreSame(Area area) {
        int iteration = data.getTile(area.srcPos()).iteration;

        for (Vector2i pos : area.allPoints())
            if (iteration != data.getTile(pos).iteration)
                return false;
        return true;
    }

    /**
     *
     * @param pos where to start checking for equality
     * @param length how far to check for equality
     * @param upwards whether to check for equality upwards or sideways
     * @return -1, 0 or 1 if all are equal, else the length at which it isn't equal (larger than 1)
     */
    private int getEdgesEqual(Vector2i pos, int length, boolean upwards) {
        int edge = getEdge(pos, 1, upwards);

        for (int i = 2; i <= length; i++)
            if (getEdge(pos, i, upwards) != edge)
                return i;

        return edge;
    }

    private int getEdge(Vector2i pos, int offset, boolean upwards) {
        return upwards ? data.getTile(new Vector2i(pos).add(0, offset)).horizontal : data.getTile(new Vector2i(pos).add(offset, 0)).vertical;
    }

    private void setEdges(Vector2i pos, int length, boolean upwards, int value) {
        for (int i = 1; i <= length; i++)
            if (upwards)
                data.getTile(new Vector2i(pos).add(0, i)).horizontal = value;
            else
                data.getTile(new Vector2i(pos).add(i, 0)).vertical = value;
    }
}
