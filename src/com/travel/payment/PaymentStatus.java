package com.travel.payment;

/**
 * Demonstrates: Type-safe state modeling for polymorphic payment strategies.
 */
public enum PaymentStatus {
    PENDING,
    PAID,
    PAY_AT_ARRIVAL,
    FAILED
}
