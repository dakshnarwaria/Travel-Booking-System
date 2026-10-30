package com.travel.exception;

/**
 * Demonstrates: Hierarchical Checked Exception.
 * Thrown when a payment attempt fails due to cancellation, declined credentials, or transaction error.
 */
public class PaymentFailedException extends TravelException {
    private final String reason;

    public PaymentFailedException(String reason) {
        super("Payment transaction failed: " + reason);
        this.reason = reason;
    }

    public PaymentFailedException(String reason, Throwable cause) {
        super("Payment transaction failed: " + reason, cause);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
