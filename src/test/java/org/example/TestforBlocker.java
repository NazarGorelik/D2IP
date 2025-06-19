package org.example;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.example.model.Product;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/* Gedanken und Einfälle :

   Welche Testfälle gibt es?

   1. "Normaler" Input - Marke wird richtig bestimmt. -> *_... => FERTIG
   2. "Normaler" Input - Speichertyp wird richtig bestimmt. -> ..._*_... => FERTIG
   3. "Normaler" Input - Speichergröße wird richtig bestimmt. -> ..._..._* => FERTIG
   4. "Abnormaler" Input - Marke kann nicht bestimmt werden. -> unknown_*_* => FERTIG
   5. "Abnormaler" Input - Speichertyp kann nicht bestimmt werden. -> *_unknown_* => FERTIG
   6. "Abnormaler" Input - Speichergröße kann nicht bestimmt werden. -> *_*_unknown => FERTIG
   7.  Kombinationen aus vorangegangenen Fälle => FERTIG

   FALLEN EUCH WEITERE SZENARIOS EIN DIE GETESTET WERDEN MÜSSEN?
    */
class TestforBlocker {

    //1. "Normaler" Input - Marke wird richtig bestimmt. -> *_...
    @Test
    void testbrandblockingkeynormalinput(){


        String input =  "86093,\"SANDISK Extreme UHS-I Micro-SDXC Speicherkarte, 64 GB, 100 MB/s, UHS Class 1\",39.99,,,";
        String output = "";
        try (CSVReader reader = new CSVReaderBuilder(new StringReader(input))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(',')
                        .withQuoteChar('"')
                        .build())
                .build()) {

            String[] fields = reader.readNext();
            int id = Integer.parseInt(fields[0]);
            Product p = new Product(id,fields[1],fields[2],fields[3],fields[4],fields[5]);
            output = Blocker.blockByBrand(p);
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }

        assertEquals("sandisk", output );
    }


    @Test
    void testtypeblockingkeynormalinput(){

        //2. "Normaler" Input - Speichertyp wird richtig bestimmt. -> ..._*_...
        String input =  "86093,\"SANDISK Extreme UHS-I Micro-SDXC Speicherkarte, 64 GB, 100 MB/s, UHS Class 1\",39.99,,,";
        String output = "";
        try (CSVReader reader = new CSVReaderBuilder(new StringReader(input))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(',')
                        .withQuoteChar('"')
                        .build())
                .build()) {

            String[] fields = reader.readNext();
            int id = Integer.parseInt(fields[0]);
            Product p = new Product(id,fields[1],fields[2],fields[3],fields[4],fields[5]);
            output = Blocker.detectStorageType(p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        assertEquals("sdxc", output );
    }

    //3. "Normaler" Input - Speichergröße wird richtig bestimmt. -> ..._..._*
    @Test
    void testsizeblockingkeynormalinput(){

        String input =  "86093,\"SANDISK Extreme UHS-I Micro-SDXC Speicherkarte, 64 GB, 100 MB/s, UHS Class 1\",39.99,,,";
        String output = "";
        try (CSVReader reader = new CSVReaderBuilder(new StringReader(input))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(',')
                        .withQuoteChar('"')
                        .build())
                .build()) {

            String[] fields = reader.readNext();
            int id = Integer.parseInt(fields[0]);
            Product p = new Product(id,fields[1],fields[2],fields[3],fields[4],fields[5]);
            output = Blocker.detectMemorySizeBasedOnEnum(p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        assertEquals("64", output );
    }

    //4. "Abnormaler" Input - Marke kann nicht bestimmt werden. -> unknown_*_*
    @Test
    void testbrandblockingkeyabnormalinput(){

        String input =  "41813,USB TransMemory Ultra SDHC XQD 80mb,13.99,,,";
        String output = "";
        try (CSVReader reader = new CSVReaderBuilder(new StringReader(input))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(',')
                        .withQuoteChar('"')
                        .build())
                .build()) {

            String[] fields = reader.readNext();
            int id = Integer.parseInt(fields[0]);
            Product p = new Product(id,fields[1],fields[2],fields[3],fields[4],fields[5]);
            output = Blocker.blockByBrand(p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        assertEquals("unknown", output );
    }

    //5. "Abnormaler" Input - Speichertyp kann nicht bestimmt werden. -> *_unknown_*
    @Test
    void testtypeblockingkeyabnormalinput(){

        String input =  "41822,\"CARTE USM64SA3 DataTraveler 32 UHS-I EXTREME\\n Drive, MB/s, 80 150MB/Sek.FFP GB\",121.99,,,";
        String output = "";
        try (CSVReader reader = new CSVReaderBuilder(new StringReader(input))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(',')
                        .withQuoteChar('"')
                        .build())
                .build()) {

            String[] fields = reader.readNext();
            int id = Integer.parseInt(fields[0]);
            Product p = new Product(id,fields[1],fields[2],fields[3],fields[4],fields[5]);
            output = Blocker.detectStorageType(p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        assertEquals("unknown", output );
    }

    //6. "Abnormaler" Input - Speichergröße kann nicht bestimmt werden. -> *_*_unknown
    @Test
    void testsizeblockingkeyabnormalinput(){

        String input =  "41813,USB TransMemory Ultra SDHC XQD 80mb,13.99,,,";
        String output = "";
        try (CSVReader reader = new CSVReaderBuilder(new StringReader(input))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(',')
                        .withQuoteChar('"')
                        .build())
                .build()) {

            String[] fields = reader.readNext();
            int id = Integer.parseInt(fields[0]);
            Product p = new Product(id,fields[1],fields[2],fields[3],fields[4],fields[5]);
            output = Blocker.detectMemorySizeBasedOnEnum(p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        assertEquals("unknown", output );
    }

    //7.  Kombinationen aus vorangegangenen Fälle
    @Test
    void testcreateblockingkey(){

        Map<String, List<Integer>> blocks = new HashMap<>();
        Map<String, List<Product>> blocksTest = new HashMap<>();

        String input =  "86093,\"SANDISK Extreme UHS-I Micro-SDXC Speicherkarte, 64 GB, 100 MB/s, UHS Class 1\",39.99,,,";
        String output = "";

        try (CSVReader reader = new CSVReaderBuilder(new StringReader(input))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(',')
                        .withQuoteChar('"')
                        .build())
                .build()) {

            String[] fields = reader.readNext();
            int id = Integer.parseInt(fields[0]);
            Product p = new Product(id,fields[1],fields[2],fields[3],fields[4],fields[5]);
            List<Product> products = new ArrayList<>();
            products.add(p);

            for (Product p1 : products) {
                String brand = Blocker.blockByBrand(p1);
                String type = Blocker.detectStorageType(p1);
                String size = Blocker.detectMemorySizeBasedOnEnum(p1);
                output = String.join("_", brand, type, size);
                //blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(p1.id);
                //blocksTest.computeIfAbsent(key, k -> new ArrayList<>()).add(p1);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        assertEquals("sandisk_sdxc_64", output );
    }


}