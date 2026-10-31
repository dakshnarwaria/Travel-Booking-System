package com.travel.service;

import com.travel.enums.SeatClass;
import com.travel.enums.TransportType;
import com.travel.seat.ChairSeat;
import com.travel.seat.Seat;
import com.travel.util.Constants;

import java.io.Serializable;

/**
 * Demonstrates: Encapsulation of Pricing Algorithms, Separation of Concerns,
 * and Elimination of Magic Numbers via Constants.
 * Implements the exact pricing tables and tax schedules from PLAN.md Section 5.
 */
public final class PricingService {

    // Add-on fees
    public static final double BUS_WINDOW_ADDON = 50.0;
    public static final double FLIGHT_EXIT_ROW_ADDON = 400.0;

    // Bus fees
    public static final double BUS_CONVENIENCE_RATE = 0.03; // 3%

    // Train reservation and convenience fees
    public static final double TRAIN_CC_RESERVATION = 40.0;
    public static final double TRAIN_CC_CONVENIENCE = 20.0;
    public static final double TRAIN_CC_GST_RATE = 0.05; // 5%

    public static final double TRAIN_SL_RESERVATION = 20.0;
    public static final double TRAIN_SL_CONVENIENCE = 20.0;
    public static final double TRAIN_SL_GST_RATE = 0.00; // No GST

    public static final double TRAIN_2A_RESERVATION = 50.0;
    public static final double TRAIN_2A_CONVENIENCE = 40.0;
    public static final double TRAIN_2A_GST_RATE = 0.05; // 5%

    public static final double TRAIN_1A_RESERVATION = 60.0;
    public static final double TRAIN_1A_CONVENIENCE = 40.0;
    public static final double TRAIN_1A_GST_RATE = 0.05; // 5%

    // Flight fees
    public static final double FLIGHT_AIRPORT_FEE = 300.0;
    public static final double FLIGHT_CONVENIENCE_FEE = 350.0;
    public static final double FLIGHT_ECONOMY_GST_RATE = 0.05; // 5%
    public static final double FLIGHT_BUSINESS_GST_RATE = 0.12; // 12%

    private PricingService() {
        throw new UnsupportedOperationException("Utility service cannot be instantiated");
    }

    /**
     * Demonstrates a static nested data-transfer class holding detailed fare calculations.
     */
    public static class FareBreakdown implements Serializable {
        private static final long serialVersionUID = 1L;

        private final double baseFare;
        private final double reservationFee;
        private final double airportFee;
        private final double gstAmount;
        private final double convenienceFee;
        private final double seatAddOn;
        private final double totalFare;

        public FareBreakdown(double baseFare, double reservationFee, double airportFee,
                             double gstAmount, double convenienceFee, double seatAddOn) {
            this.baseFare = baseFare;
            this.reservationFee = reservationFee;
            this.airportFee = airportFee;
            this.gstAmount = gstAmount;
            this.convenienceFee = convenienceFee;
            this.seatAddOn = seatAddOn;
            this.totalFare = Math.round(baseFare + reservationFee + airportFee + gstAmount + convenienceFee + seatAddOn);
        }

        public double getBaseFare() { return baseFare; }
        public double getReservationFee() { return reservationFee; }
        public double getAirportFee() { return airportFee; }
        public double getGstAmount() { return gstAmount; }
        public double getConvenienceFee() { return convenienceFee; }
        public double getSeatAddOn() { return seatAddOn; }
        public double getTotalFare() { return totalFare; }

        @Override
        public String toString() {
            return String.format("Base: " + Constants.CURRENCY + "%.0f, Res/Airport: " + Constants.CURRENCY + "%.0f, GST: " + Constants.CURRENCY + "%.0f, Conv: " + Constants.CURRENCY + "%.0f, Addon: " + Constants.CURRENCY + "%.0f => Total: " + Constants.CURRENCY + "%.0f",
                    baseFare, (reservationFee + airportFee), gstAmount, convenienceFee, seatAddOn, totalFare);
        }
    }

    /**
     * Computes the base fare using the formula: max(minFare, distanceKm * ratePerKm).
     */
    public static double computeBaseFare(SeatClass seatClass, double distanceKm) {
        if (seatClass == null) return 0.0;
        double calculated = distanceKm * seatClass.getRatePerKm();
        return Math.max(seatClass.getMinFare(), Math.round(calculated));
    }

    /**
     * Calculates the complete itemized fare breakdown for a given seat class, distance, and optional seat.
     */
    public static FareBreakdown calculateFare(SeatClass seatClass, double distanceKm, Seat seat) {
        double baseFare = computeBaseFare(seatClass, distanceKm);
        double reservation = 0.0;
        double airportFee = 0.0;
        double gstRate = 0.0;
        double convenience = 0.0;
        double seatAddOn = 0.0;

        // Seat-level add-ons
        if (seat instanceof ChairSeat) {
            ChairSeat cs = (ChairSeat) seat;
            if (seatClass.getTransportType() == TransportType.BUS && cs.isWindowSeat()) {
                seatAddOn = BUS_WINDOW_ADDON;
            } else if (seatClass.getTransportType() == TransportType.FLIGHT && cs.isExitRow()) {
                seatAddOn = FLIGHT_EXIT_ROW_ADDON;
            }
        }

        switch (seatClass) {
            case BUS_SEATER:
            case BUS_AC_SEATER:
            case BUS_SLEEPER:
            case BUS_AC_SLEEPER:
                convenience = Math.round(baseFare * BUS_CONVENIENCE_RATE);
                break;

            case TRAIN_CHAIR_CAR:
                reservation = TRAIN_CC_RESERVATION;
                gstRate = TRAIN_CC_GST_RATE;
                convenience = TRAIN_CC_CONVENIENCE;
                break;
            case TRAIN_SLEEPER:
                reservation = TRAIN_SL_RESERVATION;
                gstRate = TRAIN_SL_GST_RATE;
                convenience = TRAIN_SL_CONVENIENCE;
                break;
            case TRAIN_AC_2_TIER:
                reservation = TRAIN_2A_RESERVATION;
                gstRate = TRAIN_2A_GST_RATE;
                convenience = TRAIN_2A_CONVENIENCE;
                break;
            case TRAIN_AC_1_TIER:
                reservation = TRAIN_1A_RESERVATION;
                gstRate = TRAIN_1A_GST_RATE;
                convenience = TRAIN_1A_CONVENIENCE;
                break;

            case FLIGHT_ECONOMY:
                airportFee = FLIGHT_AIRPORT_FEE;
                gstRate = FLIGHT_ECONOMY_GST_RATE;
                convenience = FLIGHT_CONVENIENCE_FEE;
                break;
            case FLIGHT_BUSINESS:
                airportFee = FLIGHT_AIRPORT_FEE;
                gstRate = FLIGHT_BUSINESS_GST_RATE;
                convenience = FLIGHT_CONVENIENCE_FEE;
                break;
        }

        double taxableAmount = baseFare + reservation + airportFee;
        double gstAmount = Math.round(taxableAmount * gstRate);

        return new FareBreakdown(baseFare, reservation, airportFee, gstAmount, convenience, seatAddOn);
    }
}
