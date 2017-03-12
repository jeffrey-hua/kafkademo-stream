package com.lh.kafka.model;

public class NameCategory1 {
    private String userNname;

    private String itemName;

    private String category;

    public NameCategory1() {
    }

    public NameCategory1(String userNname, String itemName, String category) {
        this.userNname = userNname;
        this.itemName = itemName;
        this.category = category;
    }

    public String getUserNname() {
        return userNname;
    }

    public void setUserNname(String userNname) {
        this.userNname = userNname;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return userNname + " " + itemName + " " +  category;
    }
}
