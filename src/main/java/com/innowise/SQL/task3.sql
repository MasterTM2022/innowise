CREATE TYPE statusEnum AS ENUM (
    'completed',
    'cancelled_by_driver',
    'cancelled_by_client'
    );

CREATE TYPE roleEnum AS ENUM (
    'client',
    'driver',
    'partner'
    );

CREATE TYPE bannedEnum AS ENUM (
    'Yes',
    'No'
    );


DROP TABLE IF EXISTS Trips;
CREATE TABLE Trips
(
    id         SERIAL PRIMARY KEY,
    client_id  INTEGER NOT NULL,
    driver_id  INTEGER NOT NULL,
    city_id    INTEGER NOT NULL,
    status     statusEnum,
    request_at VARCHAR
);

-- Вставка тестовых данных в таблицу Trips
INSERT INTO Trips (id, client_id, driver_id, city_id, status, request_at)
VALUES (1, 1, 10, 1, 'completed', '2013-10-01'),
       (2, 2, 11, 1, 'cancelled_by_driver', '2013-10-01'),
       (3, 3, 12, 6, 'completed', '2013-10-01'),
       (4, 4, 13, 6, 'cancelled_by_client', '2013-10-01'),
       (5, 1, 10, 1, 'completed', '2013-10-02'),
       (6, 2, 11, 6, 'completed', '2013-10-02'),
       (7, 3, 12, 6, 'completed', '2013-10-02'),
       (8, 2, 12, 12, 'completed', '2013-10-03'),
       (9, 3, 10, 12, 'completed', '2013-10-03'),
       (10, 4, 13, 12, 'cancelled_by_driver', '2013-10-03');


DROP TABLE IF EXISTS Users;
CREATE TABLE Users
(
    users_id SERIAL PRIMARY KEY,
    banned   bannedenum,
    role     roleenum
);


INSERT INTO Users (users_id, banned, role)
VALUES (1, 'No', 'client'),
       (2, 'Yes', 'client'),
       (3, 'No', 'client'),
       (4, 'No', 'client'),
       (10, 'No', 'driver'),
       (11, 'No', 'driver'),
       (12, 'No', 'driver'),
       (13, 'No', 'driver');


--- Выборка строк по критериям запроса
SELECT Trips.request_at AS "Day",
       ROUND(
               SUM(CASE
                       WHEN Trips.status IN ('cancelled_by_driver', 'cancelled_by_client')
                           THEN 1
                       ELSE 0
                   END) * 1.0 / COUNT(*),
               2
       )            AS "Cancellation Rate"
FROM Trips
         JOIN Users u1 ON Trips.client_id = u1.users_id
         JOIN Users u2 ON Trips.driver_id = u2.users_id
WHERE Trips.request_at BETWEEN '2013-10-01' AND '2013-10-03'
  AND u1.banned = 'No'
  AND u2.banned = 'No'
GROUP BY Trips.request_at
HAVING COUNT(*) >= 1;