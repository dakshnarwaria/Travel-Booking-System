package com.travel.interfaces;

/**
 * Demonstrates: Interface abstraction and Interface Default Methods (Java 8+).
 * Enables polymorphic fare computation across all transport modalities.
 */
public interface Priceable {
    /**
     * Calculates the calculated fare for the trip/service.
     * @return base calculated fare amount
     */
    double calculateFare();

    /**
     * Demonstrates: Default method in interface.
     * Computes the total amount after applying a given Goods and Services Tax (GST) rate.
     * @param baseAmount the base price before tax
     * @param gstPercentage tax rate percentage (e.g., 5.0 for 5%)
     * @return total price including GST
     */
    default double applyGst(double baseAmount, double gstPercentage) {
        if (baseAmount < 0 || gstPercentage < 0) {
            return baseAmount;
        }
        return baseAmount + (baseAmount * (gstPercentage / 100.0));
    }
}
