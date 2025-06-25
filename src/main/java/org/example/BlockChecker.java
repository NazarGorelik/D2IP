package org.example;

import org.example.model.Pair;
import org.example.model.Product;

import java.util.*;

public class BlockChecker {

    public static void analyzeBlockingCoverage(Set<Pair> groundTruth, List<Product> products, Map<String, List<Integer>> blocks) {
        Map<Integer, Set<String>> productToKeys = new HashMap<>();

        for (Map.Entry<String, List<Integer>> entry : blocks.entrySet()) {
            for (Integer id : entry.getValue()) {
                productToKeys.computeIfAbsent(id, k -> new HashSet<>()).add(entry.getKey());
            }
        }

        int inSameBlock = 0;
        for (Pair pair : groundTruth) {
            Set<String> keys1 = productToKeys.getOrDefault(pair.id1, Collections.emptySet());
            Set<String> keys2 = productToKeys.getOrDefault(pair.id2, Collections.emptySet());
            Set<String> intersection = new HashSet<>(keys1);
            intersection.retainAll(keys2);

            if (!intersection.isEmpty()) {
                inSameBlock++;
                //System.out.printf("✅ (%d, %d): %d gemeinsame Keys → %s\n",
                        //pair.id1, pair.id2, intersection.size(), intersection);
            } else {
                //System.out.printf("❌ (%d, %d): keine gemeinsamen Keys\n", pair.id1, pair.id2);
            }
        }

        System.out.printf("%n📊 Blocking Coverage: %d von %d Ground-Truth-Pairs in gemeinsamen Blöcken\n",
                inSameBlock, groundTruth.size());
    }
}
