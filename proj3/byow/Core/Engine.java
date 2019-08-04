package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.Core.MapGenerator.position;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.util.Map;

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

        if (newOrLoad == "new") {
            theSeed = seedUI();
            beginPosition = GameUI(theSeed, "new");
        } else if (newOrLoad == "load") {
            theSeed = savedSeed;
            beginPosition = savedPlayerPosition;
            GameUI(savedSeed, "load");
        }
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
        drawFrame("Please enter seed, end with 's' :");
        String seedFromKeyboard = "";
        boolean toContinue = true;
        long theSeed = 1456780;
        while (toContinue) {
            if (!StdDraw.hasNextKeyTyped())  continue;

            char key = StdDraw.nextKeyTyped();
            if (key != 's' && key != 'S') {
                seedFromKeyboard += String.valueOf(key);
                drawFrame("Please enter seed, end with 's' :" + seedFromKeyboard);
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
        StdDraw.setPenColor(Color.white);
        Font titleFont = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(titleFont);
        StdDraw.text(midWidth, highPlace, "EXPLORE GAME");

        Font smallFont = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(smallFont);
        StdDraw.text(midWidth, midHeight, "New Game (N)");
        StdDraw.text(midWidth, midHeight - 2, "Load Game (L)");
        StdDraw.text(midWidth, midHeight - 4, "Quit (Q)");
        StdDraw.show();

        return getFirstCommand();
    }

    private void saveGame(gameInfo gi) {
        savedPlayerPosition = gi.playerP;
        savedSeed = gi.savedSeed;
    }

    private String getFirstCommand() {
        boolean validInput = false;
        char key = 'a';
        while (!validInput) {
            if (!StdDraw.hasNextKeyTyped()) continue;

            key = StdDraw.nextKeyTyped();
            if (key == 'n' || key == 'l' || key == 'q') {
                validInput = true;
            }
        }
        return String.valueOf(key).toLowerCase();
    }

    private void drawFrame(String s) {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;
        StdDraw.clear(Color.black);
        StdDraw.setPenColor(Color.white);
        Font smallFont = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(smallFont);
        StdDraw.text(midWidth, midHeight, s);
        StdDraw.show();
    }


    /**
     * Recall that strings ending in ":q" should cause the game to quite save.
     * both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     *   assume that every replay string starts with either “N#S” or “L”, where # represents the user entered seed.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {

        String s = input.toLowerCase();
        position playerPosition;
        if (savedPlayerPosition != null) {
            playerPosition = loadGame(world);
        } else {
            playerPosition = new position(0,0);
        }

        long seed = savedSeed;

        for (int i = 0; i < s.length(); i++) {
            // begin a new game with "N#S" seed
            if (beginSeed(i, s)) {
                seed = getSeed(i, s);
                MapGenerator map = generateMap(world, seed);
                playerPosition = map.getPlayerP();
                i = skipSeed(i, s);
                continue;
            }
            // time to load game or save game or move
            if (needLoadGame(i, s)) playerPosition = loadGame(world);
            else if (needSaveAndQuit(i, s)) {
                gameInfo gi = new gameInfo(playerPosition, seed, true);
                saveGame(gi);
                quit();
                i += 1;
            } else playerPosition = move(s.charAt(i), world, playerPosition);
        }
        return world;
    }

   private MapGenerator generateMap(TETile[][] theWorld, long seed) {
        MapGenerator map = new MapGenerator(seed, 90, HEIGHT, ter);
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

   private position loadGame(TETile[][] world) {
        if (savedPlayerPosition == null) {
            quit();
            return null;
        }
        MapGenerator map = new MapGenerator(savedSeed, WIDTH, HEIGHT, ter);
        map.start(world);
        sendPlayerto(map.getPlayerP(), savedPlayerPosition, world);
        return savedPlayerPosition;
   }

   private int skipSeed(int i, String s) {
       while (s.charAt(i) != 's') {
           i += 1;
       }
       return i; // pointing at 's'
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

    private boolean needSaveAndQuit (int i, String s){
        return s.charAt(i) == ':' && s.charAt(i + 1) == 'q';
    }

    private boolean needLoadGame(int i, String s) {
        return s.charAt(i) == 'l';
    }

    private void sendPlayerto (position start, position destination, TETile[][] world) {
       setTile(world, start, Tileset.FLOOR);
       setTile(world, destination, Tileset.AVATAR);
    }

}
