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

public class NfaState
{
   public static boolean unicodeWarningGiven = false;
   public static int generatedStates = 0;
   static int idCnt = 0;
   static int lohiByteCnt;
   static int dummyStateIndex = -1;
   static boolean done;
   static boolean mark[];
   static boolean stateDone[];
   static boolean nonAsciiIntersections[][] = new boolean[20][20];

   static Vector allStates = new Vector();
   static Vector indexedAllStates = new Vector();
   static Vector nonAsciiTableForMethod = new Vector();
   static Hashtable equivStatesTable = new Hashtable();
   static Hashtable allNextStates = new Hashtable();
   static Hashtable lohiByteTab = new Hashtable();
   static Hashtable stateNameForComposite = new Hashtable();
   static Hashtable compositeStateTable = new Hashtable();
   static Hashtable stateBlockTable = new Hashtable();
   static Hashtable stateSetsToFix = new Hashtable();

   public static void ReInit()
   {
      generatedStates = 0;
      idCnt = 0;
      dummyStateIndex = -1;
      done = false;
      mark = null;
      stateDone = null;

      allStates.removeAllElements();
      indexedAllStates.removeAllElements();
      equivStatesTable.clear();
      allNextStates.clear();
      compositeStateTable.clear();
      stateBlockTable.clear();
      stateNameForComposite.clear();
      stateSetsToFix.clear();
   }

   long[] asciiMoves = new long[2];
   char[] charMoves = null;
   char[] rangeMoves = null;
   NfaState next = null;
   NfaState stateForCase;
   Vector epsilonMoves = new Vector();
   String epsilonMovesString;
   NfaState[] epsilonMoveArray;

   int id;
   int stateName = -1;
   int kind = Integer.MAX_VALUE;
   int lookingFor;
   int usefulEpsilonMoves = 0;
   int inNextOf;
   private int lexState;
   int nonAsciiMethod = -1;
   int kindToPrint = Integer.MAX_VALUE;
   boolean dummy = false;
   boolean isComposite = false;
   int[] compositeStates = null;
   boolean isFinal = false;
   public Vector loByteVec;
   public int[] nonAsciiMoveIndices;
   int round = 0;
   int onlyChar = 0;
   char matchSingleChar;

   NfaState()
   {
      id = idCnt++;
      allStates.addElement(this);
      lexState = LexGen.lexStateIndex;
      lookingFor = LexGen.curKind;
   }

   NfaState CreateClone()
   {
      NfaState retVal = new NfaState();

      retVal.isFinal = isFinal;
      retVal.kind = kind;
      retVal.lookingFor = lookingFor;
      retVal.lexState = lexState;
      retVal.inNextOf = inNextOf;

      retVal.MergeMoves(this);

      return retVal;
   }

   static void InsertInOrder(Vector v, NfaState s)
   {
      int j;

      for (j = 0; j < v.size(); j++)
         if (((NfaState)v.elementAt(j)).id > s.id)
            break;
         else if (((NfaState)v.elementAt(j)).id  == s.id)
            return;

      v.insertElementAt(s, j);
   }

   private static char[] ExpandCharArr(char[] oldArr, int incr)
   {
      char[] ret = new char[oldArr.length + incr];
      System.arraycopy(oldArr, 0, ret, 0, oldArr.length);
      return ret;
   }

   void AddMove(NfaState newState)
   {
      if (!epsilonMoves.contains(newState))
         InsertInOrder(epsilonMoves, newState);
   }

   private final void AddASCIIMove(char c)
   {
      asciiMoves[c / 64] |= (1L << (c % 64));
   }

   void AddChar(char c)
   {
      onlyChar++;
      matchSingleChar = c;
      int i;
      char temp;
      char temp1;

      if ((int)c < 128) // ASCII char
      {
         AddASCIIMove(c);
         return;
      }

      if (charMoves == null)
         charMoves = new char[10];

      int len = charMoves.length;

      if (charMoves[len - 1] != 0)
      {
         charMoves = ExpandCharArr(charMoves, 10);
         len += 10;
      }

      for (i = 0; i < len; i++)
         if (charMoves[i] == 0 || charMoves[i] > c)
            break;

      if (!unicodeWarningGiven && c > 0xff &&
          !Options.getJavaUnicodeEscape() &&
          !Options.getUserCharStream())
      {
         unicodeWarningGiven = true;
         JavaCCErrors.warning(LexGen.curRE, "Non-ASCII characters used in regular expression.\n" +
              "Please make sure you use the correct Reader when you create the parser that can handle your character set.");
      }

      temp = charMoves[i];
      charMoves[i] = c;

      for (i++; i < len; i++)
      {
         if (temp == 0)
            break;

         temp1 = charMoves[i];
         charMoves[i] = temp;
         temp = temp1;
      }
   }

   void AddRange(char left, char right)
   {
      onlyChar = 2;
      int i;
      char tempLeft1, tempLeft2, tempRight1, tempRight2;

      if (left < 128)
      {
         if (right < 128)
         {
            for (; left <= right; left++)
               AddASCIIMove(left);

            return;
         }

         for (; left < 128; left++)
            AddASCIIMove(left);
      }

      if (!unicodeWarningGiven && (left > 0xff || right > 0xff) &&
          !Options.getJavaUnicodeEscape() &&
          !Options.getUserCharStream())
      {
         unicodeWarningGiven = true;
         JavaCCErrors.warning(LexGen.curRE, "Non-ASCII characters used in regular expression.\n" +
              "Please make sure you use the correct Reader when you create the parser that can handle your character set.");
      }

      if (rangeMoves == null)
         rangeMoves = new char[20];

      int len = rangeMoves.length;

      if (rangeMoves[len - 1] != 0)
      {
         rangeMoves = ExpandCharArr(rangeMoves, 20);
         len += 20;
      }

      for (i = 0; i < len; i += 2)
         if (rangeMoves[i] == 0 ||
             (rangeMoves[i] > left) ||
             ((rangeMoves[i] == left) && (rangeMoves[i + 1] > right)))
            break;

      tempLeft1 = rangeMoves[i];
      tempRight1 = rangeMoves[i + 1];
      rangeMoves[i] = left;
      rangeMoves[i + 1] = right;

      for (i += 2; i < len; i += 2)
      {
         if (tempLeft1 == 0)
            break;

         tempLeft2 = rangeMoves[i];
         tempRight2 = rangeMoves[i + 1];
         rangeMoves[i] = tempLeft1;
         rangeMoves[i + 1] = tempRight1;
         tempLeft1 = tempLeft2;
         tempRight1 = tempRight2;
      }
   }

   // From hereon down all the functions are used for code generation

   private static boolean EqualCharArr(char[] arr1, char[] arr2)
   {
      if (arr1 == arr2)
         return true;

      if (arr1 != null &&
          arr2 != null &&
          arr1.length == arr2.length)
      {
         for (int i = arr1.length; i-- > 0;)
            if (arr1[i] != arr2[i])
               return false;

         return true;
      }

      return false;
   }

   private boolean closureDone = false;

   /** This function computes the closure and also updates the kind so that
     * any time there is a move to this state, it can go on epsilon to a
     * new state in the epsilon moves that might have a lower kind of token
     * number for the same length.
   */

   private void EpsilonClosure()
   {
      int i = 0;

      if (closureDone || mark[id])
         return;

      mark[id] = true;

      // Recursively do closure
      for (i = 0; i < epsilonMoves.size(); i++)
         ((NfaState)epsilonMoves.elementAt(i)).EpsilonClosure();

      Enumeration e = epsilonMoves.elements();

      while (e.hasMoreElements())
      {
         NfaState tmp = (NfaState)e.nextElement();

         for (i = 0; i < tmp.epsilonMoves.size(); i++)
         {
            NfaState tmp1 = (NfaState)tmp.epsilonMoves.elementAt(i);
            if (tmp1.UsefulState() && !epsilonMoves.contains(tmp1))
            {
               InsertInOrder(epsilonMoves, tmp1);
               done = false;
            }
         }

         if (kind > tmp.kind)
            kind = tmp.kind;
      }

      if (HasTransitions() && !epsilonMoves.contains(this))
         InsertInOrder(epsilonMoves, this);
   }

   private boolean UsefulState()
   {
      return isFinal || HasTransitions();
   }

   public boolean HasTransitions()
   {
      return (asciiMoves[0] != 0L || asciiMoves[1] != 0L ||
              (charMoves != null && charMoves[0] != 0) ||
              (rangeMoves != null && rangeMoves[0] != 0));
   }

   void MergeMoves(NfaState other)
   {
      // Warning : This function does not merge epsilon moves
      if (asciiMoves == other.asciiMoves)
      {
         JavaCCErrors.semantic_error("Bug in JavaCC : Please send " +
                   "a report along with the input that caused this. Thank you.");
         throw new Error();
      }

      asciiMoves[0] = asciiMoves[0] | other.asciiMoves[0];
      asciiMoves[1] = asciiMoves[1] | other.asciiMoves[1];

      if (other.charMoves != null)
      {
         if (charMoves == null)
            charMoves = other.charMoves;
         else
         {
            char[] tmpCharMoves = new char[charMoves.length +
                                              other.charMoves.length];
            System.arraycopy(charMoves, 0, tmpCharMoves, 0, charMoves.length);
            charMoves = tmpCharMoves;

            for (int i = 0; i < other.charMoves.length; i++)
               AddChar(other.charMoves[i]);
         }
      }

      if (other.rangeMoves != null)
      {
         if (rangeMoves == null)
            rangeMoves = other.rangeMoves;
         else
         {
            char[] tmpRangeMoves = new char[rangeMoves.length +
                                                     other.rangeMoves.length];
            System.arraycopy(rangeMoves, 0, tmpRangeMoves,
                                                        0, rangeMoves.length);
            rangeMoves = tmpRangeMoves;
            for (int i = 0; i < other.rangeMoves.length; i += 2)
               AddRange(other.rangeMoves[i], other.rangeMoves[i + 1]);
         }
      }

      if (other.kind < kind)
         kind = other.kind;

      if (other.kindToPrint < kindToPrint)
         kindToPrint = other.kindToPrint;

      isFinal |= other.isFinal;
   }

   NfaState CreateEquivState(Vector states)
   {
      NfaState newState = ((NfaState)states.elementAt(0)).CreateClone();

      newState.next = new NfaState();

      InsertInOrder(newState.next.epsilonMoves,
                           ((NfaState)states.elementAt(0)).next);

      for (int i = 1; i < states.size(); i++)
      {
         NfaState tmp2 = ((NfaState)states.elementAt(i));

         if (tmp2.kind < newState.kind)
            newState.kind = tmp2.kind;

         newState.isFinal |= tmp2.isFinal;

         InsertInOrder(newState.next.epsilonMoves, tmp2.next);
      }

      return newState;
   }

   private NfaState GetEquivalentRunTimeState()
   {
      Outer :
      for (int i = allStates.size(); i-- > 0;)
      {
         NfaState other = (NfaState)allStates.elementAt(i);

         if (this != other && other.stateName != -1 &&
             kindToPrint == other.kindToPrint &&
             asciiMoves[0] == other.asciiMoves[0] &&
             asciiMoves[1] == other.asciiMoves[1] &&
             EqualCharArr(charMoves, other.charMoves) &&
             EqualCharArr(rangeMoves, other.rangeMoves))
         {
            if (next == other.next)
               return other;
            else if (next != null && other.next != null)
            {
               if (next.epsilonMoves.size() == other.next.epsilonMoves.size())
               {
                  for (int j = 0; j < next.epsilonMoves.size(); j++)
                     if (next.epsilonMoves.elementAt(j) !=
                           other.next.epsilonMoves.elementAt(j))
                        continue Outer;

                  return other;
               }
            }
         }
      }

      return null;
   }

   // generates code (without outputting it) and returns the name used.
   void GenerateCode()
   {
      if (stateName != -1)
         return;

      if (next != null)
      {
         next.GenerateCode();
         if (next.kind != Integer.MAX_VALUE)
            kindToPrint = next.kind;
      }

      if (stateName == -1 && HasTransitions())
      {
         NfaState tmp = GetEquivalentRunTimeState();

         if (tmp != null)
         {
            stateName = tmp.stateName;
//????
            //tmp.inNextOf += inNextOf;
//????
            dummy = true;
            return;
         }

         stateName = generatedStates++;
         indexedAllStates.addElement(this);
         GenerateNextStatesCode();
      }
   }

   public static void ComputeClosures()
   {
      for (int i = allStates.size(); i-- > 0; )
      {
         NfaState tmp = (NfaState)allStates.elementAt(i);

         if (!tmp.closureDone)
            tmp.OptimizeEpsilonMoves(true);
      }

      for (int i = 0; i < allStates.size(); i++)
      {
         NfaState tmp = (NfaState)allStates.elementAt(i);

         if (!tmp.closureDone)
            tmp.OptimizeEpsilonMoves(false);
      }

      for (int i = 0; i < allStates.size(); i++)
      {
         NfaState tmp = (NfaState)allStates.elementAt(i);
         tmp.epsilonMoveArray = new NfaState[tmp.epsilonMoves.size()];
         tmp.epsilonMoves.copyInto(tmp.epsilonMoveArray);
      }
   }

   void OptimizeEpsilonMoves(boolean optReqd)
   {
      int i;

      // First do epsilon closure
      done = false;
      while (!done)
      {
         if (mark == null || mark.length < allStates.size())
            mark = new boolean[allStates.size()];

         for (i = allStates.size(); i-- > 0;)
            mark[i] = false;

         done = true;
         EpsilonClosure();
      }

      for (i = allStates.size(); i-- > 0;)
         ((NfaState)allStates.elementAt(i)).closureDone =
                                  mark[((NfaState)allStates.elementAt(i)).id];

      // Warning : The following piece of code is just an optimization.
      // in case of trouble, just remove this piece.

      boolean sometingOptimized = true;

      NfaState newState = null;
      NfaState tmp1, tmp2;
      int j;
      Vector equivStates = null;

      while (sometingOptimized)
      {
         sometingOptimized = false;
         for (i = 0; optReqd && i < epsilonMoves.size(); i++)
         {
            if ((tmp1 = (NfaState)epsilonMoves.elementAt(i)).HasTransitions())
            {
               for (j = i + 1; j < epsilonMoves.size(); j++)
               {
                  if ((tmp2 = (NfaState)epsilonMoves.elementAt(j)).
                                                           HasTransitions() &&
                      (tmp1.asciiMoves[0] == tmp2.asciiMoves[0] &&
                       tmp1.asciiMoves[1] == tmp2.asciiMoves[1] &&
                       EqualCharArr(tmp1.charMoves, tmp2.charMoves) &&
                       EqualCharArr(tmp1.rangeMoves, tmp2.rangeMoves)))
                  {
                     if (equivStates == null)
                     {
                        equivStates = new Vector();
                        equivStates.addElement(tmp1);
                     }

                     InsertInOrder(equivStates, tmp2);
                     epsilonMoves.removeElementAt(j--);
                  }
               }
            }

            if (equivStates != null)
            {
               sometingOptimized = true;
               String tmp = "";
               for (int l = 0; l < equivStates.size(); l++)
                  tmp += String.valueOf(
                            ((NfaState)equivStates.elementAt(l)).id) + ", ";

               if ((newState = (NfaState)equivStatesTable.get(tmp)) == null)
               {
                  newState = CreateEquivState(equivStates);
                  equivStatesTable.put(tmp, newState);
               }

               epsilonMoves.removeElementAt(i--);
               epsilonMoves.addElement(newState);
               equivStates = null;
               newState = null;
            }
         }

         for (i = 0; i < epsilonMoves.size(); i++)
         {
            //if ((tmp1 = (NfaState)epsilonMoves.elementAt(i)).next == null)
               //continue;
            tmp1 = (NfaState)epsilonMoves.elementAt(i);

            for (j = i + 1; j < epsilonMoves.size(); j++)
            {
               tmp2 = (NfaState)epsilonMoves.elementAt(j);

               if (tmp1.next == tmp2.next)
               {
                  if (newState == null)
                  {
                     newState = tmp1.CreateClone();
                     newState.next = tmp1.next;
                     sometingOptimized = true;
                  }

                  newState.MergeMoves(tmp2);
                  epsilonMoves.removeElementAt(j--);
               }
            }

            if (newState != null)
            {
               epsilonMoves.removeElementAt(i--);
               epsilonMoves.addElement(newState);
               newState = null;
            }
         }
      }

      // End Warning

      // Generate an array of states for epsilon moves (not vector)
      if (epsilonMoves.size() > 0)
      {
         for (i = 0; i < epsilonMoves.size(); i++)
            // Since we are doing a closure, just epsilon moves are unncessary
            if (((NfaState)epsilonMoves.elementAt(i)).HasTransitions())
               usefulEpsilonMoves++;
            else
               epsilonMoves.removeElementAt(i--);
      }
   }

   void GenerateNextStatesCode()
   {
      if (next.usefulEpsilonMoves > 0)
         next.GetEpsilonMovesString();
   }

   String GetEpsilonMovesString()
   {
      int[] stateNames = new int[usefulEpsilonMoves];
      int cnt = 0;

      if (epsilonMovesString != null)
         return epsilonMovesString;

      if (usefulEpsilonMoves > 0)
      {
         NfaState tempState;
         epsilonMovesString = "{ ";
         for (int i = 0; i < epsilonMoves.size(); i++)
         {
            if ((tempState = (NfaState)epsilonMoves.elementAt(i)).
                                                     HasTransitions())
            {
               if (tempState.stateName == -1)
                  tempState.GenerateCode();

               ((NfaState)indexedAllStates.elementAt(tempState.stateName)).inNextOf++;
               stateNames[cnt] = tempState.stateName;
               epsilonMovesString += tempState.stateName + ", ";
               if (cnt++ > 0 && cnt % 16 == 0)
                  epsilonMovesString += "\n";
            }
         }

         epsilonMovesString += "};";
      }

      usefulEpsilonMoves = cnt;
      if (epsilonMovesString != null &&
          allNextStates.get(epsilonMovesString) == null)
      {
         int[] statesToPut = new int[usefulEpsilonMoves];

         System.arraycopy(stateNames, 0, statesToPut, 0, cnt);
         allNextStates.put(epsilonMovesString, statesToPut);
      }

      return epsilonMovesString;
   }

   public static boolean CanStartNfaUsingAscii(char c)
   {
      if (c >= 128)
         throw new Error("JavaCC Bug: Please send mail to sankar@cs.stanford.edu");

      String s = LexGen.initialState.GetEpsilonMovesString();

      if (s == null || s.equals("null;"))
         return false;

      int[] states = (int[])allNextStates.get(s);

      for (int i = 0; i < states.length; i++)
      {
         NfaState tmp = (NfaState)indexedAllStates.elementAt(states[i]);

         if ((tmp.asciiMoves[c / 64 ] & (1L << c % 64)) != 0L)
            return true;
      }

      return false;
   }

   final boolean CanMoveUsingChar(char c)
   {
      int i;

      if (onlyChar == 1)
         return c == matchSingleChar;

      if (c < 128)
         return ((asciiMoves[c / 64 ] & (1L << c % 64)) != 0L);

      // Just check directly if there is a move for this char
      if (charMoves != null && charMoves[0] != 0)
      {
         for (i = 0; i < charMoves.length; i++)
         {
            if (c == charMoves[i])
               return true;
            else if (c < charMoves[i] || charMoves[i] == 0)
               break;
         }
      }


      // For ranges, iterate thru the table to see if the current char
      // is in some range
      if (rangeMoves != null && rangeMoves[0] != 0)
         for (i = 0; i < rangeMoves.length; i += 2)
            if (c >= rangeMoves[i] && c <= rangeMoves[i + 1])
               return true;
            else if (c < rangeMoves[i] || rangeMoves[i] == 0)
               break;

      //return (nextForNegatedList != null);
      return false;
   }

   public int getFirstValidPos(String s, int i, int len)
   {
      if (onlyChar == 1)
      {
         char c = matchSingleChar;
         while (c != s.charAt(i) && ++i < len);
         return i;
      }

      do
      {
         if (CanMoveUsingChar(s.charAt(i)))
            return i;
      } while (++i < len);

      return i;
   }

   public int MoveFrom(char c, Vector newStates)
   {
      if (CanMoveUsingChar(c))
      {
         for (int i = next.epsilonMoves.size(); i-- > 0;)
            InsertInOrder(newStates, (NfaState)next.epsilonMoves.elementAt(i));

         return kindToPrint;
      }

      return Integer.MAX_VALUE;
   }

   public static int MoveFromSet(char c, Vector states, Vector newStates)
   {
      int tmp;
      int retVal = Integer.MAX_VALUE;

      for (int i = states.size(); i-- > 0;)
         if (retVal >
             (tmp = ((NfaState)states.elementAt(i)).MoveFrom(c, newStates)))
            retVal = tmp;

      return retVal;
   }

   public static int moveFromSetForRegEx(char c, NfaState[] states, NfaState[] newStates, int round)
   {
      int start = 0;
      int sz = states.length;

      for (int i = 0; i < sz; i++)
      {
         NfaState tmp1, tmp2;

         if ((tmp1 = states[i]) == null)
            break;

         if (tmp1.CanMoveUsingChar(c))
         {
            if (tmp1.kindToPrint != Integer.MAX_VALUE)
            {
               newStates[start] = null;
               return 1;
            }

            NfaState[] v = tmp1.next.epsilonMoveArray;
            for (int j = v.length; j-- > 0;)
            {
               if ((tmp2 = v[j]).round != round)
               {
                  tmp2.round = round;
                  newStates[start++] = tmp2;
               }
            }
         }
      }

      newStates[start] = null;
      return Integer.MAX_VALUE;
   }

   static Vector allBitVectors = new Vector();

   /* This function generates the bit vectors of low and hi bytes for common
      bit vectors and retunrs those that are not common with anything (in
      loBytes) and returns an array of indices that can be used to generate
      the function names for char matching using the common bit vectors.
      It also generates code to match a char with the common bit vectors.
      (Need a better comment). */

   static int[] tmpIndices = new int[512]; // 2 * 256
   void GenerateNonAsciiMoves(java.io.PrintWriter ostr)
   {
      int i = 0, j = 0;
      char hiByte;
      int cnt = 0;
      long[][] loBytes = new long[256][4];

      if ((charMoves == null || charMoves[0] == 0) &&
          (rangeMoves == null || rangeMoves[0] == 0))
         return;

      if (charMoves != null)
      {
         for (i = 0; i < charMoves.length; i++)
         {
            if (charMoves[i] == 0)
               break;

            hiByte = (char)(charMoves[i] >> 8);
            loBytes[hiByte][(charMoves[i] & 0xff) / 64] |=
                              (1L << ((charMoves[i] & 0xff) % 64));
         }
      }

      if (rangeMoves != null)
      {
         for (i = 0; i < rangeMoves.length; i += 2)
         {
            if (rangeMoves[i] == 0)
               break;

            char c, r;

            r = (char)(rangeMoves[i + 1] & 0xff);
            hiByte = (char)(rangeMoves[i] >> 8);

            if (hiByte == (char)(rangeMoves[i + 1] >> 8))
            {
               for (c = (char)(rangeMoves[i] & 0xff); c <= r; c++)
                  loBytes[hiByte][c / 64] |= (1L << (c % 64));

               continue;
            }

            for (c = (char)(rangeMoves[i] & 0xff); c <= 0xff; c++)
               loBytes[hiByte][c / 64] |= (1L << (c % 64));

            while (++hiByte < (char)(rangeMoves[i + 1] >> 8))
            {
               loBytes[hiByte][0] |= 0xffffffffffffffffL;
               loBytes[hiByte][1] |= 0xffffffffffffffffL;
               loBytes[hiByte][2] |= 0xffffffffffffffffL;
               loBytes[hiByte][3] |= 0xffffffffffffffffL;
            }

            for (c = 0; c <= r; c++)
               loBytes[hiByte][c / 64] |= (1L << (c % 64));
         }
      }

      long[] common = null;
      boolean[] done = new boolean[256];

      for (i = 0; i <= 255; i++)
      {
         if (done[i] ||
             (done[i] =
              loBytes[i][0] == 0 &&
              loBytes[i][1] == 0 &&
              loBytes[i][2] == 0 &&
              loBytes[i][3] == 0))
            continue;

         for (j = i + 1; j < 256; j++)
         {
            if (done[j])
               continue;

            if (loBytes[i][0] == loBytes[j][0] &&
                loBytes[i][1] == loBytes[j][1] &&
                loBytes[i][2] == loBytes[j][2] &&
                loBytes[i][3] == loBytes[j][3])
            {
               done[j] = true;
               if (common == null)
               {
                  done[i] = true;
                  common = new long[4];
                  common[i / 64] |= (1L << (i % 64));
               }

               common[j / 64] |= (1L << (j % 64));
            }
         }

         if (common != null)
         {
            Integer ind;
            String tmp;

            tmp = "{\n   0x" + Long.toHexString(common[0]) + "L, " +
                    "0x" + Long.toHexString(common[1]) + "L, " +
                    "0x" + Long.toHexString(common[2]) + "L, " +
                    "0x" + Long.toHexString(common[3]) + "L\n};";
            if ((ind = (Integer)lohiByteTab.get(tmp)) == null)
            {
               allBitVectors.addElement(tmp);

               if (!AllBitsSet(tmp))
                  ostr.println("static final long[] jjbitVec" +  lohiByteCnt + " = " + tmp);
               lohiByteTab.put(tmp, ind = new Integer(lohiByteCnt++));
            }

            tmpIndices[cnt++] = ind.intValue();

            tmp = "{\n   0x" + Long.toHexString(loBytes[i][0]) + "L, " +
                    "0x" + Long.toHexString(loBytes[i][1]) + "L, " +
                    "0x" + Long.toHexString(loBytes[i][2]) + "L, " +
                    "0x" + Long.toHexString(loBytes[i][3]) + "L\n};";
            if ((ind = (Integer)lohiByteTab.get(tmp)) == null)
            {
               allBitVectors.addElement(tmp);

               if (!AllBitsSet(tmp))
                  ostr.println("static final long[] jjbitVec" + lohiByteCnt + " = " + tmp);
               lohiByteTab.put(tmp, ind = new Integer(lohiByteCnt++));
            }

            tmpIndices[cnt++] = ind.intValue();

            common = null;
         }
      }

      nonAsciiMoveIndices = new int[cnt];
      System.arraycopy(tmpIndices, 0, nonAsciiMoveIndices, 0, cnt);

/*
      System.out.println("state : " + stateName + " cnt : " + cnt);
      while (cnt > 0)
      {
         System.out.print(nonAsciiMoveIndices[cnt - 1] + ", " + nonAsciiMoveIndices[cnt - 2] + ", ");
         cnt -= 2;
      }
      System.out.println("");
*/

      for (i = 0; i < 256; i++)
      {
         if (done[i])
            loBytes[i] = null;
         else
         {
            //System.out.print(i + ", ");
            String tmp;
            Integer ind;

            tmp = "{\n   0x" + Long.toHexString(loBytes[i][0]) + "L, " +
                    "0x" + Long.toHexString(loBytes[i][1]) + "L, " +
                    "0x" + Long.toHexString(loBytes[i][2]) + "L, " +
                    "0x" + Long.toHexString(loBytes[i][3]) + "L\n};";

            if ((ind = (Integer)lohiByteTab.get(tmp)) == null)
            {
               allBitVectors.addElement(tmp);

               if (!AllBitsSet(tmp))
                  ostr.println("static final long[] jjbitVec" +  lohiByteCnt + " = " + tmp);
               lohiByteTab.put(tmp, ind = new Integer(lohiByteCnt++));
            }

            if (loByteVec == null)
               loByteVec = new Vector();

            loByteVec.addElement(new Integer(i));
            loByteVec.addElement(ind);
         }
      }
      //System.out.println("");
      UpdateDuplicateNonAsciiMoves();
   }

   private void UpdateDuplicateNonAsciiMoves()
   {
      for (int i = 0; i < nonAsciiTableForMethod.size(); i++)
      {
         NfaState tmp = (NfaState)nonAsciiTableForMethod.elementAt(i);
         if (EqualLoByteVectors(loByteVec, tmp.loByteVec) &&
             EqualNonAsciiMoveIndices(nonAsciiMoveIndices, tmp.nonAsciiMoveIndices))
         {
            nonAsciiMethod = i;
            return;
         }
      }

      nonAsciiMethod = nonAsciiTableForMethod.size();
      nonAsciiTableForMethod.addElement(this);
   }

   private static boolean EqualLoByteVectors(Vector vec1, Vector vec2)
   {
      if (vec1 == null || vec2 == null)
         return false;

      if (vec1 == vec2)
         return true;

      if (vec1.size() != vec2.size())
         return false;

      for (int i = 0; i < vec1.size(); i++)
      {
         if (((Integer)vec1.elementAt(i)).intValue() !=
             ((Integer)vec2.elementAt(i)).intValue())
            return false;
      }

      return true;
   }

   private static boolean EqualNonAsciiMoveIndices(int[] moves1, int[] moves2)
   {
      if (moves1 == moves2)
         return true;

      if (moves1 == null || moves2 == null)
         return false;

      if (moves1.length != moves2.length)
         return false;

      for (int i = 0; i < moves1.length;i++)
      {
         if (moves1[i] != moves2[i])
            return false;
      }

      return true;
   }

   static String allBits = "{\n   0xffffffffffffffffL, " +
                    "0xffffffffffffffffL, " +
                    "0xffffffffffffffffL, " +
                    "0xffffffffffffffffL\n};";

   static boolean AllBitsSet(String bitVec)
   {
      return bitVec.equals(allBits);
   }

   static int AddStartStateSet(String stateSetString)
   {
      return AddCompositeStateSet(stateSetString, true);
   }

   private static int AddCompositeStateSet(String stateSetString, boolean starts)
   {
      Integer stateNameToReturn;

      if ((stateNameToReturn = (Integer)stateNameForComposite.get(stateSetString)) != null)
         return stateNameToReturn.intValue();

      int toRet = 0;
      int[] nameSet = (int[])allNextStates.get(stateSetString);

      if (!starts)
         stateBlockTable.put(stateSetString, stateSetString);

      if (nameSet == null)
         throw new Error("JavaCC Bug: Please send mail to sankar@cs.stanford.edu; nameSet null for : " + stateSetString);

      if (nameSet.length == 1)
      {
         stateNameToReturn = new Integer(nameSet[0]);
         stateNameForComposite.put(stateSetString, stateNameToReturn);
         return nameSet[0];
      }

      for (int i = 0; i < nameSet.length; i++)
      {
         if (nameSet[i] == -1)
            continue;

         NfaState st = (NfaState)indexedAllStates.elementAt(nameSet[i]);
         st.isComposite = true;
         st.compositeStates = nameSet;
      }

      while (toRet < nameSet.length &&
             (starts && ((NfaState)indexedAllStates.elementAt(nameSet[toRet])).inNextOf > 1))
         toRet++;

      Enumeration e = compositeStateTable.keys();
      String s;
      while (e.hasMoreElements())
      {
         s = (String)e.nextElement();
         if (!s.equals(stateSetString) && Intersect(stateSetString, s))
         {
            int[] other = (int[])compositeStateTable.get(s);

            while (toRet < nameSet.length &&
                   ((starts && ((NfaState)indexedAllStates.elementAt(nameSet[toRet])).inNextOf > 1) ||
                    ElemOccurs(nameSet[toRet], other) >= 0))
               toRet++;
         }
      }

      int tmp;

      if (toRet >= nameSet.length)
      {
         if (dummyStateIndex == -1)
            tmp = dummyStateIndex = generatedStates;
         else
            tmp = ++dummyStateIndex;
      }
      else
         tmp = nameSet[toRet];

      stateNameToReturn = new Integer(tmp);
      stateNameForComposite.put(stateSetString, stateNameToReturn);
      compositeStateTable.put(stateSetString, nameSet);

      return tmp;
   }

   private static int StateNameForComposite(String stateSetString)
   {
      return ((Integer)stateNameForComposite.get(stateSetString)).intValue();
   }

   static int InitStateName()
   {
      String s = LexGen.initialState.GetEpsilonMovesString();

      if (LexGen.initialState.usefulEpsilonMoves != 0)
         return StateNameForComposite(s);
      return -1;
   }

   public void GenerateInitMoves(java.io.PrintWriter ostr)
   {
      GetEpsilonMovesString();

      if (epsilonMovesString == null)
         epsilonMovesString = "null;";

      AddStartStateSet(epsilonMovesString);
   }

   static Hashtable tableToDump = new Hashtable();
   static Vector orderedStateSet = new Vector();

   static int lastIndex = 0;
   private static int[] GetStateSetIndicesForUse(String arrayString)
   {
      int[] ret;
      int[] set = (int[])allNextStates.get(arrayString);

      if ((ret = (int[])tableToDump.get(arrayString)) == null)
      {
         ret = new int[2];
         ret[0] = lastIndex;
         ret[1] = lastIndex + set.length - 1;
         lastIndex += set.length;
         tableToDump.put(arrayString, ret);
         orderedStateSet.addElement(set);
      }

      return ret;
   }

   public static void DumpStateSets(java.io.PrintWriter ostr)
   {
      int cnt = 0;

      ostr.print("static final int[] jjnextStates = {");
      for (int i = 0; i < orderedStateSet.size(); i++)
      {
         int[] set = (int[])orderedStateSet.elementAt(i);

         for (int j = 0; j < set.length; j++)
         {
            if (cnt++ % 16  == 0)
               ostr.print("\n   ");

            ostr.print(set[j] + ", ");
         }
      }

      ostr.println("\n};");
   }

   static String GetStateSetString(int[] states)
   {
      String retVal = "{ ";
      for (int i = 0; i < states.length; )
      {
         retVal += states[i] + ", ";

         if (i++ > 0 && i % 16 == 0)
            retVal += "\n";
      }

      retVal += "};";
      allNextStates.put(retVal, states);
      return retVal;
   }

   static String GetStateSetString(Vector states)
   {
      if (states == null || states.size() == 0)
         return "null;";

      int[] set = new int[states.size()];
      String retVal = "{ ";
      for (int i = 0; i < states.size(); )
      {
         int k;
         retVal += (k = ((NfaState)states.elementAt(i)).stateName) + ", ";
         set[i] = k;

         if (i++ > 0 && i % 16 == 0)
            retVal += "\n";
      }

      retVal += "};";
      allNextStates.put(retVal, set);
      return retVal;
   }

   static int NumberOfBitsSet(long l)
   {
      int ret = 0;
      for (int i = 0; i < 63; i++)
         if (((l >> i) & 1L) != 0L)
            ret++;

      return ret;
   }

   static int OnlyOneBitSet(long l)
   {
      int oneSeen = -1;
      for (int i = 0; i < 64; i++)
         if (((l >> i) & 1L) != 0L)
         {
            if (oneSeen >= 0)
               return -1;
            oneSeen = i;
         }

      return oneSeen;
   }

   private static int ElemOccurs(int elem, int[] arr)
   {
      for (int i = arr.length; i-- > 0;)
         if (arr[i] == elem)
            return i;

      return -1;
   }

   private boolean FindCommonBlocks()
   {
      if (next == null || next.usefulEpsilonMoves <= 1)
         return false;

      if (stateDone == null)
         stateDone = new boolean[generatedStates];

      String set = next.epsilonMovesString;

      int[] nameSet = (int[])allNextStates.get(set);

      if (nameSet.length <= 2 || compositeStateTable.get(set) != null)
         return false;

      int i;
      int freq[] = new int[nameSet.length];
      boolean live[] = new boolean[nameSet.length];
      int[] count = new int[allNextStates.size()];

      for (i = 0; i < nameSet.length; i++)
      {
         if (nameSet[i] != -1)
         {
            if (live[i] = !stateDone[nameSet[i]])
               count[0]++;
         }
      }

      int j, blockLen = 0, commonFreq = 0;
      Enumeration e = allNextStates.keys();
      boolean needUpdate;

      while (e.hasMoreElements())
      {
         int[] tmpSet = (int[])allNextStates.get(e.nextElement());
         if (tmpSet == nameSet)
            continue;

         needUpdate = false;
         for (j = 0; j < nameSet.length; j++)
         {
            if (nameSet[j] == -1)
               continue;

            if (live[j] && ElemOccurs(nameSet[j], tmpSet) >= 0)
            {
               if (!needUpdate)
               {
                  needUpdate = true;
                  commonFreq++;
               }

               count[freq[j]]--;
               count[commonFreq]++;
               freq[j] = commonFreq;
            }
         }

         if (needUpdate)
         {
            int foundFreq = -1;
            blockLen = 0;

            for (j = 0; j <= commonFreq; j++)
               if (count[j] > blockLen)
               {
                  foundFreq = j;
                  blockLen = count[j];
               }

            if (blockLen <= 1)
               return false;

            for (j = 0; j < nameSet.length; j++)
               if (nameSet[j] != -1 && freq[j] != foundFreq)
               {
                  live[j] = false;
                  count[freq[j]]--;
               }
         }
      }

      if (blockLen <= 1)
         return false;

      int[] commonBlock = new int[blockLen];
      int cnt = 0;
      //System.out.println("Common Block for " + set + " : ");
      for (i = 0; i < nameSet.length; i++)
      {
         if (live[i])
         {
            if (((NfaState)indexedAllStates.elementAt(nameSet[i])).isComposite)
               return false;

            stateDone[nameSet[i]] = true;
            commonBlock[cnt++] = nameSet[i];
            //System.out.print(nameSet[i] + ", ");
         }
      }

      //System.out.println("");

      String s = GetStateSetString(commonBlock);
      e = allNextStates.keys();

      Outer :
      while (e.hasMoreElements())
      {
         int at;
         boolean firstOne = true;
         String stringToFix;
         int[] setToFix = (int[])allNextStates.get(stringToFix = (String)e.nextElement());

         if (setToFix == commonBlock)
            continue;

         for (int k = 0; k < cnt; k++)
         {
            if ((at = ElemOccurs(commonBlock[k], setToFix)) >= 0)
            {
               if (!firstOne)
                  setToFix[at] = -1;
               firstOne = false;
            }
            else
               continue Outer;
         }

         if (stateSetsToFix.get(stringToFix) == null)
            stateSetsToFix.put(stringToFix, setToFix);
      }

      next.usefulEpsilonMoves -= blockLen - 1;
      AddCompositeStateSet(s, false);
      return true;
   }

   private boolean CheckNextOccursTogether()
   {
      if (next == null || next.usefulEpsilonMoves <= 1)
         return true;

      String set = next.epsilonMovesString;

      int[] nameSet = (int[])allNextStates.get(set);

      if (nameSet.length == 1 || compositeStateTable.get(set) != null ||
          stateSetsToFix.get(set) != null)
         return false;

      int i;
      Hashtable occursIn = new Hashtable();
      NfaState tmp = (NfaState)allStates.elementAt(nameSet[0]);

      for (i = 1; i < nameSet.length; i++)
      {
         NfaState tmp1 = (NfaState)allStates.elementAt(nameSet[i]);

         if (tmp.inNextOf != tmp1.inNextOf)
            return false;
      }

      int isPresent, j;
      Enumeration e = allNextStates.keys();
      while (e.hasMoreElements())
      {
         String s;
         int[] tmpSet = (int[])allNextStates.get(s = (String)e.nextElement());

         if (tmpSet == nameSet)
            continue;

         isPresent = 0;
         Outer:
         for (j = 0; j < nameSet.length; j++)
         {
            if (ElemOccurs(nameSet[j], tmpSet) >= 0)
               isPresent++;
            else if (isPresent > 0)
               return false;
         }

         if (isPresent == j)
         {
            if (tmpSet.length > nameSet.length)
               occursIn.put(s, tmpSet);

            /* May not need. But safe. */
            if (compositeStateTable.get(s) != null ||
                stateSetsToFix.get(s) != null)
               return false;
         }
         else if (isPresent != 0)
            return false;
      }

      e = occursIn.keys();
      while (e.hasMoreElements())
      {
         String s;
         int[] setToFix = (int[])occursIn.get(s = (String)e.nextElement());

         if (stateSetsToFix.get(s) == null)
            stateSetsToFix.put(s, setToFix);

         for (int k = 0; k < setToFix.length; k++)
            if (ElemOccurs(setToFix[k], nameSet) > 0)  // Not >= since need the first one (0)
               setToFix[k] = -1;
      }

      next.usefulEpsilonMoves = 1;
      AddCompositeStateSet(next.epsilonMovesString, false);
      return true;
   }

   private static void FixStateSets()
   {
      Hashtable fixedSets = new Hashtable();
      Enumeration e = stateSetsToFix.keys();
      int[] tmp = new int[generatedStates];
      int i;

      while (e.hasMoreElements())
      {
         String s;
         int[] toFix = (int[])stateSetsToFix.get(s = (String)e.nextElement());
         int cnt = 0;

         //System.out.print("Fixing : ");
         for (i = 0; i < toFix.length; i++)
         {
            //System.out.print(toFix[i] + ", ");
            if (toFix[i] != -1)
               tmp[cnt++] = toFix[i];
         }

         int[] fixed = new int[cnt];
         System.arraycopy(tmp, 0, fixed, 0, cnt);
         fixedSets.put(s, fixed);
         allNextStates.put(s, fixed);
         //System.out.println(" as " + GetStateSetString(fixed));
      }

      for (i = 0; i < allStates.size(); i++)
      {
         NfaState tmpState = (NfaState)allStates.elementAt(i);
         int[] newSet;

         if (tmpState.next == null || tmpState.next.usefulEpsilonMoves == 0)
            continue;

         /*if (compositeStateTable.get(tmpState.next.epsilonMovesString) != null)
            tmpState.next.usefulEpsilonMoves = 1;
         else*/ if ((newSet = (int[])fixedSets.get(tmpState.next.epsilonMovesString)) != null)
            tmpState.FixNextStates(newSet);
      }
   }

   private final void FixNextStates(int[] newSet)
   {
      next.usefulEpsilonMoves = newSet.length;
      //next.epsilonMovesString = GetStateSetString(newSet);
   }

   private static boolean Intersect(String set1, String set2)
   {
      if (set1 == null || set2 == null)
         return false;

      int[] nameSet1 = (int[])allNextStates.get(set1);
      int[] nameSet2 = (int[])allNextStates.get(set2);

      if (nameSet1 == null || nameSet2 == null)
         return false;

      if (nameSet1 == nameSet2)
         return true;

      for (int i = nameSet1.length; i-- > 0; )
         for (int j = nameSet2.length; j-- > 0; )
            if (nameSet1[i] == nameSet2[j])
               return true;

      return false;
   }

   private static void DumpHeadForCase(java.io.PrintWriter ostr, int byteNum)
   {
      if (byteNum == 0)
         ostr.println("         long l = 1L << curChar;");
      else if (byteNum == 1)
         ostr.println("         long l = 1L << (curChar & 077);");

      else
      {
         if (Options.getJavaUnicodeEscape() || unicodeWarningGiven)
         {
            ostr.println("         int hiByte = (int)(curChar >> 8);");
            ostr.println("         int i1 = hiByte >> 6;");
            ostr.println("         long l1 = 1L << (hiByte & 077);");
         }

         ostr.println("         int i2 = (curChar & 0xff) >> 6;");
         ostr.println("         long l2 = 1L << (curChar & 077);");
      }

      ostr.println("         MatchLoop: do");
      ostr.println("         {");

      ostr.println("            switch(jjstateSet[--i])");
      ostr.println("            {");
   }

   private static Vector PartitionStatesSetForAscii(int[] states, int byteNum)
   {
      int[] cardinalities = new int[states.length];
      Vector original = new Vector();
      Vector partition = new Vector();
      NfaState tmp;

      original.setSize(states.length);
      int cnt = 0;
      for (int i = 0; i < states.length; i++)
      {
         tmp = (NfaState)allStates.elementAt(states[i]);

         if (tmp.asciiMoves[byteNum] != 0L)
         {
            int j;
            int p = NumberOfBitsSet(tmp.asciiMoves[byteNum]);

            for (j = 0; j < i; j++)
               if (cardinalities[j] <= p)
                  break;

            for (int k = i; k > j; k--)
               cardinalities[k] = cardinalities[k - 1];

            cardinalities[j] = p;

            original.insertElementAt(tmp, j);
            cnt++;
         }
      }

      original.setSize(cnt);

      while (original.size() > 0)
      {
         tmp = (NfaState)original.elementAt(0);
         original.removeElement(tmp);

         long bitVec = tmp.asciiMoves[byteNum];
         Vector subSet = new Vector();
         subSet.addElement(tmp);

         for (int j = 0; j < original.size(); j++)
         {
            NfaState tmp1 = (NfaState)original.elementAt(j);

            if ((tmp1.asciiMoves[byteNum] & bitVec) == 0L)
            {
               bitVec |= tmp1.asciiMoves[byteNum];
               subSet.addElement(tmp1);
               original.removeElementAt(j--);
            }
         }

         partition.addElement(subSet);
      }

      return partition;
   }

   private String PrintNoBreak(java.io.PrintWriter ostr, int byteNum, boolean[] dumped)
   {
      if (inNextOf != 1)
         throw new Error("JavaCC Bug: Please send mail to sankar@cs.stanford.edu");

      dumped[stateName] = true;

      if (byteNum >= 0)
      {
         if (asciiMoves[byteNum] != 0L)
         {
            ostr.println("               case " + stateName + ":");
            DumpAsciiMoveForCompositeState(ostr, byteNum, false);
            return "";
         }
      }
      else if (nonAsciiMethod != -1)
      {
         ostr.println("               case " + stateName + ":");
         DumpNonAsciiMoveForCompositeState(ostr);
         return "";
      }

      return ("               case " + stateName + ":\n");
   }

   private static void DumpCompositeStatesAsciiMoves(java.io.PrintWriter ostr,
                                String key, int byteNum, boolean[] dumped)
   {
      int i;

      int[] nameSet = (int[])allNextStates.get(key);

      if (nameSet.length == 1 || dumped[StateNameForComposite(key)])
         return;

      NfaState toBePrinted = null;
      int neededStates = 0;
      NfaState tmp;
      NfaState stateForCase = null;
      String toPrint = "";
      boolean stateBlock = (stateBlockTable.get(key) != null);

      for (i = 0; i < nameSet.length; i++)
      {
         tmp = (NfaState)allStates.elementAt(nameSet[i]);

         if (tmp.asciiMoves[byteNum] != 0L)
         {
            if (neededStates++ == 1)
               break;
            else
               toBePrinted = tmp;
         }
         else
            dumped[tmp.stateName] = true;

         if (tmp.stateForCase != null)
         {
            if (stateForCase != null)
               throw new Error("JavaCC Bug: Please send mail to sankar@cs.stanford.edu : ");

            stateForCase = tmp.stateForCase;
         }
      }

      if (stateForCase != null)
         toPrint = stateForCase.PrintNoBreak(ostr, byteNum, dumped);

      if (neededStates == 0)
      {
         if (stateForCase != null && toPrint.equals(""))
            ostr.println("                  break;");
         return;
      }

      if (neededStates == 1)
      {
         //if (byteNum == 1)
            //System.out.println(toBePrinted.stateName + " is the only state for "
               //+ key + " ; and key is : " + StateNameForComposite(key));

         if (!toPrint.equals(""))
            ostr.print(toPrint);

         ostr.println("               case " + StateNameForComposite(key) + ":");

         if (!dumped[toBePrinted.stateName] && !stateBlock && toBePrinted.inNextOf > 1)
            ostr.println("               case " + toBePrinted.stateName + ":");

         dumped[toBePrinted.stateName] = true;
         toBePrinted.DumpAsciiMove(ostr, byteNum, dumped);
         return;
      }

      Vector partition = PartitionStatesSetForAscii(nameSet, byteNum);

      if (!toPrint.equals(""))
         ostr.print(toPrint);

      int keyState = StateNameForComposite(key);
      ostr.println("               case " + keyState + ":");
      if (keyState < generatedStates)
         dumped[keyState] = true;

      for (i = 0; i < partition.size(); i++)
      {
         Vector subSet = (Vector)partition.elementAt(i);

         for (int j = 0; j < subSet.size(); j++)
         {
            tmp = (NfaState)subSet.elementAt(j);

            if (stateBlock)
               dumped[tmp.stateName] = true;
            tmp.DumpAsciiMoveForCompositeState(ostr, byteNum, j != 0);
         }
      }

      if (stateBlock)
         ostr.println("                  break;");
      else
         ostr.println("                  break;");
   }

   private boolean selfLoop()
   {
      if (next == null || next.epsilonMovesString == null)
         return false;

      int[] set = (int[])allNextStates.get(next.epsilonMovesString);
      return ElemOccurs(stateName, set) >= 0;
   }

   private void DumpAsciiMoveForCompositeState(java.io.PrintWriter ostr, int byteNum, boolean elseNeeded)
   {
      boolean nextIntersects = selfLoop();

      for (int j = 0; j < allStates.size(); j++)
      {
         NfaState temp1 = (NfaState)allStates.elementAt(j);

         if (this == temp1 || temp1.stateName == -1 || temp1.dummy ||
             stateName == temp1.stateName || temp1.asciiMoves[byteNum] == 0L)
            continue;

         if (!nextIntersects && Intersect(temp1.next.epsilonMovesString,
                                         next.epsilonMovesString))
         {
            nextIntersects = true;
            break;
         }
      }

      //System.out.println(stateName + " \'s nextIntersects : " + nextIntersects);
      String prefix = "";
      if (asciiMoves[byteNum] != 0xffffffffffffffffL)
      {
         int oneBit = OnlyOneBitSet(asciiMoves[byteNum]);

         if (oneBit != -1)
            ostr.println("                  " + (elseNeeded ? "else " : "") + "if (curChar == " +
                    (64 * byteNum + oneBit) + ")");
         else
            ostr.println("                  " + (elseNeeded ? "else " : "") + "if ((0x" + Long.toHexString(asciiMoves[byteNum]) +
                    "L & l) != 0L)");
         prefix = "   ";
      }

      if (kindToPrint != Integer.MAX_VALUE)
      {
         if (asciiMoves[byteNum] != 0xffffffffffffffffL)
         {
            ostr.println("                  {");
         }

         ostr.println(prefix + "                  if (kind > " + kindToPrint + ")");
         ostr.println(prefix + "                     kind = " + kindToPrint + ";");
      }

      if (next != null && next.usefulEpsilonMoves > 0)
      {
         int[] stateNames = (int[])allNextStates.get(
                                          next.epsilonMovesString);
         if (next.usefulEpsilonMoves == 1)
         {
            int name = stateNames[0];

            if (nextIntersects)
               ostr.println(prefix + "                  jjCheckNAdd(" + name + ");");
            else
               ostr.println(prefix + "                  jjstateSet[jjnewStateCnt++] = " + name + ";");
         }
         else if (next.usefulEpsilonMoves == 2 && nextIntersects)
         {
            ostr.println(prefix + "                  jjCheckNAddTwoStates(" +
               stateNames[0] + ", " + stateNames[1] + ");");
         }
         else
         {
            int[] indices = GetStateSetIndicesForUse(next.epsilonMovesString);
            boolean notTwo = (indices[0] + 1 != indices[1]);

            if (nextIntersects)
               ostr.println(prefix + "                  jjCheckNAddStates(" +
                  indices[0] + (notTwo  ? (", " + indices[1]) : "") + ");");
            else
               ostr.println(prefix + "                  jjAddStates(" +
                                     indices[0] + ", " + indices[1] + ");");
         }
      }

      if (asciiMoves[byteNum] != 0xffffffffffffffffL && kindToPrint != Integer.MAX_VALUE)
         ostr.println("                  }");
   }

   private void DumpAsciiMove(java.io.PrintWriter ostr, int byteNum, boolean dumped[])
   {
      boolean nextIntersects = selfLoop() && isComposite;
      boolean onlyState = true;

      for (int j = 0; j < allStates.size(); j++)
      {
         NfaState temp1 = (NfaState)allStates.elementAt(j);

         if (this == temp1 || temp1.stateName == -1 || temp1.dummy ||
             stateName == temp1.stateName || temp1.asciiMoves[byteNum] == 0L)
            continue;

         if (onlyState && (asciiMoves[byteNum] & temp1.asciiMoves[byteNum]) != 0L)
            onlyState = false;

         if (!nextIntersects && Intersect(temp1.next.epsilonMovesString,
                                         next.epsilonMovesString))
            nextIntersects = true;

         if (!dumped[temp1.stateName] && !temp1.isComposite &&
             asciiMoves[byteNum] == temp1.asciiMoves[byteNum] &&
             kindToPrint == temp1.kindToPrint &&
             (next.epsilonMovesString == temp1.next.epsilonMovesString ||
              (next.epsilonMovesString != null &&
               temp1.next.epsilonMovesString != null &&
               next.epsilonMovesString.equals(
                            temp1.next.epsilonMovesString))))
         {
            dumped[temp1.stateName] = true;
            ostr.println("               case " + temp1.stateName + ":");
         }
      }

      //if (onlyState)
         //nextIntersects = false;

      int oneBit = OnlyOneBitSet(asciiMoves[byteNum]);
      if (asciiMoves[byteNum] != 0xffffffffffffffffL)
      {
         if ((next == null || next.usefulEpsilonMoves == 0) &&
             kindToPrint != Integer.MAX_VALUE)
         {
            String kindCheck = "";

            if (!onlyState)
               kindCheck = " && kind > " + kindToPrint;

            if (oneBit != -1)
               ostr.println("                  if (curChar == " +
                  (64 * byteNum + oneBit) + kindCheck + ")");
            else
               ostr.println("                  if ((0x" +
                   Long.toHexString(asciiMoves[byteNum]) +
                   "L & l) != 0L" + kindCheck + ")");

            ostr.println("                     kind = " + kindToPrint + ";");

            if (onlyState)
               ostr.println("                  break;");
            else
               ostr.println("                  break;");

            return;
         }
      }

      String prefix = "";
      if (kindToPrint != Integer.MAX_VALUE)
      {

         if (oneBit != -1)
         {
            ostr.println("                  if (curChar != " +
                    (64 * byteNum + oneBit) + ")");
            ostr.println("                     break;");
         }
         else if (asciiMoves[byteNum] != 0xffffffffffffffffL)
         {
            ostr.println("                  if ((0x" + Long.toHexString(asciiMoves[byteNum]) + "L & l) == 0L)");
            ostr.println("                     break;");
         }

         if (onlyState)
         {
            ostr.println("                  kind = " + kindToPrint + ";");
         }
         else
         {
            ostr.println("                  if (kind > " + kindToPrint + ")");
            ostr.println("                     kind = " + kindToPrint + ";");
         }
      }
      else
      {
         if (oneBit != -1)
         {
            ostr.println("                  if (curChar == " +
                    (64 * byteNum + oneBit) + ")");
            prefix = "   ";
         }
         else if (asciiMoves[byteNum] != 0xffffffffffffffffL)
         {
            ostr.println("                  if ((0x" + Long.toHexString(asciiMoves[byteNum]) + "L & l) != 0L)");
            prefix = "   ";
         }
      }

      if (next != null && next.usefulEpsilonMoves > 0)
      {
         int[] stateNames = (int[])allNextStates.get(
                                          next.epsilonMovesString);
         if (next.usefulEpsilonMoves == 1)
         {
            int name = stateNames[0];
            if (nextIntersects)
               ostr.println(prefix + "                  jjCheckNAdd(" + name + ");");
            else
               ostr.println(prefix + "                  jjstateSet[jjnewStateCnt++] = " + name + ";");
         }
         else if (next.usefulEpsilonMoves == 2 && nextIntersects)
         {
            ostr.println(prefix + "                  jjCheckNAddTwoStates(" +
               stateNames[0] + ", " + stateNames[1] + ");");
         }
         else
         {
            int[] indices = GetStateSetIndicesForUse(next.epsilonMovesString);
            boolean notTwo = (indices[0] + 1 != indices[1]);

            if (nextIntersects)
               ostr.println(prefix + "                  jjCheckNAddStates(" +
                  indices[0] + (notTwo  ? (", " + indices[1]) : "") + ");");
            else
               ostr.println(prefix + "                  jjAddStates(" +
                                     indices[0] + ", " + indices[1] + ");");
         }
      }

      if (onlyState)
         ostr.println("                  break;");
      else
         ostr.println("                  break;");
   }

   private static void DumpAsciiMoves(java.io.PrintWriter ostr, int byteNum)
   {
      boolean[] dumped = new boolean[Math.max(generatedStates, dummyStateIndex + 1)];
      Enumeration e = compositeStateTable.keys();

      DumpHeadForCase(ostr, byteNum);

      while (e.hasMoreElements())
         DumpCompositeStatesAsciiMoves(ostr, (String)e.nextElement(), byteNum, dumped);

      for (int i = 0; i < allStates.size(); i++)
      {
         NfaState temp = (NfaState)allStates.elementAt(i);

         if (dumped[temp.stateName] || temp.lexState != LexGen.lexStateIndex ||
             !temp.HasTransitions() || temp.dummy ||
             temp.stateName == -1)
            continue;

         String toPrint = "";

         if (temp.stateForCase != null)
         {
            if (temp.inNextOf == 1)
               continue;

            if (dumped[temp.stateForCase.stateName])
               continue;

            toPrint = (temp.stateForCase.PrintNoBreak(ostr, byteNum, dumped));

            if (temp.asciiMoves[byteNum] == 0L)
            {
               if (toPrint.equals(""))
                  ostr.println("                  break;");

               continue;
            }
         }

         if (temp.asciiMoves[byteNum] == 0L)
            continue;

         if (!toPrint.equals(""))
            ostr.print(toPrint);

         dumped[temp.stateName] = true;
         ostr.println("               case " + temp.stateName + ":");
         temp.DumpAsciiMove(ostr, byteNum, dumped);
      }

      ostr.println("               default : break;");
      ostr.println("            }");
      ostr.println("         } while(i != startsAt);");
   }

   private static void DumpCompositeStatesNonAsciiMoves(java.io.PrintWriter ostr,
                                      String key, boolean[] dumped)
   {
      int i;
      int[] nameSet = (int[])allNextStates.get(key);

      if (nameSet.length == 1 || dumped[StateNameForComposite(key)])
         return;

      NfaState toBePrinted = null;
      int neededStates = 0;
      NfaState tmp;
      NfaState stateForCase = null;
      String toPrint = "";
      boolean stateBlock = (stateBlockTable.get(key) != null);

      for (i = 0; i < nameSet.length; i++)
      {
         tmp = (NfaState)allStates.elementAt(nameSet[i]);

         if (tmp.nonAsciiMethod != -1)
         {
            if (neededStates++ == 1)
               break;
            else
               toBePrinted = tmp;
         }
         else
            dumped[tmp.stateName] = true;

         if (tmp.stateForCase != null)
         {
            if (stateForCase != null)
               throw new Error("JavaCC Bug: Please send mail to sankar@cs.stanford.edu : ");

            stateForCase = tmp.stateForCase;
         }
      }

      if (stateForCase != null)
         toPrint = stateForCase.PrintNoBreak(ostr, -1, dumped);

      if (neededStates == 0)
      {
         if (stateForCase != null && toPrint.equals(""))
            ostr.println("                  break;");

         return;
      }

      if (neededStates == 1)
      {
         if (!toPrint.equals(""))
            ostr.print(toPrint);

         ostr.println("               case " + StateNameForComposite(key) + ":");

         if (!dumped[toBePrinted.stateName] && !stateBlock && toBePrinted.inNextOf > 1)
            ostr.println("               case " + toBePrinted.stateName + ":");

         dumped[toBePrinted.stateName] = true;
         toBePrinted.DumpNonAsciiMove(ostr, dumped);
         return;
      }

      if (!toPrint.equals(""))
         ostr.print(toPrint);

      int keyState = StateNameForComposite(key);
      ostr.println("               case " + keyState + ":");
      if (keyState < generatedStates)
         dumped[keyState] = true;

      for (i = 0; i < nameSet.length; i++)
      {
         tmp = (NfaState)allStates.elementAt(nameSet[i]);

         if (tmp.nonAsciiMethod != -1)
         {
            if (stateBlock)
               dumped[tmp.stateName] = true;
            tmp.DumpNonAsciiMoveForCompositeState(ostr);
         }
      }

      if (stateBlock)
         ostr.println("                  break;");
      else
         ostr.println("                  break;");
   }

   private final void DumpNonAsciiMoveForCompositeState(java.io.PrintWriter ostr)
   {
      boolean nextIntersects = selfLoop();
      for (int j = 0; j < allStates.size(); j++)
      {
         NfaState temp1 = (NfaState)allStates.elementAt(j);

         if (this == temp1 || temp1.stateName == -1 || temp1.dummy ||
             stateName == temp1.stateName || (temp1.nonAsciiMethod == -1))
            continue;

         if (!nextIntersects && Intersect(temp1.next.epsilonMovesString,
                                         next.epsilonMovesString))
         {
            nextIntersects = true;
            break;
         }
      }

      if (!Options.getJavaUnicodeEscape() && !unicodeWarningGiven)
      {
         if (loByteVec != null && loByteVec.size() > 1)
            ostr.println("                  if ((jjbitVec" +
             ((Integer)loByteVec.elementAt(1)).intValue() + "[i2" +
                "] & l2) != 0L)");
      }
      else
      {
         ostr.println("                  if (jjCanMove_" + nonAsciiMethod +
                                                "(hiByte, i1, i2, l1, l2))");
      }

      if (kindToPrint != Integer.MAX_VALUE)
      {
         ostr.println("                  {");
         ostr.println("                     if (kind > " + kindToPrint + ")");
         ostr.println("                        kind = " + kindToPrint + ";");
      }

      if (next != null && next.usefulEpsilonMoves > 0)
      {
         int[] stateNames = (int[])allNextStates.get(
                                          next.epsilonMovesString);
         if (next.usefulEpsilonMoves == 1)
         {
            int name = stateNames[0];
            if (nextIntersects)
               ostr.println("                     jjCheckNAdd(" + name + ");");
            else
               ostr.println("                     jjstateSet[jjnewStateCnt++] = " + name + ";");
         }
         else if (next.usefulEpsilonMoves == 2 && nextIntersects)
         {
            ostr.println("                     jjCheckNAddTwoStates(" +
               stateNames[0] + ", " + stateNames[1] + ");");
         }
         else
         {
            int[] indices = GetStateSetIndicesForUse(next.epsilonMovesString);
            boolean notTwo = (indices[0] + 1 != indices[1]);

            if (nextIntersects)
               ostr.println("                     jjCheckNAddStates(" +
                  indices[0] + (notTwo  ? (", " + indices[1]) : "") + ");");
            else
               ostr.println("                     jjAddStates(" +
                                     indices[0] + ", " + indices[1] + ");");
         }
      }

      if (kindToPrint != Integer.MAX_VALUE)
         ostr.println("                  }");
   }

   private final void DumpNonAsciiMove(java.io.PrintWriter ostr, boolean dumped[])
   {
      boolean nextIntersects = selfLoop() && isComposite;

      for (int j = 0; j < allStates.size(); j++)
      {
         NfaState temp1 = (NfaState)allStates.elementAt(j);

         if (this == temp1 || temp1.stateName == -1 || temp1.dummy ||
             stateName == temp1.stateName || (temp1.nonAsciiMethod == -1))
            continue;

         if (!nextIntersects && Intersect(temp1.next.epsilonMovesString,
                                         next.epsilonMovesString))
            nextIntersects = true;

         if (!dumped[temp1.stateName] && !temp1.isComposite &&
             nonAsciiMethod == temp1.nonAsciiMethod &&
             kindToPrint == temp1.kindToPrint &&
             (next.epsilonMovesString == temp1.next.epsilonMovesString ||
              (next.epsilonMovesString != null &&
               temp1.next.epsilonMovesString != null &&
               next.epsilonMovesString.equals(temp1.next.epsilonMovesString))))
         {
            dumped[temp1.stateName] = true;
            ostr.println("               case " + temp1.stateName + ":");
         }
      }

      if (next == null || next.usefulEpsilonMoves <= 0)
      {
         String kindCheck = " && kind > " + kindToPrint;

         if (!Options.getJavaUnicodeEscape() && !unicodeWarningGiven)
         {
            if (loByteVec != null && loByteVec.size() > 1)
               ostr.println("                  if ((jjbitVec" +
                ((Integer)loByteVec.elementAt(1)).intValue() + "[i2" +
                   "] & l2) != 0L" + kindCheck + ")");
         }
         else
         {
            ostr.println("                  if (jjCanMove_" + nonAsciiMethod +
                              "(hiByte, i1, i2, l1, l2)" + kindCheck + ")");
         }
         ostr.println("                     kind = " + kindToPrint + ";");
         ostr.println("                  break;");
         return;
      }

      String prefix = "   ";
      if (kindToPrint != Integer.MAX_VALUE)
      {
         if (!Options.getJavaUnicodeEscape() && !unicodeWarningGiven)
         {
            if (loByteVec != null && loByteVec.size() > 1)
            {
               ostr.println("                  if ((jjbitVec" +
                ((Integer)loByteVec.elementAt(1)).intValue() + "[i2" +
                "] & l2) == 0L)");
               ostr.println("                     break;");
            }
         }
         else
         {
            ostr.println("                  if (!jjCanMove_" + nonAsciiMethod +
                                                      "(hiByte, i1, i2, l1, l2))");
            ostr.println("                     break;");
         }

         ostr.println("                  if (kind > " + kindToPrint + ")");
         ostr.println("                     kind = " + kindToPrint + ";");
         prefix = "";
      }
      else if (!Options.getJavaUnicodeEscape() && !unicodeWarningGiven)
      {
         if (loByteVec != null && loByteVec.size() > 1)
            ostr.println("                  if ((jjbitVec" +
             ((Integer)loByteVec.elementAt(1)).intValue() + "[i2" +
                "] & l2) != 0L)");
      }
      else
      {
         ostr.println("                  if (jjCanMove_" + nonAsciiMethod +
                                                   "(hiByte, i1, i2, l1, l2))");
      }

      if (next != null && next.usefulEpsilonMoves > 0)
      {
         int[] stateNames = (int[])allNextStates.get(
                                          next.epsilonMovesString);
         if (next.usefulEpsilonMoves == 1)
         {
            int name = stateNames[0];
            if (nextIntersects)
               ostr.println(prefix + "                  jjCheckNAdd(" + name + ");");
            else
               ostr.println(prefix + "                  jjstateSet[jjnewStateCnt++] = " + name + ";");
         }
         else if (next.usefulEpsilonMoves == 2 && nextIntersects)
         {
            ostr.println(prefix + "                  jjCheckNAddTwoStates(" +
               stateNames[0] + ", " + stateNames[1] + ");");
         }
         else
         {
            int[] indices = GetStateSetIndicesForUse(next.epsilonMovesString);
            boolean notTwo = (indices[0] + 1 != indices[1]);

            if (nextIntersects)
               ostr.println(prefix + "                  jjCheckNAddStates(" +
                  indices[0] + (notTwo  ? (", " + indices[1]) : "") + ");");
            else
               ostr.println(prefix + "                  jjAddStates(" +
                                     indices[0] + ", " + indices[1] + ");");
         }
      }

      ostr.println("                  break;");
   }

   public static void DumpCharAndRangeMoves(java.io.PrintWriter ostr)
   {
      boolean[] dumped = new boolean[Math.max(generatedStates, dummyStateIndex + 1)];
      Enumeration e = compositeStateTable.keys();
      int i;

      DumpHeadForCase(ostr, -1);

      while (e.hasMoreElements())
         DumpCompositeStatesNonAsciiMoves(ostr, (String)e.nextElement(), dumped);

      for (i = 0; i < allStates.size(); i++)
      {
         NfaState temp = (NfaState)allStates.elementAt(i);

         if (dumped[temp.stateName] || temp.lexState != LexGen.lexStateIndex ||
             !temp.HasTransitions() || temp.dummy || temp.stateName == -1)
            continue;

         String toPrint = "";

         if (temp.stateForCase != null)
         {
            if (temp.inNextOf == 1)
               continue;

            if (dumped[temp.stateForCase.stateName])
               continue;

            toPrint = (temp.stateForCase.PrintNoBreak(ostr, -1, dumped));

            if (temp.nonAsciiMethod == -1)
            {
               if (toPrint.equals(""))
                  ostr.println("                  break;");

               continue;
            }
         }

         if (temp.nonAsciiMethod == -1)
            continue;

         if (!toPrint.equals(""))
            ostr.print(toPrint);

         dumped[temp.stateName] = true;
         //System.out.println("case : " + temp.stateName);
         ostr.println("               case " + temp.stateName + ":");
         temp.DumpNonAsciiMove(ostr, dumped);
      }

      ostr.println("               default : break;");
      ostr.println("            }");
      ostr.println("         } while(i != startsAt);");
   }

   public static void DumpNonAsciiMoveMethods(java.io.PrintWriter ostr)
   {
      if (!Options.getJavaUnicodeEscape() && !unicodeWarningGiven)
         return;

      if (nonAsciiTableForMethod.size() <= 0)
         return;

      for (int i = 0; i < nonAsciiTableForMethod.size(); i++)
      {
         NfaState tmp = (NfaState)nonAsciiTableForMethod.elementAt(i);
         tmp.DumpNonAsciiMoveMethod(ostr);
      }
   }

   void DumpNonAsciiMoveMethod(java.io.PrintWriter ostr)
   {
      int j;
      ostr.println("private static final boolean jjCanMove_" + nonAsciiMethod +
                       "(int hiByte, int i1, int i2, long l1, long l2)");
      ostr.println("{");
      ostr.println("   switch(hiByte)");
      ostr.println("   {");

      if (loByteVec != null && loByteVec.size() > 0)
      {
         for (j = 0; j < loByteVec.size(); j += 2)
         {
            ostr.println("      case " +
                         ((Integer)loByteVec.elementAt(j)).intValue() + ":");
            if (!AllBitsSet((String)allBitVectors.elementAt(
                 ((Integer)loByteVec.elementAt(j + 1)).intValue())))
            {
               ostr.println("         return ((jjbitVec" +
                ((Integer)loByteVec.elementAt(j + 1)).intValue() + "[i2" +
                   "] & l2) != 0L);");
            }
            else
               ostr.println("            return true;");
         }
      }

      ostr.println("      default : ");

      if (nonAsciiMoveIndices != null &&
          (j = nonAsciiMoveIndices.length) > 0)
      {
         do
         {
            if (!AllBitsSet((String)allBitVectors.elementAt(
                               nonAsciiMoveIndices[j - 2])))
               ostr.println("         if ((jjbitVec" + nonAsciiMoveIndices[j - 2] +
                            "[i1] & l1) != 0L)");
            if (!AllBitsSet((String)allBitVectors.elementAt(
                               nonAsciiMoveIndices[j - 1])))
            {
               ostr.println("            if ((jjbitVec" + nonAsciiMoveIndices[j - 1] +
                            "[i2] & l2) == 0L)");
               ostr.println("               return false;");
               ostr.println("            else");
            }
            ostr.println("            return true;");
         }
         while ((j -= 2) > 0);
      }

      ostr.println("         return false;");
      ostr.println("   }");
      ostr.println("}");
   }

   private static void ReArrange()
   {
      Vector v = allStates;
      allStates = new Vector();
      allStates.setSize(generatedStates);

      for (int j = 0; j < v.size(); j++)
      {
         NfaState tmp = (NfaState)v.elementAt(j);
         if (tmp.stateName != -1 && !tmp.dummy)
            allStates.setElementAt(tmp, tmp.stateName);
      }
   }

   private static boolean boilerPlateDumped = false;
   static void PrintBoilerPlate(java.io.PrintWriter ostr)
   {
      ostr.println((Options.getStatic() ? "static " : "") + "private final void " +
                    "jjCheckNAdd(int state)");
      ostr.println("{");
      ostr.println("   if (jjrounds[state] != jjround)");
      ostr.println("   {");
      ostr.println("      jjstateSet[jjnewStateCnt++] = state;");
      ostr.println("      jjrounds[state] = jjround;");
      ostr.println("   }");
      ostr.println("}");

      ostr.println((Options.getStatic() ? "static " : "") + "private final void " +
                    "jjAddStates(int start, int end)");
      ostr.println("{");
      ostr.println("   do {");
      ostr.println("      jjstateSet[jjnewStateCnt++] = jjnextStates[start];");
      ostr.println("   } while (start++ != end);");
      ostr.println("}");

      ostr.println((Options.getStatic() ? "static " : "") + "private final void " +
                    "jjCheckNAddTwoStates(int state1, int state2)");
      ostr.println("{");
      ostr.println("   jjCheckNAdd(state1);");
      ostr.println("   jjCheckNAdd(state2);");
      ostr.println("}");

      ostr.println((Options.getStatic() ? "static " : "") + "private final void " +
                    "jjCheckNAddStates(int start, int end)");
      ostr.println("{");
      ostr.println("   do {");
      ostr.println("      jjCheckNAdd(jjnextStates[start]);");
      ostr.println("   } while (start++ != end);");
      ostr.println("}");

      ostr.println((Options.getStatic() ? "static " : "") + "private final void " +
                    "jjCheckNAddStates(int start)");
      ostr.println("{");
      ostr.println("   jjCheckNAdd(jjnextStates[start]);");
      ostr.println("   jjCheckNAdd(jjnextStates[start + 1]);");
      ostr.println("}");
   }

   private static void FindStatesWithNoBreak()
   {
      Hashtable printed = new Hashtable();
      boolean[] put = new boolean[generatedStates];
      int cnt = 0;
      int i, j, foundAt = 0;

      Outer :
      for (j = 0; j < allStates.size(); j++)
      {
         NfaState stateForCase = null;
         NfaState tmpState = (NfaState)allStates.elementAt(j);

         if (tmpState.stateName == -1 || tmpState.dummy || !tmpState.UsefulState() ||
             tmpState.next == null || tmpState.next.usefulEpsilonMoves < 1)
            continue;

         String s = tmpState.next.epsilonMovesString;

         if (compositeStateTable.get(s) != null || printed.get(s) != null)
            continue;

         printed.put(s, s);
         int[] nexts = (int[])allNextStates.get(s);

         if (nexts.length == 1)
            continue;

         int state = cnt;
         //System.out.println("State " + tmpState.stateName + " : " + s);
         for (i = 0; i < nexts.length; i++)
         {
            if ((state = nexts[i]) == -1)
               continue;

            NfaState tmp = (NfaState)allStates.elementAt(state);

            if (!tmp.isComposite && tmp.inNextOf == 1)
            {
               if (put[state])
                  throw new Error("JavaCC Bug: Please send mail to sankar@cs.stanford.edu");

               foundAt = i;
               cnt++;
               stateForCase = tmp;
               put[state] = true;

               //System.out.print(state + " : " + tmp.inNextOf + ", ");
               break;
            }
         }
         //System.out.println("");

         if (stateForCase == null)
            continue;

         for (i = 0; i < nexts.length; i++)
         {
            if ((state = nexts[i]) == -1)
               continue;

            NfaState tmp = (NfaState)allStates.elementAt(state);

            if (!put[state] && tmp.inNextOf > 1 && !tmp.isComposite && tmp.stateForCase == null)
            {
               cnt++;
               nexts[i] = -1;
               put[state] = true;

               int toSwap = nexts[0];
               nexts[0] = nexts[foundAt];
               nexts[foundAt] = toSwap;

               tmp.stateForCase = stateForCase;
               stateForCase.stateForCase = tmp;
               stateSetsToFix.put(s, nexts);

               //System.out.println("For : " + s + "; " + stateForCase.stateName +
                         //" and " + tmp.stateName);

               continue Outer;
            }
         }

         for (i = 0; i < nexts.length; i++)
         {
            if ((state = nexts[i]) == -1)
               continue;

            NfaState tmp = (NfaState)allStates.elementAt(state);
            if (tmp.inNextOf <= 1)
               put[state] = false;
         }
      }
   }

   static int[][] kinds;
   static int[][][] statesForState;
   public static void DumpMoveNfa(java.io.PrintWriter ostr)
   {
      if (!boilerPlateDumped)
         PrintBoilerPlate(ostr);

      boilerPlateDumped = true;
      int i;
      int[] kindsForStates = null;

      if (kinds == null)
      {
         kinds = new int[LexGen.maxLexStates][];
         statesForState = new int[LexGen.maxLexStates][][];
      }

      ReArrange();

      for (i = 0; i < allStates.size(); i++)
      {
         NfaState temp = (NfaState)allStates.elementAt(i);

         if (temp.lexState != LexGen.lexStateIndex ||
             !temp.HasTransitions() || temp.dummy ||
             temp.stateName == -1)
            continue;

         if (kindsForStates == null)
         {
            kindsForStates = new int[generatedStates];
            statesForState[LexGen.lexStateIndex] = new int[Math.max(generatedStates, dummyStateIndex + 1)][];
         }

         kindsForStates[temp.stateName] = temp.lookingFor;
         statesForState[LexGen.lexStateIndex][temp.stateName] = temp.compositeStates;

         temp.GenerateNonAsciiMoves(ostr);
      }

      Enumeration e = stateNameForComposite.keys();

      while (e.hasMoreElements())
      {
         String s = (String)e.nextElement();
         int state = ((Integer)stateNameForComposite.get(s)).intValue();

         if (state >= generatedStates)
            statesForState[LexGen.lexStateIndex][state] = (int[])allNextStates.get(s);
      }

      if (stateSetsToFix.size() != 0)
         FixStateSets();

      kinds[LexGen.lexStateIndex] = kindsForStates;

      ostr.println((Options.getStatic() ? "static " : "") + "private final int " +
                    "jjMoveNfa" + LexGen.lexStateSuffix + "(int startState, int curPos)");
      ostr.println("{");

      if (generatedStates == 0)
      {
         ostr.println("   return curPos;");
         ostr.println("}");
         return;
      }

      if (LexGen.mixed[LexGen.lexStateIndex])
      {
         ostr.println("   int strKind = jjmatchedKind;");
         ostr.println("   int strPos = jjmatchedPos;");
         ostr.println("   int seenUpto;");
         ostr.println("   input_stream.backup(seenUpto = curPos + 1);");
         ostr.println("   try { curChar = input_stream.readChar(); }");
         ostr.println("   catch(java.io.IOException e) { throw new Error(\"Internal Error\"); }");
         ostr.println("   curPos = 0;");
      }

      ostr.println("   int[] nextStates;");
      ostr.println("   int startsAt = 0;");
      ostr.println("   jjnewStateCnt = " + generatedStates + ";");
      ostr.println("   int i = 1;");
      ostr.println("   jjstateSet[0] = startState;");

      if (Options.getDebugTokenManager())
         ostr.println("      debugStream.println(\"   Starting NFA to match one of : \" + " +
                 "jjKindsForStateVector(curLexState, jjstateSet, 0, 1));");

      if (Options.getDebugTokenManager())
         ostr.println("      debugStream.println(" + (LexGen.maxLexStates > 1 ? "\"<\" + lexStateNames[curLexState] + \">\" + " : "") + "\"Current character : \" + " +
                 "TokenMgrError.addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \") at line \" + input_stream.getLine() + \" column \" + input_stream.getColumn());");

      ostr.println("   int j, kind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");
      ostr.println("   for (;;)");
      ostr.println("   {");
      ostr.println("      if (++jjround == 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
      ostr.println("         ReInitRounds();");
      ostr.println("      if (curChar < 64)");
      ostr.println("      {");

      DumpAsciiMoves(ostr, 0);

      ostr.println("      }");

      ostr.println("      else if (curChar < 128)");

      ostr.println("      {");

      DumpAsciiMoves(ostr, 1);

      ostr.println("      }");

      ostr.println("      else");
      ostr.println("      {");

      DumpCharAndRangeMoves(ostr);

      ostr.println("      }");

      ostr.println("      if (kind != 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
      ostr.println("      {");
      ostr.println("         jjmatchedKind = kind;");
      ostr.println("         jjmatchedPos = curPos;");
      ostr.println("         kind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");
      ostr.println("      }");
      ostr.println("      ++curPos;");

      if (Options.getDebugTokenManager())
      {
         ostr.println("      if (jjmatchedKind != 0 && jjmatchedKind != 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
         ostr.println("         debugStream.println(\"   Currently matched the first \" + (jjmatchedPos + 1) + \" characters as a \" + tokenImage[jjmatchedKind] + \" token.\");");
      }

      ostr.println("      if ((i = jjnewStateCnt) == (startsAt = " +
                   generatedStates + " - (jjnewStateCnt = startsAt)))");
      if (LexGen.mixed[LexGen.lexStateIndex])
         ostr.println("         break;");
      else
         ostr.println("         return curPos;");

      if (Options.getDebugTokenManager())
         ostr.println("      debugStream.println(\"   Possible kinds of longer matches : \" + " +
                 "jjKindsForStateVector(curLexState, jjstateSet, startsAt, i));");

      ostr.println("      try { curChar = input_stream.readChar(); }");

      if (LexGen.mixed[LexGen.lexStateIndex])
         ostr.println("      catch(java.io.IOException e) { break; }");
      else
         ostr.println("      catch(java.io.IOException e) { return curPos; }");

      if (Options.getDebugTokenManager())
         ostr.println("      debugStream.println(" + (LexGen.maxLexStates > 1 ? "\"<\" + lexStateNames[curLexState] + \">\" + " : "") + "\"Current character : \" + " +
                 "TokenMgrError.addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \") at line \" + input_stream.getLine() + \" column \" + input_stream.getColumn());");

      ostr.println("   }");

      if (LexGen.mixed[LexGen.lexStateIndex])
      {
         ostr.println("   if (jjmatchedPos > strPos)");
         ostr.println("      return curPos;");
         ostr.println("");
         ostr.println("   int toRet = Math.max(curPos, seenUpto);");
         ostr.println("");
         ostr.println("   if (curPos < toRet)");
         ostr.println("      for (i = toRet - Math.min(curPos, seenUpto); i-- > 0; )");
         ostr.println("         try { curChar = input_stream.readChar(); }");
         ostr.println("         catch(java.io.IOException e) { throw new Error(\"Internal Error : Please send a bug report.\"); }");
         ostr.println("");
         ostr.println("   if (jjmatchedPos < strPos)");
         ostr.println("   {");
         ostr.println("      jjmatchedKind = strKind;");
         ostr.println("      jjmatchedPos = strPos;");
         ostr.println("   }");
         ostr.println("   else if (jjmatchedPos == strPos && jjmatchedKind > strKind)");
         ostr.println("      jjmatchedKind = strKind;");
         ostr.println("");
         ostr.println("   return toRet;");
      }

      ostr.println("}");
      allStates.removeAllElements();
   }

   public static void DumpStatesForState(java.io.PrintWriter ostr)
   {
      ostr.print("protected static final int[][][] statesForState = ");

      if (statesForState == null)
      {
         ostr.println("null;");
         return;
      }
      else
         ostr.println("{");

      for (int i = 0; i < statesForState.length; i++)
      {

       if (statesForState[i] == null)
       {
          ostr.println(" null, ");
          continue;
       }

       ostr.println(" {");

       for (int j = 0; j < statesForState[i].length; j++)
       {
         int[] stateSet = statesForState[i][j];

         if (stateSet == null)
         {
            ostr.println("   { " + j + " }, ");
            continue;
         }

         ostr.print("   { ");

         for (int k = 0; k < stateSet.length; k++)
            ostr.print(stateSet[k] + ", ");

         ostr.println("},");
       }
       ostr.println(" },");
      }
      ostr.println("\n};");
   }

   public static void DumpStatesForKind(java.io.PrintWriter ostr)
   {
      DumpStatesForState(ostr);
      boolean moreThanOne = false;
      int cnt = 0;

      ostr.print("protected static final int[][] kindForState = ");

      if (kinds == null)
      {
         ostr.println("null;");
         return;
      }
      else
         ostr.println("{");

      for (int i = 0; i < kinds.length; i++)
      {
         if (moreThanOne)
            ostr.println(", ");
         moreThanOne = true;

         if (kinds[i] == null)
            ostr.println("null");
         else
         {
            cnt = 0;
            ostr.print("{ ");
            for (int j = 0; j < kinds[i].length; j++)
            {
               if (cnt++ > 0)
                  ostr.print(", ");

               if (cnt % 15 == 0)
                  ostr.print("\n  ");

               ostr.print(kinds[i][j]);
            }
            ostr.print("}");
         }
      }
      ostr.println("\n};");
   }

   public static void reInit()
   {
      unicodeWarningGiven = false;
      generatedStates = 0;
      idCnt = 0;
      lohiByteCnt = 0;
      dummyStateIndex = -1;
      done = false;
      mark = null;
      stateDone = null;
      nonAsciiIntersections = new boolean[20][20];
      allStates = new Vector();
      indexedAllStates = new Vector();
      nonAsciiTableForMethod = new Vector();
      equivStatesTable = new Hashtable();
      allNextStates = new Hashtable();
      lohiByteTab = new Hashtable();
      stateNameForComposite = new Hashtable();
      compositeStateTable = new Hashtable();
      stateBlockTable = new Hashtable();
      stateSetsToFix = new Hashtable();
      allBitVectors = new Vector();
      tmpIndices = new int[512];
      allBits = "{\n   0xffffffffffffffffL, " +
                    "0xffffffffffffffffL, " +
                    "0xffffffffffffffffL, " +
                    "0xffffffffffffffffL\n};";
      tableToDump = new Hashtable();
      orderedStateSet = new Vector();
      lastIndex = 0;
      boilerPlateDumped = false;
      kinds = null;
      statesForState = null;
   }

}
