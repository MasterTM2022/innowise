package SalesAndCustomerAnalysis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TaskTest {

    @Test
    void testStatistic1() {                         ///  All methods generate their own data, so we can only test that they don't throw exceptions.
        Assertions.assertDoesNotThrow(Task::subTask1);
    }

    @Test
    void testStatistic2() {
        Assertions.assertDoesNotThrow(Task::subTask2);
    }

    @Test
    void testStatistic3() {
        Assertions.assertDoesNotThrow(Task::subTask3);
    }

    @Test
    void testStatistic4() {
        Assertions.assertDoesNotThrow(Task::subTask4);
    }

    @Test
    void testStatistic5() {
        Assertions.assertDoesNotThrow(Task::subTask5);
    }

    @Test
    void testMain() throws IOException {
        String[] args = {"a", "b", "c"};
        Assertions.assertDoesNotThrow(() -> Task.main(args));
    }
}