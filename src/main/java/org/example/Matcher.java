package org.example;

import org.example.model.Pair;
import org.example.model.Product;

import java.util.*;

public class Matcher {
    public static List<Pair> generateMatches(Map<String, List<Integer>> blocks, List<Product> products, double threshold) {
        List<Pair> candidatePairs = new ArrayList<>();

        for (List<Integer> rowIds : blocks.values()) {
            if (rowIds.size() < 100) {
                for (int i = 0; i < rowIds.size(); i++) {
                    for (int j = i + 1; j < rowIds.size(); j++) {
                        Product p1 = products.get(rowIds.get(i));
                        Product p2 = products.get(rowIds.get(j));
                        double sim = jaccard(p1.title, p2.title);
                        if (sim >= threshold) {
                            int id1 = p1.id;
                            int id2 = p2.id;
                            if (id1 != id2) {
                                int tmp = id1;
                                id1 = id2;
                                id2 = tmp;
                            }
                            candidatePairs.add(new Pair(id1, id2));
                        }
                    }
                }
            }
        }
        return candidatePairs;
    }

    private static double jaccard(String s1, String s2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(s1.toLowerCase().split("\\s+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(s2.toLowerCase().split("\\s+")));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        int unionSize = Math.max(set1.size(), set2.size());
        return (double) intersection.size() / unionSize;
    }
}
