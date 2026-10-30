package com.travel.enums;

/**
 * Demonstrates: Enums with custom fields, Encapsulation, and Strategy Selection.
 * Represents different payment instruments and their fixed or percentage fee rules.
 */
public enum PaymentMode {
    CREDIT_CARD("Credit Card", 1.8, 0.0),
    DEBIT_CARD("Debit Card", 1.0, 0.0),
    UPI("UPI", 0.0, 0.0),
    NET_BANKING("Net Banking", 0.0, 12.0),
    CASH("Cash (Pay at Arrival)", 0.0, 30.0);

    private final String displayName;
    private final double percentageFee;
    private final double flatFee;

    PaymentMode(String displayName, double percentageFee, double flatFee) {
        this.displayName = displayName;
        this.percentageFee = percentageFee;
        this.flatFee = flatFee;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPercentageFee() {
        return percentageFee;
    }

    public double getFlatFee() {
        return flatFee;
    }

    /**
     * Calculates the exact processing fee for a given base amount.
     */
    public double calculateProcessingFee(double amount) {
        return (amount * (percentageFee / 100.0)) + flatFee;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
