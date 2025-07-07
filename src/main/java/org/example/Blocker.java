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

        // Optional: Sehr große Blöcke (>500 Einträge) entfernen
        blocks.entrySet().removeIf(entry -> entry.getValue().size() > 500);

        return blocks;
    }

    public static Set<String> generateBlockingKeys(Product p) {
        Set<String> keys = new HashSet<>();

        String rawText = (p.name + " " + p.brand)
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        String[] tokens = rawText.split(" ");

        String gb = null;
        String prefix = null;
        String suffix = null;
        String brand = p.brand != null ? p.brand.toLowerCase().replaceAll("[^a-z0-9]", "") : null;

        for (String token : tokens) {
            if (token.length() < 2) continue;

            // Speichergröße erkennen (z. B. 128gb, 64mb)
            if (gb == null && token.matches("\\d{1,4}(gb|mb)")) {
                String digits = token.replaceAll("[^0-9]", "");
                gb = "gb" + digits;
            }

            // Prefix extrahieren (z. B. sams → samsung)
            if (prefix == null && token.length() >= 4) {
                prefix = token.substring(0, 4);
            }

            // Suffix extrahieren (z. B. max, pad, note)
            if (suffix == null && token.length() >= 4) {
                suffix = token.substring(token.length() - 3);
            }

            // Einzelne Token als Key
            keys.add("TOKEN_" + token);
        }

        // Kombinierte Schlüssel
        if (gb != null && prefix != null && brand != null) {
            keys.add("BLOCK_" + gb + "_" + prefix + "_" + brand);
        }
        if (gb != null && prefix != null) {
            keys.add("BLOCK_" + gb + "_" + prefix);
        }
        if (gb != null && brand != null) {
            keys.add("BLOCK_" + gb + "_" + brand);
        }

        if (prefix != null) {
            keys.add("FALLBACK_PREFIX_" + prefix);
        }

        if (suffix != null) {
            keys.add("FALLBACK_SUFFIX_" + suffix);
        }

        // Kombinierter Suffix + Brand
        if (suffix != null && brand != null) {
            keys.add("BLOCK_" + suffix + "_" + brand);
        }

        return keys;
    }
}
