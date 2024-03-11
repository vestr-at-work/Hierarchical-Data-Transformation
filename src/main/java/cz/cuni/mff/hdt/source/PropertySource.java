package cz.cuni.mff.hdt.source;

public interface PropertySource {

  /**
   * Return string value of a referecne.
   */
  public String value(ValueReference reference);

}