package com.travel.service;

import com.travel.model.Booking;

/**
 * Demonstrates: Polymorphic abstraction for booking refund calculations.
 */
public interface RefundPolicy {

    int getRefundPercentage(Booking booking);
}
