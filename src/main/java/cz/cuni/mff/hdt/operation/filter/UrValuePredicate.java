package cz.cuni.mff.hdt.operation.filter;

import org.json.JSONArray;

import cz.cuni.mff.hdt.ur.Ur;

/**
 * Predicate that compares the value of a {@code Ur} object to a specified value.
 */
public class UrValuePredicate implements UrPredicate {
    private ComparationSign sign;
    private String value;

    /**
     * Constructs a {@code UrValuePredicate} with the specified comparison sign and value.
     *
     * @param sign the comparison sign
     * @param value the value to compare against
     */
    public UrValuePredicate(ComparationSign sign, String value) {
        this.sign = sign;
        this.value = value;
    }

    /**
     * Evaluates the predicate on a given {@code Ur}.
     *
     * @param value the {@code Ur} object to evaluate
     * @return {@code true} if the predicate holds, {@code false} otherwise
     */
    @Override
    public boolean evaluate(Ur value) {
        var inner = value.getInnerRepresentation();
        if (!inner.has(Ur.KEY_VALUE)) {
            // TODO maybe should throw exception
            return false;
        }
        var valueUnknown = inner.get(Ur.KEY_VALUE);
        if (!(valueUnknown instanceof JSONArray)) {
            return false;
        }
        var valueArray = (JSONArray)valueUnknown;
        if (valueArray.length() != 1) {
            return false;
        }
        var urPrimitiveValue = valueArray.get(0);
        if (sign == ComparationSign.Equal) {
            return this.value.equals(urPrimitiveValue);
        }
        // sign is not equal
        return !this.value.equals(urPrimitiveValue);
    }
    
}
