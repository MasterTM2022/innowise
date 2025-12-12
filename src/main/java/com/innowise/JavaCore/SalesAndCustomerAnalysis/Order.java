package com.innowise.JavaCore.SalesAndCustomerAnalysis;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private static int maxOrderID = 0;
    private String orderId;

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", orderDate=" + orderDate +
                ", customer=" + customer +
                ", items=" + items +
                ", status=" + status +
                '}';
    }

    private LocalDateTime orderDate;
    private Customer customer;
    private List<OrderItem> items;
    private OrderStatus status;

    public Order(LocalDateTime orderDate, Customer customer, List<OrderItem> items, OrderStatus status) {
        this.orderId = Integer.toString(++maxOrderID);
        this.orderDate = orderDate;
        this.customer = customer;
        this.items = items;
        this.status = status;
    }

    public static int getMaxOrderID() {
        return maxOrderID;
    }

    public static void setMaxOrderID(int maxOrderID) {
        Order.maxOrderID = maxOrderID;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Object getCity() {
        return this.getCustomer().getCity();
    }

    public double getOrderValue() {
        double sum = 0.0;
        for (OrderItem oi : this.getItems()) {
            sum += oi.getQuantity()*oi.getPrice();
        }
        return sum;
    }

}
