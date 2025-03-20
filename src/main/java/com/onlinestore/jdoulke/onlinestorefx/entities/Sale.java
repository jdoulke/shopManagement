package com.onlinestore.jdoulke.onlinestorefx.entities;

public class Sale {

    private int saleId;
    private int userId;
    private int orderId;
    private double amount;
    private String paymentMethod;
    private String saleDate;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Sale(int saleId, int userId, int orderId, double amount, String paymentMethod, String saleDate) {
        this.saleId = saleId;
        this.userId = userId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.saleDate = saleDate;
    }




}
