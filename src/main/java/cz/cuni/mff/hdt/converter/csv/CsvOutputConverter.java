package cz.cuni.mff.hdt.converter.csv;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONObject;

import cz.cuni.mff.hdt.converter.UrOutputConverter;
import cz.cuni.mff.hdt.ur.Ur;

public class CsvOutputConverter extends UrOutputConverter {

    @Override
    public String convert(Ur data) throws IOException {
        var innerJson = data.getInnerRepresentation();        
        String[] headerNames = getHeaderNames(innerJson);

        StringWriter writer = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader(headerNames)
            .build();
        CSVPrinter printer = new CSVPrinter(writer, csvFormat);

        printRows(printer, innerJson, headerNames);

        return writer.toString().trim();
    }

    private void printRows(CSVPrinter printer, JSONObject innerJson, String[] headerNames) throws IOException {
        if (!innerJson.has(Ur.KEY_CSV_ROWS)) {
            throw new IOException("Invalid CSV Unified representation. Has no " + Ur.KEY_CSV_ROWS + " key");
        }

        var rowsUrArray = getProperty(innerJson, Ur.KEY_CSV_ROWS);
        if (!getTypeInnerValue(rowsUrArray).equals(Ur.VALUE_ARRAY)) {
            throw new IOException("Invalid CSV Unified representation. " + Ur.KEY_CSV_ROWS + " is not an array");
        } 

        var rowsLength = rowsUrArray.keySet().size() - 1;
        for (Integer i = 0; i < rowsLength; i++) {
            if (!rowsUrArray.has(i.toString())) {
                throw new IOException("Invalid CSV Unified representation. Wrong indicies in the " + Ur.KEY_CSV_ROWS + " array");
            }

            var rowObject = getProperty(rowsUrArray, i.toString());
            List<String> rowTokens = getRowTokens(rowObject, headerNames);
            printer.printRecord(rowTokens);
        }
    }

    private List<String> getRowTokens(JSONObject rowObject, String[] headerNames) throws IOException {
        if (!getTypeInnerValue(rowObject).equals(Ur.VALUE_OBJECT)) {
            throw new IOException("Invalid CSV Unified representation. Row in " + Ur.KEY_CSV_ROWS + " is not an array");
        } 

        var rowTokens = new ArrayList<String>();
        var rowLength = headerNames.length;
        for (Integer i = 0; i < rowLength; i++) {
            var columnName = headerNames[i];
            if (!rowObject.has(columnName)) {
                rowTokens.add(null);
                continue;
            }

            var rowItemObject = getProperty(rowObject, columnName);
            rowTokens.add(getValueInnerValue(rowItemObject));
        }
        
        return rowTokens;
    }

    private String[] getHeaderNames(JSONObject innerJson) throws IOException {
        if (!innerJson.has(Ur.KEY_CSV_HEADER)) {
            throw new IOException("Invalid CSV Unified representation. Has no " + Ur.KEY_CSV_HEADER + " key");
        }

        var headerUrArray = getProperty(innerJson, Ur.KEY_CSV_HEADER);
        if (!getTypeInnerValue(headerUrArray).equals(Ur.VALUE_ARRAY)) {
            throw new IOException("Invalid CSV Unified representation. " + Ur.KEY_CSV_HEADER + " is not an array");
        }

        var headerLength = headerUrArray.keySet().size() - 1;
        var names = new String[headerLength];
        for (Integer i = 0; i < headerLength; i++) {
            if (!headerUrArray.has(i.toString())) {
                throw new IOException("Invalid CSV Unified representation. Wrong indicies in the " + Ur.KEY_CSV_HEADER + " array");
            }
            var namePrimitive = getProperty(headerUrArray, i.toString());
            names[i] = getValueInnerValue(namePrimitive);
        }

        return names;
    }

}
