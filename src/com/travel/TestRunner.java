package com.travel;

import com.travel.enums.PaymentMode;
import com.travel.enums.SeatClass;
import com.travel.exception.InvalidSeatException;
import com.travel.exception.SeatAlreadyBookedException;
import com.travel.layout.FlightCabinLayout;
import com.travel.layout.SeatLayout;
import com.travel.model.Booking;
import com.travel.model.City;
import com.travel.model.Passenger;
import com.travel.model.Route;
import com.travel.model.Trip;
import com.travel.payment.PaymentFactory;
import com.travel.repository.BookingRepository;
import com.travel.repository.CityRepository;
import com.travel.seat.Seat;
import com.travel.service.PricingService;
import com.travel.service.TimeBasedRefundPolicy;
import com.travel.transport.Bus;
import com.travel.enums.TransportType;
import com.travel.layout.ChairLayout;
import com.travel.util.Clock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Demonstrates: Plain-Java tests, anonymous classes, lambdas, and dependency injection.
 * Runs deterministic checks without opening the default persistent booking file.
 */
public final class TestRunner {

    private static final double FARE_TOLERANCE = 0.001;
    private static int passed;
    private static int failed;

    /** Demonstrates a functional interface used by the plain-Java test runner. */
    @FunctionalInterface
    private interface TestCase {
        void run() throws Exception;
    }

    private TestRunner() {
        throw new UnsupportedOperationException("Test runner cannot be instantiated.");
    }

    /** Runs the test suite and returns whether every check passed. */
    public static boolean run() {
        passed = 0;
        failed = 0;

        List<SeatLayout> layouts = Main.createAllLayouts();
        runTest("Seat count: Bus Seater", new TestCase() {
            @Override
            public void run() {
                assertEquals(40, layoutFor(layouts, SeatClass.BUS_SEATER).getTotalSeats());
            }
        });
        checkSeatCount(layouts, SeatClass.BUS_AC_SEATER, 40);
        checkSeatCount(layouts, SeatClass.BUS_SLEEPER, 36);
        checkSeatCount(layouts, SeatClass.BUS_AC_SLEEPER, 36);
        checkSeatCount(layouts, SeatClass.TRAIN_CHAIR_CAR, 50);
        checkSeatCount(layouts, SeatClass.TRAIN_SLEEPER, 48);
        checkSeatCount(layouts, SeatClass.TRAIN_AC_2_TIER, 36);
        checkSeatCount(layouts, SeatClass.TRAIN_AC_1_TIER, 20);
        checkSeatCount(layouts, SeatClass.FLIGHT_ECONOMY, 72);
        checkSeatCount(layouts, SeatClass.FLIGHT_BUSINESS, 16);

        checkFare(SeatClass.BUS_SEATER, 1159);
        checkFare(SeatClass.BUS_AC_SLEEPER, 2189);
        checkFare(SeatClass.FLIGHT_BUSINESS, 9646);
        runTest("Flight exit-row seat adds ₹400", () -> {
            FlightCabinLayout layout = new FlightCabinLayout(SeatClass.FLIGHT_ECONOMY);
            layout.updateAllSeatPrices(3000);
            assertEquals(3000.0, layout.getSeat("5A").getPrice(), FARE_TOLERANCE);
            assertEquals(3400.0, layout.getSeat("6A").getPrice(), FARE_TOLERANCE);
        });
        runTest("Booking an occupied seat throws SeatAlreadyBookedException", () -> {
            Seat seat = new ChairLayout(SeatClass.BUS_SEATER, 10, 2, 2,
                    new char[]{'A', 'B', 'C', 'D'}).getSeat("1A");
            seat.book();
            try {
                seat.book();
                throw new AssertionError("Expected SeatAlreadyBookedException.");
            } catch (SeatAlreadyBookedException expected) {
                // Expected by the test.
            }
        });
        runTest("Unknown seat throws InvalidSeatException", () -> {
            try {
                layoutFor(layouts, SeatClass.BUS_SEATER).getSeat("NOT-A-SEAT");
                throw new AssertionError("Expected InvalidSeatException.");
            } catch (InvalidSeatException expected) {
                // Expected by the test.
            }
        });

        checkPaymentFee("Credit card fee", PaymentMode.CREDIT_CARD, 18.0);
        checkPaymentFee("Debit card fee", PaymentMode.DEBIT_CARD, 10.0);
        checkPaymentFee("UPI fee", PaymentMode.UPI, 0.0);
        checkPaymentFee("Net banking fee", PaymentMode.NET_BANKING, 12.0);
        checkPaymentFee("Cash fee", PaymentMode.CASH, 30.0);

        checkRefund("Refund tier: over 24 hours is 90%", "05-11-2026 09:00", 90);
        checkRefund("Refund tier: 6 to 24 hours is 50%", "05-11-2026 23:00", 50);
        checkRefund("Refund tier: under 6 hours is 0%", "06-11-2026 05:00", 0);

        runTest("PNR lookup accepts TRV-10001", () -> {
            assertEquals("TRV-10001", findTemporaryBooking("TRV-10001").getPnr());
        });
        runTest("PNR lookup accepts 10001", () -> {
            assertEquals("TRV-10001", findTemporaryBooking("10001").getPnr());
        });

        System.out.printf("%d of %d tests passed%n", passed, passed + failed);
        return failed == 0;
    }

    private static void checkSeatCount(List<SeatLayout> layouts, SeatClass seatClass, int expected) {
        runTest("Seat count: " + seatClass.getDisplayName(), () ->
                assertEquals(expected, layoutFor(layouts, seatClass).getTotalSeats()));
    }

    private static void checkFare(SeatClass seatClass, double expected) {
        runTest("Fare: Hyderabad to Bengaluru " + seatClass.getDisplayName(), () -> {
            CityRepository cities = CityRepository.getInstance();
            City source = requireCity(cities, "Hyderabad");
            City destination = requireCity(cities, "Bengaluru");
            Route route = new Route(source, destination, seatClass.getTransportType());
            double actual = PricingService.calculateFare(
                    seatClass, route.getDistanceKm(), null
            ).getTotalFare();
            assertEquals(expected, actual, FARE_TOLERANCE);
        });
    }

    private static void checkPaymentFee(String label, PaymentMode mode, double expected) {
        runTest(label, () -> {
            double actual = PaymentFactory.createPayment(mode).getProcessingFee(1000);
            assertEquals(expected, actual, FARE_TOLERANCE);
        });
    }

    private static void checkRefund(String label, String now, int expectedPercentage) {
        runTest(label, () -> {
            Clock.setFixedTime(now);
            try {
                Booking booking = createBooking("TRV-REFUND", LocalDate.of(2026, 11, 6),
                        LocalTime.of(10, 0));
                assertEquals(expectedPercentage,
                        new TimeBasedRefundPolicy().getRefundPercentage(booking));
            } finally {
                Clock.clearFixedTime();
            }
        });
    }

    private static Booking findTemporaryBooking(String pnr) throws IOException {
        Path directory = Files.createTempDirectory("travel-booking-test-");
        Path store = directory.resolve("bookings.dat");
        try {
            BookingRepository repository = BookingRepository.createForTesting(store);
            repository.saveBooking(createBooking("TRV-10001", LocalDate.of(2026, 11, 6),
                    LocalTime.of(10, 0)));
            return new com.travel.service.BookingService(repository).getBookingByPnr(pnr);
        } finally {
            Files.deleteIfExists(store);
            Files.deleteIfExists(directory);
        }
    }

    private static Booking createBooking(String pnr, LocalDate date, LocalTime departure) {
        CityRepository cities = CityRepository.getInstance();
        Route route = new Route(requireCity(cities, "Hyderabad"),
                requireCity(cities, "Bengaluru"), TransportType.BUS);
        Bus bus = new Bus("Test Coach");
        SeatLayout layout = bus.getSeatLayout(SeatClass.BUS_SEATER);
        Seat seat = layout.getAllSeats().get(0);
        Passenger passenger = new Passenger("Test Passenger", 30, "Other", seat.getSeatNumber());
        Trip trip = new Trip("TEST-101", bus, route, date, departure, departure.plusHours(1), 60);
        return new Booking(pnr, trip, SeatClass.BUS_SEATER, List.of(seat),
                List.of(passenger), 1000);
    }

    private static City requireCity(CityRepository repository, String name) {
        City city = repository.getCityByName(name);
        if (city == null) {
            throw new AssertionError("City not found: " + name);
        }
        return city;
    }

    private static SeatLayout layoutFor(List<SeatLayout> layouts, SeatClass seatClass) {
        for (SeatLayout layout : layouts) {
            if (layout.getSeatClass() == seatClass) {
                return layout;
            }
        }
        throw new AssertionError("No layout found for " + seatClass);
    }

    private static void runTest(String name, TestCase test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS: " + name);
        } catch (Exception | AssertionError e) {
            failed++;
            System.out.println("FAIL: " + name + " - " + e.getMessage());
        }
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("expected " + expected + ", got " + actual);
        }
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("expected " + expected + ", got " + actual);
        }
    }

    private static void assertEquals(double expected, double actual, double tolerance) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError("expected " + expected + ", got " + actual);
        }
    }
}
