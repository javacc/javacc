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

import java.util.*;
import java.io.*;

public class ParseGen extends JavaCCGlobals implements JavaCCParserConstants {

  static public void start() throws MetaParseException {

    Token t = null;

    if (JavaCCErrors.get_error_count() != 0) throw new MetaParseException();

    if (Options.B("BUILD_PARSER")) {

      try {
        ostr = new PrintWriter(
                  new BufferedWriter(
                     new FileWriter(
                       new File(outputDir, cu_name + ".java")
                     ),
                     8192
                  )
               );
      } catch (IOException e) {
        JavaCCErrors.semantic_error("Could not open file " + cu_name + ".java for writing.");
        throw new Error();
      }

      Vector tn = (Vector)(toolNames.clone());
      tn.addElement(toolName);
      ostr.println("/* " + getIdString(tn, cu_name + ".java") + " */");

      boolean implementsExists = false;

      if (cu_to_insertion_point_1.size() != 0) {
        printTokenSetup((Token)(cu_to_insertion_point_1.elementAt(0))); ccol = 1;
        for (Enumeration enum = cu_to_insertion_point_1.elements(); enum.hasMoreElements();) {
          t = (Token)enum.nextElement();
          if (t.kind == IMPLEMENTS) {
            implementsExists = true;
          } else if (t.kind == CLASS) {
            implementsExists = false;
          }
          printToken(t, ostr);
        }
      }
      if (implementsExists) {
        ostr.print(", ");
      } else {
        ostr.print(" implements ");
      }
      ostr.print(cu_name + "Constants ");
      if (cu_to_insertion_point_2.size() != 0) {
        printTokenSetup((Token)(cu_to_insertion_point_2.elementAt(0)));
        for (Enumeration enum = cu_to_insertion_point_2.elements(); enum.hasMoreElements();) {
          t = (Token)enum.nextElement();
          printToken(t, ostr);
        }
      }

      ostr.println("");
      ostr.println("");

      ParseEngine.build(ostr);

      if (Options.B("STATIC")) {
        ostr.println("  static private boolean jj_initialized_once = false;");
      }
      if (Options.B("USER_TOKEN_MANAGER")) {
        ostr.println("  " + staticOpt() + "public TokenManager token_source;");
      } else {
        ostr.println("  " + staticOpt() + "public " + cu_name + "TokenManager token_source;");
	if (!Options.B("USER_CHAR_STREAM")) {
	  if (Options.B("JAVA_UNICODE_ESCAPE")) {
	    ostr.println("  " + staticOpt() + "JavaCharStream jj_input_stream;");
	  } else {
	    ostr.println("  " + staticOpt() + "SimpleCharStream jj_input_stream;");
	  }
	}
      }
      ostr.println("  " + staticOpt() + "public Token token, jj_nt;");
      if (!Options.B("CACHE_TOKENS")) {
	ostr.println("  " + staticOpt() + "private int jj_ntk;");
      }
      if (jj2index != 0) {
        ostr.println("  " + staticOpt() + "private Token jj_scanpos, jj_lastpos;");
        ostr.println("  " + staticOpt() + "private int jj_la;");
        ostr.println("  " + staticOpt() + "public boolean lookingAhead = false;");
        ostr.println("  " + staticOpt() + "private boolean jj_semLA;");
      }
      if (Options.B("ERROR_REPORTING")) {
        ostr.println("  " + staticOpt() + "private int jj_gen;");
        ostr.println("  " + staticOpt() + "final private int[] jj_la1 = new int[" + maskindex + "];");
	int tokenMaskSize = (tokenCount-1)/32 + 1;
	for (int i = 0; i < tokenMaskSize; i++) 
	  ostr.println("  static private int[] jj_la1_" + i + ";");
	ostr.println("  static {");
	for (int i = 0; i < tokenMaskSize; i++) 
	  ostr.println("      jj_la1_" + i + "();");
	ostr.println("   }");
	for (int i = 0; i < tokenMaskSize; i++) {
	  ostr.println("   private static void jj_la1_" + i + "() {");
	  ostr.print("      jj_la1_" + i + " = new int[] {");
	  for (Enumeration enum = maskVals.elements(); enum.hasMoreElements();) {
	    int[] tokenMask = (int[])(enum.nextElement());
	    ostr.print("0x" + Integer.toHexString(tokenMask[i]) + ",");
	  }
	  ostr.println("};");
	  ostr.println("   }");
	}
      }
      if (jj2index != 0 && Options.B("ERROR_REPORTING")) {
        ostr.println("  " + staticOpt() + "final private JJCalls[] jj_2_rtns = new JJCalls[" + jj2index + "];");
        ostr.println("  " + staticOpt() + "private boolean jj_rescan = false;");
        ostr.println("  " + staticOpt() + "private int jj_gc = 0;");
      }
      ostr.println("");

      if (!Options.B("USER_TOKEN_MANAGER")) {
        if (Options.B("USER_CHAR_STREAM")) {
          ostr.println("  public " + cu_name + "(CharStream stream) {");
          if (Options.B("STATIC")) {
            ostr.println("    if (jj_initialized_once) {");
            ostr.println("      System.out.println(\"ERROR: Second call to constructor of static parser.  You must\");");
            ostr.println("      System.out.println(\"       either use ReInit() or set the JavaCC option STATIC to false\");");
            ostr.println("      System.out.println(\"       during parser generation.\");");
            ostr.println("      throw new Error();");
            ostr.println("    }");
            ostr.println("    jj_initialized_once = true;");
          }
          ostr.println("    token_source = new " + cu_name + "TokenManager(stream);");
          ostr.println("    token = new Token();");
	  if (Options.B("CACHE_TOKENS")) {
	    ostr.println("    token.next = jj_nt = token_source.getNextToken();");
	  } else {
	    ostr.println("    jj_ntk = -1;");
	  }
          if (Options.B("ERROR_REPORTING")) {
            ostr.println("    jj_gen = 0;");
            ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
            if (jj2index != 0) {
              ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          ostr.println("  }");
          ostr.println("");
          ostr.println("  " + staticOpt() + "public void ReInit(CharStream stream) {");
          ostr.println("    token_source.ReInit(stream);");
          ostr.println("    token = new Token();");
	  if (Options.B("CACHE_TOKENS")) {
	    ostr.println("    token.next = jj_nt = token_source.getNextToken();");
	  } else {
	    ostr.println("    jj_ntk = -1;");
	  }
          if (jjtreeGenerated) {
            ostr.println("    jjtree.reset();");
          }
          if (Options.B("ERROR_REPORTING")) {
            ostr.println("    jj_gen = 0;");
            ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
            if (jj2index != 0) {
              ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          ostr.println("  }");
        } else {
          ostr.println("  public " + cu_name + "(java.io.InputStream stream) {");
          if (Options.B("STATIC")) {
            ostr.println("    if (jj_initialized_once) {");
            ostr.println("      System.out.println(\"ERROR: Second call to constructor of static parser.  You must\");");
            ostr.println("      System.out.println(\"       either use ReInit() or set the JavaCC option STATIC to false\");");
            ostr.println("      System.out.println(\"       during parser generation.\");");
            ostr.println("      throw new Error();");
            ostr.println("    }");
            ostr.println("    jj_initialized_once = true;");
          }
          if (Options.B("JAVA_UNICODE_ESCAPE")) {
            ostr.println("    jj_input_stream = new JavaCharStream(stream, 1, 1);");
          } else {
            ostr.println("    jj_input_stream = new SimpleCharStream(stream, 1, 1);");
          }
          ostr.println("    token_source = new " + cu_name + "TokenManager(jj_input_stream);");
          ostr.println("    token = new Token();");
	  if (Options.B("CACHE_TOKENS")) {
	    ostr.println("    token.next = jj_nt = token_source.getNextToken();");
	  } else {
	    ostr.println("    jj_ntk = -1;");
	  }
          if (Options.B("ERROR_REPORTING")) {
            ostr.println("    jj_gen = 0;");
            ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
            if (jj2index != 0) {
              ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          ostr.println("  }");
          ostr.println("");
          ostr.println("  " + staticOpt() + "public void ReInit(java.io.InputStream stream) {");
          if (Options.B("JAVA_UNICODE_ESCAPE")) {
            ostr.println("    jj_input_stream.ReInit(stream, 1, 1);");
          } else {
            ostr.println("    jj_input_stream.ReInit(stream, 1, 1);");
          }
          ostr.println("    token_source.ReInit(jj_input_stream);");
          ostr.println("    token = new Token();");
	  if (Options.B("CACHE_TOKENS")) {
	    ostr.println("    token.next = jj_nt = token_source.getNextToken();");
	  } else {
	    ostr.println("    jj_ntk = -1;");
	  }
          if (jjtreeGenerated) {
            ostr.println("    jjtree.reset();");
          }
          if (Options.B("ERROR_REPORTING")) {
            ostr.println("    jj_gen = 0;");
            ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
            if (jj2index != 0) {
              ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          ostr.println("  }");
	  ostr.println("");
          ostr.println("  public " + cu_name + "(java.io.Reader stream) {");
          if (Options.B("STATIC")) {
            ostr.println("    if (jj_initialized_once) {");
            ostr.println("      System.out.println(\"ERROR: Second call to constructor of static parser.  You must\");");
            ostr.println("      System.out.println(\"       either use ReInit() or set the JavaCC option STATIC to false\");");
            ostr.println("      System.out.println(\"       during parser generation.\");");
            ostr.println("      throw new Error();");
            ostr.println("    }");
            ostr.println("    jj_initialized_once = true;");
          }
          if (Options.B("JAVA_UNICODE_ESCAPE")) {
            ostr.println("    jj_input_stream = new JavaCharStream(stream, 1, 1);");
          } else {
            ostr.println("    jj_input_stream = new SimpleCharStream(stream, 1, 1);");
          }
          ostr.println("    token_source = new " + cu_name + "TokenManager(jj_input_stream);");
          ostr.println("    token = new Token();");
	  if (Options.B("CACHE_TOKENS")) {
	    ostr.println("    token.next = jj_nt = token_source.getNextToken();");
	  } else {
	    ostr.println("    jj_ntk = -1;");
	  }
          if (Options.B("ERROR_REPORTING")) {
            ostr.println("    jj_gen = 0;");
            ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
            if (jj2index != 0) {
              ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          ostr.println("  }");
          ostr.println("");
          ostr.println("  " + staticOpt() + "public void ReInit(java.io.Reader stream) {");
          if (Options.B("JAVA_UNICODE_ESCAPE")) {
            ostr.println("    jj_input_stream.ReInit(stream, 1, 1);");
          } else {
            ostr.println("    jj_input_stream.ReInit(stream, 1, 1);");
          }
          ostr.println("    token_source.ReInit(jj_input_stream);");
          ostr.println("    token = new Token();");
	  if (Options.B("CACHE_TOKENS")) {
	    ostr.println("    token.next = jj_nt = token_source.getNextToken();");
	  } else {
	    ostr.println("    jj_ntk = -1;");
	  }
          if (jjtreeGenerated) {
            ostr.println("    jjtree.reset();");
          }
          if (Options.B("ERROR_REPORTING")) {
            ostr.println("    jj_gen = 0;");
            ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
            if (jj2index != 0) {
              ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          ostr.println("  }");
        }
      }
      ostr.println("");
      if (Options.B("USER_TOKEN_MANAGER")) {
        ostr.println("  public " + cu_name + "(TokenManager tm) {");
      } else {
        ostr.println("  public " + cu_name + "(" + cu_name + "TokenManager tm) {");
      }
      if (Options.B("STATIC")) {
        ostr.println("    if (jj_initialized_once) {");
        ostr.println("      System.out.println(\"ERROR: Second call to constructor of static parser.  You must\");");
        ostr.println("      System.out.println(\"       either use ReInit() or set the JavaCC option STATIC to false\");");
        ostr.println("      System.out.println(\"       during parser generation.\");");
        ostr.println("      throw new Error();");
        ostr.println("    }");
        ostr.println("    jj_initialized_once = true;");
      }
      ostr.println("    token_source = tm;");
      ostr.println("    token = new Token();");
      if (Options.B("CACHE_TOKENS")) {
	ostr.println("    token.next = jj_nt = token_source.getNextToken();");
      } else {
	ostr.println("    jj_ntk = -1;");
      }
      if (Options.B("ERROR_REPORTING")) {
        ostr.println("    jj_gen = 0;");
        ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
        if (jj2index != 0) {
          ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
        }
      }
      ostr.println("  }");
      ostr.println("");
      if (Options.B("USER_TOKEN_MANAGER")) {
        ostr.println("  public void ReInit(TokenManager tm) {");
      } else {
        ostr.println("  public void ReInit(" + cu_name + "TokenManager tm) {");
      }
      ostr.println("    token_source = tm;");
      ostr.println("    token = new Token();");
      if (Options.B("CACHE_TOKENS")) {
	ostr.println("    token.next = jj_nt = token_source.getNextToken();");
      } else {
	ostr.println("    jj_ntk = -1;");
      }
      if (jjtreeGenerated) {
        ostr.println("    jjtree.reset();");
      }
      if (Options.B("ERROR_REPORTING")) {
        ostr.println("    jj_gen = 0;");
        ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
        if (jj2index != 0) {
          ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
        }
      }
      ostr.println("  }");
      ostr.println("");
      ostr.println("  " + staticOpt() + "final private Token jj_consume_token(int kind) throws ParseException {");
      if (Options.B("CACHE_TOKENS")) {
	ostr.println("    Token oldToken = token;");
	ostr.println("    if ((token = jj_nt).next != null) jj_nt = jj_nt.next;");
	ostr.println("    else jj_nt = jj_nt.next = token_source.getNextToken();");
      } else {
	ostr.println("    Token oldToken;");
	ostr.println("    if ((oldToken = token).next != null) token = token.next;");
	ostr.println("    else token = token.next = token_source.getNextToken();");
	ostr.println("    jj_ntk = -1;");
      }
      ostr.println("    if (token.kind == kind) {");
      if (Options.B("ERROR_REPORTING")) {
        ostr.println("      jj_gen++;");
	if (jj2index != 0) {
	  ostr.println("      if (++jj_gc > 100) {");
	  ostr.println("        jj_gc = 0;");
	  ostr.println("        for (int i = 0; i < jj_2_rtns.length; i++) {");
	  ostr.println("          JJCalls c = jj_2_rtns[i];");
	  ostr.println("          while (c != null) {");
	  ostr.println("            if (c.gen < jj_gen) c.first = null;");
	  ostr.println("            c = c.next;");
	  ostr.println("          }");
	  ostr.println("        }");
	  ostr.println("      }");
	}
      }
      if (Options.B("DEBUG_PARSER")) {
        ostr.println("      trace_token(token, \"\");");
      }
      ostr.println("      return token;");
      ostr.println("    }");
      if (Options.B("CACHE_TOKENS")) {
	ostr.println("    jj_nt = token;");
      }
      ostr.println("    token = oldToken;");
      if (Options.B("ERROR_REPORTING")) {
        ostr.println("    jj_kind = kind;");
      }
      ostr.println("    throw generateParseException();");
      ostr.println("  }");
      ostr.println("");
      if (jj2index != 0) {
        ostr.println("  static private final class LookaheadSuccess extends java.lang.Error { }");
        ostr.println("  " + staticOpt() + "final private LookaheadSuccess jj_ls = new LookaheadSuccess();");
        ostr.println("  " + staticOpt() + "final private boolean jj_scan_token(int kind) {");
        ostr.println("    if (jj_scanpos == jj_lastpos) {");
        ostr.println("      jj_la--;");
        ostr.println("      if (jj_scanpos.next == null) {");
        ostr.println("        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();");
        ostr.println("      } else {");
        ostr.println("        jj_lastpos = jj_scanpos = jj_scanpos.next;");
        ostr.println("      }");
        ostr.println("    } else {");
        ostr.println("      jj_scanpos = jj_scanpos.next;");
        ostr.println("    }");
        if (Options.B("ERROR_REPORTING")) {
          ostr.println("    if (jj_rescan) {");
          ostr.println("      int i = 0; Token tok = token;");
          ostr.println("      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }");
          ostr.println("      if (tok != null) jj_add_error_token(kind, i);");
          if (Options.B("DEBUG_LOOKAHEAD")) {
            ostr.println("    } else {");
            ostr.println("      trace_scan(jj_scanpos, kind);");
          }
          ostr.println("    }");
        } else if (Options.B("DEBUG_LOOKAHEAD")) {
          ostr.println("    trace_scan(jj_scanpos, kind);");
        }
        ostr.println("    if (jj_scanpos.kind != kind) return true;");
        ostr.println("    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;");
        ostr.println("    return false;");
        ostr.println("  }");
        ostr.println("");
      }
      ostr.println("  " + staticOpt() + "final public Token getNextToken() {");
      if (Options.B("CACHE_TOKENS")) {
	ostr.println("    if ((token = jj_nt).next != null) jj_nt = jj_nt.next;");
	ostr.println("    else jj_nt = jj_nt.next = token_source.getNextToken();");
      } else {
	ostr.println("    if (token.next != null) token = token.next;");
	ostr.println("    else token = token.next = token_source.getNextToken();");
	ostr.println("    jj_ntk = -1;");
      }
      if (Options.B("ERROR_REPORTING")) {
        ostr.println("    jj_gen++;");
      }
      if (Options.B("DEBUG_PARSER")) {
        ostr.println("      trace_token(token, \" (in getNextToken)\");");
      }
      ostr.println("    return token;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  " + staticOpt() + "final public Token getToken(int index) {");
      if (jj2index != 0) {
        ostr.println("    Token t = lookingAhead ? jj_scanpos : token;");
      } else {
        ostr.println("    Token t = token;");
      }
      ostr.println("    for (int i = 0; i < index; i++) {");
      ostr.println("      if (t.next != null) t = t.next;");
      ostr.println("      else t = t.next = token_source.getNextToken();");
      ostr.println("    }");
      ostr.println("    return t;");
      ostr.println("  }");
      ostr.println("");
      if (!Options.B("CACHE_TOKENS")) {
	ostr.println("  " + staticOpt() + "final private int jj_ntk() {");
	ostr.println("    if ((jj_nt=token.next) == null)");
	ostr.println("      return (jj_ntk = (token.next=token_source.getNextToken()).kind);");
	ostr.println("    else");
	ostr.println("      return (jj_ntk = jj_nt.kind);");
	ostr.println("  }");
	ostr.println("");
      }
      if (Options.B("ERROR_REPORTING")) {
        ostr.println("  " + staticOpt() + "private java.util.Vector jj_expentries = new java.util.Vector();");
        ostr.println("  " + staticOpt() + "private int[] jj_expentry;");
        ostr.println("  " + staticOpt() + "private int jj_kind = -1;");
        if (jj2index != 0) {
          ostr.println("  " + staticOpt() + "private int[] jj_lasttokens = new int[100];");
          ostr.println("  " + staticOpt() + "private int jj_endpos;");
          ostr.println("");
          ostr.println("  " + staticOpt() + "private void jj_add_error_token(int kind, int pos) {");
          ostr.println("    if (pos >= 100) return;");
          ostr.println("    if (pos == jj_endpos + 1) {");
          ostr.println("      jj_lasttokens[jj_endpos++] = kind;");
          ostr.println("    } else if (jj_endpos != 0) {");
          ostr.println("      jj_expentry = new int[jj_endpos];");
          ostr.println("      for (int i = 0; i < jj_endpos; i++) {");
          ostr.println("        jj_expentry[i] = jj_lasttokens[i];");
          ostr.println("      }");
          ostr.println("      boolean exists = false;");
          ostr.println("      for (java.util.Enumeration e = jj_expentries.elements(); e.hasMoreElements();) {");
          ostr.println("        int[] oldentry = (int[])(e.nextElement());");
          ostr.println("        if (oldentry.length == jj_expentry.length) {");
          ostr.println("          exists = true;");
          ostr.println("          for (int i = 0; i < jj_expentry.length; i++) {");
          ostr.println("            if (oldentry[i] != jj_expentry[i]) {");
          ostr.println("              exists = false;");
          ostr.println("              break;");
          ostr.println("            }");
          ostr.println("          }");
          ostr.println("          if (exists) break;");
          ostr.println("        }");
          ostr.println("      }");
          ostr.println("      if (!exists) jj_expentries.addElement(jj_expentry);");
          ostr.println("      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;");
          ostr.println("    }");
          ostr.println("  }");
        }
        ostr.println("");
        ostr.println("  " + staticOpt() + "public ParseException generateParseException() {");
        ostr.println("    jj_expentries.removeAllElements();");
        ostr.println("    boolean[] la1tokens = new boolean[" + tokenCount + "];");
        ostr.println("    for (int i = 0; i < " + tokenCount + "; i++) {");
        ostr.println("      la1tokens[i] = false;");
        ostr.println("    }");
        ostr.println("    if (jj_kind >= 0) {");
        ostr.println("      la1tokens[jj_kind] = true;");
        ostr.println("      jj_kind = -1;");
        ostr.println("    }");
        ostr.println("    for (int i = 0; i < " + maskindex + "; i++) {");
        ostr.println("      if (jj_la1[i] == jj_gen) {");
        ostr.println("        for (int j = 0; j < 32; j++) {");
	for (int i = 0; i < (tokenCount-1)/32 + 1; i++) {
	  ostr.println("          if ((jj_la1_" + i + "[i] & (1<<j)) != 0) {");
	  ostr.print("            la1tokens[");
	  if (i != 0) {
	    ostr.print((32*i) + "+");
	  }
	  ostr.println("j] = true;");
	  ostr.println("          }");
	}
        ostr.println("        }");
        ostr.println("      }");
        ostr.println("    }");
        ostr.println("    for (int i = 0; i < " + tokenCount + "; i++) {");
        ostr.println("      if (la1tokens[i]) {");
        ostr.println("        jj_expentry = new int[1];");
        ostr.println("        jj_expentry[0] = i;");
        ostr.println("        jj_expentries.addElement(jj_expentry);");
        ostr.println("      }");
        ostr.println("    }");
        if (jj2index != 0) {
          ostr.println("    jj_endpos = 0;");
          ostr.println("    jj_rescan_token();");
          ostr.println("    jj_add_error_token(0, 0);");
        }
        ostr.println("    int[][] exptokseq = new int[jj_expentries.size()][];");
        ostr.println("    for (int i = 0; i < jj_expentries.size(); i++) {");
        ostr.println("      exptokseq[i] = (int[])jj_expentries.elementAt(i);");
        ostr.println("    }");
        ostr.println("    return new ParseException(token, exptokseq, tokenImage);");
        ostr.println("  }");
      } else {
        ostr.println("  " + staticOpt() + "public ParseException generateParseException() {");
        ostr.println("    Token errortok = token.next;");
        if (Options.B("KEEP_LINE_COLUMN"))
           ostr.println("    int line = errortok.beginLine, column = errortok.beginColumn;");
        ostr.println("    String mess = (errortok.kind == 0) ? tokenImage[0] : errortok.image;");
        if (Options.B("KEEP_LINE_COLUMN"))
           ostr.println("    return new ParseException(\"Parse error at line \" + line + \", column \" + column + \".  Encountered: \" + mess);");
        else
           ostr.println("    return new ParseException(\"Parse error at <unknown location>.  Encountered: \" + mess);");
        ostr.println("  }");
      }
      ostr.println("");

      if (Options.B("DEBUG_PARSER")) {
        ostr.println("  " + staticOpt() + "private int trace_indent = 0;");
        ostr.println("  " + staticOpt() + "private boolean trace_enabled = true;");
        ostr.println("");
        ostr.println("  " + staticOpt() + "final public void enable_tracing() {");
        ostr.println("    trace_enabled = true;");
        ostr.println("  }");
        ostr.println("");
        ostr.println("  " + staticOpt() + "final public void disable_tracing() {");
        ostr.println("    trace_enabled = false;");
        ostr.println("  }");
        ostr.println("");
        ostr.println("  " + staticOpt() + "final private void trace_call(String s) {");
        ostr.println("    if (trace_enabled) {");
        ostr.println("      for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
        ostr.println("      System.out.println(\"Call:   \" + s);");
        ostr.println("    }");
        ostr.println("    trace_indent = trace_indent + 2;");
        ostr.println("  }");
        ostr.println("");
        ostr.println("  " + staticOpt() + "final private void trace_return(String s) {");
        ostr.println("    trace_indent = trace_indent - 2;");
        ostr.println("    if (trace_enabled) {");
        ostr.println("      for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
        ostr.println("      System.out.println(\"Return: \" + s);");
        ostr.println("    }");
        ostr.println("  }");
        ostr.println("");
        ostr.println("  " + staticOpt() + "final private void trace_token(Token t, String where) {");
        ostr.println("    if (trace_enabled) {");
        ostr.println("      for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
        ostr.println("      System.out.print(\"Consumed token: <\" + tokenImage[t.kind]);");
        ostr.println("      if (t.kind != 0 && !tokenImage[t.kind].equals(\"\\\"\" + t.image + \"\\\"\")) {");
        ostr.println("        System.out.print(\": \\\"\" + t.image + \"\\\"\");");
        ostr.println("      }");
        ostr.println("      System.out.println(\">\" + where);");
        ostr.println("    }");
        ostr.println("  }");
        ostr.println("");
        ostr.println("  " + staticOpt() + "final private void trace_scan(Token t1, int t2) {");
        ostr.println("    if (trace_enabled) {");
        ostr.println("      for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
        ostr.println("      System.out.print(\"Visited token: <\" + tokenImage[t1.kind]);");
        ostr.println("      if (t1.kind != 0 && !tokenImage[t1.kind].equals(\"\\\"\" + t1.image + \"\\\"\")) {");
        ostr.println("        System.out.print(\": \\\"\" + t1.image + \"\\\"\");");
        ostr.println("      }");
        ostr.println("      System.out.println(\">; Expected token: <\" + tokenImage[t2] + \">\");");
        ostr.println("    }");
        ostr.println("  }");
        ostr.println("");
      } else {
        ostr.println("  " + staticOpt() + "final public void enable_tracing() {");
        ostr.println("  }");
        ostr.println("");
        ostr.println("  " + staticOpt() + "final public void disable_tracing() {");
        ostr.println("  }");
        ostr.println("");
      }

      if (jj2index != 0 && Options.B("ERROR_REPORTING")) {
        ostr.println("  " + staticOpt() + "final private void jj_rescan_token() {");
        ostr.println("    jj_rescan = true;");
        ostr.println("    for (int i = 0; i < " + jj2index + "; i++) {");
        ostr.println("      JJCalls p = jj_2_rtns[i];");
        ostr.println("      do {");
        ostr.println("        if (p.gen > jj_gen) {");
        ostr.println("          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;");
        ostr.println("          switch (i) {");
        for (int i = 0; i < jj2index; i++) {
          ostr.println("            case " + i + ": jj_3_" + (i+1) + "(); break;");
        }
        ostr.println("          }");
        ostr.println("        }");
        ostr.println("        p = p.next;");
        ostr.println("      } while (p != null);");
        ostr.println("    }");
        ostr.println("    jj_rescan = false;");
        ostr.println("  }");
        ostr.println("");
        ostr.println("  " + staticOpt() + "final private void jj_save(int index, int xla) {");
        ostr.println("    JJCalls p = jj_2_rtns[index];");
        ostr.println("    while (p.gen > jj_gen) {");
        ostr.println("      if (p.next == null) { p = p.next = new JJCalls(); break; }");
        ostr.println("      p = p.next;");
        ostr.println("    }");
        ostr.println("    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;");
        ostr.println("  }");
        ostr.println("");
      }

      if (jj2index != 0 && Options.B("ERROR_REPORTING")) {
        ostr.println("  static final class JJCalls {");
        ostr.println("    int gen;");
        ostr.println("    Token first;");
        ostr.println("    int arg;");
        ostr.println("    JJCalls next;");
        ostr.println("  }");
        ostr.println("");
      }

      if (cu_from_insertion_point_2.size() != 0) {
        printTokenSetup((Token)(cu_from_insertion_point_2.elementAt(0))); ccol = 1;
        for (Enumeration enum = cu_from_insertion_point_2.elements(); enum.hasMoreElements();) {
          t = (Token)enum.nextElement();
          printToken(t, ostr);
        }
        printTrailingComments(t, ostr);
      }
      ostr.println("");

      ostr.close();

    } // matches "if (Options.B("BUILD_PARSER"))"

  }

  static private PrintWriter ostr;

   public static void reInit()
   {
      ostr = null;
   }

}
