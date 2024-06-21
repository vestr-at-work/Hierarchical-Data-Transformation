package cz.cuni.mff.hdt.converter.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.converter.InputConverter;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.ur.Ur.Type;
import cz.cuni.mff.hdt.utils.PrimitiveParser;

public class CsvInputConverter implements InputConverter {

    @Override
    public Ur convert(InputStream input) throws IOException {
        InputStreamReader reader = new InputStreamReader(input);
        CSVFormat csvFormat = CSVFormat.Builder.create()
                    .setHeader()
                    .setSkipHeaderRecord(false)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build();

        
        var parser = csvFormat.parse(reader);
        List<String> headerNames = parser.getHeaderNames();
        List<CSVRecord> rows = parser.getRecords();

        var innerJson = new JSONObject();
        innerJson.put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_OBJECT));

        var headerUrArray = getHeaderArray(headerNames); 
        innerJson.put(Ur.KEY_CSV_HEADER, new JSONArray().put(headerUrArray));

        var rowsUrArray = getRowsArray(headerNames, rows);
        innerJson.put(Ur.KEY_CSV_ROWS, new JSONArray().put(rowsUrArray));


        return new Ur(innerJson);
    }

    private static JSONObject getHeaderArray(List<String> headerNames) {
        var headerObject = new JSONObject();
        headerObject.put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_ARRAY));
        Integer index = 0;
        for (var name : headerNames) {
            var namePrimitive = new JSONObject();
            namePrimitive.put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_STRING));
            namePrimitive.put(Ur.KEY_VALUE, new JSONArray().put(name));
            headerObject.put(index.toString(), new JSONArray().put(namePrimitive));
            index++;
        }
        return headerObject;
    }

    private static JSONObject getRowsArray(List<String> headerNames, List<CSVRecord> rows) {
        var rowsObject = new JSONObject();
        rowsObject.put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_ARRAY));

        Integer index = 0;
        for (var row : rows) {
            var rowObject = new JSONObject();
            rowObject.put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_OBJECT)); 
            for (var name : headerNames) {
                if (!row.isMapped(name)) {
                    continue;
                }
                var value = row.get(name);
                Type type = PrimitiveParser.getPrimitiveType(value);
                JSONObject primitiveObject = getPrimitiveObject(type, value);
                rowObject.put(name, new JSONArray().put(primitiveObject));
            }
            rowsObject.put(index.toString(), new JSONArray().put(rowObject));
            index++;
        }

        return rowsObject;
    }

    private static JSONObject getPrimitiveObject(Type type, String value) {
        var primitiveObject = new JSONObject();
        primitiveObject.put(Ur.KEY_VALUE, new JSONArray().put(value));
        String typeString = Ur.VALUE_STRING;
        if (type.equals(Type.Boolean)) {
            typeString = Ur.VALUE_BOOLEAN;
        }
        else if (type.equals(Type.Number)) {
            typeString = Ur.VALUE_NUMBER;
        }
        primitiveObject.put(Ur.KEY_TYPE, new JSONArray().put(typeString));
        return primitiveObject;
    }
}
