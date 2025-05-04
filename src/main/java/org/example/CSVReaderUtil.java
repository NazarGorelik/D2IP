package org.example;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CSVReaderUtil {
    public static List<Product1> readZ1CSV(String filePath) throws Exception {
        List<Product1> products = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            reader.readNext();  // skip header
            while ((line = reader.readNext()) != null) {
                int id = Integer.parseInt(line[0].trim());
                String title = line[1].trim();
                products.add(new Product1(id, title));
            }
        }
        return products;
    }

    public static List<Product2> readZ2CSV(String filePath) throws Exception {
        List<Product2> products = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            reader.readNext();  // skip header
            while ((line = reader.readNext()) != null) {
                int id = Integer.parseInt(line[0].trim());
                String name = line[1].trim();
                double price = line[2].isEmpty() ? 0.0 : Double.parseDouble(line[2].trim());
                String brand = line[3].trim();
                String desc = line[4].trim();
                String cat = line[5].trim();
                products.add(new Product2(id, name, price, brand, desc, cat));
            }
        }
        return products;
    }

    public static List<MatchPair> readGroundTruth(String filePath) throws Exception {
        List<MatchPair> groundTruth = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            reader.readNext();  // skip header
            while ((line = reader.readNext()) != null) {
                int id1 = Integer.parseInt(line[0].trim());
                int id2 = Integer.parseInt(line[1].trim());
                groundTruth.add(new MatchPair(id1, id2));
            }
        }
        return groundTruth;
    }
}
