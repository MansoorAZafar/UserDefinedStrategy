package com.uds.strategies;

public class CreditCardStrategy implements IPaymentStrategy {
    private String cardNumber;
    private int cardVerificationCode;

    public CreditCardStrategy(String cardNumber, Integer cardVerificationCode) {
        if(cardNumber.length() != 16 || (cardVerificationCode < 100 || cardVerificationCode > 999) ) {
            throw new IllegalArgumentException("cardNumber must be 16 digits and cvc must be between 100-999");
        } 

        this.cardNumber = cardNumber;
        this.cardVerificationCode = cardVerificationCode;   
    }

    @Override
    public void pay(double amount) {
        System.out.println("$" 
            + amount 
            + " Paid using Credit Card [ "
            + this.cardNumber
            + " ] with cvc: [ "
            + this.cardVerificationCode
            + " ]" 
        );
    }
}
