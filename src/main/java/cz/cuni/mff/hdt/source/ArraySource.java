package cz.cuni.mff.hdt.source;

import cz.cuni.mff.hdt.reference.ArrayReference;
import cz.cuni.mff.hdt.reference.PropertyReference;
import cz.cuni.mff.hdt.reference.Reference;
import cz.cuni.mff.hdt.reference.ValueReference;

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

  /**
   * Get PropertySource from PropertyReference.
   * 
   * @param referece PropertyReference from which we want to make the source.
   * @return PropertySource from the given PropertyReference.
   */
  public PropertySource getSourceFromReference(PropertyReference referece);

}