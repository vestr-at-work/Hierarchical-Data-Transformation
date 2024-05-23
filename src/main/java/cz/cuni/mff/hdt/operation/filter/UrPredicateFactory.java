package cz.cuni.mff.hdt.operation.filter;

import java.io.IOException;

import cz.cuni.mff.hdt.operation.filter.UrPredicate.ComparationSign;
import cz.cuni.mff.hdt.ur.Ur;

public class UrPredicateFactory {
    public static UrPredicate create(String value) throws IOException {
        String[] tokens = value.split(" ");
        if (tokens.length != 3) {
            throw new IOException("Invalid predicate: '" + value + "'");
        }
        var type = tokens[0];
        var comparationSign = tokens[1];
        var comparationValue = tokens[2];

        if (!type.equals(Ur.KEY_TYPE) && !type.equals(Ur.KEY_VALUE)) {
            throw new IOException("Invalid predicate: '" + value + "'");
        }

        if (type.equals(Ur.KEY_TYPE)) {
            if (comparationSign.equals(UrPredicate.SIGN_EQUAL)) {
                return new UrTypePredicate(ComparationSign.Equal, comparationValue);
            }
            else if (comparationSign.equals(UrPredicate.SING_NOT_EQUAL)) {
                return new UrTypePredicate(ComparationSign.NotEqual, comparationValue);
            }
            else {
                throw new IOException("Invalid predicate: '" + value + "'");
            }
        }
        else { // type.equals(Ur.KEY_VALUE)
            // TODO add support for int comparation and more
            if (comparationSign.equals(UrPredicate.SIGN_EQUAL)) {
                return new UrValuePredicate(ComparationSign.Equal, comparationValue);
            }
            else if (comparationSign.equals(UrPredicate.SING_NOT_EQUAL)) {
                return new UrValuePredicate(ComparationSign.NotEqual, comparationValue);
            }
            else {
                throw new IOException("Invalid predicate: '" + value + "'");
            }
        }
    }
}
