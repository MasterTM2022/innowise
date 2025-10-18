// Create your own realization of LinkedList and implement the following operations:
//    size() - returns the size of the list
//    addFirst(el) - adds the element in the beginning of the list
//    addLast(el) - adds the element in the end of the list
//    add(index, el) - adds the element in the list by index
//    getFirst() - returns the first element of the list
//    getLast() - returns the last element of the list
//    get(index) - returns the element by index
//    removeFirst() - retrieve and remove the first element of the list
//    removeLast() - retrieve and remove the last element of the list
//    remove(index) - retrieve and remove the element of the list by index
//Cover all these operations with unit tests using JUnit 5

package JavaCore_CustomLinkedList;

import java.util.NoSuchElementException;

public class CustomLinkedList<E> {
    private Node<E> first;
    private Node<E> last;
    private int size;

    public CustomLinkedList() {
        this.first = null;
        this.last = null;
        this.size = 0;
    }

    public static class Node<E> {
        E object;
        Node<E> prev;
        Node<E> next;

        Node(E object) {
            this.object = object;
            this.prev = null;
            this.next = null;
        }
    }


    public int size() {
        return size;
    }

    public void addLast(E el) {
        Node<E> newNode = new Node<>(el);
        if (last == null) {
            last = newNode;
            first = newNode;
        } else {
            last.next = newNode;
            newNode.prev = last;
            last = newNode;
        }
        size++;
    }

    public void addFirst(E el) {
        Node<E> newNode = new Node<>(el);
        if (first == null) {
            last = newNode;
        } else {
            first.prev = newNode;
            newNode.next = first;
        }
        first = newNode;
        size++;
    }

    public void add(int index, E el) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index " + index + "is out of range [0; " + (size - 1) + "], current CustomLinkedList's size is " + size);
        } else {
            if (index == 0) {
                addFirst(el);
            } else if (index == size) {
                addLast(el);
            } else {
                Node<E> newNode = new Node<>(el);
                Node<E> currentNode;

                if (index < size / 2) {
                    currentNode = first;
                    for (int i = 0; i < index; i++) {
                        currentNode = currentNode.next;
                    }
                } else {
                    currentNode = last;
                    for (int i = size - 2; i >= index; i--) {
                        currentNode = currentNode.prev;
                    }
                }
                newNode.prev = currentNode.prev;
                newNode.next = currentNode;
                currentNode.prev.next = newNode;
                currentNode.prev = newNode;
                size++;
            }

        }
    }

    public E getFirst() {
        if (first == null) {
            throw new NoSuchElementException("CustomLinkedList is empty");
        }
        return first.object;
    }

    public E getLast() {
        if (last == null) {
            throw new NoSuchElementException("CustomLinkedList is empty");
        }
        return last.object;
    }

    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of range [0; " + (size - 1) + "], current CustomLinkedList's size is " + size);
        } else {
            Node<E> currentNode;

            if (index < size / 2) {
                currentNode = first;
                for (int i = 0; i < index; i++) {
                    currentNode = currentNode.next;
                }
            } else {
                currentNode = last;
                for (int i = size - 1; i > index; i--) {
                    currentNode = currentNode.prev;
                }
            }
            return currentNode.object;
        }
    }

    public E removeFirst() {
        if (first == null) {
            throw new NoSuchElementException("CustomLinkedList is empty");
        }
        E object = first.object;
        if (first == last) {
            first = null;
            last = null;
        } else {
            first = first.next;
            first.prev = null;
        }
        size--;
        return object;
    }

    public E removeLast() {
        if (last == null) {
            throw new NoSuchElementException("CustomLinkedList is empty");
        }
        E object = last.object;
        if (first == last) {
            first = null;
            last = null;
        } else {
            last = last.prev;
            last.next = null;
        }
        size--;
        return object;
    }

    public E remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of range [0; " + (size - 1) + "], current CustomLinkedList's size is " + size);
        } else {
            if (index == 0) {
                return removeFirst();
            } else if (index == size - 1) {
                return removeLast();
            } else {
                E object;
                if (last == first) {
                    object = first.object;
                    last = null;
                    first = null;
                } else {
                    Node<E> currentNode;

                    if (index < size / 2) {
                        currentNode = first;
                        for (int i = 0; i < index; i++) {
                            currentNode = currentNode.next;
                        }
                    } else {
                        currentNode = last;
                        for (int i = size - 1; i > index; i--) {
                            currentNode = currentNode.prev;
                        }
                    }
                    currentNode.prev.next = currentNode.next;
                    currentNode.next.prev = currentNode.prev;
                    object = currentNode.object;
                }
                size--;
                return object;
            }
        }
    }
}
