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

    public boolean findIterate(Vector2i pos, int max) {
        for (int x = 0; x < max; x = (x>=0) ? -x-1 : -x)
            for (int y = 0; y < max; y = (y>=0) ? -y-1 : -y)
                if (iterate(new Vector2i(x, y).add(pos)))
                    return true;
        return false;
    }

    public boolean iterate(Vector2i pos) {
        int x = pos.x;
        int y = pos.y;

        int l = data.getTile(x, y+1).horizontal;
        int r = data.getTile(x+1, y+1).horizontal;
        int f = data.getTile(x+1, y+1).vertical;
        int b = data.getTile(x+1, y).vertical;

        if (l == -1 || r == 1 || f == 1 || b == -1)
            return false;

        IterationSurfaceType.SurfaceType allowed = IterationSurfaceType.SurfaceType.FLAT;
        boolean tl = allowed.equals(data.getIterationSurfaceType(x, y+1).getSurfaceType());
        boolean tr = allowed.equals(data.getIterationSurfaceType(x+1, y+1).getSurfaceType());
        boolean dl = allowed.equals(data.getIterationSurfaceType(x, y).getSurfaceType());
        boolean dr = allowed.equals(data.getIterationSurfaceType(x+1, y).getSurfaceType());

        if (l == 0 && f == 0 && !tl || f == 0 && r == 0 && !tr || l == 0 && b == 0 && !dl || b == 0 && r == 0 && !dr)
            return false;


        eroder.changeArea(new Vector2i(x, y).mul(data.chunkSize), new Vector2i(data.chunkSize).mul(2));
        eroder.erosionSteps(data.chunkSize, l == 0, r == 0, f == 0, b == 0);

        l--;
        r++;
        f++;
        b--;

        data.getTile(x, y+1).horizontal = l;
        data.getTile(x+1, y+1).horizontal = r;
        data.getTile(x+1, y+1).vertical = f;
        data.getTile(x+1, y).vertical = b;

        return true;
    }
}
