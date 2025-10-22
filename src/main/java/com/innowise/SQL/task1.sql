-- Создание таблицы Employee
DROP TABLE IF EXISTS Employee;
CREATE TABLE Employee
(
    id        SERIAL PRIMARY KEY, -- ID employee
    name      TEXT    NOT NULL,   -- employee's name
    salary    INTEGER NOT NULL,   -- employee's salary
    managerID INTEGER             -- employee's manager

);

-- Вставка тестовых данных в таблицу Employee
INSERT INTO Employee (id, name, salary, managerID)
VALUES (1, 'Joe', 70000, 3),
       (2, 'Henry', 80000, 4),
       (3, 'Sam', 60000, null),
       (4, 'Max', 90000, null);


-- Выборка строк по критериям запроса
SELECT table1.name AS "Employee"
FROM Employee table1
         LEFT JOIN Employee table2
                   ON table1.managerID = table2.id
WHERE table1.salary > table2.salary
ORDER BY table1."id"