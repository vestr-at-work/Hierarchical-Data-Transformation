package cz.cuni.mff.hdt.transformation;

import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.hdt.transformation.operations.Operation;

/**
 * Container of transformation operations in order
 */
public class TransformationDefinition {
    /*
     * Operations to be executed in a transformations in order
     */
    public List<Operation> operations = new ArrayList<>();
}
