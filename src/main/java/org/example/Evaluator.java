package org.example;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Evaluator {
    public static void evaluate(List<MatchPair> candidates, List<MatchPair> groundTruth) {
        Set<MatchPair> candidateSet = new HashSet<>(candidates);
        Set<MatchPair> groundTruthSet = new HashSet<>(groundTruth);

        int tp = 0;
        for (MatchPair mp : candidateSet) {
            if (groundTruthSet.contains(mp)) tp++;
        }
        int fp = candidateSet.size() - tp;
        int fn = groundTruthSet.size() - tp;

        double recall = tp / (double) (tp + fn);
        double precision = tp / (double) (tp + fp);
        double f1 = (2 * precision * recall) / (precision + recall);

        System.out.println("Pairs: " + candidateSet.size());
        System.out.println("TP: " + tp);
        System.out.println("FP: " + fp);
        System.out.println("FN: " + fn);
        System.out.println("Recall: " + recall);
        System.out.println("Precision: " + precision);
        System.out.println("F1: " + f1);
    }
}
