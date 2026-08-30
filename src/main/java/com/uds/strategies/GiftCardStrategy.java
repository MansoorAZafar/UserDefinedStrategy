package com.uds.strategies;

import java.security.SecureRandom;
public class GiftCardStrategy implements IPaymentStrategy {
    private final String id;
    private final int maxIdLength = 16; 
    private static final SecureRandom random = new SecureRandom();
    private static final String CHARACTERS = "ABCDEFHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890@";

    public GiftCardStrategy(final String id) {
        if (id.length() > 16) { 
            throw new IllegalArgumentException("Cannot be > 16 digits");
        }
        
        this.id = id;
    }

    public GiftCardStrategy() {
        StringBuilder randomStringBuilder = new StringBuilder();
        for (int i = 0; i < this.maxIdLength; ++i) {
            final int randomIndex = random.nextInt(CHARACTERS.length());
            randomStringBuilder.append(CHARACTERS.charAt(randomIndex));
        }

        this.id = randomStringBuilder.toString();
    }

    public void pay(double amount) {
        System.out.println("Paying $" + amount + " with GiftCard: " + this.id);
    }
}
