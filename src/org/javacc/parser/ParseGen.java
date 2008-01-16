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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Generate the parser.
 */
public class ParseGen extends JavaCCGlobals implements JavaCCParserConstants {

  static public void start() throws MetaParseException {

    Token t = null;

    if (JavaCCErrors.get_error_count() != 0) throw new MetaParseException();

    if (Options.getBuildParser()) {

      try {
        ostr = new PrintWriter(
                  new BufferedWriter(
                     new FileWriter(
                       new File(Options.getOutputDirectory(), cu_name + ".java")
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
        for (Enumeration enumeration = cu_to_insertion_point_1.elements(); enumeration.hasMoreElements();) {
          t = (Token)enumeration.nextElement();
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
        for (Enumeration enumeration = cu_to_insertion_point_2.elements(); enumeration.hasMoreElements();) {
          t = (Token)enumeration.nextElement();
          printToken(t, ostr);
        }
      }

      ostr.println("");
      ostr.println("");

      ParseEngine.build(ostr);

      if (Options.getStatic()) {
        ostr.println("  static private boolean jj_initialized_once = false;");
      }
      if (Options.getUserTokenManager()) {
        ostr.println("  /** User defined Token Manager. */");
        ostr.println("  " + staticOpt() + "public TokenManager token_source;");
      } else {
        ostr.println("  /** Generated Token Manager. */");
        ostr.println("  " + staticOpt() + "public " + cu_name + "TokenManager token_source;");
        if (!Options.getUserCharStream()) {
          if (Options.getJavaUnicodeEscape()) {
            ostr.println("  " + staticOpt() + "JavaCharStream jj_input_stream;");
          } else {
            ostr.println("  " + staticOpt() + "SimpleCharStream jj_input_stream;");
          }
        }
      }
      ostr.println("  /** Current token. */");
      ostr.println("  " + staticOpt() + "public Token token;");
      ostr.println("  /** Next token. */");
      ostr.println("  " + staticOpt() + "public Token jj_nt;");
      if (!Options.getCacheTokens()) {
        ostr.println("  " + staticOpt() + "private int jj_ntk;");
      }
      if (jj2index != 0) {
        ostr.println("  " + staticOpt() + "private Token jj_scanpos, jj_lastpos;");
        ostr.println("  " + staticOpt() + "private int jj_la;");
        ostr.println("  /** Whether we are looking ahead. */");
        ostr.println("  " + staticOpt() + "private boolean jj_lookingAhead = false;");
        ostr.println("  " + staticOpt() + "private boolean jj_semLA;");
      }
      if (Options.getErrorReporting()) {
        ostr.println("  " + staticOpt() + "private int jj_gen;");
        ostr.println("  " + staticOpt() + "final private int[] jj_la1 = new int[" + maskindex + "];");
        int tokenMaskSize = (tokenCount-1)/32 + 1;
        for (int i = 0; i < tokenMaskSize; i++)
          ostr.println("  static private int[] jj_la1_" + i + ";");
        ostr.println("  static {");
        for (int i = 0; i < tokenMaskSize; i++)
          ostr.println("      jj_la1_init_" + i + "();");
        ostr.println("   }");
        for (int i = 0; i < tokenMaskSize; i++) {
          ostr.println("   private static void jj_la1_init_" + i + "() {");
          ostr.print("      jj_la1_" + i + " = new int[] {");
          for (Enumeration enumeration = maskVals.elements(); enumeration.hasMoreElements();) {
            int[] tokenMask = (int[])(enumeration.nextElement());
            ostr.print("0x" + Integer.toHexString(tokenMask[i]) + ",");
          }
          ostr.println("};");
          ostr.println("   }");
        }
      }
      if (jj2index != 0 && Options.getErrorReporting()) {
        ostr.println("  " + staticOpt() + "final private JJCalls[] jj_2_rtns = new JJCalls[" + jj2index + "];");
        ostr.println("  " + staticOpt() + "private boolean jj_rescan = false;");
        ostr.println("  " + staticOpt() + "private int jj_gc = 0;");
      }
      ostr.println("");

      if (!Options.getUserTokenManager()) {
        if (Options.getUserCharStream()) {
          ostr.println("  /** Constructor with user supplied CharStream. */");
          ostr.println("  public " + cu_name + "(CharStream stream) {");
          if (Options.getStatic()) {
            ostr.println("    if (jj_initialized_once) {");
            ostr.println("      System.out.println(\"ERROR: Second call to constructor of static parser.  \");");
            ostr.println("      System.out.println(\"       You must either use ReInit() " +
                    "or set the JavaCC option STATIC to false\");");
            ostr.println("      System.out.println(\"       during parser generation.\");");
            ostr.println("      throw new Error();");
            ostr.println("    }");
            ostr.println("    jj_initialized_once = true;");
          }
          if(Options.getTokenManagerUsesParser() && !Options.getStatic()){
            ostr.println("    token_source = new " + cu_name + "TokenManager(this, stream);");
          } else {
            ostr.println("    token_source = new " + cu_name + "TokenManager(stream);");
          }
          ostr.println("    token = new Token();");
          if (Options.getCacheTokens()) {
            ostr.println("    token.next = jj_nt = token_source.getNextToken();");
          } else {
            ostr.println("    jj_ntk = -1;");
          }
          if (Options.getErrorReporting()) {
            ostr.println("    jj_gen = 0;");
            ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
            if (jj2index != 0) {
              ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          ostr.println("  }");
          ostr.println("");
          ostr.println("  /** Reinitialise. */");
          ostr.println("  " + staticOpt() + "public void ReInit(CharStream stream) {");
          ostr.println("    token_source.ReInit(stream);");
          ostr.println("    token = new Token();");
          if (Options.getCacheTokens()) {
            ostr.println("    token.next = jj_nt = token_source.getNextToken();");
          } else {
            ostr.println("    jj_ntk = -1;");
          }
          ostr.println("    jj_lookingAhead = false;");
          if (jjtreeGenerated) {
            ostr.println("    jjtree.reset();");
          }
          if (Options.getErrorReporting()) {
            ostr.println("    jj_gen = 0;");
            ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
            if (jj2index != 0) {
              ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          ostr.println("  }");
        } else {
          ostr.println("  /** Constructor with InputStream. */");
          ostr.println("  public " + cu_name + "(java.io.InputStream stream) {");
          ostr.println("     this(stream, null);");
          ostr.println("  }");
          ostr.println("  /** Constructor with InputStream and supplied encoding */");
          ostr.println("  public " + cu_name + "(java.io.InputStream stream, String encoding) {");
          if (Options.getStatic()) {
            ostr.println("    if (jj_initialized_once) {");
            ostr.println("      System.out.println(\"ERROR: Second call to constructor of static parser.  \");");
            ostr.println("      System.out.println(\"       You must either use ReInit() or " +
                    "set the JavaCC option STATIC to false\");");
            ostr.println("      System.out.println(\"       during parser generation.\");");
            ostr.println("      throw new Error();");
            ostr.println("    }");
            ostr.println("    jj_initialized_once = true;");
          }
          if (Options.getJavaUnicodeEscape()) {
              if (Options.getJdkVersion().equals("1.3")) {
                  ostr.println("    try { jj_input_stream = new JavaCharStream(stream, encoding, 1, 1); } " +
                          "catch(java.io.UnsupportedEncodingException e) {" +
                          " throw new RuntimeException(e.getMessage()); }");
              } else {
                ostr.println("    try { jj_input_stream = new JavaCharStream(stream, encoding, 1, 1); } " +
                        "catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }");
              }
          } else {
              if (Options.getJdkVersion().equals("1.3")) {
                  ostr.println("    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } " +
                          "catch(java.io.UnsupportedEncodingException e) { " +
                          "throw new RuntimeException(e.getMessage()); }");
          } else {
            ostr.println("    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } " +
                    "catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }");
          }
          }
          if(Options.getTokenManagerUsesParser() && !Options.getStatic()){
            ostr.println("    token_source = new " + cu_name + "TokenManager(this, jj_input_stream);");
          } else {
            ostr.println("    token_source = new " + cu_name + "TokenManager(jj_input_stream);");
          }
          ostr.println("    token = new Token();");
          if (Options.getCacheTokens()) {
            ostr.println("    token.next = jj_nt = token_source.getNextToken();");
          } else {
            ostr.println("    jj_ntk = -1;");
          }
          if (Options.getErrorReporting()) {
            ostr.println("    jj_gen = 0;");
            ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
            if (jj2index != 0) {
              ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          ostr.println("  }");
          ostr.println("");
          ostr.println("  /** Reinitialise. */");
          ostr.println("  " + staticOpt() + "public void ReInit(java.io.InputStream stream) {");
          ostr.println("     ReInit(stream, null);");
          ostr.println("  }");
          ostr.println("  /** Reinitialise. */");
          ostr.println("  " + staticOpt() + "public void ReInit(java.io.InputStream stream, String encoding) {");
          if (Options.getJdkVersion().equals("1.3")) {
            ostr.println("    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } " +
                    "catch(java.io.UnsupportedEncodingException e) { " +
                    "throw new RuntimeException(e.getMessage()); }");
          } else {
            ostr.println("    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } " +
                    "catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }");
          }
          ostr.println("    token_source.ReInit(jj_input_stream);");
          ostr.println("    token = new Token();");
          if (Options.getCacheTokens()) {
            ostr.println("    token.next = jj_nt = token_source.getNextToken();");
          } else {
            ostr.println("    jj_ntk = -1;");
          }
          if (jjtreeGenerated) {
            ostr.println("    jjtree.reset();");
          }
          if (Options.getErrorReporting()) {
            ostr.println("    jj_gen = 0;");
            ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
            if (jj2index != 0) {
              ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          ostr.println("  }");
          ostr.println("");
          ostr.println("  /** Constructor. */");
          ostr.println("  public " + cu_name + "(java.io.Reader stream) {");
          if (Options.getStatic()) {
            ostr.println("    if (jj_initialized_once) {");
            ostr.println("      System.out.println(\"ERROR: Second call to constructor of static parser. \");");
            ostr.println("      System.out.println(\"       You must either use ReInit() or " +
                    "set the JavaCC option STATIC to false\");");
            ostr.println("      System.out.println(\"       during parser generation.\");");
            ostr.println("      throw new Error();");
            ostr.println("    }");
            ostr.println("    jj_initialized_once = true;");
          }
          if (Options.getJavaUnicodeEscape()) {
            ostr.println("    jj_input_stream = new JavaCharStream(stream, 1, 1);");
          } else {
            ostr.println("    jj_input_stream = new SimpleCharStream(stream, 1, 1);");
          }
          if(Options.getTokenManagerUsesParser() && !Options.getStatic()){
            ostr.println("    token_source = new " + cu_name + "TokenManager(this, jj_input_stream);");
          } else {
            ostr.println("    token_source = new " + cu_name + "TokenManager(jj_input_stream);");
          }
          ostr.println("    token = new Token();");
          if (Options.getCacheTokens()) {
            ostr.println("    token.next = jj_nt = token_source.getNextToken();");
          } else {
            ostr.println("    jj_ntk = -1;");
          }
          if (Options.getErrorReporting()) {
            ostr.println("    jj_gen = 0;");
            ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
            if (jj2index != 0) {
              ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
            }
          }
          ostr.println("  }");
          ostr.println("");
          ostr.println("  /** Reinitialise. */");
          ostr.println("  " + staticOpt() + "public void ReInit(java.io.Reader stream) {");
          if (Options.getJavaUnicodeEscape()) {
            ostr.println("    jj_input_stream.ReInit(stream, 1, 1);");
          } else {
            ostr.println("    jj_input_stream.ReInit(stream, 1, 1);");
          }
          ostr.println("    token_source.ReInit(jj_input_stream);");
          ostr.println("    token = new Token();");
          if (Options.getCacheTokens()) {
            ostr.println("    token.next = jj_nt = token_source.getNextToken();");
          } else {
            ostr.println("    jj_ntk = -1;");
          }
          if (jjtreeGenerated) {
            ostr.println("    jjtree.reset();");
          }
          if (Options.getErrorReporting()) {
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
      if (Options.getUserTokenManager()) {
        ostr.println("  /** Constructor with user supplied Token Manager. */");
        ostr.println("  public " + cu_name + "(TokenManager tm) {");
      } else {
        ostr.println("  /** Constructor with generated Token Manager. */");
        ostr.println("  public " + cu_name + "(" + cu_name + "TokenManager tm) {");
      }
      if (Options.getStatic()) {
        ostr.println("    if (jj_initialized_once) {");
        ostr.println("      System.out.println(\"ERROR: Second call to constructor of static parser. \");");
        ostr.println("      System.out.println(\"       You must either use ReInit() or " +
                "set the JavaCC option STATIC to false\");");
        ostr.println("      System.out.println(\"       during parser generation.\");");
        ostr.println("      throw new Error();");
        ostr.println("    }");
        ostr.println("    jj_initialized_once = true;");
      }
      ostr.println("    token_source = tm;");
      ostr.println("    token = new Token();");
      if (Options.getCacheTokens()) {
        ostr.println("    token.next = jj_nt = token_source.getNextToken();");
      } else {
        ostr.println("    jj_ntk = -1;");
      }
      if (Options.getErrorReporting()) {
        ostr.println("    jj_gen = 0;");
        ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
        if (jj2index != 0) {
          ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
        }
      }
      ostr.println("  }");
      ostr.println("");
      if (Options.getUserTokenManager()) {
        ostr.println("  /** Reinitialise. */");
        ostr.println("  public void ReInit(TokenManager tm) {");
      } else {
        ostr.println("  /** Reinitialise. */");
        ostr.println("  public void ReInit(" + cu_name + "TokenManager tm) {");
      }
      ostr.println("    token_source = tm;");
      ostr.println("    token = new Token();");
      if (Options.getCacheTokens()) {
        ostr.println("    token.next = jj_nt = token_source.getNextToken();");
      } else {
        ostr.println("    jj_ntk = -1;");
      }
      if (jjtreeGenerated) {
        ostr.println("    jjtree.reset();");
      }
      if (Options.getErrorReporting()) {
        ostr.println("    jj_gen = 0;");
        ostr.println("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
        if (jj2index != 0) {
          ostr.println("    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
        }
      }
      ostr.println("  }");
      ostr.println("");
      ostr.println("  " + staticOpt() + "private Token jj_consume_token(int kind) throws ParseException {");
      if (Options.getCacheTokens()) {
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
      if (Options.getErrorReporting()) {
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
      if (Options.getDebugParser()) {
        ostr.println("      trace_token(token, \"\");");
      }
      ostr.println("      return token;");
      ostr.println("    }");
      if (Options.getCacheTokens()) {
        ostr.println("    jj_nt = token;");
      }
      ostr.println("    token = oldToken;");
      if (Options.getErrorReporting()) {
        ostr.println("    jj_kind = kind;");
      }
      ostr.println("    throw generateParseException();");
      ostr.println("  }");
      ostr.println("");
      if (jj2index != 0) {
        ostr.println("  static private final class LookaheadSuccess extends java.lang.Error { }");
        ostr.println("  " + staticOpt() + "final private LookaheadSuccess jj_ls = new LookaheadSuccess();");
        ostr.println("  " + staticOpt() + "private boolean jj_scan_token(int kind) {");
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
        if (Options.getErrorReporting()) {
          ostr.println("    if (jj_rescan) {");
          ostr.println("      int i = 0; Token tok = token;");
          ostr.println("      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }");
          ostr.println("      if (tok != null) jj_add_error_token(kind, i);");
          if (Options.getDebugLookahead()) {
            ostr.println("    } else {");
            ostr.println("      trace_scan(jj_scanpos, kind);");
          }
          ostr.println("    }");
        } else if (Options.getDebugLookahead()) {
          ostr.println("    trace_scan(jj_scanpos, kind);");
        }
        ostr.println("    if (jj_scanpos.kind != kind) return true;");
        ostr.println("    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;");
        ostr.println("    return false;");
        ostr.println("  }");
        ostr.println("");
      }
      ostr.println("");
      ostr.println("/** Get the next Token. */");
      ostr.println("  " + staticOpt() + "final public Token getNextToken() {");
      if (Options.getCacheTokens()) {
        ostr.println("    if ((token = jj_nt).next != null) jj_nt = jj_nt.next;");
        ostr.println("    else jj_nt = jj_nt.next = token_source.getNextToken();");
      } else {
        ostr.println("    if (token.next != null) token = token.next;");
        ostr.println("    else token = token.next = token_source.getNextToken();");
        ostr.println("    jj_ntk = -1;");
      }
      if (Options.getErrorReporting()) {
        ostr.println("    jj_gen++;");
      }
      if (Options.getDebugParser()) {
        ostr.println("      trace_token(token, \" (in getNextToken)\");");
      }
      ostr.println("    return token;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Get the specific Token. */");
      ostr.println("  " + staticOpt() + "final public Token getToken(int index) {");
      if (jj2index != 0) {
        ostr.println("    Token t = jj_lookingAhead ? jj_scanpos : token;");
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
      if (!Options.getCacheTokens()) {
        ostr.println("  " + staticOpt() + "private int jj_ntk() {");
        ostr.println("    if ((jj_nt=token.next) == null)");
        ostr.println("      return (jj_ntk = (token.next=token_source.getNextToken()).kind);");
        ostr.println("    else");
        ostr.println("      return (jj_ntk = jj_nt.kind);");
        ostr.println("  }");
        ostr.println("");
      }
      if (Options.getErrorReporting()) {
        if (!Options.getJdkVersion().equals("1.5"))
          ostr.println("  " + staticOpt() + "private java.util.List jj_expentries = new java.util.ArrayList();");
        else
          ostr.println("  " + staticOpt() +
                  "private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();");
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
          ostr.println("      for (java.util.Iterator it = jj_expentries.iterator(); it.hasNext();) {");
          ostr.println("        int[] oldentry = (int[])(it.next());");
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
          ostr.println("      if (!exists) jj_expentries.add(jj_expentry);");
          ostr.println("      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;");
          ostr.println("    }");
          ostr.println("  }");
        }
        ostr.println("");
        ostr.println("  /** Generate ParseException. */");
        ostr.println("  " + staticOpt() + "public ParseException generateParseException() {");
        ostr.println("    jj_expentries.clear();");
        ostr.println("    boolean[] la1tokens = new boolean[" + tokenCount + "];");
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
        ostr.println("        jj_expentries.add(jj_expentry);");
        ostr.println("      }");
        ostr.println("    }");
        if (jj2index != 0) {
          ostr.println("    jj_endpos = 0;");
          ostr.println("    jj_rescan_token();");
          ostr.println("    jj_add_error_token(0, 0);");
        }
        ostr.println("    int[][] exptokseq = new int[jj_expentries.size()][];");
        ostr.println("    for (int i = 0; i < jj_expentries.size(); i++) {");
        if (!Options.getJdkVersion().equals("1.5"))
           ostr.println("      exptokseq[i] = (int[])jj_expentries.get(i);");
        else
           ostr.println("      exptokseq[i] = jj_expentries.get(i);");
        ostr.println("    }");
        ostr.println("    return new ParseException(token, exptokseq, tokenImage);");
        ostr.println("  }");
      } else {
        ostr.println("  /** Generate ParseException. */");
        ostr.println("  " + staticOpt() + "public ParseException generateParseException() {");
        ostr.println("    Token errortok = token.next;");
        if (Options.getKeepLineColumn())
           ostr.println("    int line = errortok.beginLine, column = errortok.beginColumn;");
        ostr.println("    String mess = (errortok.kind == 0) ? tokenImage[0] : errortok.image;");
        if (Options.getKeepLineColumn())
           ostr.println("    return new ParseException(" +
               "\"Parse error at line \" + line + \", column \" + column + \".  " +
               "Encountered: \" + mess);");
        else
           ostr.println("    return new ParseException(\"Parse error at <unknown location>.  " +
                   "Encountered: \" + mess);");
        ostr.println("  }");
      }
      ostr.println("");

      if (Options.getDebugParser()) {
        ostr.println("  " + staticOpt() + "private int trace_indent = 0;");
        ostr.println("  " + staticOpt() + "private boolean trace_enabled = true;");
        ostr.println("");
        ostr.println("/** Enable tracing. */");
        ostr.println("  " + staticOpt() + "final public void enable_tracing() {");
        ostr.println("    trace_enabled = true;");
        ostr.println("  }");
        ostr.println("");
        ostr.println("/** Disable tracing. */");
        ostr.println("  " + staticOpt() + "final public void disable_tracing() {");
        ostr.println("    trace_enabled = false;");
        ostr.println("  }");
        ostr.println("");
        ostr.println("  " + staticOpt() + "private void trace_call(String s) {");
        ostr.println("    if (trace_enabled) {");
        ostr.println("      for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
        ostr.println("      System.out.println(\"Call:   \" + s);");
        ostr.println("    }");
        ostr.println("    trace_indent = trace_indent + 2;");
        ostr.println("  }");
        ostr.println("");
        ostr.println("  " + staticOpt() + "private void trace_return(String s) {");
        ostr.println("    trace_indent = trace_indent - 2;");
        ostr.println("    if (trace_enabled) {");
        ostr.println("      for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
        ostr.println("      System.out.println(\"Return: \" + s);");
        ostr.println("    }");
        ostr.println("  }");
        ostr.println("");
        ostr.println("  " + staticOpt() + "private void trace_token(Token t, String where) {");
        ostr.println("    if (trace_enabled) {");
        ostr.println("      for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
        ostr.println("      System.out.print(\"Consumed token: <\" + tokenImage[t.kind]);");
        ostr.println("      if (t.kind != 0 && !tokenImage[t.kind].equals(\"\\\"\" + t.image + \"\\\"\")) {");
        ostr.println("        System.out.print(\": \\\"\" + t.image + \"\\\"\");");
        ostr.println("      }");
        ostr.println("      System.out.println(\" at line \" + t.beginLine + " +
                "\" column \" + t.beginColumn + \">\" + where);");
        ostr.println("    }");
        ostr.println("  }");
        ostr.println("");
        ostr.println("  " + staticOpt() + "private void trace_scan(Token t1, int t2) {");
        ostr.println("    if (trace_enabled) {");
        ostr.println("      for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
        ostr.println("      System.out.print(\"Visited token: <\" + tokenImage[t1.kind]);");
        ostr.println("      if (t1.kind != 0 && !tokenImage[t1.kind].equals(\"\\\"\" + t1.image + \"\\\"\")) {");
        ostr.println("        System.out.print(\": \\\"\" + t1.image + \"\\\"\");");
        ostr.println("      }");
        ostr.println("      System.out.println(\" at line \" + t1.beginLine + \"" +
                " column \" + t1.beginColumn + \">; Expected token: <\" + tokenImage[t2] + \">\");");
        ostr.println("    }");
        ostr.println("  }");
        ostr.println("");
      } else {
        ostr.println("  /** Enable tracing. */");
        ostr.println("  " + staticOpt() + "final public void enable_tracing() {");
        ostr.println("  }");
        ostr.println("");
        ostr.println("  /** Disable tracing. */");
        ostr.println("  " + staticOpt() + "final public void disable_tracing() {");
        ostr.println("  }");
        ostr.println("");
      }

      if (jj2index != 0 && Options.getErrorReporting()) {
        ostr.println("  " + staticOpt() + "private void jj_rescan_token() {");
        ostr.println("    jj_rescan = true;");
        ostr.println("    for (int i = 0; i < " + jj2index + "; i++) {");
        ostr.println("    try {");
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
        ostr.println("      } catch(LookaheadSuccess ls) { }");
        ostr.println("    }");
        ostr.println("    jj_rescan = false;");
        ostr.println("  }");
        ostr.println("");
        ostr.println("  " + staticOpt() + "private void jj_save(int index, int xla) {");
        ostr.println("    JJCalls p = jj_2_rtns[index];");
        ostr.println("    while (p.gen > jj_gen) {");
        ostr.println("      if (p.next == null) { p = p.next = new JJCalls(); break; }");
        ostr.println("      p = p.next;");
        ostr.println("    }");
        ostr.println("    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;");
        ostr.println("  }");
        ostr.println("");
      }

      if (jj2index != 0 && Options.getErrorReporting()) {
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
        for (Enumeration enumeration = cu_from_insertion_point_2.elements(); enumeration.hasMoreElements();) {
          t = (Token)enumeration.nextElement();
          printToken(t, ostr);
        }
        printTrailingComments(t, ostr);
      }
      ostr.println("");

      ostr.close();

    } // matches "if (Options.getBuildParser())"

  }

  static private PrintWriter ostr;

   public static void reInit()
   {
      ostr = null;
   }

}
