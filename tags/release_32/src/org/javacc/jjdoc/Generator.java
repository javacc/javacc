
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

import org.javacc.parser.*;

public class Generator {
  protected PrintWriter ostr;

  public Generator(PrintWriter o) {
    ostr = o;
  }

  public void text(String s) {
    print(s);
  }

  public void print(String s) {
    ostr.print(s);
  }

  public void specialTokens(String s) {
    ostr.print(s);
  }

  public void documentStart() {
    ostr.print("\nDOCUMENT START\n");
  }
  public void documentEnd() {
    ostr.print("\nDOCUMENT END\n");
  }

  public void nonterminalsStart() {
    text("NON-TERMINALS\n");
  }
  public void nonterminalsEnd() {
  }

  public void tokensStart() {
    text("TOKENS\n");
  }
  public void tokensEnd() {
  }

  public void javacode(JavaCodeProduction jp) {
    productionStart(jp);
    text("java code");
    productionEnd(jp);
  }

  public void productionStart(NormalProduction np) {
    ostr.print("\t" + np.lhs + "\t:=\t");
  }
  public void productionEnd(NormalProduction np) {
    ostr.print("\n");
  }

  public void expansionStart(Expansion e, boolean first) {
    if (!first) {
      ostr.print("\n\t\t|\t");
    }
  }
  public void expansionEnd(Expansion e, boolean first) {
  }

  public void nonTerminalStart(NonTerminal nt) {
  }
  public void nonTerminalEnd(NonTerminal nt) {
  }

  public void reStart(RegularExpression r) {
  }
  public void reEnd(RegularExpression r) {
  }
}
