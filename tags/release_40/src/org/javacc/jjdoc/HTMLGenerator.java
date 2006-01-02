
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
import java.util.Hashtable;

import org.javacc.parser.*;

public class HTMLGenerator extends Generator {
  private Hashtable id_map = new Hashtable();
  private int id = 1;

  public HTMLGenerator(PrintWriter o) {
    super(o);
  }

  private String get_id(String nt) {
    String i = (String)id_map.get(nt);
    if (i == null) {
      i = "prod" + id++;
      id_map.put(nt, i);
    }
    return i;
  }

  private void println(String s) {
    print(s + "\n");
  }

  public void text(String s) {
    String ss = "";
    for (int i = 0; i < s.length(); ++i) {
      if (s.charAt(i) == '<') {
	ss += "&lt;";
      } else if (s.charAt(i) == '>') {
	ss += "&gt;";
      } else if (s.charAt(i) == '&') {
	ss += "&amp;";
      } else {
	ss += s.charAt(i);
      }
    }
    print(ss);
  }

  public void print(String s) {
    ostr.print(s);
  }

  public void specialTokens(String s) {
    if (!JJDocOptions.getOneTable()) {
      println("<PRE>");
      print(s);
      println("</PRE>");
    }
  }

  public void documentStart() {
    println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">");
    println("<HTML>");
    println("<HEAD>");
    if (JJDocGlobals.input_file != null) {
      println("<TITLE>BNF for " + JJDocGlobals.input_file + "</TITLE>");
    } else {
      println("<TITLE>A BNF grammar by JJDoc</TITLE>");
    }
    println("</HEAD>");
    println("<BODY>");
    println("<H1 ALIGN=CENTER>BNF for " + JJDocGlobals.input_file + "</H1>");
  }
  public void documentEnd() {
    println("</BODY>");
    println("</HTML>");
  }

  public void nonterminalsStart() {
    println("<H2 ALIGN=CENTER>NON-TERMINALS</H2>");
    if (JJDocOptions.getOneTable()) {
      println("<TABLE>");
    }
  }
  public void nonterminalsEnd() {
    if (JJDocOptions.getOneTable()) {
      println("</TABLE>");
    }
  }

  public void tokensStart() {
    println("<H2 ALIGN=CENTER>TOKENS</H2>");
    println("<TABLE>");
  }
  public void tokensEnd() {
    println("</TABLE>");
  }

  public void javacode(JavaCodeProduction jp) {
    productionStart(jp);
    println("<I>java code</I></TD></TR>");
    productionEnd(jp);
  }

  public void productionStart(NormalProduction np) {
    if (!JJDocOptions.getOneTable()) {
      println("");
      println("<TABLE ALIGN=CENTER>");
      println("<CAPTION><STRONG>" + np.lhs + "</STRONG></CAPTION>");
    }
    println("<TR>");
    println("<TD ALIGN=RIGHT VALIGN=BASELINE><A NAME=\"" + get_id(np.lhs) + "\">" + np.lhs + "</A></TD>");
    println("<TD ALIGN=CENTER VALIGN=BASELINE>::=</TD>");
    print("<TD ALIGN=LEFT VALIGN=BASELINE>");
  }
  public void productionEnd(NormalProduction np) {
    if (!JJDocOptions.getOneTable()) {
      println("</TABLE>");
      println("<HR>");
    }
  }

  public void expansionStart(Expansion e, boolean first) {
    if (!first) {
      println("<TR>");
      println("<TD ALIGN=RIGHT VALIGN=BASELINE></TD>");
      println("<TD ALIGN=CENTER VALIGN=BASELINE>|</TD>");
      print("<TD ALIGN=LEFT VALIGN=BASELINE>");
    }
  }
  public void expansionEnd(Expansion e, boolean first) {
    println("</TD>");
    println("</TR>");
  }

  public void nonTerminalStart(NonTerminal nt) {
    print("<A HREF=\"#" + get_id(nt.name) + "\">");
  }
  public void nonTerminalEnd(NonTerminal nt) {
    print("</A>");
  }

  public void reStart(RegularExpression r) {
  }
  public void reEnd(RegularExpression r) {
  }
}
