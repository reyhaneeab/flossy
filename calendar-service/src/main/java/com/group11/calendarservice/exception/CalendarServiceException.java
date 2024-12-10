package com.group11.calendarservice.exception;




/**
 * Custom exception class for Calendar Service related errors.
 */
public class CalendarServiceException extends RuntimeException {

    //Constructs a new CalendarServiceException with null as its detail message.
   @SuppressWarnings("unused")
    public CalendarServiceException() {
        super();
    }

    //Constructs a new CalendarServiceException with the specified detail message.
    public CalendarServiceException(String message) {
        super(message);
    }

    //Constructs a new CalendarServiceException with the specified detail message and cause.
    @SuppressWarnings("unused")
    public CalendarServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    //Constructs a new CalendarServiceException with the specified cause
    @SuppressWarnings("unused")
    public CalendarServiceException(Throwable cause) {
        super(cause);
    }
}