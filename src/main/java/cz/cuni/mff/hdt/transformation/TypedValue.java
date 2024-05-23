package cz.cuni.mff.hdt.transformation;

/**
 * Represents a typed value with a type and a value.
 *
 * @param type the type of the value
 * @param value the value itself
 */
public record TypedValue(
    String type,
    String value
) {}
