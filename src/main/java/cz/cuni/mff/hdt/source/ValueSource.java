package cz.cuni.mff.hdt.source;

public interface ValueSource {

  /**
   * Return string value of a referecne.
   */
  public String value(ValueReference reference);

}