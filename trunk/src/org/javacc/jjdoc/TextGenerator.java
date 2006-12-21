
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

import org.javacc.parser.Expansion;
import org.javacc.parser.JavaCodeProduction;
import org.javacc.parser.NonTerminal;
import org.javacc.parser.NormalProduction;
import org.javacc.parser.RegularExpression;

public class TextGenerator implements Generator {
  protected PrintWriter ostr;

  public TextGenerator() {
    ostr = create_output_stream();
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#text(java.lang.String)
   */
  public void text(String s) {
    print(s);
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#print(java.lang.String)
   */
  public void print(String s) {
    ostr.print(s);
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#specialTokens(java.lang.String)
   */
  public void specialTokens(String s) {
    ostr.print(s);
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#documentStart()
   */
  public void documentStart() {
    ostr.print("\nDOCUMENT START\n");
  }
  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#documentEnd()
   */
  public void documentEnd() {
    ostr.print("\nDOCUMENT END\n");
    ostr.close();
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#nonterminalsStart()
   */
  public void nonterminalsStart() {
    text("NON-TERMINALS\n");
  }
  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#nonterminalsEnd()
   */
  public void nonterminalsEnd() {
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#tokensStart()
   */
  public void tokensStart() {
    text("TOKENS\n");
  }
  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#tokensEnd()
   */
  public void tokensEnd() {
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#javacode(org.javacc.parser.JavaCodeProduction)
   */
  public void javacode(JavaCodeProduction jp) {
    productionStart(jp);
    text("java code");
    productionEnd(jp);
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#productionStart(org.javacc.parser.NormalProduction)
   */
  public void productionStart(NormalProduction np) {
    ostr.print("\t" + np.lhs + "\t:=\t");
  }
  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#productionEnd(org.javacc.parser.NormalProduction)
   */
  public void productionEnd(NormalProduction np) {
    ostr.print("\n");
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#expansionStart(org.javacc.parser.Expansion, boolean)
   */
  public void expansionStart(Expansion e, boolean first) {
    if (!first) {
      ostr.print("\n\t\t|\t");
    }
  }
  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#expansionEnd(org.javacc.parser.Expansion, boolean)
   */
  public void expansionEnd(Expansion e, boolean first) {
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#nonTerminalStart(org.javacc.parser.NonTerminal)
   */
  public void nonTerminalStart(NonTerminal nt) {
  }
  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#nonTerminalEnd(org.javacc.parser.NonTerminal)
   */
  public void nonTerminalEnd(NonTerminal nt) {
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#reStart(org.javacc.parser.RegularExpression)
   */
  public void reStart(RegularExpression r) {
  }
  /**
   * {@inheritDoc}
   * @see org.javacc.jjdoc.Generator#reEnd(org.javacc.parser.RegularExpression)
   */
  public void reEnd(RegularExpression r) {
  }

  /**
   * Create an output stream for the generated Jack code. Try to open a file
   * based on the name of the parser, but if that fails use the standard output
   * stream.
   */
  protected PrintWriter create_output_stream() {
    PrintWriter ostr;

    if (JJDocOptions.getOutputFile().equals("")) {
      if (JJDocGlobals.input_file.equals("standard input")) {
        return new java.io.PrintWriter(
                                       new java.io.OutputStreamWriter(
                                                                      System.out));
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
            JJDocGlobals.output_file = JJDocGlobals.input_file.substring(0, i)
                + ext;
          }
        }
      }
    } else {
      JJDocGlobals.output_file = JJDocOptions.getOutputFile();
    }

    try {
      ostr = new java.io.PrintWriter(
                                     new java.io.FileWriter(
                                                            JJDocGlobals.output_file));
    } catch (java.io.IOException e) {
      System.err.println("JJDoc: can't open output stream on file "
          + JJDocGlobals.output_file + ".  Using standard output.");
      ostr = new java.io.PrintWriter(new java.io.OutputStreamWriter(System.out));
    }

    return ostr;
  }

}
