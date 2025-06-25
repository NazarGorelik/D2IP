package org.example;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.example.model.Pair;
import org.example.model.Product;

import java.util.*;
import java.util.stream.Collectors;

public class Matcher {

    public static Set<Pair> generateMatches(Map<String, List<Integer>> blocks, List<Product> products, double threshold) {
        Set<Pair> candidatePairs = new HashSet<>();
        Map<Integer, Product> productById = products.stream()
                .collect(Collectors.toMap(p -> p.id, p -> p));

        // Map für jedes Produkt: ID → Blocking Keys
        Map<Integer, Set<String>> productToKeys = new HashMap<>();
        Blocker blocker = new Blocker();
        for (Product p : products) {
            productToKeys.put(p.id, blocker.generateBlockingKeys(p));
        }

        Set<String> seenPairs = new HashSet<>();
        int matchCount = 0;

        for (List<Integer> ids : blocks.values()) {
            for (int i = 0; i < ids.size(); i++) {
                for (int j = i + 1; j < ids.size(); j++) {
                    int id1 = Math.min(ids.get(i), ids.get(j));
                    int id2 = Math.max(ids.get(i), ids.get(j));
                    String key = id1 + "#" + id2;
                    if (!seenPairs.add(key)) continue;

                    Product p1 = productById.get(id1);
                    Product p2 = productById.get(id2);

                    Set<String> keys1 = productToKeys.get(id1);
                    Set<String> keys2 = productToKeys.get(id2);
                    Set<String> intersection = new HashSet<>(keys1);
                    intersection.retainAll(keys2);

                    if (intersection.size() >= 4) {
                        candidatePairs.add(new Pair(id1, id2));
                        continue;
                    }

                    // Ähnlichkeitsvergleich nur bei < 4 gemeinsamen Keys
                    double jacc = jaccardSimilarity(p1, p2);
                    double lev = levenshteinSimilarity(p1, p2);
                    double score = 0.6 * jacc + 0.4 * lev;

                    if (score >= threshold) {
                        candidatePairs.add(new Pair(id1, id2));
                    }
                }
            }
        }

        return candidatePairs;
    }

    private static double jaccardSimilarity(Product p1, Product p2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(normalize(p1).split(" ")));
        Set<String> set2 = new HashSet<>(Arrays.asList(normalize(p2).split(" ")));
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private static double levenshteinSimilarity(Product p1, Product p2) {
        String s1 = fullText(p1);
        String s2 = fullText(p2);
        LevenshteinDistance ld = new LevenshteinDistance();
        int dist = ld.apply(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        return maxLen == 0 ? 1.0 : 1.0 - (double) dist / maxLen;
    }

    private static String normalize(Product p) {
        return fullText(p).toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private static String fullText(Product p) {
        return (p.name + " " + p.brand + " ").toLowerCase();
    }
}
