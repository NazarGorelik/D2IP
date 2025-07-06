package org.example;

import org.example.model.Product;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BlockerTest {

    @Test
    public void testGenerateBlockingKey_MixedTokens() {
        String title = "Dell 13 9380 i7 16GB SSD512";
        String expected = "13 16gb 9380 i7 ssd512"; // sorted lexically
        String result = Blocker.generateBlockingKey(title);
        assertEquals(expected, result);
    }

    @Test
    public void testGenerateBlockingKey_NoDigits() {
        String title = "Apple MacBook Pro";
        String result = Blocker.generateBlockingKey(title);
        assertEquals("", result, "Should return empty string when no digit-containing words found.");
    }

    @Test
    public void testGenerateBlockingKey_DuplicateTokens() {
        String title = "Lenovo ThinkPad X1 X1 Carbon i5 i5";
        String expected = "i5 i5 x1 x1"; // all matching tokens with digits, duplicates preserved
        String result = Blocker.generateBlockingKey(title);
        assertEquals(expected, result);
    }

    @Test
    public void testCreateBlocks_GroupsByBlockingKey() {
        List<Product> products = List.of(
                new Product(1, "HP EliteBook 850 G5 i5 8GB 256GB SSD"),
                new Product(2, "HP EliteBook 850 G5 i5 8GB 256GB SSD"),
                new Product(3, "MacBook Air") // should not be included
        );

        Map<String, List<Integer>> blocks = Blocker.createBlocks(products);
        assertEquals(1, blocks.size());

        String expectedKey = Blocker.generateBlockingKey(products.get(0).title);
        assertTrue(blocks.containsKey(expectedKey));
        assertEquals(List.of(0, 1), blocks.get(expectedKey));
    }

    @Test
    public void testCreateBlocks_EmptyTitlesAreIgnored() {
        List<Product> products = List.of(
                new Product(1, ""),
                new Product(2, "Test 128GB SSD"),
                new Product(3, "NoDigitsHere")
        );

        Map<String, List<Integer>> blocks = Blocker.createBlocks(products);

        assertEquals(1, blocks.size()); // Only one should have a blocking key
        assertTrue(blocks.values().stream().flatMap(List::stream).anyMatch(i -> i == 1));
    }

    @Test
    void testGenerateBlockingKey_alphanumericPattern() {
        String title = "SanDisk Ultra 32GB SDHC UHS-I";
        String result = Blocker.generateBlockingKey(title);
        assertEquals("32gb", result); // erwartet Tokens mit mindestens einer Ziffer
    }

    @Test
    void testGenerateBlockingKey_emptyInput() {
        String title = "";
        String result = Blocker.generateBlockingKey(title);
        assertEquals("", result);
    }

    @Test
    void testCreateBlocks_singleProduct() {
        List<Product> products = List.of(new Product(0, "SanDisk Ultra 32GB SDHC UHS-I"));
        Map<String, List<Integer>> blocks = Blocker.createBlocks(products);
        assertFalse(blocks.isEmpty());
        for (List<Integer> list : blocks.values()) {
            assertTrue(list.contains(0));
        }
    }

    @Test
    void testGenerateAlternativeKey_filtersOnlyAlphaTokens() {
        String title = "USB3 64GB SanDisk Ultra SDHC";
        String result = Blocker.generateBlockingKey(title);
        assertTrue(result.contains("64gb")); // nur alphabetisch ≥ 4 Buchstaben
    }

    @Test
    void testGenerateAlternativeKey_containsKingston() {
        String key = Blocker.generateBlockingKey("Kingston DataTraveler 128GB USB 3.0");
        assertTrue(key.contains("128"));
    }

    @Test
    void testCreateAlternativeBlocks_combinedKey() {
        Product p1 = new Product(0, "Lexar USB Stick 128GB");
        Product p2 = new Product(1, "Kingston USB Drive 64GB");

        List<Product> products = List.of(p1, p2);
        Map<String, List<Integer>> blocks = Blocker.createBlocks(products);

        // Erwarteter kombinierter Key (alphabetisch sortiert, ≥4 Buchstaben, keine Zahlen)
        String expectedKey1 = "64gb";      // "usb" wird gefiltert, da nur 3 Buchstaben
        String expectedKey2 = "128gb";   // "usb" auch hier raus

        assertTrue(blocks.containsKey(expectedKey1));
        assertTrue(blocks.get(expectedKey1).contains(1));

        assertTrue(blocks.containsKey(expectedKey2));
        assertTrue(blocks.get(expectedKey2).contains(0));
    }
}
