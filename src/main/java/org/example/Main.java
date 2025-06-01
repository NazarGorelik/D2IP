package org.example;

import com.opencsv.CSVReader;
import org.example.model.Pair;
import org.example.model.Product;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class Main {
    public static List<Product> loadProducts(String filePath) throws Exception {
        List<Product> products = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 6);
                if (parts.length < 6){
                    continue;
                }
                try {
                    products.add(new Product(Integer.parseInt(parts[0].trim()), parts[1].trim(), parts[2].trim(),
                            parts[3].trim(), parts[4].trim(), parts[5].trim()));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return products;
    }

    public static List<Pair> loadGroundTruth(String filePath) throws Exception {
        Set<Pair> gtSet = new HashSet<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            reader.readNext(); // Skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                int id1 = Integer.parseInt(line[0].trim());
                int id2 = Integer.parseInt(line[1].trim());

                if (id1 != id2) {
                    // Ensure consistent ordering (id1 < id2)
                    int minId = Math.min(id1, id2);
                    int maxId = Math.max(id1, id2);
                    gtSet.add(new Pair(minId, maxId));
                }
            }
        }
        return new ArrayList<>(gtSet);
    }

    public static void main(String[] args) throws Exception {
        List<Product> products = loadProducts("src/main/resources/dataset_2/Z2.csv");
        List<Pair> groundTruth = loadGroundTruth("src/main/resources/dataset_2/ZY2.csv");

        long start = System.currentTimeMillis();
        //generate blocks with similar pattern
        Map<String, List<Integer>> blocks = Blocker.createBlocks(products);
        List<Pair> matches = Matcher.generateMatches(blocks, products, 0.5);
        long end = System.currentTimeMillis();

        System.out.println("------------- Evaluation -------------");
        System.out.printf("Runtime: %.2f seconds\n", (end - start) / 1000.0);
        Evaluator.evaluate(matches, groundTruth);
    }
}
