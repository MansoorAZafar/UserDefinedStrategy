package com.uds;
import com.uds.enums.MenuOption;
import com.uds.strategies.*;
import com.uds.utilities.*;

import java.util.Scanner;

public class App  {
    private static final String StrategyResourceFilename = "strategies.txt";
    public static void main( String[] args ) {
        final IPaymentStrategy DEFAULT_STRATEGY = new CreditCardStrategy("1234567898765432", 435);
        Scanner scanner = new Scanner(System.in);
        
        TransactionManager manager = new TransactionManager(DEFAULT_STRATEGY, scanner, App.StrategyResourceFilename);
        final String message = String.format(
            """
            Welcome to UDS
            ________________
            Simulate a transaction using a Payment Strategy.
            The order of calls goes:
            
            --> enter amount
            --> payment strategy calls pay()
            
            Current Strategy: [ %s ]
            ____________________

            1. Switch existing Banking Strategy
            2. Make a custom Banking Strategy
            3. Simulate Transaction
            0. Quit

            >\s""", 
            manager.getStrategy()
        );

        boolean isRunning = true;
        while(isRunning) {
            Utilities.clearConsole();
            System.out.print(message);
            
            final int rawSelection = Utilities.getSafeIntInput(scanner, val -> val >= 0 && val <= 3);
            final MenuOption selection = MenuOption.fromInt(rawSelection);
            System.out.println();
            
            if (selection != MenuOption.EMPTY && selection != MenuOption.QUIT) { 
                Utilities.clearConsole();
            }
            
            switch(selection) {                
                case MenuOption.SWITCH_STRATEGY -> {
                    manager.switchStrategy();
                }

                case MenuOption.CREATE_STRATEGY -> {
                    System.out.println("CREATE STRATEGY");
                }

                case MenuOption.SIMULATE_TRANSACTION -> {
                    manager.simulateTransaction();
                }

                case MenuOption.QUIT -> { 
                    System.out.println("... Quitting ...");
                    isRunning = false; 
                }
                
                default -> { }
            }

            Utilities.waitToContinue(scanner);
        }

        scanner.close();
        System.out.println("... Goodbye ...");
    }
}
