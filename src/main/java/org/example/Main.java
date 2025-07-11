package org.example;

import org.example.model.Pair;
import org.example.model.Product;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws Exception {
        long startTime = System.nanoTime();  //  Startzeit

        String productFile = "src/main/resources/dataset_2/Z2.csv";
        String pairFile = "src/main/resources/dataset_2/ZY2.csv";
        double threshold = 0.63;

        List<Product> products = DataLoader.loadProducts(productFile);
        List<Pair> groundTruth = DataLoader.loadTruePairs(pairFile);

        System.out.println("Produkte geladen: " + products.size());
        System.out.println("Ground Truth Pairs geladen: " + groundTruth.size());

        Map<String, List<Integer>> blocks = Blocker.blockProducts(products);
        System.out.println(" Anzahl Blocks: " + blocks.size());

        Set<Pair> predictedPairs = Matcher.generateMatches(blocks, products, threshold);

        System.out.println("Starte Matching mit Schwellenwert: " + threshold);
        Evaluator.evaluate(predictedPairs.stream().toList(), groundTruth);

        long endTime = System.nanoTime();  //  Endzeit
        double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
        System.out.printf("Laufzeit: %.2f Sekunden%n", durationSeconds);
    }
}
