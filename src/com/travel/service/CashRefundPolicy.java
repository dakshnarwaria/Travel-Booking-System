package com.travel.service;

import com.travel.model.Booking;
import com.travel.payment.PaymentStatus;

/**
 * Demonstrates: Specialized refund strategy overriding the common refund contract.
 */
public class CashRefundPolicy implements RefundPolicy {

    @Override
    public int getRefundPercentage(Booking booking) {
        return booking != null
                && booking.getPayment() != null
                && booking.getPayment().getStatus() == PaymentStatus.PAY_AT_ARRIVAL
                ? 0
                : new TimeBasedRefundPolicy().getRefundPercentage(booking);
    }
}
