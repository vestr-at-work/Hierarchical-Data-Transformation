package cz.cuni.mff.hdt.source;

import cz.cuni.mff.hdt.reference.ValueReference;

public interface ValueSource {

  /**
   * Return string value of a referecne.
   */
  public String value(ValueReference reference);

}