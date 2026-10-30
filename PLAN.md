# Travel Ticket Booking System (Java OOPS): Master Plan

Fares and city data below are realistic approximations based on typical RedBus, IRCTC and domestic airline ranges. They are calibrated to the reference screenshot (₹899 chair seat, ₹1,399 sleeper on a roughly 500 km bus route). They are not live prices, which is fine for a project.

## 1. Final decisions

- Java 25 (LTS), no preview features. Keep the code compatible with Java 17 (do not use features newer than 17) in case the college lab has an older JDK.
- Plain console app, no libraries, no framework, no GUI.
- One class per file, organised in packages.
- Seat maps drawn with Unicode box characters and ANSI colours, with an ASCII fallback switch for old terminals.
- Each trip holds a separate seat inventory per class. The user picks the class first, then sees only that class's seat map.
- Travel date is asked (dd-MM-yyyy). Availability is generated per trip and date using a fixed random seed, so about 35% of seats are already sold and the same trip always looks the same.
- Bookings persist in data/bookings.dat using Java serialization. Cities load from data/cities.csv.
- Premium rule: any AC class or Business class is premium (yellow). Everything else is standard (green).

## 2. Package structure

```
src/com/travel/
  Main.java
  enums/       TransportType, SeatClass, SeatType, SeatStatus, PaymentMode, BookingStatus
  interfaces/  Displayable, Bookable, Payable, Priceable
  model/       City, Route, Passenger, Booking, Ticket, Trip
  seat/        Seat, ChairSeat, SleeperSeat, BerthSeat
  layout/      SeatLayout, ChairLayout, SleeperDeckLayout, BerthBayLayout, CabinLayout, FlightCabinLayout
  transport/   Transport, Bus, Train, Flight
  payment/     Payment, CardPayment, CreditCardPayment, DebitCardPayment, UpiPayment, NetBankingPayment, CashPayment, PaymentFactory
  service/     SearchService, PricingService, BookingService, PaymentService
  repository/  CityRepository, BookingRepository
  exception/   TravelException, SeatAlreadyBookedException, InvalidSeatException, PaymentFailedException, InvalidInputException, RouteNotFoundException
  ui/          ConsoleMenu, SeatMapRenderer
  util/        ConsoleColors, InputHelper, IdGenerator, DistanceCalculator
```

## 3. Class diagram (inheritance tree and relationships)

```
INTERFACES
  Displayable  : render()
  Bookable     : book(), cancel(), isAvailable()
  Payable      : pay(amount), getProcessingFee(amount)
  Priceable    : calculateFare()

SEAT HIERARCHY (abstract + multilevel inheritance)
  Seat (abstract) implements Displayable, Bookable, Serializable
   |-- ChairSeat
   |-- SleeperSeat
        |-- BerthSeat   (adds berthType: LB, UB, SL, SU)

LAYOUT HIERARCHY (abstract)
  SeatLayout (abstract)  has-a List<Seat>, decks, rows
   |-- ChairLayout          (bus seater, train chair car, flight)
   |-- SleeperDeckLayout    (bus sleeper: lower + upper deck)
   |-- BerthBayLayout       (train non-AC sleeper, AC 2-tier)
   |-- CabinLayout          (train AC 1-tier private cabins)

TRANSPORT HIERARCHY (abstract)
  Transport (abstract) implements Priceable
   |-- Bus      has Map<SeatClass, SeatLayout>
   |-- Train
   |-- Flight

PAYMENT HIERARCHY (abstract, strategy pattern)
  Payment (abstract) implements Payable
   |-- CardPayment (abstract)
   |     |-- CreditCardPayment
   |     |-- DebitCardPayment
   |-- UpiPayment
   |-- NetBankingPayment
   |-- CashPayment

MODEL / RELATIONSHIPS
  Trip        = Transport + Route + date + departure/arrival time
  Route       has-a City source, City destination, distanceKm
  Booking     has-a Trip, SeatClass, List<Seat>, List<Passenger>, Payment, BookingStatus  (composition)
  Ticket      is generated from Booking (PNR, printable)

EXCEPTIONS
  TravelException (checked)
   |-- SeatAlreadyBookedException
   |-- InvalidSeatException
   |-- PaymentFailedException
   |-- RouteNotFoundException
   InvalidInputException (unchecked, extends RuntimeException)
```

### OOPS coverage map (viva cheat sheet)

| Concept | Where it appears |
|---|---|
| Abstraction | Seat, SeatLayout, Transport, Payment abstract classes; interfaces |
| Encapsulation | Private fields with getters/setters and validation in Seat, Passenger, Booking |
| Inheritance (single, multilevel, hierarchical) | Seat → SleeperSeat → BerthSeat; Transport → Bus/Train/Flight |
| Multiple inheritance via interfaces | Seat implements Displayable + Bookable + Serializable |
| Runtime polymorphism (overriding) | seat.render(), payment.pay(), transport.getSeatLayout() |
| Compile-time polymorphism (overloading) | bookSeat(String) / bookSeat(String, Passenger) / bookSeat(List) |
| Constructors, this, super, chaining | All model and seat classes |
| static, final, static blocks | ConsoleColors constants, IdGenerator counter, Main config |
| Enums with fields and methods | SeatClass (rate, premium flag), PaymentMode |
| Inner, nested, anonymous classes, lambdas | SeatMapRenderer helper class, Comparator for sorting trips |
| Exception handling | Custom checked and unchecked exceptions, try/catch/finally |
| Collections and generics | ArrayList, HashMap, EnumMap, Map<SeatClass, SeatLayout> |
| Object class methods | equals/hashCode/toString in Seat, City, Booking |
| Design patterns | Strategy (payment), Factory (PaymentFactory), Singleton (repositories) |
| Packages and access modifiers | Whole structure above |
| File handling / serialization | BookingRepository |
| Interface default and static methods | Priceable.applyGst(), Displayable |
| instanceof and casting | Renderer checks SleeperSeat vs ChairSeat |
| varargs | bookSeats(String... seatNumbers) |

## 4. Seat layouts per class

Seat numbers: bus sleepers L1.. and U1.. (lower and upper deck), chair seats by row and letter (1A, 1B), trains show seat number plus berth code, flights use 1A to 12F.

| Transport | Class | Seat shape | Arrangement | Decks | Total seats | Colour |
|---|---|---|---|---|---|---|
| Bus | Seater (non-AC) | Chair | 10 rows of 2+2 | Lower only | 40 | Standard |
| Bus | AC Seater | Chair | 10 rows of 2+2 | Lower only | 40 | Premium |
| Bus | Sleeper (non-AC) | Sleeper block | 6 rows of 1+2 per deck | Lower + Upper | 36 | Standard |
| Bus | AC Sleeper | Sleeper block | 6 rows of 1+2 per deck | Lower + Upper | 36 | Premium |
| Train | Chair Car | Chair | 10 rows of 3+2 | Single | 50 | Standard |
| Train | Non-AC Sleeper | Berth | 8 bays x (2 lower + 2 upper + side lower + side upper) | Lower + Upper | 48 | Standard |
| Train | AC 2-Tier | Berth | 6 bays, same pattern as above | Lower + Upper | 36 | Premium |
| Train | AC 1-Tier | Private cabin berth | 5 cabins x 4 berths (2 lower + 2 upper) | Lower + Upper | 20 | Premium |
| Flight | Economy | Chair | 12 rows of 3+3 (A to F) | Single cabin | 72 | Standard |
| Flight | Business | Wide chair | 4 rows of 2+2 (A, C, D, F) | Single cabin | 16 | Premium |

Notes:
- Real trains have 3-tier sleeper berths. We simplify to two decks (lower and upper) so the map stays readable and matches the requirement. Mention this as a design decision in the viva.
- Bus lower deck prints a small (driver) marker at the top, like RedBus.
- Optional stretch layout from the screenshot: Seater + Sleeper hybrid (lower deck 1 sleeper + 2 chairs, upper deck 1 + 2 sleepers, 8 rows each). The layout classes are designed so this is a small addition.
- Optional stretch: Ladies-only seats (pink) like the screenshot, using a gender flag on Seat.

### Seat map look

```
 Lower deck (driver)            Upper deck
 ┌───┐ ┌───┐   ┌───┐ ┌───┐
 │ 1A│ │X1B│   │ 1C│ │ 1D│
 └───┘ └───┘   └───┘ └───┘
 ₹899  Sold    ₹899  ₹899

 ┌───────┐
 │  L12  │       booked sleeper: number stays visible,
 │ ▂▂▂▂▂ │       cross drawn over it, dimmed colour
 └───────┘
   ₹1399
```

Colour legend printed above every map: Standard available (green), Premium available (yellow), Booked (grey with X), Selected (cyan).

## 5. Pricing rules (calibrated to real-world ranges)

Formula:

```
distanceKm = haversine(city lat/long) x roadFactor   (bus 1.25, train 1.20, flight 1.00)
baseFare   = max(minFare, distanceKm x ratePerKm)
total      = baseFare + reservationFee + GST + convenienceFee + paymentProcessingFee
```

| Transport | Class | Rate ₹/km | Min fare ₹ | Extra |
|---|---|---|---|---|
| Bus | Seater | 1.8 | 250 | Convenience 3% |
| Bus | AC Seater | 2.4 | 350 | Convenience 3% |
| Bus | Sleeper | 2.8 | 400 | Convenience 3% |
| Bus | AC Sleeper | 3.4 | 500 | Convenience 3% |
| Train | Chair Car | 1.3 | 200 | Reservation ₹40, GST 5%, convenience ₹20 |
| Train | Non-AC Sleeper | 0.8 | 150 | Reservation ₹20, no GST, convenience ₹20 |
| Train | AC 2-Tier | 2.4 | 500 | Reservation ₹50, GST 5%, convenience ₹40 |
| Train | AC 1-Tier | 4.0 | 900 | Reservation ₹60, GST 5%, convenience ₹40 |
| Flight | Economy | 5.0 | 2,500 | Airport fee ₹300, GST 5%, convenience ₹350 |
| Flight | Business | 14.0 | 8,000 | Airport fee ₹300, GST 12%, convenience ₹350 |

Check against the screenshot: a 400 km straight-line route is about 500 km by road, so bus seater = 500 x 1.8 = ₹900 and sleeper = 500 x 2.8 = ₹1,400.

Seat-level add-ons: flight exit-row seats +₹400, bus window seats +₹50. Different seat classes naturally have different prices because rate is a property of SeatClass.

Payment processing fee (polymorphism): Credit card 1.8%, Debit card 1.0%, UPI ₹0, Net banking ₹12 flat, Cash (pay at arrival) ₹30 handling.

Refund rule on cancellation: more than 24 hours before departure 90%, 6 to 24 hours 50%, under 6 hours 0%.

## 6. Cities and tourist places

Source of the grouping: Tier 1 uses the Government of India HRA X-class cities. Tier 2 are major Y-class and regional hub cities. The third group is major tourist destinations that are not metro or big-hub cities. This gives 50 cities.

### Tier 1 (8)

| City | State | Tourist places |
|---|---|---|
| Delhi | Delhi | India Gate, Red Fort, Qutub Minar, Humayun's Tomb, Lotus Temple |
| Mumbai | Maharashtra | Gateway of India, Marine Drive, Elephanta Caves, CST Station |
| Kolkata | West Bengal | Victoria Memorial, Howrah Bridge, Dakshineswar Temple, Indian Museum |
| Chennai | Tamil Nadu | Marina Beach, Kapaleeshwarar Temple, Fort St. George |
| Bengaluru | Karnataka | Lalbagh, Bangalore Palace, Cubbon Park, Nandi Hills |
| Hyderabad | Telangana | Charminar, Golconda Fort, Ramoji Film City, Hussain Sagar |
| Ahmedabad | Gujarat | Sabarmati Ashram, Adalaj Stepwell, Sidi Saiyyed Mosque |
| Pune | Maharashtra | Shaniwar Wada, Aga Khan Palace, Sinhagad Fort |

### Tier 2 (27)

| City | State | Tourist places |
|---|---|---|
| Jaipur | Rajasthan | Amber Fort, Hawa Mahal, City Palace, Jantar Mantar |
| Lucknow | Uttar Pradesh | Bara Imambara, Rumi Darwaza, The Residency |
| Kanpur | Uttar Pradesh | JK Temple, Kanpur Zoo, Bithoor |
| Nagpur | Maharashtra | Deekshabhoomi, Ramtek Fort, Zero Mile Stone |
| Indore | Madhya Pradesh | Rajwada, Lal Bagh Palace, Sarafa Bazaar |
| Bhopal | Madhya Pradesh | Upper Lake, Taj-ul-Masajid, Sanchi Stupa (nearby) |
| Patna | Bihar | Golghar, Takht Sri Patna Sahib, Bihar Museum |
| Surat | Gujarat | Dumas Beach, Surat Castle, Dutch Garden |
| Vadodara | Gujarat | Laxmi Vilas Palace, Sayaji Baug, Statue of Unity (nearby) |
| Coimbatore | Tamil Nadu | Marudhamalai Temple, Adiyogi at Isha Yoga Center, Siruvani Waterfalls |
| Kochi | Kerala | Fort Kochi, Mattancherry Palace, Marine Drive |
| Visakhapatnam | Andhra Pradesh | RK Beach, Kailasagiri, Araku Valley (nearby) |
| Chandigarh | Chandigarh | Rock Garden, Sukhna Lake, Capitol Complex |
| Guwahati | Assam | Kamakhya Temple, Umananda Island, Brahmaputra Riverfront |
| Bhubaneswar | Odisha | Lingaraj Temple, Udayagiri and Khandagiri Caves, Dhauli Shanti Stupa |
| Thiruvananthapuram | Kerala | Padmanabhaswamy Temple, Kovalam Beach, Napier Museum |
| Mysuru | Karnataka | Mysore Palace, Chamundi Hills, Brindavan Gardens |
| Madurai | Tamil Nadu | Meenakshi Amman Temple, Thirumalai Nayakkar Mahal |
| Nashik | Maharashtra | Trimbakeshwar Temple, Pandavleni Caves, Sula Vineyards |
| Varanasi | Uttar Pradesh | Kashi Vishwanath, Dashashwamedh Ghat, Sarnath |
| Agra | Uttar Pradesh | Taj Mahal, Agra Fort, Fatehpur Sikri |
| Amritsar | Punjab | Golden Temple, Jallianwala Bagh, Wagah Border |
| Ranchi | Jharkhand | Hundru Falls, Jagannath Temple, Rock Garden |
| Raipur | Chhattisgarh | Nandan Van Zoo, Mahant Ghasidas Museum, Purkhouti Muktangan |
| Dehradun | Uttarakhand | Robber's Cave, Sahastradhara, Mindrolling Monastery |
| Jodhpur | Rajasthan | Mehrangarh Fort, Umaid Bhawan Palace, Jaswant Thada |
| Udaipur | Rajasthan | City Palace, Lake Pichola, Fateh Sagar Lake, Saheliyon ki Bari |

### Major tourist destinations (15)

| City | State / UT | Tourist places |
|---|---|---|
| Goa (Panaji) | Goa | Baga Beach, Basilica of Bom Jesus, Fort Aguada |
| Shimla | Himachal Pradesh | The Ridge, Mall Road, Kufri |
| Manali | Himachal Pradesh | Rohtang Pass, Solang Valley, Hadimba Temple |
| Rishikesh | Uttarakhand | Laxman Jhula, Triveni Ghat, river rafting |
| Haridwar | Uttarakhand | Har Ki Pauri, Mansa Devi Temple |
| Darjeeling | West Bengal | Tiger Hill, Batasia Loop, Himalayan Toy Train |
| Gangtok | Sikkim | Tsomgo Lake, Rumtek Monastery, MG Marg |
| Ooty | Tamil Nadu | Botanical Garden, Ooty Lake, Doddabetta Peak |
| Munnar | Kerala | Tea gardens, Eravikulam National Park, Mattupetty Dam |
| Pondicherry | Puducherry | Promenade Beach, Auroville, French Quarter |
| Jaisalmer | Rajasthan | Jaisalmer Fort, Sam Sand Dunes, Patwon ki Haveli |
| Leh | Ladakh | Pangong Lake, Shanti Stupa, Leh Palace |
| Srinagar | Jammu and Kashmir | Dal Lake, Mughal Gardens, Shankaracharya Temple |
| Puri | Odisha | Jagannath Temple, Puri Beach, Konark Sun Temple (nearby) |
| Mount Abu | Rajasthan | Dilwara Temples, Nakki Lake, Sunset Point |

### Mode availability rules (simplified for the project)

- Bus: all 50 cities.
- Train: all cities except Leh, Gangtok, Manali, Munnar.
- Flight: all cities except Haridwar, Rishikesh, Manali, Darjeeling, Ooty, Munnar, Mount Abu, Puri.
- If a mode is not available between the chosen cities, hide it from the menu and say why.
- The CSV has columns: id, name, state, tier, latitude, longitude, hasAirport, hasStation, touristPlaces (separated by |). Coordinates are filled in by Antigravity from its knowledge. Spot-check 5 to 6 of them on Google Maps, since AI can make small errors.

## 7. User journey in the console

```
1. Main menu: Book ticket / View ticket by PNR / Cancel ticket / Exit
2. Enter source city (type to search or pick by number), destination city
3. Enter travel date
4. Choose mode: Bus / Train / Flight (only available ones shown)
5. Choose class (shows starting fare for each class)
6. Choose a trip (operator, departure, arrival, seats left, fare)
7. Seat map appears: pick seat numbers (up to 6)
8. Enter passenger details for each seat
9. Fare summary
10. Payment gateway: Card / UPI / Net banking / Cash
11. Success screen with ticket + PNR, seat map redrawn with new crosses
```

## 8. Phased build plan

| Phase | Goal | Deliverable you verify |
|---|---|---|
| 1 | Foundation: enums, interfaces, exceptions, utils | Compiles; small demo prints colour legend |
| 2 | Seats, layouts, renderer | Printed seat maps for all 10 classes with crosses and colours (hard-coded demo) |
| 3 | Cities, routes, transports, trip generation | City list loaded; trips listed for a route |
| 4 | Pricing and search flow | Menu runs up to trip selection with correct fares |
| 5 | Booking flow | Seat selection, passenger details, booking, crosses appear |
| 6 | Payment gateway | All four payment modes work |
| 7 | Persistence, tickets, cancellation | Restart program and bookings still there |
| 8 | Polish, OOPS proof, README | OOPS coverage report matches Section 3 |

## 9. Prompts for Antigravity

### Master context prompt (paste first, and keep it in the rules or instructions file if your Antigravity version supports one; otherwise paste at the start of each phase)

```
You are building a console-based Java 25 (no preview features, code compatible with Java 17) Travel Ticket Booking System for a college OOPS project. The attached PLAN.md is the single source of truth for packages, class names, hierarchy, seat layouts, pricing and cities. Follow it exactly.

Rules:
1. Only core Java. No external libraries, no frameworks, no GUI.
2. One public class, interface or enum per file, inside the packages listed in PLAN.md under src/com/travel/.
3. Do not merge classes or simplify the hierarchy. The structure must visibly demonstrate OOPS concepts.
4. Put a short comment at the top of every class saying which OOPS concept it demonstrates.
5. Code must compile with javac and run through com.travel.Main.
6. No magic numbers: use constants or enum fields. Validate all user input. The program must never crash on bad input.
7. Before writing any code, summarise your understanding of the plan in 10 lines and wait for my OK.
8. Work on ONE phase only. At the end of the phase: compile, run, paste the real output, list files created, then stop. Do not start the next phase.
```

### Phase 1 prompt

```
PHASE 1: Foundation. Create the folder structure from PLAN.md Section 2.
- enums: TransportType, SeatClass (fields: transportType, displayName, ratePerKm, minFare, premium, with getters; values exactly as in Section 5), SeatType (CHAIR, SLEEPER, BERTH), SeatStatus, PaymentMode, BookingStatus.
- interfaces: Displayable, Bookable, Payable, Priceable (with one default method applyGst).
- exceptions: the full tree from Section 3 with meaningful messages.
- util: ConsoleColors (final class, ANSI constants, plus an ASCII fallback flag), InputHelper (Scanner wrapper with validated readInt in range, readString, readDate dd-MM-yyyy, confirm y/n), IdGenerator (static counter for PNR), DistanceCalculator (haversine).
- Main: print the colour legend (standard green, premium yellow, booked grey with X, selected cyan) to prove colours work.
- Also create build.bat (Windows) and build.sh (Mac/Linux) that compile everything into an out folder and run com.travel.Main.
Stop after compiling and running.
```

### Phase 2 prompt

```
PHASE 2: Seats, layouts, renderer.
- seat: abstract Seat (private fields seatNumber, seatClass, price, status, deckLabel; getters/setters with validation; implements Displayable, Bookable, Serializable; equals/hashCode/toString), ChairSeat, SleeperSeat extends Seat, BerthSeat extends SleeperSeat (berthType LB/UB/SL/SU). Each overrides render() differently.
- layout: abstract SeatLayout plus ChairLayout, SleeperDeckLayout, BerthBayLayout, CabinLayout as in PLAN.md Section 3 and 4, producing exactly the seat counts in Section 4.
- ui/SeatMapRenderer: draws a layout. Chair seats are small boxes with the number inside and price underneath. Sleeper and berth seats are taller boxes. Booked seats keep the number visible with an X drawn over them in grey. Premium available seats yellow, standard green. Show Lower deck and Upper deck side by side when there are two decks (needs about 80 columns), a (driver) marker on bus lower deck, and a legend on top. Add ASCII fallback mode.
- Main (temporary demo): menu to preview all 10 classes, with about 35% seats randomly marked booked.
Stop and show me the real terminal output for every class. I will review the look before we continue.
```

### Phase 3 prompt

```
PHASE 3: Data, transports, trips.
- Create data/cities.csv with the 50 cities in PLAN.md Section 6. Columns: id,name,state,tier,latitude,longitude,hasAirport,hasStation,touristPlaces (| separated). Use accurate coordinates and the availability rules from Section 6.
- model: City, Route (computes distanceKm with DistanceCalculator and road factors), Trip, Passenger.
- repository: CityRepository (singleton, loads the CSV, search by name prefix).
- transport: abstract Transport implements Priceable with a Map<SeatClass, SeatLayout>; Bus, Train, Flight override getTransportType(), road factor and layouts.
- TripGenerator: for a route, mode and date, generate 3 to 4 trips per day with fictional operator names (bus companies, train names with 5 digit numbers, fictional airlines), realistic departure times, travel durations from distance (bus 55 km/h, train 65 km/h, flight 700 km/h plus 30 min), and a seeded Random (seed from trip id + date) that pre-sells about 35% of seats.
- Main (temporary): pick two cities, a mode and a date, print the trips.
Stop after running.
```

### Phase 4 prompt

```
PHASE 4: Pricing and search flow.
- PricingService implementing the exact formula and table in PLAN.md Section 5 (reservation fee, GST, convenience, window seat and exit-row add-ons). Seat prices must be set on each Seat when a layout is created.
- SearchService: filter available modes between two cities, list classes with starting fare, list trips sorted by departure using a Comparator lambda.
- ui/ConsoleMenu: implement steps 1 to 6 of the user journey in PLAN.md Section 7. Show seats left per class.
Stop after running a full demo path and printing fares for 3 different routes so I can sanity check them (for example Hyderabad to Bengaluru bus, Delhi to Mumbai flight, Mumbai to Pune train).
```

### Phase 5 prompt

```
PHASE 5: Booking flow.
- Seat selection screen: show the seat map for the chosen class, user types seat numbers (comma separated, max 6). Reject booked or invalid seats with SeatAlreadyBookedException or InvalidSeatException and let the user retry.
- Passenger details (name, age, gender) per seat with validation.
- Booking, Ticket, BookingService with overloaded bookSeat(...) methods and a varargs bookSeats(String...). Booking gets a PNR from IdGenerator, status PENDING until payment.
- After a booking is confirmed (payment is stubbed as always successful for now), redraw the seat map so the new seats show a cross, and print the available seat count.
Stop after running a full booking.
```

### Phase 6 prompt

```
PHASE 6: Payment gateway (strategy pattern).
- abstract Payment implements Payable with abstract pay() and getProcessingFee(); CardPayment (abstract) with CreditCardPayment and DebitCardPayment, UpiPayment, NetBankingPayment, CashPayment. Processing fees as in PLAN.md Section 5.
- PaymentFactory creates the right Payment from PaymentMode.
- Card: ask card number, name, expiry, CVV. Accept any non-empty characters but mask the number on screen. UPI: show a pseudo QR drawn with block characters generated from the PNR hash, then ask the user to press Enter to simulate payment. Net banking: ask account number and IFSC, accept any non-empty value. Cash: show 'Pay at arrival' and confirm directly.
- PaymentFailedException if the user types cancel. Booking becomes CONFIRMED on success and the seats are marked booked only then.
- Show a final fare summary before payment including the payment mode fee.
Stop after demonstrating all four modes.
```

### Phase 7 prompt

```
PHASE 7: Persistence, tickets, cancellation.
- BookingRepository (singleton) saving bookings and seat states to data/bookings.dat with Java serialization. Load on startup. Seat availability for a trip and date must survive restarts.
- View ticket by PNR: prints a formatted ticket with box characters.
- Cancel ticket: refund rule from PLAN.md Section 5, seats become available again, status CANCELLED.
- Handle a missing or corrupted file gracefully.
Stop after proving: book, exit, restart, view the ticket, cancel it.
```

### Phase 8 prompt

```
PHASE 8: Polish and OOPS proof.
- Review the whole code base against the OOPS coverage map in PLAN.md Section 3. Add anything missing (inner class, anonymous class, static block, interface static method, instanceof with casting, equals/hashCode).
- Fix all compiler warnings and remove dead code.
- Create README.md: how to compile and run (javac and java commands), project structure, class diagram in text form, and a table mapping every OOPS concept to file names and line numbers.
- Add a simple TestRunner (plain Java, no JUnit) that checks pricing, seat booking conflicts, refund rules.
Stop after showing the test output.
```

## 10. Workflow and what to do before prompting Antigravity

### Before the first prompt

1. Save this document as PLAN.md in your project folder (for example TravelBooking/PLAN.md). Antigravity should read it from the workspace, so you don't paste the whole plan into chat.
2. Install a JDK (Java 25 or 17+). In a terminal run `java -version` and `javac -version`. Both must work.
3. Create an empty folder with src and data inside it. Open that folder in Antigravity.
4. Run `git init` and make a first commit. You will commit after every phase so you can roll back when the AI breaks something.
5. Check your terminal supports colours and Unicode. In VS Code or IntelliJ terminals it works. On Windows CMD run `chcp 65001` first, or rely on the ASCII fallback.
6. In Antigravity pick the strongest model available to you. If it offers a Planning mode, use it for Phases 1 to 3 and when something fails.
7. Read Sections 3 to 5 once yourself so you know the design. You are the one explaining it in the viva.

### The loop for every phase

1. Paste the master prompt (first time only) and then the phase prompt.
2. Let it restate the plan, check it matches, say OK.
3. When it finishes, run the program yourself. Do not trust the AI's own claim that it works.
4. Check the output against the Phase table in Section 8.
5. If something is wrong, describe the problem plus the exact output you saw, and ask for a fix in that phase only.
6. Commit to git with a message like `phase-2-seat-rendering`.
7. Write 3 lines of notes per new class in your own words: what it does and which OOPS concept it shows.
8. Only then start the next phase.

### Common problems and what to do

- It dumps everything into one file or skips packages: stop it and repeat rules 2 and 3 of the master prompt.
- Seat map looks broken or misaligned: this is normal in Phase 2. Ask for changes with specifics such as 'sleeper box is 9 characters wide, make gaps 3 characters, align decks at the top'.
- Out of free quota: copy PLAN.md and your latest phase prompt into Claude Code, Cursor or Copilot and continue from the last git commit.
- Wrong coordinates or tourist places: fix the CSV by hand, it is just data.

### Final checklist before submission

- Program compiles with one command and runs from a clean clone.
- All 10 classes show correct seat maps, premium colour differs, crosses show on booked seats.
- All four payment modes work.
- Bookings survive a restart.
- README maps every OOPS concept to a file.
- You can explain every class in the viva without opening the AI.
