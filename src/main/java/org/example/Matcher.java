package org.example;

import org.example.model.Pair;
import org.example.model.Product;

import java.util.*;
import java.util.stream.Collectors;

public class Matcher {

    public static Set<Pair> generateMatches(Map<String, List<Integer>> blocks, List<Product> products, double threshold) {
        Set<Pair> candidatePairs = new HashSet<>();
        Set<Pair> seenPairs = new HashSet<>();

        Map<Integer, Product> productById = products.stream()
                .collect(Collectors.toMap(p -> p.id, p -> p));

        for (List<Integer> ids : blocks.values()) {
            for (int i = 0; i < ids.size(); i++) {
                for (int j = i + 1; j < ids.size(); j++) {
                    int id1 = Math.min(ids.get(i), ids.get(j));
                    int id2 = Math.max(ids.get(i), ids.get(j));
                    Pair pair = new Pair(id1, id2);

                    if (seenPairs.contains(pair)) continue;
                    seenPairs.add(pair);

                    Product p1 = productById.get(id1);
                    Product p2 = productById.get(id2);

                    double jacc = jaccardSimilarity(p1, p2);

                    if (jacc >= threshold) {
                        candidatePairs.add(pair);
                    }
                }
            }
        }

        return candidatePairs;
    }

    private static double jaccardSimilarity(Product p1, Product p2) {
        Set<String> set1 = tokenize(p1.name + " " + p1.brand);
        Set<String> set2 = tokenize(p2.name + " " + p2.brand);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private static Set<String> tokenize(String text) {
        return new HashSet<>(Arrays.asList(
                text.toLowerCase()
                        .replaceAll("[^a-z0-9 ]", " ")
                        .replaceAll("\\s+", " ")
                        .trim()
                        .split(" ")
        ));
    }
}
