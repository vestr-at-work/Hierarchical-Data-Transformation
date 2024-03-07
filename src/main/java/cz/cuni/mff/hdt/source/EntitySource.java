package cz.cuni.mff.hdt.source;

interface EntitySource {

  /**
   * Return reference to values under given key or null it the key does not exists.
   */
  ArrayReference property(EntityReference reference, String property);

  /**
   * Return reference to all parents who have given entity stored under given key.
   */
  ArrayReference reverseProperty(Reference reference, String property);

  /**
   * Return reference to all items, keys and values, in given entity.
   */
  ArrayReference items(EntityReference reference);

}