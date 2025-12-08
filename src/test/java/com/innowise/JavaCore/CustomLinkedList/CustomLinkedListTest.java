package com.innowise.JavaCore.CustomLinkedList;

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
    @DisplayName("Add at index 0 to empty list -> becomes first and last")
    void testAddAtIndex0ToEmptyList() {
        list.add(0, "01");
        assertEquals(1, list.size());
        assertEquals("01", list.getFirst());
        assertEquals("01", list.getLast());
    }

    @Test
    @DisplayName("Add at max index to non-empty list -> increasing size")
    void testAddAtIndexMax() {
        int randomSize = (int) (Math.random() * 10) + 1;
        for (int i = 0; i < randomSize; i++) {
            list.add(i, String.format("%02d", i));
        }
        list.add(randomSize, "100");
        assertEquals(randomSize + 1, list.size());
        assertEquals("00", list.getFirst());
        assertEquals("100", list.getLast());
    }


    @Test
    @DisplayName("Adding to the beginning to empty list and non-empty list -> increasing the size")
    void testAddFirstIncreasesSize() {
        list.addFirst("001");
        assertEquals(1, list.size());
        list.addFirst("000");
        assertEquals(2, list.size());
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
        @DisplayName("Removing the last element by index")
        void testRemoveLast() {
            assertEquals("04", list.removeLast());
            assertEquals(3, list.size());
            assertEquals("03", list.getLast());
        }

        @Test
        @DisplayName("Removing the last element in multiple, non-multiple and empty lists")
        void testRemoveSingle() {
            while (list.size() > 1) {
                list.removeLast();
                assertEquals(String.format("%02d", list.size()), list.getLast());
            }
            assertEquals(1, list.size());
            list.removeLast();
            assertEquals(0, list.size());

            assertThrows(NoSuchElementException.class, () -> list.removeFirst());
            assertThrows(NoSuchElementException.class, () -> list.removeLast());
        }

        @Test
        @DisplayName("Removing from an empty list -> an exception")
        void testRemoveFromEmptyList() {
            list = new CustomLinkedList<>();
            assertThrows(NoSuchElementException.class, () -> list.removeFirst());
            assertThrows(NoSuchElementException.class, () -> list.removeLast());
        }

        @Test
        @DisplayName("Remove the only element -> list becomes empty")
        void testRemoveOnlyElement() {
            list = new CustomLinkedList<>();
            list.addFirst("001");

            assertEquals("001", list.removeFirst());
            assertEquals(0, list.size());

            assertThrows(NoSuchElementException.class, () -> list.getFirst());
            assertThrows(NoSuchElementException.class, () -> list.getLast());
        }

        @Test
        @DisplayName("Delete by index, including 0 and MAX, and single")
        void testRemoveAtIndex() {
            assertEquals("02", list.remove(1));
            assertEquals(3, list.size());
            assertEquals("03", list.get(1));

            assertEquals("04", list.remove(list.size()-1));
            assertEquals(2, list.size());

            assertEquals("01", list.remove(0));
            assertEquals("03", list.get(0));

            assertEquals("03", list.remove(0));
            assertEquals(0, list.size());
        }

        @Test
        @DisplayName("Deleting by invalid index should throw an exception")
        void testRemoveInvalidIndex() {
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(4));
        }
    }
}