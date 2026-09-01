package com.uds.utilities;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import com.uds.App;

import java.util.function.Function;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.io.InputStream;
import java.util.Scanner;
import java.util.List;

public class Utilities {
    public static void clearConsole() {
        System.out.print("\033[H\033[2J");
    }

    public static void waitToContinue(Scanner scanner) {
        System.out.println("Enter anything to continue");
        scanner.nextLine();
    }

    public static <T> T getSafeTypeInput(Scanner scanner, Function<String, T> parser, Function<T, Boolean> validator, T nullValue) {
        String input = scanner.nextLine();
        try {
            final T selection = parser.apply(input); 
            if (!validator.apply(selection)) {
                throw new IllegalArgumentException("Invalid Input");
            }
            
            return selection;
        } catch (IllegalArgumentException e) {
            System.out.println("\nInvalid Input");
        } catch (Exception e) {
            System.out.println("\nInvalid Input: " + e.getMessage());
        }

        return nullValue;
    }

    public static int getSafeIntInput(Scanner scanner, Function<Integer, Boolean> filter) {
        // String input = scanner.nextLine();
        // try {
        //     final int selection = Integer.parseInt(input);
        //     if(selection < 0 || selection > 3) throw new IllegalArgumentException("Selection must be between 0-3");
        
        //     return selection;
        // } catch (NumberFormatException e) {
        //     System.out.println("Invalid Input. Please enter a whole number without letters or decimals");
        // } catch (IllegalArgumentException e) {
        //     System.out.println("Please enter a whole number between 0-3");
        // }

        // return -1;
        return getSafeTypeInput(
            scanner, 
            input -> Integer.parseInt(input), 
            filter, 
            -1
        );
    }

    public static int getSafeIntInput(Scanner scanner, Function<Integer, Boolean> filter, int nullValue) {
        return getSafeTypeInput(
            scanner, 
            input -> Integer.parseInt(input), 
            filter, 
            nullValue
        );
    }

    public static double getSafeDoubleInput(Scanner scanner) {
        return getSafeTypeInput(
            scanner, 
            input -> Double.parseDouble(input), 
            val -> val >= 0, 
            -1.0
        );
    }

    // Loads default strategies if the strategies.txt doesn't exist
    // if it does, this function reads them and returns it
    // otherwise, it just creates the externel one and copies it there
    public static ArrayList<String> loadDefaultStrategies(final String filename) {
        final Path CONFIG_PATH = Paths.get(App.StrategyResourceFilename);
        try {
            if (!Files.exists(CONFIG_PATH)) {
                try (InputStream inputStream = Utilities.class.getClassLoader().getResourceAsStream(filename)) {
                    if (inputStream == null) { throw new IllegalArgumentException("Resource file not found: " + filename); }
                    Files.copy(inputStream, CONFIG_PATH);
                }
            }

            try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                return reader.lines().collect(Collectors.toCollection(ArrayList::new));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // load the credit card strategy by default into the set
        return new ArrayList<String>(List.of("quit", "com.uds.strategies.CreditCardStrategy", "com.uds.strategies.GiftCardStrategy"));
    }

    public void writeToStrategiesFile(final String content) {
        final Path strategiesPath = Paths.get(App.StrategyResourceFilename);
        try (BufferedWriter writer = Files.newBufferedWriter(strategiesPath, StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
