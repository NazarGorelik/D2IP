package org.example;

import org.example.model.Product;

import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Blocker {
    private static final List<String> BRANDS = Arrays.asList(
            "intenso", "kingston", "lexar", "pny", "samsung", "sandisk", "sony", "toshiba", "transcend"
    );

    private static final List<String> STORAGE_TYPES = Arrays.asList(
            "usb", "ssd", "cd", "hdd", "sd", "micro sd", "microsd", "xqd", "compactflash", "cf", "sdhc", "sdxc"
    );

    private static final List<Integer> MEMORY_SIZES = Arrays.asList(
            4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048
    );

    public static Map<String, List<Integer>> createBlocks(List<Product> products) {
        Map<String, List<Integer>> layeredBlocks = new HashMap<>();
        // block by brand
        Map<String, List<Product>> brandBlocks = Blocker.blockByBrand(products);
        // assign elements with no brand to some brand based on element's name
        reassignMissingBrandsByName(brandBlocks);

        Map<String, List<Product>> layeredBlocksTest = new HashMap<>();

        for (Map.Entry<String, List<Product>> entry : brandBlocks.entrySet()) {
            String brandKey = entry.getKey();

            for (Product p : entry.getValue()) {
                // block by storage type
                String storageType = detectStorageType(p);
                // block by memory size
                String memorySize = detectMemorySize(p);

                String blockKey = brandKey + "_" + storageType + "_" + memorySize;
                layeredBlocks.computeIfAbsent(blockKey, k -> new ArrayList<>()).add(p.id);
                layeredBlocksTest.computeIfAbsent(blockKey, k -> new ArrayList<>()).add(p);
            }
        }
        return layeredBlocks;
    }

    private static String detectStorageType(Product p) {
        String text = (p.name + " " + p.description).toLowerCase();
        for (String type : STORAGE_TYPES) {
            if (text.contains(type)) {
                return type.replace(" ", ""); // e.g., "micro sd" → "microsd"
            }
        }
        return "unknown";
    }

    public static Map<String, List<Product>> blockByBrand(List<Product> products) {
        Map<String, List<Product>> brandBlocks = new HashMap<>();

        for (Product p : products) {
            String brand = normalize(p.brand);
            String blockKey = BRANDS.contains(brand) ? brand : "";
            brandBlocks.computeIfAbsent(blockKey, k -> new ArrayList<>()).add(p);
        }

        return brandBlocks;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase().trim(); // strip punctuation & unify casing
    }

    public static void reassignMissingBrandsByName(Map<String, List<Product>> brandBlocks) {
        List<Product> noBrandProducts = brandBlocks.getOrDefault("", new ArrayList<>());
        Map<String, List<Product>> updatedBlocks = new HashMap<>(brandBlocks);
        updatedBlocks.remove(""); // We'll re-add leftovers if needed

        Set<String> knownBrands = brandBlocks.keySet()
                .stream()
                .filter(b -> !b.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        for (Product p : noBrandProducts) {
            String nameLower = p.name.toLowerCase();
            boolean reassigned = false;

            for (String brand : knownBrands) {
                if (nameLower.contains(brand)) {
                    updatedBlocks.get(brand).add(p);
                    reassigned = true;
                    break;
                }
            }

            if (!reassigned) {
                updatedBlocks.computeIfAbsent("", k -> new ArrayList<>()).add(p);
            }
        }

        // Update original blocks map
        brandBlocks.clear();
        brandBlocks.putAll(updatedBlocks);
    }

    private static String detectMemorySize(Product p) {
        String content = (p.name + " " + p.description).toLowerCase();

        // First try to extract patterns like 32gb
        Pattern pattern = Pattern.compile("\\b(\\d{1,4})\\s*gb\\b");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // Fallback: just search for any number matching known sizes
        for (int size : MEMORY_SIZES) {
            if (content.contains(String.valueOf(size))) {
                return String.valueOf(size);
            }
        }

        return "unknown";
    }
}
