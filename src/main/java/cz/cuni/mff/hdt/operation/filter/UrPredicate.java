package cz.cuni.mff.hdt.operation.filter;

import cz.cuni.mff.hdt.ur.Ur;


/**
 * Predicate that can be evaluated as true or false on a given {@code Ur}.
 * Interface used for predicates in the filter operation.
 */
public interface UrPredicate {
    public static final String SIGN_EQUAL = "==";
    public static final String SING_NOT_EQUAL = "!=";
    public static final String SIGN_LESS_THAN = "<";
    public static final String SIGN_LESS_OR_EQUAL_THAN = "<=";
    public static final String SIGN_GREATER_THAN = ">";
    public static final String SIGN_GREATER_OR_EQUAL_THAN = ">=";

    /**
     * Enumeration for comparison signs.
     */
    public static enum ComparationSign { 
        Equal, 
        NotEqual, 
        LessThan, 
        LessOrEqualThan, 
        GreaterThan, 
        GreaterOrEqualThan 
    };

    /**
     * Evaluates the predicate on a given {@code Ur} value.
     *
     * @param value the {@code Ur} value to evaluate
     * @return true if the predicate evaluates to true, false otherwise
     */
    public boolean evaluate(Ur value);
}
