/*
 * Copyright © 2002 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * California 95054, U.S.A. All rights reserved.  Sun Microsystems, Inc. has
 * intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation,
 * these intellectual property rights may include one or more of the U.S.
 * patents listed at http://www.sun.com/patents and one or more additional
 * patents or pending patent applications in the U.S. and in other countries.
 * U.S. Government Rights - Commercial software. Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and applicable
 * provisions of the FAR and its supplements.  Use is subject to license terms.
 * Sun,  Sun Microsystems,  the Sun logo and  Java are trademarks or registered
 * trademarks of Sun Microsystems, Inc. in the U.S. and other countries.  This
 * product is covered and controlled by U.S. Export Control laws and may be
 * subject to the export or import laws in other countries.  Nuclear, missile,
 * chemical biological weapons or nuclear maritime end uses or end users,
 * whether direct or indirect, are strictly prohibited.  Export or reexport
 * to countries subject to U.S. embargo or to entities identified on U.S.
 * export exclusion lists, including, but not limited to, the denied persons
 * and specially designated nationals lists is strictly prohibited.
 */

package org.javacc.parser;

import java.util.*;
import java.io.*;

public class LexGen
    extends JavaCCGlobals
    implements JavaCCParserConstants
{
  static private java.io.PrintWriter ostr;
  static private String staticString;
  static private String tokMgrClassName;

  // Hashtable of vectors
  static Hashtable allTpsForState = new Hashtable();
  public static int lexStateIndex = 0;
  static int[] kinds;
  public static int maxOrdinal = 1;
  public static String lexStateSuffix;
  static String[] newLexState;
  public static int[] lexStates;
  public static boolean[] ignoreCase;
  public static Action[] actions;
  public static Hashtable initStates = new Hashtable();
  public static int stateSetSize;
  public static int maxLexStates;
  public static String[] lexStateName;
  static NfaState[] singlesToSkip;
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
  static boolean hasSkipActions = false;
  static boolean hasMoreActions = false;
  static boolean hasTokenActions = false;
  static boolean hasSpecial = false;
  static boolean hasSkip = false;
  static boolean hasMore = false;
  static boolean hasToken = false;
  public static RegularExpression curRE;
  public static boolean keepLineCol;

  static void PrintClassHead()
  {
     int i, j;

     try {
       File tmp = new File(outputDir, tokMgrClassName + ".java");
       ostr = new java.io.PrintWriter(
                 new java.io.BufferedWriter(
                    new java.io.FileWriter(tmp),
                    8092
                 )
              );
       Vector tn = (Vector)(toolNames.clone());
       tn.addElement(toolName); 

       ostr.println("/* " + getIdString(tn, tokMgrClassName + ".java") + " */");

       int l = 0, kind;
       i = 1;
       for (;;)
       {
        if (cu_to_insertion_point_1.size() <= l)
         break;

        kind = ((Token)cu_to_insertion_point_1.elementAt(l)).kind;
        if(kind == PACKAGE || kind == IMPORT) {
         for (; i < cu_to_insertion_point_1.size(); i++) {
           kind = ((Token)cu_to_insertion_point_1.elementAt(i)).kind;
           if (kind == SEMICOLON ||
               kind == ABSTRACT ||
               kind == FINAL ||
               kind == PUBLIC ||
               kind == CLASS ||
               kind == INTERFACE)
           {
             cline = ((Token)(cu_to_insertion_point_1.elementAt(l))).beginLine;
             ccol = ((Token)(cu_to_insertion_point_1.elementAt(l))).beginColumn;
             for (j = l; j < i; j++) {
               printToken((Token)(cu_to_insertion_point_1.elementAt(j)), ostr);
             }
             if (kind == SEMICOLON)
               printToken((Token)(cu_to_insertion_point_1.elementAt(j)), ostr);
             ostr.println("");
             break;
           }
         }
         l = ++i;
        }
        else
         break;
       }

       ostr.println("");
       ostr.println("public class " + tokMgrClassName + " implements " +
                    cu_name + "Constants");
       ostr.println("{"); // }
     }
     catch (java.io.IOException err) {
      JavaCCErrors.semantic_error("Could not create file : " + tokMgrClassName + ".java\n");
      throw new Error();
    }

    if (token_mgr_decls != null && token_mgr_decls.size() > 0)
    {
       Token t = (Token)token_mgr_decls.elementAt(0);
       boolean commonTokenActionSeen = false;
       boolean commonTokenActionNeeded = Options.B("COMMON_TOKEN_ACTION");

       printTokenSetup((Token)token_mgr_decls.elementAt(0));
       ccol = 1;

       for (j = 0; j < token_mgr_decls.size(); j++)
       {
          t = (Token)token_mgr_decls.elementAt(j);
          if (t.kind == IDENTIFIER &&
              commonTokenActionNeeded &&
              !commonTokenActionSeen)
             commonTokenActionSeen = t.image.equals("CommonTokenAction");

          printToken(t, ostr);
       }

       ostr.println("");
       if (commonTokenActionNeeded && !commonTokenActionSeen)
          JavaCCErrors.warning("You have the COMMON_TOKEN_ACTION option set. But it appears you have not defined the method :\n"+
                          "      " + staticString + "void CommonTokenAction(Token t)\n" +
                          "in your TOKEN_MGR_DECLS. The generated token manager will not compile.");
 
    }
    else if (Options.B("COMMON_TOKEN_ACTION"))
    {
       JavaCCErrors.warning("You have the COMMON_TOKEN_ACTION option set. But you have not defined the method :\n"+
                          "      " + staticString + "void CommonTokenAction(Token t)\n" +
                          "in your TOKEN_MGR_DECLS. The generated token manager will not compile.");
    }
 
    ostr.println("  public " + staticString + " java.io.PrintStream debugStream = System.out;");
    ostr.println("  public " + staticString + " void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }");
  }

  static void DumpDebugMethods()
  {

    ostr.println("  " + staticString + " int kindCnt = 0;");
    ostr.println("  protected " + staticString + " final String jjKindsForBitVector(int i, long vec)");
    ostr.println("  {");
    ostr.println("    String retVal = \"\";");
    ostr.println("    if (i == 0)");
    ostr.println("       kindCnt = 0;");
    ostr.println("    for (int j = 0; j < 64; j++)");
    ostr.println("    {");
    ostr.println("       if ((vec & (1L << j)) != 0L)");
    ostr.println("       {");
    ostr.println("          if (kindCnt++ > 0)");
    ostr.println("             retVal += \", \";");
    ostr.println("          if (kindCnt % 5 == 0)");
    ostr.println("             retVal += \"\\n     \";");
    ostr.println("          retVal += tokenImage[i * 64 + j];");
    ostr.println("       }");
    ostr.println("    }");
    ostr.println("    return retVal;");
    ostr.println("  }");
    ostr.println("");

    ostr.println("  protected " + staticString + " final String jjKindsForStateVector(int lexState, int[] vec, int start, int end)");
    ostr.println("  {");
    ostr.println("    boolean[] kindDone = new boolean[" + maxOrdinal + "];");
    ostr.println("    String retVal = \"\";");
    ostr.println("    int cnt = 0;");
    ostr.println("    for (int i = start; i < end; i++)");
    ostr.println("    {");
    ostr.println("     if (vec[i] == -1)");
    ostr.println("       continue;");
    ostr.println("     int[] stateSet = statesForState[curLexState][vec[i]];");
    ostr.println("     for (int j = 0; j < stateSet.length; j++)");
    ostr.println("     {");
    ostr.println("       int state = stateSet[j];");
    ostr.println("       if (!kindDone[kindForState[lexState][state]])");
    ostr.println("       {");
    ostr.println("          kindDone[kindForState[lexState][state]] = true;");
    ostr.println("          if (cnt++ > 0)");
    ostr.println("             retVal += \", \";");
    ostr.println("          if (cnt % 5 == 0)");
    ostr.println("             retVal += \"\\n     \";");
    ostr.println("          retVal += tokenImage[kindForState[lexState][state]];");
    ostr.println("       }");
    ostr.println("     }");
    ostr.println("    }");
    ostr.println("    if (cnt == 0)");
    ostr.println("       return \"{  }\";");
    ostr.println("    else");
    ostr.println("       return \"{ \" + retVal + \" }\";");
    ostr.println("  }");
    ostr.println("");
  }

  static void BuildLexStatesTable()
  {
     Enumeration e = rexprlist.elements();
     TokenProduction tp;
     int i;

     String[] tmpLexStateName = new String[lexstate_I2S.size()];
     while (e.hasMoreElements())
     {
        tp = (TokenProduction)e.nextElement();
        Vector respecs = tp.respecs;
        Vector tps;

        for (i = 0; i < tp.lexStates.length; i++)
        {
           if ((tps = (Vector)allTpsForState.get(tp.lexStates[i])) == null)
           {
              tmpLexStateName[maxLexStates++] = tp.lexStates[i];
              allTpsForState.put(tp.lexStates[i], tps = new Vector());
           }

           tps.addElement(tp);
        }

        if (respecs == null || respecs.size() == 0)
           continue;

        RegularExpression re;
        for (i = 0; i < respecs.size(); i++)
           if (maxOrdinal <= (re = ((RegExprSpec)respecs.elementAt(i)).rexp).ordinal)
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
     System.arraycopy(tmpLexStateName, 0, lexStateName, 0, maxLexStates);

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

  public static void start()
  {
     if (!Options.B("BUILD_TOKEN_MANAGER") ||
         Options.B("USER_TOKEN_MANAGER") ||
         JavaCCErrors.get_error_count() > 0)
        return;

     keepLineCol = Options.B("KEEP_LINE_COLUMN");
     Vector choices = new Vector();
     Enumeration e;
     TokenProduction tp;
     int i, j;

     staticString = (Options.B("STATIC") ? "static " : "");
     tokMgrClassName = cu_name + "TokenManager";

     PrintClassHead();
     BuildLexStatesTable();

     e = allTpsForState.keys();

     boolean ignoring = false;
 
     while (e.hasMoreElements())
     {
        NfaState.ReInit();
        RStringLiteral.ReInit();

        String key = (String)e.nextElement();

        lexStateIndex = GetIndex(key);
        lexStateSuffix = "_" + lexStateIndex;
        Vector allTps = (Vector)allTpsForState.get(key);
        initStates.put(key, initialState = new NfaState());
        ignoring = false;

        singlesToSkip[lexStateIndex] = new NfaState();
        singlesToSkip[lexStateIndex].dummy = true;

        if (key.equals("DEFAULT"))
           defaultLexState = lexStateIndex;

        for (i = 0; i < allTps.size(); i++)
        {
           tp = (TokenProduction)allTps.elementAt(i);
           int kind = tp.kind;
           boolean ignore = tp.ignoreCase;
           Vector rexps = tp.respecs;

           if (i == 0)
              ignoring = ignore;

           for (j = 0; j < rexps.size(); j++)
           {
              RegExprSpec respec = (RegExprSpec)rexps.elementAt(j);
              curRE = respec.rexp;

              rexprs[curKind = curRE.ordinal] = curRE;
              lexStates[curRE.ordinal] = lexStateIndex;
              ignoreCase[curRE.ordinal] = ignore;

              if (curRE.private_rexp)
              {
                 kinds[curRE.ordinal] = -1;
                 continue;
              }

              if (curRE instanceof RStringLiteral &&
                  !((RStringLiteral)curRE).image.equals(""))
              {
                 ((RStringLiteral)curRE).GenerateDfa(ostr, curRE.ordinal);
                 if (i != 0 && !mixed[lexStateIndex] && ignoring != ignore)
                    mixed[lexStateIndex] = true;
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
                    choices.addElement(curRE);

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

              if (respec.act != null && respec.act.action_tokens != null &&
                  respec.act.action_tokens.size() > 0)
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
                    hasToken = true;
                    toToken[curRE.ordinal / 64] |= 1L << (curRE.ordinal % 64);
                    break;
              }
           }
        }

        // Generate a static block for initializing the nfa transitions
        NfaState.ComputeClosures();

        for (i = 0; i < initialState.epsilonMoves.size(); i++)
           ((NfaState)initialState.epsilonMoves.elementAt(i)).GenerateCode();

        if (hasNfa[lexStateIndex] = (NfaState.generatedStates != 0))
        {
           initialState.GenerateCode();
           initialState.GenerateInitMoves(ostr);
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
           RStringLiteral.GenerateNfaStartStates(ostr, initialState);

        RStringLiteral.DumpDfaCode(ostr);

        if (hasNfa[lexStateIndex])
           NfaState.DumpMoveNfa(ostr);

        if (stateSetSize < NfaState.generatedStates)
           stateSetSize = NfaState.generatedStates;
     }

     for (i = 0; i < choices.size(); i++)
        ((RChoice)choices.elementAt(i)).CheckUnmatchability();

     NfaState.DumpStateSets(ostr);
     CheckEmptyStringMatch();
     NfaState.DumpNonAsciiMoveMethods(ostr);
     RStringLiteral.DumpStrLiteralImages(ostr);
     DumpStaticVarDeclarations();
     DumpFillToken();
     DumpGetNextToken();

     if (Options.B("DEBUG_TOKEN_MANAGER"))
     {
        NfaState.DumpStatesForKind(ostr);
        DumpDebugMethods();
     }

     if (hasLoop)
     {
        ostr.println(staticString + "int[] jjemptyLineNo = new int[" + maxLexStates + "];");
        ostr.println(staticString + "int[] jjemptyColNo = new int[" + maxLexStates + "];");
        ostr.println(staticString + "boolean[] jjbeenHere = new boolean[" + maxLexStates + "];");
     }

     if (hasSkipActions)
        DumpSkipActions();
     if (hasMoreActions)
        DumpMoreActions();
     if (hasTokenActions)
        DumpTokenActions();

     ostr.println(/*{*/ "}");
     ostr.close();
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
           reList += "line " + rexprs[initMatch[j]].line + ", column " + 
                     rexprs[initMatch[j]].column;
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

  static void PrintArrayInitializer(int noElems)
  {
     ostr.print("{");
     for (int i = 0; i < noElems; i++)
     {
        if (i % 25 == 0)
           ostr.print("\n   ");
        ostr.print("0, ");
     }
     ostr.println("\n};");
  }

  static void DumpStaticVarDeclarations()
  {
      int i;
      String charStreamName;

      ostr.println("public static final String[] lexStateNames = {");
      for (i = 0; i < maxLexStates; i++)
         ostr.println("   \"" + lexStateName[i] + "\", ");
      ostr.println("};");

      if (maxLexStates > 1)
      {
         ostr.print("public static final int[] jjnewLexState = {");

         for (i = 0; i < maxOrdinal; i++)
         {
            if (i % 25 == 0)
               ostr.print("\n   ");

            if (newLexState[i] == null)
               ostr.print("-1, ");
            else
               ostr.print(GetIndex(newLexState[i]) + ", ");
         }
         ostr.println("\n};");
      }

      if (hasSkip || hasMore || hasSpecial)
      {
         // Bit vector for TOKEN
         ostr.print("static final long[] jjtoToken = {");
         for (i = 0; i < maxOrdinal / 64 + 1; i++)
         {
            if (i % 4 == 0)
               ostr.print("\n   ");
            ostr.print("0x" + Long.toHexString(toToken[i]) + "L, ");
         }
         ostr.println("\n};");
      }

      if (hasSkip || hasSpecial)
      {
         // Bit vector for SKIP
         ostr.print("static final long[] jjtoSkip = {");
         for (i = 0; i < maxOrdinal / 64 + 1; i++)
         {
            if (i % 4 == 0)
               ostr.print("\n   ");
            ostr.print("0x" + Long.toHexString(toSkip[i]) + "L, ");
         }
         ostr.println("\n};");
      }

      if (hasSpecial)
      {
         // Bit vector for SPECIAL
         ostr.print("static final long[] jjtoSpecial = {");
         for (i = 0; i < maxOrdinal / 64 + 1; i++)
         {
            if (i % 4 == 0)
               ostr.print("\n   ");
            ostr.print("0x" + Long.toHexString(toSpecial[i]) + "L, ");
         }
         ostr.println("\n};");
      }

      if (hasMore)
      {
         // Bit vector for MORE
         ostr.print("static final long[] jjtoMore = {");
         for (i = 0; i < maxOrdinal / 64 + 1; i++)
         {
            if (i % 4 == 0)
               ostr.print("\n   ");
            ostr.print("0x" + Long.toHexString(toMore[i]) + "L, ");
         }
         ostr.println("\n};");
      }

      if (Options.B("USER_CHAR_STREAM"))
         charStreamName = "CharStream";
      else
      {
         if (Options.B("JAVA_UNICODE_ESCAPE"))
            charStreamName = "JavaCharStream";
         else
            charStreamName = "SimpleCharStream";
      }

      ostr.println(staticString + "protected " + charStreamName + " input_stream;");

      ostr.println(staticString + "private final int[] jjrounds = " +
                      "new int[" + stateSetSize + "];");
      ostr.println(staticString + "private final int[] jjstateSet = " +
                      "new int[" + (2 * stateSetSize) + "];");

      if (hasMoreActions || hasSkipActions || hasTokenActions)
      {
         ostr.println(staticString + "StringBuffer image;");
         ostr.println(staticString + "int jjimageLen;");
         ostr.println(staticString + "int lengthOfMatch;");
      }

      ostr.println(staticString + "protected char curChar;");

      ostr.println("public " + tokMgrClassName + "(" + charStreamName + " stream)");
      ostr.println("{");

      if (Options.B("STATIC") && !Options.B("USER_CHAR_STREAM"))
      {
         ostr.println("   if (input_stream != null)");
         ostr.println("      throw new TokenMgrError(\"ERROR: Second call to constructor of static lexer. You must use ReInit() to initialize the static variables.\", TokenMgrError.STATIC_LEXER_ERROR);");
      }
      else if (!Options.B("USER_CHAR_STREAM"))
      {
         if (Options.B("JAVA_UNICODE_ESCAPE"))
            ostr.println("   if (JavaCharStream.staticFlag)");
         else
            ostr.println("   if (SimpleCharStream.staticFlag)");

         ostr.println("      throw new Error(\"ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.\");");
      }

      ostr.println("   input_stream = stream;");

      ostr.println("}");

      ostr.println("public " + tokMgrClassName + "(" + charStreamName + " stream, int lexState)");
      ostr.println("{");
      ostr.println("   this(stream);");
      ostr.println("   SwitchTo(lexState);");
      ostr.println("}");

      // Reinit method for reinitializing the parser (for static parsers).
      ostr.println(staticString + "public void ReInit(" + charStreamName + " stream)");
      ostr.println("{");
      ostr.println("   jjmatchedPos = jjnewStateCnt = 0;");
      ostr.println("   curLexState = defaultLexState;");
      ostr.println("   input_stream = stream;");
      ostr.println("   ReInitRounds();");
      ostr.println("}");

      // Method to reinitialize the jjrounds array.
      ostr.println(staticString + "private final void ReInitRounds()");
      ostr.println("{");
      ostr.println("   int i;");
      ostr.println("   jjround = 0x" + Integer.toHexString(Integer.MIN_VALUE + 1)+ ";");
      ostr.println("   for (i = " + stateSetSize + "; i-- > 0;)");
      ostr.println("      jjrounds[i] = 0x" + Integer.toHexString(Integer.MIN_VALUE) + ";");
      ostr.println("}");

      // Reinit method for reinitializing the parser (for static parsers).
      ostr.println(staticString + "public void ReInit(" + charStreamName + " stream, int lexState)");
      ostr.println("{");
      ostr.println("   ReInit(stream);");
      ostr.println("   SwitchTo(lexState);");
      ostr.println("}");

      ostr.println(staticString + "public void SwitchTo(int lexState)");
      ostr.println("{");
      ostr.println("   if (lexState >= " + lexStateName.length + " || lexState < 0)");
      ostr.println("      throw new TokenMgrError(\"Error: Ignoring invalid lexical state : \"" +
                     " + lexState + \". State unchanged.\", TokenMgrError.INVALID_LEXICAL_STATE);");
      ostr.println("   else");
      ostr.println("      curLexState = lexState;");
      ostr.println("}");

      ostr.println("");
  }

  // Assumes l != 0L
  static char MaxChar(long l)
  {
     for (int i = 64; i-- > 0; )
       if ((l & (1L << i)) != 0L)
          return (char)i;

     return 0xffff;
  }

  static void DumpFillToken()
  {
     ostr.println(staticString + "protected Token jjFillToken()");
     ostr.println("{");
     ostr.println("   Token t = Token.newToken(jjmatchedKind);");
     ostr.println("   t.kind = jjmatchedKind;");

     if (hasEmptyMatch)
     {
        ostr.println("   if (jjmatchedPos < 0)");
        ostr.println("   {");
        ostr.println("      if (image == null)");
        ostr.println("         t.image = \"\";");
        ostr.println("      else");
        ostr.println("         t.image = image.toString();");

        if (keepLineCol)
        {
           ostr.println("      t.beginLine = t.endLine = input_stream.getBeginLine();");
           ostr.println("      t.beginColumn = t.endColumn = input_stream.getBeginColumn();");
        }

        ostr.println("   }");
        ostr.println("   else");
        ostr.println("   {");
        ostr.println("      String im = jjstrLiteralImages[jjmatchedKind];");
        ostr.println("      t.image = (im == null) ? input_stream.GetImage() : im;");

        if (keepLineCol)
        {
           ostr.println("      t.beginLine = input_stream.getBeginLine();");
           ostr.println("      t.beginColumn = input_stream.getBeginColumn();");
           ostr.println("      t.endLine = input_stream.getEndLine();");
           ostr.println("      t.endColumn = input_stream.getEndColumn();");
        }

        ostr.println("   }");
     }
     else
     {
        ostr.println("   String im = jjstrLiteralImages[jjmatchedKind];");
        ostr.println("   t.image = (im == null) ? input_stream.GetImage() : im;");

        if (keepLineCol)
        {
           ostr.println("   t.beginLine = input_stream.getBeginLine();");
           ostr.println("   t.beginColumn = input_stream.getBeginColumn();");
           ostr.println("   t.endLine = input_stream.getEndLine();");
           ostr.println("   t.endColumn = input_stream.getEndColumn();");
        }

     }

     ostr.println("   return t;");
     ostr.println("}");
  }

  static void DumpGetNextToken()
  {
     int i;

     ostr.println("");
     ostr.println(staticString + "int curLexState = " + defaultLexState + ";");
     ostr.println(staticString + "int defaultLexState = " + defaultLexState + ";");
     ostr.println(staticString + "int jjnewStateCnt;");
     ostr.println(staticString + "int jjround;");
     ostr.println(staticString + "int jjmatchedPos;");
     ostr.println(staticString + "int jjmatchedKind;");
     ostr.println("");
     ostr.println("public " + staticString + "Token getNextToken()" +
                 " ");
     ostr.println("{");
     ostr.println("  int kind;");
     ostr.println("  Token specialToken = null;");
     ostr.println("  Token matchedToken;");
     ostr.println("  int curPos = 0;");
     ostr.println("");
     ostr.println("  EOFLoop :\n  for (;;)");
     ostr.println("  {   ");
     ostr.println("   try   ");
     ostr.println("   {     ");
     ostr.println("      curChar = input_stream.BeginToken();");
     ostr.println("   }     ");
     ostr.println("   catch(java.io.IOException e)");
     ostr.println("   {        ");

     if (Options.B("DEBUG_TOKEN_MANAGER"))
         ostr.println("      debugStream.println(\"Returning the <EOF> token.\");");

     ostr.println("      jjmatchedKind = 0;");
     ostr.println("      matchedToken = jjFillToken();");

     if (hasSpecial)
        ostr.println("      matchedToken.specialToken = specialToken;");

     if (nextStateForEof != null || actForEof != null)
        ostr.println("      TokenLexicalActions(matchedToken);");

     if (Options.B("COMMON_TOKEN_ACTION"))
        ostr.println("      CommonTokenAction(matchedToken);");

     ostr.println("      return matchedToken;");
     ostr.println("   }");

     if (hasMoreActions || hasSkipActions || hasTokenActions)
     {
        ostr.println("   image = null;");
        ostr.println("   jjimageLen = 0;");
     }

     ostr.println("");

     String prefix = "";
     if (hasMore)
     {
        ostr.println("   for (;;)");
        ostr.println("   {");
        prefix = "  ";
     }

     String endSwitch = "";
     String caseStr = "";
     // this also sets up the start state of the nfa
     if (maxLexStates > 1)
     {
        ostr.println(prefix + "   switch(curLexState)");
        ostr.println(prefix + "   {");
        endSwitch = prefix + "   }";
        caseStr = prefix + "     case ";
        prefix += "    ";
     }

     prefix += "   ";
     for(i = 0; i < maxLexStates; i++)
     {
        if (maxLexStates > 1)
           ostr.println(caseStr + i + ":");

        if (singlesToSkip[i].HasTransitions())
        {
           // added the backup(0) to make JIT happy
           ostr.println(prefix + "try { input_stream.backup(0);");
           if (singlesToSkip[i].asciiMoves[0] != 0L &&
               singlesToSkip[i].asciiMoves[1] != 0L)
           {
              ostr.println(prefix + "   while ((curChar < 64" + " && (0x" +
                           Long.toHexString(singlesToSkip[i].asciiMoves[0]) +
                           "L & (1L << curChar)) != 0L) || \n" +
                           prefix + "          (curChar >> 6) == 1" +
                           " && (0x" +
                           Long.toHexString(singlesToSkip[i].asciiMoves[1]) +
                           "L & (1L << (curChar & 077))) != 0L)");
           }
           else if (singlesToSkip[i].asciiMoves[1] == 0L)
           {
              ostr.println(prefix + "   while (curChar <= " +
                           (int)MaxChar(singlesToSkip[i].asciiMoves[0]) + " && (0x" +
                           Long.toHexString(singlesToSkip[i].asciiMoves[0]) +
                           "L & (1L << curChar)) != 0L)");
           }
           else if (singlesToSkip[i].asciiMoves[0] == 0L)
           {
              ostr.println(prefix + "   while (curChar > 63 && curChar <= " +
                           ((int)MaxChar(singlesToSkip[i].asciiMoves[1]) + 64) +
                           " && (0x" +
                           Long.toHexString(singlesToSkip[i].asciiMoves[1]) +
                           "L & (1L << (curChar & 077))) != 0L)");
           }

           if (Options.B("DEBUG_TOKEN_MANAGER"))
           {
              ostr.println(prefix + "{");
              ostr.println("      debugStream.println(" + (maxLexStates > 1 ? "\"<\" + lexStateNames[curLexState] + \">\" + " : "") + "\"Skipping character : \" + " +
                 "TokenMgrError.addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \")\");");
           }
           ostr.println(prefix + "      curChar = input_stream.BeginToken();");

           if (Options.B("DEBUG_TOKEN_MANAGER"))
              ostr.println(prefix + "}");

           ostr.println(prefix + "}");
           ostr.println(prefix + "catch (java.io.IOException e1) { continue EOFLoop; }");
        }

        if (initMatch[i] != Integer.MAX_VALUE && initMatch[i] != 0)
        {
           if (Options.B("DEBUG_TOKEN_MANAGER"))
              ostr.println("      debugStream.println(\"   Matched the empty string as \" + tokenImage[" +
                initMatch[i] + "] + \" token.\");");

           ostr.println(prefix + "jjmatchedKind = " + initMatch[i] + ";");
           ostr.println(prefix + "jjmatchedPos = -1;");
           ostr.println(prefix + "curPos = 0;");
        }
        else
        {
           ostr.println(prefix + "jjmatchedKind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");
           ostr.println(prefix + "jjmatchedPos = 0;");
        }

     if (Options.B("DEBUG_TOKEN_MANAGER"))
        ostr.println("      debugStream.println(" + (maxLexStates > 1 ? "\"<\" + lexStateNames[curLexState] + \">\" + " : "") + "\"Current character : \" + " +
                 "TokenMgrError.addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \")\");");

        ostr.println(prefix + "curPos = jjMoveStringLiteralDfa0_" + i + "();");

        if (canMatchAnyChar[i] != -1)
        {
           if (initMatch[i] != Integer.MAX_VALUE && initMatch[i] != 0)
              ostr.println(prefix + "if (jjmatchedPos < 0 || (jjmatchedPos == 0 && jjmatchedKind > " +
                     canMatchAnyChar[i] + "))");
           else
              ostr.println(prefix + "if (jjmatchedPos == 0 && jjmatchedKind > " +
                     canMatchAnyChar[i] + ")");
           ostr.println(prefix + "{");
           
           if (Options.B("DEBUG_TOKEN_MANAGER"))
              ostr.println("           debugStream.println(\"   Current character matched as a \" + tokenImage[" +
                canMatchAnyChar[i] + "] + \" token.\");");
           ostr.println(prefix + "   jjmatchedKind = " + canMatchAnyChar[i] + ";");

           if (initMatch[i] != Integer.MAX_VALUE && initMatch[i] != 0)
              ostr.println(prefix + "   jjmatchedPos = 0;");

           ostr.println(prefix + "}");
        }

        if (maxLexStates > 1)
           ostr.println(prefix + "break;");
     }

     if (maxLexStates > 1)
        ostr.println(endSwitch);
     else if (maxLexStates == 0)
        ostr.println("       jjmatchedKind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");

     if (maxLexStates > 1)
        prefix = "  ";
     else
        prefix = "";

     if (maxLexStates > 0)
     {
        ostr.println(prefix + "   if (jjmatchedKind != 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
        ostr.println(prefix + "   {");
        ostr.println(prefix + "      if (jjmatchedPos + 1 < curPos)");

        if (Options.B("DEBUG_TOKEN_MANAGER"))
        {
           ostr.println(prefix + "      {");
           ostr.println(prefix + "         debugStream.println(\"   Putting back \" + (curPos - jjmatchedPos - 1) + \" characters into the input stream.\");");
        }

        ostr.println(prefix + "         input_stream.backup(curPos - jjmatchedPos - 1);");

        if (Options.B("DEBUG_TOKEN_MANAGER"))
           ostr.println(prefix + "      }");

        if (Options.B("DEBUG_TOKEN_MANAGER"))
        {
           if (Options.B("JAVA_UNICODE_ESCAPE") ||
               Options.B("USER_CHAR_STREAM"))
              ostr.println("    debugStream.println(\"****** FOUND A \" + tokenImage[jjmatchedKind] + \" MATCH (\" + TokenMgrError.addEscapes(new String(input_stream.GetSuffix(jjmatchedPos + 1))) + \") ******\\n\");");
           else
              ostr.println("    debugStream.println(\"****** FOUND A \" + tokenImage[jjmatchedKind] + \" MATCH (\" + TokenMgrError.addEscapes(new String(input_stream.GetSuffix(jjmatchedPos + 1))) + \") ******\\n\");");
        }

        if (hasSkip || hasMore || hasSpecial)
        {
           ostr.println(prefix + "      if ((jjtoToken[jjmatchedKind >> 6] & " + 
                                     "(1L << (jjmatchedKind & 077))) != 0L)");
           ostr.println(prefix + "      {");
        }

        ostr.println(prefix + "         matchedToken = jjFillToken();");

        if (hasSpecial)
           ostr.println(prefix + "         matchedToken.specialToken = specialToken;");

        if (hasTokenActions)
           ostr.println(prefix + "         TokenLexicalActions(matchedToken);");

        if (maxLexStates > 1)
        {
           ostr.println("       if (jjnewLexState[jjmatchedKind] != -1)");
           ostr.println(prefix + "       curLexState = jjnewLexState[jjmatchedKind];");
        }

        if (Options.B("COMMON_TOKEN_ACTION"))
           ostr.println(prefix + "         CommonTokenAction(matchedToken);");

        ostr.println(prefix + "         return matchedToken;");

        if (hasSkip || hasMore || hasSpecial)
        {
           ostr.println(prefix + "      }");

           if (hasSkip || hasSpecial)
           {
              if (hasMore)
              {
                 ostr.println(prefix + "      else if ((jjtoSkip[jjmatchedKind >> 6] & " +
                                      "(1L << (jjmatchedKind & 077))) != 0L)");
              }
              else
                 ostr.println(prefix + "      else");

              ostr.println(prefix + "      {");

              if (hasSpecial)
              {
                 ostr.println(prefix + "         if ((jjtoSpecial[jjmatchedKind >> 6] & " +
                                    "(1L << (jjmatchedKind & 077))) != 0L)");
                 ostr.println(prefix + "         {");

                 ostr.println(prefix + "            matchedToken = jjFillToken();");

                 ostr.println(prefix + "            if (specialToken == null)");
                 ostr.println(prefix + "               specialToken = matchedToken;");
                 ostr.println(prefix + "            else");
                 ostr.println(prefix + "            {");
                 ostr.println(prefix + "               matchedToken.specialToken = specialToken;");
                 ostr.println(prefix + "               specialToken = (specialToken.next = matchedToken);");
                 ostr.println(prefix + "            }");

                 if (hasSkipActions)
                    ostr.println(prefix + "            SkipLexicalActions(matchedToken);");

                 ostr.println(prefix + "         }");

                 if (hasSkipActions)
                 {
                    ostr.println(prefix + "         else ");
                    ostr.println(prefix + "            SkipLexicalActions(null);");
                 }
              }
              else if (hasSkipActions)
                 ostr.println(prefix + "         SkipLexicalActions(null);");

              if (maxLexStates > 1)
              {
                 ostr.println("         if (jjnewLexState[jjmatchedKind] != -1)");
                 ostr.println(prefix + "         curLexState = jjnewLexState[jjmatchedKind];");
              }

              ostr.println(prefix + "         continue EOFLoop;");
              ostr.println(prefix + "      }");
           }

           if (hasMore)
           {
              if (hasMoreActions)
                 ostr.println(prefix + "      MoreLexicalActions();");
              else if (hasSkipActions || hasTokenActions)
                 ostr.println(prefix + "      jjimageLen += jjmatchedPos + 1;");

              if (maxLexStates > 1)
              {
                 ostr.println("      if (jjnewLexState[jjmatchedKind] != -1)");
                 ostr.println(prefix + "      curLexState = jjnewLexState[jjmatchedKind];");
              }
              ostr.println(prefix + "      curPos = 0;");
              ostr.println(prefix + "      jjmatchedKind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");

              ostr.println(prefix + "      try {");
              ostr.println(prefix + "         curChar = input_stream.readChar();");

              if (Options.B("DEBUG_TOKEN_MANAGER"))
                 ostr.println("   debugStream.println(" + (maxLexStates > 1 ? "\"<\" + lexStateNames[curLexState] + \">\" + " : "") + "\"Current character : \" + " +
                    "TokenMgrError.addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \")\");");
              ostr.println(prefix + "         continue;");
              ostr.println(prefix + "      }");
              ostr.println(prefix + "      catch (java.io.IOException e1) { }");
           }
        }

        ostr.println(prefix + "   }");
        ostr.println(prefix + "   int error_line = input_stream.getEndLine();");
        ostr.println(prefix + "   int error_column = input_stream.getEndColumn();");
        ostr.println(prefix + "   String error_after = null;");
        ostr.println(prefix + "   boolean EOFSeen = false;");
        ostr.println(prefix + "   try { input_stream.readChar(); input_stream.backup(1); }");
        ostr.println(prefix + "   catch (java.io.IOException e1) {");
        ostr.println(prefix + "      EOFSeen = true;");
        ostr.println(prefix + "      error_after = curPos <= 1 ? \"\" : input_stream.GetImage();");
        ostr.println(prefix + "      if (curChar == '\\n' || curChar == '\\r') {");
        ostr.println(prefix + "         error_line++;");
        ostr.println(prefix + "         error_column = 0;");
        ostr.println(prefix + "      }");
        ostr.println(prefix + "      else");
        ostr.println(prefix + "         error_column++;");
        ostr.println(prefix + "   }");
        ostr.println(prefix + "   if (!EOFSeen) {");
        ostr.println(prefix + "      input_stream.backup(1);");
        ostr.println(prefix + "      error_after = curPos <= 1 ? \"\" : input_stream.GetImage();");
        ostr.println(prefix + "   }");
        ostr.println(prefix + "   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);");
     }

     if (hasMore)
        ostr.println(prefix + " }");

     ostr.println("  }");
     ostr.println("}");
     ostr.println("");
  }

  public static void DumpSkipActions()
  {
     Action act;

     ostr.println(staticString + "void SkipLexicalActions(Token matchedToken)");
     ostr.println("{");
     ostr.println("   switch(jjmatchedKind)");
     ostr.println("   {");

     Outer:
     for (int i = 0; i < maxOrdinal; i++)
     {
        if ((toSkip[i / 64] & (1L << (i % 64))) == 0L)
           continue;

        for (;;)
        {
           if (((act = (Action)actions[i]) == null ||
                 act.action_tokens == null ||
                 act.action_tokens.size() == 0) && !canLoop[lexStates[i]])
              continue Outer;

           ostr.println("      case " + i + " :");

           if (initMatch[lexStates[i]] == i && canLoop[lexStates[i]])
           {
              ostr.println("         if (jjmatchedPos == -1)");
              ostr.println("         {");
              ostr.println("            if (jjbeenHere[" + lexStates[i] + "] &&");
              ostr.println("                jjemptyLineNo[" + lexStates[i] + "] == input_stream.getBeginLine() && ");
              ostr.println("                jjemptyColNo[" + lexStates[i] + "] == input_stream.getBeginColumn())");
              ostr.println("               throw new TokenMgrError((\"Error: Bailing out of infinite loop caused by repeated empty string matches at line \" + input_stream.getBeginLine() + \", column \" + input_stream.getBeginColumn() + \".\"), TokenMgrError.LOOP_DETECTED);");
              ostr.println("            jjemptyLineNo[" + lexStates[i] + "] = input_stream.getBeginLine();");
              ostr.println("            jjemptyColNo[" + lexStates[i] + "] = input_stream.getBeginColumn();");
              ostr.println("            jjbeenHere[" + lexStates[i] + "] = true;");
              ostr.println("         }");
           }

           if ((act = (Action)actions[i]) == null ||
                act.action_tokens.size() == 0)
              break;

           ostr.println("         if (image == null)");
           ostr.print("            image = ");

           if (RStringLiteral.allImages[i] != null)
              ostr.println("new StringBuffer(jjstrLiteralImages[" + i + "]);");
           else
           {
              if (Options.B("JAVA_UNICODE_ESCAPE") ||
                  Options.B("USER_CHAR_STREAM"))
                 ostr.println("new StringBuffer(new String(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1))));");
              else
                 ostr.println("new StringBuffer(new String(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1))));");
           }

           ostr.println("         else");
           ostr.print("            image.append");

           if (RStringLiteral.allImages[i] != null)
              ostr.println("(jjstrLiteralImages[" + i + "]);");
           else
              if (Options.B("JAVA_UNICODE_ESCAPE") ||
                  Options.B("USER_CHAR_STREAM"))
                 ostr.println("(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));");
              else
                 ostr.println("(new String(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1))));");

           printTokenSetup((Token)act.action_tokens.elementAt(0));
           ccol = 1;

           for (int j = 0; j < act.action_tokens.size(); j++)
              printToken((Token)act.action_tokens.elementAt(j), ostr);
           ostr.println("");

           break;
        }

        ostr.println("         break;");
     }

     ostr.println("      default :");
     ostr.println("         break;");
     ostr.println("   }");
     ostr.println("}");
  }

  public static void DumpMoreActions()
  {
     Action act;

     ostr.println(staticString + "void MoreLexicalActions()");
     ostr.println("{");
     ostr.println("   jjimageLen += (lengthOfMatch = jjmatchedPos + 1);");
     ostr.println("   switch(jjmatchedKind)");
     ostr.println("   {");

     Outer:
     for (int i = 0; i < maxOrdinal; i++)
     {
        if ((toMore[i / 64] & (1L << (i % 64))) == 0L)
           continue;

        for (;;)
        {
           if (((act = (Action)actions[i]) == null ||
                 act.action_tokens == null ||
                 act.action_tokens.size() == 0) && !canLoop[lexStates[i]])
              continue Outer;

           ostr.println("      case " + i + " :");

           if (initMatch[lexStates[i]] == i && canLoop[lexStates[i]])
           {
              ostr.println("         if (jjmatchedPos == -1)");
              ostr.println("         {");
              ostr.println("            if (jjbeenHere[" + lexStates[i] + "] &&");
              ostr.println("                jjemptyLineNo[" + lexStates[i] + "] == input_stream.getBeginLine() && ");
              ostr.println("                jjemptyColNo[" + lexStates[i] + "] == input_stream.getBeginColumn())");
              ostr.println("               throw new TokenMgrError((\"Error: Bailing out of infinite loop caused by repeated empty string matches at line \" + input_stream.getBeginLine() + \", column \" + input_stream.getBeginColumn() + \".\"), TokenMgrError.LOOP_DETECTED);");
              ostr.println("            jjemptyLineNo[" + lexStates[i] + "] = input_stream.getBeginLine();");
              ostr.println("            jjemptyColNo[" + lexStates[i] + "] = input_stream.getBeginColumn();");
              ostr.println("            jjbeenHere[" + lexStates[i] + "] = true;");
              ostr.println("         }");
           }

           if ((act = (Action)actions[i]) == null ||
                act.action_tokens.size() == 0)
           {
              break;
           }

           ostr.println("         if (image == null)");
           ostr.print("              image = ");

           if (RStringLiteral.allImages[i] != null)
              ostr.println("new StringBuffer(jjstrLiteralImages[" + i + "]);");
           else
           {
              if (Options.B("JAVA_UNICODE_ESCAPE") ||
                  Options.B("USER_CHAR_STREAM"))
                 ostr.println("new StringBuffer(new String(input_stream.GetSuffix(jjimageLen)));");
              else
                 ostr.println("new StringBuffer(new String(input_stream.GetSuffix(jjimageLen)));");
           }

           ostr.println("         else");
           ostr.print("            image.append");

           if (RStringLiteral.allImages[i] != null)
              ostr.println("(jjstrLiteralImages[" + i + "]);");
           else
              if (Options.B("JAVA_UNICODE_ESCAPE") ||
                  Options.B("USER_CHAR_STREAM"))
                 ostr.println("(input_stream.GetSuffix(jjimageLen));");
              else
                 ostr.println("(new String(input_stream.GetSuffix(jjimageLen)));");

           ostr.println("         jjimageLen = 0;");
           printTokenSetup((Token)act.action_tokens.elementAt(0));
           ccol = 1;

           for (int j = 0; j < act.action_tokens.size(); j++)
              printToken((Token)act.action_tokens.elementAt(j), ostr);
           ostr.println("");

           break;
        }

        ostr.println("         break;");
     }

     ostr.println("      default : ");
     ostr.println("         break;");

     ostr.println("   }");
     ostr.println("}");
  }

  public static void DumpTokenActions()
  {
     Action act;
     int i;

     ostr.println(staticString + "void TokenLexicalActions(Token matchedToken)");
     ostr.println("{");
     ostr.println("   switch(jjmatchedKind)");
     ostr.println("   {");

     Outer:
     for (i = 0; i < maxOrdinal; i++)
     {
        if ((toToken[i / 64] & (1L << (i % 64))) == 0L)
           continue;

        for (;;)
        {
           if (((act = (Action)actions[i]) == null ||
                 act.action_tokens == null ||
                 act.action_tokens.size() == 0) && !canLoop[lexStates[i]])
              continue Outer;

           ostr.println("      case " + i + " :");

           if (initMatch[lexStates[i]] == i && canLoop[lexStates[i]])
           {
              ostr.println("         if (jjmatchedPos == -1)");
              ostr.println("         {");
              ostr.println("            if (jjbeenHere[" + lexStates[i] + "] &&");
              ostr.println("                jjemptyLineNo[" + lexStates[i] + "] == input_stream.getBeginLine() && ");
              ostr.println("                jjemptyColNo[" + lexStates[i] + "] == input_stream.getBeginColumn())");
              ostr.println("               throw new TokenMgrError((\"Error: Bailing out of infinite loop caused by repeated empty string matches at line \" + input_stream.getBeginLine() + \", column \" + input_stream.getBeginColumn() + \".\"), TokenMgrError.LOOP_DETECTED);");
              ostr.println("            jjemptyLineNo[" + lexStates[i] + "] = input_stream.getBeginLine();");
              ostr.println("            jjemptyColNo[" + lexStates[i] + "] = input_stream.getBeginColumn();");
              ostr.println("            jjbeenHere[" + lexStates[i] + "] = true;");
              ostr.println("         }");
           }

           if ((act = (Action)actions[i]) == null ||
                act.action_tokens.size() == 0)
              break;

           if (i == 0)
           {
              ostr.println("      image = null;"); // For EOF no image is there
           }
           else
           {
           ostr.println("        if (image == null)");
           ostr.print("            image = ");

           if (RStringLiteral.allImages[i] != null)
              ostr.println("new StringBuffer(jjstrLiteralImages[" + i + "]);");
           else
           {
              if (Options.B("JAVA_UNICODE_ESCAPE") ||
                  Options.B("USER_CHAR_STREAM"))
                 ostr.println("new StringBuffer(new String(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1))));");
              else
                 ostr.println("new StringBuffer(new String(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1))));");
           }

           ostr.println("         else");
           ostr.print("            image.append");

           if (RStringLiteral.allImages[i] != null)
              ostr.println("(jjstrLiteralImages[" + i + "]);");
           else
              if (Options.B("JAVA_UNICODE_ESCAPE") ||
                  Options.B("USER_CHAR_STREAM"))
                 ostr.println("(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));");
              else
                 ostr.println("(new String(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1))));");

           }
           printTokenSetup((Token)act.action_tokens.elementAt(0));
           ccol = 1;

           for (int j = 0; j < act.action_tokens.size(); j++)
              printToken((Token)act.action_tokens.elementAt(j), ostr);
           ostr.println("");

           break;
        }

        ostr.println("         break;");
     }

     ostr.println("      default : ");
     ostr.println("         break;");
     ostr.println("   }");
     ostr.println("}");
  }

   public static void reInit()
   {
      ostr = null;
      staticString = null;
      tokMgrClassName = null;
      allTpsForState = new Hashtable();
      lexStateIndex = 0;
      kinds = null;
      maxOrdinal = 1;
      lexStateSuffix = null;
      newLexState = null;
      lexStates = null;
      ignoreCase = null;
      actions = null;
      initStates = new Hashtable();
      stateSetSize = 0;
      maxLexStates = 0;
      lexStateName = null;
      singlesToSkip = null;
      toSkip = null;
      toSpecial = null;
      toMore = null;
      toToken = null;
      defaultLexState = 0;
      rexprs = null;
      maxLongsReqd = null;
      initMatch = null;
      canMatchAnyChar = null;
      hasEmptyMatch = false;
      canLoop = null;
      stateHasActions = null;
      hasLoop = false;
      canReachOnMore = null;
      hasNfa = null;
      mixed = null;
      initialState = null;
      curKind = 0;
      hasSkipActions = false;
      hasMoreActions = false;
      hasTokenActions = false;
      hasSpecial = false;
      hasSkip = false;
      hasMore = false;
      hasToken = false;
      curRE = null;
   }

}
