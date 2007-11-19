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

import java.util.HashSet;
import java.util.Hashtable;

public class ParseEngine extends JavaCCGlobals {

  static private java.io.PrintWriter ostr;
  static private int gensymindex = 0;
  static private int indentamt;
  static private boolean jj2LA;

  /**
   * These lists are used to maintain expansions for which code generation
   * in phase 2 and phase 3 is required.  Whenever a call is generated to
   * a phase 2 or phase 3 routine, a corresponding entry is added here if
   * it has not already been added.
   * The phase 3 routines have been optimized in version 0.7pre2.  Essentially
   * only those methods (and only those portions of these methods) are
   * generated that are required.  The lookahead amount is used to determine
   * this.  This change requires the use of a hash table because it is now
   * possible for the same phase 3 routine to be requested multiple times
   * with different lookaheads.  The hash table provides a easily searchable
   * capability to determine the previous requests.
   * The phase 3 routines now are performed in a two step process - the first
   * step gathers the requests (replacing requests with lower lookaheads with
   * those requiring larger lookaheads).  The second step then generates these
   * methods.
   * This optimization and the hashtable makes it look like we do not need
   * the flag "phase3done" any more.  But this has not been removed yet.
   */
  static private java.util.Vector phase2list = new java.util.Vector();
  static private java.util.Vector phase3list = new java.util.Vector();
  static private java.util.Hashtable phase3table = new java.util.Hashtable();

  /**
   * The phase 1 routines generates their output into String's and dumps
   * these String's once for each method.  These String's contain the
   * special characters '\u0001' to indicate a positive indent, and '\u0002'
   * to indicate a negative indent.  '\n' is used to indicate a line terminator.
   * The characters '\u0003' and '\u0004' are used to delineate portions of
   * text where '\n's should not be followed by an indentation.
   */

  /**
   * Returns true if there is a JAVACODE production that the argument expansion
   * may directly expand to (without consuming tokens or encountering lookahead).
   */
  static private boolean javaCodeCheck(Expansion exp) {
    if (exp instanceof RegularExpression) {
      return false;
    } else if (exp instanceof NonTerminal) {
      NormalProduction prod = ((NonTerminal)exp).prod;
      if (prod instanceof JavaCodeProduction) {
        return true;
      } else {
        return javaCodeCheck(prod.expansion);
      }
    } else if (exp instanceof Choice) {
      Choice ch = (Choice)exp;
      for (int i = 0; i < ch.choices.size(); i++) {
        if (javaCodeCheck((Expansion)(ch.choices.get(i)))) {
          return true;
        }
      }
      return false;
    } else if (exp instanceof Sequence) {
      Sequence seq = (Sequence)exp;
      for (int i = 0; i < seq.units.size(); i++) {
        Expansion[] units = (Expansion[])seq.units.toArray(new Expansion[seq.units.size()]);
        if (units[i] instanceof Lookahead && ((Lookahead)units[i]).isExplicit) {
          // An explicit lookahead (rather than one generated implicitly). Assume
          // the user knows what he / she is doing, e.g.
          //    "A" ( "B" | LOOKAHEAD("X") jcode() | "C" )* "D"
          return false;
        } else if (javaCodeCheck((units[i]))) {
          return true;
        } else if (!Semanticize.emptyExpansionExists(units[i])) {
          return false;
        }
      }
      return false;
    } else if (exp instanceof OneOrMore) {
      OneOrMore om = (OneOrMore)exp;
      return javaCodeCheck(om.expansion);
    } else if (exp instanceof ZeroOrMore) {
      ZeroOrMore zm = (ZeroOrMore)exp;
      return javaCodeCheck(zm.expansion);
    } else if (exp instanceof ZeroOrOne) {
      ZeroOrOne zo = (ZeroOrOne)exp;
      return javaCodeCheck(zo.expansion);
    } else if (exp instanceof TryBlock) {
      TryBlock tb = (TryBlock)exp;
      return javaCodeCheck(tb.exp);
    } else {
      return false;
    }
  }

  /**
   * An array used to store the first sets generated by the following method.
   * A true entry means that the corresponding token is in the first set.
   */
  static private boolean[] firstSet;

  /**
   * Sets up the array "firstSet" above based on the Expansion argument
   * passed to it.  Since this is a recursive function, it assumes that
   * "firstSet" has been reset before the first call.
   */
  static private void genFirstSet(Expansion exp) {
    if (exp instanceof RegularExpression) {
      firstSet[((RegularExpression)exp).ordinal] = true;
    } else if (exp instanceof NonTerminal) {
        if (!(((NonTerminal)exp).prod instanceof JavaCodeProduction))
        {
        	genFirstSet(((BNFProduction)(((NonTerminal)exp).prod)).expansion);
        }
    } else if (exp instanceof Choice) {
      Choice ch = (Choice)exp;
      for (int i = 0; i < ch.choices.size(); i++) {
        genFirstSet((Expansion)(ch.choices.elementAt(i)));
      }
    } else if (exp instanceof Sequence) {
      Sequence seq = (Sequence)exp;
      Object obj = seq.units.elementAt(0);
      if ((obj instanceof Lookahead) && (((Lookahead)obj).action_tokens.size() != 0)) {
        jj2LA = true;
      }
      for (int i = 0; i < seq.units.size(); i++) {
        Expansion unit = (Expansion) seq.units.elementAt(i);
	// Javacode productions can not have FIRST sets. Instead we generate the FIRST set
	// for the preceding LOOKAHEAD (the semantic checks should have made sure that
	// the LOOKAHEAD is suitable).
        if (unit instanceof NonTerminal && ((NonTerminal)unit).prod instanceof JavaCodeProduction) {
          if (i > 0 && seq.units.elementAt(i-1) instanceof Lookahead) {
            Lookahead la = (Lookahead)seq.units.elementAt(i-1);
            genFirstSet(la.la_expansion);
          }
        } else {
          genFirstSet((Expansion)(seq.units.elementAt(i)));
        }
        if (!Semanticize.emptyExpansionExists((Expansion)(seq.units.elementAt(i)))) {
          break;
        }
      }
    } else if (exp instanceof OneOrMore) {
      OneOrMore om = (OneOrMore)exp;
      genFirstSet(om.expansion);
    } else if (exp instanceof ZeroOrMore) {
      ZeroOrMore zm = (ZeroOrMore)exp;
      genFirstSet(zm.expansion);
    } else if (exp instanceof ZeroOrOne) {
      ZeroOrOne zo = (ZeroOrOne)exp;
      genFirstSet(zo.expansion);
    } else if (exp instanceof TryBlock) {
      TryBlock tb = (TryBlock)exp;
      genFirstSet(tb.exp);
    }
  }

  /**
   * Constants used in the following method "buildLookaheadChecker".
   */
  static final int NOOPENSTM = 0;
  static final int OPENIF = 1;
  static final int OPENSWITCH = 2;

  private static void dumpLookaheads(Lookahead[] conds, String[] actions) {
    for (int i = 0; i < conds.length; i++) {
      System.err.println("Lookahead: " + i);
      System.err.println(conds[i].dump(0, new HashSet()));
      System.err.println();
    }
  }
  
  /**
   * This method takes two parameters - an array of Lookahead's
   * "conds", and an array of String's "actions".  "actions" contains
   * exactly one element more than "conds".  "actions" are Java source
   * code, and "conds" translate to conditions - so lets say
   * "f(conds[i])" is true if the lookahead required by "conds[i]" is
   * indeed the case.  This method returns a string corresponding to
   * the Java code for:
   *
   *   if (f(conds[0]) actions[0]
   *   else if (f(conds[1]) actions[1]
   *   . . .
   *   else actions[action.length-1]
   *
   * A particular action entry ("actions[i]") can be null, in which
   * case, a noop is generated for that action.
   */
  static String buildLookaheadChecker(Lookahead[] conds, String[] actions) {
    
	  // The state variables.
    int state = NOOPENSTM;
    int indentAmt = 0;
    boolean[] casedValues = new boolean[tokenCount];
    String retval = "";
    Lookahead la;
    Token t = null;
    int tokenMaskSize = (tokenCount-1)/32 + 1;
    int[] tokenMask = null;

    // Iterate over all the conditions.
    int index = 0;
    while (index < conds.length) {

      la = conds[index];
      jj2LA = false;

      if ((la.amount == 0) ||
           Semanticize.emptyExpansionExists(la.la_expansion) ||
           javaCodeCheck(la.la_expansion)
          ) {

        // This handles the following cases:
        // . If syntactic lookahead is not wanted (and hence explicitly specified
        //   as 0).
        // . If it is possible for the lookahead expansion to recognize the empty
        //   string - in which case the lookahead trivially passes.
        // . If the lookahead expansion has a JAVACODE production that it directly
        //   expands to - in which case the lookahead trivially passes.
        if (la.action_tokens.size() == 0) {
          // In addition, if there is no semantic lookahead, then the
          // lookahead trivially succeeds.  So break the main loop and
          // treat this case as the default last action.
          break;
        } else {
          // This case is when there is only semantic lookahead
          // (without any preceding syntactic lookahead).  In this
          // case, an "if" statement is generated.
          switch (state) {
            case NOOPENSTM:
              retval += "\n" + "if (";
              indentAmt++;
              break;
            case OPENIF:
              retval += "\u0002\n" + "} else if (";
              break;
            case OPENSWITCH:
              retval += "\u0002\n" + "default:" + "\u0001";
              if (Options.getErrorReporting()) {
                retval += "\njj_la1[" + maskindex + "] = jj_gen;";
                maskindex++;
              }
              maskVals.addElement(tokenMask);
              retval += "\n" + "if (";
              indentAmt++;
          }
          printTokenSetup((Token)(la.action_tokens.elementAt(0)));
          for (java.util.Enumeration enumeration = la.action_tokens.elements(); enumeration.hasMoreElements();) {
            t = (Token)enumeration.nextElement();
            retval += printToken(t);
          }
          retval += printTrailingComments(t);
          retval += ") {\u0001" + actions[index];
          state = OPENIF;
        }

      } else if (la.amount == 1 && la.action_tokens.size() == 0) {
        // Special optimal processing when the lookahead is exactly 1, and there
        // is no semantic lookahead.

        if (firstSet == null) {
          firstSet = new boolean[tokenCount];
        }
        for (int i = 0; i < tokenCount; i++) {
          firstSet[i] = false;
        }
        // jj2LA is set to false at the beginning of the containing "if" statement.
        // It is checked immediately after the end of the same statement to determine
        // if lookaheads are to be performed using calls to the jj2 methods.
        genFirstSet(la.la_expansion);
        // genFirstSet may find that semantic attributes are appropriate for the next
        // token.  In which case, it sets jj2LA to true.
        if (!jj2LA) {

          // This case is if there is no applicable semantic lookahead and the lookahead
          // is one (excluding the earlier cases such as JAVACODE, etc.).
          switch (state) {
            case OPENIF:
              retval += "\u0002\n" + "} else {\u0001";
              // Control flows through to next case.
            case NOOPENSTM:
              retval += "\n" + "switch (";
              if (Options.getCacheTokens()) {
                retval += "jj_nt.kind) {\u0001";
              } else {
                retval += "(jj_ntk==-1)?jj_ntk():jj_ntk) {\u0001";
              }
              for (int i = 0; i < tokenCount; i++) {
                casedValues[i] = false;
              }
              indentAmt++;
              tokenMask = new int[tokenMaskSize];
              for (int i = 0; i < tokenMaskSize; i++) {
                tokenMask[i] = 0;
              }
            // Don't need to do anything if state is OPENSWITCH.
          }
          for (int i = 0; i < tokenCount; i++) {
            if (firstSet[i]) {
              if (!casedValues[i]) {
                casedValues[i] = true;
                retval += "\u0002\ncase ";
                int j1 = i/32;
                int j2 = i%32;
                tokenMask[j1] |= 1 << j2;
                String s = (String)(names_of_tokens.get(new Integer(i)));
                if (s == null) {
                  retval += i;
                } else {
                  retval += s;
                }
                retval += ":\u0001";
              }
            }
          }
          retval += actions[index];
          retval += "\nbreak;";
          state = OPENSWITCH;

        }

      } else {
        // This is the case when lookahead is determined through calls to
        // jj2 methods.  The other case is when lookahead is 1, but semantic
        // attributes need to be evaluated.  Hence this crazy control structure.

        jj2LA = true;

      }

      if (jj2LA) {
        // In this case lookahead is determined by the jj2 methods.

        switch (state) {
          case NOOPENSTM:
            retval += "\n" + "if (";
            indentAmt++;
            break;
          case OPENIF:
            retval += "\u0002\n" + "} else if (";
            break;
          case OPENSWITCH:
            retval += "\u0002\n" + "default:" + "\u0001";
            if (Options.getErrorReporting()) {
              retval += "\njj_la1[" + maskindex + "] = jj_gen;";
              maskindex++;
            }
            maskVals.addElement(tokenMask);
            retval += "\n" + "if (";
            indentAmt++;
          }
          jj2index++;
          // At this point, la.la_expansion.internal_name must be "".
          la.la_expansion.internal_name = "_" + jj2index;
          phase2list.addElement(la);
          retval += "jj_2" + la.la_expansion.internal_name + "(" + la.amount + ")";
          if (la.action_tokens.size() != 0) {
            // In addition, there is also a semantic lookahead.  So concatenate
            // the semantic check with the syntactic one.
            retval += " && (";
            printTokenSetup((Token)(la.action_tokens.elementAt(0)));
            for (java.util.Enumeration enumeration = la.action_tokens.elements(); enumeration.hasMoreElements();) {
              t = (Token)enumeration.nextElement();
              retval += printToken(t);
            }
            retval += printTrailingComments(t);
            retval += ")";
          }
          retval += ") {\u0001" + actions[index];
          state = OPENIF;
        }

        index++;
      }

    // Generate code for the default case.  Note this may not
    // be the last entry of "actions" if any condition can be
    // statically determined to be always "true".

    switch (state) {
    case NOOPENSTM:
      retval += actions[index];
      break;
    case OPENIF:
      retval += "\u0002\n" + "} else {\u0001" + actions[index];
      break;
    case OPENSWITCH:
      retval += "\u0002\n" + "default:" + "\u0001";
      if (Options.getErrorReporting()) {
        retval += "\njj_la1[" + maskindex + "] = jj_gen;";
        maskVals.addElement(tokenMask);
        maskindex++;
      }
      retval += actions[index];
    }
    for (int i = 0; i < indentAmt; i++) {
      retval += "\u0002\n}";
    }

    return retval;

  }

  static void dumpFormattedString(String str) {
    char ch = ' ';
    char prevChar;
    boolean indentOn = true;
    for (int i = 0; i < str.length(); i++) {
      prevChar = ch;
      ch = str.charAt(i);
      if (ch == '\n' && prevChar == '\r') {
        // do nothing - we've already printed a new line for the '\r'
        // during the previous iteration.
      } else if (ch == '\n' || ch == '\r') {
        if (indentOn) {
          phase1NewLine();
        } else {
          ostr.println("");
        }
      } else if (ch == '\u0001') {
        indentamt += 2;
      } else if (ch == '\u0002') {
        indentamt -= 2;
      } else if (ch == '\u0003') {
        indentOn = false;
      } else if (ch == '\u0004') {
        indentOn = true;
      } else {
        ostr.print(ch);
      }
    }
  }

  static void buildPhase1Routine(BNFProduction p) {
    Token t;
    t = (Token)(p.return_type_tokens.elementAt(0));
    boolean voidReturn = false;
    if (t.kind == JavaCCParserConstants.VOID) {
      voidReturn = true;
    }
    printTokenSetup(t); ccol = 1;
    printLeadingComments(t, ostr);
    ostr.print("  " + staticOpt() + "final " +(p.accessMod != null ? p.accessMod : "public")+ " ");
    cline = t.beginLine; ccol = t.beginColumn;
    printTokenOnly(t, ostr);
    for (int i = 1; i < p.return_type_tokens.size(); i++) {
      t = (Token)(p.return_type_tokens.elementAt(i));
      printToken(t, ostr);
    }
    printTrailingComments(t, ostr);
    ostr.print(" " + p.lhs + "(");
    if (p.parameter_list_tokens.size() != 0) {
      printTokenSetup((Token)(p.parameter_list_tokens.elementAt(0)));
      for (java.util.Enumeration enumeration = p.parameter_list_tokens.elements(); enumeration.hasMoreElements();) {
        t = (Token)enumeration.nextElement();
        printToken(t, ostr);
      }
      printTrailingComments(t, ostr);
    }
    ostr.print(") throws ParseException");
    for (java.util.Enumeration enumeration = p.throws_list.elements(); enumeration.hasMoreElements();) {
      ostr.print(", ");
      java.util.Vector name = (java.util.Vector)enumeration.nextElement();
      for (java.util.Enumeration enum1 = name.elements(); enum1.hasMoreElements();) {
        t = (Token)enum1.nextElement();
        ostr.print(t.image);
      }
    }
    ostr.print(" {");
    indentamt = 4;
    if (Options.getDebugParser()) {
      ostr.println("");
      ostr.println("    trace_call(\"" + p.lhs + "\");");
      ostr.print("    try {");
      indentamt = 6;
    }
    if (p.declaration_tokens.size() != 0) {
      printTokenSetup((Token)(p.declaration_tokens.elementAt(0))); cline--;
      for (java.util.Enumeration enumeration = p.declaration_tokens.elements(); enumeration.hasMoreElements();) {
        t = (Token)enumeration.nextElement();
        printToken(t, ostr);
      }
      printTrailingComments(t, ostr);
    }
    String code = phase1ExpansionGen(p.expansion);
    dumpFormattedString(code);
    ostr.println("");
    if (p.jumpPatched && !voidReturn) {
      ostr.println("    throw new Error(\"Missing return statement in function\");");
    }
    if (Options.getDebugParser()) {
      ostr.println("    } finally {");
      ostr.println("      trace_return(\"" + p.lhs + "\");");
      ostr.println("    }");
    }
    ostr.println("  }");
    ostr.println("");
  }

  static void phase1NewLine() {
    ostr.println("");
    for (int i = 0; i < indentamt; i++) {
      ostr.print(" ");
    }
  }

  static String phase1ExpansionGen(Expansion e) {
    String retval = "";
    Token t = null;
    Lookahead[] conds;
    String[] actions;
    if (e instanceof RegularExpression) {
      RegularExpression e_nrw = (RegularExpression)e;
      retval += "\n";
      if (e_nrw.lhsTokens.size() != 0) {
        printTokenSetup((Token)(e_nrw.lhsTokens.elementAt(0)));
        for (java.util.Enumeration enumeration = e_nrw.lhsTokens.elements(); enumeration.hasMoreElements();) {
          t = (Token)enumeration.nextElement();
          retval += printToken(t);
        }
        retval += printTrailingComments(t);
        retval += " = ";
      }
      String tail = e_nrw.rhsToken == null ? ");" : ")." + e_nrw.rhsToken.image + ";";
      if (e_nrw.label.equals("")) {
        Object label = names_of_tokens.get(new Integer(e_nrw.ordinal));
        if (label != null) {
          retval += "jj_consume_token(" + (String)label + tail;
        } else {
          retval += "jj_consume_token(" + e_nrw.ordinal + tail;
        }
      } else {
        retval += "jj_consume_token(" + e_nrw.label + tail;
      }
    } else if (e instanceof NonTerminal) {
      NonTerminal e_nrw = (NonTerminal)e;
      retval += "\n";
      if (e_nrw.lhsTokens.size() != 0) {
        printTokenSetup((Token)(e_nrw.lhsTokens.elementAt(0)));
        for (java.util.Enumeration enumeration = e_nrw.lhsTokens.elements(); enumeration.hasMoreElements();) {
          t = (Token)enumeration.nextElement();
          retval += printToken(t);
        }
        retval += printTrailingComments(t);
        retval += " = ";
      }
      retval += e_nrw.name + "(";
      if (e_nrw.argument_tokens.size() != 0) {
        printTokenSetup((Token)(e_nrw.argument_tokens.elementAt(0)));
        for (java.util.Enumeration enumeration = e_nrw.argument_tokens.elements(); enumeration.hasMoreElements();) {
          t = (Token)enumeration.nextElement();
          retval += printToken(t);
        }
        retval += printTrailingComments(t);
      }
      retval += ");";
    } else if (e instanceof Action) {
      Action e_nrw = (Action)e;
      retval += "\u0003\n";
      if (e_nrw.action_tokens.size() != 0) {
        printTokenSetup((Token)(e_nrw.action_tokens.elementAt(0))); ccol = 1;
        for (java.util.Enumeration enumeration = e_nrw.action_tokens.elements(); enumeration.hasMoreElements();) {
          t = (Token)enumeration.nextElement();
          retval += printToken(t);
        }
        retval += printTrailingComments(t);
      }
      retval += "\u0004";
    } else if (e instanceof Choice) {
      Choice e_nrw = (Choice)e;
      conds = new Lookahead[e_nrw.choices.size()];
      actions = new String[e_nrw.choices.size() + 1];
      actions[e_nrw.choices.size()] = "\n" + "jj_consume_token(-1);\n" + "throw new ParseException();";
      // In previous line, the "throw" never throws an exception since the
      // evaluation of jj_consume_token(-1) causes ParseException to be
      // thrown first.
      Sequence nestedSeq;
      for (int i = 0; i < e_nrw.choices.size(); i++) {
        nestedSeq = (Sequence)(e_nrw.choices.elementAt(i));
        actions[i] = phase1ExpansionGen(nestedSeq);
        conds[i] = (Lookahead)(nestedSeq.units.elementAt(0));
      }
      retval = buildLookaheadChecker(conds, actions);
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      for (int i = 1; i < e_nrw.units.size(); i++) {
        retval += phase1ExpansionGen((Expansion)(e_nrw.units.elementAt(i)));
      }
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead)(((Sequence)nested_e).units.elementAt(0));
      } else {
        la = new Lookahead();
        la.amount = Options.getLookahead();
        la.la_expansion = nested_e;
      }
      retval += "\n";
      int labelIndex = ++gensymindex;
      retval += "label_" + labelIndex + ":\n";
      retval += "while (true) {\u0001";
      retval += phase1ExpansionGen(nested_e);
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = "\n;";
      actions[1] = "\nbreak label_" + labelIndex + ";";
      retval += buildLookaheadChecker(conds, actions);
      retval += "\u0002\n" + "}";
    } else if (e instanceof ZeroOrMore) {
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead)(((Sequence)nested_e).units.elementAt(0));
      } else {
        la = new Lookahead();
        la.amount = Options.getLookahead();
        la.la_expansion = nested_e;
      }
      retval += "\n";
      int labelIndex = ++gensymindex;
      retval += "label_" + labelIndex + ":\n";
      retval += "while (true) {\u0001";
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = "\n;";
      actions[1] = "\nbreak label_" + labelIndex + ";";
      retval += buildLookaheadChecker(conds, actions);
      retval += phase1ExpansionGen(nested_e);
      retval += "\u0002\n" + "}";
    } else if (e instanceof ZeroOrOne) {
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      Expansion nested_e = e_nrw.expansion;
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead)(((Sequence)nested_e).units.elementAt(0));
      } else {
        la = new Lookahead();
        la.amount = Options.getLookahead();
        la.la_expansion = nested_e;
      }
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = phase1ExpansionGen(nested_e);
      actions[1] = "\n;";
      retval += buildLookaheadChecker(conds, actions);
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      Expansion nested_e = e_nrw.exp;
      java.util.Vector v;
      retval += "\n";
      retval += "try {\u0001";
      retval += phase1ExpansionGen(nested_e);
      retval += "\u0002\n" + "}";
      for (int i = 0; i < e_nrw.catchblks.size(); i++) {
        retval += " catch (";
        v = (java.util.Vector)(e_nrw.types.elementAt(i));
        if (v.size() != 0) {
          printTokenSetup((Token)(v.elementAt(0)));
          for (java.util.Enumeration enumeration = v.elements(); enumeration.hasMoreElements();) {
            t = (Token)enumeration.nextElement();
            retval += printToken(t);
          }
          retval += printTrailingComments(t);
        }
        retval += " ";
        t = (Token)(e_nrw.ids.elementAt(i));
        printTokenSetup(t);
        retval += printToken(t);
        retval += printTrailingComments(t);
        retval += ") {\u0003\n";
        v = (java.util.Vector)(e_nrw.catchblks.elementAt(i));
        if (v.size() != 0) {
          printTokenSetup((Token)(v.elementAt(0))); ccol = 1;
          for (java.util.Enumeration enumeration = v.elements(); enumeration.hasMoreElements();) {
            t = (Token)enumeration.nextElement();
            retval += printToken(t);
          }
          retval += printTrailingComments(t);
        }
        retval += "\u0004\n" + "}";
      }
      if (e_nrw.finallyblk != null) {
        retval += " finally {\u0003\n";
        if (e_nrw.finallyblk.size() != 0) {
          printTokenSetup((Token)(e_nrw.finallyblk.elementAt(0))); ccol = 1;
          for (java.util.Enumeration enumeration = e_nrw.finallyblk.elements(); enumeration.hasMoreElements();) {
            t = (Token)enumeration.nextElement();
            retval += printToken(t);
          }
          retval += printTrailingComments(t);
        }
        retval += "\u0004\n" + "}";
      }
    }
    return retval;
  }

  static void buildPhase2Routine(Lookahead la) {
    Expansion e = la.la_expansion;
    ostr.println("  " + staticOpt() + "private boolean jj_2" + e.internal_name + "(int xla) {");
    ostr.println("    jj_la = xla; jj_lastpos = jj_scanpos = token;");
    ostr.println("    try { return !jj_3" + e.internal_name + "(); }");
    ostr.println("    catch(LookaheadSuccess ls) { return true; }");
    if (Options.getErrorReporting())
      ostr.println("    finally { jj_save(" + (Integer.parseInt(e.internal_name.substring(1))-1) + ", xla); }");
    ostr.println("  }");
    ostr.println("");
    Phase3Data p3d = new Phase3Data(e, la.amount);
    phase3list.addElement(p3d);
    phase3table.put(e, p3d);
  }

  static private boolean xsp_declared;

  static Expansion jj3_expansion;

  static String genReturn(boolean value) {
    String retval = (value ? "true" : "false");
    if (Options.getDebugLookahead() && jj3_expansion != null) {
      String tracecode = "trace_return(\"" + ((NormalProduction)jj3_expansion.parent).lhs +
                         "(LOOKAHEAD " + (value ? "FAILED" : "SUCCEEDED") + ")\");";
      if (Options.getErrorReporting()) {
        tracecode = "if (!jj_rescan) " + tracecode;
      }
      return "{ " + tracecode + " return " + retval + "; }";
    } else {
      return "return " + retval + ";";
    }
  }

  private static void generate3R(Expansion e, Phase3Data inf)
  {
    Expansion seq = e;
    if (e.internal_name.equals(""))
    {
      while (true)
      {
         if (seq instanceof Sequence && ((Sequence)seq).units.size() == 2)
         {
            seq = (Expansion)((Sequence)seq).units.elementAt(1);
         }
         else if (seq instanceof NonTerminal)
         {
            NonTerminal e_nrw = (NonTerminal)seq;
            NormalProduction ntprod = (NormalProduction)(production_table.get(e_nrw.name));
            if (ntprod instanceof JavaCodeProduction)
            {
              break; // nothing to do here
            }
            else
            {
              seq = ntprod.expansion;
            }
         }
         else
            break;
      }

      if (seq instanceof RegularExpression)
      {
         e.internal_name = "jj_scan_token(" + ((RegularExpression)seq).ordinal + ")";
         return;
      }

      gensymindex++;
//if (gensymindex == 100)
//{
//new Error().printStackTrace();
//System.out.println(" ***** seq: " + seq.internal_name + "; size: " + ((Sequence)seq).units.size());
//}
      e.internal_name = "R_" + gensymindex;
    }
    Phase3Data p3d = (Phase3Data)(phase3table.get(e));
    if (p3d == null || p3d.count < inf.count) {
      p3d = new Phase3Data(e, inf.count);
      phase3list.addElement(p3d);
      phase3table.put(e, p3d);
    }
  }

  static void setupPhase3Builds(Phase3Data inf) {
    Expansion e = inf.exp;
    if (e instanceof RegularExpression) {
      ; // nothing to here
    } else if (e instanceof NonTerminal) {
      // All expansions of non-terminals have the "name" fields set.  So
      // there's no need to check it below for "e_nrw" and "ntexp".  In
      // fact, we rely here on the fact that the "name" fields of both these
      // variables are the same.
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod = (NormalProduction)(production_table.get(e_nrw.name));
      if (ntprod instanceof JavaCodeProduction) {
        ; // nothing to do here
      } else {
        generate3R(ntprod.expansion, inf);
      }
    } else if (e instanceof Choice) {
      Choice e_nrw = (Choice)e;
      for (int i = 0; i < e_nrw.choices.size(); i++) {
        generate3R((Expansion)(e_nrw.choices.elementAt(i)), inf);
      }
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      int cnt = inf.count;
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = (Expansion)(e_nrw.units.elementAt(i));
        setupPhase3Builds(new Phase3Data(eseq, cnt));
        cnt -= minimumSize(eseq);
        if (cnt <= 0) break;
      }
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      setupPhase3Builds(new Phase3Data(e_nrw.exp, inf.count));
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      generate3R(e_nrw.expansion, inf);
    } else if (e instanceof ZeroOrMore) {
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      generate3R(e_nrw.expansion, inf);
    } else if (e instanceof ZeroOrOne) {
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      generate3R(e_nrw.expansion, inf);
    }
  }

  private static String genjj_3Call(Expansion e)
  {
     if (e.internal_name.startsWith("jj_scan_token"))
        return e.internal_name;
     else
        return "jj_3" + e.internal_name + "()";
  }

  static Hashtable generated = new Hashtable();
  static void buildPhase3Routine(Phase3Data inf, boolean recursive_call) {
    Expansion e = inf.exp;
    Token t = null;
    if (e.internal_name.startsWith("jj_scan_token"))
       return;

    if (!recursive_call) {
      ostr.println("  " + staticOpt() + "private boolean jj_3" + e.internal_name + "() {");
      xsp_declared = false;
      if (Options.getDebugLookahead() && e.parent instanceof NormalProduction) {
        ostr.print("    ");
        if (Options.getErrorReporting()) {
          ostr.print("if (!jj_rescan) ");
        }
        ostr.println("trace_call(\"" + ((NormalProduction)e.parent).lhs + "(LOOKING AHEAD...)\");");
        jj3_expansion = e;
      } else {
        jj3_expansion = null;
      }
    }
    if (e instanceof RegularExpression) {
      RegularExpression e_nrw = (RegularExpression)e;
      if (e_nrw.label.equals("")) {
        Object label = names_of_tokens.get(new Integer(e_nrw.ordinal));
        if (label != null) {
          ostr.println("    if (jj_scan_token(" + (String)label + ")) " + genReturn(true));
        } else {
          ostr.println("    if (jj_scan_token(" + e_nrw.ordinal + ")) " + genReturn(true));
        }
      } else {
        ostr.println("    if (jj_scan_token(" + e_nrw.label + ")) " + genReturn(true));
      }
      //ostr.println("    if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
    } else if (e instanceof NonTerminal) {
      // All expansions of non-terminals have the "name" fields set.  So
      // there's no need to check it below for "e_nrw" and "ntexp".  In
      // fact, we rely here on the fact that the "name" fields of both these
      // variables are the same.
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod = (NormalProduction)(production_table.get(e_nrw.name));
      if (ntprod instanceof JavaCodeProduction) {
        ostr.println("    if (true) { jj_la = 0; jj_scanpos = jj_lastpos; " + genReturn(false) + "}");
      } else {
        Expansion ntexp = ntprod.expansion;
        //ostr.println("    if (jj_3" + ntexp.internal_name + "()) " + genReturn(true));
        ostr.println("    if (" + genjj_3Call(ntexp)+ ") " + genReturn(true));
        //ostr.println("    if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
      }
    } else if (e instanceof Choice) {
      Sequence nested_seq;
      Choice e_nrw = (Choice)e;
      if (e_nrw.choices.size() != 1) {
        if (!xsp_declared) {
          xsp_declared = true;
          ostr.println("    Token xsp;");
        }
        ostr.println("    xsp = jj_scanpos;");
      }
      for (int i = 0; i < e_nrw.choices.size(); i++) {
        nested_seq = (Sequence)(e_nrw.choices.elementAt(i));
        Lookahead la = (Lookahead)(nested_seq.units.elementAt(0));
        if (la.action_tokens.size() != 0) {
          // We have semantic lookahead that must be evaluated.
          ostr.println("    lookingAhead = true;");
          ostr.print("    jj_semLA = ");
          printTokenSetup((Token)(la.action_tokens.elementAt(0)));
          for (java.util.Enumeration enumeration = la.action_tokens.elements(); enumeration.hasMoreElements();) {
            t = (Token)enumeration.nextElement();
            printToken(t, ostr);
          }
          printTrailingComments(t, ostr);
          ostr.println(";");
          ostr.println("    lookingAhead = false;");
        }
        ostr.print("    if (");
        if (la.action_tokens.size() != 0) {
          ostr.print("!jj_semLA || ");
        }
        if (i != e_nrw.choices.size() - 1) {
          //ostr.println("jj_3" + nested_seq.internal_name + "()) {");
          ostr.println(genjj_3Call(nested_seq) + ") {");
          ostr.println("    jj_scanpos = xsp;");
        } else {
          //ostr.println("jj_3" + nested_seq.internal_name + "()) " + genReturn(true));
          ostr.println(genjj_3Call(nested_seq) + ") " + genReturn(true));
          //ostr.println("    if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
        }
      }
      for (int i = 1; i < e_nrw.choices.size(); i++) {
        //ostr.println("    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
        ostr.println("    }");
      }
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      int cnt = inf.count;
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = (Expansion)(e_nrw.units.elementAt(i));
        buildPhase3Routine(new Phase3Data(eseq, cnt), true);

//System.out.println("minimumSize: line: " + eseq.line + ", column: " + eseq.column + ": " + 
//        minimumSize(eseq));//Test Code

        cnt -= minimumSize(eseq);
        if (cnt <= 0) break;
      }
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      buildPhase3Routine(new Phase3Data(e_nrw.exp, inf.count), true);
    } else if (e instanceof OneOrMore) {
      if (!xsp_declared) {
        xsp_declared = true;
        ostr.println("    Token xsp;");
      }
      OneOrMore e_nrw = (OneOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      //ostr.println("    if (jj_3" + nested_e.internal_name + "()) " + genReturn(true));
      ostr.println("    if (" + genjj_3Call(nested_e) + ") " + genReturn(true));
      //ostr.println("    if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
      ostr.println("    while (true) {");
      ostr.println("      xsp = jj_scanpos;");
      //ostr.println("      if (jj_3" + nested_e.internal_name + "()) { jj_scanpos = xsp; break; }");
      ostr.println("      if (" + genjj_3Call(nested_e) + ") { jj_scanpos = xsp; break; }");
      //ostr.println("      if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
      ostr.println("    }");
    } else if (e instanceof ZeroOrMore) {
      if (!xsp_declared) {
        xsp_declared = true;
        ostr.println("    Token xsp;");
      }
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      ostr.println("    while (true) {");
      ostr.println("      xsp = jj_scanpos;");
      //ostr.println("      if (jj_3" + nested_e.internal_name + "()) { jj_scanpos = xsp; break; }");
      ostr.println("      if (" + genjj_3Call(nested_e) + ") { jj_scanpos = xsp; break; }");
      //ostr.println("      if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
      ostr.println("    }");
    } else if (e instanceof ZeroOrOne) {
      if (!xsp_declared) {
        xsp_declared = true;
        ostr.println("    Token xsp;");
      }
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      Expansion nested_e = e_nrw.expansion;
      ostr.println("    xsp = jj_scanpos;");
      //ostr.println("    if (jj_3" + nested_e.internal_name + "()) jj_scanpos = xsp;");
      ostr.println("    if (" + genjj_3Call(nested_e) + ") jj_scanpos = xsp;");
      //ostr.println("    else if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
    }
    if (!recursive_call) {
      ostr.println("    " + genReturn(false));
      ostr.println("  }");
      ostr.println("");
    }
  }

  static int minimumSize(Expansion e) {
     return minimumSize(e, Integer.MAX_VALUE);
  }

  /*
   * Returns the minimum number of tokens that can parse to this expansion.
   */
  static int minimumSize(Expansion e, int oldMin) {
    int retval = 0;  // should never be used.  Will be bad if it is.
    if (e.inMinimumSize) {
      // recursive search for minimum size unnecessary.
      return Integer.MAX_VALUE;
    }
    e.inMinimumSize = true;
    if (e instanceof RegularExpression) {
      retval = 1;
    } else if (e instanceof NonTerminal) {
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod = (NormalProduction)(production_table.get(e_nrw.name));
      if (ntprod instanceof JavaCodeProduction) {
        retval = Integer.MAX_VALUE;
        // Make caller think this is unending (for we do not go beyond JAVACODE during
        // phase3 execution).
      } else {
        Expansion ntexp = ntprod.expansion;
        retval = minimumSize(ntexp);
      }
    } else if (e instanceof Choice) {
      int min = oldMin;
      Expansion nested_e;
      Choice e_nrw = (Choice)e;
      for (int i = 0; min > 1 && i < e_nrw.choices.size(); i++) {
        nested_e = (Expansion)(e_nrw.choices.elementAt(i));
        int min1 = minimumSize(nested_e, min);
        if (min > min1) min = min1;
      }
      retval = min;
    } else if (e instanceof Sequence) {
      int min = 0;
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = (Expansion)(e_nrw.units.elementAt(i));
        int mineseq = minimumSize(eseq);
        if (min == Integer.MAX_VALUE || mineseq == Integer.MAX_VALUE) {
          min = Integer.MAX_VALUE; // Adding infinity to something results in infinity.
        } else {
          min += mineseq;
          if (min > oldMin)
             break;
        }
      }
      retval = min;
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      retval = minimumSize(e_nrw.exp);
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      retval = minimumSize(e_nrw.expansion);
    } else if (e instanceof ZeroOrMore) {
      retval = 0;
    } else if (e instanceof ZeroOrOne) {
      retval = 0;
    } else if (e instanceof Lookahead) {
      retval = 0;
    } else if (e instanceof Action) {
      retval = 0;
    }
    e.inMinimumSize = false;
    return retval;
  }

  static void build(java.io.PrintWriter ps) {
    NormalProduction p;
    JavaCodeProduction jp;
    Token t = null;

    ostr = ps;

    for (java.util.Enumeration enumeration = bnfproductions.elements(); enumeration.hasMoreElements();) {
      p = (NormalProduction)enumeration.nextElement();
      if (p instanceof JavaCodeProduction) {
        jp = (JavaCodeProduction)p;
        t = (Token)(jp.return_type_tokens.elementAt(0));
        printTokenSetup(t); ccol = 1;
        printLeadingComments(t, ostr);
        ostr.print("  " + staticOpt() + (p.accessMod != null ? p.accessMod + " " : ""));
        cline = t.beginLine; ccol = t.beginColumn;
        printTokenOnly(t, ostr);
        for (int i = 1; i < jp.return_type_tokens.size(); i++) {
          t = (Token)(jp.return_type_tokens.elementAt(i));
          printToken(t, ostr);
        }
        printTrailingComments(t, ostr);
        ostr.print(" " + jp.lhs + "(");
        if (jp.parameter_list_tokens.size() != 0) {
          printTokenSetup((Token)(jp.parameter_list_tokens.elementAt(0)));
          for (java.util.Enumeration enum1 = jp.parameter_list_tokens.elements(); enum1.hasMoreElements();) {
            t = (Token)enum1.nextElement();
            printToken(t, ostr);
          }
          printTrailingComments(t, ostr);
        }
        ostr.print(") throws ParseException");
        for (java.util.Enumeration enum1 = jp.throws_list.elements(); enum1.hasMoreElements();) {
          ostr.print(", ");
          java.util.Vector name = (java.util.Vector)enum1.nextElement();
          for (java.util.Enumeration enum2 = name.elements(); enum2.hasMoreElements();) {
            t = (Token)enum2.nextElement();
            ostr.print(t.image);
          }
        }
        ostr.print(" {");
        if (Options.getDebugParser()) {
          ostr.println("");
          ostr.println("    trace_call(\"" + jp.lhs + "\");");
          ostr.print("    try {");
        }
        if (jp.code_tokens.size() != 0) {
          printTokenSetup((Token)(jp.code_tokens.elementAt(0))); cline--;
          for (java.util.Enumeration enum1 = jp.code_tokens.elements(); enum1.hasMoreElements();) {
            t = (Token)enum1.nextElement();
            printToken(t, ostr);
          }
          printTrailingComments(t, ostr);
        }
        ostr.println("");
        if (Options.getDebugParser()) {
          ostr.println("    } finally {");
          ostr.println("      trace_return(\"" + jp.lhs + "\");");
          ostr.println("    }");
        }
        ostr.println("  }");
        ostr.println("");
      } else {
        buildPhase1Routine((BNFProduction)p);
      }
    }

    for (int phase2index = 0; phase2index < phase2list.size(); phase2index++) {
      buildPhase2Routine((Lookahead)(phase2list.elementAt(phase2index)));
    }

    int phase3index = 0;

    while (phase3index < phase3list.size()) {
      for (; phase3index < phase3list.size(); phase3index++) {
        setupPhase3Builds((Phase3Data)(phase3list.elementAt(phase3index)));
      }
    }

    for (java.util.Enumeration enumeration = phase3table.elements(); enumeration.hasMoreElements();) {
      buildPhase3Routine((Phase3Data)(enumeration.nextElement()), false);
    }

  }

   public static void reInit()
   {
      ostr = null;
      gensymindex = 0;
      indentamt = 0;
      jj2LA = false;
      phase2list = new java.util.Vector();
      phase3list = new java.util.Vector();
      phase3table = new java.util.Hashtable();
      firstSet = null;
      xsp_declared = false;
      jj3_expansion = null;
   }

}

/**
 * This class stores information to pass from phase 2 to phase 3.
 */
class Phase3Data {

  /*
   * This is the expansion to generate the jj3 method for.
   */
  Expansion exp;

  /*
   * This is the number of tokens that can still be consumed.  This
   * number is used to limit the number of jj3 methods generated.
   */
  int count;

  Phase3Data(Expansion e, int c) {
    exp = e;
    count = c;
  }

}
