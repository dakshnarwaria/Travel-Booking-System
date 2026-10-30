package com.travel.util;

/**
 * Demonstrates: Encapsulation, Static Methods, and Mathematical Modeling without magic numbers.
 * Calculates great-circle distances between geographic coordinates using the Haversine formula.
 */
public final class DistanceCalculator {

    // Mean radius of Earth in kilometers
    public static final double EARTH_RADIUS_KM = 6371.0;

    private DistanceCalculator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Calculates the great-circle distance between two points on the Earth in kilometers.
     *
     * @param lat1 Latitude of source in degrees
     * @param lon1 Longitude of source in degrees
     * @param lat2 Latitude of destination in degrees
     * @param lon2 Longitude of destination in degrees
     * @return Straight-line distance in kilometers
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double originLatRad = Math.toRadians(lat1);
        double destLatRad = Math.toRadians(lat2);

        double sinHalfDLat = Math.sin(dLat / 2.0);
        double sinHalfDLon = Math.sin(dLon / 2.0);

        double a = (sinHalfDLat * sinHalfDLat)
                + Math.cos(originLatRad) * Math.cos(destLatRad) * (sinHalfDLon * sinHalfDLon);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculates route distance adjusted by the transport modality's road/path factor.
     */
    public static double calculateRouteDistance(double lat1, double lon1, double lat2, double lon2, double roadFactor) {
        double directDistance = haversine(lat1, lon1, lat2, lon2);
        return directDistance * roadFactor;
    }
}
