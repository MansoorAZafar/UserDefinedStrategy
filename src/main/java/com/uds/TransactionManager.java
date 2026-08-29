package com.uds;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import com.uds.strategies.IPaymentStrategy;
import com.uds.utilities.Utilities;

public class TransactionManager {
    private HashSet<String> knownStrategies;
    private StringBuilder knownStrategiesString;

    private IPaymentStrategy strategy;
    private final Scanner scanner;

    public TransactionManager(IPaymentStrategy defaultStrategy, Scanner scanner, final String strategyResourceFilename) {
        this.knownStrategies = Utilities.readFromFile(strategyResourceFilename);
        this.strategy = defaultStrategy;
        this.scanner = scanner;

        this.knownStrategiesString = new StringBuilder();
        for (final String item : this.knownStrategies) {
            this.knownStrategiesString.append(item + "\n");   
        }
    }

    public void switchStrategy() {
        final String message =  "\tSWITCH STRATEGY\n____________________\nExisting Strategies:\n" 
            + this.knownStrategiesString
            + "\n> ";

        String input = "";
        boolean invalidInput = true;
        while (invalidInput) {
            Utilities.clearConsole();
            System.out.print(message);

            input = this.scanner.nextLine();
            if (this.knownStrategies.contains(input)) {
                invalidInput = false;
            } else {
                System.out.println("Please choose one of the strategies or quit to leave\n");
                Utilities.waitToContinue(this.scanner);
            }
        }

        if ("quit".equals(input)) { return; }
        try {
            Class<?> chosenStrategy = Class.forName(input);
            System.out.println("Constructors: " + Arrays.toString(chosenStrategy.getDeclaredConstructors()));

            /** @TODO
             *  Take args from input, and the types
             *  Call constructor with those inputs and respective types
             */


            this.strategy = (IPaymentStrategy) chosenStrategy.getDeclaredConstructor(
                String.class, 
                int.class
            ).newInstance("1234567891234567", 123);
        } catch (ClassNotFoundException e) {
            System.out.println("The class " + input + " could not be found.");
        } catch (ClassCastException e) {
            System.out.println("The loaded class is not a subclass of IPaymentStrategy.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getStrategy() {
        return this.strategy.getClass().getSimpleName();
    }

    public void simulateTransaction() {
        System.out.println("\tSIMULATE TRANSACTION\n____________________\n");

        System.out.print("Enter an amount to be paid: \n> ");
        final double debt = Utilities.getSafeDoubleInput(scanner);

        if (debt != -1) {
            System.out.println("\nCalling pay() with amount: " + debt);
            strategy.pay(debt);

            System.out.println();
        }
    }
}
