package com.travel.interfaces;

import com.travel.exception.PaymentFailedException;

/**
 * Demonstrates: Interface abstraction supporting the Strategy Pattern for payment processors.
 */
public interface Payable {
    /**
     * Executes the payment transaction for the specified amount.
     * @param amount the base or gross amount to pay
     * @return true if payment succeeded
     * @throws PaymentFailedException if payment validation fails or is cancelled
     */
    boolean pay(double amount) throws PaymentFailedException;

    /**
     * Calculates the payment processing surcharge based on instrument type and amount.
     * @param amount the gross amount
     * @return processing fee in rupees
     */
    double getProcessingFee(double amount);
}
