package com.att.tdp.popcorn_palace.Exception;

public enum ErrorType {
    MOVIE_NOT_FOUND,
    SHOWTIME_NOT_FOUND,
    DUPLICATE_MOVIE_TITLE,
    MOVIE_HAS_ACTIVE_SHOWTIMES,
    SHOWTIME_HAS_TICKETS,
    OVERLAPPING_SHOWTIME,
    VALIDATION_ERROR,
    INVALID_SHOWTIME, INTERNAL_SERVER_ERROR, SEAT_ALREADY_BOOKED
}
