package SalesAndCustomerAnalysis;

import net.datafaker.Faker;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Task {
    public static int customerQuantity = 10;
    public static int orderQuantity = 50;
    public static int maxQuantityPerOrder = 5;
    public static int daysStartBefore = 365;
    public static ArrayList<Customer> allCustomers;
    public static ArrayList<Order> allOrders;


    public static void main(String[] args) {
        allCustomers = createNewCustomerList();
        allOrders = createNewOrderList();
        subTask1();
        subTask2();
        subTask3();
        subTask4();
        subTask5();
    }


    private static ArrayList<Customer> createNewCustomerList() {
        Faker faker = new Faker();
        ArrayList<Customer> allCustomer = new ArrayList<>();
        for (int i = 0; i < Task.customerQuantity; i++) {
            String fullName = faker.name().fullName();
            allCustomer.add(new Customer(fullName,
                    fullName.replaceAll("\\.\\s|\\s", ".") + "@" + faker.domain().fullDomain("").replaceAll("\\.\\.|\\s", "."),
                    faker.date().past(daysStartBefore, TimeUnit.DAYS).toLocalDateTime(),
                    faker.random().nextInt(18, 100),
                    faker.address().city())
            );
            //            allCustomer.forEach(System.out::println);
        }
        return allCustomer;
    }

    private static ArrayList<Order> createNewOrderList() {
        Faker faker = new Faker();
        allOrders = new ArrayList<>();
        OrderStatus[] orderStatuses = OrderStatus.values();
        for (int i = 0; i < orderQuantity; i++) {
            Customer customer = allCustomers.get(faker.random().nextInt(0, allCustomers.size() - 1));
            allOrders.add(new Order(
                    LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochSecond(
                                    customer.getRegisteredAt().atZone(ZoneId.systemDefault()).toEpochSecond()
                                            + new Random().nextLong(LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()
                                            - customer.getRegisteredAt().atZone(ZoneId.systemDefault()).toEpochSecond() + 1)),
                            ZoneId.systemDefault()),
                    customer,
                    createNewOrderItemList(),
                    orderStatuses[faker.random().nextInt(0, orderStatuses.length - 1)]));

        }
//        allOrders.forEach(System.out::println);
        return allOrders;
    }

    private static ArrayList<OrderItem> createNewOrderItemList() {
        Faker faker = new Faker();
        ArrayList<OrderItem> allItems = new ArrayList<>();
        Category[] category = Category.values();
        int currentMaxQuantity = faker.random().nextInt(1, maxQuantityPerOrder);
        for (int i = 0; i < currentMaxQuantity; i++) {
            allItems.add(new OrderItem(
                    faker.marketing().buzzwords(),
                    faker.random().nextInt(1, 100),
                    faker.random().nextDouble(0.01, 1000.0),
                    category[faker.random().nextInt(0, category.length - 1)]));
        }
//        allItems.forEach(System.out::println);
        return allItems;
    }

    public static void subTask1() {
        System.out.println("\n1. List of unique cities where orders came from:");

        allOrders.stream()
                .map(Order::getCity)
                .distinct()
                .sorted()
                .forEach(System.out::println);
    }

    public static void subTask2() {
        System.out.print("\n2. Total income of delivered orders: ");
        double totalRevenue = allOrders.stream()
                .filter(st -> st.getStatus().equals(OrderStatus.DELIVERED))            // selecting only delivered orders
                .mapToDouble(Order::getOrderValue)
                .sum();
        System.out.println(Math.round(100 * totalRevenue) / 100.00 + " $");
    }

    public static void subTask3() {
        System.out.println("\n3. The most popular product by sales are:");
        record popularResult(List<String> mostFrequent, List<String> mostQuantity) {
        }
        Stream<OrderItem> flattenedStream = allOrders.stream()
                .map(Order::getItems)
                .flatMap(Collection::stream);                                            // getting all order items in one stream

        popularResult result = flattenedStream
                .collect(
                        Collectors.teeing(
                                Collectors.groupingBy(
                                        OrderItem::getProductName,
                                        Collectors.counting()
                                ),
                                Collectors.groupingBy(
                                        OrderItem::getProductName,
                                        Collectors.summingDouble(OrderItem::getQuantity)
                                ),
                                (Map<String, Long> mostFrequentMap, Map<String, Double> mostQuantityMap) -> {
                                    long maxF = mostFrequentMap.values().stream().mapToLong(Long::longValue).max().orElse(0);
                                    List<String> mostFrequent = mostFrequentMap
                                            .entrySet()
                                            .stream()
                                            .filter(e -> e.getValue() == maxF)
                                            .map(Map.Entry::getKey)
                                            .toList();
                                    double maxQ = mostQuantityMap.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                                    List<String> mostQuantity = mostQuantityMap
                                            .entrySet()
                                            .stream()
                                            .filter(e -> e.getValue() == maxQ)
                                            .map(Map.Entry::getKey)
                                            .toList();
                                    return new popularResult(mostFrequent, mostQuantity);
                                }
                        )
                );
        System.out.println("3.1. by frequency in orders: " + result.mostFrequent.toString());
        System.out.println("3.2. by quantity in orders: " + result.mostQuantity);
    }

    public static void subTask4() {
        System.out.print("\n4. Average check for successfully delivered orders: ");
        DoubleSummaryStatistics averageResult = allOrders.stream()
                .filter(order -> order.getStatus().equals(OrderStatus.DELIVERED))
                .collect(Collectors.summarizingDouble(Order::getOrderValue));
        System.out.println((double) Math.round(100 * averageResult.getAverage()) / 100 + " $");
    }

    public static void subTask5() {
        System.out.println("\n5. Customers who have more than 5 orders (only names):");
        allOrders.stream()
                .map(Order::getCustomer)
                .map(Customer::getName)
                .collect(
                        Collectors.groupingBy(
                                name -> name,
                                Collectors.counting()
                        )
                )
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 5)
                .map(Map.Entry::getKey)
                .toList()
                .forEach(System.out::println);
    }
}