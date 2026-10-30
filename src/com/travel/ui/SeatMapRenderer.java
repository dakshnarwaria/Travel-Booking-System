package com.travel.ui;

import com.travel.enums.SeatClass;
import com.travel.enums.SeatStatus;
import com.travel.enums.TransportType;
import com.travel.layout.BerthBayLayout;
import com.travel.layout.CabinLayout;
import com.travel.layout.ChairLayout;
import com.travel.layout.FlightCabinLayout;
import com.travel.layout.SeatLayout;
import com.travel.layout.SleeperDeckLayout;
import com.travel.seat.Seat;
import com.travel.seat.SleeperSeat;
import com.travel.util.ConsoleColors;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates: Polymorphism, Clean Visual Alignment, and ANSI Color Encapsulation.
 * Implements simplified geometric seat rendering using strictly permitted characters.
 */
public final class SeatMapRenderer {

    public static final int SLEEPER_WIDTH = 7;
    public static final int SLEEPER_LINES = 6; // 5 box lines + 1 price line

    public static final int CHAIR_WIDTH = 6;
    public static final int CHAIR_LINES = 4; // 3 box lines + 1 price line

    private SeatMapRenderer() {
        throw new UnsupportedOperationException("Utility renderer cannot be instantiated");
    }

    /**
     * Renders a single seat block. Returns lines with exact visible width:
     * 7 wide for sleepers/berths (6 lines), 6 wide for chairs (4 lines).
     */
    public static List<String> renderBlock(Seat seat) {
        if (seat == null) {
            return emptyBlock(CHAIR_WIDTH, CHAIR_LINES);
        }

        boolean isSleeper = (seat instanceof SleeperSeat);
        boolean booked = (seat.getStatus() == SeatStatus.BOOKED);
        List<String> rawLines = new ArrayList<>();

        if (isSleeper) {
            // Sleeper / Berth block: 7 wide, 5 box lines + 1 price line
            rawLines.add("┌─────┐");
            rawLines.add(booked ? "│  X  │" : "│     │");
            rawLines.add("│" + padString(seat.getSeatNumber(), 5) + "│");
            rawLines.add(booked ? "│     │" : "│ === │");
            rawLines.add("└─────┘");
            String priceStr = booked ? "Sold" : ("₹" + Math.round(seat.getPrice()));
            rawLines.add(padString(priceStr, SLEEPER_WIDTH));
        } else {
            // Chair block: 6 wide, 3 box lines + 1 price line
            rawLines.add("┌────┐");
            String numStr = booked ? ("X" + seat.getSeatNumber()) : seat.getSeatNumber();
            rawLines.add("│" + padString(numStr, 4) + "│");
            rawLines.add("└────┘");
            String priceStr = booked ? "Sold" : ("₹" + Math.round(seat.getPrice()));
            rawLines.add(padString(priceStr, CHAIR_WIDTH));
        }

        // Apply colour: standard green, premium yellow, booked grey, selected cyan
        String colorCode;
        if (booked) {
            colorCode = ConsoleColors.COLOR_BOOKED;
        } else if (seat.getStatus() == SeatStatus.SELECTED) {
            colorCode = ConsoleColors.COLOR_SELECTED;
        } else if (seat.getSeatClass() != null && seat.getSeatClass().isPremium()) {
            colorCode = ConsoleColors.COLOR_PREMIUM;
        } else {
            colorCode = ConsoleColors.COLOR_STANDARD;
        }

        List<String> colored = new ArrayList<>();
        for (String raw : rawLines) {
            if (ConsoleColors.isNoColor()) {
                colored.add(raw);
            } else {
                colored.add(colorCode + raw + ConsoleColors.RESET);
            }
        }
        return colored;
    }

    public static List<String> emptyBlock(int width, int lines) {
        List<String> list = new ArrayList<>();
        String blank = repeatChar(' ', width);
        for (int i = 0; i < lines; i++) {
            list.add(blank);
        }
        return list;
    }

    /**
     * Renders the complete seat layout map.
     */
    public static void renderLayout(SeatLayout layout) {
        if (layout == null) return;

        printLegend(layout);

        if (layout instanceof SleeperDeckLayout) {
            renderBusSleeper((SleeperDeckLayout) layout);
        } else if (layout instanceof BerthBayLayout) {
            renderTrainBerthBay((BerthBayLayout) layout);
        } else if (layout instanceof CabinLayout) {
            renderTrainCabin((CabinLayout) layout);
        } else if (layout instanceof FlightCabinLayout) {
            renderFlight((FlightCabinLayout) layout);
        } else if (layout instanceof ChairLayout) {
            renderChair((ChairLayout) layout);
        }
        System.out.println();
    }

    public static void printLegend(SeatLayout layout) {
        SeatClass sc = layout.getSeatClass();
        String className = (sc != null) ? sc.getDisplayName() : "Coach";
        String modeName = (sc != null) ? sc.getTransportType().getDisplayName() : "Transport";

        System.out.println("================================================================================");
        System.out.println(ConsoleColors.colorize(
                String.format("SEAT MAP - %s %s", modeName.toUpperCase(), className.toUpperCase()),
                ConsoleColors.CYAN,
                ConsoleColors.BOLD
        ));
        System.out.printf("Capacity: %d Seats | Available: %d | Booked: %d\n",
                layout.getTotalSeats(), layout.getAvailableSeatsCount(), layout.getBookedSeatsCount());
        System.out.println("--------------------------------------------------------------------------------");
        System.out.print("Legend: ");
        System.out.print(ConsoleColors.colorize("[Green] Standard  ", ConsoleColors.COLOR_STANDARD));
        System.out.print(ConsoleColors.colorize("[Yellow] Premium  ", ConsoleColors.COLOR_PREMIUM));
        System.out.print(ConsoleColors.colorize("[Grey] Booked (X)  ", ConsoleColors.COLOR_BOOKED));
        System.out.print(ConsoleColors.colorize("[Cyan] Selected", ConsoleColors.COLOR_SELECTED));
        System.out.println("\n--------------------------------------------------------------------------------");
    }

    // -------------------------------------------------------------------------
    // Bus Sleeper: Lower deck and Upper deck side by side (each 6 rows of 1+2)
    // -------------------------------------------------------------------------

    private static void renderBusSleeper(SleeperDeckLayout layout) {
        // Lower deck: Block1 (7) + 4 sp + Block2 (7) + 2 sp + Block3 (7) = 27 visible chars
        // Upper deck: Block1 (7) + 4 sp + Block2 (7) + 2 sp + Block3 (7) = 27 visible chars
        // Deck gap: 6 spaces
        String lowerHead = padString("Lower deck (driver)", 27);
        String upperHead = padString("Upper deck", 27);
        String deckGap = "      ";

        System.out.println(ConsoleColors.colorize(lowerHead + deckGap + upperHead, ConsoleColors.WHITE, ConsoleColors.BOLD));
        System.out.println();

        List<Seat> lowerSeats = layout.getSeatsByDeck("Lower");
        List<Seat> upperSeats = layout.getSeatsByDeck("Upper");
        int rows = layout.getTotalRows();

        for (int r = 0; r < rows; r++) {
            int lBase = r * SleeperDeckLayout.SEATS_PER_ROW;
            Seat l0 = (lBase < lowerSeats.size()) ? lowerSeats.get(lBase) : null;
            Seat l1 = (lBase + 1 < lowerSeats.size()) ? lowerSeats.get(lBase + 1) : null;
            Seat l2 = (lBase + 2 < lowerSeats.size()) ? lowerSeats.get(lBase + 2) : null;

            int uBase = r * SleeperDeckLayout.SEATS_PER_ROW;
            Seat u0 = (uBase < upperSeats.size()) ? upperSeats.get(uBase) : null;
            Seat u1 = (uBase + 1 < upperSeats.size()) ? upperSeats.get(uBase + 1) : null;
            Seat u2 = (uBase + 2 < upperSeats.size()) ? upperSeats.get(uBase + 2) : null;

            List<String> bl0 = renderBlock(l0);
            List<String> bl1 = renderBlock(l1);
            List<String> bl2 = renderBlock(l2);

            List<String> bu0 = renderBlock(u0);
            List<String> bu1 = renderBlock(u1);
            List<String> bu2 = renderBlock(u2);

            for (int line = 0; line < SLEEPER_LINES; line++) {
                String leftCol = bl0.get(line) + "    " + bl1.get(line) + "  " + bl2.get(line);
                String rightCol = bu0.get(line) + "    " + bu1.get(line) + "  " + bu2.get(line);
                System.out.println(leftCol + deckGap + rightCol);
            }
            System.out.println();
        }
    }

    // -------------------------------------------------------------------------
    // Train Berth Bay: Lower and Upper side by side per bay
    // -------------------------------------------------------------------------

    private static void renderTrainBerthBay(BerthBayLayout layout) {
        String lowerHead = padString("Lower Deck (LB & SL)", 27);
        String upperHead = padString("Upper Deck (UB & SU)", 27);
        String deckGap = "      ";

        System.out.println(ConsoleColors.colorize(lowerHead + deckGap + upperHead, ConsoleColors.WHITE, ConsoleColors.BOLD));
        System.out.println();

        List<Seat> lowerSeats = layout.getSeatsByDeck("Lower");
        List<Seat> upperSeats = layout.getSeatsByDeck("Upper");
        int bays = layout.getTotalBays();

        for (int bay = 0; bay < bays; bay++) {
            System.out.println(ConsoleColors.colorize(String.format("Bay %d", (bay + 1)), ConsoleColors.WHITE, ConsoleColors.BOLD));

            int lBase = bay * 3;
            Seat l1 = (lBase < lowerSeats.size()) ? lowerSeats.get(lBase) : null;
            Seat l2 = (lBase + 1 < lowerSeats.size()) ? lowerSeats.get(lBase + 1) : null;
            Seat lSide = (lBase + 2 < lowerSeats.size()) ? lowerSeats.get(lBase + 2) : null;

            int uBase = bay * 3;
            Seat u1 = (uBase < upperSeats.size()) ? upperSeats.get(uBase) : null;
            Seat u2 = (uBase + 1 < upperSeats.size()) ? upperSeats.get(uBase + 1) : null;
            Seat uSide = (uBase + 2 < upperSeats.size()) ? upperSeats.get(uBase + 2) : null;

            List<String> bl1 = renderBlock(l1);
            List<String> bl2 = renderBlock(l2);
            List<String> blSide = renderBlock(lSide);

            List<String> bu1 = renderBlock(u1);
            List<String> bu2 = renderBlock(u2);
            List<String> buSide = renderBlock(uSide);

            for (int line = 0; line < SLEEPER_LINES; line++) {
                String leftCol = bl1.get(line) + "  " + bl2.get(line) + "    " + blSide.get(line);
                String rightCol = bu1.get(line) + "  " + bu2.get(line) + "    " + buSide.get(line);
                System.out.println(leftCol + deckGap + rightCol);
            }
            System.out.println();
        }
    }

    // -------------------------------------------------------------------------
    // Train Cabin: Private coupes side by side per cabin
    // -------------------------------------------------------------------------

    private static void renderTrainCabin(CabinLayout layout) {
        String lowerHead = padString("Lower Deck (LB)", 16);
        String upperHead = padString("Upper Deck (UB)", 16);
        String deckGap = "      ";

        System.out.println(ConsoleColors.colorize(lowerHead + deckGap + upperHead, ConsoleColors.WHITE, ConsoleColors.BOLD));
        System.out.println();

        List<Seat> lowerSeats = layout.getSeatsByDeck("Lower");
        List<Seat> upperSeats = layout.getSeatsByDeck("Upper");
        int cabins = layout.getTotalRows();

        for (int c = 0; c < cabins; c++) {
            System.out.println(ConsoleColors.colorize(String.format("Cabin %d", (c + 1)), ConsoleColors.WHITE, ConsoleColors.BOLD));

            int lBase = c * 2;
            Seat l1 = (lBase < lowerSeats.size()) ? lowerSeats.get(lBase) : null;
            Seat l2 = (lBase + 1 < lowerSeats.size()) ? lowerSeats.get(lBase + 1) : null;

            int uBase = c * 2;
            Seat u1 = (uBase < upperSeats.size()) ? upperSeats.get(uBase) : null;
            Seat u2 = (uBase + 1 < upperSeats.size()) ? upperSeats.get(uBase + 1) : null;

            List<String> bl1 = renderBlock(l1);
            List<String> bl2 = renderBlock(l2);
            List<String> bu1 = renderBlock(u1);
            List<String> bu2 = renderBlock(u2);

            for (int line = 0; line < SLEEPER_LINES; line++) {
                String leftCol = bl1.get(line) + "  " + bl2.get(line);
                String rightCol = bu1.get(line) + "  " + bu2.get(line);
                System.out.println(leftCol + deckGap + rightCol);
            }
            System.out.println();
        }
    }

    // -------------------------------------------------------------------------
    // Chair Layouts: 10 rows of 2+2 (bus) or 3+2 (train)
    // -------------------------------------------------------------------------

    private static void renderChair(ChairLayout layout) {
        boolean isBus = layout.getSeatClass().getTransportType() == TransportType.BUS;
        int leftCols = layout.getLeftCols();
        int rightCols = layout.getRightCols();
        int colsPerRow = leftCols + rightCols;

        if (isBus) {
            System.out.println(ConsoleColors.colorize("  Lower deck (driver)", ConsoleColors.WHITE, ConsoleColors.BOLD));
            System.out.println(ConsoleColors.colorize("Window  Aisle    Aisle  Window", ConsoleColors.GREY));
        } else {
            System.out.println(ConsoleColors.colorize("Window Middle Aisle    Aisle Window", ConsoleColors.GREY));
        }
        System.out.println();

        List<Seat> allSeats = layout.getAllSeats();
        int totalRows = layout.getTotalRows();

        for (int r = 0; r < totalRows; r++) {
            List<List<String>> rowBlocks = new ArrayList<>();
            for (int c = 0; c < colsPerRow; c++) {
                int idx = r * colsPerRow + c;
                Seat seat = (idx < allSeats.size()) ? allSeats.get(idx) : null;
                rowBlocks.add(renderBlock(seat));
            }
            printChairRow(rowBlocks, leftCols);
        }
    }

    // -------------------------------------------------------------------------
    // Flight Layouts: 3+3 (Economy) or 2+2 (Business)
    // -------------------------------------------------------------------------

    private static void renderFlight(FlightCabinLayout layout) {
        boolean isBusiness = layout.getSeatClass() == SeatClass.FLIGHT_BUSINESS;
        int leftCols = isBusiness ? 2 : 3;
        int colsPerRow = isBusiness ? 4 : 6;

        if (isBusiness) {
            System.out.println(ConsoleColors.colorize("Window  Aisle    Aisle  Window", ConsoleColors.GREY));
        } else {
            System.out.println(ConsoleColors.colorize("Window Middle Aisle    Aisle Middle Window", ConsoleColors.GREY));
        }
        System.out.println();

        List<Seat> allSeats = layout.getAllSeats();
        int totalRows = layout.getTotalRows();

        for (int r = 0; r < totalRows; r++) {
            if (!isBusiness && r == 5) {
                System.out.println(ConsoleColors.colorize("--- EXIT ROW (+₹400) --------------------------------", ConsoleColors.YELLOW));
            }

            List<List<String>> rowBlocks = new ArrayList<>();
            for (int c = 0; c < colsPerRow; c++) {
                int idx = r * colsPerRow + c;
                Seat seat = (idx < allSeats.size()) ? allSeats.get(idx) : null;
                rowBlocks.add(renderBlock(seat));
            }
            printChairRow(rowBlocks, leftCols);
        }
    }

    private static void printChairRow(List<List<String>> rowBlocks, int leftSplit) {
        for (int line = 0; line < CHAIR_LINES; line++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rowBlocks.size(); i++) {
                sb.append(rowBlocks.get(i).get(line));
                if (i < rowBlocks.size() - 1) {
                    if (i == leftSplit - 1) {
                        sb.append("    "); // 4-space aisle gap
                    } else {
                        sb.append("  "); // 2-space gap between seats
                    }
                }
            }
            System.out.println(sb.toString());
        }
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // Formatting Helpers
    // -------------------------------------------------------------------------

    public static String stripAnsi(String s) {
        if (s == null) return "";
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    public static int visibleWidth(String s) {
        return stripAnsi(s).length();
    }

    public static String padString(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) {
            return s.substring(0, width);
        }
        int diff = width - s.length();
        int left = diff / 2;
        int right = diff - left;
        return repeatChar(' ', left) + s + repeatChar(' ', right);
    }

    private static String repeatChar(char c, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
