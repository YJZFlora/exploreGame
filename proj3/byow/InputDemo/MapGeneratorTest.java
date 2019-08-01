package byow.InputDemo;

import byow.Core.Engine;
import byow.Core.MapGenerator;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import org.junit.Test;

public class MapGeneratorTest {

    public static void main(String[] args) {
       /*
        TERenderer ter = new TERenderer();
        int width = 90;
        int height = 49;
        ter.initialize(width, height);
        */

        Engine e = new Engine();
        TETile[][] a = new TETile[3][3];
        changeA(a);

        TETile[][] world =  e.interactWithInputString("N46Sass:ql:qlas");
        world = e.interactWithInputString("lsdwwN123Sad:q");
        /*
        // draws the world to the screen
        ter.renderFrame(world);

         */

    }

    private static void changeA(TETile[][] a) {
        TETile[][] b = new TETile[4][2];
        a = b;
    }

    @Test
    public void interactWithInputStringTest() {
        TERenderer ter = new TERenderer();

        ter.initialize(30, 20);
        MapGenerator mg = new MapGenerator(1000, 30, 20, ter);

    }

}
