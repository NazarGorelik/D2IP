package org.example;

import org.example.model.Pair;
import org.example.model.Product;

import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        // 🔄 Daten laden
        List<Product> products = DataLoader.loadProducts("src/main/resources/dataset_2/Z2_mittel.csv");
        Set<Pair> groundTruth = new HashSet<>(DataLoader.loadTruePairs("src/main/resources/dataset_2/ZY2_mittel.csv"));
        System.out.println("✅ Produkte geladen: " + products.size());
        System.out.println("✅ Ground Truth Pairs geladen: " + groundTruth.size());

        // 🔗 Blocking
        Blocker blocker = new Blocker();
        Map<String, List<Integer>> blocks = blocker.blockProducts(products);
        System.out.println("📦 Anzahl Blocks: " + blocks.size());

        // 📊 Blocking Coverage analysieren
        BlockChecker.analyzeBlockingCoverage(groundTruth, products, blocks);

        // 🤖 Matching starten
        double threshold = 0.5;
        System.out.println("🔍 Starte Matching mit Schwellenwert: " + threshold);
        Set<Pair> predictedPairs = new HashSet<>(Matcher.generateMatches(blocks, products, threshold));

        // 📈 Evaluation
        Evaluator.evaluate(new ArrayList<>(predictedPairs), new ArrayList<>(groundTruth));

        // ⏱️ Laufzeit messen
        long endTime = System.currentTimeMillis();
        double durationSec = (endTime - startTime) / 1000.0;
        System.out.printf("⏱️ Laufzeit: %.2f Sekunden\n", durationSec);
    }
}
