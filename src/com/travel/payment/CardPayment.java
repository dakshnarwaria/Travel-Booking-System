package com.travel.payment;

import com.travel.enums.PaymentMode;

/**
 * Demonstrates: Intermediate abstraction and inheritance for card strategies.
 */
public abstract class CardPayment extends Payment {

    private static final long serialVersionUID = 1L;

    protected CardPayment(PaymentMode paymentMode) {
        super(paymentMode);
    }
}
