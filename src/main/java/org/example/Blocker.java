// === Blocker.java ===
package org.example;

import org.example.model.Product;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Blocker {

    public static String generateBlockingKey(String title) {
        // Pattern: Wörter mit mindestens einer Ziffer, z. B. i5, 256GB, x250
        Pattern pattern = Pattern.compile("\\b\\w*\\d+\\w*\\b");
        Matcher matcher = pattern.matcher(title);
        List<String> parts = new ArrayList<>();

        while (matcher.find()) {
            parts.add(matcher.group().toLowerCase());
        }

        Collections.sort(parts);
        return String.join(" ", parts);
    }

    public static Map<String, List<Integer>> createBlocks(List<Product> products) {
        Map<String, List<Integer>> blocks = new HashMap<>();
        for (int i = 0; i < products.size(); i++) {
            String key = generateBlockingKey(products.get(i).title);
            if (!key.isEmpty()) {
                blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
            }
        }
        return blocks;
    }
}
