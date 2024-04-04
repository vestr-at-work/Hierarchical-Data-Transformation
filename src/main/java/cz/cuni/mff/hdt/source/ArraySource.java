package cz.cuni.mff.hdt.source;

public interface ArraySource {

  /**
   * Retun next referece or null when there are no other values.
   */
  public Reference next(ArrayReference reference);

  /**
   * Return independet clone of a given array reference.
   */
  public ArrayReference clone(ArrayReference reference);

  /**
   * Get ValueSource from ValueReference.
   * 
   * @param referece ValueReference from which we want to make the source.
   * @return ValueSource from the given ValueReference.
   */
  public ValueSource getSourceFromReference(ValueReference referece);

}