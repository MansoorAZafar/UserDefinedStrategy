package com.uds.utilities;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            System.out.println("Invalid Input");
        } catch (Exception e) {
            System.out.println("Invalid Input: " + e.getMessage());
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

    public static HashSet<String> readFromFile(final String filename) {
        try (InputStream inputStream = Utilities.class.getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) { throw new IllegalArgumentException("Resource file not found: " + filename); }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.toCollection(HashSet::new));
            }
        } catch (final Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // load the credit card strategy by default into the set
        return new HashSet<String>(Set.of("quit", "com.uds.strategies.CreditCardStrategy"));
    }
}
