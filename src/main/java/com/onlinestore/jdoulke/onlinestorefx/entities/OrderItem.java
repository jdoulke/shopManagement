package com.onlinestore.jdoulke.onlinestorefx.entities;

public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int productId;
    private int quantity;
    private double itemPrice;
    private double totalPrice;
    private String name;

    public OrderItem(int orderItemId, int orderId, int productId, int quantity, double itemPrice, String name) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.itemPrice = itemPrice;
        this.totalPrice = quantity * itemPrice;
        this.name = name;
    }


    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = this.quantity * this.itemPrice;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String productName) {
        this.name = productName;
    }
}
