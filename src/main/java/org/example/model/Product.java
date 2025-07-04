package org.example.model;

public class Product {
    public int id;
    public String name;
    //public String price;
    public String brand;
    //public String description;
    //public String category;

    public Product(int id, String name, String brand) {
        this.id = id;
        this.name = name != null ? name : "";
        this.brand = brand != null ? brand : "";
        //this.description = description != null ? description : "";
        //this.category = category != null ? category : "";
        //this.price = price;
    }
}
