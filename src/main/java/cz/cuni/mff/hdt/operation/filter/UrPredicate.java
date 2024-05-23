package cz.cuni.mff.hdt.operation.filter;

import cz.cuni.mff.hdt.ur.Ur;

/*
 * Predicate that can be evaluated true or false on a given Ur. 
 * Interface used for predicates in the filter operation.
 */
public interface UrPredicate {
    public static final String SIGN_EQUAL = "==";
    public static final String SING_NOT_EQUAL = "!=";
    public static final String SIGN_LESS_THAN = "<";
    public static final String SIGN_LESS_OR_EQUAL_THAN = "<=";
    public static final String SIGN_GREATER_THAN = ">";
    public static final String SIGN_GREATER_OR_EQUAL_THAN = ">=";

    public static enum ComparationSign { 
        Equal, 
        NotEqual, 
        LessThan, 
        LessOrEqualThan, 
        GreaterThan, 
        GreaterOrEqualThan 
    };

    /*
     * Evaluate the predicate on a given Ur.
     */
    public boolean evaluate(Ur value);
}
