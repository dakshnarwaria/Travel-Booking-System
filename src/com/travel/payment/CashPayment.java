package com.travel.payment;

import com.travel.enums.PaymentMode;
import com.travel.exception.PaymentFailedException;

/**
 * Demonstrates: Strategy specialization for deferred cash payment.
 */
public class CashPayment extends Payment {

    public static final double PROCESSING_FEE = 30.0;

    public CashPayment() {
        super(PaymentMode.CASH);
    }

    @Override
    public boolean pay() throws PaymentFailedException {
        return completePayment(PaymentStatus.PAY_AT_ARRIVAL);
    }

    @Override
    public double getProcessingFee(double amount) {
        return PROCESSING_FEE;
    }
}
