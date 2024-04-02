package cz.cuni.mff.hdt.source;

public interface EntitySource {

  /**
   * Return reference to values under given key or null it the key does not exists.
   */
  public ArrayReference property(EntityReference reference, String property);

  /**
   * Return reference to all items, keys and values, in given entity.
   */
  public ArrayReference items(EntityReference reference);

  /**
   * Get ArraySource from ArrayReference.
   * 
   * @param referece ArrayReference from which we want to make the source.
   * @return ArraySource from the given ArrayReference.
   */
  public ArraySource source(ArrayReference referece);

}