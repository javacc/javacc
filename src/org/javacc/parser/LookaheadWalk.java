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

import java.util.List;
import java.util.Vector;

public final class LookaheadWalk {

  public static boolean considerSemanticLA;

  public static Vector sizeLimitedMatches;

  private LookaheadWalk() {}

  public static void vectorAppend(Vector vToAppendTo, Vector vToAppend) {
    for (int i = 0; i < vToAppend.size(); i++) {
      vToAppendTo.addElement(vToAppend.elementAt(i));
    }
  }

  public static Vector genFirstSet(Vector partialMatches, Expansion exp) {
    if (exp instanceof RegularExpression) {
      Vector retval = new Vector();
      for (int i = 0; i < partialMatches.size(); i++) {
        MatchInfo m = (MatchInfo)partialMatches.elementAt(i);
        MatchInfo mnew = new MatchInfo();
        for (int j = 0; j < m.firstFreeLoc; j++) {
          mnew.match[j] = m.match[j];
        }
        mnew.firstFreeLoc = m.firstFreeLoc;
        mnew.match[mnew.firstFreeLoc++] = ((RegularExpression)exp).ordinal;
        if (mnew.firstFreeLoc == MatchInfo.laLimit) {
          sizeLimitedMatches.addElement(mnew);
        } else {
          retval.addElement(mnew);
        }
      }
      return retval;
    } else if (exp instanceof NonTerminal) {
      NormalProduction prod = ((NonTerminal)exp).prod;
      if (prod instanceof JavaCodeProduction) {
        return new Vector();
      } else {
        return genFirstSet(partialMatches, prod.expansion);
      }
    } else if (exp instanceof Choice) {
      Vector retval = new Vector();
      Choice ch = (Choice)exp;
      for (int i = 0; i < ch.choices.size(); i++) {
        Vector v = genFirstSet(partialMatches, (Expansion)ch.choices.elementAt(i));
        vectorAppend(retval, v);
      }
      return retval;
    } else if (exp instanceof Sequence) {
      Vector v = partialMatches;
      Sequence seq = (Sequence)exp;
      for (int i = 0; i < seq.units.size(); i++) {
        v = genFirstSet(v, (Expansion)seq.units.elementAt(i));
        if (v.size() == 0) break;
      }
      return v;
    } else if (exp instanceof OneOrMore) {
      Vector retval = new Vector();
      Vector v = partialMatches;
      OneOrMore om = (OneOrMore)exp;
      while (true) {
        v = genFirstSet(v, om.expansion);
        if (v.size() == 0) break;
        vectorAppend(retval, v);
      }
      return retval;
    } else if (exp instanceof ZeroOrMore) {
      Vector retval = new Vector();
      vectorAppend(retval, partialMatches);
      Vector v = partialMatches;
      ZeroOrMore zm = (ZeroOrMore)exp;
      while (true) {
        v = genFirstSet(v, zm.expansion);
        if (v.size() == 0) break;
        vectorAppend(retval, v);
      }
      return retval;
    } else if (exp instanceof ZeroOrOne) {
      Vector retval = new Vector();
      vectorAppend(retval, partialMatches);
      vectorAppend(retval, genFirstSet(partialMatches, ((ZeroOrOne)exp).expansion));
      return retval;
    } else if (exp instanceof TryBlock) {
      return genFirstSet(partialMatches, ((TryBlock)exp).exp);
    } else if (considerSemanticLA &&
               exp instanceof Lookahead &&
               ((Lookahead)exp).action_tokens.size() != 0
              ) {
      return new Vector();
    } else {
      Vector retval = new Vector();
      vectorAppend(retval, partialMatches);
      return retval;
    }
  }

  public static void vectorSplit(Vector toSplit, Vector mask, Vector partInMask, Vector rest) {
    OuterLoop:
    for (int i = 0; i < toSplit.size(); i++) {
      for (int j = 0; j < mask.size(); j++) {
        if (toSplit.elementAt(i) == mask.elementAt(j)) {
          partInMask.addElement(toSplit.elementAt(i));
          continue OuterLoop;
        }
      }
      rest.addElement(toSplit.elementAt(i));
    }
  }

  public static Vector genFollowSet(Vector partialMatches, Expansion exp, long generation) {
    if (exp.myGeneration == generation) {
      return new Vector();
    }
//System.out.println("*** Parent: " + exp.parent);
    exp.myGeneration = generation;
    if (exp.parent == null) {
      Vector retval = new Vector();
      vectorAppend(retval, partialMatches);
      return retval;
    } else if (exp.parent instanceof NormalProduction) {
      List parents = ((NormalProduction)exp.parent).parents;
      Vector retval = new Vector();
//System.out.println("1; gen: " + generation + "; exp: " + exp);
      for (int i = 0; i < parents.size(); i++) {
        Vector v = genFollowSet(partialMatches, (Expansion)parents.get(i), generation);
        vectorAppend(retval, v);
      }
      return retval;
    } else if (exp.parent instanceof Sequence) {
      Sequence seq = (Sequence)exp.parent;
      Vector v = partialMatches;
      for (int i = exp.ordinal+1; i < seq.units.size(); i++) {
        v = genFirstSet(v, (Expansion)seq.units.elementAt(i));
        if (v.size() == 0) return v;
      }
      Vector v1 = new Vector();
      Vector v2 = new Vector();
      vectorSplit(v, partialMatches, v1, v2);
      if (v1.size() != 0) {
//System.out.println("2; gen: " + generation + "; exp: " + exp);
        v1 = genFollowSet(v1, seq, generation);
      }
      if (v2.size() != 0) {
//System.out.println("3; gen: " + generation + "; exp: " + exp);
        v2 = genFollowSet(v2, seq, Expansion.nextGenerationIndex++);
      }
      vectorAppend(v2, v1);
      return v2;
    } else if (exp.parent instanceof OneOrMore || exp.parent instanceof ZeroOrMore) {
      Vector moreMatches = new Vector();
      vectorAppend(moreMatches, partialMatches);
      Vector v = partialMatches;
      while (true) {
        v = genFirstSet(v, exp);
        if (v.size() == 0) break;
        vectorAppend(moreMatches, v);
      }
      Vector v1 = new Vector();
      Vector v2 = new Vector();
      vectorSplit(moreMatches, partialMatches, v1, v2);
      if (v1.size() != 0) {
//System.out.println("4; gen: " + generation + "; exp: " + exp);
        v1 = genFollowSet(v1, (Expansion)exp.parent, generation);
      }
      if (v2.size() != 0) {
//System.out.println("5; gen: " + generation + "; exp: " + exp);
        v2 = genFollowSet(v2, (Expansion)exp.parent, Expansion.nextGenerationIndex++);
      }
      vectorAppend(v2, v1);
      return v2;
    } else {
//System.out.println("6; gen: " + generation + "; exp: " + exp);
      return genFollowSet(partialMatches, (Expansion)exp.parent, generation);
    }
  }

   public static void reInit()
   {
      considerSemanticLA = false;
      sizeLimitedMatches = null;
   }

}
