package cz.cuni.mff.hdt.source;

import cz.cuni.mff.hdt.reference.ArrayReference;
import cz.cuni.mff.hdt.reference.EntityReference;

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
  public ArraySource getSourceFromReference(ArrayReference referece);

  /**
   * Get EntitySource from EntityReference.
   * 
   * @param entityReferece EntityReference from which we want to make the source.
   * @return EntitySource from the given EntityReference.
   */
  public EntitySource getSourceFromReference(EntityReference entityReferece);

}