package com.travel.payment;

import com.travel.enums.PaymentMode;
import com.travel.exception.PaymentFailedException;
import com.travel.interfaces.Payable;
import com.travel.util.IdGenerator;

import java.io.Serializable;

/**
 * Demonstrates: Abstraction and runtime polymorphism for payment strategies.
 */
public abstract class Payment implements Payable, Serializable {

    private static final long serialVersionUID = 1L;

    private final PaymentMode paymentMode;
    private double amount;
    private PaymentStatus status = PaymentStatus.PENDING;
    private String transactionId;

    protected Payment(PaymentMode paymentMode) {
        if (paymentMode == null) {
            throw new IllegalArgumentException("Payment mode cannot be null.");
        }
        this.paymentMode = paymentMode;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public final boolean pay(double amount) throws PaymentFailedException {
        if (!Double.isFinite(amount) || amount <= 0.0) {
            throw new PaymentFailedException("Payment amount must be a positive, finite value.");
        }
        this.amount = amount;
        return pay();
    }

    public abstract boolean pay() throws PaymentFailedException;

    @Override
    public abstract double getProcessingFee(double amount);

    protected final boolean completePayment(PaymentStatus completedStatus) {
        if (completedStatus != PaymentStatus.PAID && completedStatus != PaymentStatus.PAY_AT_ARRIVAL) {
            throw new IllegalArgumentException("A completed payment must be paid or payable at arrival.");
        }
        status = completedStatus;
        transactionId = generateTransactionId();
        return true;
    }

    public final void markFailed() {
        status = PaymentStatus.FAILED;
    }

    protected String generateTransactionId() {
        return IdGenerator.generateTransactionId();
    }
}
