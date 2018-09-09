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

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.javacc.parser.JavaCCGlobals.*;

import org.javacc.utils.OutputFileGenerator;

/**
 * Generate lexer.
 */
public class LexGen extends CodeGenHelper implements JavaCCParserConstants
{
  
  public static String staticString;
  public static String tokMgrClassName;

  // Hashtable of vectors
  public static Hashtable allTpsForState = new Hashtable();
  public static int lexStateIndex = 0;
  public static int[] kinds;
  public static int maxOrdinal = 1;
  public static String lexStateSuffix;
  public static String[] newLexState;
  public static int[] lexStates;
  public static boolean[] ignoreCase;
  public static Action[] actions;
  public static Hashtable initStates = new Hashtable();
  public static int stateSetSize;
  public static int totalNumStates;
  public static int maxLexStates;
  public static String[] lexStateName;
  public static NfaState[] singlesToSkip;
  public static long[] toSkip;
  public static long[] toSpecial;
  public static long[] toMore;
  public static long[] toToken;
  public static int defaultLexState;
  public static RegularExpression[] rexprs;
  public static int[] maxLongsReqd;
  public static int[] initMatch;
  public static int[] canMatchAnyChar;
  public static boolean hasEmptyMatch;
  public static boolean[] canLoop;
  public static boolean[] stateHasActions;
  public static boolean hasLoop = false;
  public static boolean[] canReachOnMore;
  public static boolean[] hasNfa;
  public static boolean[] mixed;
  public static NfaState initialState;
  public static int curKind;
  public static boolean hasSkipActions = false;
  public static boolean hasMoreActions = false;
  public static boolean hasTokenActions = false;
  public static boolean hasSpecial = false;
  public static boolean hasSkip = false;
  public static boolean hasMore = false;
  public static RegularExpression curRE;
  public static boolean keepLineCol;
  public static String errorHandlingClass;
  public static TokenizerData tokenizerData;
  public static boolean generateDataOnly;

  @SuppressWarnings("unchecked")
  protected void writeTemplate(String name, Object... additionalOptions) throws IOException
  {
    Map<String, Object> options = new HashMap<>(Options.getOptions());

    options.put("maxOrdinal", Integer.valueOf(maxOrdinal));
    options.put("maxLexStates", Integer.valueOf(maxLexStates));
    options.put("hasEmptyMatch", Boolean.valueOf(hasEmptyMatch));
    options.put("hasSkip", Boolean.valueOf(hasSkip));
    options.put("hasMore", Boolean.valueOf(hasMore));
    options.put("hasSpecial", Boolean.valueOf(hasSpecial));
    options.put("hasMoreActions", Boolean.valueOf(hasMoreActions));
    options.put("hasSkipActions", Boolean.valueOf(hasSkipActions));
    options.put("hasTokenActions", Boolean.valueOf(hasTokenActions));
    options.put("stateSetSize", stateSetSize);
    options.put("hasActions", hasMoreActions || hasSkipActions || hasTokenActions);
    options.put("tokMgrClassName", tokMgrClassName);
    int x = 0;
    for (int l : maxLongsReqd) x = Math.max(x, l);
    options.put("maxLongs", x);
    options.put("cu_name", cu_name);

    // options.put("", .valueOf(maxOrdinal));
    
    
    for (int i = 0; i < additionalOptions.length; i++)
    {
      Object o = additionalOptions[i];
    
      if (o instanceof Map<?,?>)
      {
        options.putAll((Map<String,Object>) o);
      }
      else
      {
        if (i == additionalOptions.length - 1)
          throw new IllegalArgumentException("Must supply pairs of [name value] args");
        
        options.put((String) o, additionalOptions[i+1]);
        i++;
      }
    }
    
    OutputFileGenerator gen = new OutputFileGenerator(name, options);
    StringWriter sw = new StringWriter();
    gen.generate(new PrintWriter(sw));
    sw.close();
    genCode(sw.toString());
  }

  static void BuildLexStatesTable()
  {
    Iterator<TokenProduction> it = rexprlist.iterator();
    TokenProduction tp;
    int i;

    String[] tmpLexStateName = new String[lexstate_I2S.size()];
    while (it.hasNext())
    {
      tp = it.next();
      List<RegExprSpec> respecs = tp.respecs;
      List<TokenProduction> tps;

      for (i = 0; i < tp.lexStates.length; i++)
      {
        if ((tps = (List)allTpsForState.get(tp.lexStates[i])) == null)
        {
          tmpLexStateName[maxLexStates++] = tp.lexStates[i];
          allTpsForState.put(tp.lexStates[i], tps = new ArrayList());
        }

        tps.add(tp);
      }

      if (respecs == null || respecs.size() == 0)
        continue;

      RegularExpression re;
      for (i = 0; i < respecs.size(); i++)
        if (maxOrdinal <= (re = respecs.get(i).rexp).ordinal)
          maxOrdinal = re.ordinal + 1;
    }

    kinds = new int[maxOrdinal];
    toSkip = new long[maxOrdinal / 64 + 1];
    toSpecial = new long[maxOrdinal / 64 + 1];
    toMore = new long[maxOrdinal / 64 + 1];
    toToken = new long[maxOrdinal / 64 + 1];
    toToken[0] = 1L;
    actions = new Action[maxOrdinal];
    actions[0] = actForEof;
    hasTokenActions = actForEof != null;
    initStates = new Hashtable();
    canMatchAnyChar = new int[maxLexStates];
    canLoop = new boolean[maxLexStates];
    stateHasActions = new boolean[maxLexStates];
    lexStateName = new String[maxLexStates];
    singlesToSkip = new NfaState[maxLexStates];
    //System.arraycopy(tmpLexStateName, 0, lexStateName, 0, maxLexStates);

    for (int l: lexstate_I2S.keySet()) {
      lexStateName[l] = lexstate_I2S.get(l);
    }

    for (i = 0; i < maxLexStates; i++)
      canMatchAnyChar[i] = -1;

    hasNfa = new boolean[maxLexStates];
    mixed = new boolean[maxLexStates];
    maxLongsReqd = new int[maxLexStates];
    initMatch = new int[maxLexStates];
    newLexState = new String[maxOrdinal];
    newLexState[0] = nextStateForEof;
    hasEmptyMatch = false;
    lexStates = new int[maxOrdinal];
    ignoreCase = new boolean[maxOrdinal];
    rexprs = new RegularExpression[maxOrdinal];
    RStringLiteral.allImages = new String[maxOrdinal];
    canReachOnMore = new boolean[maxLexStates];
  }

  static int GetIndex(String name)
  {
    for (int i = 0; i < lexStateName.length; i++)
      if (lexStateName[i] != null && lexStateName[i].equals(name))
        return i;

    throw new Error(); // Should never come here
  }

  public static void AddCharToSkip(char c, int kind)
  {
    singlesToSkip[lexStateIndex].AddChar(c);
    singlesToSkip[lexStateIndex].kind = kind;
  }

  public void start() throws IOException
  {
    if (!Options.getBuildTokenManager() ||
        Options.getUserTokenManager() ||
        JavaCCErrors.get_error_count() > 0)
      return;

    keepLineCol = Options.getKeepLineColumn();
    errorHandlingClass = Options.getTokenMgrErrorClass();
    List choices = new ArrayList();
    Enumeration e;
    TokenProduction tp;
    int i, j;

    staticString = (Options.getStatic() ? "static " : "");
    tokMgrClassName = cu_name + "TokenManager";

    BuildLexStatesTable();

    e = allTpsForState.keys();

    boolean ignoring = false;

    //while (e.hasMoreElements())
    for (int k = 0; k < lexStateName.length; k++)
    {
      int startState = -1;
      NfaState.ReInit();
      RStringLiteral.ReInit();

      //String key = (String)e.nextElement();
      String key = lexStateName[k];

      lexStateIndex = GetIndex(key);
      lexStateSuffix = "_" + lexStateIndex;
      List<TokenProduction> allTps = (List<TokenProduction>)allTpsForState.get(key);
      initStates.put(key, initialState = new NfaState());
      ignoring = false;

      singlesToSkip[lexStateIndex] = new NfaState();
      singlesToSkip[lexStateIndex].dummy = true;

      if (key.equals("DEFAULT"))
        defaultLexState = lexStateIndex;

      for (i = 0; i < allTps.size(); i++)
      {
        tp = allTps.get(i);
        int kind = tp.kind;
        boolean ignore = tp.ignoreCase;
        List<RegExprSpec> rexps = tp.respecs;

        if (i == 0)
          ignoring = ignore;

        for (j = 0; j < rexps.size(); j++)
        {
          RegExprSpec respec = rexps.get(j);
          curRE = respec.rexp;

          rexprs[curKind = curRE.ordinal] = curRE;
          lexStates[curRE.ordinal] = lexStateIndex;
          ignoreCase[curRE.ordinal] = ignore;

          if (curRE.private_rexp)
          {
            kinds[curRE.ordinal] = -1;
            continue;
          }

          if (!Options.getNoDfa() && curRE instanceof RStringLiteral &&
              !((RStringLiteral)curRE).image.equals(""))
          {
            ((RStringLiteral)curRE).GenerateDfa(this, curRE.ordinal);
            if (i != 0 && !mixed[lexStateIndex] && ignoring != ignore) {
              mixed[lexStateIndex] = true;
            }
          }
          else if (curRE.CanMatchAnyChar())
          {
            if (canMatchAnyChar[lexStateIndex] == -1 ||
                canMatchAnyChar[lexStateIndex] > curRE.ordinal)
              canMatchAnyChar[lexStateIndex] = curRE.ordinal;
          }
          else
          {
            Nfa temp;

            if (curRE instanceof RChoice)
              choices.add(curRE);

            temp = curRE.GenerateNfa(ignore);
            temp.end.isFinal = true;
            temp.end.kind = curRE.ordinal;
            initialState.AddMove(temp.start);
          }

          if (kinds.length < curRE.ordinal)
          {
            int[] tmp = new int[curRE.ordinal + 1];

            System.arraycopy(kinds, 0, tmp, 0, kinds.length);
            kinds = tmp;
          }
          //System.out.println("   ordina : " + curRE.ordinal);

          kinds[curRE.ordinal] = kind;

          if (respec.nextState != null &&
              !respec.nextState.equals(lexStateName[lexStateIndex]))
            newLexState[curRE.ordinal] = respec.nextState;

          if (respec.act != null && respec.act.getActionTokens() != null &&
              respec.act.getActionTokens().size() > 0)
            actions[curRE.ordinal] = respec.act;

          switch(kind)
          {
          case TokenProduction.SPECIAL :
            hasSkipActions |= (actions[curRE.ordinal] != null) ||
            (newLexState[curRE.ordinal] != null);
            hasSpecial = true;
            toSpecial[curRE.ordinal / 64] |= 1L << (curRE.ordinal % 64);
            toSkip[curRE.ordinal / 64] |= 1L << (curRE.ordinal % 64);
            break;
          case TokenProduction.SKIP :
            hasSkipActions |= (actions[curRE.ordinal] != null);
            hasSkip = true;
            toSkip[curRE.ordinal / 64] |= 1L << (curRE.ordinal % 64);
            break;
          case TokenProduction.MORE :
            hasMoreActions |= (actions[curRE.ordinal] != null);
            hasMore = true;
            toMore[curRE.ordinal / 64] |= 1L << (curRE.ordinal % 64);

            if (newLexState[curRE.ordinal] != null)
              canReachOnMore[GetIndex(newLexState[curRE.ordinal])] = true;
            else
              canReachOnMore[lexStateIndex] = true;

            break;
          case TokenProduction.TOKEN :
            hasTokenActions |= (actions[curRE.ordinal] != null);
            toToken[curRE.ordinal / 64] |= 1L << (curRE.ordinal % 64);
            break;
          }
        }
      }

      // Generate a static block for initializing the nfa transitions
      NfaState.ComputeClosures();

      for (i = 0; i < initialState.epsilonMoves.size(); i++)
        initialState.epsilonMoves.elementAt(i).GenerateCode();

      if (hasNfa[lexStateIndex] = (NfaState.generatedStates != 0))
      {
        initialState.GenerateCode();
        startState = initialState.GenerateInitMoves(this);
      }

      if (initialState.kind != Integer.MAX_VALUE && initialState.kind != 0)
      {
        if ((toSkip[initialState.kind / 64] & (1L << initialState.kind)) != 0L ||
            (toSpecial[initialState.kind / 64] & (1L << initialState.kind)) != 0L)
          hasSkipActions = true;
        else if ((toMore[initialState.kind / 64] & (1L << initialState.kind)) != 0L)
          hasMoreActions = true;
        else
          hasTokenActions = true;

        if (initMatch[lexStateIndex] == 0 ||
            initMatch[lexStateIndex] > initialState.kind)
        {
          initMatch[lexStateIndex] = initialState.kind;
          hasEmptyMatch = true;
        }
      }
      else if (initMatch[lexStateIndex] == 0)
        initMatch[lexStateIndex] = Integer.MAX_VALUE;

      RStringLiteral.FillSubString();

      if (hasNfa[lexStateIndex] && !mixed[lexStateIndex])
        RStringLiteral.GenerateNfaStartStates(this, initialState);

      RStringLiteral.UpdateStringLiteralData(totalNumStates, lexStateIndex);
      NfaState.UpdateNfaData(totalNumStates, startState, lexStateIndex,
                             canMatchAnyChar[lexStateIndex]);
      totalNumStates += NfaState.generatedStates;
      if (stateSetSize < NfaState.generatedStates)
        stateSetSize = NfaState.generatedStates;
    }

    for (i = 0; i < choices.size(); i++)
      ((RChoice)choices.get(i)).CheckUnmatchability();

    CheckEmptyStringMatch();

    tokenizerData.setParserName(cu_name);
    NfaState.BuildTokenizerData(tokenizerData);
    RStringLiteral.BuildTokenizerData(tokenizerData);

    int[] newLexStateIndices = new int[maxOrdinal];
    StringBuilder tokenMgrDecls = new StringBuilder();
    if (token_mgr_decls != null && token_mgr_decls.size() > 0) {
      Token t = (Token)token_mgr_decls.get(0);
      for (j = 0; j < token_mgr_decls.size(); j++) {
        tokenMgrDecls.append(((Token)token_mgr_decls.get(j)).image + " ");
      }
    }
    tokenizerData.setDecls(tokenMgrDecls.toString());
    Map<Integer, String> actionStrings = new HashMap<>();
    for (i = 0; i < maxOrdinal; i++) {
      if (newLexState[i] == null) {
        newLexStateIndices[i] = -1;
      } else {
        newLexStateIndices[i] = GetIndex(newLexState[i]);
      }
      // For java, we have this but for other languages, eventually we will
      // simply have a string.
      Action act = actions[i];
      if (act == null) continue;
      StringBuilder sb = new StringBuilder();
      for (int k = 0; k < act.getActionTokens().size(); k++) {
        sb.append(act.getActionTokens().get(k).image);
        sb.append(" ");
      }
      actionStrings.put(i, sb.toString());
    }
    tokenizerData.setDefaultLexState(defaultLexState);
    tokenizerData.setLexStateNames(lexStateName);
    tokenizerData.updateMatchInfo(
        actionStrings, newLexStateIndices,
        toSkip, toSpecial, toMore, toToken);
    Map<Integer, String> labels = new HashMap<>();
    String[] images = new String[rexps_of_tokens.size() + 1];
    for (Integer o: rexps_of_tokens.keySet()) {
      RegularExpression re = rexps_of_tokens.get(o);
      String label = re.label;
      if (label != null && label.length() > 0) {
        labels.put(o, label);
      }
      if (re instanceof RStringLiteral) {
        images[o] = ((RStringLiteral)re).image;
      }
    }
    tokenizerData.setLabelsAndImages(names_of_tokens, images);

    if (generateDataOnly) return;

    TokenManagerCodeGenerator gen = getCodeGenerator().getTokenManagerCodeGenerator();
    CodeGeneratorSettings settings = new CodeGeneratorSettings(Options.getOptions());
    gen.generateCode(settings, tokenizerData);
    gen.finish(settings, tokenizerData);
  }

  static void CheckEmptyStringMatch()
  {
    int i, j, k, len;
    boolean[] seen = new boolean[maxLexStates];
    boolean[] done = new boolean[maxLexStates];
    String cycle;
    String reList;

    Outer:
      for (i = 0; i < maxLexStates; i++)
      {
        if (done[i] || initMatch[i] == 0 || initMatch[i] == Integer.MAX_VALUE ||
            canMatchAnyChar[i] != -1)
          continue;

        done[i] = true;
        len = 0;
        cycle = "";
        reList = "";

        for (k = 0; k < maxLexStates; k++)
          seen[k] = false;

        j = i;
        seen[i] = true;
        cycle += lexStateName[j] + "-->";
        while (newLexState[initMatch[j]] != null)
        {
          cycle += newLexState[initMatch[j]];
          if (seen[j = GetIndex(newLexState[initMatch[j]])])
            break;

          cycle += "-->";
          done[j] = true;
          seen[j] = true;
          if (initMatch[j] == 0 || initMatch[j] == Integer.MAX_VALUE ||
              canMatchAnyChar[j] != -1)
            continue Outer;
          if (len != 0)
            reList += "; ";
          reList += "line " + rexprs[initMatch[j]].getLine() + ", column " +
          rexprs[initMatch[j]].getColumn();
          len++;
        }

        if (newLexState[initMatch[j]] == null)
          cycle += lexStateName[lexStates[initMatch[j]]];

        for (k = 0; k < maxLexStates; k++)
          canLoop[k] |= seen[k];

        hasLoop = true;
        if (len == 0)
          JavaCCErrors.warning(rexprs[initMatch[i]],
              "Regular expression" + ((rexprs[initMatch[i]].label.equals(""))
                  ? "" : (" for " + rexprs[initMatch[i]].label)) +
                  " can be matched by the empty string (\"\") in lexical state " +
                  lexStateName[i] + ". This can result in an endless loop of " +
          "empty string matches.");
        else
        {
          JavaCCErrors.warning(rexprs[initMatch[i]],
              "Regular expression" + ((rexprs[initMatch[i]].label.equals(""))
                  ? "" : (" for " + rexprs[initMatch[i]].label)) +
                  " can be matched by the empty string (\"\") in lexical state " +
                  lexStateName[i] + ". This regular expression along with the " +
                  "regular expressions at " + reList + " forms the cycle \n   " +
                  cycle + "\ncontaining regular expressions with empty matches." +
          " This can result in an endless loop of empty string matches.");
        }
      }
  }

  // Assumes l != 0L
  static char MaxChar(long l)
  {
    for (int i = 64; i-- > 0; )
      if ((l & (1L << i)) != 0L)
        return (char)i;

    return 0xffff;
  }

  public static void reInit()
  {
    actions = null;
    allTpsForState = new Hashtable();
    canLoop = null;
    canMatchAnyChar = null;
    canReachOnMore = null;
    curKind = 0;
    curRE = null;
    defaultLexState = 0;
    errorHandlingClass = null;
    hasEmptyMatch = false;
    hasLoop = false;
    hasMore = false;
    hasMoreActions = false;
    hasNfa = null;
    hasSkip = false;
    hasSkipActions = false;
    hasSpecial = false;
    hasTokenActions = false;
    ignoreCase = null;
    initMatch = null;
    initStates = new Hashtable();
    initialState = null;
    keepLineCol = false;
    kinds = null;
    lexStateIndex = 0;
    lexStateName = null;
    lexStateSuffix = null;
    lexStates = null;
    maxLexStates = 0;
    maxLongsReqd = null;
    maxOrdinal = 1;
    mixed = null;
    newLexState = null;
    rexprs = null;
    singlesToSkip = null;
    stateHasActions = null;
    stateSetSize = 0;
    staticString = null;
    toMore = null;
    toSkip = null;
    toSpecial = null;
    toToken = null;
    tokMgrClassName = null;
    tokenizerData = new TokenizerData();
    generateDataOnly = false;
  }

}
