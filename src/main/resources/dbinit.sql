TRUNCATE TABLE ticket CASCADE;
TRUNCATE TABLE showtime CASCADE;
TRUNCATE TABLE movies CASCADE;

INSERT INTO movies (title, genre, duration, rating, release_year)
VALUES
    ('Barbie', 'Comedy', 114, 7.3, 2023),
    ('Oppenheimer', 'Drama', 180, 8.9, 2023),
    ('Inside Out 2', 'Animation', 105, 8.2, 2024),
    ('Interstellar', 'Sci-Fi', 169, 8.6, 2014),
    ('The Godfather', 'Crime', 175, 9.2, 1972);


INSERT INTO showtime (movie_id, theater, start_time, end_time, price)
VALUES
    (1, 'Hall 1', '2025-04-01T14:00:00', '2025-04-01T16:00:00', 35.00),
    (2, 'Hall 1', '2025-04-01T17:00:00', '2025-04-01T20:00:00', 45.00),
    (3, 'Hall 3', '2025-04-03T10:30:00', '2025-04-03T12:30:00', 32.00),
    (4, 'Hall 2', '2025-04-04T20:00:00', '2025-04-04T23:00:00', 50.00),
    (5, 'Hall 4', '2025-04-05T19:00:00', '2025-04-05T22:00:00', 38.00);



INSERT INTO ticket (showtime_id, seat_number)
VALUES
    (1, 'A1'),
    (1, 'A2'),
    (2, 'B5'),
    (3, 'C3'),
    (5, 'D1');

