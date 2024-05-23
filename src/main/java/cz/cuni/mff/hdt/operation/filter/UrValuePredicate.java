package cz.cuni.mff.hdt.operation.filter;

import org.json.JSONArray;

import cz.cuni.mff.hdt.ur.Ur;

public class UrValuePredicate implements UrPredicate {
    private ComparationSign sign;
    private String value;

    public UrValuePredicate(ComparationSign sign, String value) {
        this.sign = sign;
        this.value = value;
    }

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
