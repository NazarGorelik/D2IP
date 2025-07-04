package org.example;

import org.example.model.Pair;
import org.example.model.Product;

import java.util.*;
import java.util.stream.Collectors;

public class Matcher {

    public static Set<Pair> generateMatches(Map<String, List<Integer>> blocks, List<Product> products, double threshold) {
        Set<Pair> candidatePairs = new HashSet<>();
        Set<Pair> seen = new HashSet<>();

        Map<Integer, Product> productById = products.stream()
                .collect(Collectors.toMap(p -> p.id, p -> p));

        for (List<Integer> ids : blocks.values()) {
            for (int i = 0; i < ids.size(); i++) {
                for (int j = i + 1; j < ids.size(); j++) {
                    int id1 = Math.min(ids.get(i), ids.get(j));
                    int id2 = Math.max(ids.get(i), ids.get(j));
                    Pair pair = new Pair(id1, id2);
                    if (seen.contains(pair)) continue;
                    seen.add(pair);

                    Product p1 = productById.get(id1);
                    Product p2 = productById.get(id2);

                    double similarity = jaccardSimilarity(normalize(p1), normalize(p2));
                    if (similarity >= threshold) {
                        candidatePairs.add(pair);
                    }
                }
            }
        }

        return candidatePairs;
    }

    private static String normalize(Product p) {
        return (p.name + " " + p.brand).toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private static double jaccardSimilarity(String s1, String s2) {
        Set<String> tokens1 = new HashSet<>(Arrays.asList(s1.split(" ")));
        Set<String> tokens2 = new HashSet<>(Arrays.asList(s2.split(" ")));

        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);

        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);

        return (double) intersection.size() / union.size();
    }
}
