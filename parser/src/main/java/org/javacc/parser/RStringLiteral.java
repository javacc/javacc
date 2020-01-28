// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.javacc.parser;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Describes string literals.
 */

public class RStringLiteral extends RegularExpression {

  /**
   * The string image of the literal.
   */
  public String image;

  public RStringLiteral() {
  }

  RStringLiteral(Token token, String image) {
    this.image = image;
    this.setLine(token.beginLine);
    this.setColumn(token.beginColumn);
  }

  private static int maxStrKind = 0;
  private static int maxLen = 0;

  private static int[] maxLenForActive = new int[100]; // 6400 tokens
  private static int[][] intermediateKinds;
  private static int[][] intermediateMatchedPos;

  private static boolean subString[];
  private static boolean subStringAtPos[];
  private static Hashtable<String, long[]>[] statesForPos;

  static String[] allImages;

  /**
   * Initialize all the static variables, so that there is no interference
   * between the various states of the lexer.
   *
   * Need to call this method after generating code for each lexical state.
   */
  static void ReInit()
  {
    maxStrKind = 0;
    maxLen = 0;
    maxLenForActive = new int[100]; // 6400 tokens
    intermediateKinds = null;
    intermediateMatchedPos = null;
    subString = null;
    subStringAtPos = null;
    statesForPos = null;
  }

  /**
   * Used for top level string literals.
   */
  void GenerateDfa(int kind)
  {
     int len;

     if (maxStrKind <= ordinal)
        maxStrKind = ordinal + 1;

     if ((len = image.length()) > maxLen)
        maxLen = len;

     maxLenForActive[ordinal / 64] = Math.max(maxLenForActive[ordinal / 64], len -1);
     allImages[ordinal] = image;
  }

  @Override
  public Nfa GenerateNfa(boolean ignoreCase)
  {
     if (image.length() == 1)
     {
        RCharacterList temp = new RCharacterList(image.charAt(0));
        return temp.GenerateNfa(ignoreCase);
     }

     NfaState startState = new NfaState();
     NfaState theStartState = startState;
     NfaState finalState = null;

     if (image.length() == 0)
        return new Nfa(theStartState, theStartState);

     int i;

     for (i = 0; i < image.length(); i++)
     {
        finalState = new NfaState();
        startState.charMoves = new char[1];
        startState.AddChar(image.charAt(i));

        if (Options.getIgnoreCase() || ignoreCase)
        {
           startState.AddChar(Character.toLowerCase(image.charAt(i)));
           startState.AddChar(Character.toUpperCase(image.charAt(i)));
        }

        startState.next = finalState;
        startState = finalState;
     }

     return new Nfa(theStartState, finalState);
  }

  private static int GetStateSetForKind(int pos, int kind)
  {
     if (Main.lg.mixed[Main.lg.lexStateIndex] || NfaState.generatedStates == 0)
        return -1;

     Hashtable<String, long[]> allStateSets = statesForPos[pos];

     if (allStateSets == null)
        return -1;

     Enumeration<String> e = allStateSets.keys();

     while (e.hasMoreElements())
     {
        String s = e.nextElement();
        long[] actives = allStateSets.get(s);

        s = s.substring(s.indexOf(", ") + 2);
        s = s.substring(s.indexOf(", ") + 2);

        if (s.equals("null;"))
           continue;

        if (actives != null &&
            (actives[kind / 64] & (1L << (kind % 64))) != 0L)
        {
           return NfaState.AddStartStateSet(s);
        }
     }

     return -1;
  }

  /**
   * Returns true if s1 starts with s2 (ignoring case for each character).
   */
  static private boolean StartsWithIgnoreCase(String s1, String s2)
  {
     if (s1.length() < s2.length())
        return false;

     for (int i = 0; i < s2.length(); i++)
     {
        char c1 = s1.charAt(i), c2 = s2.charAt(i);

        if (c1 != c2 && Character.toLowerCase(c2) != c1 &&
            Character.toUpperCase(c2) != c1)
           return false;
     }

     return true;
  }

  static void FillSubString()
  {
     String image;
     subString = new boolean[maxStrKind + 1];
     subStringAtPos = new boolean[maxLen];

     for (int i = 0; i < maxStrKind; i++)
     {
        subString[i] = false;

        if ((image = allImages[i]) == null ||
            Main.lg.lexStates[i] != Main.lg.lexStateIndex)
           continue;

        if (Main.lg.mixed[Main.lg.lexStateIndex])
        {
           // We will not optimize for mixed case
           subString[i] = true;
           subStringAtPos[image.length() - 1] = true;
           continue;
        }

        for (int j = 0; j < maxStrKind; j++)
        {
           if (j != i && Main.lg.lexStates[j] == Main.lg.lexStateIndex &&
               (allImages[j]) != null)
           {
              if (allImages[j].indexOf(image) == 0)
              {
                 subString[i] = true;
                 subStringAtPos[image.length() - 1] = true;
                 break;
              }
              else if (Options.getIgnoreCase() &&
                       StartsWithIgnoreCase(allImages[j], image))
              {
                 subString[i] = true;
                 subStringAtPos[image.length() - 1] = true;
                 break;
              }
           }
        }
     }
  }

  private static final int GetStrKind(String str)
  {
     for (int i = 0; i < maxStrKind; i++)
     {
        if (Main.lg.lexStates[i] != Main.lg.lexStateIndex)
           continue;

        String image = allImages[i];
        if (image != null && image.equals(str))
           return i;
     }

     return Integer.MAX_VALUE;
  }

  static void GenerateNfaStartStates(NfaState initialState)
  {
     boolean[] seen = new boolean[NfaState.generatedStates];
     Hashtable<String, String> stateSets = new Hashtable<>();
     String stateSetString  = "";
     int i, j, kind, jjmatchedPos = 0;
     int maxKindsReqd = maxStrKind / 64 + 1;
     long[] actives;
     List<NfaState> newStates = new ArrayList<>();
     List<NfaState> oldStates = null, jjtmpStates;

     statesForPos = new Hashtable[maxLen];
     intermediateKinds = new int[maxStrKind + 1][];
     intermediateMatchedPos = new int[maxStrKind + 1][];

     for (i = 0; i < maxStrKind; i++)
     {
        if (Main.lg.lexStates[i] != Main.lg.lexStateIndex)
           continue;

        String image = allImages[i];

        if (image == null || image.length() < 1)
           continue;

        try
        {
           if ((oldStates = (List<NfaState>)initialState.epsilonMoves.clone()) == null ||
               oldStates.size() == 0)
           {
              return;
           }
        }
        catch(Exception e)
        {
           JavaCCErrors.semantic_error("Error cloning state vector");
        }

        intermediateKinds[i] = new int[image.length()];
        intermediateMatchedPos[i] = new int[image.length()];
        jjmatchedPos = 0;
        kind = Integer.MAX_VALUE;

        for (j = 0; j < image.length(); j++)
        {
           if (oldStates == null || oldStates.size() <= 0)
           {
              // Here, j > 0
              kind = intermediateKinds[i][j] = intermediateKinds[i][j - 1];
              jjmatchedPos = intermediateMatchedPos[i][j] = intermediateMatchedPos[i][j - 1];
           }
           else
           {
              kind = NfaState.MoveFromSet(image.charAt(j), oldStates, newStates);
              oldStates.clear();

              if (j == 0 && kind != Integer.MAX_VALUE &&
                  Main.lg.canMatchAnyChar[Main.lg.lexStateIndex] != -1 &&
                  kind > Main.lg.canMatchAnyChar[Main.lg.lexStateIndex])
                 kind = Main.lg.canMatchAnyChar[Main.lg.lexStateIndex];

              if (GetStrKind(image.substring(0, j + 1)) < kind)
              {
                 intermediateKinds[i][j] = kind = Integer.MAX_VALUE;
                 jjmatchedPos = 0;
              }
              else if (kind != Integer.MAX_VALUE)
              {
                 intermediateKinds[i][j] = kind;
                 jjmatchedPos = intermediateMatchedPos[i][j] = j;
              }
              else if (j == 0)
                 kind = intermediateKinds[i][j] = Integer.MAX_VALUE;
              else
              {
                 kind = intermediateKinds[i][j] = intermediateKinds[i][j - 1];
                 jjmatchedPos = intermediateMatchedPos[i][j] = intermediateMatchedPos[i][j - 1];
              }

              stateSetString = NfaState.GetStateSetString(newStates);
           }

           if (kind == Integer.MAX_VALUE &&
               (newStates == null || newStates.size() == 0))
              continue;

           int p;
           if (stateSets.get(stateSetString) == null)
           {
              stateSets.put(stateSetString, stateSetString);
              for (p = 0; p < newStates.size(); p++)
              {
                 if (seen[newStates.get(p).stateName])
                    newStates.get(p).inNextOf++;
                 else
                    seen[newStates.get(p).stateName] = true;
              }
           }
           else
           {
              for (p = 0; p < newStates.size(); p++)
                 seen[newStates.get(p).stateName] = true;
           }

           jjtmpStates = oldStates;
           oldStates = newStates;
           (newStates = jjtmpStates).clear();

           if (statesForPos[j] == null)
              statesForPos[j] = new Hashtable<>();

           if ((actives = (statesForPos[j].get(kind + ", " +
                                    jjmatchedPos + ", " + stateSetString))) == null)
           {
              actives = new long[maxKindsReqd];
              statesForPos[j].put(kind + ", " + jjmatchedPos + ", " +
                                                 stateSetString, actives);
           }

           actives[i / 64] |= 1L << (i % 64);
           //String name = NfaState.StoreStateSet(stateSetString);
        }
     }
  }

  /**
   * Return to original state.
   */
  static void reInit()
  {
    ReInit();

    allImages = null;
    literalsByLength.clear();
    literalKinds.clear();
    kindToLexicalState.clear();
    kindToIgnoreCase.clear();
    nfaStateMap.clear();
  }

  @Override
  public StringBuffer dump(int indent, Set<Expansion> alreadyDumped) {
    StringBuffer sb = super.dump(indent, alreadyDumped).append(' ').append(image);
    return sb;
  }

  @Override
  public String toString() {
    return super.toString() + " - " + image;
  }

  private static final Map<Integer, List<String>> literalsByLength =
      new HashMap<Integer, List<String>>();
  private static final Map<Integer, List<Integer>> literalKinds =
      new HashMap<Integer, List<Integer>>();
  private static final Map<Integer, Integer> kindToLexicalState =
      new HashMap<Integer, Integer>();
  private static final Set<Integer> kindToIgnoreCase =
      new HashSet<Integer>();
  private static final Map<Integer, NfaState> nfaStateMap =
      new HashMap<Integer, NfaState>();

  static void UpdateStringLiteralData(
      int generatedNfaStates, int lexStateIndex) {
    for (int kind = 0; kind < allImages.length; kind++) {
      if (allImages[kind] == null || allImages[kind].equals("") ||
          Main.lg.lexStates[kind] != lexStateIndex) {
        continue;
      }
      String s = allImages[kind];
      boolean ignoreCase = Main.lg.ignoreCase[kind];
      int actualKind;
      if (intermediateKinds != null &&
          intermediateKinds[kind][s.length() - 1] != Integer.MAX_VALUE &&
          intermediateKinds[kind][s.length() - 1] < kind) {
        JavaCCErrors.warning("Token: " + s + " will not be matched as " +
                             "specified. It will be matched as token " +
                             "of kind: " +
                             intermediateKinds[kind][s.length() - 1] +
                             " instead.");
        actualKind = intermediateKinds[kind][s.length() - 1];
      } else {
        actualKind = kind;
      }
      kindToLexicalState.put(actualKind, lexStateIndex);
      if (Options.getIgnoreCase() || ignoreCase) {
        s = s.toLowerCase();
      }
      char c = s.charAt(0);
      int key = Main.lg.lexStateIndex << 16 | c;
      UpdateStringLiteralDataForKey(key, actualKind, s);

      if(ignoreCase) {
        kindToIgnoreCase.add(kind);
        c = s.toUpperCase().charAt(0);
        key = Main.lg.lexStateIndex << 16 | c;
        UpdateStringLiteralDataForKey(key, actualKind, s);
      }

      int stateIndex = GetStateSetForKind(s.length() - 1, kind);
      if (stateIndex != -1) {
        nfaStateMap.put(actualKind, NfaState.getNfaState(stateIndex));
      } else {
        nfaStateMap.put(actualKind, null);
      }
    }
  }
  
  private static void UpdateStringLiteralDataForKey(int key, int actualKind, String s) {
    List<String> l = literalsByLength.get(key);
    List<Integer> kinds = literalKinds.get(key);
    int j = 0;
    if (l == null) {
      literalsByLength.put(key, l = new ArrayList<String>());
      assert(kinds == null);
      kinds = new ArrayList<Integer>();
      literalKinds.put(key, kinds = new ArrayList<Integer>());
    }
    while (j < l.size() && l.get(j).length() > s.length()) j++;
    l.add(j, s);
    kinds.add(j, actualKind);
  }

  static void BuildTokenizerData(TokenizerData tokenizerData) {
    Map<Integer, Integer> nfaStateIndices = new HashMap<Integer, Integer>();
    for (int kind : nfaStateMap.keySet()) {
      if (nfaStateMap.get(kind) != null) {
        if (nfaStateIndices.put(kind, nfaStateMap.get(kind).stateName) != null) {
          System.err.println("ERROR: Multiple start states for kind: " + kind);
        }
      } else {
        nfaStateIndices.put(kind, -1);
      }
    }
    tokenizerData.setLiteralSequence(literalsByLength);
    tokenizerData.setLiteralKinds(literalKinds);
    tokenizerData.setIgnoreCaserKinds(kindToIgnoreCase);
    tokenizerData.setKindToNfaStartState(nfaStateIndices);
  }
}
