package org.example;

import org.example.model.Product;

import java.util.*;

public class Blocker {

    public static Map<String, List<Integer>> blockProducts(List<Product> products) {
        Map<String, List<Integer>> blocks = new HashMap<>();

        for (Product p : products) {
            Set<String> keys = generateBlockingKeys(p);
            for (String key : keys) {
                blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(p.id);
            }
        }

        // Optional: Entferne extrem große Blöcke (z. B. >300)
        blocks.entrySet().removeIf(entry -> entry.getValue().size() > 300);

        return blocks;
    }

    public static Set<String> generateBlockingKeys(Product p) {
        Set<String> keys = new HashSet<>();

        String combined = (p.name + " " + p.brand).toLowerCase().replaceAll("[^a-z0-9 ]", " ");
        String[] tokens = combined.trim().split("\\s+");

        for (String token : tokens) {
            if (token.length() < 2) continue;

            // Generischer Token
            keys.add("TOKEN_" + token);

            // Speichergrößen wie "32gb", "64mb", "128g", etc.
            if (token.matches("\\d{1,4}(gb|mb|g|m)")) {
                String digits = token.replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    keys.add("GB_" + digits + "gb");
                }
            }

            // Wenn Token z. B. "64" ist, prüfe Kontext
            if (token.matches("\\d{2,4}")) {
                keys.add("NUM_" + token);
            }
        }

        return keys;
    }
}
