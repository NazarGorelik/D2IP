package org.example;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.example.model.Product;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



public class Blocker {




    private static final List<String> BRANDS = loadbrands();/*Arrays.asList(
            "intenso", "kingston", "lexar", "pny", "samsung", "sandisk", "sony", "toshiba", "transcend"
    );*/
    private static final Set<String> BRAND_SET = new HashSet<>(BRANDS);
    static final Pattern ALL_BRANDS = Pattern.compile(
            "(?i)" + "(" + BRAND_SET.stream().map(Pattern::quote).collect(Collectors.joining("|")) +")"
    );
    private static final List<String> STORAGE_TYPES = Arrays.asList(
            "xqd", "compactflash", "micro sd", "microsd", "sdxc", "sdhc", "usb", "ssd", "hdd", "sd", "cd"
    );
    private static final List<String> MEMORY_SIZES = Arrays.asList(
           // "2048gb", "1024gb", "512gb", "256gb", "128gb", "64gb", "32gb", "16gb", "8gb", "4gb",
            "2048", "1024", "512", "256", "200", "128", "64", "32", "16", "8", "4"
    );

    private static final Map<String, String> STORAGE_ALIASES = new LinkedHashMap<>();
    static {

        STORAGE_ALIASES.put("usb[ -]?adapter",   "zubehör");
        STORAGE_ALIASES.put("micro[ -]?sdxc[ -]?adapter",   "zubehör");
        STORAGE_ALIASES.put("micro[ -]?sdhc[ -]?adapter",   "zubehör");
        STORAGE_ALIASES.put("micro[ -]?sd[ -]?adapter",   "zubehör");
        STORAGE_ALIASES.put("adapter",   "zubehör");
        STORAGE_ALIASES.put("xqd",            "xqd");
        STORAGE_ALIASES.put("compact ?flash", "compactflash");
        STORAGE_ALIASES.put("micro\\b.*?\\bsdxc", "microsdxc");
        STORAGE_ALIASES.put("micro\\b.*?\\bsdhc", "microsdhc");
        STORAGE_ALIASES.put("micro\\b.*?\\bsd", "microsd");
        STORAGE_ALIASES.put("micro[ -]?sdhc", "microsdhc");
        STORAGE_ALIASES.put("micro[ -]?sdxc", "microsdxc");
        STORAGE_ALIASES.put("micro[ -]?sd",   "microsd");
        STORAGE_ALIASES.put("sdxc",           "sdxc");
        //STORAGE_ALIASES.put("2.0",           "usb");
        //STORAGE_ALIASES.put("3.0",           "usb");
        STORAGE_ALIASES.put("speicherkarte",           "sd");
        STORAGE_ALIASES.put("sdhc",           "sdhc");
        STORAGE_ALIASES.put("ssd",            "ssd");
        STORAGE_ALIASES.put("hdd",            "hdd");
        STORAGE_ALIASES.put("\\bsd\\b",       "sd");
        STORAGE_ALIASES.put("sd card",        "sd");
        STORAGE_ALIASES.put("memory card",    "sd");
        STORAGE_ALIASES.put("\\busb\\b(?![ -]?adapter)", "usb");
        STORAGE_ALIASES.put("cd",             "cd");
        STORAGE_ALIASES.put("extreme",             "extreme");
    }

    private static final List<Map.Entry<Pattern,String>> STORAGE_PATTERNS =
            STORAGE_ALIASES.entrySet()
                    .stream()
                    .map(e -> Map.entry(
                            Pattern.compile("\\b(?i)" + e.getKey() + "\\b"),
                            e.getValue()))
                    .toList();
    /**
     * Single-pass blocking: by Brand, StorageType, and MemorySize
     */
    public static Map<String, List<Integer>> createBlocks(List<Product> products) {
        Map<String, List<Integer>> blocks = new HashMap<>();
        Map<String, List<Product>> blocksTest = new HashMap<>();
        for (Product p : products) {
            String brand = blockByBrand(p);
            String type = detectStorageType(p);
//            String size = detectMemorySize(p);
            String size = detectMemorySizeBasedOnEnum(p);
            String key = String.join("_", brand, type, size); //, size
            blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(p.id);
            blocksTest.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
        }
        // remove blocks with only one element
        blocks.entrySet().removeIf(entry -> entry.getValue().size() < 2);
        blocksTest.entrySet().removeIf(entry -> entry.getValue().size() < 2);
        return blocks;
    }

    public static String unify(){
        return null;
    }

    public static String blockByBrand(Product product) {
        String brand = product.brand.toLowerCase();
        String blockKey = BRAND_SET.contains(brand) ? brand : "";

        if(blockKey.isEmpty()) {
            blockKey = reassignMissingBrandsByName(product);
        }
        return blockKey;
    }

    /**
     * Reassigns products with missing brand to existing brand blocks based on product name.
     *
     **/
     public static String reassignMissingBrandsByName(Product product) {
         String name = product.brand.toLowerCase() + " " + product.price + " " + product.description + " " + product.name;
         name.toLowerCase(Locale.ROOT);
         Matcher m = ALL_BRANDS.matcher(name);
         if (m.find()) {
             return m.group().toLowerCase();
         }
         return "unknown";
     }

    public static List<String> loadbrands(){
        Object Reader;
        List<String> row = new ArrayList<>();

        String[] line = new String[10];
        int count = 0;
        try {
            Reader input = new InputStreamReader(Files.newInputStream(Path.of("src/main/resources/dataset_2/brandsstoragedevices.csv")));
            CSVReader reader = new CSVReaderBuilder(input).build();

            while ((line = reader.readNext()) != null){
                row.add(Arrays.toString(line).toLowerCase().replace("[", "").replace("]", ""));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return row;
    }

    /**
     * Detects storage type using word boundaries; checks rarer types first
     */
    public static String detectStorageType(Product p) {
        String txt = (p.name + " " + p.price + " " + p.description + " " + p.brand)
                .toLowerCase(Locale.ROOT);

        for (Map.Entry<Pattern, String> e : STORAGE_PATTERNS) {
            if (e.getKey().matcher(txt).find()) {
                return e.getValue();
            }
        }
        return "unknown";
        /*String txt = (p.name + " " + p.price+ " " + p.description).toLowerCase();
        String result = "";
        List<String> matched = new ArrayList<>();
        for (Map.Entry<Pattern, String> e : STORAGE_PATTERNS) {
            if (e.getKey().matcher(txt).find()) {
                String val = e.getValue();
                if (!matched.contains(val)) {
                    matched.add(val);
                }
            }
        }
        matched.sort(Comparator.comparingInt(String::length).reversed());

        StringBuilder sb = new StringBuilder();
        for (String part : matched) {
            sb.append(part);
        }
        result = sb.toString();
        if (result.equals("")) {
            return "unknown";
        }
        return result;*/
    }
    /**
     * Ermitteln der memory size
     * wichtig hier schliesse konstrukte wie 2.0 gb aus
     *
     */
//    private static String detectMemorySize(Product p) {
//        String text = (p.name + " " + p.description).toLowerCase();
//        StringTokenizer st = new StringTokenizer(text);
//        String prev = null;
//
//        while (st.hasMoreTokens()) {
//            String token = st.nextToken().trim();
//            if (token.isEmpty()) {
//                prev = token;
//                continue;
//            }
//            String lower = token.toLowerCase();
//            // Erster Fall: gb ohne leerzeichen
//            if (lower.endsWith("gb")) {
//                String num = lower.substring(0, lower.length() - 2);
//                // Skip floats like "2.0"
//                if (!num.contains(".") && !num.contains(",")) {
//                    boolean digitsOnly = true;
//                    for (char c : num.toCharArray()) {
//                        if (!Character.isDigit(c)) {
//                            digitsOnly = false;
//                            break;
//                        }
//                    }
//                    if (digitsOnly && !num.isEmpty()) {
//                        return num;
//                    }
//                }
//            }
//            // hier gibt es ein leerzeichen
//            if ("gb".equals(lower) && prev != null) {
//                String num = prev;
//                if (!num.contains(".") && !num.contains(",")) {
//                    boolean digitsOnly = true;
//                    for (char c : num.toCharArray()) {
//                        if (!Character.isDigit(c)) {
//                            digitsOnly = false;
//                            break;
//                        }
//                    }
//                    if (digitsOnly && !num.isEmpty()) {
//                        return num;
//                    }
//                }
//            }
//            prev = lower;
//        }
//
//        return detectMemorySizeBasedOnEnum(text);
//    }

    public static String detectMemorySizeBasedOnEnum(Product p){
        String text = p.name.toLowerCase() + " " + p.description.toLowerCase();
        List<String> words = Arrays.asList(text.split(" "));
        //String text = (p.name + " " + p.description).toLowerCase(Locale.ROOT);

        for (String sz : MEMORY_SIZES) {
            String regexGb =   Pattern.quote(sz) + "\\s*gb";
            if (Pattern.compile(regexGb, Pattern.CASE_INSENSITIVE).matcher(text).find()) {
                return sz;
            }
        }


        for (String sz : MEMORY_SIZES) {
            String regexNum = "(?<!\\d)"
                    + Pattern.quote(sz)
                    + "(?![A-Za-z\\d])";
            if (Pattern.compile(regexNum).matcher(text).find()) {
                return sz;
            }
        }
        /*for (String sz : MEMORY_SIZES) {
            // split by space and check if word is equal to memory_size. otherwise unknown
            if (words.contains(String.valueOf(sz))) {
                return String.valueOf(sz);
            }
        }

        // check if memory size is concatenated with other words. "sun64samsung" -> 64 gb
        for (String sz : MEMORY_SIZES) {
            // split by space and check if word is equal to memory_size. otherwise unknown
            String regex = "(?<!\\d)" + sz + "(?!\\d)";

            if (Pattern.compile(regex).matcher(text).find()) {
                return String.valueOf(sz);
            }
        }*/
        return "unknown";
    }

    public static String detectPriceWindow(Product p) {
        String str = p.price != null
                ? p.price.replaceAll("[^0-9.,]", "").replace(',', '.')
                : "";
        try {
            double pr = Double.parseDouble(str);
            if (pr <= 15.99) return "cheap";
            if (pr <= 99.99) return "mid";
            return "expensive";
        } catch (NumberFormatException e) {
            return "unknown";
        }
    }
}
    /*
    private static final List<String> BRANDS = Arrays.asList(
            "intenso", "kingston", "lexar", "pny", "samsung", "sandisk", "sony", "toshiba", "transcend"
    );

    private static final List<String> STORAGE_TYPES = Arrays.asList(
            "usb", "ssd", "cd", "hdd", "sd", "micro sd", "microsd", "xqd", "compactflash", "cf", "sdhc", "sdxc"
    );

    private static final List<Integer> MEMORY_SIZES = Arrays.asList(
            4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048
    );
    private static final Pattern GB_PATTERN = Pattern.compile("\\b(\\d{1,4})\\s*gb\\b");


    // Pass A: Brand + StorageType + MemorySize
    public static Map<String, List<Integer>> blockByBrandTypeSize(List<Product> prods) {
        Map<String, List<Integer>> blocks = new HashMap<>();
        for (Product p : prods) {
            String brand = normalize(p.brand);
            String type = detectStorageType(p);
            String size = detectMemorySize(p);
            String key = brand + "_" + type + "_" + size;
            blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(p.id);
        }
        return blocks;
    }

    // Pass B: Preis-Bucket (100er) + Marke
    public static Map<String, List<Integer>> blockByPriceBrand(List<Product> prods) {
        Map<String, List<Integer>> blocks = new HashMap<>();
        for (Product p : prods) {
            String bucket = detectPriceBucket(p);
            String brand = normalize(p.brand);
            String key = bucket + "_" + brand;
            blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(p.id);
        }
        return blocks;
    }

    // Optional additional passes
    public static Map<String, List<Integer>> blockByBrandType(List<Product> prods) {
        Map<String, List<Integer>> blocks = new HashMap<>();
        for (Product p : prods) {
            String brand = normalize(p.brand);
            String type = detectStorageType(p);
            String key = brand + "_" + type;
            blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(p.id);
        }
        return blocks;
    }

    public static Map<String, List<Integer>> blockByBrandSize(List<Product> prods) {
        Map<String, List<Integer>> blocks = new HashMap<>();
        for (Product p : prods) {
            String brand = normalize(p.brand);
            String size = detectMemorySize(p);
            String key = brand + "_" + size;
            blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(p.id);
        }
        return blocks;
    }

    public static Map<String, List<Integer>> blockByTypeSize(List<Product> prods) {
        Map<String, List<Integer>> blocks = new HashMap<>();
        for (Product p : prods) {
            String type = detectStorageType(p);
            String size = detectMemorySize(p);
            String key = type + "_" + size;
            blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(p.id);
        }
        return blocks;
    }

    public static Map<String, List<Integer>> blockByFirstToken(List<Product> prods) {
        Map<String, List<Integer>> blocks = new HashMap<>();
        for (Product p : prods) {
            String token = p.name.toLowerCase()
                    .replaceAll("[^a-z0-9 ]", " ")
                    .split("\\s+")[0];
            blocks.computeIfAbsent(token, k -> new ArrayList<>()).add(p.id);
        }
        return blocks;
    }


    private static String detectPriceBucket(Product p) {
        String str = p.price != null
                ? p.price.replaceAll("[^0-9.,]", "").replace(',', '.')
                : "";
        try {
            double pr = Double.parseDouble(str);
            int b = (int) (pr / 100);
            return (b * 100) + "-" + (b * 100 + 99);
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static String detectPriceWindow(Product p) {
        String str = p.price != null
                ? p.price.replaceAll("[^0-9.,]", "").replace(',', '.')
                : "";
        try {
            double pr = Double.parseDouble(str);
            if (pr <= 15.99) return "cheap";
            if (pr <= 99.99) return "mid";
            return "expensive";
        } catch (NumberFormatException e) {
            return "unknown";
        }
    }


    private static String normalize(String s) {
        if (s == null) return "";
        String b = s.toLowerCase().trim();
        return BRANDS.contains(b) ? b : "";
    }

    private static String detectStorageType(Product p) {
        String txt = (p.name + " " + p.description).toLowerCase();
        for (String t : STORAGE_TYPES) {
            // \b ensures matching whole words, so 'sd' won't match inside 'sdxc'
            Pattern pat = Pattern.compile("\\b" + Pattern.quote(t) + "\\b");
            if (pat.matcher(txt).find()) {
                return t.replace(" ", "");
            }
        }
        return "unknown";
    }

    private static String detectMemorySize(Product p) {
        String text = (p.name + " " + p.description).toLowerCase();
        // Tokenize by whitespace without regex
        StringTokenizer st = new StringTokenizer(text);
        String prev = null;
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            if (token.isEmpty()) continue;
            // Check pattern: "<number>gb" suffix
            String lower = token.toLowerCase();
            if (lower.endsWith("gb")) {
                String num = lower.substring(0, lower.length() - 2);
                boolean digitsOnly = true;
                for (char c : num.toCharArray()) {
                    if (!Character.isDigit(c)) { digitsOnly = false; break; }
                }
                if (digitsOnly && !num.isEmpty()) {
                    return num;
                }
            }
            // Check case: "<number>" followed by "gb"
            if ("gb".equals(lower) && prev != null) {
                boolean digitsOnly = true;
                for (char c : prev.toCharArray()) {
                    if (!Character.isDigit(c)) { digitsOnly = false; break; }
                }
                if (digitsOnly && !prev.isEmpty()) {
                    return prev;
                }
            }
            prev = token;
        }
        return "unknown";
    }

for (int sz : MEMORY_SIZES) {
        if (txt.contains(String.valueOf(sz))) {
        return String.valueOf(sz);
            }
                    }
                    return "unknown";
                    }
                    }
/*
    private static String detectMemorySize(Product p) {
        String text = (p.name + " " + p.description).toLowerCase();
        String[] tokens = text.split("\\s+");

        for (int i = 0; i < tokens.length - 1; i++) {
            String current = tokens[i];
            String next = tokens[i + 1];

            if (next.equals("gb")) {
                // check if current is an integer and not a float
                if (current.chars().allMatch(Character::isDigit)) {
                    return current;
                }
            }
        }

        // fallback: check if any known sizes are in the string
        for (int size : MEMORY_SIZES) {
            if (text.contains(String.valueOf(size) + "gb")) {
                return String.valueOf(size);
            }
        }

        return "unknown";
    }
}

 */

    /*// Pass A: Preis-Bucket (100er) + Marke
    public static Map<String,List<Integer>> blockByPriceBrand(List<Product> prods) {
        Map<String,List<Integer>> blocks = new HashMap<>();
        for (Product p:prods) {
            String bucket = detectPriceBucket(p);
            String brand = normalize(p.brand);
            String key = bucket + "_" + brand;
            blocks.computeIfAbsent(key, k->new ArrayList<>()).add(p.id);
        }
        return blocks;
    }

    // Pass B: Marke + StorageType
    public static Map<String,List<Integer>> blockByBrandType(List<Product> prods) {
        Map<String,List<Integer>> blocks = new HashMap<>();
        for (Product p:prods) {
            String brand = normalize(p.brand);
            String type  = detectStorageType(p);
            String key = brand + "_" + type;
            blocks.computeIfAbsent(key, k->new ArrayList<>()).add(p.id);
        }
        return blocks;
    }

    // Pass C: Marke + Speichergröße
    public static Map<String,List<Integer>> blockByBrandSize(List<Product> prods) {
        Map<String,List<Integer>> blocks = new HashMap<>();
        for (Product p:prods) {
            String brand = normalize(p.brand);
            String size  = detectMemorySize(p);
            String key = brand + "_" + size;
            blocks.computeIfAbsent(key, k->new ArrayList<>()).add(p.id);
        }
        return blocks;
    }

    // Pass D: StorageType + Speichergröße
    public static Map<String,List<Integer>> blockByTypeSize(List<Product> prods) {
        Map<String,List<Integer>> blocks = new HashMap<>();
        for (Product p:prods) {
            String type = detectStorageType(p);
            String size = detectMemorySize(p);
            String key = type + "_" + size;
            blocks.computeIfAbsent(key, k->new ArrayList<>()).add(p.id);
        }
        return blocks;
    }

    // Pass E: Erstes Namens-Token
    public static Map<String,List<Integer>> blockByFirstToken(List<Product> prods) {
        Map<String,List<Integer>> blocks = new HashMap<>();
        for (Product p:prods) {
            String token = p.name.toLowerCase()
                    .replaceAll("[^a-z0-9 ]"," ")
                    .split("\\s+")[0];
            blocks.computeIfAbsent(token, k->new ArrayList<>()).add(p.id);
        }
        return blocks;
    }

    // Helpers:

    private static String detectPriceBucket(Product p) {
        String str = p.price!=null
                ? p.price.replaceAll("[^0-9.,]","").replace(',','.')
                : "";
        try {
            double pr = Double.parseDouble(str);
            int b = (int)(pr/100);
            return (b*100) + "-" + (b*100+99);
        } catch(Exception e) {
            return "unknown";
        }
    }

    private static String normalize(String s) {
        if (s==null) return "";
        s = s.toLowerCase().trim();
        return BRANDS.contains(s) ? s : "";
    }

    private static String detectStorageType(Product p) {
        String txt = (p.name+" "+p.description).toLowerCase();
        for (String t:STORAGE_TYPES) {
            if (txt.contains(t)) return t.replace(" ","");
        }
        return "unknown";
    }

    private static String detectMemorySize(Product p) {
        String txt = (p.name+" "+p.description).toLowerCase();
        Matcher m = GB_PATTERN.matcher(txt);
        if (m.find()) return m.group(1);
        for (int sz:MEMORY_SIZES) {
            if (txt.contains(String.valueOf(sz))) return String.valueOf(sz);
        }
        return "unknown";
    }
}*/
    /*
    public static Map<String, List<Integer>> createBlocks(List<Product> products) {


        Map<String, List<Integer>> blocks = new HashMap<>();

        for (Product p : products) {
            String priceBucket = detectPriceBucket(p);
            String brandKey = normalize(p.brand);
            String storageType = detectStorageType(p);
            String memorySize = detectMemorySize(p);

            String blockKey = String.join("_", priceBucket, brandKey, storageType, memorySize);
            blocks.computeIfAbsent(blockKey, k -> new ArrayList<>()).add(p.id);
        }

        return blocks;
    }

    /**
     * Determines the 100€ interval for the product price.
     * Buckets: 0-99, 100-199, 200-299, ...
     *
    private static String detectPriceBucket(Product p) {
        String priceStr = p.price != null ? p.price.replaceAll("[^0-9.,]", "").replace(',', '.') : "";
        try {
            double price = Double.parseDouble(priceStr);
            int bucket = (int) (price / 100);
            int low = bucket * 100;
            int high = low + 99;
            return low + "-" + high;
        } catch (NumberFormatException e) {
            return "unknown";
        }
    }

    /**
     * Detects storage type by checking known keywords in name+description.
     *
    private static String detectStorageType(Product p) {
        String text = (p.name + " " + p.description).toLowerCase();
        for (String type : STORAGE_TYPES) {
            if (text.contains(type)) {
                return type.replace(" ", "");
            }
        }
        return "unknown";
    }

    /**
     * Detects memory size by regex for 'xxgb' or fallback search in known sizes.
     *
    private static String detectMemorySize(Product p) {
        String content = (p.name + " " + p.description).toLowerCase();
        Pattern pattern = Pattern.compile("\\b(\\d{1,4})\\s*gb\\b");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        for (int size : MEMORY_SIZES) {
            if (content.contains(String.valueOf(size))) {
                return String.valueOf(size);
            }
        }
        return "unknown";
    }

    /**
     * Normalizes brand string to lowercase and trims, empty if null or not known.
     *
    private static String normalize(String s) {
        if (s == null) return "";
        String b = s.toLowerCase().trim();
        return BRANDS.contains(b) ? b : "";
    }
}


     */

    /*public static Map<String, List<Integer>> createBlocks(List<Product> products) {
        Map<String, List<Integer>> blocks = new HashMap<>();

        // 1) Assign each product to a price-bucket + brand block
        for (Product p : products) {
            String priceBucket = detectPriceBucket(p);
            String brandKey = normalize(p.brand);
            String blockKey = priceBucket + "_" + brandKey;

            blocks.computeIfAbsent(blockKey, k -> new ArrayList<>()).add(p.id);
        }

        return blocks;
    }

    /**
     * Determines the 10€ interval for the product price.
     * Buckets: 0-9, 10-19, 20-29, ...
     *
    private static String detectPriceBucket(Product p) {
        String priceStr = p.price != null ? p.price.replaceAll("[^0-9.,]", "").replace(',', '.') : "";
        try {
            double price = Double.parseDouble(priceStr);
            int bucket = (int) (price / 10);
            int low = bucket * 10;
            int high = low + 9;
            return low + "-" + high;
        } catch (NumberFormatException e) {
            return "unknown";
        }
    }

    /**
     * Normalizes a brand string: lowercase, trimmed, empty if null.
     *
    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase().trim();
    }
}

     */

    /*public static Map<String, List<Integer>> createBlocks(List<Product> products) {
        Map<String, List<Integer>> layeredBlocks = new HashMap<>();

        // 1) Block by brand
        Map<String, List<Product>> brandBlocks = blockByBrand(products);
        // 2) Reassign missing brands based on name
        reassignMissingBrandsByName(brandBlocks);

        // 3) For each brand block, further subdivide by memory size
        for (Map.Entry<String, List<Product>> entry : brandBlocks.entrySet()) {
            String brandKey = entry.getKey();
            for (Product p : entry.getValue()) {
                String memorySize = detectMemorySize(p);
                String blockKey = brandKey + "_" + memorySize;
                layeredBlocks
                        .computeIfAbsent(blockKey, k -> new ArrayList<>())
                        .add(p.id);
            }
        }

        return layeredBlocks;
    }

    /**
     * Extracts memory size from product name or description.
     *
    private static String detectMemorySize(Product p) {
        String content = (p.name + " " + p.description).toLowerCase();
        // Try pattern like '32gb'
        Pattern pattern = Pattern.compile("\\b(\\d{1,4})\\s*gb\\b");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Fallback: search known sizes
        for (int size : MEMORY_SIZES) {
            if (content.contains(String.valueOf(size))) {
                return String.valueOf(size);
            }
        }
        return "unknown";
    }

    /**
     * Groups products by normalized brand key (empty for unknown).
     *
    public static Map<String, List<Product>> blockByBrand(List<Product> products) {
        Map<String, List<Product>> brandBlocks = new HashMap<>();
        for (Product p : products) {
            String brand = normalize(p.brand);
            String blockKey = BRANDS.contains(brand) ? brand : "";
            brandBlocks
                    .computeIfAbsent(blockKey, k -> new ArrayList<>())
                    .add(p);
        }
        return brandBlocks;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase().trim();
    }

    /**
     * Reassigns products with missing brand to existing brand blocks based on product name.
     *
    public static void reassignMissingBrandsByName(Map<String, List<Product>> brandBlocks) {
        List<Product> noBrand = brandBlocks.getOrDefault("", new ArrayList<>());
        Map<String, List<Product>> updated = new HashMap<>(brandBlocks);
        updated.remove("");

        Set<String> known = brandBlocks.keySet().stream()
                .filter(b -> !b.isEmpty())
                .collect(Collectors.toSet());

        for (Product p : noBrand) {
            String nameLower = p.name.toLowerCase();
            boolean reassigned = false;
            for (String brand : known) {
                if (nameLower.contains(brand)) {
                    updated.get(brand).add(p);
                    reassigned = true;
                    break;
                }
            }
            if (!reassigned) {
                updated.computeIfAbsent("", k -> new ArrayList<>()).add(p);
            }
        }

        brandBlocks.clear();
        brandBlocks.putAll(updated);
    }
}

     */
