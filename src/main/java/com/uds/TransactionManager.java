package com.uds;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.lang.reflect.Constructor;

import com.uds.api.IPaymentStrategy;
import com.uds.utilities.DynamicClassLoader;
import com.uds.utilities.Utilities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

public class TransactionManager {
    private ArrayList<String> knownStrategies;
    private StringBuilder knownStrategiesString;

    private DynamicClassLoader classLoader;
    private IPaymentStrategy strategy;
    private final Scanner scanner;

    public TransactionManager(IPaymentStrategy defaultStrategy, Scanner scanner, final String strategyResourceFilename) {
        this.knownStrategies = Utilities.loadDefaultStrategies(strategyResourceFilename);
        this.strategy = defaultStrategy;
        this.scanner = scanner;

        this.knownStrategiesString = new StringBuilder();
        for (int index = 0; index < knownStrategies.size(); ++index) {
            final String message = "\t" + index + ": " + knownStrategies.get(index) + "\n";
            this.knownStrategiesString.append(message);   
        }

        this.classLoader = new DynamicClassLoader(new URL[0], IPaymentStrategy.class.getClassLoader()); 
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
            final Class<?> chosenStrategy = this.classLoader.loadClass(input);
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
        DO NOTE ADD THE PACKAGE, THAT IS AUTO ADDED!

        Step 1: Enter the file name
        >\s """;

        String input = "\n";
        HashSet<String> takenStrategyNames = new HashSet<>(this.knownStrategies);

        do {
            System.out.print(message);
            input = this.scanner.nextLine();
        } while (takenStrategyNames.contains(input));
        if ("quit".equals(input)) return;

        final String fileName = input;
        StringBuilder fileContentBuilder = new StringBuilder("package com.uds.strategies;\n\nimport com.uds.api.IPaymentStrategy;\n\n");
        
        System.out.println("\nStep 2: File Content\n");
        input = this.scanner.nextLine();
        
        while (!"EOF".equals(input)) {
            fileContentBuilder.append(input + "\n");
            input = this.scanner.nextLine();
        }

        /**
         * @TODO
         * Create a file in strategies
         * add it into resources.txt
         * Add it into current program
         * 
         * Load it into memory, 
         * write it to a file 
         * but for this runtime use the loaded memory one
         * - delete it from runtime memory before closing program
         */

        // Dedicated Runtime Dir
        // Java has lots of system properties (info about env which JVM is running)
        // Basically, make a temp dir called uds_codegen within the tmp file the app uses
        final Path tempRoot = Paths.get(
            System.getProperty("java.io.tmpdir"), // where OS expect app to put temp files
            "uds_codegen"
        );

        // we mimic the package strucutre for the tmp codegen files
        // so we'll have /tmp/uds_codegen/com/uds/strategies
        final Path packageDir = tempRoot.resolve(
            Paths.get("com", "uds", "strategies")
        );

        // actually create the package dir
        try {
            Files.createDirectories(packageDir);
        } catch (IOException e) {
            System.err.println("Could not create dynamic directories: " + e.getMessage());
            return;
        }

        // create the actual .java file (i.e. if inputted filename is PayPal, we make PayPal.java)
        // we make it in that tmp codegen package dir
        // Temp/uds_codegen\com/uds/strategies/PayPal.java
        final Path sourceFilePath = packageDir.resolve(fileName + ".java");
        try {
            // write the actual code content into that file
            Files.writeString(sourceFilePath, fileContentBuilder.toString());
        } catch (IOException e) {
            System.err.println("Failed to write source file.");
            return;
        }

        // ^ Writing to disk not compiled

        // -classpath means where to look for classes
        // because our IPaymentStrategy is in our app, we pass the current path the JVM started with
        // System.getProperty("java.class.path") - classPath used by JVM
        // -d means where should the compiler PUT the generated .class file(s)
        // aka Put the resulting .class files underneath uds_codegen, respecting their package structure
        // even though the files we create dynamically are in tmp/uds_codegen because we linked our 
        // current app stuff, we can still access the IPaymentStrategy. like dll's or static libs for simple example
        final List<String> options = List.of(
            "-classpath",
            System.getProperty("java.class.path"),
            "-d",
            tempRoot.toString()
        );

        // javac but through code
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.out.println("A JDK is required to dynamically compile Java code.");
            return;
        }

        // StandardJavaFileManager helps compiler deal w files and locations
        try (StandardJavaFileManager fileManager =
            compiler.getStandardFileManager(null, null, null)) {
            
            // Takes the source file's path and makes a compiler representation of it
            // aka, here's WHERE the java source files to be compiled are 
            Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjects(sourceFilePath.toFile());

            JavaCompiler.CompilationTask task =
                compiler.getTask(
                    null,
                    fileManager,
                    diagnostic -> {
                        System.err.println(diagnostic);
                    },
                    options,
                    null,
                    compilationUnits
                );

            final boolean success = task.call();
            if (!success) {
                System.err.println("Compilation failed.");
                return;
            }

            System.out.println("Compilation successful!");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        final String completePathFileName = "com.uds.strategies." + fileName;
        try {
            this.classLoader.addJarURL(tempRoot.toUri().toURL());

            Class<?> clazz = this.classLoader.loadClass(completePathFileName);
            if (!IPaymentStrategy.class.isAssignableFrom(clazz)) {
                System.err.println(
                    "The class does not implement IPaymentStrategy."
                );
                return;
            }
        } catch (Exception e) {
            System.err.println("Failed to load dynamic strategy.");
            e.printStackTrace();

            return;
        }

        this.knownStrategies.add(completePathFileName);
        this.knownStrategiesString.append("\t" + (this.knownStrategies.size() - 1) + ": " + completePathFileName + "\n");
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

    public void cleanup() {
        try {
            this.classLoader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
