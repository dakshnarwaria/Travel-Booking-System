package com.travel.service;

import com.travel.model.Booking;
import com.travel.util.Clock;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Demonstrates: Runtime polymorphism for time-tiered cancellation refunds.
 */
public class TimeBasedRefundPolicy implements RefundPolicy {

    public static final int REFUND_MORE_THAN_24_HOURS_PERCENT = 90;
    public static final int REFUND_6_TO_24_HOURS_PERCENT = 50;
    public static final int REFUND_UNDER_6_HOURS_PERCENT = 0;
    public static final long HOURS_BEFORE_DEPARTURE_HIGH_TIER = 24;
    public static final long HOURS_BEFORE_DEPARTURE_LOW_TIER = 6;

    @Override
    public int getRefundPercentage(Booking booking) {
        if (booking == null || booking.getTrip() == null) {
            return REFUND_UNDER_6_HOURS_PERCENT;
        }

        LocalDateTime departure = booking.getTrip().getDate()
                .atTime(booking.getTrip().getDepartureTime());

        Duration remaining = Duration.between(Clock.now(), departure);
        if (remaining.isNegative() || remaining.isZero()) {
            return REFUND_UNDER_6_HOURS_PERCENT;
        }
        if (remaining.compareTo(Duration.ofHours(HOURS_BEFORE_DEPARTURE_HIGH_TIER)) > 0) {
            return REFUND_MORE_THAN_24_HOURS_PERCENT;
        }
        if (remaining.compareTo(Duration.ofHours(HOURS_BEFORE_DEPARTURE_LOW_TIER)) >= 0) {
            return REFUND_6_TO_24_HOURS_PERCENT;
        }
        return REFUND_UNDER_6_HOURS_PERCENT;
    }
}
