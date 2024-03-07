package cz.cuni.mff.hdt.sink;

interface Sink {

  void openObject();

  void closeObject();

  void openArray();

  void closeArray();

  void setNextKey(String key);

  void writeValue(String value);

}