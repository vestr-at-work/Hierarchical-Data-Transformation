package cz.cuni.mff.hdt.source;

interface ArraySource {

  /**
   * Retun next referece or null when there are no other values.
   */
  Reference next(ArrayReference reference);

  /**
   * Return independet clone of a given array reference.
   */
  ArrayReference clone(ArrayReference reference);

}