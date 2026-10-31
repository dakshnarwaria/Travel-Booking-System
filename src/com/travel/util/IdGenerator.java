package com.travel.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates: Static members, Atomic operations, and Thread-safe counter encapsulation.
 * Generates monotonic, distinct PNR numbers and transaction identifiers without magic numbers.
 */
public final class IdGenerator {

    public static final String PNR_PREFIX = "TRV";
    private static final int INITIAL_PNR_SEQUENCE = 10001;
    private static final AtomicInteger PNR_COUNTER;

    private static final String TICKET_PREFIX = "TKT";
    private static final int INITIAL_TICKET_SEQUENCE = 1001;
    private static final AtomicInteger TICKET_COUNTER;

    private static final String TRANSACTION_PREFIX = "TXN";
    private static final int INITIAL_TRANSACTION_SEQUENCE = 50001;
    private static final AtomicInteger TRANSACTION_COUNTER;

    static {
        PNR_COUNTER = new AtomicInteger(INITIAL_PNR_SEQUENCE);
        TICKET_COUNTER = new AtomicInteger(INITIAL_TICKET_SEQUENCE);
        TRANSACTION_COUNTER = new AtomicInteger(INITIAL_TRANSACTION_SEQUENCE);
    }

    private IdGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Generates a unique Passenger Name Record (PNR) identifier.
     * Example: TRV-10001
     */
    public static String generatePnr() {
        return PNR_PREFIX + "-" + PNR_COUNTER.getAndIncrement();
    }

    /**
     * Generates a unique ticket identifier.
     * Example: TKT-1001
     */
    public static String generateTicketId() {
        return TICKET_PREFIX + "-" + TICKET_COUNTER.getAndIncrement();
    }

    public static String generateTransactionId() {
        return TRANSACTION_PREFIX + "-" + TRANSACTION_COUNTER.getAndIncrement();
    }

    /**
     * Resets or sets counter sequence if needed (e.g. on repository restoration).
     */
    public static synchronized void ensurePnrSequenceAtLeast(int minSequence) {
        if (PNR_COUNTER.get() <= minSequence) {
            PNR_COUNTER.set(minSequence + 1);
        }
    }
}
