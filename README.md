# EventBoard Server (Java TCP Event Management System)

![Java](https://img.shields.io/badge/Java-17%2B-007396?logo=java&logoColor=white)
![Sockets](https://img.shields.io/badge/Networking-TCP%20Sockets-blue)
![Threads](https://img.shields.io/badge/Concurrency-Multithreading-orange)
![Status](https://img.shields.io/badge/Status-Completed-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow)

A lightweight, multi-threaded **TCP server** for managing event data through a custom text-based protocol.  
Designed to showcase backend fundamentals: networking, concurrency, error handling, shared state, and protocol design.

---

## Features

- Multi-threaded — **one thread per client connection**
- Shared, thread-safe **EventStore** instance
- Custom protocol supporting:
  - `add; date; time; description`
  - `remove; date; time; description`
  - `list; date; -; -`
  - `STOP`
- Robust input validation using a custom `InvalidCommandException`
- Automatic chronological sorting of events based on parsed times
- Clean, predictable responses for both success and error cases

---

## Tech Stack

| Area       | Technology                                  |
|-----------|----------------------------------------------|
| Language  | Java 17+                                     |
| Networking| TCP sockets (`ServerSocket`, `Socket`)       |
| Concurrency | Multi-threading (`Thread`, `Runnable`)     |
| Storage   | In-memory `HashMap<String, List<Event>>`     |
| Validation | Custom protocol parsing + checked exception |

---

## Project Structure

    eventboard-server/
    ├─ EventServerMain.java       # Starts the TCP server / accepts clients
    ├─ ClientHandler.java         # One-thread-per-client command processor
    ├─ EventStore.java            # Thread-safe shared storage of events
    ├─ Event.java                 # Simple event model (date, time, description)
    └─ InvalidCommandException.java

---

## Running the Server

Compile the project (from the folder that contains the `eventboard` package):

    javac eventboard/*.java

Run the server:

    java eventboard.EventServerMain

The server starts on **port 5550** and waits for client connections.

---

## Architecture Overview

### 1. Server Entry Point (`EventServerMain`)

- Creates a `ServerSocket` on port **5550**.
- Creates a single shared `EventStore` instance.
- Runs an infinite loop:
  - `accept()` waits for an incoming connection.
  - For every incoming connection, it creates:
    - one `Socket` for that client
    - one `ClientHandler` linked to the shared `EventStore`
    - one `Thread` wrapping the `ClientHandler` and starts it

This architecture demonstrates proper concurrency management using a shared in-memory store and one thread per client.

### 2. ClientHandler

Each `ClientHandler`:

- Reads text commands sent by the client over TCP.
- Trims and validates the incoming line.
- Applies protocol rules:
  - Command must not end with `;`.
  - It must contain **exactly four fields** separated by `;`:
    `action; date; time; description`.
- Routes the request to `EventStore` and returns a formatted response.
- Catches `InvalidCommandException` and sends back human-readable error messages.
- Terminates when the client sends `STOP`.

### 3. EventStore

`EventStore` is the server-side source of truth for all events. It:

- Stores events in a `HashMap<String, List<Event>>` where the key is the **date**.
- Provides `synchronized` methods to guarantee thread safety:
  - `addEvent(Event e)`
  - `removeEvent(String date, String time, String description)`
  - `listEvents(String date)`
- Parses and validates time strings with `timeToMinutes(String time)`:
  - Accepts formats like `6 pm` and `7.30 pm`.
  - Checks for `am` / `pm`, valid hour and minute ranges.
  - Converts times to minutes from midnight for easy comparison.
- Sorts events chronologically within each date.
- Formats all events of a date into a single line like:

      2 November 2025; 12 pm, Concert; 6 pm, Food Hall

This ensures consistent server replies for all clients.

### 4. Event Model

`Event` is a simple, focused model class that contains:

- `date`
- `time`
- `description`

Its `toString()` method returns:

    time + ", " + description

This is used when building formatted responses inside `EventStore`.

### 5. InvalidCommandException

A custom checked exception used to signal:

- Wrong number of fields in the command.
- Empty date, time, or description where not allowed.
- Unsupported actions (commands different from add/remove/list).
- Incorrect time formats or out-of-range values.

It helps to separate validation logic from transport logic and keeps error handling explicit and readable.

---

## Demonstrated Backend Skills

- TCP networking with `ServerSocket` and `Socket`
- Multi-threaded design (one thread per client)
- Shared-memory synchronization with `synchronized` methods
- Custom text-based protocol design and validation
- Clean, modular object-oriented design
- Error handling with a custom exception type

---

## Possible Improvements

- Persist events to disk (e.g. JSON, CSV, or SQLite)
- Add update/edit command for existing events
- Add a command to list all dates with events
- Introduce logging using SLF4J or Log4j
- Replace per-client manual threads with an `ExecutorService` thread pool
- Add unit tests for `EventStore` and `timeToMinutes`

---

## 👤 Author

**Leandro Crisol — Student No. 23156503**  
BSc (Honours) in Computing — National College of Ireland
