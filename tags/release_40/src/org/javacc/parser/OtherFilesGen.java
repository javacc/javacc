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

public class OtherFilesGen extends JavaCCGlobals implements JavaCCParserConstants {

  public static boolean keepLineCol;
  static public void start() throws MetaParseException {

    Token t = null;
    keepLineCol = Options.getKeepLineColumn();

    if (JavaCCErrors.get_error_count() != 0) throw new MetaParseException();

    JavaFiles.gen_TokenMgrError();
    JavaFiles.gen_ParseException();
    JavaFiles.gen_Token();
    if (Options.getUserTokenManager()) {
      JavaFiles.gen_TokenManager();
    } else if (Options.getUserCharStream()) {
      JavaFiles.gen_CharStream();
    } else {
      if (Options.getJavaUnicodeEscape()) {
        JavaFiles.gen_JavaCharStream();
      } else {
        JavaFiles.gen_SimpleCharStream();
      }
    }

    try {
      ostr = new java.io.PrintWriter(
                new java.io.BufferedWriter(
                   new java.io.FileWriter(
                     new java.io.File(Options.getOutputDirectory(), cu_name + "Constants.java")
                   ),
                   8192
                )
             );
    } catch (java.io.IOException e) {
      JavaCCErrors.semantic_error("Could not open file " + cu_name + "Constants.java for writing.");
      throw new Error();
    }

    java.util.Vector tn = (java.util.Vector)(toolNames.clone());
    tn.addElement(toolName);
    ostr.println("/* " + getIdString(tn, cu_name + "Constants.java") + " */");

    if (cu_to_insertion_point_1.size() != 0 &&
        ((Token)cu_to_insertion_point_1.elementAt(0)).kind == PACKAGE
       ) {
      for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
        if (((Token)cu_to_insertion_point_1.elementAt(i)).kind == SEMICOLON) {
          printTokenSetup((Token)(cu_to_insertion_point_1.elementAt(0)));
          for (int j = 0; j <= i; j++) {
            t = (Token)(cu_to_insertion_point_1.elementAt(j));
            printToken(t, ostr);
          }
          printTrailingComments(t, ostr);
          ostr.println("");
          ostr.println("");
          break;
        }
      }
    }
    ostr.println("public interface " + cu_name + "Constants {");
    ostr.println("");
    RegularExpression re;
    ostr.println("  int EOF = 0;");
    for (java.util.Enumeration enum = ordered_named_tokens.elements(); enum.hasMoreElements();) {
      re = (RegularExpression)enum.nextElement();
      ostr.println("  int " + re.label + " = " + re.ordinal + ";");
    }
    ostr.println("");
    if (!Options.getUserTokenManager() && Options.getBuildTokenManager()) {
      for (int i = 0; i < LexGen.lexStateName.length; i++) {
        ostr.println("  int " + LexGen.lexStateName[i] + " = " + i + ";");
      }
      ostr.println("");
    }
    ostr.println("  String[] tokenImage = {");
    ostr.println("    \"<EOF>\",");

    for (java.util.Enumeration enum = rexprlist.elements(); enum.hasMoreElements();) {
      TokenProduction tp = (TokenProduction)(enum.nextElement());
      java.util.Vector respecs = tp.respecs;
      for (java.util.Enumeration enum1 = respecs.elements(); enum1.hasMoreElements();) {
        RegExprSpec res = (RegExprSpec)(enum1.nextElement());
        re = res.rexp;
        if (re instanceof RStringLiteral) {
          ostr.println("    \"\\\"" + add_escapes(add_escapes(((RStringLiteral)re).image)) + "\\\"\",");
        } else if (!re.label.equals("")) {
          ostr.println("    \"<" + re.label + ">\",");
        } else {
          if (re.tpContext.kind == TokenProduction.TOKEN) {
            JavaCCErrors.warning(re, "Consider giving this non-string token a label for better error reporting.");
          }
          ostr.println("    \"<token of kind " + re.ordinal + ">\",");
        }

      }
    }
    ostr.println("  };");
    ostr.println("");
    ostr.println("}");

    ostr.close();

  }

  static private java.io.PrintWriter ostr;

   public static void reInit()
   {
      ostr = null;
   }

}
