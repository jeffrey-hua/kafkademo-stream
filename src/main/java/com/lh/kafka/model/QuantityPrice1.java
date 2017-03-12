package com.lh.kafka.model;

public class QuantityPrice1 {

    private int quantity;

    private double price;

    private double total;

    public QuantityPrice1() {
    }

    public QuantityPrice1(int quantity, double price, double total) {
        this.quantity = quantity;
        this.price = price;
        this.total = total;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return quantity + " " + price + " " + total;
    }

}
