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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

final class KindInfo
{
   long[] validKinds;
   long[] finalKinds;
   int    validKindCnt = 0;
   int    finalKindCnt = 0;

   KindInfo(int maxKind)
   {
      validKinds = new long[maxKind / 64 + 1];
      finalKinds = new long[maxKind / 64 + 1];
   }

   public void InsertValidKind(int kind)
   {
      validKinds[kind / 64] |= (1L << (kind % 64));
      validKindCnt++;
   }

   public void InsertFinalKind(int kind)
   {
      finalKinds[kind / 64] |= (1L << (kind % 64));
      finalKindCnt++;
   }
};

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

    public RStringLiteral(Token t, String image) {
        this.line = t.beginLine;
        this.column = t.beginColumn;
        this.image = image;
    }

  static int maxStrKind = 0;
  static int maxLen = 0;
  static int charCnt = 0;
  static Vector charPosKind = new Vector(); // Elements are hashtables
                                            // with single char keys;
  static int[] maxLenForActive = new int[100]; // 6400 tokens
  public static String[] allImages;
  static int[][] intermediateKinds;
  static int[][] intermediateMatchedPos;

  static int startStateCnt = 0;
  static boolean subString[];
  static boolean subStringAtPos[];
  static Hashtable[] statesForPos;

  // Need to call this method after gnerating code for each lexical state. It
  // initializes all the static variables, so that there is no interference
  // between the various states of the lexer.
  public static void ReInit()
  {
     maxStrKind = 0;
     maxLen = 0;
     charPosKind = new Vector();
     maxLenForActive = new int[100]; // 6400 tokens
     intermediateKinds = null;
     intermediateMatchedPos = null;
     startStateCnt = 0;
     subString = null;
     subStringAtPos = null;
     statesForPos = null;
  }

  public static void DumpStrLiteralImages(java.io.PrintWriter ostr)
  {
     String image;
     int charCnt = 0, i;

     ostr.println("public static final String[] jjstrLiteralImages = {");

     if (allImages == null || allImages.length == 0)
     {
        ostr.println("};");
        return;
     }

     allImages[0] = "";
     for (i = 0; i < allImages.length; i++)
     {
        if ((image = allImages[i]) == null ||
            ((LexGen.toSkip[i / 64] & (1L << (i % 64))) == 0L &&
             (LexGen.toMore[i / 64] & (1L << (i % 64))) == 0L &&
             (LexGen.toToken[i / 64] & (1L << (i % 64))) == 0L) ||
            (LexGen.toSkip[i / 64] & (1L << (i % 64))) != 0L ||
            (LexGen.toMore[i / 64] & (1L << (i % 64))) != 0L ||
            LexGen.canReachOnMore[LexGen.lexStates[i]] ||
            ((Options.getIgnoreCase() || LexGen.ignoreCase[i]) &&
             (!image.equals(image.toLowerCase()) ||
              !image.equals(image.toUpperCase()))))
        {
           allImages[i] = null;
           if ((charCnt += 6) > 80)
           {
              ostr.println("");
              charCnt = 0;
           }

           ostr.print("null, ");
           continue;
        }

        String toPrint = "\"";

        for (int j = 0; j < image.length(); j++)
        {
           if (image.charAt(j) <= 0xff)
              toPrint += ("\\" + Integer.toOctalString((int)image.charAt(j)));
           else
           {
              String hexVal = Integer.toHexString((int)image.charAt(j));

              if (hexVal.length() == 3)
                 hexVal = "0" + hexVal;
              toPrint += ("\\u" + hexVal);
           }
        }

        toPrint += ("\", ");

        if ((charCnt += toPrint.length()) >= 80)
        {
           ostr.println("");
           charCnt = 0;
        }

        ostr.print(toPrint);
     }

     while (++i < LexGen.maxOrdinal)
     {
        if ((charCnt += 6) > 80)
        {
           ostr.println("");
           charCnt = 0;
        }

        ostr.print("null, ");
        continue;
     }

     ostr.println("};");
  }

  // Used for top level string literals
  public void GenerateDfa(java.io.PrintWriter ostr, int kind)
  {
     String s;
     Hashtable temp;
     KindInfo info;
     int len;

     if (maxStrKind <= ordinal)
        maxStrKind = ordinal + 1;

     if ((len = image.length()) > maxLen)
        maxLen = len;

     char c;
     for (int i = 0; i < len; i++)
     {
        if (Options.getIgnoreCase())
           s = ("" + (c = image.charAt(i))).toLowerCase();
        else
           s = "" + (c = image.charAt(i));

        if (!NfaState.unicodeWarningGiven && c > 0xff &&
            !Options.getJavaUnicodeEscape() &&
            !Options.getUserCharStream())
        {
           NfaState.unicodeWarningGiven = true;
           JavaCCErrors.warning(LexGen.curRE, "Non-ASCII characters used in regular expression." +
              "Please make sure you use the correct Reader when you create the parser that can handle your character set.");
        }

        if (i >= charPosKind.size()) // Kludge, but OK
           charPosKind.addElement(temp = new Hashtable());
        else
           temp = (Hashtable)charPosKind.elementAt(i);

        if ((info = (KindInfo)temp.get(s)) == null)
           temp.put(s, info = new KindInfo(LexGen.maxOrdinal));

        if (i + 1 == len)
           info.InsertFinalKind(ordinal);
        else
           info.InsertValidKind(ordinal);

        if (!Options.getIgnoreCase() && LexGen.ignoreCase[ordinal] &&
            c != Character.toLowerCase(c))
        {
           s = ("" + image.charAt(i)).toLowerCase();

           if (i >= charPosKind.size()) // Kludge, but OK
              charPosKind.addElement(temp = new Hashtable());
           else
              temp = (Hashtable)charPosKind.elementAt(i);

           if ((info = (KindInfo)temp.get(s)) == null)
              temp.put(s, info = new KindInfo(LexGen.maxOrdinal));

           if (i + 1 == len)
              info.InsertFinalKind(ordinal);
           else
              info.InsertValidKind(ordinal);
        }

        if (!Options.getIgnoreCase() && LexGen.ignoreCase[ordinal] &&
            c != Character.toUpperCase(c))
        {
           s = ("" + image.charAt(i)).toUpperCase();

           if (i >= charPosKind.size()) // Kludge, but OK
              charPosKind.addElement(temp = new Hashtable());
           else
              temp = (Hashtable)charPosKind.elementAt(i);

           if ((info = (KindInfo)temp.get(s)) == null)
              temp.put(s, info = new KindInfo(LexGen.maxOrdinal));

           if (i + 1 == len)
              info.InsertFinalKind(ordinal);
           else
              info.InsertValidKind(ordinal);
        }
     }

     maxLenForActive[ordinal / 64] = Math.max(maxLenForActive[ordinal / 64],
                                                                        len -1);
     allImages[ordinal] = image;
  }

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

  static void DumpNullStrLiterals(java.io.PrintWriter ostr)
  {
     ostr.println("{");

     if (NfaState.generatedStates != 0)
        ostr.println("   return jjMoveNfa" + LexGen.lexStateSuffix + "(" + NfaState.InitStateName() + ", 0);");
     else
        ostr.println("   return 1;");

     ostr.println("}");
  }

  private static int GetStateSetForKind(int pos, int kind)
  {
     if (LexGen.mixed[LexGen.lexStateIndex] || NfaState.generatedStates == 0)
        return -1;

     Hashtable allStateSets = statesForPos[pos];

     if (allStateSets == null)
        return -1;

     Enumeration e = allStateSets.keys();

     while (e.hasMoreElements())
     {
        String s = (String)e.nextElement();
        long[] actives = (long[])allStateSets.get(s);

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

  static String GetLabel(int kind)
  {
     RegularExpression re = LexGen.rexprs[kind];

     if (re instanceof RStringLiteral)
       return " \"" + JavaCCGlobals.add_escapes(((RStringLiteral)re).image) + "\"";
     else if (!re.label.equals(""))
       return " <" + re.label + ">";
     else
       return " <token of kind " + kind + ">";
  }

  static int GetLine(int kind)
  {
     return LexGen.rexprs[kind].line;
  }

  static int GetColumn(int kind)
  {
     return LexGen.rexprs[kind].column;
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
            LexGen.lexStates[i] != LexGen.lexStateIndex)
           continue;

        if (LexGen.mixed[LexGen.lexStateIndex])
        {
           // We will not optimize for mixed case
           subString[i] = true;
           subStringAtPos[image.length() - 1] = true;
           continue;
        }

        for (int j = 0; j < maxStrKind; j++)
        {
           if (j != i && LexGen.lexStates[j] == LexGen.lexStateIndex &&
               ((String)allImages[j]) != null)
           {
              if (((String)allImages[j]).indexOf(image) == 0)
              {
                 subString[i] = true;
                 subStringAtPos[image.length() - 1] = true;
                 break;
              }
              else if (Options.getIgnoreCase() &&
                       StartsWithIgnoreCase((String)allImages[j], image))
              {
                 subString[i] = true;
                 subStringAtPos[image.length() - 1] = true;
                 break;
              }
           }
        }
     }
  }

  static void DumpStartWithStates(java.io.PrintWriter ostr)
  {
     ostr.println((Options.getStatic() ? "static " : "") + "private final int " +
                  "jjStartNfaWithStates" + LexGen.lexStateSuffix + "(int pos, int kind, int state)");
     ostr.println("{");
     ostr.println("   jjmatchedKind = kind;");
     ostr.println("   jjmatchedPos = pos;");

     if (Options.getDebugTokenManager())
     {
        ostr.println("   debugStream.println(\"   No more string literal token matches are possible.\");");
        ostr.println("   debugStream.println(\"   Currently matched the first \" + (jjmatchedPos + 1) + \" characters as a \" + tokenImage[jjmatchedKind] + \" token.\");");
     }

     ostr.println("   try { curChar = input_stream.readChar(); }");
     ostr.println("   catch(java.io.IOException e) { return pos + 1; }");

     if (Options.getDebugTokenManager())
        ostr.println("   debugStream.println(" + (LexGen.maxLexStates > 1 ? "\"<\" + lexStateNames[curLexState] + \">\" + " : "") + "\"Current character : \" + " +
                 "TokenMgrError.addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \") at line \" + input_stream.getLine() + \" column \" + input_stream.getColumn());");

     ostr.println("   return jjMoveNfa" + LexGen.lexStateSuffix + "(state, pos + 1);");
     ostr.println("}");
  }

  private static boolean boilerPlateDumped = false;
  static void DumpBoilerPlate(java.io.PrintWriter ostr)
  {
     ostr.println((Options.getStatic() ? "static " : "") + "private final int " +
                  "jjStopAtPos(int pos, int kind)");
     ostr.println("{");
     ostr.println("   jjmatchedKind = kind;");
     ostr.println("   jjmatchedPos = pos;");

     if (Options.getDebugTokenManager())
     {
        ostr.println("   debugStream.println(\"   No more string literal token matches are possible.\");");
        ostr.println("   debugStream.println(\"   Currently matched the first \" + (jjmatchedPos + 1) + \" characters as a \" + tokenImage[jjmatchedKind] + \" token.\");");
     }

     ostr.println("   return pos + 1;");
     ostr.println("}");
  }

  static String[] ReArrange(Hashtable tab)
  {
     String[] ret = new String[tab.size()];
     Enumeration e = tab.keys();
     int cnt = 0;

     while (e.hasMoreElements())
     {
        int i = 0, j;
        String s;
        char c = (s = (String)e.nextElement()).charAt(0);

        while (i < cnt && ret[i].charAt(0) < c) i++;

        if (i < cnt)
           for (j = cnt - 1; j >= i; j--)
             ret[j + 1] = ret[j];

        ret[i] = s;
        cnt++;
     }

     return ret;
  }

  static void DumpDfaCode(java.io.PrintWriter ostr)
  {
     Hashtable tab;
     String key;
     KindInfo info;
     int maxLongsReqd = maxStrKind / 64 + 1;
     int i, j, k;
     boolean ifGenerated;
     LexGen.maxLongsReqd[LexGen.lexStateIndex] = maxLongsReqd;

     if (maxLen == 0)
     {
        ostr.println((Options.getStatic() ? "static " : "") + "private final int " +
                       "jjMoveStringLiteralDfa0" + LexGen.lexStateSuffix + "()");

        DumpNullStrLiterals(ostr);
        return;
     }

     if (!boilerPlateDumped)
     {
        DumpBoilerPlate(ostr);
        boilerPlateDumped = true;
     }

     if (!LexGen.mixed[LexGen.lexStateIndex] && NfaState.generatedStates != 0)
        DumpStartWithStates(ostr);

     boolean startNfaNeeded;
     for (i = 0; i < maxLen; i++)
     {
        boolean atLeastOne = false;
        startNfaNeeded = false;
        tab = (Hashtable)charPosKind.elementAt(i);
        String[] keys = ReArrange(tab);

        ostr.print((Options.getStatic() ? "static " : "") + "private final int " +
                       "jjMoveStringLiteralDfa" + i + LexGen.lexStateSuffix + "(");

        if (i != 0)
        {
           if (i == 1)
           {
              for (j = 0; j < maxLongsReqd - 1; j++)
                 if (i <= maxLenForActive[j])
                 {
                    if (atLeastOne)
                       ostr.print(", ");
                    else
                       atLeastOne = true;
                    ostr.print("long active" + j);
                 }

              if (i <= maxLenForActive[j])
              {
                 if (atLeastOne)
                    ostr.print(", ");
                 ostr.print("long active" + j);
              }
           }
           else
           {
              for (j = 0; j < maxLongsReqd - 1; j++)
                 if (i <= maxLenForActive[j] + 1)
                 {
                    if (atLeastOne)
                       ostr.print(", ");
                    else
                       atLeastOne = true;
                    ostr.print("long old" + j + ", long active" + j);
                 }

              if (i <= maxLenForActive[j] + 1)
              {
                 if (atLeastOne)
                    ostr.print(", ");
                 ostr.print("long old" + j + ", long active" + j);
              }
           }
        }
        ostr.println(")");
        ostr.println("{");

        if (i != 0)
        {
           if (i > 1)
           {
              atLeastOne = false;
              ostr.print("   if ((");

              for (j = 0; j < maxLongsReqd - 1; j++)
                 if (i <= maxLenForActive[j] + 1)
                 {
                    if (atLeastOne)
                       ostr.print(" | ");
                    else
                       atLeastOne = true;
                    ostr.print("(active" + j + " &= old" + j + ")");
                 }

              if (i <= maxLenForActive[j] + 1)
              {
                 if (atLeastOne)
                    ostr.print(" | ");
                 ostr.print("(active" + j + " &= old" + j + ")");
              }

              ostr.println(") == 0L)");
              if (!LexGen.mixed[LexGen.lexStateIndex] && NfaState.generatedStates != 0)
              {
                 ostr.print("      return jjStartNfa" + LexGen.lexStateSuffix +
                                 "(" + (i - 2) + ", ");
                 for (j = 0; j < maxLongsReqd - 1; j++)
                    if (i <= maxLenForActive[j] + 1)
                       ostr.print("old" + j + ", ");
                    else
                       ostr.print("0L, ");
                 if (i <= maxLenForActive[j] + 1)
                    ostr.println("old" + j + "); ");
                 else
                    ostr.println("0L);");
              }
              else if (NfaState.generatedStates != 0)
                 ostr.println("      return jjMoveNfa" + LexGen.lexStateSuffix + "(" + NfaState.InitStateName() + ", " + (i - 1) + ");");
              else
                 ostr.println("      return " + i + ";");
           }

           if (i != 0 && Options.getDebugTokenManager())
           {
              ostr.println("   if (jjmatchedKind != 0 && jjmatchedKind != 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
              ostr.println("      debugStream.println(\"   Currently matched the first \" + (jjmatchedPos + 1) + \" characters as a \" + tokenImage[jjmatchedKind] + \" token.\");");

              ostr.println("   debugStream.println(\"   Possible string literal matches : { \"");

              for (int vecs = 0; vecs < maxStrKind / 64 + 1; vecs++)
              {
                 if (i <= maxLenForActive[vecs])
                 {
                    ostr.println(" + ");
                    ostr.print("         jjKindsForBitVector(" + vecs + ", ");
                    ostr.print("active" + vecs + ") ");
                 }
              }

              ostr.println(" + \" } \");");
           }

           ostr.println("   try { curChar = input_stream.readChar(); }");
           ostr.println("   catch(java.io.IOException e) {");

           if (!LexGen.mixed[LexGen.lexStateIndex] && NfaState.generatedStates != 0)
           {
              ostr.print("      jjStopStringLiteralDfa" + LexGen.lexStateSuffix + "(" + (i - 1) + ", ");
              for (k = 0; k < maxLongsReqd - 1; k++)
                 if (i <= maxLenForActive[k])
                    ostr.print("active" + k + ", ");
                 else
                    ostr.print("0L, ");

              if (i <= maxLenForActive[k])
                 ostr.println("active" + k + ");");
              else
                 ostr.println("0L);");


              if (i != 0 && Options.getDebugTokenManager())
              {
                 ostr.println("      if (jjmatchedKind != 0 && jjmatchedKind != 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
                 ostr.println("         debugStream.println(\"   Currently matched the first \" + (jjmatchedPos + 1) + \" characters as a \" + tokenImage[jjmatchedKind] + \" token.\");");
              }
              ostr.println("      return " + i + ";");
           }
           else if (NfaState.generatedStates != 0)
              ostr.println("   return jjMoveNfa" + LexGen.lexStateSuffix + "(" + NfaState.InitStateName() + ", " + (i - 1) + ");");
           else
              ostr.println("      return " + i + ";");

           ostr.println("   }");
        }

        if (i != 0 && Options.getDebugTokenManager())
           ostr.println("   debugStream.println(" + (LexGen.maxLexStates > 1 ? "\"<\" + lexStateNames[curLexState] + \">\" + " : "") + "\"Current character : \" + " +
                 "TokenMgrError.addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \") at line \" + input_stream.getLine() + \" column \" + input_stream.getColumn());");

        ostr.println("   switch(curChar)");
        ostr.println("   {");

        CaseLoop:
        for (int q = 0; q < keys.length; q++)
        {
           key = keys[q];
           info = (KindInfo)tab.get(key);
           ifGenerated = false;
           char c = key.charAt(0);

           if (i == 0 && c < 128 && info.finalKindCnt != 0 &&
               (NfaState.generatedStates == 0 || !NfaState.CanStartNfaUsingAscii(c)))
           {
              int kind;
              for (j = 0; j < maxLongsReqd; j++)
                 if (info.finalKinds[j] != 0L)
                    break;

              for (k = 0; k < 64; k++)
                 if ((info.finalKinds[j] & (1L << k)) != 0L &&
                     !subString[kind = (j * 64 + k)])
                 {
                   if ((intermediateKinds != null &&
                        intermediateKinds[(j * 64 + k)] != null &&
                        intermediateKinds[(j * 64 + k)][i] < (j * 64 + k) &&
                        intermediateMatchedPos != null &&
                        intermediateMatchedPos[(j * 64 + k)][i] == i) ||
                       (LexGen.canMatchAnyChar[LexGen.lexStateIndex] >= 0 &&
                        LexGen.canMatchAnyChar[LexGen.lexStateIndex] < (j * 64 + k)))
                      break;
                    else if ((LexGen.toSkip[kind / 64] & (1L << (kind % 64))) != 0L  &&
                             (LexGen.toSpecial[kind / 64] & (1L << (kind % 64))) == 0L  &&
                             LexGen.actions[kind] == null &&
                             LexGen.newLexState[kind] == null)
                    {
                       LexGen.AddCharToSkip(c, kind);

                       if (Options.getIgnoreCase())
                       {
                          if (c != Character.toUpperCase(c))
                             LexGen.AddCharToSkip(Character.toUpperCase(c), kind);

                          if (c != Character.toLowerCase(c))
                             LexGen.AddCharToSkip(Character.toLowerCase(c), kind);
                       }
                       continue CaseLoop;
                    }
                 }
           }

           // Since we know key is a single character ...
           if (Options.getIgnoreCase())
           {
              if (c != Character.toUpperCase(c))
                 ostr.println("      case " + (int)Character.toUpperCase(c) + ":");

              if (c != Character.toLowerCase(c))
                 ostr.println("      case " + (int)Character.toLowerCase(c) + ":");
           }

           ostr.println("      case " + (int)c + ":");

           long matchedKind;
           String prefix = (i == 0) ? "         " : "            ";

           if (info.finalKindCnt != 0)
           {
              for (j = 0; j < maxLongsReqd; j++)
              {
                 if ((matchedKind = info.finalKinds[j]) == 0L)
                    continue;

                 for (k = 0; k < 64; k++)
                 {
                    if ((matchedKind & (1L << k)) == 0L)
                       continue;

                    if (ifGenerated)
                    {
                       ostr.print("         else if ");
                    }
                    else if (i != 0)
                       ostr.print("         if ");

                    ifGenerated = true;

                    int kindToPrint;
                    if (i != 0)
                    {
                       ostr.println("((active" + j +
                          " & 0x" + Long.toHexString(1L << k) + "L) != 0L)");
                    }

                    if (intermediateKinds != null &&
                        intermediateKinds[(j * 64 + k)] != null &&
                        intermediateKinds[(j * 64 + k)][i] < (j * 64 + k) &&
                        intermediateMatchedPos != null &&
                        intermediateMatchedPos[(j * 64 + k)][i] == i)
                    {
                       JavaCCErrors.warning(" \"" +
                           JavaCCGlobals.add_escapes(allImages[j * 64 + k]) +
                           "\" cannot be matched as a string literal token " +
                           "at line " + GetLine(j * 64 + k) + ", column " + GetColumn(j * 64 + k) +
                           ". It will be matched as " +
                           GetLabel(intermediateKinds[(j * 64 + k)][i]) + ".");
                       kindToPrint = intermediateKinds[(j * 64 + k)][i];
                    }
                    else if (i == 0 &&
                         LexGen.canMatchAnyChar[LexGen.lexStateIndex] >= 0 &&
                         LexGen.canMatchAnyChar[LexGen.lexStateIndex] < (j * 64 + k))
                    {
                       JavaCCErrors.warning(" \"" +
                           JavaCCGlobals.add_escapes(allImages[j * 64 + k]) +
                           "\" cannot be matched as a string literal token " +
                           "at line " + GetLine(j * 64 + k) + ", column " + GetColumn(j * 64 + k) +
                           ". It will be matched as " +
                           GetLabel(LexGen.canMatchAnyChar[LexGen.lexStateIndex]) + ".");
                       kindToPrint = LexGen.canMatchAnyChar[LexGen.lexStateIndex];
                    }
                    else
                       kindToPrint = j * 64 + k;

                    if (!subString[(j * 64 + k)])
                    {
                       int stateSetName = GetStateSetForKind(i, j * 64 + k);

                       if (stateSetName != -1)
                       {
                          ostr.println(prefix + "return jjStartNfaWithStates" +
                              LexGen.lexStateSuffix + "(" + i +
                              ", " + kindToPrint + ", " + stateSetName + ");");
                       }
                       else
                          ostr.println(prefix + "return jjStopAtPos" + "(" + i + ", " + kindToPrint + ");");
                    }
                    else
                    {
                       if ((LexGen.initMatch[LexGen.lexStateIndex] != 0 &&
                            LexGen.initMatch[LexGen.lexStateIndex] != Integer.MAX_VALUE) ||
                            i != 0)
                       {
                          ostr.println("         {");
                          ostr.println(prefix + "jjmatchedKind = " +
                                                     kindToPrint + ";");
                          ostr.println(prefix + "jjmatchedPos = " + i + ";");
                          ostr.println("         }");
                       }
                       else
                          ostr.println(prefix + "jjmatchedKind = " +
                                                     kindToPrint + ";");
                    }
                 }
              }
           }

           if (info.validKindCnt != 0)
           {
              atLeastOne = false;

              if (i == 0)
              {
                 ostr.print("         return ");

                 ostr.print("jjMoveStringLiteralDfa" + (i + 1) +
                                LexGen.lexStateSuffix + "(");
                 for (j = 0; j < maxLongsReqd - 1; j++)
                    if ((i + 1) <= maxLenForActive[j])
                    {
                       if (atLeastOne)
                          ostr.print(", ");
                       else
                          atLeastOne = true;

                       ostr.print("0x" + Long.toHexString(info.validKinds[j]) + "L");
                    }

                 if ((i + 1) <= maxLenForActive[j])
                 {
                    if (atLeastOne)
                       ostr.print(", ");

                    ostr.print("0x" + Long.toHexString(info.validKinds[j]) + "L");
                 }
                 ostr.println(");");
              }
              else
              {
                 ostr.print("         return ");

                 ostr.print("jjMoveStringLiteralDfa" + (i + 1) +
                                LexGen.lexStateSuffix + "(");

                 for (j = 0; j < maxLongsReqd - 1; j++)
                    if ((i + 1) <= maxLenForActive[j] + 1)
                    {
                       if (atLeastOne)
                          ostr.print(", ");
                       else
                          atLeastOne = true;

                       if (info.validKinds[j] != 0L)
                          ostr.print("active" + j + ", 0x" +
                                  Long.toHexString(info.validKinds[j]) + "L");
                       else
                          ostr.print("active" + j + ", 0L");
                    }

                 if ((i + 1) <= maxLenForActive[j] + 1)
                 {
                    if (atLeastOne)
                       ostr.print(", ");
                    if (info.validKinds[j] != 0L)
                       ostr.print("active" + j + ", 0x" +
                                  Long.toHexString(info.validKinds[j]) + "L");
                    else
                       ostr.print("active" + j + ", 0L");
                 }

                 ostr.println(");");
              }
           }
           else
           {
              // A very special case.
              if (i == 0 && LexGen.mixed[LexGen.lexStateIndex])
              {

                 if (NfaState.generatedStates != 0)
                    ostr.println("         return jjMoveNfa" + LexGen.lexStateSuffix + "(" + NfaState.InitStateName() + ", 0);");
                 else
                    ostr.println("         return 1;");
              }
              else if (i != 0) // No more str literals to look for
              {
                 ostr.println("         break;");
                 startNfaNeeded = true;
              }
           }
        }

        /* default means that the current character is not in any of the
           strings at this position. */
        ostr.println("      default :");

        if (Options.getDebugTokenManager())
           ostr.println("      debugStream.println(\"   No string literal matches possible.\");");

        if (NfaState.generatedStates != 0)
        {
           if (i == 0)
           {
              /* This means no string literal is possible. Just move nfa with
                 this guy and return. */
              ostr.println("         return jjMoveNfa" + LexGen.lexStateSuffix + "(" + NfaState.InitStateName() + ", 0);");
           }
           else
           {
              ostr.println("         break;");
              startNfaNeeded = true;
           }
        }
        else
        {
           ostr.println("         return " + (i + 1) + ";");
        }


        ostr.println("   }");

        if (i != 0)
        {
          if (startNfaNeeded)
          {
           if (!LexGen.mixed[LexGen.lexStateIndex] && NfaState.generatedStates != 0)
           {
              /* Here, a string literal is successfully matched and no more
                 string literals are possible. So set the kind and state set
                 upto and including this position for the matched string. */

              ostr.print("   return jjStartNfa" + LexGen.lexStateSuffix + "(" + (i - 1) + ", ");
              for (k = 0; k < maxLongsReqd - 1; k++)
                 if (i <= maxLenForActive[k])
                    ostr.print("active" + k + ", ");
                 else
                    ostr.print("0L, ");
              if (i <= maxLenForActive[k])
                 ostr.println("active" + k + ");");
              else
                 ostr.println("0L);");
           }
           else if (NfaState.generatedStates != 0)
              ostr.println("   return jjMoveNfa" + LexGen.lexStateSuffix + "(" + NfaState.InitStateName() + ", " + i + ");");
           else
              ostr.println("   return " + (i + 1) + ";");
          }
        }

        ostr.println("}");
     }
  }

  static final int GetStrKind(String str)
  {
     for (int i = 0; i < maxStrKind; i++)
     {
        if (LexGen.lexStates[i] != LexGen.lexStateIndex)
           continue;

        String image = allImages[i];
        if (image != null && image.equals(str))
           return i;
     }

     return Integer.MAX_VALUE;
  }

  static void GenerateNfaStartStates(java.io.PrintWriter ostr,
                                                NfaState initialState)
  {
     boolean[] seen = new boolean[NfaState.generatedStates];
     Hashtable stateSets = new Hashtable();
     String stateSetString  = "";
     int i, j, kind, jjmatchedPos = 0;
     int maxKindsReqd = maxStrKind / 64 + 1;
     long[] actives;
     Vector newStates = new Vector();
     Vector oldStates = null, jjtmpStates;

     statesForPos = new Hashtable[maxLen];
     intermediateKinds = new int[maxStrKind + 1][];
     intermediateMatchedPos = new int[maxStrKind + 1][];

     for (i = 0; i < maxStrKind; i++)
     {
        if (LexGen.lexStates[i] != LexGen.lexStateIndex)
           continue;

        String image = allImages[i];

        if (image == null || image.length() < 1)
           continue;

        try
        {
           if ((oldStates = (Vector)initialState.epsilonMoves.clone()) == null ||
               oldStates.size() == 0)
           {
              DumpNfaStartStatesCode(statesForPos, ostr);
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
              oldStates.removeAllElements();

              if (j == 0 && kind != Integer.MAX_VALUE &&
                  LexGen.canMatchAnyChar[LexGen.lexStateIndex] != -1 &&
                  kind > LexGen.canMatchAnyChar[LexGen.lexStateIndex])
                 kind = LexGen.canMatchAnyChar[LexGen.lexStateIndex];

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
                 if (seen[((NfaState)newStates.elementAt(p)).stateName])
                    ((NfaState)newStates.elementAt(p)).inNextOf++;
                 else
                    seen[((NfaState)newStates.elementAt(p)).stateName] = true;
              }
           }
           else
           {
              for (p = 0; p < newStates.size(); p++)
                 seen[((NfaState)newStates.elementAt(p)).stateName] = true;
           }

           jjtmpStates = oldStates;
           oldStates = newStates;
           (newStates = jjtmpStates).removeAllElements();

           if (statesForPos[j] == null)
              statesForPos[j] = new Hashtable();

           if ((actives = ((long[])statesForPos[j].get(kind + ", " +
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

     DumpNfaStartStatesCode(statesForPos, ostr);
  }

  static void DumpNfaStartStatesCode(Hashtable[] statesForPos,
                                              java.io.PrintWriter ostr)
  {
      if (maxStrKind == 0) { // No need to generate this function
         return;
      }

     int i, maxKindsReqd = maxStrKind / 64 + 1;
     boolean condGenerated = false;
     int ind = 0;

     ostr.print("private" + (Options.getStatic() ? " static" : "") + " final int jjStopStringLiteralDfa" +
                  LexGen.lexStateSuffix + "(int pos, ");
     for (i = 0; i < maxKindsReqd - 1; i++)
        ostr.print("long active" + i + ", ");
     ostr.println("long active" + i + ")\n{");

     if (Options.getDebugTokenManager())
        ostr.println("      debugStream.println(\"   No more string literal token matches are possible.\");");

     ostr.println("   switch (pos)\n   {");

     for (i = 0; i < maxLen - 1; i++)
     {
        if (statesForPos[i] == null)
           continue;

        ostr.println("      case " + i + ":");

        Enumeration e = statesForPos[i].keys();
        while (e.hasMoreElements())
        {
           String stateSetString = (String)e.nextElement();
           long[] actives = (long[])statesForPos[i].get(stateSetString);

           for (int j = 0; j < maxKindsReqd; j++)
           {
              if (actives[j] == 0L)
                 continue;

              if (condGenerated)
                 ostr.print(" || ");
              else
                 ostr.print("         if (");

              condGenerated = true;

              ostr.print("(active" + j + " & 0x" +
                          Long.toHexString(actives[j]) + "L) != 0L");
           }

           if (condGenerated)
           {
              ostr.println(")");

              String kindStr = stateSetString.substring(0,
                                       ind = stateSetString.indexOf(", "));
              String afterKind = stateSetString.substring(ind + 2);
              int jjmatchedPos = Integer.parseInt(
                           afterKind.substring(0, afterKind.indexOf(", ")));

              if (!kindStr.equals(String.valueOf(Integer.MAX_VALUE)))
                 ostr.println("         {");

              if (!kindStr.equals(String.valueOf(Integer.MAX_VALUE)))
              {
                 if (i == 0)
                 {
                    ostr.println("            jjmatchedKind = " + kindStr + ";");

                    if ((LexGen.initMatch[LexGen.lexStateIndex] != 0 &&
                        LexGen.initMatch[LexGen.lexStateIndex] != Integer.MAX_VALUE))
                       ostr.println("            jjmatchedPos = 0;");
                 }
                 else if (i == jjmatchedPos)
                 {
                    if (subStringAtPos[i])
                    {
                       ostr.println("            if (jjmatchedPos != " + i + ")");
                       ostr.println("            {");
                       ostr.println("               jjmatchedKind = " + kindStr + ";");
                       ostr.println("               jjmatchedPos = " + i + ";");
                       ostr.println("            }");
                    }
                    else
                    {
                       ostr.println("            jjmatchedKind = " + kindStr + ";");
                       ostr.println("            jjmatchedPos = " + i + ";");
                    }
                 }
                 else
                 {
                    if (jjmatchedPos > 0)
                       ostr.println("            if (jjmatchedPos < " + jjmatchedPos + ")");
                    else
                       ostr.println("            if (jjmatchedPos == 0)");
                    ostr.println("            {");
                    ostr.println("               jjmatchedKind = " + kindStr + ";");
                    ostr.println("               jjmatchedPos = " + jjmatchedPos + ";");
                    ostr.println("            }");
                 }
              }

              kindStr = stateSetString.substring(0,
                                    ind = stateSetString.indexOf(", "));
              afterKind = stateSetString.substring(ind + 2);
              stateSetString = afterKind.substring(
                                       afterKind.indexOf(", ") + 2);

              if (stateSetString.equals("null;"))
                 ostr.println("            return -1;");
              else
                 ostr.println("            return " +
                    NfaState.AddStartStateSet(stateSetString) + ";");

              if (!kindStr.equals(String.valueOf(Integer.MAX_VALUE)))
                 ostr.println("         }");
              condGenerated = false;
           }
        }

        ostr.println("         return -1;");
     }

     ostr.println("      default :");
     ostr.println("         return -1;");
     ostr.println("   }");
     ostr.println("}");

     ostr.print("private" + (Options.getStatic() ? " static" : "") + " final int jjStartNfa" +
                  LexGen.lexStateSuffix + "(int pos, ");
     for (i = 0; i < maxKindsReqd - 1; i++)
        ostr.print("long active" + i + ", ");
     ostr.println("long active" + i + ")\n{");

     if (LexGen.mixed[LexGen.lexStateIndex])
     {
         if (NfaState.generatedStates != 0)
            ostr.println("   return jjMoveNfa" + LexGen.lexStateSuffix + "(" + NfaState.InitStateName() + ", pos + 1);");
         else
            ostr.println("   return pos + 1;");

         ostr.println("}");
         return;
     }

     ostr.print("   return jjMoveNfa" + LexGen.lexStateSuffix + "(" +
               "jjStopStringLiteralDfa" + LexGen.lexStateSuffix + "(pos, ");
     for (i = 0; i < maxKindsReqd - 1; i++)
        ostr.print("active" + i + ", ");
     ostr.print("active" + i + ")");
     ostr.println(", pos + 1);");
     ostr.println("}");
  }

   public static void reInit()
   {
      maxStrKind = 0;
      maxLen = 0;
      charCnt = 0;
      charPosKind = new Vector();
      maxLenForActive = new int[100];
      allImages = null;
      intermediateKinds = null;
      intermediateMatchedPos = null;
      startStateCnt = 0;
      subString = null;
      subStringAtPos = null;
      statesForPos = null;
      boilerPlateDumped = false;
   }

}
