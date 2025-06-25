package org.example;

import org.example.model.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Evaluator {

    public static void evaluate(List<Pair> candidates, List<Pair> groundTruth) {
        Set<Pair> predicted = new HashSet<>(candidates);
        Set<Pair> actual = new HashSet<>(groundTruth);

        Set<Pair> tp = new HashSet<>(predicted);
        tp.retainAll(actual);

        Set<Pair> fp = new HashSet<>(predicted);
        fp.removeAll(actual);

        Set<Pair> fn = new HashSet<>(actual);
        fn.removeAll(predicted);

        double precision = tp.size() + fp.size() == 0 ? 0 : tp.size() / (double) (tp.size() + fp.size());
        double recall = tp.size() + fn.size() == 0 ? 0 : tp.size() / (double) (tp.size() + fn.size());
        double f1 = precision + recall == 0 ? 0 : 2 * precision * recall / (precision + recall);

        System.out.println("------------- Evaluation -------------");
        System.out.println("Reported # of Pairs: " + predicted.size());
        System.out.println("TP: " + tp.size());
        System.out.println("FP: " + fp.size());
        System.out.println("FN: " + fn.size());
        System.out.printf("Recall:    %.4f\n", recall);
        System.out.printf("Precision: %.4f\n", precision);
        System.out.printf("F1 Score:  %.4f\n", f1);
    }
}
