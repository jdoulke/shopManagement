package com.onlinestore.jdoulke.onlinestorefx.entities;

public class Order {
    private int orderId;
    private int customerId;
    private int userId;
    private String orderDate;
    private String status;
    private double amount;

    public Order(int orderId, int customerId, int userId, String orderDate, String status, double amount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.userId = userId;
        this.orderDate = orderDate;
        this.status = status;
        this.amount = amount;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}

