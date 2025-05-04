package org.example;

public class Product1 {
    private final int id;
    private final String title;

    public Product1(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Product1{" + "id=" + id + ", title='" + title + '\'' + '}';
    }
}