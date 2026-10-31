package com.travel.payment;

import com.travel.enums.PaymentMode;
import com.travel.exception.PaymentFailedException;

/**
 * Demonstrates: Strategy specialization and method overriding for net banking.
 */
public class NetBankingPayment extends Payment {

    public static final double PROCESSING_FEE = 12.0;

    public NetBankingPayment() {
        super(PaymentMode.NET_BANKING);
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
