-- Создание таблицы Employee
DROP TABLE IF EXISTS Employee;
CREATE TABLE Employee
(
    id     SERIAL PRIMARY KEY, -- ID employee
    salary INTEGER NOT NULL    -- employee's salary
);

-- Вставка тестовых данных в таблицу Employee
INSERT INTO Employee (id, salary)
VALUES (1, 90000),
       (2, 90000),
       (3, 90000),
       (4, 90000);

--- Выборка строк по критериям запроса
SELECT CASE
           WHEN COUNT(DISTINCT salary) < 1 THEN NULL
           ELSE MAX(salary)
           END AS SecondHighestSalary
FROM (SELECT DISTINCT salary
      FROM Employee
      ORDER BY salary DESC
      OFFSET 1 LIMIT 1) temp;


--- ТО же самое, но с UNION
SELECT MAX(salary) AS SecondHighestSalary
FROM (
         -- Основной случай: второе наибольшее значение
         SELECT DISTINCT salary
         FROM Employee
         WHERE salary < (SELECT MAX(salary) FROM Employee)
         ORDER BY salary DESC
         LIMIT 1)

UNION

SELECT NULL::int
WHERE (SELECT COUNT(DISTINCT salary) FROM Employee) < 2;