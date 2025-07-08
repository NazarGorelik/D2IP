// === Blocker.java ===
package org.example;

import org.example.model.Product;
import org.example.model.Pair;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Blocker {

    // Extrahiert alphanumerische Tokens (mit Ziffern)
    public static String generateBlockingKey(String title) {
        Pattern pattern = Pattern.compile("\\b\\w*\\d+\\w*\\b");
        Matcher matcher = pattern.matcher(title);
        List<String> parts = new ArrayList<>();

        while (matcher.find()) {
            parts.add(matcher.group().toLowerCase());
        }

        Collections.sort(parts);
        return String.join(" ", parts);
    }

    // Extrahiert alphabetische Tokens (nur Buchstaben, ≥4 Zeichen)
    public static String generateAlternativeKey(String title) {
        String normalized = title.toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        List<String> parts = new ArrayList<>();

        for (String token : normalized.split(" ")) {
            if (token.matches("^[a-z]{4,}$")) {
                parts.add(token);
            }
        }

        Collections.sort(parts);
        return String.join(" ", parts);
    }

    // Kombiniert beide Blocking Keys (alphanumerisch & alphabetisch)
    public static Map<String, List<Integer>> createCombinedBlocks(List<Product> products) {
        Map<String, List<Integer>> blocks = new HashMap<>();

        for (int i = 0; i < products.size(); i++) {
            Set<String> keys = new HashSet<>();
            String key1 = generateBlockingKey(products.get(i).title);
            String key2 = generateAlternativeKey(products.get(i).title);

            if (!key1.isEmpty()) keys.add(key1);
            if (!key2.isEmpty()) keys.add(key2);

            for (String key : keys) {
                blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
            }
        }

        return blocks;
    }
}
