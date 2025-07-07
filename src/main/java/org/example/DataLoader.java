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
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line = br.readLine(); // Header überspringen

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",", -1);
            if (parts.length < 2) continue; // Ungültige Zeile überspringen

            try {
                int id = Integer.parseInt(parts[0].trim());
                String name = parts.length > 1 ? parts[1].trim() : "";
                String price = parts.length > 2 ? parts[2].trim() : "";
                String brand = parts.length > 3 ? parts[3].trim() : "";
                String description = parts.length > 4 ? parts[4].trim() : "";
                String category = parts.length > 5 ? parts[5].trim() : "";

                // ANPASSUNG HIER: passende Reihenfolge zum Konstruktor
                products.add(new Product(id, name, brand, description, category, price));
            } catch (NumberFormatException e) {
                // Fehlerhafte ID ignorieren
            }
        }

        br.close();
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
