package org.javacc.parser;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ParserData {
  public enum LookaheadType {
    TOKEN,
    PRODUCTION,
    SEQUENCE,
    CHOICE,
    ZERORORMORE
  };

  public static class LookaheadInfo {
    public LookaheadType lokaheadType;
    public List<Integer> data;
  };

  Map<Integer, List<LookaheadInfo>> lookaheads;
}
