package org.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CsvConverter {
    public static InputStream convertCsvToDoubleTabStream(String inputPath) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (
                Reader in = new FileReader(inputPath, StandardCharsets.UTF_8);
                StringWriter stringWriter = new StringWriter();
                CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withFirstRecordAsHeader());
                CSVPrinter printer = new CSVPrinter(stringWriter, CSVFormat.DEFAULT
                        .withDelimiter('\t')
                        .withHeader(parser.getHeaderMap().keySet().toArray(new String[0])))
        ) {
            for (CSVRecord record : parser) {
                printer.printRecord(record);
            }

            // Replace single tabs with double tabs as delimiter
            String result = stringWriter.toString().replace("\t", "\t\t");
            outputStream.write(result.getBytes(StandardCharsets.UTF_8));
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
