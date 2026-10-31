package com.travel.payment;

import com.travel.enums.PaymentMode;
import com.travel.exception.PaymentFailedException;

/**
 * Demonstrates: Strategy specialization and method overriding for UPI.
 */
public class UpiPayment extends Payment {

    private static final long serialVersionUID = 1L;

    public static final double PROCESSING_FEE = 0.0;

    public UpiPayment() {
        super(PaymentMode.UPI);
    }

    @Override
    public boolean pay() throws PaymentFailedException {
        return completePayment(PaymentStatus.PAID);
    }

    @Override
    public double getProcessingFee(double amount) {
        return PROCESSING_FEE;
    }
}
