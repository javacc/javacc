package org.javacc.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.javacc.parser.JavaCCGlobals.*;

/**
 * Generate the parser.
 */
public class ParseGenCPP extends ParseGen {

  public void start() throws MetaParseException {

    Token t = null;

    if (JavaCCErrors.get_error_count() != 0) throw new MetaParseException();

    List tn = new ArrayList(toolNames);
    tn.add(toolName);
    switchToStaticsFile();

    boolean implementsExists = false;

    switchToIncludeFile();

    //standard includes
    genCodeLine("#include \"JavaCC.h\"");
    genCodeLine("#include \"CharStream.h\"");
    genCodeLine("#include \"Token.h\"");
    genCodeLine("#include \"TokenMgrError.h\"");
    genCodeLine("#include \"ParseException.h\"");
    genCodeLine("#include \"TokenManager.h\"");
    genCodeLine("#include \"" + cu_name + "Constants.h\"");

    if (jjtreeGenerated) {
      genCodeLine("#include \"JJTree.h\"");
    }

    switchToIncludeFile();
    genCodeLine("  typedef struct _JJCalls {");
    genCodeLine("    int gen;");
    genCodeLine("    Token first;");
    genCodeLine("    int arg;");
    genCodeLine("    _JJCalls *next;");
    genCodeLine("    ~_JJCalls() { if (next) delete next; }");
    genCodeLine("  } *JJCalls;");
    genCodeLine("");

    genClassStart("", cu_name, new String[0], new String[0]);
    switchToMainFile();
    if (cu_to_insertion_point_2.size() != 0) {
      printTokenSetup((Token)(cu_to_insertion_point_2.get(0)));
      for (Iterator it = cu_to_insertion_point_2.iterator(); it.hasNext();) {
        t = (Token)it.next();
        printToken(t);
      }
    }

    switchToMainFile();
    genCodeLine("typedef class _LookaheadSuccess { } *LookaheadSuccess; // Dummy class");
    genCodeLine("  static LookaheadSuccess jj_ls = new _LookaheadSuccess();");

    genCodeLine("");
    genCodeLine("");

    new ParseEngine().build(this);

    switchToIncludeFile();
    genCodeLine("  public: TokenManager token_source;");
    genCodeLine("  public: CharStream jj_input_stream;");
    genCodeLine("  /** Current token. */");
    genCodeLine("  public: Token token;");
    genCodeLine("  /** Next token. */");
    genCodeLine("  public: Token jj_nt;");
    genCodeLine("  private: int jj_ntk;");

    genCodeLine("  private: JJCalls jj_2_rtns[" + jj2index + "];");
    genCodeLine("  private: boolean jj_rescan;");
    genCodeLine("  private: int jj_gc;");
    genCodeLine("  private: Token jj_scanpos, jj_lastpos;");
    genCodeLine("  private: int jj_la;");
    genCodeLine("  /** Whether we are looking ahead. */");
    genCodeLine("  private: boolean jj_lookingAhead;");
    genCodeLine("  private: boolean jj_semLA;");

    genCodeLine("  private: int jj_gen;");
    genCodeLine("  private: int jj_la1[" + maskindex + "];");
    int tokenMaskSize = (tokenCount-1)/32 + 1;

    if (Options.getErrorReporting()) {
      switchToStaticsFile();
      for (int i = 0; i < tokenMaskSize; i++) {
        genCodeLine("  int jj_la1_" + i + "[] = {");
        for (Iterator it = maskVals.iterator(); it.hasNext();) {
          int[] tokenMask = (int[])(it.next());
          genCode("0x" + Integer.toHexString(tokenMask[i]) + ",");
        }
        genCodeLine("};");
      }
    }

    genCodeLine("");

    genCodeLine("  /** Constructor with user supplied TokenManager. */");

    switchToIncludeFile(); // TEMP
    genCodeLine(" Token head; ");
    genCodeLine(" public: ");
    generateMethodDefHeader("", cu_name, cu_name + "(TokenManager tm)");
    genCodeLine("{");
    genCodeLine("    head = null;");
    genCodeLine("    ReInit(tm);");
    genCodeLine("}");

    switchToIncludeFile();
    genCodeLine("   public: virtual ~" + cu_name + "();");
    switchToMainFile();
    genCodeLine("   " + cu_name + "::~" +cu_name + "()");
    genCodeLine("{");
    genCodeLine("  if (token_source) delete token_source;");
    genCodeLine("  if (head) delete head;");
    genCodeLine("  for (int i = 0; i < " + jj2index + "; i++)");
    genCodeLine("    delete jj_2_rtns[i];");
    genCodeLine("}");
    generateMethodDefHeader("void", cu_name, "ReInit(TokenManager tm)");
    genCodeLine("{");
    genCodeLine("    if (head) delete head;");
    genCodeLine("    token_source = tm;");
    genCodeLine("    head = token = new _Token();");
    genCodeLine("    token->kind = 0;");
    genCodeLine("    token->next = null;");
    genCodeLine("    jj_lookingAhead = false;");
    genCodeLine("    jj_rescan = false;");
    genCodeLine("    jj_scanpos = jj_lastpos = null;");
    genCodeLine("    jj_gc = 0;");
    genCodeLine("    jj_kind = -1;");
    genCodeLine("    trace_indent = 0;");
    genCodeLine("    trace_enabled = " + Options.getDebugParser() + ";");

    if (Options.getCacheTokens()) {
      genCodeLine("    token->next = jj_nt = token_source->getNextToken();");
    } else {
      genCodeLine("    jj_ntk = -1;");
    }
    if (jjtreeGenerated) {
      genCodeLine("    jjtree.reset();");
    }
    if (Options.getErrorReporting()) {
      genCodeLine("    jj_gen = 0;");
      genCodeLine("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
      if (jj2index != 0) {
        genCodeLine("    for (int i = 0; i < " + jj2index + "; i++) jj_2_rtns[i] = new _JJCalls();");
      }
    }
    genCodeLine("  }");

    genCodeLine("");
    generateMethodDefHeader("Token ", cu_name, "jj_consume_token(int kind)", "ParseException");
    genCodeLine("  {");
    if (Options.getCacheTokens()) {
      genCodeLine("    Token oldToken = token;");
      genCodeLine("    if ((token = jj_nt)->next != null) jj_nt = jj_nt->next;");
      genCodeLine("    else jj_nt = jj_nt->next = token_source->getNextToken();");
    } else {
      genCodeLine("    Token oldToken;");
      genCodeLine("    if ((oldToken = token)->next != null) token = token->next;");
      genCodeLine("    else token = token->next = token_source->getNextToken();");
      genCodeLine("    jj_ntk = -1;");
    }
    genCodeLine("    if (token->kind == kind) {");
    if (Options.getErrorReporting()) {
      genCodeLine("      jj_gen++;");
      if (jj2index != 0) {
        genCodeLine("      if (++jj_gc > 100) {");
        genCodeLine("        jj_gc = 0;");
        genCodeLine("        for (int i = 0; i < " + jj2index + "; i++) {");
        genCodeLine("          JJCalls c = jj_2_rtns[i];");
        genCodeLine("          while (c != null) {");
        genCodeLine("            if (c->gen < jj_gen) c->first = null;");
        genCodeLine("            c = c->next;");
        genCodeLine("          }");
        genCodeLine("        }");
        genCodeLine("      }");
      }
    }
    if (Options.getDebugParser()) {
      genCodeLine("      trace_token(token, \"\");");
    }
    genCodeLine("      return token;");
    genCodeLine("    }");
    if (Options.getCacheTokens()) {
      genCodeLine("    jj_nt = token;");
    }
    genCodeLine("    token = oldToken;");
    if (Options.getErrorReporting()) {
      genCodeLine("    jj_kind = kind;");
    }
    genCodeLine("    throw generateParseException();");
    genCodeLine("  }");
    genCodeLine("");

    if (jj2index != 0) {
      switchToMainFile();
      generateMethodDefHeader("boolean ", cu_name, "jj_scan_token(int kind)");
      genCodeLine("{");
      genCodeLine("    if (jj_scanpos == jj_lastpos) {");
      genCodeLine("      jj_la--;");
      genCodeLine("      if (jj_scanpos->next == null) {");
      genCodeLine("        jj_lastpos = jj_scanpos = jj_scanpos->next = token_source->getNextToken();");
      genCodeLine("      } else {");
      genCodeLine("        jj_lastpos = jj_scanpos = jj_scanpos->next;");
      genCodeLine("      }");
      genCodeLine("    } else {");
      genCodeLine("      jj_scanpos = jj_scanpos->next;");
      genCodeLine("    }");
      if (Options.getErrorReporting()) {
        genCodeLine("    if (jj_rescan) {");
        genCodeLine("      int i = 0; Token tok = token;");
        genCodeLine("      while (tok != null && tok != jj_scanpos) { i++; tok = tok->next; }");
        genCodeLine("      if (tok != null) jj_add_error_token(kind, i);");
        if (Options.getDebugLookahead()) {
          genCodeLine("    } else {");
          genCodeLine("      trace_scan(jj_scanpos, kind);");
        }
        genCodeLine("    }");
      } else if (Options.getDebugLookahead()) {
        genCodeLine("    trace_scan(jj_scanpos, kind);");
      }
      genCodeLine("    if (jj_scanpos->kind != kind) return true;");
      genCodeLine("    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;");
      genCodeLine("    return false;");
      genCodeLine("  }");
      genCodeLine("");
    }
    genCodeLine("");
    genCodeLine("/** Get the next Token. */");
    generateMethodDefHeader(" Token ", cu_name, "getNextToken()");
    genCodeLine("{");
    if (Options.getCacheTokens()) {
      genCodeLine("    if ((token = jj_nt)->next != null) jj_nt = jj_nt->next;");
      genCodeLine("    else jj_nt = jj_nt->next = token_source->getNextToken();");
    } else {
      genCodeLine("    if (token->next != null) token = token->next;");
      genCodeLine("    else token = token->next = token_source->getNextToken();");
      genCodeLine("    jj_ntk = -1;");
    }
    if (Options.getErrorReporting()) {
      genCodeLine("    jj_gen++;");
    }
    if (Options.getDebugParser()) {
      genCodeLine("      trace_token(token, \" (in getNextToken)\");");
    }
    genCodeLine("    return token;");
    genCodeLine("  }");
    genCodeLine("");
    genCodeLine("/** Get the specific Token. */");
    generateMethodDefHeader("Token ", cu_name, "getToken(int index)");
    genCodeLine("{");
    if (lookaheadNeeded) {
      genCodeLine("    Token t = jj_lookingAhead ? jj_scanpos : token;");
    } else {
      genCodeLine("    Token t = token;");
    }
    genCodeLine("    for (int i = 0; i < index; i++) {");
    genCodeLine("      if (t->next != null) t = t->next;");
    genCodeLine("      else t = t->next = token_source->getNextToken();");
    genCodeLine("    }");
    genCodeLine("    return t;");
    genCodeLine("  }");
    genCodeLine("");
    if (!Options.getCacheTokens()) {
      generateMethodDefHeader("int", cu_name, "jj_ntk_f()");
      genCodeLine("{");
 
      genCodeLine("    if ((jj_nt=token->next) == null)");
      genCodeLine("      return (jj_ntk = (token->next=token_source->getNextToken())->kind);");
      genCodeLine("    else");
      genCodeLine("      return (jj_ntk = jj_nt->kind);");
      genCodeLine("  }");
      genCodeLine("");
    }

    switchToIncludeFile();
    genCodeLine(" private: int jj_kind;");
    if (Options.getErrorReporting()) {
      genCodeLine("  int **jj_expentries;");
      genCodeLine("  int *jj_expentry;");
      if (jj2index != 0) {
        switchToStaticsFile();
        genCodeLine("  int *jj_lasttokens = new int[100];");
        genCodeLine("  int jj_endpos;");
        genCodeLine("");

        generateMethodDefHeader("void",  cu_name, "jj_add_error_token(int kind, int pos)");
        genCodeLine("  {");
        //genCodeLine("    if (pos >= 100) return;");
        //genCodeLine("    if (pos == jj_endpos + 1) {");
        //genCodeLine("      jj_lasttokens[jj_endpos++] = kind;");
        //genCodeLine("    } else if (jj_endpos != 0) {");
        //genCodeLine("      jj_expentry = new int[jj_endpos];");
        //genCodeLine("      for (int i = 0; i < jj_endpos; i++) {");
        //genCodeLine("        jj_expentry[i] = jj_lasttokens[i];");
        //genCodeLine("      }");
        //genCodeLine("      jj_entries_loop: for (java.util.Iterator it = jj_expentries.iterator(); it.hasNext();) {");
        //genCodeLine("        int[] oldentry = (int[])(it->next());");
        //genCodeLine("        if (oldentry.length == jj_expentry.length) {");
        //genCodeLine("          for (int i = 0; i < jj_expentry.length; i++) {");
        //genCodeLine("            if (oldentry[i] != jj_expentry[i]) {");
        //genCodeLine("              continue jj_entries_loop;");
        //genCodeLine("            }");
        //genCodeLine("          }");
        //genCodeLine("          jj_expentries.add(jj_expentry);");
        //genCodeLine("          break jj_entries_loop;");
        //genCodeLine("        }");
        //genCodeLine("      }");
        //genCodeLine("      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;");
        //genCodeLine("    }");
        genCodeLine("  }");
      }
      genCodeLine("");

      genCodeLine("  /** Generate ParseException. */");
      generateMethodDefHeader("ParseException",  cu_name, "generateParseException()");
      genCodeLine("   {");
      //genCodeLine("    jj_expentries.clear();");
      //genCodeLine("    boolean[] la1tokens = new boolean[" + tokenCount + "];");
      //genCodeLine("    if (jj_kind >= 0) {");
      //genCodeLine("      la1tokens[jj_kind] = true;");
      //genCodeLine("      jj_kind = -1;");
      //genCodeLine("    }");
      //genCodeLine("    for (int i = 0; i < " + maskindex + "; i++) {");
      //genCodeLine("      if (jj_la1[i] == jj_gen) {");
      //genCodeLine("        for (int j = 0; j < 32; j++) {");
      //for (int i = 0; i < (tokenCount-1)/32 + 1; i++) {
        //genCodeLine("          if ((jj_la1_" + i + "[i] & (1<<j)) != 0) {");
        //genCode("            la1tokens[");
        //if (i != 0) {
          //genCode((32*i) + "+");
        //}
        //genCodeLine("j] = true;");
        //genCodeLine("          }");
      //}
      //genCodeLine("        }");
      //genCodeLine("      }");
      //genCodeLine("    }");
      //genCodeLine("    for (int i = 0; i < " + tokenCount + "; i++) {");
      //genCodeLine("      if (la1tokens[i]) {");
      //genCodeLine("        jj_expentry = new int[1];");
      //genCodeLine("        jj_expentry[0] = i;");
      //genCodeLine("        jj_expentries.add(jj_expentry);");
      //genCodeLine("      }");
      //genCodeLine("    }");
      //if (jj2index != 0) {
        //genCodeLine("    jj_endpos = 0;");
        //genCodeLine("    jj_rescan_token();");
        //genCodeLine("    jj_add_error_token(0, 0);");
      //}
      //genCodeLine("    int exptokseq[][1] = new int[1];");
      //genCodeLine("    for (int i = 0; i < jj_expentries.size(); i++) {");
      //if (!Options.getGenerateGenerics())
         //genCodeLine("      exptokseq[i] = (int[])jj_expentries.get(i);");
      //else
         //genCodeLine("      exptokseq[i] = jj_expentries.get(i);");
      //genCodeLine("    }");
      genCodeLine("    return new _ParseException();");//token, null, tokenImage);");
      genCodeLine("  }");
    } else {

      genCodeLine("  /** Generate ParseException. */");
      generateMethodDefHeader("ParseException",  cu_name, "generateParseException()");
      genCodeLine("   {");
      genCodeLine("    Token errortok = token->next;");
      if (Options.getKeepLineColumn())
         genCodeLine("    int line = errortok.beginLine, column = errortok.beginColumn;");
      genCodeLine("    String mess = (errortok->kind == 0) ? tokenImage[0] : errortok->image;");
      if (Options.getKeepLineColumn())
         genCodeLine("    return new _ParseException();");// +
             //"\"Parse error at line \" + line + \", column \" + column + \".  " +
             //"Encountered: \" + mess);");
      else
         genCodeLine("    return new _ParseException();");//\"Parse error at <unknown location>.  " +
                 //"Encountered: \" + mess);");
      genCodeLine("  }");
    }
    genCodeLine("");

    switchToIncludeFile();
    genCodeLine("  private: int trace_indent;");
    genCodeLine("  private: boolean trace_enabled;");
    if (Options.getDebugParser()) {
      genCodeLine("");

      genCodeLine("/** Enable tracing. */");
      generateMethodDefHeader("void",  cu_name, "enable_tracing()");
      genCodeLine("  {");
      genCodeLine("    trace_enabled = true;");
      genCodeLine("  }");
      genCodeLine("");

      genCodeLine("/** Disable tracing. */");
      generateMethodDefHeader("void",  cu_name, "disble_tracing()");
      genCodeLine("  {");
      genCodeLine("    trace_enabled = false;");
      genCodeLine("  }");
      genCodeLine("");

      generateMethodDefHeader("void",  cu_name, "trace_call(String s)");
      genCodeLine("  {");
      genCodeLine("    if (trace_enabled) {");
      genCodeLine("      for (int i = 0; i < trace_indent; i++) { printf(\" \"); }");
      genCodeLine("      printf(\"Call:   %s\\n\", s.c_str());");
      genCodeLine("    }");
      genCodeLine("    trace_indent = trace_indent + 2;");
      genCodeLine("  }");
      genCodeLine("");

      generateMethodDefHeader("void",  cu_name, "trace_return(String s)");
      genCodeLine("  {");
      genCodeLine("    trace_indent = trace_indent - 2;");
      genCodeLine("    if (trace_enabled) {");
      genCodeLine("      for (int i = 0; i < trace_indent; i++) { printf(\" \"); }");
      genCodeLine("      printf(\"Return: %s\\n\", s.c_str());");
      genCodeLine("    }");
      genCodeLine("  }");
      genCodeLine("");

      generateMethodDefHeader("void",  cu_name, "trace_token(Token t, String where)");
      genCodeLine("  {");
      genCodeLine("    if (trace_enabled) {");
      genCodeLine("      for (int i = 0; i < trace_indent; i++) { printf(\" \"); }");
      genCodeLine("      printf(\"Consumed token: <kind: %d(%s), \\\"%s\\\"\", t->kind, tokenImage[t->kind].c_str(), t->image.c_str());");
      //genCodeLine("      if (t->kind != 0 && !tokenImage[t->kind].equals(\"\\\"\" + t->image + \"\\\"\")) {");
      //genCodeLine("        System.out.print(\": \\\"\" + t->image + \"\\\"\");");
      //genCodeLine("      }");
      genCodeLine("      printf(\" at line %d column %d> %s\\n\", t->beginLine, t->beginColumn, where.c_str());");
      genCodeLine("    }");
      genCodeLine("  }");
      genCodeLine("");

      generateMethodDefHeader("void",  cu_name, "trace_scan(Token t1, int t2)");
      genCodeLine("  {");
      genCodeLine("    if (trace_enabled) {");
      genCodeLine("      for (int i = 0; i < trace_indent; i++) { printf(\" \"); }");
      genCodeLine("      printf(\"Visited token: <Kind: %d(%s), \\\"%s\\\"\", t1->kind, tokenImage[t1->kind].c_str(), t1->image.c_str());");
      //genCodeLine("      if (t1->kind != 0 && !tokenImage[t1->kind].equals(\"\\\"\" + t1->image + \"\\\"\")) {");
      //genCodeLine("        System.out.print(\": \\\"\" + t1->image + \"\\\"\");");
      //genCodeLine("      }");
      genCodeLine("      printf(\" at line %d column %d>; Expected token: %s\\n\", t1->beginLine, t1->beginColumn, tokenImage[t2].c_str());");
      genCodeLine("    }");
      genCodeLine("  }");
      genCodeLine("");
    } else {

      genCodeLine("  /** Enable tracing. */");
      generateMethodDefHeader("void",  cu_name, "enable_tracing()");
      genCodeLine("  {");
      genCodeLine("  }");
      genCodeLine("");
      genCodeLine("  /** Disable tracing. */");
      generateMethodDefHeader("void",  cu_name, "disle_tracing()");
      genCodeLine("  {");
      genCodeLine("  }");
      genCodeLine("");
    }

    if (jj2index != 0 && Options.getErrorReporting()) {
      generateMethodDefHeader("void",  cu_name, "jj_rescan_token()");
      genCodeLine("{");
      genCodeLine("    jj_rescan = true;");
      genCodeLine("    for (int i = 0; i < " + jj2index + "; i++) {");
      genCodeLine("    try {");
      genCodeLine("      JJCalls p = jj_2_rtns[i];");
      genCodeLine("      do {");
      genCodeLine("        if (p->gen > jj_gen) {");
      genCodeLine("          jj_la = p->arg; jj_lastpos = jj_scanpos = p->first;");
      genCodeLine("          switch (i) {");
      for (int i = 0; i < jj2index; i++) {
        genCodeLine("            case " + i + ": jj_3_" + (i+1) + "(); break;");
      }
      genCodeLine("          }");
      genCodeLine("        }");
      genCodeLine("        p = p->next;");
      genCodeLine("      } while (p != null);");
      genCodeLine("      } catch(LookaheadSuccess ls) { }");
      genCodeLine("    }");
      genCodeLine("    jj_rescan = false;");
      genCodeLine("  }");
      genCodeLine("");

      generateMethodDefHeader("void",  cu_name, "jj_save(int index, int xla)");
      genCodeLine("{");
      genCodeLine("    JJCalls p = jj_2_rtns[index];");
      genCodeLine("    while (p->gen > jj_gen) {");
      genCodeLine("      if (p->next == null) { p = p->next = new _JJCalls(); break; }");
      genCodeLine("      p = p->next;");
      genCodeLine("    }");
      genCodeLine("    p->gen = jj_gen + xla - jj_la; p->first = token; p->arg = xla;");
      genCodeLine("  }");
      genCodeLine("");
    }

    if (cu_from_insertion_point_2.size() != 0) {
      printTokenSetup((Token)(cu_from_insertion_point_2.get(0))); ccol = 1;
      for (Iterator it = cu_from_insertion_point_2.iterator(); it.hasNext();) {
        t = (Token)it.next();
        printToken(t);
      }
      printTrailingComments(t);
    }
    genCodeLine("");

    // in the include file close the class signature
    switchToIncludeFile();

    // copy other stuff
    Token t1 = JavaCCGlobals.otherLanguageDeclTokenBeg;
    Token t2 = JavaCCGlobals.otherLanguageDeclTokenEnd;
    while(t1 != t2) {
      printToken(t1);
      t1 = t1.next;
    }

    if (jjtreeGenerated) {
      genCodeLine("_JJT" + cu_name + "State jjtree;");
    }

    genCodeLine(/*{*/ "\n};");

    saveOutput(Options.getOutputDirectory() + File.separator + cu_name + getFileExtension(Options.getOutputLanguage()));
  }

   public static void reInit()
   {
      lookaheadNeeded = false;
   }

}
