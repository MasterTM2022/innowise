package JavaCore_CustomLinkedList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CustomLinkedListTest {

    private CustomLinkedList<String> list;

    @BeforeEach
    void setUp() {
        list = new CustomLinkedList<>();
    }

    @Test
    @DisplayName("The size of an empty list -> 0")
    void testEmptyListSize() {
        assertEquals(0, list.size());
    }

    @Test
    @DisplayName("Getting the first element from an empty list -> an exception")
    void testGetFromEmptyList() {
        assertThrows(NoSuchElementException.class, () -> list.getFirst());
        assertThrows(NoSuchElementException.class, () -> list.getLast());
    }

    @Test
    @DisplayName("Removing from an empty list -> an exception")
    void testRemoveFromEmptyList() {
        assertThrows(NoSuchElementException.class, () -> list.removeFirst());
        assertThrows(NoSuchElementException.class, () -> list.removeLast());
    }

    @Test
    @DisplayName("Adding to the beginning -> increasing the size")
    void testAddFirstIncreasesSize() {
        list.addFirst("001");
        assertEquals(1, list.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"01", "02", "03", "04", "05"})
    @DisplayName("Adding elements multiple times and increasing the size")
    void testMultipleAdditions(int count) {
        for (int i = 0; i < count; i++) {
            list.addLast(String.format("%02d", i));
        }
        assertEquals(count, list.size());
    }

    @Test
    @DisplayName("Adding by index and increasing the size")
    void testAddAtIndexMiddle() {
        list.addLast("01");
        list.addLast("03");
        list.add(1, "02");
        assertEquals(3, list.size());
        assertEquals("01", list.get(0));
        assertEquals("02", list.get(1));
        assertEquals("03", list.get(2));
    }

    @Test
    @DisplayName("Adding with an invalid index -> an exception")
    void testAddAtInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, "10"));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, "10"));

        list.addFirst("07");
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(2, "10"));
    }

    @Nested
    @DisplayName("Element Getting Tests")
    class GetTests {

        @BeforeEach
        void setUp() {
            list.addLast("01");
            list.addLast("02");
            list.addLast("03");
        }

        @Test
        @DisplayName("Getting the first element")
        void testGetFirst() {
            assertEquals("01", list.getFirst());
        }

        @Test
        @DisplayName("Getting the last element")
        void testGetLast() {
            assertEquals("03", list.getLast());
        }

        @ParameterizedTest
        @CsvSource({"0, 01", "1, 02", "2, 03"})
        @DisplayName("Getting an element by index")
        void testGetByIndex(int index, String expected) {
            assertEquals(expected, list.get(index));
        }

        @Test
        @DisplayName("Getting by invalid index -> an exception")
        void testGetInvalidIndex() {
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));
        }
    }

    @Nested
    @DisplayName("Element Removal Tests")
    class RemoveTests {

        @BeforeEach
        void setUp() {
            list.addLast("01");
            list.addLast("02");
            list.addLast("03");
            list.addLast("04");
        }

        @Test
        @DisplayName("Removing the first element")
        void testRemoveFirst() {
            assertEquals("01", list.removeFirst());
            assertEquals(3, list.size());
            assertEquals("02", list.getFirst());
        }

        @Test
        @DisplayName("Removing the last element")
        void testRemoveLast() {
            assertEquals("04", list.removeLast());
            assertEquals(3, list.size());
            assertEquals("03", list.getLast());
        }

        @Test
        @DisplayName("Delete by index")
        void testRemoveAtIndex() {
            assertEquals("02", list.remove(1));
            assertEquals(3, list.size());
            assertEquals("03", list.get(1));
        }

        @Test
        @DisplayName("Deleting by invalid index should throw an exception")
        void testRemoveInvalidIndex() {
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(4));
        }
    }
}