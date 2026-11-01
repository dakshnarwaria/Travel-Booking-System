# Travel Ticket Booking System

A console-based travel ticket booking system written in **core Java**, built to demonstrate **Object Oriented Programming (OOPS)**. Search routes between 50 Indian cities, choose bus, train or flight, pick seats on a RedBus-style seat map, pay through a mock payment gateway, and manage your bookings, all from the terminal.

**Repository:** https://github.com/dakshnarwaria/Travel-Booking-System

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Features](#2-features)
3. [Requirements: JDK Installation](#3-requirements-jdk-installation)
4. [Clone and Setup](#4-clone-and-setup)
5. [How to Run and Check Every Part](#5-how-to-run-and-check-every-part)
6. [User Journey](#6-user-journey)
7. [Pricing Rules](#7-pricing-rules)
8. [Project Structure](#8-project-structure)
9. [Class Diagram](#9-class-diagram)
10. [Testing](#10-testing)
11. [Java and OOPS Concepts Used](#11-java-and-oops-concepts-used)
12. [Known Limitations](#12-known-limitations)

---

## 1. Project Overview

| Item | Details |
|---|---|
| Type | College project, terminal (console) application |
| Language | Java only (no frameworks, no external libraries, no GUI) |
| Goal | Cover all major OOPS concepts in one realistic system |
| Data | 50 cities in `data/cities.csv`, bookings saved in `data/bookings.dat` |
| Java version | JDK 17 or newer |

### 1.1 What the system does

1. Takes a source city, a destination city and a travel date.
2. Lets the user choose a mode of transport and a travel class.
3. Shows a seat map in the terminal with the seat number on every seat and a cross on booked seats.
4. Calculates a fare from the real distance between the cities.
5. Takes payment through a mock gateway and prints a ticket with a PNR.
6. Saves bookings to a file so they are still there after a restart, and allows viewing and cancelling them.

---

## 2. Features

### 2.1 Transport modes and classes

| Mode | Travel classes | Seat style |
|---|---|---|
| Bus | Seater, AC Seater, Sleeper, AC Sleeper | Chairs; sleepers on Lower and Upper deck |
| Train | Chair Car, Non-AC Sleeper, AC 2-Tier, AC 1-Tier | Chairs; berths on Lower and Upper deck |
| Flight | Economy, Business | Chairs in a single cabin |

### 2.2 Seats per class

| Class | Seats | Class | Seats |
|---|---|---|---|
| Bus Seater | 40 | Train Non-AC Sleeper | 48 |
| Bus AC Seater | 40 | Train AC 2-Tier | 36 |
| Bus Sleeper | 36 | Train AC 1-Tier | 20 |
| Bus AC Sleeper | 36 | Flight Economy | 72 |
| Train Chair Car | 50 | Flight Business | 16 |

### 2.3 Seat map colours

| Colour | Meaning |
|---|---|
| Green | Available, standard class |
| Yellow | Available, premium class (any AC class and Business) |
| Grey with X | Already booked (the seat number stays visible) |
| Cyan | Selected by the user |

### 2.4 Cities

50 cities in three groups: **8 Tier 1 cities**, **27 Tier 2 cities** and **15 major tourist destinations**, each with its tourist places. Modes are only offered where they exist (for example, no train for Leh, no flight for Haridwar).

### 2.5 Payment gateway (mock)

| Option | What happens |
|---|---|
| Credit card | Asks for pseudo card details, number is masked, CVV never shown |
| Debit card | Same as credit card with a different fee |
| UPI | Shows a pseudo QR code, then Enter simulates the payment |
| Net banking | Asks for account number and IFSC code |
| Pay at arrival (cash) | Confirms directly and marks the ticket as pay at arrival |

### 2.6 Booking management

1. **View Ticket by PNR** (works with `TRV-10001` and `10001`).
2. **My Bookings**: list all bookings, open one from the list, or search by passenger name.
3. **Cancel Ticket** with a refund: more than 24 hours before departure 90%, 6 to 24 hours 50%, under 6 hours 0%.
4. Seats become available again after a cancellation.

### 2.7 Safety

All input is validated. The program does not crash on wrong input, and it exits cleanly if the input ends.

---

## 3. Requirements: JDK Installation

The project needs the **JDK (Java Development Kit)**, not only the JRE, because it uses `javac` to compile. **JDK 17 or newer** is required. No preview features are used.

### 3.1 Install

1. Download a JDK from **Eclipse Temurin** (https://adoptium.net) or **Oracle** (https://www.oracle.com/java/technologies/downloads/).
2. Run the installer. On Windows, tick the options that add Java to `PATH` and set `JAVA_HOME`.
3. Close and reopen the terminal.

### 3.2 Verify

```
java -version
javac -version
```

Both commands must print a version number (17 or higher). If `javac` is not recognised, Java is not on your `PATH`.

---

## 4. Clone and Setup

### 4.1 Clone the repository

```
git clone https://github.com/dakshnarwaria/Travel-Booking-System.git
cd Travel-Booking-System
```

Tip: keep the project outside a OneDrive-synced folder if possible, because OneDrive can lock files while syncing.

### 4.2 Windows terminal setup (do this in every new terminal)

The program prints box characters and the rupee symbol. Run these two lines first, otherwise the output looks garbled:

```
chcp 65001
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

### 4.3 Compile

**Windows PowerShell:**

```
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
```

**macOS / Linux:**

```
rm -rf out
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

Compilation must end without errors. The compiled classes go into the `out` folder.

---

## 5. How to Run and Check Every Part

Run all commands from the project root, after compiling. Dates use the format `dd-MM-yyyy`, so use a future date.

### 5.1 Command reference

| No. | What to check | Command |
|---|---|---|
| 1 | Full interactive booking system | `java -cp out com.travel.Main --menu` |
| 2 | Seat maps of all 10 classes | `java -cp out com.travel.Main --demo` |
| 3 | Seat map of one class | `java -cp out com.travel.Main --demo "Bus AC Sleeper"` |
| 4 | All 50 cities with tourist places | `java -cp out com.travel.Main --cities` |
| 5 | Trips on a route | `java -cp out com.travel.Main --trips Hyderabad Bengaluru bus 05-11-2026` |
| 6 | Fares of every class on a route | `java -cp out com.travel.Main --fares Hyderabad Bengaluru 05-11-2026` |
| 7 | Built-in tests | `java -cp out com.travel.Main --test` |
| 8 | Usage help | `java -cp out com.travel.Main` |

### 5.2 Optional flags

| Flag | Effect |
|---|---|
| `--ascii` | Draws boxes with plain `+ - \|` characters, for old terminals |
| `--nocolor` | Turns colours off |
| `--now "dd-MM-yyyy HH:mm"` | Overrides the current time, used to test the refund rules (use with `--menu`) |

### 5.3 Routes worth trying

| Mode | Command | What to notice |
|---|---|---|
| Bus | `--trips Hyderabad Bengaluru bus 05-11-2026` | About 625 km by road, 3 trips |
| Train | `--trips Mumbai Pune train 05-11-2026` | Short route, so only Chair Car and Non-AC Sleeper |
| Flight | `--trips Delhi Mumbai flight 05-11-2026` | Economy and Business, about 1,148 km |
| Unavailable | `--trips Leh Delhi train 05-11-2026` | Clear "unavailable" message |

### 5.4 Expected fares (Hyderabad to Bengaluru)

| Class | Total fare |
|---|---|
| Bus Seater | ₹1,159 |
| Bus AC Sleeper | ₹2,189 |
| Flight Business | ₹9,646 |

### 5.5 Walk-through of the main menu

```
java -cp out com.travel.Main --menu
```

1. Choose **1 Book Travel Ticket** and follow the journey in section 6.
2. Try an already booked seat and a wrong seat number to see the error handling.
3. Pay with different payment methods in different bookings.
4. Note the **PNR** on the ticket, then exit.
5. Start the program again and open **3 My Bookings**. The booking is still there.
6. Use **2 View Ticket by PNR** and **4 Cancel Ticket** to see the refund.

---

## 6. User Journey

| Step | Screen | What the user does |
|---|---|---|
| 1 | Main menu | Book Travel Ticket / View Ticket by PNR / My Bookings / Cancel Ticket / Exit |
| 2 | Route | Types source and destination city (first letters are enough) |
| 3 | Date | Enters the travel date |
| 4 | Mode | Bus, train or flight (only available modes are shown) |
| 5 | Class | Picks a class, with the starting fare of each |
| 6 | Trip | Picks a trip: operator, departure, arrival, seats left |
| 7 | Seat map | Types seat numbers, for example `L1,L2` or `1A,1B` (up to 6) |
| 8 | Passengers | Enters name, age and gender for each seat |
| 9 | Fare summary | Sees the full fare breakdown and confirms |
| 10 | Payment | Chooses card, UPI, net banking or cash |
| 11 | Result | Green success box, ticket with PNR, updated seat map with crosses |
| 11b | Failure | Red box if the payment is cancelled, and no seat is booked |

---

## 7. Pricing Rules

### 7.1 Formula

```
distanceKm = haversine(city coordinates) x road factor   (bus 1.25, train 1.20, flight 1.00)
baseFare   = max(minFare, distanceKm x ratePerKm)
total      = baseFare + reservation or airport fee + GST + convenience fee + payment fee + add-ons
```

### 7.2 Rates

| Mode | Class | Rate (₹ per km) | Minimum fare (₹) |
|---|---|---|---|
| Bus | Seater | 1.8 | 250 |
| Bus | AC Seater | 2.4 | 350 |
| Bus | Sleeper | 2.8 | 400 |
| Bus | AC Sleeper | 3.4 | 500 |
| Train | Chair Car | 1.3 | 200 |
| Train | Non-AC Sleeper | 0.8 | 150 |
| Train | AC 2-Tier | 2.4 | 500 |
| Train | AC 1-Tier | 4.0 | 900 |
| Flight | Economy | 5.0 | 2,500 |
| Flight | Business | 14.0 | 8,000 |

### 7.3 Fees and taxes

| Item | Value |
|---|---|
| Bus convenience fee | 3% |
| Train reservation fee | ₹20 to ₹60 depending on class |
| Train GST | 5% on Chair Car and AC classes, none on Non-AC Sleeper |
| Flight airport fee | ₹300 |
| Flight GST | 5% Economy, 12% Business |
| Flight exit-row seat | +₹400 |

### 7.4 Payment fees

| Payment | Fee |
|---|---|
| Credit card | 1.8% |
| Debit card | 1.0% |
| UPI | ₹0 |
| Net banking | ₹12 flat |
| Cash (pay at arrival) | ₹30 flat |

Fares and city data are realistic approximations for a project. They are not live prices.

---

## 8. Project Structure

```
Travel-Booking-System/
  README.md
  PLAN.md                    design plan: class diagram, layouts, pricing, cities
  .gitignore
  data/
    cities.csv               50 cities with coordinates and tourist places
    bookings.dat             saved bookings (created automatically)
  test_*.txt                 piped input files for scripted menu tests
  src/com/travel/
    Main.java                entry point and command line arguments
    TestRunner.java          built-in tests (--test)
    enums/                   TransportType, SeatClass, SeatType, SeatStatus, PaymentMode, BookingStatus
    interfaces/              Displayable, Bookable, Payable, Priceable
    model/                   City, Route, Trip, Passenger, Booking, Ticket
    seat/                    Seat, ChairSeat, SleeperSeat, BerthSeat
    layout/                  SeatLayout, ChairLayout, SleeperDeckLayout, BerthBayLayout, CabinLayout, FlightCabinLayout
    transport/               Transport, Bus, Train, Flight
    payment/                 Payment, CardPayment, CreditCardPayment, DebitCardPayment, UpiPayment, NetBankingPayment, CashPayment, PaymentFactory
    service/                 SearchService, PricingService, BookingService, PaymentService
    repository/              CityRepository, BookingRepository
    exception/               TravelException and its subclasses
    ui/                      ConsoleMenu, SeatMapRenderer
    util/                    ConsoleColors, InputHelper, IdGenerator, DistanceCalculator
```

Helper classes such as the clock and refund policy sit in the package where they fit best. Check the `src` folder for the exact final list.

---

## 9. Class Diagram

```
INTERFACES
  Displayable : render()
  Bookable    : book(), cancel(), isAvailable()
  Payable     : pay(), getProcessingFee()
  Priceable   : calculateFare()

SEATS
  Seat (abstract) implements Displayable, Bookable, Serializable
   |-- ChairSeat
   |-- SleeperSeat
        |-- BerthSeat

LAYOUTS
  SeatLayout (abstract)
   |-- ChairLayout
   |-- SleeperDeckLayout
   |-- BerthBayLayout
   |-- CabinLayout
   |-- FlightCabinLayout

TRANSPORT
  Transport (abstract) implements Priceable
   |-- Bus
   |-- Train
   |-- Flight

PAYMENT (strategy pattern)
  Payment (abstract) implements Payable
   |-- CardPayment (abstract)
   |     |-- CreditCardPayment
   |     |-- DebitCardPayment
   |-- UpiPayment
   |-- NetBankingPayment
   |-- CashPayment

EXCEPTIONS
  TravelException
   |-- SeatAlreadyBookedException
   |-- InvalidSeatException
   |-- PaymentFailedException
   |-- RouteNotFoundException
  InvalidInputException (unchecked)

RELATIONSHIPS
  Trip     = Transport + Route + date + times
  Route    has two City objects and a distance
  Booking  has a Trip, a class, seats, passengers, a Payment and a status
  Ticket   is generated from a Booking
```

---

## 10. Testing

### 10.1 Built-in tests

```
java -cp out com.travel.Main --test
```

Prints PASS or FAIL for each check and ends with a summary such as `X of Y tests passed`. It checks:

1. Seat counts of all 10 classes.
2. Fare calculation for known routes.
3. The flight exit-row surcharge.
4. Booking conflicts (`SeatAlreadyBookedException`, `InvalidSeatException`).
5. Payment fees for all payment types.
6. Refund tiers (90%, 50%, 0%).
7. PNR lookup with and without the `TRV-` prefix.

The tests do not touch `data/bookings.dat`.

### 10.2 Scripted menu tests (piped input)

In **PowerShell**, pipe an input file into the menu (the `<` redirect does not work in PowerShell):

```
Get-Content test_card.txt | java -cp out com.travel.Main --menu
```

In **cmd, macOS or Linux**:

```
java -cp out com.travel.Main --menu < test_card.txt
```

| Input file | What it tests |
|---|---|
| `test_card.txt` | Card payment, green success box, ticket and PNR |
| `test_upi.txt` | UPI payment with the pseudo QR code |
| `test_netbanking.txt` | Net banking payment |
| `test_cash.txt` | Pay at arrival booking |
| `test_cancel.txt` | Cancelled payment, red box, no seats booked |

Each file holds one answer per line and ends with the Exit option. When input ends, the program prints "Input ended. Exiting." and stops.

### 10.3 Manual checklist

1. All 10 seat maps print with the correct seat counts.
2. Premium classes are yellow, standard classes are green, booked seats are grey with an X.
3. Flight exit rows cost ₹400 more.
4. A wrong seat and an already booked seat give an error and allow a retry.
5. A bad age or empty name is rejected.
6. The fare summary matches the fare table.
7. The success box and ticket (with PNR) appear after payment.
8. A booking is still listed after restarting the program.
9. After a cancellation, the seats are available again.

---

## 11. Java and OOPS Concepts Used

### 11.1 Core OOPS concepts

| No. | Concept | Where it is used |
|---|---|---|
| 1 | Abstraction | Abstract classes `Seat`, `SeatLayout`, `Transport`, `Payment`; interfaces `Displayable`, `Bookable`, `Payable`, `Priceable` |
| 2 | Encapsulation | Private fields with getters, setters and validation in `Seat`, `Passenger`, `Booking`, `Payment` |
| 3 | Inheritance (single, multilevel, hierarchical) | `Seat` → `SleeperSeat` → `BerthSeat`; `Transport` → `Bus` / `Train` / `Flight`; `Payment` → `CardPayment` → `CreditCardPayment` / `DebitCardPayment` |
| 4 | Multiple inheritance through interfaces | `Seat` implements `Displayable`, `Bookable` and `Serializable` |
| 5 | Runtime polymorphism (overriding) | `render()` per seat type, `pay()` and `getProcessingFee()` per payment type |
| 6 | Compile-time polymorphism (overloading) | Overloaded `bookSeat(...)` methods in `BookingService` |
| 7 | Constructors, `this`, `super`, chaining | Seat, transport, payment and model classes |
| 8 | Interfaces with default and static methods | `Priceable` (GST helper) and the other interfaces |
| 9 | Composition and aggregation | `Booking` has a `Trip`, seats, passengers, a `Payment`; `Route` has two `City` objects |

### 11.2 Java language features

| No. | Feature | Where it is used |
|---|---|---|
| 1 | Packages and access modifiers | The whole `com.travel.*` structure |
| 2 | `static`, `final`, static blocks | `ConsoleColors` constants, `IdGenerator` counter, final utility classes |
| 3 | Enums with fields and methods | `SeatClass` (rate, minimum fare, premium flag), `PaymentMode`, `TransportType` |
| 4 | Custom exceptions, `try` / `catch` / `finally` | The `TravelException` tree and `InvalidInputException` |
| 5 | Collections and generics | `List`, `Map<SeatClass, SeatLayout>`, `EnumMap`, `HashMap`, `ArrayList` |
| 6 | `Object` class methods | `equals`, `hashCode`, `toString` in `Seat`, `City`, `Booking` |
| 7 | Varargs | `bookSeats(String... seatNumbers)` |
| 8 | Inner, nested and anonymous classes, lambdas | Renderer helpers, `Comparator` lambda for sorting trips |
| 9 | `instanceof` and casting | The renderer checks `SleeperSeat` versus `ChairSeat` |
| 10 | File handling and serialization | `BookingRepository` (`data/bookings.dat`), `CityRepository` (`data/cities.csv`) |
| 11 | Date and time API | `LocalDate`, `LocalDateTime` for travel dates, times and refund rules |
| 12 | Seeded `Random` | Repeatable trip generation and pre-sold seats |
| 13 | Command line arguments | `Main` (`--menu`, `--demo`, `--cities`, `--trips`, `--fares`, `--test`, `--now`) |

### 11.3 Design patterns

| No. | Pattern | Where |
|---|---|---|
| 1 | Strategy | The payment types, each with its own `pay()` and fee logic |
| 2 | Factory | `PaymentFactory` creates a `Payment` from a `PaymentMode` |
| 3 | Singleton | `CityRepository`, `BookingRepository` |

The tables follow the design plan in `PLAN.md`. If a class was renamed during development, the file names in `src/com/travel` are the final reference.

---

**Repository:** https://github.com/dakshnarwaria/Travel-Booking-System
