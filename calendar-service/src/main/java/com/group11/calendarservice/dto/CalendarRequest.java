package com.group11.calendarservice.dto;

public record CalendarRequest(String date,
        String time,
        String dentistOffice,
                              boolean available) {
}
