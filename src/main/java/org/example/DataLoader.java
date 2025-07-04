package org.example;

import org.example.model.Pair;
import org.example.model.Product;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    public static List<Product> loadProducts(String filename) throws Exception {
        List<Product> products = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // Header überspringen

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 2) continue; // Skip Zeile, wenn zu kurz

                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    String brand = parts.length > 3 ? parts[3].trim() : "";
                    products.add(new Product(id, name, brand));
                } catch (NumberFormatException ignored) {
                    // Ignoriere fehlerhafte Zeilen
                }
            }
        }

        return products;
    }

    public static List<Pair> loadTruePairs(String filename) throws Exception {
        List<Pair> truePairs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine(); // Header überspringen

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 2) continue;

                try {
                    int id1 = Integer.parseInt(parts[0].trim());
                    int id2 = Integer.parseInt(parts[1].trim());
                    truePairs.add(new Pair(id1, id2));
                } catch (NumberFormatException ignored) {
                    // Ignoriere fehlerhafte Zeilen
                }
            }
        }

        return truePairs;
    }
}
