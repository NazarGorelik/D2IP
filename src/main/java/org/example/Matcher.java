package org.example;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.example.model.Pair;
import org.example.model.Product;

import java.util.*;
import java.util.stream.Collectors;

public class Matcher {

    public static List<Pair> generateMatches(Map<String, List<Integer>> blocks, List<Product> products, double threshold) {
        List<Pair> candidatePairs = new ArrayList<>();
        Set<Pair> seenPairs = new HashSet<>();
        Map<Integer, Product> productById = products.stream()
                .collect(Collectors.toMap(p -> p.id, p -> p));
        int count = 0;
        for (List<Integer> rowIds : blocks.values()) {
            if(rowIds.size() <= 1000) {
                for (int i = 0; i < rowIds.size(); i++) {
                    for (int j = i + 1; j < rowIds.size(); j++) {
                        Product p1 = productById.get(rowIds.get(i));
                        Product p2 = productById.get(rowIds.get(j));

                        double jaccardSimilarity = jaccardSimilarity(p1, p2);
                        if (jaccardSimilarity < 0.2) continue; // skip clearly dissimilar

                        double sim = 0.6 * jaccardSimilarity(p1, p2) + 0.4 * levenshteinSimilarity(p1, p2);

                        if (sim >= threshold) {
                            int id1 = Math.min(p1.id, p2.id);
                            int id2 = Math.max(p1.id, p2.id);
                            Pair pair = new Pair(id1, id2);
                            if (seenPairs.add(pair)) {
                                candidatePairs.add(pair);
                            }
                        }
                    }
                }
                System.out.println(++count);
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
        int distance = ld.apply(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        return maxLen == 0 ? 1.0 : 1.0 - ((double) distance / maxLen);
    }

    private static String normalize(Product p) {
        return fullText(p).toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String fullText(Product p) {
        return (p.name + " " + p.brand + " " + p.description).toLowerCase();
    }
}
