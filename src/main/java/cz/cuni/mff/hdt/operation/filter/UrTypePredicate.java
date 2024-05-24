package cz.cuni.mff.hdt.operation.filter;

import org.json.JSONArray;

import cz.cuni.mff.hdt.ur.Ur;

/**
 * Predicate that compares the type of a {@code Ur} object to a specified value.
 */
public class UrTypePredicate implements UrPredicate {
    private ComparationSign sign;
    private String value;

    /**
     * Constructs a {@code UrTypePredicate} with the specified comparison sign and value.
     *
     * @param sign the comparison sign
     * @param value the value to compare against
     */
    public UrTypePredicate(ComparationSign sign, String value) {
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
        if (!inner.has(Ur.KEY_TYPE)) {
            // TODO maybe should throw exception
            return false;
        }
        var typeUnknown = inner.get(Ur.KEY_TYPE);
        if (!(typeUnknown instanceof JSONArray)) {
            return false;
        }
        var typeArray = (JSONArray)typeUnknown;
        if (typeArray.length() != 1) {
            return false;
        }
        var type = typeArray.get(0);
        if (sign == ComparationSign.Equal) {
            return this.value.equals(type);
        }
        // sign is not equal
        return !this.value.equals(type);
    } 
}
