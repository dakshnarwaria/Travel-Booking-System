package com.travel.payment;

import com.travel.enums.PaymentMode;
import com.travel.exception.PaymentFailedException;

/**
 * Demonstrates: Strategy specialization and method overriding for credit cards.
 */
public class CreditCardPayment extends CardPayment {

    private static final long serialVersionUID = 1L;

    public static final double PROCESSING_FEE_RATE = 0.018;

    public CreditCardPayment() {
        super(PaymentMode.CREDIT_CARD);
    }

    @Override
    public boolean pay() throws PaymentFailedException {
        return completePayment(PaymentStatus.PAID);
    }

    @Override
    public double getProcessingFee(double amount) {
        return Math.round(amount * PROCESSING_FEE_RATE);
    }
}
