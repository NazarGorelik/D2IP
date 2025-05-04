package org.example;

import java.util.*;

public class DuplicateDetector {

    public static Map<String, List<Integer>> createBlocksProduct1(List<Product1> products) {
        Map<String, List<Integer>> blocks = new HashMap<>();
        for (int i = 0; i < products.size(); i++) {
            String key = BlockingKeyGenerator.generateBlockingKey(products.get(i).getTitle());
            if (!key.isEmpty()) {
                blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
            }
        }
        return blocks;
    }

    public static Map<String, List<Integer>> createBlocksProduct2(List<Product2> products) {
        Map<String, List<Integer>> blocks = new HashMap<>();
        for (int i = 0; i < products.size(); i++) {
            String key = BlockingKeyGenerator.generateBlockingKey(products.get(i).getName());
            if (!key.isEmpty()) {
                blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
            }
        }
        return blocks;
    }

    public static List<MatchPair> generateMatchesProduct1(Map<String, List<Integer>> blocks, List<Product1> products) {
        return generateMatchesGeneric(blocks, products, true);
    }

    public static List<MatchPair> generateMatchesProduct2(Map<String, List<Integer>> blocks, List<Product2> products) {
        return generateMatchesGeneric(blocks, products, false);
    }

    private static <T> List<MatchPair> generateMatchesGeneric(Map<String, List<Integer>> blocks, List<T> products, boolean isProduct1) {
        List<MatchPair> candidatePairs = new ArrayList<>();
        List<String> names = new ArrayList<>();

        for (T prod : products) {
            if (isProduct1) {
                names.add(((Product1) prod).getTitle());
            } else {
                names.add(((Product2) prod).getName());
            }
        }

        for (Map.Entry<String, List<Integer>> entry : blocks.entrySet()) {
            List<Integer> ids = entry.getValue();
            if (ids.size() < 100) {
                for (int i = 0; i < ids.size(); i++) {
                    for (int j = i + 1; j < ids.size(); j++) {
                        int id1 = ids.get(i);
                        int id2 = ids.get(j);

                        int pid1 = ((isProduct1 ? ((Product1) products.get(id1)).getId() : ((Product2) products.get(id1)).getId()));
                        int pid2 = ((isProduct1 ? ((Product1) products.get(id2)).getId() : ((Product2) products.get(id2)).getId()));

                        String name1 = names.get(id1);
                        String name2 = names.get(id2);

                        double sim = jaccardSimilarity(name1, name2);
                        if (sim >= 0.5) {
                            int min = Math.min(pid1, pid2);
                            int max = Math.max(pid1, pid2);
                            candidatePairs.add(new MatchPair(min, max));
                        }
                    }
                }
            }
        }
        return candidatePairs;
    }

    private static double jaccardSimilarity(String s1, String s2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(s1.toLowerCase().split("\\s+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(s2.toLowerCase().split("\\s+")));
        Set<String> inter = new HashSet<>(set1);
        inter.retainAll(set2);
        return (double) inter.size() / Math.max(set1.size(), set2.size());
    }
}
