package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.Core.MapGenerator.position;
import edu.princeton.cs.introcs.StdDraw;
import java.awt.*;

public class Engine {
    TERenderer ter = new TERenderer();

    public static final int WIDTH = 90;
    public static final int HEIGHT = 49;
    private long savedSeed;
    private position savedPlayerPosition;
    TETile[][] world = new TETile[WIDTH][HEIGHT];

    class gameInfo {
        position playerP;
        long savedSeed;
        boolean savedGame;

        gameInfo(position playerPosition, long seed, boolean savedGame) {
            this.playerP = playerPosition;
            this.savedSeed = seed;
            this.savedGame = savedGame;
        }
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        StdDraw.setCanvasSize(this.WIDTH * 16, this.HEIGHT * 16);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);

        String firstCommand = menuUI();

        boolean saveGame = false;
        boolean quit = false;

        // open game UI
        while (!quit) {
            gameInfo savedInfo = new gameInfo(savedPlayerPosition, savedSeed, false);
            if (beginSeed(0, firstCommand)) {
                savedInfo =  beginGameWithInput("new");
            } else if (needLoadGame(0, firstCommand)) {
                savedInfo =  beginGameWithInput("load");
            } else if (firstCommand.charAt(0) == 'q') {
                quit();
            }
            saveGame(savedInfo);
            firstCommand = menuUI();
        }

    }

    private gameInfo beginGameWithInput(String newOrLoad) {
        long theSeed = 0;
        position beginPosition = new position(0,0);

        if (newOrLoad.equals("new")) {
            theSeed = seedUI();
            beginPosition = GameUI(theSeed, "new");
        } else if (newOrLoad.equals("load")) {
            theSeed = savedSeed;
            beginPosition = savedPlayerPosition;
            GameUI(savedSeed, "load");
        }

        // interacting
        position playerPosition = beginPosition;
        boolean saveGame = false;
        char c1 = 'a';
        while (!saveGame) {
            if (!StdDraw.hasNextKeyTyped()) continue;

            char key = StdDraw.nextKeyTyped();
            if (c1 == ':' && key == 'q') {
                saveGame = true;
                break;
            }
            c1 = key;
            playerPosition = move(key, world, playerPosition);
            ter.renderFrame(world);
        }
        return new gameInfo(playerPosition, theSeed, saveGame);
    }

    private position GameUI(long theSeed, String loadOrNew) {
        MapGenerator map = generateMap(world, theSeed);
        ter.initialize(WIDTH, HEIGHT);
        position beginP = map.getPlayerP();
        if (loadOrNew == "load") {
            sendPlayerto(beginP, savedPlayerPosition, world);
            beginP =  savedPlayerPosition;
        }
        ter.renderFrame(world);
        return beginP;
    }

    private long seedUI() {
        StdDraw.clear(Color.black);
        drawFrame("Please enter seed, end with 's' :", WIDTH / 2,  HEIGHT / 2, 20);
        StdDraw.show();

        String seedFromKeyboard = "";
        boolean toContinue = true;
        long theSeed = 1456780;
        while (toContinue) {
            if (!StdDraw.hasNextKeyTyped())  continue;

            char key = StdDraw.nextKeyTyped();
            if (key != 's' && key != 'S') {
                seedFromKeyboard += String.valueOf(key);
                StdDraw.clear(Color.black);
                String toShow = "Please enter seed, end with 's' :" + seedFromKeyboard;
                drawFrame(toShow, WIDTH / 2,  HEIGHT / 2, 20);
                StdDraw.show();

            } else {
                toContinue = false;
                theSeed = getSeed(0, "n" + seedFromKeyboard + "s");
            }
        }
        return theSeed;
    }

    private String menuUI() {
        int midWidth = WIDTH / 2;
        int highPlace = HEIGHT * 5/8;
        int midHeight = HEIGHT / 2;
        StdDraw.clear(Color.black);

        drawFrame("EXPLORE GAME", midWidth, highPlace, 40);
        drawFrame("New Game (N)", midWidth, midHeight, 20);
        drawFrame("Load Game (L)", midWidth, midHeight - 2, 20);
        drawFrame("Quit (Q)", midWidth, midHeight - 4, 20);

        StdDraw.show();

        return getFirstCommand();
    }
    private void drawFrame(String s, int x, int y, int fontSize) {

        StdDraw.setPenColor(Color.white);
        Font smallFont = new Font("Monaco", Font.BOLD, fontSize);
        StdDraw.setFont(smallFont);
        StdDraw.text(x, y, s);
    }

    private void saveGame(gameInfo gi) {
        savedPlayerPosition = gi.playerP;
        savedSeed = gi.savedSeed;
    }

    private String getFirstCommand() {
        boolean validInput = false;
        char key = 'a';
        String r = "a";
        while (!validInput) {
            if (!StdDraw.hasNextKeyTyped()) continue;

            key = StdDraw.nextKeyTyped();
            r = String.valueOf(key).toLowerCase();
            if (r.equals("n") || r.equals("l") || r.equals("q")) {
                validInput = true;
            }
        }
        return r;
    }

   private MapGenerator generateMap(TETile[][] theWorld, long seed) {
        MapGenerator map = new MapGenerator(seed, WIDTH, HEIGHT, ter);
        map.start(theWorld);
        return map;
   }

   private long getSeed(int i, String s) {
       i += 1; // skip 'n'
       long seed = 0;
       boolean isNegative = false;
       while (s.charAt(i) != 's') {
           if(s.charAt(i) == '-') {
               isNegative = true;
               i += 1;
               continue;
           }
           seed = seed * 10 + s.charAt(i) - '0';
           i += 1;
       }
       if (isNegative) seed *= -1;
       return seed;
   }

   /* in the world, move one step
    * a, s, d, w: left, down, right, up
    */
   private position move(char c, TETile[][] world, position playerPosition) {
       setTile(world, playerPosition, Tileset.FLOOR);
        int x = playerPosition.x;
        int y = playerPosition.y;
       if (c == 'a' && canMove(world, x - 1, y))  x -= 1;
       else if (c == 's' && canMove(world, x, y - 1)) y -= 1;
       else if (c == 'w' && canMove(world, x, y + 1)) y += 1;
       else if (c == 'd' && canMove(world, x + 1, y)) x += 1;

       position p = new position(x, y);
       setTile(world, p, Tileset.AVATAR);
       return p;
   }

   private boolean canMove(TETile[][] world, int x, int y) {
        return world[x][y].equals(Tileset.FLOOR);
   }

    private void setTile(TETile[][] world, position p, TETile t) {
        world[p.x][p.y] = t;
    }

    private void quit() {
       System.exit(0);
    }

    private boolean beginSeed(int i, String s) {
        return s.charAt(i) == 'n';
    }

    private boolean needLoadGame(int i, String s) {
        return s.charAt(i) == 'l';
    }

    private void sendPlayerto (position start, position destination, TETile[][] world) {
       setTile(world, start, Tileset.FLOOR);
       setTile(world, destination, Tileset.AVATAR);
    }

}
