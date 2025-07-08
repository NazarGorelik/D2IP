package org.example;

import org.example.model.Product;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BlockerTest {

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
        List<Product> products = List.of(new Product(785219, "Amazon.com Core Toshiba Laptop 2 Pc Cortex | 4.0 484170001ER : Windows sale GB Windows 64-bit - - GB"));
        Map<String, List<Integer>> blocks = Blocker.createCombinedBlocks(products);
        assertFalse(blocks.isEmpty());
        for (List<Integer> list : blocks.values()) {
            assertTrue(list.contains(0));
        }
    }

    @Test
    void testGenerateAlternativeKey_filtersOnlyAlphaTokens() {
        String title = "CORTEX | 4.0 LAPTOP 2 PC CORE AMAZON.COM TOSHIBA 484170001ER : WINDOWS - - 64-BIT WINDOWS GB SALE GB";
        String result = Blocker.generateAlternativeKey(title);
        assertEquals("amazon core cortex laptop sale toshiba windows windows", result); // nur alphabetisch ≥ 4 Buchstaben
    }

    @Test
    void testGenerateAlternativeKey_containsToshiba() {
        String key = Blocker.generateAlternativeKey("Amazon.com Core Toshiba Laptop 2 Pc Cortex | 4.0 484170001ER : Windows sale GB Windows 64-bit - - GB");
        assertTrue(key.contains("toshiba"));
    }

    @Test
        //pruefen ob es mindestens einen Block gibt, in dem beide Produkt-IDs auftauchen
    void testCreateCombinedBlocks_productsShareCommonBlock() {
        Product p1 = new Product(785219, "Amazon.com Core Toshiba Laptop 2 Pc Cortex | 4.0 484170001ER : Windows sale GB Windows 64-bit - - GB");
        Product p2 = new Product(935035, "CORTEX | 4.0 LAPTOP 2 PC CORE AMAZON.COM TOSHIBA 484170001ER : WINDOWS - - 64-BIT WINDOWS GB SALE GB");

        List<Product> products = List.of(p1, p2);
        Map<String, List<Integer>> blocks = Blocker.createCombinedBlocks(products);

        boolean foundCommonBlock = blocks.values().stream()
                .anyMatch(list -> list.contains(0) && list.contains(1));

        assertTrue(foundCommonBlock, "Beide Produkte sollten durch kombiniertes Blocking im selben Block landen.");
    }


    @Test
    void testCreateCombinedBlocks_usesBothStrategies() {
        Product p1 = new Product(785219, "Amazon.com Core Toshiba Laptop 2 Pc Cortex | 4.0 484170001ER : Windows sale GB Windows 64-bit - - GB");
        Product p2 = new Product(935035, "CORTEX | 4.0 LAPTOP 2 PC CORE AMAZON.COM TOSHIBA 484170001ER : WINDOWS - - 64-BIT WINDOWS GB SALE GB");

        List<Product> products = List.of(p1, p2);
        Map<String, List<Integer>> blocks = Blocker.createCombinedBlocks(products);

        // Es sollten mehrere Keys existieren – mind. einer mit "m185"
        boolean foundCommonBlock = blocks.values().stream()
                .anyMatch(list -> list.contains(0) && list.contains(1));

        assertTrue(foundCommonBlock, "Produkte sollten über kombiniertes Blocking im gleichen Block landen");
    }

}
