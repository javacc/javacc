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

import java.util.Vector;

public class LookaheadWalk {

  public static boolean considerSemanticLA;

  public static Vector sizeLimitedMatches;

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
    exp.myGeneration = generation;
    if (exp.parent == null) {
      Vector retval = new Vector();
      vectorAppend(retval, partialMatches);
      return retval;
    } else if (exp.parent instanceof NormalProduction) {
      Vector parents = ((NormalProduction)exp.parent).parents;
      Vector retval = new Vector();
      for (int i = 0; i < parents.size(); i++) {
        Vector v = genFollowSet(partialMatches, (Expansion)parents.elementAt(i), generation);
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
        v1 = genFollowSet(v1, seq, generation);
      }
      if (v2.size() != 0) {
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
        v1 = genFollowSet(v1, (Expansion)exp.parent, generation);
      }
      if (v2.size() != 0) {
        v2 = genFollowSet(v2, (Expansion)exp.parent, Expansion.nextGenerationIndex++);
      }
      vectorAppend(v2, v1);
      return v2;
    } else {
      return genFollowSet(partialMatches, (Expansion)exp.parent, generation);
    }
  }

   public static void reInit()
   {
      considerSemanticLA = false;
      sizeLimitedMatches = null;
   }

}
