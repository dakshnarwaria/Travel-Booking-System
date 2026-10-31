package com.travel.repository;

import com.travel.enums.BookingStatus;
import com.travel.enums.SeatClass;
import com.travel.exception.InvalidInputException;
import com.travel.model.Booking;
import com.travel.model.Trip;
import com.travel.seat.Seat;
import com.travel.util.ConsoleColors;
import com.travel.util.IdGenerator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Demonstrates: Singleton pattern, Java serialization, and repository encapsulation.
 */
public final class BookingRepository {

    private static final String DEFAULT_FILE_PATH = "data/bookings.dat";
    private static final int STORE_VERSION = 1;
    private static BookingRepository instance;

    private final Path filePath;
    private final Map<String, Booking> bookings = new LinkedHashMap<>();
    private final Map<String, Set<String>> bookedSeatsByTrip = new HashMap<>();

    private BookingRepository() {
        this(Paths.get(DEFAULT_FILE_PATH));
    }

    private BookingRepository(Path filePath) {
        this.filePath = filePath;
        load();
    }

    public static synchronized BookingRepository getInstance() {
        if (instance == null) {
            instance = new BookingRepository();
        }
        return instance;
    }

    public synchronized List<Booking> getAllBookings() {
        return Collections.unmodifiableList(new ArrayList<>(bookings.values()));
    }

    public synchronized Booking getBookingByPnr(String pnr) {
        if (pnr == null || pnr.trim().isEmpty()) {
            return null;
        }
        return bookings.get(normalizePnr(pnr));
    }

    public synchronized void saveBooking(Booking booking) {
        if (booking == null || booking.getPnr() == null) {
            throw new InvalidInputException("A booking with a PNR is required.");
        }
        Map<String, Booking> updatedBookings = new LinkedHashMap<>(bookings);
        updatedBookings.put(normalizePnr(booking.getPnr()), booking);
        Map<String, Set<String>> updatedSeatIndex = buildBookedSeatIndex(updatedBookings);
        persist(new StoreData(updatedBookings, updatedSeatIndex));
        bookings.clear();
        bookings.putAll(updatedBookings);
        bookedSeatsByTrip.clear();
        bookedSeatsByTrip.putAll(updatedSeatIndex);
    }

    public synchronized void applySavedSeats(Trip trip) {
        if (trip == null) {
            return;
        }
        for (Map.Entry<SeatClass, com.travel.layout.SeatLayout> entry
                : trip.getTransport().getSeatLayouts().entrySet()) {
            Set<String> savedSeats = bookedSeatsByTrip.get(seatKey(trip, entry.getKey()));
            if (savedSeats == null || savedSeats.isEmpty()) {
                continue;
            }
            for (Seat seat : entry.getValue().getAllSeats()) {
                if (savedSeats.contains(seat.getSeatNumber())) {
                    seat.setStatus(com.travel.enums.SeatStatus.BOOKED);
                }
            }
        }
    }

    private void load() {
        if (!Files.exists(filePath)) {
            return;
        }

        try (ObjectInputStream input = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(filePath)))) {
            Object stored = input.readObject();
            if (!(stored instanceof StoreData)) {
                throw new IOException("Booking data has an unexpected format.");
            }

            StoreData data = (StoreData) stored;
            if (data.version != STORE_VERSION) {
                throw new IOException("Booking data version is not supported.");
            }
            bookings.putAll(data.bookings);
            for (Map.Entry<String, Set<String>> entry : data.bookedSeatsByTrip.entrySet()) {
                bookedSeatsByTrip.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
            }
            ensurePnrSequence();
        } catch (IOException | ClassNotFoundException | ClassCastException | NullPointerException e) {
            bookings.clear();
            bookedSeatsByTrip.clear();
            System.out.println(ConsoleColors.colorize(
                    "Warning: Booking data could not be loaded (" + e.getMessage() + "). Starting with an empty booking store.",
                    ConsoleColors.YELLOW
            ));
        }
    }

    private void ensurePnrSequence() {
        int highestSequence = 0;
        for (String pnr : bookings.keySet()) {
            String normalized = normalizePnr(pnr);
            if (normalized.startsWith(IdGenerator.PNR_PREFIX + "-")) {
                try {
                    highestSequence = Math.max(
                            highestSequence,
                            Integer.parseInt(normalized.substring(IdGenerator.PNR_PREFIX.length() + 1))
                    );
                } catch (NumberFormatException ignored) {
                    // Invalid legacy identifiers do not affect the numeric PNR sequence.
                }
            }
        }
        IdGenerator.ensurePnrSequenceAtLeast(highestSequence);
    }

    private void persist(StoreData data) {
        Path temporaryPath = filePath.resolveSibling(filePath.getFileName() + ".tmp");
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (ObjectOutputStream output = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporaryPath)))) {
                output.writeObject(data);
            }
            try {
                Files.move(temporaryPath, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryPath, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save booking data to " + filePath + ": " + e.getMessage(), e);
        }
    }

    private Map<String, Set<String>> buildBookedSeatIndex(Map<String, Booking> bookingsToIndex) {
        Map<String, Set<String>> index = new HashMap<>();
        for (Booking booking : bookingsToIndex.values()) {
            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                continue;
            }
            for (Seat seat : booking.getSeats()) {
                index.computeIfAbsent(
                        seatKey(booking.getTrip(), booking.getSeatClass()),
                        ignored -> new LinkedHashSet<>()
                ).add(seat.getSeatNumber());
            }
        }
        return index;
    }

    private String seatKey(Trip trip, SeatClass seatClass) {
        return trip.getTripId()
                + "|" + trip.getRoute().getSource().getId()
                + "|" + trip.getRoute().getDestination().getId()
                + "|" + trip.getRoute().getTransportType().name()
                + "|" + trip.getDate()
                + "|" + seatClass.name();
    }

    private String normalizePnr(String pnr) {
        String normalized = pnr.trim().toUpperCase();
        if (normalized.startsWith(IdGenerator.PNR_PREFIX + "-")) {
            return normalized;
        }
        if (normalized.startsWith(IdGenerator.PNR_PREFIX)) {
            normalized = normalized.substring(IdGenerator.PNR_PREFIX.length());
            if (normalized.startsWith("-")) {
                normalized = normalized.substring(1);
            }
        }
        return IdGenerator.PNR_PREFIX + "-" + normalized;
    }

    private static final class StoreData implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int version = STORE_VERSION;
        private final Map<String, Booking> bookings;
        private final Map<String, Set<String>> bookedSeatsByTrip;

        private StoreData(Map<String, Booking> bookings, Map<String, Set<String>> bookedSeatsByTrip) {
            this.bookings = new LinkedHashMap<>(bookings);
            this.bookedSeatsByTrip = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : bookedSeatsByTrip.entrySet()) {
                this.bookedSeatsByTrip.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
            }
        }
    }
}
