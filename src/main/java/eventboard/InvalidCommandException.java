/*
 * Advanced Programming – CA1
 * Student Name: Leandro Crisol
 * Student ID: 23156503
 *
 * Class: InvalidCommandException
 * 
 * Custom checked exception thrown when a command or event input does not
 * meet the required format (invalid fields, wrong number of arguments, or
 * invalid time/date structure).
 */
package eventboard;

/**
 *
 * @author Leandro
 */
public class InvalidCommandException extends Exception {

    public InvalidCommandException(String message) {
        super(message);
    }
}
