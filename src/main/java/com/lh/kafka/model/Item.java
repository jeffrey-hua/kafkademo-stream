package com.lh.kafka.model;

public class Item {
	private String name;
	private String address;
	private String category;
	private double price;

	public Item() {}
	
	public Item(String name, String address, String category, double price) {
		this.name = name;
		this.address = address;
		this.category = category;
		this.price = price;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String itemName) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

}
