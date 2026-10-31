# Travel Ticket Booking System

A plain Java, terminal-based travel booking application built to demonstrate object-oriented programming. It offers mock bus, train, and flight searches, seat selection, payment simulations, saved bookings, ticket lookup, and cancellation.

## Features

- Three transport modes: bus, train, and flight.
- Ten travel classes across those modes.
- Fifty Indian cities loaded from `data/cities.csv`.
- Seat maps with available, premium, selected, and booked-seat states.
- Mock credit-card, debit-card, UPI, net-banking, and pay-at-arrival payment strategies.
- Confirmed bookings and booked-seat inventory saved in `data/bookings.dat`.
- PNR lookup, My Bookings, passenger-name search, and cancellation with time-based refunds.
- Non-interactive seat-map demo, route fares, trip search, and an isolated plain-Java test runner.

Payments are simulations; the program does not connect to banks or payment networks.

## Requirements

- JDK 17 or newer (`java` and `javac` on `PATH`).
- PowerShell on Windows for the commands below.

## Compile and run on Windows PowerShell

Set the terminal code page and PowerShell output encoding first:

```powershell
chcp 65001
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

Without these settings, Unicode box-drawing characters and the rupee symbol may look garbled in the Windows terminal.

Compile all sources from a clean output directory:

```powershell
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue
javac -Xlint:all -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
```

Run the menu (requires input):

```powershell
java -cp out com.travel.Main --menu
```

Run the non-interactive checks:

```powershell
java -cp out com.travel.Main --test
```

To try a completed scripted booking without interacting manually:

```powershell
Get-Content test_card.txt | java -cp out com.travel.Main --menu
```

## Command-line arguments

| Argument | Purpose |
|---|---|
| `--menu` | Start the booking menu. |
| `--demo ["Class Name"]` | Render all seat layouts, or one matching class. |
| `--cities` | List the loaded cities by tier. |
| `--trips <src> <dst> <mode> <dd-MM-yyyy>` | Search scheduled trips, for example `--trips Hyderabad Bengaluru bus 05-11-2026`. |
| `--fares <src> <dst> <dd-MM-yyyy>` | Print the fare table for a route. |
| `--test` | Run the plain-Java test suite; it uses temporary booking storage, not `data/bookings.dat`. |
| `--now "dd-MM-yyyy HH:mm"` | Override the clock for cancellation/refund testing, e.g. `--now "31-10-2026 20:47" --menu`. |
| `--ascii` | Use ASCII seat-map borders instead of Unicode box characters. |
| `--nocolor` | Disable ANSI terminal colors. |

With no arguments, the program prints command-line help.

## Project structure

```text
Travel-Booking-System/
├── data/
│   ├── cities.csv
│   └── bookings.dat                 # Created at runtime; ignored by Git
├── src/com/travel/
│   ├── Main.java
│   ├── TestRunner.java
│   ├── enums/                        # Travel, seat, booking, and payment enums
│   ├── exception/                    # Domain and input exceptions
│   ├── interfaces/                   # Bookable, Displayable, Payable, Priceable
│   ├── layout/                       # Abstract seat layout and five implementations
│   ├── model/                        # City, Route, Trip, Passenger, Booking, Ticket
│   ├── payment/                      # Payment strategies and factory
│   ├── repository/                   # City and serialized booking repositories
│   ├── seat/                         # Seat hierarchy
│   ├── service/                      # Search, pricing, booking, payment, refund
│   ├── transport/                    # Bus, Train, Flight
│   ├── ui/                           # Console menu and seat-map renderer
│   └── util/                         # Input, clock, IDs, colors, distance, constants
├── test_*.txt                        # Piped menu workflows
├── build.bat
└── build.sh
```

## Class diagram

```text
INTERFACES
  Displayable: render(), default getDisplaySummary()
  Bookable: book(), cancel(), isAvailable()
  Payable: pay(amount), getProcessingFee(amount)
  Priceable: calculateFare(), default applyGst(), static isValidGstPercentage()

SEATS
  Seat (abstract; Displayable, Bookable, Serializable)
  ├── ChairSeat
  └── SleeperSeat
      └── BerthSeat

LAYOUTS
  SeatLayout (abstract; owns seats and a seat-number map)
  ├── ChairLayout
  ├── SleeperDeckLayout
  ├── BerthBayLayout
  ├── CabinLayout
  └── FlightCabinLayout

TRANSPORT
  Transport (abstract; Priceable)
  ├── Bus
  ├── Train
  └── Flight

PAYMENTS (strategy pattern)
  Payment (abstract; Payable, Serializable)
  ├── CardPayment (abstract)
  │   ├── CreditCardPayment
  │   └── DebitCardPayment
  ├── UpiPayment
  ├── NetBankingPayment
  └── CashPayment
  PaymentFactory creates a strategy from PaymentMode.

MODELS
  City ── Route ── Trip (Transport + Route + travel date/times)
  Booking (Trip + SeatClass + Seats + Passengers + Payment + status)
  Ticket is rendered from a Booking.

EXCEPTIONS
  TravelException (checked)
  ├── SeatAlreadyBookedException
  ├── InvalidSeatException
  ├── PaymentFailedException
  └── RouteNotFoundException
  InvalidInputException (unchecked)
  ├── InputEndedException
  └── TooManyInvalidAttemptsException
```

## User journey

1. Choose **Book Travel Ticket**, **View Ticket by PNR**, **My Bookings**, **Cancel Ticket**, or **Exit**.
2. For a booking, select the source and destination cities, travel date, transport mode, trip, and class.
3. Review the class seat map, select seats, and enter passenger details.
4. Review the fare breakdown, choose a payment method, and enter mock payment details if needed.
5. On successful payment, see the PNR, ticket, and updated seat map. Confirmed bookings remain available after restarting.
6. Look up a ticket by PNR or select a booking to view or cancel it. Refund percentage depends on departure time; cash-at-arrival bookings have no refund.

## Pricing rules

The route distance is the haversine distance between the city coordinates multiplied by the transport factor (bus `1.25`, train `1.20`, flight `1.00`). For each class:

```text
base fare = max(class minimum fare, round(distance in km × class rate per km))
fare before payment = base fare + reservation/airport fee + GST + convenience fee + seat add-on
total at payment = fare before payment + payment-mode processing fee
```

Rates, minimums, and fees are defined in the enums/services. Bus window seats add ₹50; flight exit-row seats add ₹400. Payment processing is credit card 1.8%, debit card 1.0%, UPI ₹0, net banking ₹12, and cash-at-arrival ₹30. Cancellation refunds are 90% more than 24 hours before departure, 50% from 6 through 24 hours, and 0% under 6 hours or after departure.

## OOPS coverage

| OOPS concept | Files and examples |
|---|---|
| Abstraction | `seat/Seat.java` — `Seat`; `layout/SeatLayout.java` — `SeatLayout`; `transport/Transport.java` — `Transport`; `payment/Payment.java` — `Payment`; `interfaces/` — `Bookable`, `Displayable`, `Payable`, `Priceable`. |
| Encapsulation | `seat/Seat.java` — validated private state; `model/Passenger.java` — validated passenger fields; `model/Booking.java` — booking state and unmodifiable list views. |
| Inheritance (single, multilevel, hierarchical) | `seat/ChairSeat.java`, `seat/SleeperSeat.java`, `seat/BerthSeat.java` — `Seat → SleeperSeat → BerthSeat`; `transport/Bus.java`, `Train.java`, `Flight.java` extend `Transport`. |
| Multiple inheritance via interfaces | `seat/Seat.java` — `Seat implements Displayable, Bookable, Serializable`. |
| Runtime polymorphism (overriding) | `seat/ChairSeat.java`, `SleeperSeat.java`, `BerthSeat.java` — `render()`; payment subclasses override `Payment.pay()` and `getProcessingFee()`; transports provide their own type and speed. |
| Compile-time polymorphism (overloading) | `service/BookingService.java` — overloaded `bookSeat(...)` methods. |
| Constructors, `this`, `super`, chaining | `transport/Train.java` — `Train(String)` delegates with `this(...)`; seat subclasses and transport subclasses use `super(...)`. |
| `static`, `final`, static initializer | `util/IdGenerator.java` — static counters initialized in a static block; `util/ConsoleColors.java` — final utility class and constants; `payment/Payment.java` — final payment operation. |
| Enums with fields and methods | `enums/SeatClass.java` — class, rate, minimum fare, premium flag; `enums/PaymentMode.java` — display name and fee calculation. |
| Inner/nested/anonymous classes and lambdas | `service/PricingService.java` — nested `FareBreakdown`; `repository/BookingRepository.java` — nested `StoreData`; `TestRunner.java` — nested `TestCase` and an anonymous test implementation; `service/SearchService.java` — comparator lambda. |
| Exception handling | `exception/TravelException.java` and subclasses; `ui/ConsoleMenu.java` — bounded input and payment failure handling; `repository/BookingRepository.java` — corrupted-store recovery. |
| Collections and generics | `layout/SeatLayout.java` — `List<Seat>` and `Map<String, Seat>`; `transport/Transport.java` — `Map<SeatClass, SeatLayout>`; repositories and services use typed collections. |
| Object methods (`equals`, `hashCode`, `toString`) | `seat/Seat.java` — `Seat`; `model/City.java` — `City`; `model/Booking.java` — `Booking`; also `model/Route.java`, `Passenger.java`, and `Trip.java`. |
| Design patterns | Strategy: `payment/Payment.java` and concrete payment classes; factory: `payment/PaymentFactory.java`; singleton: `repository/CityRepository.java` and `BookingRepository.java`. |
| Packages and access modifiers | All source files under `src/com/travel/`; private model state, protected hierarchy hooks, and public service/repository contracts. |
| File handling and serialization | `repository/BookingRepository.java` — versioned serialized booking store; `model/Booking.java`, `model/Trip.java`, `seat/Seat.java` and related model classes implement `Serializable`. |
| Interface default and static methods | `interfaces/Priceable.java` — `applyGst()` and `isValidGstPercentage()`; `interfaces/Displayable.java` — default `getDisplaySummary()`. |
| `instanceof` and casting | `service/PricingService.java` — recognizes and casts `ChairSeat` for seat add-ons; `ui/SeatMapRenderer.java` — dispatches layout rendering by layout subtype. |
| Varargs | `service/BookingService.java` — `bookSeats(SeatLayout, String... seatNumbers)`. |

## Tests and sample workflows

The `--test` runner prints a result for each seat count, fare, seat validation, payment fee, refund tier, and PNR form. Its PNR tests use a temporary repository file and do not read or write `data/bookings.dat`. The `test_*.txt` files provide piped examples for end-to-end menu workflows.
