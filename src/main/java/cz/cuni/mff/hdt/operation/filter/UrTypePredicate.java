package cz.cuni.mff.hdt.operation.filter;

import org.json.JSONArray;

import cz.cuni.mff.hdt.ur.Ur;

public class UrTypePredicate implements UrPredicate {
    private ComparationSign sign;
    private String value;

    public UrTypePredicate(ComparationSign sign, String value) {
        this.sign = sign;
        this.value = value;
    }

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
