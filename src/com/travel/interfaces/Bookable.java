package com.travel.interfaces;

import com.travel.exception.SeatAlreadyBookedException;

/**
 * Demonstrates: Interface abstraction defining reservation lifecycle behaviors.
 * Enforces a strict booking/cancellation contract across reservable resources.
 */
public interface Bookable {
    /**
     * Attempts to reserve the item.
     * @return true if successfully booked.
     * @throws SeatAlreadyBookedException if the item has already been reserved.
     */
    boolean book() throws SeatAlreadyBookedException;

    /**
     * Cancels an existing reservation.
     * @return true if successfully cancelled.
     */
    boolean cancel();

    /**
     * Checks whether the item is free for reservation.
     * @return true if available.
     */
    boolean isAvailable();
}
