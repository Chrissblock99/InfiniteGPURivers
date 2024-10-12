package me.chriss99;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageWriter {
    public static void main(String[] args) {
        writeImageHeightMap(new Float2DBufferWrapper(new float[][]{{1, 0.5f}, {0, .75f}}), "test", true);
    }

    public static void writeImageHeightMap(Float2DBufferWrapper heightMap, String filename, boolean negative) {
        BufferedImage image = new BufferedImage(heightMap.width, heightMap.height, BufferedImage.TYPE_INT_RGB);

        for(int i = 0; i < heightMap.width; i++)
            for(int j = 0; j < heightMap.height; j++) {
                float heightValue = heightMap.getFloat(i, j)/250 + ((negative) ? .5f : 0);
                Color myRGB = new Color(heightValue, heightValue, heightValue);
                int rgb = myRGB.getRGB();
                image.setRGB(i, j, rgb);
            }

        try {
            ImageIO.write(image, "png", new File("images/" + filename + ".png"));
        } catch (IOException e) {
            System.out.println("Couldn't save image!");
        }
    }
}
