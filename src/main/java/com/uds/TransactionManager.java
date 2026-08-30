package com.uds;

import java.lang.reflect.InvocationTargetException;
import com.uds.strategies.IPaymentStrategy;
import java.lang.reflect.Constructor;
import com.uds.utilities.Utilities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class TransactionManager {
    private ArrayList<String> knownStrategies;
    private StringBuilder knownStrategiesString;

    private IPaymentStrategy strategy;
    private final Scanner scanner;

    public TransactionManager(IPaymentStrategy defaultStrategy, Scanner scanner, final String strategyResourceFilename) {
        this.knownStrategies = Utilities.readFromFile(strategyResourceFilename);
        this.strategy = defaultStrategy;
        this.scanner = scanner;

        this.knownStrategiesString = new StringBuilder();
        for (int index = 0; index < knownStrategies.size(); ++index) {
            final String message = "\t" + index + ": " + knownStrategies.get(index) + "\n";
            this.knownStrategiesString.append(message);   
        }
    }

    public void switchStrategy() {
        final String message =  "\tSWITCH STRATEGY\n____________________\nExisting Strategies:\n\n" 
            + this.knownStrategiesString
            + "\n> ";

        int chosenStrategyIndex = -1;
        do {
            Utilities.clearConsole();
            System.out.print(message);
        } while (
            (chosenStrategyIndex = Utilities.getSafeIntInput(
                this.scanner, 
                val -> val >= 0 && val < knownStrategies.size()
            )) == -1
        );

        if (chosenStrategyIndex == 0) { return; }
        String input = this.knownStrategies.get(chosenStrategyIndex);

        try {
            final Class<?> chosenStrategy = Class.forName(input);
            final Constructor<?>[] constructors = chosenStrategy.getDeclaredConstructors();
            
            StringBuilder stringBuilder = new StringBuilder("Choose a Constructor\n\n");
            for (int i = 0; i < constructors.length; ++i) {
                stringBuilder.append("\t" + i + ": " + constructors[i] + "\n");
            }

            stringBuilder.append("\n> ");
            int chosenConstructorIndex = -1;
            do {
                Utilities.clearConsole();
                System.out.print(stringBuilder);
            } while (
                (chosenConstructorIndex = Utilities.getSafeIntInput(
                    this.scanner, 
                    val -> val >= 0 && val < constructors.length
                )) == -1
            );

            final Constructor<?> chosenConstructor = constructors[chosenConstructorIndex];
            Object[] arguments = new Object[chosenConstructor.getParameterCount()];
            
            int argumentsIndex = 0;
            for (Class<?> parameterType : chosenConstructor.getParameterTypes()) {
                boolean canContinue = false;
                while (!canContinue) { 
                    try {
                        System.out.print("Enter a " + parameterType.getName() + " value\nOr enter quit to leave\n> ");
                        
                        input = this.scanner.nextLine();
                        if ("quit".equals(input)) {
                            canContinue = false;
                            return;
                        }
                        
                        // each arg needs to be parsable from a string, if not :(
                        arguments[argumentsIndex++] 
                            = parameterType.getConstructor(String.class).newInstance(input);
                        canContinue = true;
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid Input");   
                    } catch (InvocationTargetException e) {
                        System.out.println("!!! Bad Arugments for your constructor, See Stack trace below: !!!\n");
                        final Throwable rootCause = e.getCause(); 
            
                        System.out.println("Reflection wrapper caught: " + e);
                        System.out.println("The actual error is: " + rootCause);

                        if (rootCause != null) {
                            rootCause.printStackTrace(); 
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            this.strategy = (IPaymentStrategy) chosenConstructor.newInstance(arguments);
        } catch (ClassNotFoundException e) {
            System.out.println("The class " + input + " could not be found.");
        } catch (ClassCastException e) {
            System.out.println("The loaded class is not a subclass of IPaymentStrategy.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPayment() {
        final String message = """
        Add Strategy
        _____________________
        
        Must implement the IPaymentStrategy, 
        must implement a constructor (any) and 
        can potentially use custom types IF they implement a 
        string constructor.

        IPaymentStrategy Interface
        public interface IPaymentStrategy {
            public void pay(double amount);   
        }
        
        ___________________________________
        Keep writing the code line by line and write EOF
        on its own line when done. CODE IS NOT VALIDATED.

        Step 1: Enter the file name
        >\s """;

        String input = "";
        HashSet<String> takenStrategyNames = new HashSet<>(this.knownStrategies);

        do {
            System.out.print(message);
            input = this.scanner.nextLine();
        } while (takenStrategyNames.contains(input));
        if ("quit".equals(input)) return;

        StringBuilder fileContentBuilder = new StringBuilder();
        System.out.println("\nStep 2: File Content\n");
        while (!"EOF".equals(input)) {
            input = this.scanner.nextLine();
            fileContentBuilder.append(input + "\n");
        }
        
        System.out.println("\n\nFile CONTENT:\n" + fileContentBuilder);
    }

    public String getStrategy() {
        return this.strategy.getClass().getSimpleName();
    }

    public void simulateTransaction() {
        final String message = "SIMULATE TRANSACTION\n____________________\n\nEnter an amount to be paid: \n> ";
        
        double debt = -1.0;
        do {
            Utilities.clearConsole();
            System.out.print(message);
        } while ((debt = Utilities.getSafeDoubleInput(scanner)) == -1.0);

        if (debt != -1) {
            System.out.println("\nCalling pay() with amount: " + debt);
            strategy.pay(debt);

            System.out.println();
        }
    }
}
