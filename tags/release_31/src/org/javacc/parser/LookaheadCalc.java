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

public class LookaheadCalc extends JavaCCGlobals {

  static MatchInfo overlap(Vector v1, Vector v2) {
    MatchInfo m1, m2, m3;
    int size;
    boolean diff;
    for (int i = 0; i < v1.size(); i++) {
      m1 = (MatchInfo)v1.elementAt(i);
      for (int j = 0; j < v2.size(); j++) {
        m2 = (MatchInfo)v2.elementAt(j);
        size = m1.firstFreeLoc; m3 = m1;
        if (size > m2.firstFreeLoc) {
          size = m2.firstFreeLoc; m3 = m2;
        }
        if (size == 0) return null;
        // we wish to ignore empty expansions and the JAVACODE stuff here.
        diff = false;
        for (int k = 0; k < size; k++) {
          if (m1.match[k] != m2.match[k]) {
            diff = true;
            break;
          }
        }
        if (!diff) return m3;
      }
    }
    return null;
  }

  static boolean javaCodeCheck(Vector v) {
    for (int i = 0; i < v.size(); i++) {
      if (((MatchInfo)v.elementAt(i)).firstFreeLoc == 0) {
        return true;
      }
    }
    return false;
  }

  static String image(MatchInfo m) {
    String ret = "";
    for (int i = 0; i < m.firstFreeLoc; i++) {
      if (m.match[i] == 0) {
        ret += " <EOF>";
      } else {
        RegularExpression re = (RegularExpression)rexps_of_tokens.get(new Integer(m.match[i]));
        if (re instanceof RStringLiteral) {
          ret += " \"" + add_escapes(((RStringLiteral)re).image) + "\"";
        } else if (!re.label.equals("")) {
          ret += " <" + re.label + ">";
        } else {
          ret += " <token of kind " + i + ">";
        }
      }
    }
    if (m.firstFreeLoc == 0) {
      return "";
    } else {
      return ret.substring(1);
    }
  }

  public static void choiceCalc(Choice ch) {
    int first = firstChoice(ch);
    // dbl[i] and dbr[i] are vectors of size limited matches for choice i
    // of ch.  dbl ignores matches with semantic lookaheads (when force_la_check
    // is false), while dbr ignores semantic lookahead.
    Vector[] dbl = new Vector[ch.choices.size()];
    Vector[] dbr = new Vector[ch.choices.size()];
    int[] minLA = new int[ch.choices.size()-1];
    MatchInfo[] overlapInfo = new MatchInfo[ch.choices.size()-1];
    int[] other = new int[ch.choices.size()-1];
    MatchInfo m;
    Vector v;
    boolean overlapDetected;
    for (int la = 1; la <= Options.I("CHOICE_AMBIGUITY_CHECK"); la++) {
      MatchInfo.laLimit = la;
      LookaheadWalk.considerSemanticLA = !Options.B("FORCE_LA_CHECK");
      for (int i = first; i < ch.choices.size()-1; i++) {
        LookaheadWalk.sizeLimitedMatches = new java.util.Vector();
        m = new MatchInfo();
        m.firstFreeLoc = 0;
        v = new java.util.Vector();
        v.addElement(m);
        LookaheadWalk.genFirstSet(v, (Expansion)ch.choices.elementAt(i));
        dbl[i] = LookaheadWalk.sizeLimitedMatches;
      }      
      LookaheadWalk.considerSemanticLA = false;
      for (int i = first+1; i < ch.choices.size(); i++) {
        LookaheadWalk.sizeLimitedMatches = new java.util.Vector();
        m = new MatchInfo();
        m.firstFreeLoc = 0;
        v = new java.util.Vector();
        v.addElement(m);
        LookaheadWalk.genFirstSet(v, (Expansion)ch.choices.elementAt(i));
        dbr[i] = LookaheadWalk.sizeLimitedMatches;
      }      
      if (la == 1) {
        for (int i = first; i < ch.choices.size()-1; i++) {
          Expansion exp = (Expansion)ch.choices.elementAt(i);
          if (Semanticize.emptyExpansionExists(exp)) {
            JavaCCErrors.warning(exp, "This choice can expand to the empty token sequence and will therefore always be taken in favor of the choices appearing later.");
            break;
          } else if (javaCodeCheck(dbl[i])) {
            JavaCCErrors.warning(exp, "JAVACODE non-terminal will force this choice to be taken in favor of the choices appearing later.");
            break;
          }
        }
      }
      overlapDetected = false;
      for (int i = first; i < ch.choices.size()-1; i++) {
        for (int j = i+1; j < ch.choices.size(); j++) {
          if ((m = overlap(dbl[i], dbr[j])) != null) {
            minLA[i] = la+1;
            overlapInfo[i] = m;
            other[i] = j;
            overlapDetected = true;
            break;
          }
        }
      }
      if (!overlapDetected) {
        break;
      }
    }
    for (int i = first; i < ch.choices.size()-1; i++) {
      if (explicitLA((Expansion)ch.choices.elementAt(i)) && !Options.B("FORCE_LA_CHECK")) {
        continue;
      }
      if (minLA[i] > Options.I("CHOICE_AMBIGUITY_CHECK")) {
        JavaCCErrors.warning("Choice conflict involving two expansions at");
        System.err.print("         line " + ((Expansion)ch.choices.elementAt(i)).line);
        System.err.print(", column " + ((Expansion)ch.choices.elementAt(i)).column);
        System.err.print(" and line " + ((Expansion)ch.choices.elementAt(other[i])).line);
        System.err.print(", column " + ((Expansion)ch.choices.elementAt(other[i])).column);
        System.err.println(" respectively.");
        System.err.println("         A common prefix is: " + image(overlapInfo[i]));
        System.err.println("         Consider using a lookahead of " + minLA[i] + " or more for earlier expansion.");
      } else if (minLA[i] > 1) {
        JavaCCErrors.warning("Choice conflict involving two expansions at");
        System.err.print("         line " + ((Expansion)ch.choices.elementAt(i)).line);
        System.err.print(", column " + ((Expansion)ch.choices.elementAt(i)).column);
        System.err.print(" and line " + ((Expansion)ch.choices.elementAt(other[i])).line);
        System.err.print(", column " + ((Expansion)ch.choices.elementAt(other[i])).column);
        System.err.println(" respectively.");
        System.err.println("         A common prefix is: " + image(overlapInfo[i]));
        System.err.println("         Consider using a lookahead of " + minLA[i] + " for earlier expansion.");
      }
    }
  }

  static boolean explicitLA(Expansion exp) {
    if (!(exp instanceof Sequence)) {
      return false;
    }
    Sequence seq = (Sequence)exp;
    Object obj = seq.units.elementAt(0);
    if (!(obj instanceof Lookahead)) {
      return false;
    }
    Lookahead la = (Lookahead)obj;
    return la.isExplicit;
  }

  static int firstChoice(Choice ch) {
    if (Options.B("FORCE_LA_CHECK")) {
      return 0;
    }
    for (int i = 0; i < ch.choices.size(); i++) {
      if (!explicitLA((Expansion)ch.choices.elementAt(i))) {
        return i;
      }
    }
    return ch.choices.size();
  }

  private static String image(Expansion exp) {
    if (exp instanceof OneOrMore) {
      return "(...)+";
    } else if (exp instanceof ZeroOrMore) {
      return "(...)*";
    } else /* if (exp instanceof ZeroOrOne) */ {
      return "[...]";
    }
  }

  public static void ebnfCalc(Expansion exp, Expansion nested) {
    // exp is one of OneOrMore, ZeroOrMore, ZeroOrOne
    MatchInfo m, m1 = null;
    Vector v, first, follow;
    int la;
    for (la = 1; la <= Options.I("OTHER_AMBIGUITY_CHECK"); la++) {
      MatchInfo.laLimit = la;
      LookaheadWalk.sizeLimitedMatches = new java.util.Vector();
      m = new MatchInfo();
      m.firstFreeLoc = 0;
      v = new java.util.Vector();
      v.addElement(m);
      LookaheadWalk.considerSemanticLA = !Options.B("FORCE_LA_CHECK");
      LookaheadWalk.genFirstSet(v, nested);
      first = LookaheadWalk.sizeLimitedMatches;
      LookaheadWalk.sizeLimitedMatches = new java.util.Vector();
      LookaheadWalk.considerSemanticLA = false;
      LookaheadWalk.genFollowSet(v, exp, Expansion.nextGenerationIndex++);
      follow = LookaheadWalk.sizeLimitedMatches;
      if (la == 1) {
        if (javaCodeCheck(first)) {
          JavaCCErrors.warning(nested, "JAVACODE non-terminal within " + image(exp) + " construct will force this construct to be entered in favor of expansions occurring after construct.");
        }
      }
      if ((m = overlap(first, follow)) == null) {
        break;
      }
      m1 = m;
    }
    if (la > Options.I("OTHER_AMBIGUITY_CHECK")) {
      JavaCCErrors.warning("Choice conflict in " + image(exp) + " construct at line " + exp.line + ", column " + exp.column + ".");
      System.err.println("         Expansion nested within construct and expansion following construct");
      System.err.println("         have common prefixes, one of which is: " + image(m1));
      System.err.println("         Consider using a lookahead of " + la + " or more for nested expansion.");
    } else if (la > 1) {
      JavaCCErrors.warning("Choice conflict in " + image(exp) + " construct at line " + exp.line + ", column " + exp.column + ".");
      System.err.println("         Expansion nested within construct and expansion following construct");
      System.err.println("         have common prefixes, one of which is: " + image(m1));
      System.err.println("         Consider using a lookahead of " + la + " for nested expansion.");
    }
  }

}
