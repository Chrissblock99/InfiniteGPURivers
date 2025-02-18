package me.chriss99;

import me.chriss99.worldmanagement.iteration.IterateableWorld;
import org.joml.Vector2i;

public class ErosionManager {
    private final GPUTerrainEroder eroder;
    private final IterateableWorld data;

    private final Vector2i maxChunks;

    public ErosionManager(GPUTerrainEroder eroder, IterateableWorld data) {
        this.eroder = eroder;
        this.data = data;

        maxChunks = new Vector2i(eroder.getTextureLimit()).div(data.chunkSize);
    }

    public boolean findIterate(Vector2i pos, int maxSearch, int maxIteration) {
        Vector2i bestPos = null;
        Vector2i bestSize = new Vector2i();
        int bestIteration = Integer.MAX_VALUE;

        for (int x = 0; x < maxSearch; x = (x>=0) ? -x-1 : -x)
            for (int y = 0; y < maxSearch; y = (y>=0) ? -y-1 : -y) {
                Vector2i currentPos = new Vector2i(x, y).add(pos);
                Vector2i size = new Vector2i(maxChunks);
                int iteration = data.getTile(currentPos.x, currentPos.y).iteration;

                if (iteration > bestIteration || iteration > maxIteration)
                    continue;

                while (size.x >= 2) {
                    if ((iteration < bestIteration || size.x > bestSize.x) && iterable(currentPos, size)) {
                        bestPos = currentPos;
                        bestSize = size;
                        bestIteration = iteration;
                        break;
                    }
                    size.sub(1, 1);
                }
            }

        if (bestPos == null)
            return false;

        iterate(bestPos, bestSize);
        return true;
    }

    private boolean iterable(Vector2i pos, Vector2i size) {
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

        return isFlat(new Vector2i(pos).add(1, 1), new Vector2i(size).sub(2, 2));
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
            for (int x = 1; x < size.y; x++)
                data.getTile(pos.x + x, pos.y + size.y).iteration += data.chunkSize;
        if (b == 0)
            for (int x = 1; x < size.y; x++)
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

    private boolean isFlat(Vector2i pos, Vector2i size) {
        for (int x = 0; x < size.x; x++)
            for (int y = 0; y < size.y; y++)
                if (!data.getIterationSurfaceType(pos.x + x, pos.y + y).getSurfaceType().equals(IterationSurfaceType.SurfaceType.FLAT))
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
