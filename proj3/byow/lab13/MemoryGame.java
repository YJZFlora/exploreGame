package byow.lab13;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

import static java.awt.Font.*;

public class MemoryGame {
    private int width;
    private int height;
    private int round;
    private Random rand;
    private boolean gameOver;
    private boolean playerTurn;
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        int seed = Integer.parseInt(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, int seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        //Initialize random number generator
        rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        // Generate random string of letters of length n
        String s = "";
        for (int i = 0; i < n; i++) {
            int r = rand.nextInt(CHARACTERS.length);
            s = CHARACTERS[r]  + s;
        }
        return s;
    }

    public void drawFrame(String s) {
        // Take the string and display it in the center of the screen
        // If game is not over, display relevant game information at the top of the screen
        StdDraw.clear(Color.BLACK);

        // draw the GUI
        if (!gameOver) {
            Font smallFont = new Font("Monaco", BOLD, 20);
            StdDraw.setFont(smallFont);
            String roundInfo = "Round: " + round;
            StdDraw.textLeft(2, height - 1, roundInfo);

            if (playerTurn) {
                StdDraw.text(width / 2, height - 1, "Typed!");
            } else {
                StdDraw.text(width / 2, height - 1, "Watch!");
            }
            StdDraw.textRight(width - 1, height - 1, ENCOURAGEMENT[round % ENCOURAGEMENT.length]);
            StdDraw.line(0, height - 2, width, height - 2);
        }

        Font f = new Font("Monaco", BOLD, 30);
        StdDraw.setFont(f);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(width / 2, height /2, s);
        StdDraw.show();
    }

    public void flashSequence(String letters) {
        // Display each character in letters, making sure to blank the screen between letters
        for (int i = 0; i < letters.length(); i++) {
            StdDraw.pause(200); // 0.5 second break between characters
            drawFrame(letters.substring(i, i + 1));
            StdDraw.pause(750); // 1 second visible
            StdDraw.clear(Color.BLACK);
        }
    }

    /*
    @ Hug solution spoiler
     */
    public String solicitNCharsInput(int n) {
        // Read n letters of player input
        StdDraw.clear(Color.BLACK);
        String input = "";
        drawFrame(input);
       while (input.length() < n) {
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }
           char key = StdDraw.nextKeyTyped();
            input = input + key;
           drawFrame(input);
        }
        StdDraw.pause(500);
        return input;
    }

    public void startGame() {
        // Set any relevant variables before the game starts
        round = 1;
        gameOver = false;
        playerTurn = false;
        while (!gameOver) {
            playerTurn = false;
            drawFrame("Round " + round);
            StdDraw.pause(700);

            String s = generateRandomString(round);
            flashSequence(s);

            playerTurn = true;
            String userInput = solicitNCharsInput(round);

            if (!userInput.equals(s)) {
                gameOver = true;
                drawFrame("Game Over! You made it to round: " + round);
            } else {
                drawFrame("Correct!");
                StdDraw.pause(1500);
                round += 1;
            }
        }


        // Establish Engine loop
    }

}
