package org.example;

import org.example.model.Product;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Blocker {

    public static Map<String, List<Integer>> createBlocks(List<Product> products) {
        Map<String, List<Integer>> blocks = new HashMap<>();

        for (int i = 0; i < products.size(); i++) {
            Set<String> keys = generateBlockingKeys(products.get(i));
            for (String key : keys) {
                blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
            }
        }

        return blocks;
    }

    private static Set<String> generateBlockingKeys(Product p) {
        Set<String> keys = new HashSet<>();

        // Combine and normalize fields
        String text = (p.name + " " + p.brand + " " + p.description).toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")   // remove special characters
                .replaceAll("\\s+", " ")         // normalize spaces
                .trim();

        // Tokenize
        String[] tokens = text.split(" ");

        // Filter tokens
        List<String> filtered = Arrays.stream(tokens)
                .filter(t -> t.length() > 1)             // remove single-char words
                .filter(t -> !t.matches("\\d+"))         // remove pure numbers
                .distinct()
                .toList();

        // Add individual tokens as blocking keys
        keys.addAll(filtered);

        // Add token bigrams (sliding window of 2)
        for (int i = 0; i < filtered.size() - 1; i++) {
            keys.add(filtered.get(i) + "_" + filtered.get(i + 1));
        }

        return keys;
    }
}
