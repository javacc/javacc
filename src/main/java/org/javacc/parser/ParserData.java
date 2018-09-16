package org.javacc.parser;

import java.util.List;
import java.util.Map;

public class ParserData {
  public enum LookaheadType {
    TOKEN,
    PRODUCTION,
    SEQUENCE,
    CHOICE,
    ZERORORMORE
  };

  // TODO(sreeni): for now, just use the existing code gen.
  public List<NormalProduction> bnfproductions;

  public String parserName;

  public int tokenCount;
  public Map<Integer, String> namesOfTokens;
  public Map<String, NormalProduction> productionTable;
  public String decls;
}
