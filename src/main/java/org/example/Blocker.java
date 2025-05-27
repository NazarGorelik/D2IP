package org.example;

import org.example.model.Product;

import java.util.*;
import java.util.regex.*;
import java.util.regex.Matcher;

public class Blocker {

    private static final Set<String> BRANDS = new HashSet<>(Arrays.asList(
            "dell", "lenovo", "hp", "asus", "acer", "sony", "toshiba", "samsung", "apple", "canon", "fujitsu"
    ));

    public static String preprocessTitle(String title) {
        String[] tokens = title.toLowerCase().split("\\W+");
        List<String> tokenList = new ArrayList<>(Arrays.asList(tokens));

        for (String brand : BRANDS) {
            if (tokenList.contains(brand) && !tokenList.get(0).equals(brand)) {
                tokenList.remove(brand);
                tokenList.add(0, brand);
                break;
            }
        }

        return String.join(" ", tokenList);
    }

    public static String generateBlockingKey(String title) {
        String cleanedTitle = preprocessTitle(title);
        Pattern pattern = Pattern.compile("\\w+\\s\\w+\\d+");
        Matcher matcher = pattern.matcher(cleanedTitle);
        List<String> parts = new ArrayList<>();

        while (((java.util.regex.Matcher) matcher).find()) {
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
