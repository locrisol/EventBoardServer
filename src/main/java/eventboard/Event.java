/*
 * Advanced Programming – CA1
 * Student Name: Leandro Crisol
 * Student ID: 23156503
 *
 * Class: Event
 * 
 * Represents a single event containing a date, time, and description.
 * Provides basic getters and a formatted string representation used by the
 * server when listing events.
 */
package eventboard;

/**
 *
 * @author Leandro
 */
public class Event {
    private String date;
    private String time;
    private String description;

    public Event(String date, String time, String description) {
        this.date = date;
        this.time = time;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        // For server  like: "6 pm, Concert Dublin City Centre"
        return time + ", " + description;
    }
}
