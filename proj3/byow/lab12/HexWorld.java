package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final long SEED = 1000;
    private static final Random RANDOM = new Random(SEED);


    // adds a hexagon of side length s to a given position in the world
    public static void addHexagon(int s, int x, int y, TETile[][] world, TETile t) {
        if (s < 2) {
            throw new IllegalArgumentException("Hexagon must be at least size 2.");
        }
        drawUpper(s, x, y, world, t);
        drawDown(s, x, y, world, t);
    }

    private static void drawUpper(int s, int x, int y, TETile[][] world, TETile t) {
        for(int i = 0; i < s; i++) {
            int startX = x - i;
            int numToDraw = s + 2 * i;
            for (int j = 0; j < numToDraw; j++) {
                world[startX + j][y + i] = t;
            }
        }
    }

    private static void drawDown(int s, int x, int y, TETile[][] world, TETile t) {
        for(int i = s - 1; i >= 0; i--) {
            int startX = x - i;
            int numToDraw = s + 2 * i;
            for (int j = 0; j < numToDraw; j++) {
                int positionY = y + 2 * s - 1 - i;
                world[startX + j][positionY] = t;
            }
        }
    }

    // draw a settlers of catan with random biome
    public static void drawWorld(int s, TETile[][] world) {
        // draw the 3, 4, 5 column from left to right, from bottom to top
        for (int i = 0; i < 3; i++) {
            int startX = getStartX(i, s);
            int startY = getStartY(i,  s);
            drawColumn(3 + i, startX, startY, s, world);
        }
        // draw the remaining 4, 3 column
        for (int i = 0; i < 2; i++) {
            int sX = getStartX(3 + i, s);
            int sY = getStartY(1 - i, s);
            drawColumn(4 - i, sX, sY, s, world);
        }

    }

    private static int getStartX(int i, int s) {
        int gap = 2 * s - 1;
        return s - 1 + i * gap;
    }

    private static int getStartY(int i, int s) {
        return s * (4 - i) - 1;
    }

    private static void drawColumn(int num, int startX, int startY, int s, TETile[][] world) {
        for (int i = 0; i < num; i++) {
            int x = startX;
            int y = startY + i * 2 * s;
            TETile t = randomTile();
            addHexagon(s, x, y, world, t);
        }
    }

    /** Picks a RANDOM tile with a 33% change of being
     *  a wall, 33% chance of being a flower, and 33%
     *  chance of being empty space.
     */
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(6);
        switch (tileNum) {
            case 0: return Tileset.GRASS;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.SAND;
            case 3: return Tileset.TREE;
            case 4: return Tileset.WALL;
            case 5: return Tileset.AVATAR;
            default: return Tileset.NOTHING;
        }
    }

    public static void main(String[] args){
        TERenderer ter = new TERenderer();
        ter.initialize(60, 50);

        // initialize tiles
        TETile[][] world = new TETile[60][50];
        TETile t = Tileset.FLOWER;
        for (int x = 0; x < 60; x += 1) {
            for (int y = 0; y < 50; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        drawWorld(3, world);

        // draws the world to the screen
        ter.renderFrame(world);
    }
}
