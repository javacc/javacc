
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


package org.javacc.jjdoc;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

import org.javacc.parser.*;


public class JJDoc extends JavaCCGlobals {

  /**
   * The main entry point for JJDoc.
   */
  static void start() {
    PrintWriter pw = create_output_stream();

    Generator gen;

    if (JJDocOptions.getText()) {
      gen = new Generator(pw);
    } else {
      gen = new HTMLGenerator(pw);
    }
    gen.documentStart();
    emitTokenProductions(gen, rexprlist);
    emitNormalProductions(gen, bnfproductions);
    gen.documentEnd();

    pw.close();
  }


  private static Token getPrecedingSpecialToken(Token tok) {
    Token t = tok;
    while (t.specialToken != null) {
      t = t.specialToken;
    }
    return (t != tok) ? t : null;
  }

  private static void emitTopLevelSpecialTokens(Token tok, Generator gen) {
    if (tok == null) {
      // Strange ...
      return;
    }

    tok = getPrecedingSpecialToken(tok);
    String s = "";
    if (tok != null) {
      cline = tok.beginLine;
      ccol = tok.beginColumn;
      while (tok != null) {
	s += printTokenOnly(tok);
	tok = tok.next;
      }
    }
    gen.specialTokens(s);
  }


  private static boolean toplevelExpansion(Expansion exp) {
    return exp.parent != null
      && ( (exp.parent instanceof NormalProduction)
	   ||
	   (exp.parent instanceof TokenProduction)
	   );
  }


  private static void emitTokenProductions(Generator gen, Vector prods) {
//     gen.tokensStart();
    for (Enumeration enum = prods.elements(); enum.hasMoreElements();) {
      TokenProduction tp = (TokenProduction)enum.nextElement();
//       emitTopLevelSpecialTokens(ostr, tp.firstToken);

//       if (tp.isExplicit) {
// 	if (tp.lexStates == null) {
// 	  ostr.print("<*> ");
// 	} else {
// 	  ostr.print("<");
// 	  for (int i = 0; i < tp.lexStates.length; ++i) {
// 	    ostr.print(tp.lexStates[i]);
// 	    if (i < tp.lexStates.length - 1) {
// 	      ostr.print(",");
// 	    }
// 	  }
// 	  ostr.print("> ");
// 	}
// 	ostr.print(tp.kindImage[tp.kind]);
// 	if (tp.ignoreCase) {
// 	  ostr.print(" [IGNORE_CASE]");
// 	}
// 	ostr.print(" : {\n");
// 	for (Enumeration e2 = tp.respecs.elements(); e2.hasMoreElements();) {
// 	  RegExprSpec res = (RegExprSpec)e2.nextElement();

// 	  emitRE(res.rexp, ostr);

// 	  if (res.nsTok != null) {
// 	    ostr.print(" : " + res.nsTok.image);
// 	  }

// 	  ostr.print("\n");
// 	  if (e2.hasMoreElements()) {
// 	    ostr.print("| ");
// 	  }
// 	}
// 	ostr.print("}\n\n");
//       }
    }
//     gen.tokensEnd();
  }

  
  private static void emitNormalProductions(Generator gen, Vector prods) {
    gen.nonterminalsStart();
    for (Enumeration enum = prods.elements(); enum.hasMoreElements();) {
      NormalProduction np = (NormalProduction)enum.nextElement();

      emitTopLevelSpecialTokens(np.firstToken, gen);

      if (np instanceof BNFProduction) {
	gen.productionStart(np);
	if (np.expansion instanceof Choice) {
	  boolean first = true;
	  Choice c = (Choice)np.expansion;
	  for (java.util.Enumeration enume = c.choices.elements();
	       enume.hasMoreElements();) {
	    Expansion e = (Expansion)(enume.nextElement());
	    gen.expansionStart(e, first);
	    emitExpansionTree(e, gen);
	    gen.expansionEnd(e, first);
	    first = false;
	  }
	} else {
	  gen.expansionStart(np.expansion, true);
	  emitExpansionTree(np.expansion, gen);
	  gen.expansionEnd(np.expansion, true);
	}
	gen.productionEnd(np);
      } else if (np instanceof JavaCodeProduction) {
	gen.javacode((JavaCodeProduction)np);
      }
    }
    gen.nonterminalsEnd();
  }


  private static void emitExpansionTree(Expansion exp, Generator gen) {
//     gen.text("[->" + exp.getClass().getName() + "]");
    if (exp instanceof Action) {
      emitExpansionAction((Action)exp, gen);
    } else if (exp instanceof Choice) {
      emitExpansionChoice((Choice)exp, gen);
    } else if (exp instanceof Lookahead) {
      emitExpansionLookahead((Lookahead)exp, gen);
    } else if (exp instanceof NonTerminal) {
      emitExpansionNonTerminal((NonTerminal)exp, gen);
    } else if (exp instanceof OneOrMore) {
      emitExpansionOneOrMore((OneOrMore)exp, gen);
    } else if (exp instanceof RegularExpression) {
      emitExpansionRegularExpression((RegularExpression)exp, gen);
    } else if (exp instanceof Sequence) {
      emitExpansionSequence((Sequence)exp, gen);
    } else if (exp instanceof TryBlock) {
      emitExpansionTryBlock((TryBlock)exp, gen);
    } else if (exp instanceof ZeroOrMore) {
      emitExpansionZeroOrMore((ZeroOrMore)exp, gen);
    } else if (exp instanceof ZeroOrOne) {
      emitExpansionZeroOrOne((ZeroOrOne)exp, gen);
    } else {
      System.out.println("Oops: Unknown expansion type.");
    }
//     gen.text("[<-" + exp.getClass().getName() + "]");
  }


  private static void emitExpansionAction(Action a, Generator gen) {
  }

  private static void emitExpansionChoice(Choice c, Generator gen) {
    for (java.util.Enumeration enum = c.choices.elements();
	 enum.hasMoreElements();) {
      Expansion e = (Expansion)(enum.nextElement());
      emitExpansionTree(e, gen);
      if (enum.hasMoreElements()) {
	gen.text(" | ");
      }
    }
  }

  private static void emitExpansionLookahead(Lookahead l, Generator gen) {
  }

  private static void emitExpansionNonTerminal(NonTerminal nt, Generator gen) {
    gen.nonTerminalStart(nt);
    gen.text(nt.name);
    gen.nonTerminalEnd(nt);
  }

  private static void emitExpansionOneOrMore(OneOrMore o, Generator gen) {
    gen.text("( ");
    emitExpansionTree(o.expansion, gen);
    gen.text(" )+");
  }

  private static void emitExpansionRegularExpression(RegularExpression r,
						     Generator gen) {
    gen.reStart(r);
    emitRE(r, gen);
    gen.reEnd(r);
  }

  private static void emitExpansionSequence(Sequence s, Generator gen) {
    boolean firstUnit = true;
    for (java.util.Enumeration enum = s.units.elements();
	 enum.hasMoreElements();) {
      Expansion e = (Expansion)enum.nextElement();

      if (e instanceof Lookahead || e instanceof Action) {
	continue;
      }
      
      if (!firstUnit) {
	gen.text(" ");
      }

      boolean needParens = (e instanceof Choice) || (e instanceof Sequence);
      if (needParens) {
	gen.text("( ");
      }
      emitExpansionTree(e, gen);
      if (needParens) {
	gen.text(" )");
      }

      firstUnit = false;
    }
  }

  private static void emitExpansionTryBlock(TryBlock t, Generator gen) {
    boolean needParens = t.exp instanceof Choice;
    if (needParens) {
      gen.text("( ");
    }
    emitExpansionTree(t.exp, gen);
    if (needParens) {
      gen.text(" )");
    }
  }

  private static void emitExpansionZeroOrMore(ZeroOrMore z, Generator gen) {
    gen.text("( ");
    emitExpansionTree(z.expansion, gen);
    gen.text(" )*");
  }

  private static void emitExpansionZeroOrOne(ZeroOrOne z, Generator gen) {
    gen.text("( ");
    emitExpansionTree(z.expansion, gen);
    gen.text(" )?");
  }


  private static void emitRE(RegularExpression re, Generator gen) {
    boolean hasLabel = !re.label.equals("");
    boolean justName = re instanceof RJustName;
    boolean eof = re instanceof REndOfFile;
    boolean isString = re instanceof RStringLiteral;
    boolean toplevelRE = (re.tpContext != null);

    boolean needBrackets
      = justName || eof || hasLabel || (!isString && toplevelRE);

    if (needBrackets) {
      gen.text("<");
      if (!justName) {
	if (re.private_rexp) {
	  gen.text("#");
	}
	if (hasLabel) {
	  gen.text(re.label);
	  gen.text(": ");
	}
      }
    }

    if (re instanceof RCharacterList) {
      RCharacterList cl = (RCharacterList)re;
      if (cl.negated_list) {
	gen.text("~");
      }
      gen.text("[");
      for (java.util.Enumeration enum = cl.descriptors.elements();
	   enum.hasMoreElements();) {
	Object o = enum.nextElement();
	if (o instanceof SingleCharacter) {
	  gen.text("\"");
	  char s[] = { ((SingleCharacter)o).ch };
	  gen.text(add_escapes(new String(s)));
	  gen.text("\"");
	} else if (o instanceof CharacterRange) {
	  gen.text("\"");
	  char s[] = { ((CharacterRange)o).left };
	  gen.text(add_escapes(new String(s)));
	  gen.text("\"-\"");
	  s[0] = ((CharacterRange)o).right;
	  gen.text(add_escapes(new String(s)));
	  gen.text("\"");
	} else {
	  System.out.println("Oops: unknown character list element type.");
	}
	if (enum.hasMoreElements()) {
	  gen.text(",");
	}
      }
      gen.text("]");

    } else if (re instanceof RChoice) {
      RChoice c = (RChoice)re;
      for (java.util.Enumeration enum = c.choices.elements();
	   enum.hasMoreElements();) {
	RegularExpression sub = (RegularExpression)(enum.nextElement());
	emitRE(sub, gen);
	if (enum.hasMoreElements()) {
	  gen.text(" | ");
	}
      }

    } else if (re instanceof REndOfFile) {
      gen.text("EOF");

    } else if (re instanceof RJustName) {
      RJustName jn = (RJustName)re;
      gen.text(jn.label);

    } else if (re instanceof ROneOrMore) {
      ROneOrMore om = (ROneOrMore)re;
      gen.text("(");
      emitRE(om.regexpr, gen);
      gen.text(")+");

    } else if (re instanceof RSequence) {
      RSequence s = (RSequence)re;
      for (java.util.Enumeration enum = s.units.elements();
	   enum.hasMoreElements();) {
	RegularExpression sub = (RegularExpression)(enum.nextElement());
	boolean needParens = false;
	if (sub instanceof RChoice) {
	  needParens = true;
	}
	if (needParens) {
	  gen.text("(");
	}
	emitRE(sub, gen);
	if (needParens) {
	  gen.text(")");
	}
 	if (enum.hasMoreElements()) {
	  gen.text(" ");
	}
      }

    } else if (re instanceof RStringLiteral) {
      RStringLiteral sl = (RStringLiteral)re;
      gen.text("\"" + JavaCCParserInternals.add_escapes(sl.image) + "\"");

    } else if (re instanceof RZeroOrMore) {
      RZeroOrMore zm = (RZeroOrMore)re;
      gen.text("(");
      emitRE(zm.regexpr, gen);
      gen.text(")*");

    } else if (re instanceof RZeroOrOne) {
      RZeroOrOne zo = (RZeroOrOne)re;
      gen.text("(");
      emitRE(zo.regexpr, gen);
      gen.text(")?");

    } else {
      System.out.println("Oops: Unknown regular expression type.");
    }

    if (needBrackets) {
      gen.text(">");
    }
  }


  private static String v2s(Vector v, boolean newLine) {
    String s = "";
    boolean firstToken = true;
    for (Enumeration enum = v.elements(); enum.hasMoreElements();) {
      Token tok = (Token)enum.nextElement();
      Token stok = getPrecedingSpecialToken(tok);
      
      if (firstToken) {
	if (stok != null) {
	  cline = stok.beginLine;
	  ccol = stok.beginColumn;
	} else {
	  cline = tok.beginLine;
	  ccol = tok.beginColumn;
	}
	s = ws(ccol - 1);
	firstToken = false;
      }
      while (stok != null) {
	s += printToken(stok);
	stok = stok.next;
      }
      s += printToken(tok);
    }
    return s;
  }


  /**
   * Create an output stream for the generated Jack code.  Try to open
   * a file based on the name of the parser, but if that fails use the
   * standard output stream.
   */
  private static PrintWriter create_output_stream() {
    PrintWriter ostr;

    if (JJDocOptions.getOutputFile().equals("")) {
      if (JJDocGlobals.input_file.equals("standard input")) {
	return new java.io.PrintWriter(new java.io.OutputStreamWriter(System.out));
      } else {
	String ext = ".html";
	if (JJDocOptions.getText()) {
	  ext = ".txt";
	}
	int i = JJDocGlobals.input_file.lastIndexOf('.');
	if (i == -1) {
	  JJDocGlobals.output_file = JJDocGlobals.input_file + ext;
	} else {
	  String suffix = JJDocGlobals.input_file.substring(i);
	  if (suffix.equals(ext)) {
	    JJDocGlobals.output_file = JJDocGlobals.input_file + ext;
	  } else {
	    JJDocGlobals.output_file = JJDocGlobals.input_file.substring(0, i) + ext;
	  }
	}
      }
    } else {
      JJDocGlobals.output_file = JJDocOptions.getOutputFile();
    }

    try {
      ostr = new java.io.PrintWriter(new java.io.FileWriter(JJDocGlobals.output_file));
    } catch (java.io.IOException e) {
      System.err.println("JJDoc: can't open output stream on file " +
			 JJDocGlobals.output_file + ".  Using standard output.");
      ostr = new java.io.PrintWriter(new java.io.OutputStreamWriter(System.out));
    }

    return ostr;
  }


  /**
   * A utility to produce a string of blanks.
   */
  private static String ws(int len) {
    String s = "";
    for (int i = 0; i < len; ++i) {
      s += " ";
    }
    return s;
  }


}
	
