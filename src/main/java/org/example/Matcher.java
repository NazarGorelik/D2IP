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

                        double jaccardSim = jaccard(p1.title, p2.title);
                        double levenshteinSim = normalizedLevenshtein(p1.title, p2.title);

                        if (jaccardSim >= threshold &&
                                passesBalancedFilter(p1.title, p2.title) &&
                                levenshteinSim >= 0.7) {

                            int id1 = Math.min(p1.id, p2.id);
                            int id2 = Math.max(p1.id, p2.id);
                            candidatePairs.add(new Pair(id1, id2));
                        }
                    }
                }
            }
        }

        return candidatePairs;
    }

    private static boolean passesBalancedFilter(String title1, String title2) {
        Set<String> tokens1 = new HashSet<>(Arrays.asList(title1.toLowerCase().split("\\W+")));
        Set<String> tokens2 = new HashSet<>(Arrays.asList(title2.toLowerCase().split("\\W+")));

        Set<String> shared = new HashSet<>(tokens1);
        shared.retainAll(tokens2);

        boolean hasModelToken = shared.stream().anyMatch(token -> token.matches(".*\\d.*"));
        long validTokens = shared.stream().filter(token -> token.length() >= 4).count();

        return hasModelToken && validTokens >= 2;
    }

    private static double jaccard(String s1, String s2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(s1.toLowerCase().split("\\W+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(s2.toLowerCase().split("\\W+")));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.size() > 0 ? (double) intersection.size() / union.size() : 0.0;
    }

    private static int levenshtein(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(
                                    dp[i - 1][j] + 1,        // deletion
                                    dp[i][j - 1] + 1),       // insertion
                            dp[i - 1][j - 1] + cost  // substitution
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private static double normalizedLevenshtein(String s1, String s2) {
        int distance = levenshtein(s1.toLowerCase(), s2.toLowerCase());
        int maxLength = Math.max(s1.length(), s2.length());
        return maxLength > 0 ? 1.0 - ((double) distance / maxLength) : 1.0;
    }
}
