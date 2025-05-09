package org.example;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        String z1Path = "src/main/resources/data/Z1.csv";
        String z2Path = "src/main/resources/data/Z2.csv";
        String zy1Path = "src/main/resources/data/ZY1.csv";
        String zy2Path = "src/main/resources/data/ZY2.csv";

        System.out.println("!!!!!!");

        List<Product1> z1Products = CSVReaderUtil.readZ1CSV(z1Path);
        List<Product2> z2Products = CSVReaderUtil.readZ2CSV(z2Path);

        long startTime = System.currentTimeMillis();

        System.out.println("Dataset 1...");
        Map<String, List<Integer>> blocks1 = DuplicateDetector.createBlocksProduct1(z1Products);
        List<MatchPair> matches1 = DuplicateDetector.generateMatchesProduct1(blocks1, z1Products);

        System.out.println("Dataset 2...");
        Map<String, List<Integer>> blocks2 = DuplicateDetector.createBlocksProduct2(z2Products);
        List<MatchPair> matches2 = DuplicateDetector.generateMatchesProduct2(blocks2, z2Products);

        long endTime = System.currentTimeMillis();
        System.out.println("Runtime: " + (endTime - startTime) / 1000.0 + " seconds");

        System.out.println("Evaluation for Dataset 1:");
        List<MatchPair> groundTruth1 = CSVReaderUtil.readGroundTruth(zy1Path);
        Evaluator.evaluate(matches1, groundTruth1);

        System.out.println("Evaluation for Dataset 2:");
        List<MatchPair> groundTruth2 = CSVReaderUtil.readGroundTruth(zy2Path);
        Evaluator.evaluate(matches2, groundTruth2);
    }
}