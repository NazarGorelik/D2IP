// === Main.java ===
package org.example;

import com.opencsv.CSVReader;
import org.example.model.Pair;
import org.example.model.Product;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static List<Product> loadProducts(String filePath) throws Exception {
        List<Product> products = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2); // only split on first comma
                if (parts.length < 2) continue; // if there is no id or title
                try {
                    products.add(new Product(Integer.parseInt(parts[0].trim()), parts[1].trim()));
                } catch (NumberFormatException e) {
                    // skip malformed line
                }
            }
        }
        return products;
    }

    // Pair(smallId, bigId)
    public static List<Pair> loadGroundTruth(String filePath) throws Exception {
        List<Pair> gt = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            reader.readNext(); // Skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                int id1 = Integer.parseInt(line[0]);
                int id2 = Integer.parseInt(line[1]);
                if (id1 != id2) {
                    id1 = Math.min(id1, id2);
                    id2 = Math.max(id1, id2);
                    gt.add(new Pair(id1, id2));
                }
            }
        }
        return gt;
    }

    public static void main(String[] args) throws Exception {
        String productFile = "src/main/resources/dataset_1/Z1_update.csv";
        String groundTruthFile = "src/main/resources/dataset_1/ZY1_update.csv";

        List<Product> products = loadProducts(productFile);
        List<Pair> groundTruth = loadGroundTruth(groundTruthFile);

        Map<String, List<Integer>> blocks = Blocker.createBlocks(products);

        System.out.println("------------- BEST COMBINED EVALUATION (Multi-Key Blocking) -------------");
        long start = System.currentTimeMillis();
        List<Pair> combinedMatches = Matcher.generateMatches(blocks, products, 0.65, Matcher.SimilarityType.COMBINED);
        long end = System.currentTimeMillis();

        System.out.printf("Runtime: %.2f seconds\n", (end - start) / 1000.0);
        Evaluator.evaluate(combinedMatches, groundTruth);
    }
}
