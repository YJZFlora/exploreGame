package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/* fill x and y in TETile[][] world
    then render, draw the world
 */
public class MapGenerator {
    private long seed;
    private int width;
    private int height;
    private TERenderer ter;
    private Random rand;
    private int roomOrHall;
    private position playerP;
    private int upOrDown;

    // constructor
    public MapGenerator(long seed, int width, int height, TERenderer ter) {
        this.seed = seed;
        this.width = width;
        this.height = height;
        this.ter = ter;
        rand = new Random(seed);
        roomOrHall = 0;
        upOrDown = 0;
    }

    public static class position {
        public int x;
        public int y;

        position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public position getPlayerP() {
        return playerP;
    }

    /*
    step 1 choose random position in the world
    step 2  generate a room, return position 2 which in one side of the room
    step 3 in position 2, generate a hallway, return position 3 which is the end of the hallway
    step 4 back to step 2
     */
    public TETile[][] start(TETile[][] world) {

        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        int round = width;
        position vertualStart = new position(0, height - 1);
        position nextP = getRandomP(vertualStart, width - 1, height - 1);
        while (round > 0) {
            nextP = generator(nextP, world);
            round --;
        }

        world[playerP.x][playerP.y] = Tileset.AVATAR;
        return world;
    }

    private position generator(position p, TETile[][] world) {
        roomOrHall += 1;
        if (roomOrHall > 3) {
            roomOrHall = 0;
        }
        switch (roomOrHall) {
            case 0: return lSpaceHallway(p, world);
            case 1: return hHallway(p, world);
            case 2: return vHallway(p, world);
            case 3: return room(p, world);
            default: return new position(0, 0);
        }
    }


    private position room(position startP, TETile[][] world) {
        return rectangle(getRandomSideLength(2), getRandomSideLength(2), startP, world);
    }

    private position hHallway(position startP, TETile[][] world) {
        return rectangle(getRandomSideLength(2), 1, startP, world);
    }

    private position vHallway(position startP,  TETile[][] world) {
        return rectangle(1, getRandomSideLength(2), startP, world);
    }

    private position lSpaceHallway(position startP, TETile[][] world) {
        position p1 = hHallway(startP, world);
        return vHallway(p1, world);
    }

    private int getRandomSideLength(int bound) {
        int length = rand.nextInt(width / 7);
        while (length < bound) {
            length = rand.nextInt(width / 7);
        }
        return length;
    }
    /* generate rectangle filled with floor and closed with wall
    return the next position to draw next hall or room
    the next position must be inside the wall (ie, must come from the floor)
    h == 1, is horizontal hallway
    w == 1, is vertical hallway
    h > 1 and w > 1, is room
     */
    private position rectangle(int w, int h, position startP, TETile[][] world) {
        int wallWidth = w + 2;
        int wallHeight = h + 2;
        // the start to draw wall
        int drawX = startP.x - wallWidth / 2;
        int drawY = startP.y + wallHeight / 2;

        // touch out of canvas
        if (drawX < 0) {
            drawX = 0;
            upOrDown = 3;  // up right to develop
            if (drawY >  height / 2) {
                upOrDown = 0; // down right
            }
        }
        if (drawX + wallWidth >= width) {
            wallWidth = width - drawX;
            upOrDown = 2;  // up left to develop
            if (drawY >  height / 2) {
                upOrDown = 1; // down left
            }
        }
        if (drawY >= height ) {
            drawY = height - 1;
            upOrDown = 0;
            if (drawX > width / 2) {
                upOrDown = 1; // down left
            }
        }
        if (drawY - wallHeight < 0) {
            wallHeight = drawY + 1;
            upOrDown = 3;  // up right
            if (drawX > width / 2) {
                upOrDown = 2; // up left
            }
        }

        // draw wall
        drawWall(wallWidth, wallHeight, drawX, drawY, world);

        // draw floor and get final position fp
        position fp =  drawFloor(wallHeight - 2, wallWidth - 2, drawX + 1, drawY - 1, world);

        // in the floor
        playerP = new position(drawX + 1, drawY - 1);

        // return next position to start new rectangle
        // after the draw floor loop, fp is the position of one conner wall.
        switch (upOrDown) {
            default: return new position(fp.x + 1, fp.y - 1); // down right
            case 0: return new position(fp.x + 1, fp.y - 1); // down right
            case 1: return new position(drawX, fp.y - 1);    // down left
            case 2: return new position(drawX, drawY); // up left
            case 3: return new position(fp.x + 1, drawY);   // up right
        }
    }

    private void drawWall(int w, int h, int beginX, int beginY, TETile[][] world) {
        position wp;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                wp = new position(beginX + j, beginY - i);
                setWall(wp, world);
            }
        }
    }

    private position drawFloor(int w, int h, int beginX, int beginY,  TETile[][] world) {
        position fp = new position(beginX, beginY);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                fp = new position(beginX + j, beginY - i);
                setFloor(fp, world);
            }
        }
        return fp;
    }

    private position getRandomP(position start, int xRange, int yRange) {
        int xPlus = rand.nextInt(xRange) + start.x;
        int yPlus = start.y - rand.nextInt(yRange);
        return new position(xPlus, yPlus);
    }

    private void setWall(position p, TETile[][] world) {
        // when building wall, if the position[x][y] is floor, skip it
        if (!world[p.x][p.y].equals(Tileset.FLOOR)) {
            world[p.x][p.y] = Tileset.WALL;
        }
    }

    private void setFloor(position p, TETile[][] world) {
        world[p.x][p.y] = Tileset.FLOOR;
    }

}
