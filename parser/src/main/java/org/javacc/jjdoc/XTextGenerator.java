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

import java.util.Iterator;

import org.javacc.parser.CppCodeProduction;
import org.javacc.parser.Expansion;
import org.javacc.parser.JavaCodeProduction;
import org.javacc.parser.NonTerminal;
import org.javacc.parser.NormalProduction;
import org.javacc.parser.RegExprSpec;
import org.javacc.parser.RegularExpression;
import org.javacc.parser.TokenProduction;

/**
 * Output BNF in HTML 3.2 format.
 */
public class XTextGenerator extends TextGenerator implements Generator {
 
  @Override
  public void handleTokenProduction(TokenProduction tp) {
      
      StringBuilder sb = new StringBuilder();
      
      for (Iterator<RegExprSpec> it2 = tp.respecs.iterator(); it2.hasNext();) {
          RegExprSpec res = it2.next();

          String regularExpressionText = JJDoc.emitRE(res.rexp);
          sb.append(regularExpressionText);

          if (res.nsTok != null) {
              sb.append(" : " + res.nsTok.image);
          }

          sb.append("\n");
//          if (it2.hasNext()) {
//              sb.append("| ");
//          }
        }
      
      //text(sb.toString());
  }

  

  private void println(String s) {
    print(s + "\n");
  }

  @Override
  public void text(String s) {
//    String ss = "";
//    for (int i = 0; i < s.length(); ++i) {
//      if (s.charAt(i) == '<') {
//  ss += "&lt;";
//      } else if (s.charAt(i) == '>') {
//  ss += "&gt;";
//      } else if (s.charAt(i) == '&') {
//  ss += "&amp;";
//      } else {
//  ss += s.charAt(i);
//      }
//    }
    print(s);
  }

  @Override
  public void print(String s) {
    ostr.print(s);
  }

  
  @Override
  public void documentStart() {
    ostr = create_output_stream();
    println("grammar "+JJDocGlobals.input_file+" with org.eclipse.xtext.common.Terminals");
    println("import \"http://www.eclipse.org/emf/2002/Ecore\" as ecore");
    println("");
//    
//    
//    println("<HTML>");
//    println("<HEAD>");
//    if (!"".equals(JJDocOptions.getCSS())) {
//      println("<LINK REL=\"stylesheet\" type=\"text/css\" href=\"" + JJDocOptions.getCSS() + "\"/>");
//    }
//    if (JJDocGlobals.input_file != null) {
//      println("<TITLE>BNF for " + JJDocGlobals.input_file + "</TITLE>");
//    } else {
//      println("<TITLE>A BNF grammar by JJDoc</TITLE>");
//    }
//    println("</HEAD>");
//    println("<BODY>");
//    println("<H1 ALIGN=CENTER>BNF for " + JJDocGlobals.input_file + "</H1>");
  }

  @Override
  public void documentEnd() {
//    println("</BODY>");
//    println("</HTML>");
    ostr.close();
  }

  /**
   * Prints out comments, used for tokens and non-terminals.
   * {@inheritDoc}
   * @see org.javacc.jjdoc.TextGenerator#specialTokens(java.lang.String)
   */
  @Override
  public void specialTokens(String s) {
//    println(" <!-- Special token -->");
//    println(" <TR>");
//    println("  <TD>");
//    println("<PRE>");
    print(s);
//    println("</PRE>");
//    println("  </TD>");
//    println(" </TR>");
  }


  @Override
  public void nonterminalsStart() {
//    println("<H2 ALIGN=CENTER>NON-TERMINALS</H2>");
//    if (JJDocOptions.getOneTable()) {
//      println("<TABLE>");
//    }
  }
  @Override
  public void nonterminalsEnd() {
//    if (JJDocOptions.getOneTable()) {
//      println("</TABLE>");
//    }
  }

  @Override
  public void tokensStart() {
//    println("<H2 ALIGN=CENTER>TOKENS</H2>");
//    println("<TABLE>");
  }
  @Override
  public void tokensEnd() {
//    println("</TABLE>");
  }

  @Override
  public void javacode(JavaCodeProduction jp) {
//    productionStart(jp);
//    println("<I>java code</I></TD></TR>");
//    productionEnd(jp);
  }

  @Override
  public void cppcode(CppCodeProduction cp) {
//    productionStart(cp);
//    println("<I>c++ code</I></TD></TR>");
//    productionEnd(cp);
  }

  @Override
  public void productionStart(NormalProduction np) {
//    if (!JJDocOptions.getOneTable()) {
//      println("");
//      println("<TABLE ALIGN=CENTER>");
//      println("<CAPTION><STRONG>" + np.getLhs() + "</STRONG></CAPTION>");
//    }
//    println("<TR>");
//    println("<TD ALIGN=RIGHT VALIGN=BASELINE><A NAME=\"" + get_id(np.getLhs()) + "\">" + np.getLhs() + "</A></TD>");
//    println("<TD ALIGN=CENTER VALIGN=BASELINE>::=</TD>");
//    print("<TD ALIGN=LEFT VALIGN=BASELINE>");
  }
  @Override
  public void productionEnd(NormalProduction np) {
//    if (!JJDocOptions.getOneTable()) {
//      println("</TABLE>");
//      println("<HR>");
//    }
  }

  @Override
  public void expansionStart(Expansion e, boolean first) {
//      
//      
//      
//    if (!first) {
//      println("<TR>");
//      println("<TD ALIGN=RIGHT VALIGN=BASELINE></TD>");
//      println("<TD ALIGN=CENTER VALIGN=BASELINE>|</TD>");
//      print("<TD ALIGN=LEFT VALIGN=BASELINE>");
//    }
  }
  @Override
  public void expansionEnd(Expansion e, boolean first) {
    println(";");
  }

  @Override
  public void nonTerminalStart(NonTerminal nt) {
    print("terminal ");
  }
  @Override
  public void nonTerminalEnd(NonTerminal nt) {
    print(";");
  }

  @Override
  public void reStart(RegularExpression r) {
  }
  @Override
  public void reEnd(RegularExpression r) {
  }
  
  
  
}
