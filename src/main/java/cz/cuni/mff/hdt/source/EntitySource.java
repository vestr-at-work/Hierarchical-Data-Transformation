package cz.cuni.mff.hdt.source;

public interface EntitySource {

  /**
   * Return reference to values under given key or null it the key does not exists.
   */
  public ArrayReference property(EntityReference reference, String property);

  /**
   * Return reference to all parents who have given entity stored under given key.
   */
  public ArrayReference reverseProperty(Reference reference, String property);

  /**
   * Return reference to all items, keys and values, in given entity.
   */
  public ArrayReference items(EntityReference reference);

}