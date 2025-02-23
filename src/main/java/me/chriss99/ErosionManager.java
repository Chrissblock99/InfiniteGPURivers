package me.chriss99;

import me.chriss99.worldmanagement.iteration.IterableWorld;
import org.joml.Vector2i;

import java.util.*;

public class ErosionManager {
    private final GPUTerrainEroder eroder;
    private final IterableWorld data;

    private final Vector2i maxChunks;

    public ErosionManager(GPUTerrainEroder eroder, IterableWorld data) {
        this.eroder = eroder;
        this.data = data;

        maxChunks = new Vector2i(eroder.getTextureLimit()).div(data.chunkSize);
    }

    public boolean findIterate(Vector2i pos, Vector2i size, int maxIteration) {
        HashMap<Integer, LinkedHashSet<Vector2i>> tilesAtIteration = new LinkedHashMap<>();

        for (int x = 0; x < size.x; x++)
            for (int y = 0; y < size.y; y++) {
                Vector2i currentPos = new Vector2i(x, y).add(pos);
                int iteration = data.getTile(currentPos.x, currentPos.y).iteration;

                tilesAtIteration.computeIfAbsent(iteration, k -> new LinkedHashSet<>()).add(currentPos);
            }

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


        Vector2i bestPos = new Vector2i(Integer.MIN_VALUE);
        Vector2i bestSize = new Vector2i();

        for (Vector2i currentPos : candidates) {
            betterAreaFrom(currentPos, bestPos, bestSize);
            if (bestSize.equals(maxChunks))
                break;
        }

        if (bestPos.equals(new Vector2i(Integer.MIN_VALUE)))
            return false;

        iterate(bestPos, bestSize);
        return true;
    }

    private boolean hasIterable2x2Area(LinkedHashSet<Vector2i> tiles) {
        for (Vector2i pos : tiles)
            if (tiles.contains(new Vector2i(pos).add(1, 0)) && tiles.contains(new Vector2i(pos).add(0, 1)) && tiles.contains(new Vector2i(pos).add(1, 1)) && iterable(pos, new Vector2i(2)))
                return true;

        return false;
    }

    private static final Vector2i[] posChanges = new Vector2i[]{new Vector2i(1, 0), new Vector2i(0, 1), new Vector2i(), new Vector2i()};
    private static final Vector2i[] sizeChanges = new Vector2i[]{new Vector2i(1, 0), new Vector2i(0, 1), new Vector2i(1, 0), new Vector2i(0, 1)};
    private boolean betterAreaFrom(Vector2i pos, Vector2i bestPos, Vector2i bestSize) {
        Vector2i size = new Vector2i(2);

        if (!iterable(pos, size))
            return false;

        boolean[] directions = new boolean[]{true, true, true, true};

        for (int i = 0; directions[0] || directions[1] || directions[2] || directions[3]; i = (i+1) % 4) {
            if (size.x == maxChunks.x) {
                directions[0] = false;
                directions[2] = false;
            }
            if (size.y == maxChunks.y) {
                directions[1] = false;
                directions[3] = false;
            }


            if (!directions[i])
                continue;

            pos.sub(posChanges[i]);
            size.add(sizeChanges[i]);

            if (!iterable(pos, size)) {
                pos.add(posChanges[i]);
                size.sub(sizeChanges[i]);
                directions[i] = false;
            }
        }


        if (size.x*size.y > bestSize.x*bestSize.y) {
            bestPos.x = pos.x;
            bestPos.y = pos.y;
            bestSize.x = size.x;
            bestSize.y = size.y;

            return true;
        }

        return false;
    }

    private boolean iterable(Vector2i pos, Vector2i size) {
        if (size.x < 2 || size.y < 2)
            return false;

        int x = pos.x;
        int y = pos.y;

        Vector2i length = new Vector2i(size).sub(1, 1);

        int l = getEdgesEqual(pos, length.y, true);
        int r = getEdgesEqual(new Vector2i(pos).add(length.x,0), length.y, true);
        int f = getEdgesEqual(new Vector2i(pos).add(0,length.y), length.x, false);
        int b = getEdgesEqual(pos, length.x, false);

        if (l > 1 || r > 1 || f > 1 || b > 1)
            return false;

        if (l == -1 || r == 1 || f == 1 || b == -1)
            return false;

        IterationSurfaceType.SurfaceType allowed = IterationSurfaceType.SurfaceType.FLAT;
        boolean tl = allowed.equals(data.getIterationSurfaceType(x, y+length.y).getSurfaceType());
        boolean tr = allowed.equals(data.getIterationSurfaceType(x+length.x, y+length.y).getSurfaceType());
        boolean dl = allowed.equals(data.getIterationSurfaceType(x, y).getSurfaceType());
        boolean dr = allowed.equals(data.getIterationSurfaceType(x+length.x, y).getSurfaceType());

        if (l == 0 && f == 0 && !tl || f == 0 && r == 0 && !tr || l == 0 && b == 0 && !dl || b == 0 && r == 0 && !dr)
            return false;

        return iterationsAreSame(pos, size);
    }

    private void iterate(Vector2i pos, Vector2i size) {
        int x = pos.x;
        int y = pos.y;

        Vector2i length = new Vector2i(size).sub(1, 1);

        int l = getEdgesEqual(pos, length.y, true);
        int r = getEdgesEqual(new Vector2i(pos).add(length.x,0), length.y, true);
        int f = getEdgesEqual(new Vector2i(pos).add(0,length.y), length.x, false);
        int b = getEdgesEqual(pos, length.x, false);

        eroder.changeArea(new Vector2i(x, y).mul(data.chunkSize), new Vector2i(data.chunkSize).mul(size));
        eroder.erosionSteps(data.chunkSize, l == 0, r == 0, f == 0, b == 0);

        l--;
        r++;
        f++;
        b--;

        setEdges(pos, length.y, true, l);
        setEdges(new Vector2i(pos).add(length.x,0), length.y, true, r);
        setEdges(new Vector2i(pos).add(0,length.y), length.x, false, f);
        setEdges(pos, length.x, false, b);

        increaseIteration(pos, size, l, r, f, b);
    }

    private void increaseIteration(Vector2i pos, Vector2i size, int l, int r, int f, int b) {
        size = new Vector2i(size).sub(1, 1);

        for (int x = 1; x < size.x; x++)
            for (int y = 1; y < size.y; y++)
                data.getTile(pos.x + x, pos.y + y).iteration += data.chunkSize;

        if (l == 0)
            for (int y = 1; y < size.y; y++)
                data.getTile(pos.x, pos.y + y).iteration += data.chunkSize;
        if (r == 0)
            for (int y = 1; y < size.y; y++)
                data.getTile(pos.x + size.x, pos.y + y).iteration += data.chunkSize;
        if (f == 0)
            for (int x = 1; x < size.x; x++)
                data.getTile(pos.x + x, pos.y + size.y).iteration += data.chunkSize;
        if (b == 0)
            for (int x = 1; x < size.x; x++)
                data.getTile(pos.x + x, pos.y).iteration += data.chunkSize;

        if (l == 0 && b == 0)
            data.getTile(pos.x, pos.y).iteration += data.chunkSize;
        if (l == 0 && f == 0)
            data.getTile(pos.x, pos.y + size.y).iteration += data.chunkSize;
        if (r == 0 && b == 0)
            data.getTile(pos.x + size.x, pos.y).iteration += data.chunkSize;
        if (r == 0 && f == 0)
            data.getTile(pos.x + size.x, pos.y + size.y).iteration += data.chunkSize;
    }

    private boolean iterationsAreSame(Vector2i pos, Vector2i size) {
        int iteration = data.getTile(pos.x, pos.y).iteration;

        for (int x = 0; x < size.x; x++)
            for (int y = 0; y < size.y; y++)
                if (iteration != data.getTile(pos.x + x, pos.y + y).iteration)
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
        return upwards ? data.getTile(pos.x, pos.y+offset).horizontal : data.getTile(pos.x+offset, pos.y).vertical;
    }

    private void setEdges(Vector2i pos, int length, boolean upwards, int value) {
        for (int i = 1; i <= length; i++)
            if (upwards)
                data.getTile(pos.x, pos.y+i).horizontal = value;
            else
                data.getTile(pos.x+i, pos.y).vertical = value;
    }
}
