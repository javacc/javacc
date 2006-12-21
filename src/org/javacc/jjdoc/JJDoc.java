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
package org.javacc.jjdoc;
import java.util.Enumeration;
import java.util.Vector;
import org.javacc.parser.Action;
import org.javacc.parser.BNFProduction;
import org.javacc.parser.CharacterRange;
import org.javacc.parser.Choice;
import org.javacc.parser.Expansion;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.JavaCCParserInternals;
import org.javacc.parser.JavaCodeProduction;
import org.javacc.parser.Lookahead;
import org.javacc.parser.NonTerminal;
import org.javacc.parser.NormalProduction;
import org.javacc.parser.OneOrMore;
import org.javacc.parser.RCharacterList;
import org.javacc.parser.RChoice;
import org.javacc.parser.REndOfFile;
import org.javacc.parser.RJustName;
import org.javacc.parser.ROneOrMore;
import org.javacc.parser.RSequence;
import org.javacc.parser.RStringLiteral;
import org.javacc.parser.RZeroOrMore;
import org.javacc.parser.RZeroOrOne;
import org.javacc.parser.RegularExpression;
import org.javacc.parser.Sequence;
import org.javacc.parser.SingleCharacter;
import org.javacc.parser.Token;
import org.javacc.parser.TokenProduction;
import org.javacc.parser.TryBlock;
import org.javacc.parser.ZeroOrMore;
import org.javacc.parser.ZeroOrOne;
public class JJDoc extends JavaCCGlobals {
  static Generator generator;
  /**
   * The main entry point for JJDoc.
   */
  static void start() {
    generator = getGenerator();
    generator.documentStart();
    emitTokenProductions(generator, rexprlist);
    emitNormalProductions(generator, bnfproductions);
    generator.documentEnd();
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
    for (Enumeration enumeration = prods.elements(); enumeration.hasMoreElements();) {
      TokenProduction tp = (TokenProduction)enumeration.nextElement();
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
    for (Enumeration enumeration = prods.elements(); enumeration.hasMoreElements();) {
      NormalProduction np = (NormalProduction)enumeration.nextElement();
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
    for (java.util.Enumeration enumeration = c.choices.elements();
	 enumeration.hasMoreElements();) {
      Expansion e = (Expansion)(enumeration.nextElement());
      emitExpansionTree(e, gen);
      if (enumeration.hasMoreElements()) {
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
    for (java.util.Enumeration enumeration = s.units.elements();
	 enumeration.hasMoreElements();) {
      Expansion e = (Expansion)enumeration.nextElement();
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
      for (java.util.Enumeration enumeration = cl.descriptors.elements();
	   enumeration.hasMoreElements();) {
        Object o = enumeration.nextElement();
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
        if (enumeration.hasMoreElements()) {
          gen.text(",");
        }
      }
      gen.text("]");
    } else if (re instanceof RChoice) {
      RChoice c = (RChoice)re;
      for (java.util.Enumeration enumeration = c.choices.elements();
	   enumeration.hasMoreElements();) {
        RegularExpression sub = (RegularExpression)(enumeration.nextElement());
        emitRE(sub, gen);
        if (enumeration.hasMoreElements()) {
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
      for (java.util.Enumeration enumeration = s.units.elements();
	   enumeration.hasMoreElements();) {
        RegularExpression sub = (RegularExpression)(enumeration.nextElement());
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
        if (enumeration.hasMoreElements()) {
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
    for (Enumeration enumeration = v.elements(); enumeration.hasMoreElements();) {
      Token tok = (Token)enumeration.nextElement();
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
   * A utility to produce a string of blanks.
   */
  private static String ws(int len) {
    String s = "";
    for (int i = 0; i < len; ++i) {
      s += " ";
    }
    return s;
  }
  
  /**
   * @return Returns the generator.
   */
  public static Generator getGenerator() {
    if (generator == null) {
      if (JJDocOptions.getText()) {
        generator = new TextGenerator();
      } else {
        generator = new HTMLGenerator();
      }
    }
    return generator;
  }
  /**
   * @param generator
   *        The generator to set.
   */
  public static void setGenerator(Generator generator) {
    JJDoc.generator = generator;
  }
}
