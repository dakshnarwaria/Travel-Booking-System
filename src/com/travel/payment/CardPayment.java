package com.travel.payment;

import com.travel.enums.PaymentMode;

/**
 * Demonstrates: Intermediate abstraction and inheritance for card strategies.
 */
public abstract class CardPayment extends Payment {

    protected CardPayment(PaymentMode paymentMode) {
        super(paymentMode);
    }
}
