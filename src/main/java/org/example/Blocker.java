package org.example;

import org.example.model.Product;

import java.util.*;
import java.util.regex.Pattern;


public class Blocker {

    private static final Pattern gbPattern = Pattern.compile("(\\d{1,4})\\s*[-]?(g|gb|gbyte|gigabyte)s?", Pattern.CASE_INSENSITIVE);

    public static Map<String, List<Integer>> blockProducts(List<Product> products) {
        Map<String, List<Integer>> blocks = new HashMap<>();

        for (Product p : products) {
            Set<String> keys = generateBlockingKeys(p);
            for (String key : keys) {
                blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(p.id);
            }
        }

        return blocks;
    }

    public static Set<String> generateBlockingKeys(Product p) {
        Set<String> keys = new HashSet<>();

        // Basistext normalisieren
        String text = (p.name + " " + p.brand).toLowerCase().replaceAll("[^a-z0-9 ]", " ");
        String[] tokens = text.split("\\s+");

        for (String token : tokens) {
            if (token.length() > 1) {
                keys.add("TOKEN_" + token);
            }

            // GB-Erkennung
            java.util.regex.Matcher matcher = gbPattern.matcher(token);
            if (matcher.find()) {
                String gb = matcher.group(1);
                keys.add("GB_" + gb.toLowerCase() + "gb");
            }
        }

        // Kombinationen
        if (!p.brand.isEmpty()) {
            keys.add("BRAND_" + p.brand.toLowerCase());
        }

        return keys;
    }
}
