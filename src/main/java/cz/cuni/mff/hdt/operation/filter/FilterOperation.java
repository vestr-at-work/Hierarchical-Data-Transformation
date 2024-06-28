package cz.cuni.mff.hdt.operation.filter;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.path.UrPath;

/**
 * Class implementing the filter operation of the transformation language.
 * This operation filters elements from the input based on specified predicates.
 */
public class FilterOperation implements Operation {
    public static final String KEY_PATH = "path";
    public static final String KEY_PREDICATE = "predicate";

    private ArrayList<Pair<UrPath, UrPredicate>> filters;

    /**
     * Constructs a FilterOperation with the given operation specifications.
     *
     * @param operationSpecs the JSONArray containing operation specifications
     * @throws IOException if there is an error parsing the specifications
     */
    public FilterOperation(JSONArray operationSpecs) throws IOException {
        filters = new ArrayList<>();
        for (var spec : operationSpecs) {
            filters.add(parseSpec(spec));
        }
    }

    /**
     * Executes the filter operation on the given input {@code Ur}.
     *
     * @param inputUr the input {@code Ur} to filter
     * @return the filtered {@code Ur}
     * @throws OperationFailedException if the operation fails
     */
    @Override
    public Ur execute(Ur inputUr) throws OperationFailedException {
        var outputUr = new Ur(new JSONObject(inputUr.getInnerRepresentation().toMap()));
        for (var pair : filters) {
            var path = pair.getLeft();
            var predicate = pair.getRight();
            try {
                var value = outputUr.get(path);
                if (!predicate.evaluate(value)) {
                    outputUr.delete(path);
                }
            }
            catch (IOException e) {
                throw new OperationFailedException(e.getMessage());
            }
        }

        return outputUr;
    }

    private Pair<UrPath, UrPredicate> parseSpec(Object spec) throws IOException {
        if (!(spec instanceof JSONObject)) {
            throw new IOException("Incorrect type of an item in specs");
        }
        var specObject = (JSONObject)spec;
        if (!specObject.has(KEY_PATH) || !specObject.has(KEY_PREDICATE)) {
            throw new IOException("Mandatory keys are missing from item in specs");
        }
        var pathUnknown = specObject.get(KEY_PATH);
        var predicateUnknown = specObject.get(KEY_PREDICATE);
        if (!(pathUnknown instanceof String)) {
            throw new IOException("Incorrect type of key '" + KEY_PATH + "' in spec item");
        }
        if (!(predicateUnknown instanceof String)) {
            throw new IOException("Incorrect type of key '" + KEY_PREDICATE + "' in spec item");
        }
        return Pair.of(new UrPath((String)pathUnknown), UrPredicateFactory.create((String)predicateUnknown));
    }
}
