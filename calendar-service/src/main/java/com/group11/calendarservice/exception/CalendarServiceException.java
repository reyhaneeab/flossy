package com.group11.calendarservice.exception;




/**
 * Custom exception class for Calendar Service related errors.
 */
public class CalendarServiceException extends RuntimeException {

    //Constructs a new CalendarServiceException with null as its detail message.
    public CalendarServiceException() {
        super();
    }

    //Constructs a new CalendarServiceException with the specified detail message.
    public CalendarServiceException(String message) {
        super(message);
    }

    //Constructs a new CalendarServiceException with the specified detail message and cause.
    public CalendarServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    //Constructs a new CalendarServiceException with the specified cause and a
    public CalendarServiceException(Throwable cause) {
        super(cause);
    }
}