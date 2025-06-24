package org.example;

import com.opencsv.CSVReader;
import org.example.model.Pair;
import org.example.model.Product;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    public static List<Product> loadProducts(InputStream stream) throws Exception {
        List<Product> products = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t\t", -1);
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
        // generates input stream with double tab as a delimiter between columns
        InputStream stream = CsvConverter.convertCsvToDoubleTabStream("src/main/resources/dataset_2/Z2_small.csv");
        List<Product> products = loadProducts(stream);
        // clean product names and brands from weird characters
        cleanProductNamesAndBrands(products);
        List<Pair> groundTruth = loadGroundTruth("src/main/resources/dataset_2/ZY2_small.csv");

        long start = System.currentTimeMillis();

        
/*
        //generate blocks with similar pattern
        Map<String, List<Integer>> blocks = Blocker.createBlocks(products);
        List<Pair> matches = Matcher.generateMatches(blocks, products, 0.5);
        long end = System.currentTimeMillis();

        System.out.println("------------- Evaluation -------------");
        System.out.printf("Runtime: %.2f seconds\n", (end - start) / 1000.0);
        Evaluator.evaluate(matches, groundTruth);*/
        // 1) Multi-Pass Blocking: erstelle verschiedene Block-Maps
      /*  Map<String, List<Integer>> passA = Blocker.blockByPriceBrand(products);
        Map<String, List<Integer>> passB = Blocker.blockByBrandType(products);
        Map<String, List<Integer>> passC = Blocker.blockByBrandSize(products);
        Map<String, List<Integer>> passD = Blocker.blockByTypeSize(products);
        Map<String, List<Integer>> passE = Blocker.blockByFirstToken(products);

        // 2) Für jeden Pass Matches generieren und in einem Set sammeln
        Set<Pair> allMatches = new HashSet<>();
        double threshold = 0.5;
        allMatches.addAll(Matcher.generateMatches(passA, products, threshold));
        allMatches.addAll(Matcher.generateMatches(passB, products, threshold));
        allMatches.addAll(Matcher.generateMatches(passC, products, threshold));
        allMatches.addAll(Matcher.generateMatches(passD, products, threshold));
        allMatches.addAll(Matcher.generateMatches(passE, products, threshold));

        List<Pair> finalMatches = new ArrayList<>(allMatches);
        long end = System.currentTimeMillis();
       */
        Map<String, List<Integer>> blocks =
              Blocker.createBlocks(products);

        // Calculate total size of all blocks
        int totalBlockEntries = 0;
        int grosseblöcke = 0;
        int blockgrösse = 0;
        for (List<Integer> ids : blocks.values()) {
            if (ids.size() > 1000) {++grosseblöcke; blockgrösse += ids.size();}
            totalBlockEntries += ids.size();
        }
        System.out.println("Total block entries: " + totalBlockEntries);
        System.out.println("Total number of large blocks: " + grosseblöcke + " " );


        // Generate matches based on these blocks
        double threshold = 0.55;
        List<Pair> matches =
                Matcher.generateMatches(blocks, products, threshold);

        long end = System.currentTimeMillis();


        System.out.println("------------- Evaluation -------------");
        System.out.printf("Runtime: %.2f seconds\n", (end - start)/1000.0);
        Evaluator.evaluate(matches, groundTruth);
    }

    public static void cleanProductNamesAndBrands(List<Product> products) {
        for (Product p : products) {
            if (p.name != null) {
                // Keep letters, digits, and spaces only
                String cleanedName = CleanString(p.name);
                p.name = cleanedName;
            }
            if (p.brand != null) {
                // Keep letters, digits, and spaces only
                String[] cleanedBrands = CleanString(p.brand).trim().split("\\s+");
                p.brand = cleanedBrands[0];
            }
        }
    }

    private static String CleanString(String word){
        String cleaned = word
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")   // <-- space is preserved!
                .replaceAll("\\s+", " ")        // normalize multiple spaces
                .trim();

        return cleaned;
    }
}
