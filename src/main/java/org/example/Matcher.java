package org.example;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.example.model.Pair;
import org.example.model.Product;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Matcher {
    static AtomicInteger paircount = new AtomicInteger();
    public static List<Pair> generateMatches(Map<String, List<Integer>> blocks, List<Product> products, double threshold) {
        List<Pair> candidatePairs = new CopyOnWriteArrayList<>();
        Set<Pair> seenPairs = ConcurrentHashMap.newKeySet();
        Set<Pair> usedIds = new HashSet<>();
        Map<Integer, Product> productById = products.stream()
                .collect(Collectors.toMap(p -> p.id, p -> p));

        AtomicInteger count = new AtomicInteger();
        blocks.entrySet().parallelStream().forEach(block -> {

                    // for (Map.Entry<String, List<Integer>> block : blocks.entrySet()  ) {

            String blockName = block.getKey();
            double fixedsim = 0;
            int unknowncount = countUnknownRegex(blockName);
            usedIds.clear();
            switch (unknowncount) {
                case 0:
                    fixedsim = 0.3;
                    break;
                case 1:
                    fixedsim = 0.1;

                    break;
                case 2:
                    fixedsim = 0.02;
                    break;
                case 3:
                    fixedsim = 0.0;
                    break;
                case 7:
                    fixedsim = 0.05;
                    break;
                    case 8:
                        fixedsim = 0.05;
            }

            List<Integer> rowIds = block.getValue();
            //if (rowIds.size() <= 1000) {
            outer:
            for (int i = 0; i < rowIds.size(); i++) {


                int j=0;
                if (i==0 || i == j) j=i+1;
                inner:
                for (; j < rowIds.size(); j++) {
                    if (i == j&& (j+1) < rowIds.size()-1 ){ j=i+1;}
                    else if (i == j) continue inner;
                    double tempfixedsim = fixedsim;

                    Product p1 = productById.get(rowIds.get(i));
                    Product p2 = productById.get(rowIds.get(j));
                    int id1 = Math.min(p1.id, p2.id);
                    int id2 = Math.max(p1.id, p2.id);
                    Pair p = new Pair(id1, id2);

                    if ( usedIds.contains(p)) {
                        continue inner;
                    }
                    if (p1.brand.equals(p2.brand) && !p1.brand.isEmpty()) {tempfixedsim = tempfixedsim + 0.05;}

                    double jaccardSimilarity = jaccardSimilarity(p1, p2);
                    if (jaccardSimilarity < 0.18) {

                        usedIds.add(p);
                        continue inner;// skip clearly dissimilar
                    }
                    //double levenshteinsim = levenshteinSimilarity(p1, p2);


                    double sim = 0.83 * jaccardSimilarity;  //+ tempfixedsim;// * levenshteinsim

                    if (sim >= (threshold)) {

                        Pair pair = new Pair(id1, id2);
                        //rowIds.remove(i);
                        //rowIds.remove(j-1);
                        //REMOVE PAIR ID IF FOUND TO REDUCE NUMBER OF COMPARISONS- Sami
                        if (seenPairs.add(pair)) {
                            candidatePairs.add(pair);
                            paircount.incrementAndGet();
                            usedIds.add(pair);


                            continue inner;
                        }
                    }else{
                        usedIds.add(p);
                    }
                        /*if (j == rowIds.size() - 1){
                            rowIds.remove(i);
                            i -= 1;
                        }*/

                }
            }
            System.out.println("Gefundene Matches: " + paircount.get());
            System.out.println(count.incrementAndGet());
         });
       // }
      //  }
            /* {
                // 1) Große Blöcke in drei Preis-Sub-Blöcke unterteilen:
                Map<String, List<Integer>> priceBuckets = new HashMap<>();
                for (Integer id : rowIds) {
                    Product p = productById.get(id);
                    Blocker b = new Blocker();
                    String bucket = b.detectPriceWindow(p); // cheap, mid, expensive
                    priceBuckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(id);
                }
                // 2) Dann wieder wie gewohnt innerhalb dieser Sub-Blöcke matchen:
                for (List<Integer> subIds : priceBuckets.values()) {
                    if (subIds.size() <= 1000) {
                        for (int i = 0; i < subIds.size(); i++) {
                            for (int j = i + 1; j < subIds.size(); j++) {
                                Product p1 = productById.get(subIds.get(i));
                                Product p2 = productById.get(subIds.get(j));
                                double jacc0 = jaccardSimilarity(p1, p2);
                                if (jacc0 < 0.2) continue;
                                double sim = 0.6 * jacc0 + 0.4 * levenshteinSimilarity(p1, p2);
                                if (sim >= threshold) {
                                    int id1 = Math.min(p1.id, p2.id);
                                    int id2 = Math.max(p1.id, p2.id);
                                    Pair pair = new Pair(id1, id2);
                                    if (seenPairs.add(pair)) {
                                        candidatePairs.add(pair);
                                        paircount++;
                                    }
                                }
                            }
                        }
                        System.out.println("Gefundene Matches: " + paircount);
                        System.out.println(++count);
                    }
                }

            }
        }*/
        System.out.println(candidatePairs);
        return candidatePairs;
    }



    public static int countUnknownRegex(String text) {
        /*String norm = text.replace('_', ' ')
                .toLowerCase();
        Pattern pat = Pattern.compile("\\bunknown\\b", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m   = pat.matcher(norm);*/
        int count = 0;
        String[] parts = text.split("_", -1);

        if (parts.length >= 3 && "unknown".equalsIgnoreCase(parts[2])) {
            count = 7;
        }else if("unknown".equalsIgnoreCase(parts[0]) ){
            count = 1;}
        else if ( "unknown".equalsIgnoreCase(parts[1])){
            count = 8;
        } else if ("unknown".equalsIgnoreCase(parts[0]) && "unknown".equalsIgnoreCase(parts[1])) {
            count = 2;
        }


        /*while (m.find()) {
            count++;
        }*/
        return count;
    }

    private static double jaccardSimilarity(Product p1, Product p2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(normalize(p1).split(" ")));
        Set<String> set2 = new HashSet<>(Arrays.asList(normalize(p2).split(" ")));
        double bonus = 0.0;
        int s = 0;
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        if (intersection.size() >= 5 ) bonus = 0.1;
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);





        return union.isEmpty() ? 0.0 : ((double) intersection.size() / union.size()) + bonus;
    }

    private static double levenshteinSimilarity(Product p1, Product p2) {
        String s1 = fullText(p1);
        String s2 = fullText(p2);
        LevenshteinDistance ld = new LevenshteinDistance();
        int distance = ld.apply(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        return maxLen == 0 ? 1.0 : 1.0 - ((double) distance / maxLen);
    }

    private static String normalize(Product p) {
        return fullText(p).toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String fullText(Product p) {
        return (p.name + " " + p.brand ).toLowerCase();//" " + p.description " " + p.price
    }
}
