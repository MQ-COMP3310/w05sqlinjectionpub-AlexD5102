package workshop05code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {
    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            System.out.println("Wordle created and connected.");
        } else {
            System.out.println("Not able to connect. Sorry!");
            return;
        }
        if (wordleDatabaseConnection.createWordleTables()) {
            System.out.println("Wordle structures in place.");
        } else {
            System.out.println("Not able to launch. Sorry!");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (line.matches("^[a-z]{4}$")) {
                    wordleDatabaseConnection.addValidWord(i, line);
                    logger.info("Valid word added from file: " + line);
                    i++;
                } else {
                    logger.severe("Invalid word in data.txt: " + line);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading data.txt", e);
            System.out.println("Failed to load words. Please try again later.");
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a 4 letter word for a guess or q to quit: ");
            String guess = scanner.nextLine();

            while (!guess.equals("q")) {
                if (!guess.matches("^[a-z]{4}$")) {
                    logger.warning("Invalid user guess: " + guess);
                    System.out.println("Invalid input. Please enter a 4-letter word with lowercase letters a-z.");
                } else {
                    System.out.println("You've guessed '" + guess + "'.");
                    if (wordleDatabaseConnection.isValidWord(guess)) {
                        System.out.println("Success! It is in the the list.\n");
                    } else {
                        System.out.println("Sorry. This word is NOT in the list.\n");
                    }
                }

                System.out.print("Enter a 4 letter word for a guess or q to quit: ");
                guess = scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.SEVERE, "Exception during user input", e);
            System.out.println("An error occurred while reading input.");
        }
    }
}
