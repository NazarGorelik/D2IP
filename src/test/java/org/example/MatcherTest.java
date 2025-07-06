package org.example;

import org.example.model.Pair;
import org.example.model.Product;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MatcherTest {
    @Test
    public void testJaccardSimilarity_IdenticalStrings() {
        String s1 = "sandisk ultra 128gb";
        String s2 = "sandisk ultra 128gb";
        double sim = Matcher.jaccardSimilarity(s1, s2);
        assertEquals(1.0, sim, 0.0001);
    }

    @Test
    public void testJaccardSimilarity_PartialOverlap() {
        String s1 = "sandisk ultra 128gb";
        String s2 = "sandisk extreme 128gb";
        double sim = Matcher.jaccardSimilarity(s1, s2);
        assertTrue(sim > 0.4 && sim < 1.0);
    }

    @Test
    public void testJaccardSimilarity_NoOverlap() {
        String s1 = "kingston 64gb";
        String s2 = "toshiba 128gb";
        double sim = Matcher.jaccardSimilarity(s1, s2);
        assertTrue(sim == 0.0);
    }

    @Test
    public void testLevenshteinSimilarity_Identical() {
        String s1 = "sandisk ultra 128gb";
        String s2 = "sandisk ultra 128gb";
        double sim = Matcher.levenshteinSimilarity(s1, s2);
        assertEquals(1.0, sim, 0.0001);
    }

    @Test
    public void testLevenshteinSimilarity_PartialMatch() {
        String s1 = "sandisk ultra 128gb";
        String s2 = "sandisk extreme 128gb";
        double sim = Matcher.levenshteinSimilarity(s1, s2);
        assertTrue(sim > 0.5 && sim < 1.0);
    }

    @Test
    public void testLevenshteinSimilarity_EmptyStrings() {
        double sim = Matcher.levenshteinSimilarity("", "");
        assertEquals(1.0, sim, 0.000002);
    }

    @Test
    public void testGenerateMatches_JaccardMatch() {
        List<Product> products = List.of(
                new Product(1, "sandisk ultra 128gb sdxc"),
                new Product(2, "sandisk 128gb ultra sdxc"),
                new Product(3, "kingston 64gb usb")
        );

        Map<String, List<Integer>> blocks = new HashMap<>();
        blocks.put("block1", List.of(0, 1, 2));

        List<Pair> matches = Matcher.generateMatches(blocks, products, 0.5, Matcher.SimilarityType.JACCARD);
        assertTrue(matches.contains(new Pair(1, 2)));
        assertEquals(1, matches.size());
    }

    @Test
    public void testGenerateMatches_CombinedMatch() {
        List<Product> products = List.of(
                new Product(1, "sandisk ultra 128gb sdxc"),
                new Product(2, "sandisk 128gb sdxc ultra")
        );

        Map<String, List<Integer>> blocks = new HashMap<>();
        blocks.put("block1", List.of(0, 1));

        List<Pair> matches = Matcher.generateMatches(blocks, products, 0.8, Matcher.SimilarityType.COMBINED);
        assertEquals(1, matches.size());
        assertEquals(new Pair(1, 2), matches.get(0));
    }

    @Test
    public void testGenerateMatches_ThresholdTooHigh() {
        List<Product> products = List.of(
                new Product(1, "sandisk ultra 128gb sdxc"),
                new Product(2, "sandisk 128gb sdxc ultra")
        );

        Map<String, List<Integer>> blocks = new HashMap<>();
        blocks.put("block1", List.of(0, 1));

        List<Pair> matches = Matcher.generateMatches(blocks, products, 0.99, Matcher.SimilarityType.COMBINED);
        assertTrue(matches.isEmpty());
    }

    @Test
    public void testGenerateMatches_LargeBlockIgnored() {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            products.add(new Product(i, "generic title " + i));
        }

        Map<String, List<Integer>> blocks = Map.of("tooLarge", new ArrayList<>(Collections.nCopies(101, 0)));
        List<Pair> matches = Matcher.generateMatches(blocks, products, 0.5, Matcher.SimilarityType.JACCARD);

        assertEquals(0, matches.size());
    }
}
