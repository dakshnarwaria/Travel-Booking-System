package com.travel.payment;

import com.travel.enums.PaymentMode;

/**
 * Demonstrates: Factory pattern for selecting a payment strategy by enum.
 */
public final class PaymentFactory {

    private PaymentFactory() {
        throw new UnsupportedOperationException("Factory cannot be instantiated.");
    }

    public static Payment createPayment(PaymentMode paymentMode) {
        if (paymentMode == null) {
            throw new IllegalArgumentException("Payment mode cannot be null.");
        }

        switch (paymentMode) {
            case CREDIT_CARD:
                return new CreditCardPayment();
            case DEBIT_CARD:
                return new DebitCardPayment();
            case UPI:
                return new UpiPayment();
            case NET_BANKING:
                return new NetBankingPayment();
            case CASH:
                return new CashPayment();
            default:
                throw new IllegalArgumentException("Unsupported payment mode: " + paymentMode);
        }
    }
}
