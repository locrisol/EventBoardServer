/*
 * Advanced Programming – CA1
 * Student Name: Leandro Crisol
 * Student ID: 23156503
 *
 * Class: EventStore
 * 
 * Core data management class that stores events using a HashMap keyed by
 * date. Validates event times, adds and removes events, retrieves lists,
 * and ensures events are always sorted chronologically using a Comparator.
 */
package eventboard;

import java.util.*;

/**
 *
 * @author Leandro
 */
public class EventStore {

    // Map: key = date, value = list of events for that date
    // Best way to store and retrieve events by date in constant time
    private Map<String, List<Event>> eventsByDate = new HashMap<>();

    // Time parser to convert time format like "6 pm" or "7.30 pm" to minutes
    // Used only to sort events by time
    private int timeToMinutes(String time) throws InvalidCommandException {
        if (time == null || time.trim().isEmpty()) {
            throw new InvalidCommandException("time is empty");
        }

        String t = time.trim().toLowerCase();
        String[] parts = t.split("\\s+"); // ["6", "pm"] or ["7.30", "pm"]

        if (parts.length != 2) {
            throw new InvalidCommandException("time must be like '6 pm' or '7.30 pm'");
        }

        String numberPart = parts[0];    // "6" or "7.30"
        String ampm = parts[1];          // "am" or "pm"

        if (!ampm.equals("am") && !ampm.equals("pm")) {
            throw new InvalidCommandException("time must end with 'am' or 'pm'");
        }

        int hour;
        int minute = 0;

        try {
            if (numberPart.contains(".")) {
                String[] hm = numberPart.split("\\.");
                hour = Integer.parseInt(hm[0]);
                minute = Integer.parseInt(hm[1]);
            } else {
                hour = Integer.parseInt(numberPart);
            }
        } catch (NumberFormatException e) {
            throw new InvalidCommandException("time must be like '6 pm' or '7.30 pm'");
        }

        if (hour < 1 || hour > 12 || minute < 0 || minute > 59) {
            throw new InvalidCommandException("time out of range");
        }

        // Convert to 24h minutes format
        if (ampm.equals("pm") && hour != 12) {
            hour += 12;
        }
        if (ampm.equals("am") && hour == 12) {
            hour = 0;
        }

        return hour * 60 + minute;
    }

    // Add new event, return list of all events for that date
    public synchronized List<Event> addEvent(Event e) throws InvalidCommandException {
        // Check if time format is valid
        timeToMinutes(e.getTime());

        // Checks if there is any event with that same date previously created
        // If not, create a new List to be stored in the HashMap
        List<Event> list = eventsByDate.get(e.getDate());
        if (list == null) {
            list = new ArrayList<>();
            eventsByDate.put(e.getDate(), list);
        }
        list.add(e);

        // Sort events by time using the built-in comparator
        sortEventsByTime(list);

        // Return a copy so the sorter can’t modify the internal list
        return new ArrayList<>(list);
    }

    // Remove event, if not found -> throw InvalidCommandException
    public synchronized List<Event> removeEvent(String date, String time, String description) throws InvalidCommandException {

        timeToMinutes(time); // checks if time format is valid

        List<Event> list = eventsByDate.get(date);
        if (list == null || list.isEmpty()) {
            throw new InvalidCommandException("event not found for removal");
        }

        boolean removed = false;
        for (int i = 0; i < list.size(); i++) {
            Event e = list.get(i);
            boolean timeIsSame = e.getTime().equalsIgnoreCase(time.trim());
            boolean descriptionIsSame = e.getDescription().equalsIgnoreCase(description.trim());

            if (timeIsSame && descriptionIsSame) {
                list.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed) {
            throw new InvalidCommandException("event not found for removal");
        }

        if (list.isEmpty()) {
            eventsByDate.remove(date);
            return new ArrayList<>();
        }

        // sort again after deletion
        sortEventsByTime(list);

        return new ArrayList<>(list);
    }

    // List all events for a date (can be empty)
    public synchronized List<Event> listEvents(String date) {
        List<Event> list = eventsByDate.get(date);
        if (list == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(list);
    }

    // Helper to make sure events on same date are formatted like
    // "2 November 2024; 12 pm, Concert; 6 pm, Food Hall"
    public String formatEvents(String date, List<Event> events) {
        StringBuilder sb = new StringBuilder();
        sb.append(date).append("; ");
        if (events.isEmpty()) {
            sb.append("no events");
        } else {
            for (int i = 0; i < events.size(); i++) {
                sb.append(events.get(i).toString());
                if (i < events.size() - 1) {
                    sb.append("; ");
                }
            }
        }
        return sb.toString();
    }

    // Private method to sort event by time using the built-in comparator
    // Use a custome Comparator by comparing time in minutes from 2 events,
    // then Collections will sort them based on the results
    private void sortEventsByTime(List<Event> list) {
        Collections.sort(list, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                try {
                    int t1 = timeToMinutes(e1.getTime());
                    int t2 = timeToMinutes(e2.getTime());
                    return t1 - t2; // smaller/earlier time first
                } catch (InvalidCommandException ex) {
                    // If time is invalid, keep the original order
                    return 0;
                }
            }
        });
    }
}
