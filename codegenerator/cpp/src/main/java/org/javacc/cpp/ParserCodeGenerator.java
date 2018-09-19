// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

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
package org.javacc.cpp;


import static org.javacc.parser.JavaCCGlobals.bnfproductions;
import static org.javacc.parser.JavaCCGlobals.cu_from_insertion_point_2;
import static org.javacc.parser.JavaCCGlobals.cu_name;
import static org.javacc.parser.JavaCCGlobals.cu_to_insertion_point_2;
import static org.javacc.parser.JavaCCGlobals.jj2index;
import static org.javacc.parser.JavaCCGlobals.jjtreeGenerated;
import static org.javacc.parser.JavaCCGlobals.lookaheadNeeded;
import static org.javacc.parser.JavaCCGlobals.maskVals;
import static org.javacc.parser.JavaCCGlobals.maskindex;
import static org.javacc.parser.JavaCCGlobals.names_of_tokens;
import static org.javacc.parser.JavaCCGlobals.production_table;
import static org.javacc.parser.JavaCCGlobals.tokenCount;
import static org.javacc.parser.JavaCCGlobals.toolName;
import static org.javacc.parser.JavaCCGlobals.toolNames;

import org.javacc.parser.*;

import java.io.File;
import java.util.*;

/**
 * Generate the parser.
 */
public class ParserCodeGenerator implements org.javacc.parser.ParserCodeGenerator {

  /**
   * These lists are used to maintain expansions for which code generation
   * in phase 2 and phase 3 is required.  Whenever a call is generated to
   * a phase 2 or phase 3 routine, a corresponding entry is added here if
   * it has not already been added.
   * The phase 3 routines have been optimized in version 0.7pre2.  Essentially
   * only those methods (and only those portions of these methods) are
   * generated that are required.  The lookahead amount is used to determine
   * this.  This change requires the use of a hash table because it is now
   * possible for the same phase 3 routine to be requested multiple times
   * with different lookaheads.  The hash table provides a easily searchable
   * capability to determine the previous requests.
   * The phase 3 routines now are performed in a two step process - the first
   * step gathers the requests (replacing requests with lower lookaheads with
   * those requiring larger lookaheads).  The second step then generates these
   * methods.
   * This optimization and the hashtable makes it look like we do not need
   * the flag "phase3done" any more.  But this has not been removed yet.
   */
  
  private CodeGenHelper codeGenerator = new CppCodeGenHelper();
  private ParserData parserData;
  
  private int cline, ccol;
  
  private final Map<Expansion, String> internalNames = 
      new HashMap<Expansion, String>();
  private final Map<Expansion, Integer> internalIndexes = 
      new HashMap<Expansion, Integer>();
 
  @Override
  public void generateCode(
      CodeGeneratorSettings settings, ParserData parserData) {
    this.parserData = parserData;    

    List<String> tn = new ArrayList<>(toolNames);
    tn.add(toolName);
    codeGenerator.switchToStaticsFile();
    
    codeGenerator.switchToIncludeFile();

    //standard includes
    codeGenerator.genCodeLine("#include \"JavaCC.h\"");
    codeGenerator.genCodeLine("#include \"CharStream.h\"");
    codeGenerator.genCodeLine("#include \"Token.h\"");
    codeGenerator.genCodeLine("#include \"TokenManager.h\"");

    
    Object object = Options.objectValue(Options.USEROPTION__CPP_PARSER_INCLUDES);
    
    if (object instanceof String) {
      String include = (String)object;
      if (include.length() > 0) {
        if (include.charAt(0) == '<')
          codeGenerator.genCodeLine("#include " + include);
        else
          codeGenerator.genCodeLine("#include \"" + include + "\"");
      }
    } else
    if (object instanceof List<?>) {
      for(String include : (List<String>)object)
          if (include.length() > 0) {
            if (include.charAt(0) == '<')
              codeGenerator.genCodeLine("#include " + include);
            else
              codeGenerator.genCodeLine("#include \"" + include + "\"");
          }
    }

    codeGenerator.genCodeLine("#include \"" + cu_name + "Constants.h\"");

    if (jjtreeGenerated) {
      codeGenerator.genCodeLine("#include \"JJT" + cu_name + "State.h\"");
    }

    codeGenerator.genCodeLine("#include \"ErrorHandler.h\"");

    if (jjtreeGenerated) {
      codeGenerator.genCodeLine("#include \"" + cu_name + "Tree.h\"");
    }

    if (Options.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
      codeGenerator.genCodeLine("namespace " + Options.stringValue("NAMESPACE_OPEN"));
    }
    
    codeGenerator.genCodeLine("  struct JJCalls {");
    codeGenerator.genCodeLine("    int        gen;");
    codeGenerator.genCodeLine("    int        arg;");
    codeGenerator.genCodeLine("    JJCalls*   next;");
    codeGenerator.genCodeLine("    Token*     first;");
    codeGenerator.genCodeLine("    ~JJCalls() { if (next) delete next; }");
    codeGenerator.genCodeLine("     JJCalls() { next = nullptr; arg = 0; gen = -1; first = nullptr; }");
    codeGenerator.genCodeLine("  };");
    codeGenerator.genCodeLine("");

   
    String superClass = Options.stringValue(Options.USEROPTION__PARSER_SUPER_CLASS);
    codeGenerator.genClassStart("", cu_name, new String[]{},
                  superClass == null  ? new String[0] : new String[] {
                   "public " + superClass});
    codeGenerator.switchToMainFile();
    if (cu_to_insertion_point_2.size() != 0) {
      codeGenerator.printTokenSetup(cu_to_insertion_point_2.get(0));
      for (Iterator<Token> it = cu_to_insertion_point_2.iterator(); it.hasNext();) {
        codeGenerator.printToken(it.next());
      }
    }

    codeGenerator.switchToMainFile();

    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("");

    build(codeGenerator);
//    new ParseEngine().build(codeGenerator);

    codeGenerator.switchToIncludeFile();
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("public: ");
    codeGenerator.genCodeLine("  void setErrorHandler(ErrorHandler *eh) {");
    codeGenerator.genCodeLine("    if (delete_eh) delete errorHandler;");
    codeGenerator.genCodeLine("    errorHandler = eh;");
    codeGenerator.genCodeLine("    delete_eh = false;");
    codeGenerator.genCodeLine("  }");
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("  TokenManager *token_source = nullptr;");
    codeGenerator.genCodeLine("  CharStream   *jj_input_stream = nullptr;");
    codeGenerator.genCodeLine("  /** Current token. */");
    codeGenerator.genCodeLine("  Token        *token = nullptr;");
    codeGenerator.genCodeLine("  /** Next token. */");
    codeGenerator.genCodeLine("  Token        *jj_nt = nullptr;");
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("private: ");
    codeGenerator.genCodeLine("  int           jj_ntk;");

    codeGenerator.genCodeLine("  JJCalls       jj_2_rtns[" + (jj2index + 1) + "];");
    codeGenerator.genCodeLine("  bool          jj_rescan;");
    codeGenerator.genCodeLine("  int           jj_gc;");
    codeGenerator.genCodeLine("  Token        *jj_scanpos, *jj_lastpos;");
    codeGenerator.genCodeLine("  int           jj_la;");
    codeGenerator.genCodeLine("  /** Whether we are looking ahead. */");
    codeGenerator.genCodeLine("  bool          jj_lookingAhead;");
    codeGenerator.genCodeLine("  bool          jj_semLA;");

    codeGenerator.genCodeLine("  int           jj_gen;");
    codeGenerator.genCodeLine("  int           jj_la1[" + (maskindex + 1) + "];");
    codeGenerator.genCodeLine("  ErrorHandler *errorHandler = nullptr;");
    codeGenerator.genCodeLine("  bool delete_eh = false;");
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("protected: ");
    codeGenerator.genCodeLine("  bool          hasError;");
    codeGenerator.genCodeLine("");
    int tokenMaskSize = (tokenCount-1)/32 + 1;

    if (Options.getErrorReporting() && tokenMaskSize > 0) {
      codeGenerator.switchToStaticsFile();
      for (int i = 0; i < tokenMaskSize; i++) {
        if (maskVals.size() > 0) {
          codeGenerator.genCodeLine("  unsigned int jj_la1_" + i + "[] = {");
          for (Iterator<int[]> it = maskVals.iterator(); it.hasNext();) {
            int[] tokenMask = it.next();
            codeGenerator.genCode("0x" + Integer.toHexString(tokenMask[i]) + ",");
          }
          codeGenerator.genCodeLine("};");
        }
      }
    }

    if (Options.getDepthLimit() > 0) {
      codeGenerator.genCodeLine("  private: int jj_depth;");
      codeGenerator.genCodeLine("  private: bool jj_depth_error;");
      codeGenerator.genCodeLine("  friend class __jj_depth_inc;");
      codeGenerator.genCodeLine("  class __jj_depth_inc {public:");
      codeGenerator.genCodeLine("    " + cu_name + "* parent;");
      codeGenerator.genCodeLine("    __jj_depth_inc(" + cu_name + "* p): parent(p) { parent->jj_depth++; };");
      codeGenerator.genCodeLine("    ~__jj_depth_inc(){ parent->jj_depth--; }");
      codeGenerator.genCodeLine("  };");
    }
    if (!Options.getStackLimit().equals("")) {
      codeGenerator.genCodeLine("  public: size_t jj_stack_limit;");
      codeGenerator.genCodeLine("  private: void* jj_stack_base;");
      codeGenerator.genCodeLine("  private: bool jj_stack_error;");
    }

    codeGenerator.genCodeLine("");

    codeGenerator.genCodeLine("  /** Constructor with user supplied TokenManager. */");

    codeGenerator.switchToIncludeFile(); // TEMP
    codeGenerator.genCodeLine("  Token *head; ");
    codeGenerator.genCodeLine("public: ");
    codeGenerator.generateMethodDefHeader(" ", cu_name, cu_name + "(TokenManager *tokenManager)");
    if (superClass != null)
    {
      codeGenerator.genCodeLine(" : " + superClass + "()");
    }
    codeGenerator.genCodeLine("{");
    codeGenerator.genCodeLine("    head = nullptr;");
    codeGenerator.genCodeLine("    ReInit(tokenManager);");
    if (Options.getTokenManagerUsesParser())
      codeGenerator.genCodeLine("    tokenManager->setParser(this);");
    codeGenerator.genCodeLine("}");

    codeGenerator.switchToIncludeFile();
    codeGenerator.genCodeLine("  virtual ~" + cu_name + "();");
    codeGenerator.switchToMainFile();
    codeGenerator.genCodeLine("" + cu_name + "::~" +cu_name + "()");
    codeGenerator.genCodeLine("{");
    codeGenerator.genCodeLine("  clear();");
    codeGenerator.genCodeLine("}");
    codeGenerator.generateMethodDefHeader("void", cu_name, "ReInit(TokenManager* tokenManager)");
    codeGenerator.genCodeLine("{");
    codeGenerator.genCodeLine("    clear();");    
    codeGenerator.genCodeLine("    errorHandler = new ErrorHandler();");
    codeGenerator.genCodeLine("    delete_eh = true;");
    codeGenerator.genCodeLine("    hasError = false;");
    codeGenerator.genCodeLine("    token_source = tokenManager;");
    codeGenerator.genCodeLine("    head = token = new Token();");
    codeGenerator.genCodeLine("    token->kind = 0;");
    codeGenerator.genCodeLine("    token->next = nullptr;");
    codeGenerator.genCodeLine("    jj_lookingAhead = false;");
    codeGenerator.genCodeLine("    jj_rescan = false;");
    codeGenerator.genCodeLine("    jj_done = false;");
    codeGenerator.genCodeLine("    jj_scanpos = jj_lastpos = nullptr;");
    codeGenerator.genCodeLine("    jj_gc = 0;");
    codeGenerator.genCodeLine("    jj_kind = -1;");
    codeGenerator.genCodeLine("    indent = 0;");
    codeGenerator.genCodeLine("    trace = " + Options.getDebugParser() + ";");
    if (!Options.getStackLimit().equals("")) {
      codeGenerator.genCodeLine("    jj_stack_limit = "+Options.getStackLimit()+";");
      codeGenerator.genCodeLine("    jj_stack_error = jj_stack_check(true);");
    }

    if (Options.getCacheTokens()) {
      codeGenerator.genCodeLine("    token->next = jj_nt = token_source->getNextToken();");
    } else {
      codeGenerator.genCodeLine("    jj_ntk = -1;");
    }
    if (jjtreeGenerated) {
      codeGenerator.genCodeLine("    jjtree.reset();");
    }
    if (Options.getDepthLimit() > 0) {
      codeGenerator.genCodeLine("    jj_depth = 0;");
      codeGenerator.genCodeLine("    jj_depth_error = false;");
    }
    if (Options.getErrorReporting()) {
      codeGenerator.genCodeLine("    jj_gen = 0;");
      if (maskindex > 0) {
        codeGenerator.genCodeLine("    for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
      }
    }
    codeGenerator.genCodeLine("  }");
    codeGenerator.genCodeLine("");
    
    codeGenerator.generateMethodDefHeader("void", cu_name, "clear()");
    codeGenerator.genCodeLine("{");
    codeGenerator.genCodeLine("  //Since token manager was generate from outside,");
    codeGenerator.genCodeLine("  //parser should not take care of deleting");
    codeGenerator.genCodeLine("  //if (token_source) delete token_source;");
    codeGenerator.genCodeLine("  if (head) {");
    codeGenerator.genCodeLine("    Token *next, *t = head;");
    codeGenerator.genCodeLine("    while (t) {");
    codeGenerator.genCodeLine("      next = t->next;");
    codeGenerator.genCodeLine("      delete t;");
    codeGenerator.genCodeLine("      t = next;");
    codeGenerator.genCodeLine("    }");
    codeGenerator.genCodeLine("  }");
    codeGenerator.genCodeLine("  if (delete_eh) {");
    codeGenerator.genCodeLine("    delete errorHandler, errorHandler = nullptr;");
    codeGenerator.genCodeLine("    delete_eh = false;");
    codeGenerator.genCodeLine("  }");
    if (Options.getDepthLimit() > 0) {
      codeGenerator.genCodeLine("  assert(jj_depth==0);");
    }
    codeGenerator.genCodeLine("}");
    codeGenerator.genCodeLine("");

    if (!Options.getStackLimit().equals("")) {
      codeGenerator.genCodeLine("");                                                          
      codeGenerator.switchToIncludeFile();                                                    
      codeGenerator.genCodeLine(" virtual");                                                  
      codeGenerator.switchToMainFile();                                                       
      codeGenerator.generateMethodDefHeader("bool ", cu_name, "jj_stack_check(bool init)");   
      codeGenerator.genCodeLine("  {");                                                       
      codeGenerator.genCodeLine("     if(init) {");                                           
      codeGenerator.genCodeLine("       jj_stack_base = nullptr;");                           
      codeGenerator.genCodeLine("       return false;");                                      
      codeGenerator.genCodeLine("     } else {");                                             
      codeGenerator.genCodeLine("       volatile int q = 0;");                                
      codeGenerator.genCodeLine("       if(!jj_stack_base) {");                               
      codeGenerator.genCodeLine("         jj_stack_base = (void*)&q;");                       
      codeGenerator.genCodeLine("         return false;");                                    
      codeGenerator.genCodeLine("       } else {");                                           
      codeGenerator.genCodeLine("         // Stack can grow in both directions, depending on arch");
      codeGenerator.genCodeLine("         std::ptrdiff_t used = (char*)jj_stack_base-(char*)&q;");
      codeGenerator.genCodeLine("         return (std::abs(used) > jj_stack_limit);");        
      codeGenerator.genCodeLine("       }");                                                  
      codeGenerator.genCodeLine("     }");                                                    
      codeGenerator.genCodeLine("  }");                                                       
    }                                                                           
                                                                                
    
    codeGenerator.generateMethodDefHeader("Token *", cu_name, "jj_consume_token(int kind)", "ParseException");
    codeGenerator.genCodeLine("  {");
    if (!Options.getStackLimit().equals("")) {
      codeGenerator.genCodeLine("    if(kind != -1 && (jj_stack_error || jj_stack_check(false))) {");
      codeGenerator.genCodeLine("      if (!jj_stack_error) {");
      codeGenerator.genCodeLine("        errorHandler->handleOtherError(\"Stack overflow while trying to parse\", this);");
      codeGenerator.genCodeLine("        jj_stack_error=true;");
      codeGenerator.genCodeLine("      }");
      codeGenerator.genCodeLine("      return jj_consume_token(-1);");
      codeGenerator.genCodeLine("    }");
    }
    if (Options.getCacheTokens()) {
      codeGenerator.genCodeLine("    Token *oldToken = token;");
      codeGenerator.genCodeLine("    if ((token = jj_nt)->next != nullptr) jj_nt = jj_nt->next;");
      codeGenerator.genCodeLine("    else jj_nt = jj_nt->next = token_source->getNextToken();");
    } else {
      codeGenerator.genCodeLine("    Token *oldToken;");
      codeGenerator.genCodeLine("    if ((oldToken = token)->next != nullptr) token = token->next;");
      codeGenerator.genCodeLine("    else token = token->next = token_source->getNextToken();");
      codeGenerator.genCodeLine("    jj_ntk = -1;");
    }
    codeGenerator.genCodeLine("    if (token->kind == kind) {");
    if (Options.getErrorReporting()) {
      codeGenerator.genCodeLine("      jj_gen++;");
      if (jj2index != 0) {
        codeGenerator.genCodeLine("      if (++jj_gc > 100) {");
        codeGenerator.genCodeLine("        jj_gc = 0;");
        codeGenerator.genCodeLine("        for (int i = 0; i < " + jj2index + "; i++) {");
        codeGenerator.genCodeLine("          JJCalls *c = &jj_2_rtns[i];");
        codeGenerator.genCodeLine("          while (c != nullptr) {");
        codeGenerator.genCodeLine("            if (c->gen < jj_gen) c->first = nullptr;");
        codeGenerator.genCodeLine("            c = c->next;");
        codeGenerator.genCodeLine("          }");
        codeGenerator.genCodeLine("        }");
        codeGenerator.genCodeLine("      }");
      }
    }
    if (Options.getDebugParser()) {
      codeGenerator.genCodeLine("      trace_token(token, \"\");");
    }
    codeGenerator.genCodeLine("      return token;");
    codeGenerator.genCodeLine("    }");
    if (Options.getCacheTokens()) {
      codeGenerator.genCodeLine("    jj_nt = token;");
    }
    codeGenerator.genCodeLine("    token = oldToken;");
    if (Options.getErrorReporting()) {
      codeGenerator.genCodeLine("    jj_kind = kind;");
    }
    //codeGenerator.genCodeLine("    throw generateParseException();");
    if (!Options.getStackLimit().equals("")) {
      codeGenerator.genCodeLine("    if (!jj_stack_error) {");
    }
    codeGenerator.genCodeLine("    JJString image = kind >= 0 ? tokenImage[kind] : tokenImage[0];");
    codeGenerator.genCodeLine("    errorHandler->handleUnexpectedToken(kind, image.substr(1, image.size() - 2), getToken(1), this);");
    if (!Options.getStackLimit().equals("")) {
      codeGenerator.genCodeLine("    }");
    }
    codeGenerator.genCodeLine("    hasError = true;");
    codeGenerator.genCodeLine("    return token;");
    codeGenerator.genCodeLine("  }");
    codeGenerator.genCodeLine("");

    if (jj2index != 0) {
      codeGenerator.switchToMainFile();
      codeGenerator.generateMethodDefHeader("bool ", cu_name, "jj_scan_token(int kind)");
      codeGenerator.genCodeLine("{");
      if (!Options.getStackLimit().equals("")) {
        codeGenerator.genCodeLine("    if(kind != -1 && (jj_stack_error || jj_stack_check(false))) {");
        codeGenerator.genCodeLine("      if (!jj_stack_error) {");
        codeGenerator.genCodeLine("        errorHandler->handleOtherError(\"Stack overflow while trying to parse\", this);");
        codeGenerator.genCodeLine("        jj_stack_error=true;");
        codeGenerator.genCodeLine("      }");
        codeGenerator.genCodeLine("      return jj_consume_token(-1);");
        codeGenerator.genCodeLine("    }");
      }
      codeGenerator.genCodeLine("    if (jj_scanpos == jj_lastpos) {");
      codeGenerator.genCodeLine("      jj_la--;");
      codeGenerator.genCodeLine("      if (jj_scanpos->next == nullptr) {");
      codeGenerator.genCodeLine("        jj_lastpos = jj_scanpos = jj_scanpos->next = token_source->getNextToken();");
      codeGenerator.genCodeLine("      } else {");
      codeGenerator.genCodeLine("        jj_lastpos = jj_scanpos = jj_scanpos->next;");
      codeGenerator.genCodeLine("      }");
      codeGenerator.genCodeLine("    } else {");
      codeGenerator.genCodeLine("      jj_scanpos = jj_scanpos->next;");
      codeGenerator.genCodeLine("    }");
      if (Options.getErrorReporting()) {
        codeGenerator.genCodeLine("    if (jj_rescan) {");
        codeGenerator.genCodeLine("      int i = 0; Token *tok = token;");
        codeGenerator.genCodeLine("      while (tok != nullptr && tok != jj_scanpos) { i++; tok = tok->next; }");
        codeGenerator.genCodeLine("      if (tok != nullptr) jj_add_error_token(kind, i);");
        if (Options.getDebugLookahead()) {
          codeGenerator.genCodeLine("    } else {");
          codeGenerator.genCodeLine("      trace_scan(jj_scanpos, kind);");
        }
        codeGenerator.genCodeLine("    }");
      } else if (Options.getDebugLookahead()) {
        codeGenerator.genCodeLine("    trace_scan(jj_scanpos, kind);");
      }
      codeGenerator.genCodeLine("    if (jj_scanpos->kind != kind) return true;");
      //codeGenerator.genCodeLine("    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;");
      codeGenerator.genCodeLine("    if (jj_la == 0 && jj_scanpos == jj_lastpos) { return jj_done = true; }");
      codeGenerator.genCodeLine("    return false;");
      codeGenerator.genCodeLine("  }");
      codeGenerator.genCodeLine("");
    }
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("/** Get the next Token. */");
    codeGenerator.generateMethodDefHeader("Token *", cu_name, "getNextToken()");
    codeGenerator.genCodeLine("{");
    if (Options.getCacheTokens()) {
      codeGenerator.genCodeLine("    if ((token = jj_nt)->next != nullptr) jj_nt = jj_nt->next;");
      codeGenerator.genCodeLine("    else jj_nt = jj_nt->next = token_source->getNextToken();");
    } else {
      codeGenerator.genCodeLine("    if (token->next != nullptr) token = token->next;");
      codeGenerator.genCodeLine("    else token = token->next = token_source->getNextToken();");
      codeGenerator.genCodeLine("    jj_ntk = -1;");
    }
    if (Options.getErrorReporting()) {
      codeGenerator.genCodeLine("    jj_gen++;");
    }
    if (Options.getDebugParser()) {
      codeGenerator.genCodeLine("      trace_token(token, \" (in getNextToken)\");");
    }
    codeGenerator.genCodeLine("    return token;");
    codeGenerator.genCodeLine("  }");
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("/** Get the specific Token. */");
    codeGenerator.generateMethodDefHeader("Token *", cu_name, "getToken(int index)");
    codeGenerator.genCodeLine("{");
    if (lookaheadNeeded) {
      codeGenerator.genCodeLine("    Token *t = jj_lookingAhead ? jj_scanpos : token;");
    } else {
      codeGenerator.genCodeLine("    Token *t = token;");
    }
    codeGenerator.genCodeLine("    for (int i = 0; i < index; i++) {");
    codeGenerator.genCodeLine("      if (t->next != nullptr) t = t->next;");
    codeGenerator.genCodeLine("      else t = t->next = token_source->getNextToken();");
    codeGenerator.genCodeLine("    }");
    codeGenerator.genCodeLine("    return t;");
    codeGenerator.genCodeLine("  }");
    codeGenerator.genCodeLine("");
    if (!Options.getCacheTokens()) {
      codeGenerator.generateMethodDefHeader("int", cu_name, "jj_ntk_f()");
      codeGenerator.genCodeLine("{");
 
      codeGenerator.genCodeLine("    if ((jj_nt=token->next) == nullptr)");
      codeGenerator.genCodeLine("      return (jj_ntk = (token->next=token_source->getNextToken())->kind);");
      codeGenerator.genCodeLine("    else");
      codeGenerator.genCodeLine("      return (jj_ntk = jj_nt->kind);");
      codeGenerator.genCodeLine("  }");
      codeGenerator.genCodeLine("");
    }

    codeGenerator.switchToIncludeFile();
    codeGenerator.genCodeLine("private:");
    codeGenerator.genCodeLine("  int jj_kind;");
    if (Options.getErrorReporting()) {
      codeGenerator.genCodeLine("  int **jj_expentries;");
      codeGenerator.genCodeLine("  int *jj_expentry;");
      if (jj2index != 0) {
        codeGenerator.switchToStaticsFile();
        // For now we don't support ERROR_REPORTING in the C++ version.
        //codeGenerator.genCodeLine("  static int *jj_lasttokens = new int[100];");
        //codeGenerator.genCodeLine("  static int jj_endpos;");
        codeGenerator.genCodeLine("");

        codeGenerator. generateMethodDefHeader("  void",  cu_name, "jj_add_error_token(int kind, int pos)");
        codeGenerator.genCodeLine("  {");
        // For now we don't support ERROR_REPORTING in the C++ version.

        //codeGenerator.genCodeLine("    if (pos >= 100) return;");
        //codeGenerator.genCodeLine("    if (pos == jj_endpos + 1) {");
        //codeGenerator.genCodeLine("      jj_lasttokens[jj_endpos++] = kind;");
        //codeGenerator.genCodeLine("    } else if (jj_endpos != 0) {");
        //codeGenerator.genCodeLine("      jj_expentry = new int[jj_endpos];");
        //codeGenerator.genCodeLine("      for (int i = 0; i < jj_endpos; i++) {");
        //codeGenerator.genCodeLine("        jj_expentry[i] = jj_lasttokens[i];");
        //codeGenerator.genCodeLine("      }");
        //codeGenerator.genCodeLine("      jj_entries_loop: for (java.util.Iterator it = jj_expentries.iterator(); it.hasNext();) {");
        //codeGenerator.genCodeLine("        int[] oldentry = (int[])(it->next());");
        //codeGenerator.genCodeLine("        if (oldentry.length == jj_expentry.length) {");
        //codeGenerator.genCodeLine("          for (int i = 0; i < jj_expentry.length; i++) {");
        //codeGenerator.genCodeLine("            if (oldentry[i] != jj_expentry[i]) {");
        //codeGenerator.genCodeLine("              continue jj_entries_loop;");
        //codeGenerator.genCodeLine("            }");
        //codeGenerator.genCodeLine("          }");
        //codeGenerator.genCodeLine("          jj_expentries.add(jj_expentry);");
        //codeGenerator.genCodeLine("          break jj_entries_loop;");
        //codeGenerator.genCodeLine("        }");
        //codeGenerator.genCodeLine("      }");
        //codeGenerator.genCodeLine("      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;");
        //codeGenerator.genCodeLine("    }");
        codeGenerator.genCodeLine("  }");
      }
      codeGenerator.genCodeLine("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.genCodeLine("protected:");
      codeGenerator.genCodeLine("  /** Generate ParseException. */");
      codeGenerator.generateMethodDefHeader("  virtual void ",  cu_name, "parseError()");
      codeGenerator.genCodeLine("   {");
      if (Options.getErrorReporting()) {
        codeGenerator.genCodeLine("      fprintf(stderr, \"Parse error at: %d:%d, after token: %s encountered: %s\\n\", token->beginLine, token->beginColumn, addUnicodeEscapes(token->image).c_str(), addUnicodeEscapes(getToken(1)->image).c_str());");
      }
      codeGenerator.genCodeLine("   }");
      /*generateMethodDefHeader("ParseException",  cu_name, "generateParseException()");
      codeGenerator.genCodeLine("   {");
      //codeGenerator.genCodeLine("    jj_expentries.clear();");
      //codeGenerator.genCodeLine("    bool[] la1tokens = new boolean[" + tokenCount + "];");
      //codeGenerator.genCodeLine("    if (jj_kind >= 0) {");
      //codeGenerator.genCodeLine("      la1tokens[jj_kind] = true;");
      //codeGenerator.genCodeLine("      jj_kind = -1;");
      //codeGenerator.genCodeLine("    }");
      //codeGenerator.genCodeLine("    for (int i = 0; i < " + maskindex + "; i++) {");
      //codeGenerator.genCodeLine("      if (jj_la1[i] == jj_gen) {");
      //codeGenerator.genCodeLine("        for (int j = 0; j < 32; j++) {");
      //for (int i = 0; i < (tokenCount-1)/32 + 1; i++) {
        //codeGenerator.genCodeLine("          if ((jj_la1_" + i + "[i] & (1<<j)) != 0) {");
        //genCode("            la1tokens[");
        //if (i != 0) {
          //genCode((32*i) + "+");
        //}
        //codeGenerator.genCodeLine("j] = true;");
        //codeGenerator.genCodeLine("          }");
      //}
      //codeGenerator.genCodeLine("        }");
      //codeGenerator.genCodeLine("      }");
      //codeGenerator.genCodeLine("    }");
      //codeGenerator.genCodeLine("    for (int i = 0; i < " + tokenCount + "; i++) {");
      //codeGenerator.genCodeLine("      if (la1tokens[i]) {");
      //codeGenerator.genCodeLine("        jj_expentry = new int[1];");
      //codeGenerator.genCodeLine("        jj_expentry[0] = i;");
      //codeGenerator.genCodeLine("        jj_expentries.add(jj_expentry);");
      //codeGenerator.genCodeLine("      }");
      //codeGenerator.genCodeLine("    }");
      //if (jj2index != 0) {
        //codeGenerator.genCodeLine("    jj_endpos = 0;");
        //codeGenerator.genCodeLine("    jj_rescan_token();");
        //codeGenerator.genCodeLine("    jj_add_error_token(0, 0);");
      //}
      //codeGenerator.genCodeLine("    int exptokseq[][1] = new int[1];");
      //codeGenerator.genCodeLine("    for (int i = 0; i < jj_expentries.size(); i++) {");
      //if (!Options.getGenerateGenerics())
         //codeGenerator.genCodeLine("      exptokseq[i] = (int[])jj_expentries.get(i);");
      //else
         //codeGenerator.genCodeLine("      exptokseq[i] = jj_expentries.get(i);");
      //codeGenerator.genCodeLine("    }");
      codeGenerator.genCodeLine("    return new _ParseException();");//token, nullptr, tokenImage);");
      codeGenerator.genCodeLine("  }"); */
    } else {
      codeGenerator.genCodeLine("protected:");
      codeGenerator.genCodeLine("  /** Generate ParseException. */");
      codeGenerator.generateMethodDefHeader("virtual void ",  cu_name, "parseError()");
      codeGenerator.genCodeLine("   {");
      if (Options.getErrorReporting()) {
        codeGenerator.genCodeLine("      fprintf(stderr, \"Parse error at: %d:%d, after token: %s encountered: %s\\n\", token->beginLine, token->beginColumn, addUnicodeEscapes(token->image).c_str(), addUnicodeEscapes(getToken(1)->image).c_str());");
      }
      codeGenerator.genCodeLine("   }");
      /*generateMethodDefHeader("ParseException",  cu_name, "generateParseException()");
      codeGenerator.genCodeLine("   {");
      codeGenerator.genCodeLine("    Token *errortok = token->next;");
      if (Options.getKeepLineColumn())
         codeGenerator.genCodeLine("    int line = errortok.beginLine, column = errortok.beginColumn;");
      codeGenerator.genCodeLine("    JJString mess = (errortok->kind == 0) ? tokenImage[0] : errortok->image;");
      if (Options.getKeepLineColumn())
         codeGenerator.genCodeLine("    return new _ParseException();");// +
             //"\"Parse error at line \" + line + \", column \" + column + \".  " +
             //"Encountered: \" + mess);");
      else
         codeGenerator.genCodeLine("    return new _ParseException();");//\"Parse error at <unknown location>.  " +
                 //"Encountered: \" + mess);");
      codeGenerator.genCodeLine("  }");*/
    }
    codeGenerator.genCodeLine("");

    codeGenerator.switchToIncludeFile();
    codeGenerator.genCodeLine("private:");
    codeGenerator.genCodeLine("  int  indent; // trace indentation");
    codeGenerator.genCodeLine("  bool trace = " + Options.getDebugParser() + "; // trace enabled if true");
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("public:");
    codeGenerator.generateMethodDefHeader("  bool",  cu_name, "trace_enabled()");
    codeGenerator.genCodeLine("  {");
    codeGenerator.genCodeLine("    return trace;");
    codeGenerator.genCodeLine("  }");
    codeGenerator.genCodeLine("");
   if (Options.getDebugParser()) {
     codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void",  cu_name, "enable_tracing()");
      codeGenerator.genCodeLine("{");
      codeGenerator.genCodeLine("    trace = true;");
      codeGenerator.genCodeLine("}");
      codeGenerator.genCodeLine("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void",  cu_name, "disable_tracing()");
      codeGenerator.genCodeLine("{");
      codeGenerator.genCodeLine("    trace = false;");
      codeGenerator.genCodeLine("}");
      codeGenerator.genCodeLine("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void",  cu_name, "trace_call(const char *s)");
      codeGenerator.genCodeLine("  {");
      codeGenerator.genCodeLine("    if (trace_enabled()) {");
      codeGenerator.genCodeLine("      for (int i = 0; i < indent; i++) { printf(\" \"); }");
      codeGenerator.genCodeLine("      printf(\"Call:   %s\\n\", s);");
      codeGenerator.genCodeLine("    }");
      codeGenerator.genCodeLine("    indent = indent + 2;");
      codeGenerator.genCodeLine("  }");
      codeGenerator.genCodeLine("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void",  cu_name, "trace_return(const char *s)");
      codeGenerator.genCodeLine("  {");
      codeGenerator.genCodeLine("    indent = indent - 2;");
      codeGenerator.genCodeLine("    if (trace_enabled()) {");
      codeGenerator.genCodeLine("      for (int i = 0; i < indent; i++) { printf(\" \"); }");
      codeGenerator.genCodeLine("      printf(\"Return: %s\\n\", s);");
      codeGenerator.genCodeLine("    }");
      codeGenerator.genCodeLine("  }");
      codeGenerator.genCodeLine("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void",  cu_name, "trace_token(Token *t, const char *where)");
      codeGenerator.genCodeLine("  {");
      codeGenerator.genCodeLine("    if (trace_enabled()) {");
      codeGenerator.genCodeLine("      for (int i = 0; i < indent; i++) { printf(\" \"); }");
      codeGenerator.genCodeLine("      printf(\"Consumed token: <kind: %d(%s), \\\"%s\\\"\", t->kind, addUnicodeEscapes(tokenImage[t->kind]).c_str(), addUnicodeEscapes(t->image).c_str());");
      //codeGenerator.genCodeLine("      if (t->kind != 0 && !tokenImage[t->kind].equals(\"\\\"\" + t->image + \"\\\"\")) {");
      //codeGenerator.genCodeLine("        System.out.print(\": \\\"\" + t->image + \"\\\"\");");
      //codeGenerator.genCodeLine("      }");
      codeGenerator.genCodeLine("      printf(\" at line %d column %d> %s\\n\", t->beginLine, t->beginColumn, where);");
      codeGenerator.genCodeLine("    }");
      codeGenerator.genCodeLine("  }");
      codeGenerator.genCodeLine("");

      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void",  cu_name, "trace_scan(Token *t1, int t2)");
      codeGenerator.genCodeLine("  {");
      codeGenerator.genCodeLine("    if (trace_enabled()) {");
      codeGenerator.genCodeLine("      for (int i = 0; i < indent; i++) { printf(\" \"); }");
      codeGenerator.genCodeLine("      printf(\"Visited token: <Kind: %d(%s), \\\"%s\\\"\", t1->kind, addUnicodeEscapes(tokenImage[t1->kind]).c_str(), addUnicodeEscapes(t1->image).c_str());");
      //codeGenerator.genCodeLine("      if (t1->kind != 0 && !tokenImage[t1->kind].equals(\"\\\"\" + t1->image + \"\\\"\")) {");
      //codeGenerator.genCodeLine("        System.out.print(\": \\\"\" + t1->image + \"\\\"\");");
      //codeGenerator.genCodeLine("      }");
      codeGenerator.genCodeLine("      printf(\" at line %d column %d>; Expected token: %s\\n\", t1->beginLine, t1->beginColumn, addUnicodeEscapes(tokenImage[t2]).c_str());");
      codeGenerator.genCodeLine("    }");
      codeGenerator.genCodeLine("  }");
      codeGenerator.genCodeLine("");
    } else {
      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void",  cu_name, "enable_tracing()");
      codeGenerator.genCodeLine("  {");
      codeGenerator.genCodeLine("  }");
      codeGenerator.switchToIncludeFile();
      codeGenerator.generateMethodDefHeader("  void",  cu_name, "disable_tracing()");
      codeGenerator.genCodeLine("  {");
      codeGenerator.genCodeLine("  }");
      codeGenerator.genCodeLine("");
    }

    if (jj2index != 0 && Options.getErrorReporting()) {
      codeGenerator.generateMethodDefHeader("  void",  cu_name, "jj_rescan_token()");
      codeGenerator.genCodeLine("{");
      codeGenerator.genCodeLine("    jj_rescan = true;");
      codeGenerator.genCodeLine("    for (int i = 0; i < " + jj2index + "; i++) {");
      //codeGenerator.genCodeLine("    try {");
      codeGenerator.genCodeLine("      JJCalls *p = &jj_2_rtns[i];");
      codeGenerator.genCodeLine("      do {");
      codeGenerator.genCodeLine("        if (p->gen > jj_gen) {");
      codeGenerator.genCodeLine("          jj_la = p->arg; jj_lastpos = jj_scanpos = p->first;");
      codeGenerator.genCodeLine("          switch (i) {");
      for (int i = 0; i < jj2index; i++) {
        codeGenerator.genCodeLine("            case " + i + ": jj_3_" + (i+1) + "(); break;");
      }
      codeGenerator.genCodeLine("          }");
      codeGenerator.genCodeLine("        }");
      codeGenerator.genCodeLine("        p = p->next;");
      codeGenerator.genCodeLine("      } while (p != nullptr);");
      //codeGenerator.genCodeLine("      } catch(LookaheadSuccess ls) { }");
      codeGenerator.genCodeLine("    }");
      codeGenerator.genCodeLine("    jj_rescan = false;");
      codeGenerator.genCodeLine("  }");
      codeGenerator.genCodeLine("");

      codeGenerator.generateMethodDefHeader("  void",  cu_name, "jj_save(int index, int xla)");
      codeGenerator.genCodeLine("{");
      codeGenerator.genCodeLine("    JJCalls *p = &jj_2_rtns[index];");
      codeGenerator.genCodeLine("    while (p->gen > jj_gen) {");
      codeGenerator.genCodeLine("      if (p->next == nullptr) { p = p->next = new JJCalls(); break; }");
      codeGenerator.genCodeLine("      p = p->next;");
      codeGenerator.genCodeLine("    }");
      codeGenerator.genCodeLine("    p->gen = jj_gen + xla - jj_la; p->first = token; p->arg = xla;");
      codeGenerator.genCodeLine("  }");
      codeGenerator.genCodeLine("");
    }

    if (cu_from_insertion_point_2.size() != 0) {
      Token t = null;
      codeGenerator.printTokenSetup((cu_from_insertion_point_2.get(0))); ccol = 1;
      for (Iterator<Token> it = cu_from_insertion_point_2.iterator(); it.hasNext();) {
        t = it.next();
        codeGenerator.printToken(t);
      }
      codeGenerator.printTrailingComments(t);
    }
    codeGenerator.genCodeLine("");

    // in the include file close the class signature
    codeGenerator.switchToIncludeFile();

    // copy other stuff
    Token t1 = JavaCCGlobals.otherLanguageDeclTokenBeg;
    Token t2 = JavaCCGlobals.otherLanguageDeclTokenEnd;
    while(t1 != t2) {
      codeGenerator.printToken(t1);
      t1 = t1.next;
    }
    codeGenerator.genCodeLine("\n");
    if (jjtreeGenerated) {
      codeGenerator.genCodeLine("  JJT" + cu_name + "State jjtree;");
    }
    codeGenerator.genCodeLine("private:");
    codeGenerator.genCodeLine("  bool jj_done;");

    codeGenerator.genCodeLine( "};");
    
    codeGenerator.saveOutput(Options.getOutputDirectory() + File.separator + parserData.parserName + ".cc");
  }

  @Override
  public void finish(
      CodeGeneratorSettings settings,
      ParserData parserData) {
  }

  private int gensymindex = 0;
  private int indentamt;
  private boolean jj2LA;


  /**
   * These lists are used to maintain expansions for which code generation
   * in phase 2 and phase 3 is required.  Whenever a call is generated to
   * a phase 2 or phase 3 routine, a corresponding entry is added here if
   * it has not already been added.
   * The phase 3 routines have been optimized in version 0.7pre2.  Essentially
   * only those methods (and only those portions of these methods) are
   * generated that are required.  The lookahead amount is used to determine
   * this.  This change requires the use of a hash table because it is now
   * possible for the same phase 3 routine to be requested multiple times
   * with different lookaheads.  The hash table provides a easily searchable
   * capability to determine the previous requests.
   * The phase 3 routines now are performed in a two step process - the first
   * step gathers the requests (replacing requests with lower lookaheads with
   * those requiring larger lookaheads).  The second step then generates these
   * methods.
   * This optimization and the hashtable makes it look like we do not need
   * the flag "phase3done" any more.  But this has not been removed yet.
   */
  private List<Lookahead> phase2list = new ArrayList<>();
  private List<Phase3Data> phase3list = new ArrayList<>();
  private Hashtable<Expansion, Phase3Data> phase3table = new Hashtable<>();

  /**
   * The phase 1 routines generates their output into String's and dumps
   * these String's once for each method.  These String's contain the
   * special characters '\u0001' to indicate a positive indent, and '\u0002'
   * to indicate a negative indent.  '\n' is used to indicate a line terminator.
   * The characters '\u0003' and '\u0004' are used to delineate portions of
   * text where '\n's should not be followed by an indentation.
   */

  /**
   * Returns true if there is a JAVACODE production that the argument expansion
   * may directly expand to (without consuming tokens or encountering lookahead).
   */
  private boolean javaCodeCheck(Expansion exp) {
    if (exp instanceof RegularExpression) {
      return false;
    } else if (exp instanceof NonTerminal) {
      NormalProduction prod = ((NonTerminal)exp).getProd();
      if (prod instanceof CodeProduction) {
        return true;
      } else {
        return javaCodeCheck(prod.getExpansion());
      }
    } else if (exp instanceof Choice) {
      Choice ch = (Choice)exp;
      for (int i = 0; i < ch.getChoices().size(); i++) {
        if (javaCodeCheck(ch.getChoices().get(i))) {
          return true;
        }
      }
      return false;
    } else if (exp instanceof Sequence) {
      Sequence seq = (Sequence)exp;
      for (int i = 0; i < seq.units.size(); i++) {
        Expansion[] units = seq.units.toArray(new Expansion[seq.units.size()]);
        if (units[i] instanceof Lookahead && ((Lookahead)units[i]).isExplicit()) {
          // An explicit lookahead (rather than one generated implicitly). Assume
          // the user knows what he / she is doing, e.g.
          //    "A" ( "B" | LOOKAHEAD("X") jcode() | "C" )* "D"
          return false;
        } else if (javaCodeCheck(units[i])) {
          return true;
        } else if (!Semanticize.emptyExpansionExists(units[i])) {
          return false;
        }
      }
      return false;
    } else if (exp instanceof OneOrMore) {
      OneOrMore om = (OneOrMore)exp;
      return javaCodeCheck(om.expansion);
    } else if (exp instanceof ZeroOrMore) {
      ZeroOrMore zm = (ZeroOrMore)exp;
      return javaCodeCheck(zm.expansion);
    } else if (exp instanceof ZeroOrOne) {
      ZeroOrOne zo = (ZeroOrOne)exp;
      return javaCodeCheck(zo.expansion);
    } else if (exp instanceof TryBlock) {
      TryBlock tb = (TryBlock)exp;
      return javaCodeCheck(tb.exp);
    } else {
      return false;
    }
  }

  /**
   * An array used to store the first sets generated by the following method.
   * A true entry means that the corresponding token is in the first set.
   */
  private boolean[] firstSet;

  /**
   * Sets up the array "firstSet" above based on the Expansion argument
   * passed to it.  Since this is a recursive function, it assumes that
   * "firstSet" has been reset before the first call.
   */
  private void genFirstSet(Expansion exp) {
    if (exp instanceof RegularExpression) {
      firstSet[((RegularExpression)exp).ordinal] = true;
    } else if (exp instanceof NonTerminal) {
      if (!(((NonTerminal)exp).getProd() instanceof CodeProduction))
      {
        genFirstSet(((BNFProduction)((NonTerminal)exp).getProd()).getExpansion());
      }
    } else if (exp instanceof Choice) {
      Choice ch = (Choice)exp;
      for (int i = 0; i < ch.getChoices().size(); i++) {
        genFirstSet(ch.getChoices().get(i));
      }
    } else if (exp instanceof Sequence) {
      Sequence seq = (Sequence)exp;
      Object obj = seq.units.get(0);
      if (obj instanceof Lookahead && ((Lookahead)obj).getActionTokens().size() != 0) {
        jj2LA = true;
      }
      for (int i = 0; i < seq.units.size(); i++) {
        Expansion unit = seq.units.get(i);
        // Javacode productions can not have FIRST sets. Instead we generate the FIRST set
        // for the preceding LOOKAHEAD (the semantic checks should have made sure that
        // the LOOKAHEAD is suitable).
        if (unit instanceof NonTerminal && ((NonTerminal)unit).getProd() instanceof CodeProduction) {
          if (i > 0 && seq.units.get(i-1) instanceof Lookahead) {
            Lookahead la = (Lookahead)seq.units.get(i-1);
            genFirstSet(la.getLaExpansion());
          }
        } else {
          genFirstSet(seq.units.get(i));
        }
        if (!Semanticize.emptyExpansionExists(seq.units.get(i))) {
          break;
        }
      }
    } else if (exp instanceof OneOrMore) {
      OneOrMore om = (OneOrMore)exp;
      genFirstSet(om.expansion);
    } else if (exp instanceof ZeroOrMore) {
      ZeroOrMore zm = (ZeroOrMore)exp;
      genFirstSet(zm.expansion);
    } else if (exp instanceof ZeroOrOne) {
      ZeroOrOne zo = (ZeroOrOne)exp;
      genFirstSet(zo.expansion);
    } else if (exp instanceof TryBlock) {
      TryBlock tb = (TryBlock)exp;
      genFirstSet(tb.exp);
    }
  }

  /**
   * Constants used in the following method "buildLookaheadChecker".
   */
  final int NOOPENSTM = 0;
  final int OPENIF = 1;
  final int OPENSWITCH = 2;

  private void dumpLookaheads(Lookahead[] conds, String[] actions) {
    for (int i = 0; i < conds.length; i++) {
      System.err.println("Lookahead: " + i);
      System.err.println(conds[i].dump(0, new HashSet()));
      System.err.println();
    }
  }

  /**
   * This method takes two parameters - an array of Lookahead's
   * "conds", and an array of String's "actions".  "actions" contains
   * exactly one element more than "conds".  "actions" are Java source
   * code, and "conds" translate to conditions - so lets say
   * "f(conds[i])" is true if the lookahead required by "conds[i]" is
   * indeed the case.  This method returns a string corresponding to
   * the Java code for:
   *
   *   if (f(conds[0]) actions[0]
   *   else if (f(conds[1]) actions[1]
   *   . . .
   *   else actions[action.length-1]
   *
   * A particular action entry ("actions[i]") can be null, in which
   * case, a noop is generated for that action.
   */
  String buildLookaheadChecker(Lookahead[] conds, String[] actions) {

    // The state variables.
    int state = NOOPENSTM;
    int indentAmt = 0;
    boolean[] casedValues = new boolean[tokenCount];
    String retval = "";
    Lookahead la;
    Token t = null;
    int tokenMaskSize = (tokenCount-1)/32 + 1;
    int[] tokenMask = null;

    // Iterate over all the conditions.
    int index = 0;
    while (index < conds.length) {

      la = conds[index];
      jj2LA = false;

      if (la.getAmount() == 0 ||
          Semanticize.emptyExpansionExists(la.getLaExpansion()) ||
          javaCodeCheck(la.getLaExpansion())
      ) {

        // This handles the following cases:
        // . If syntactic lookahead is not wanted (and hence explicitly specified
        //   as 0).
        // . If it is possible for the lookahead expansion to recognize the empty
        //   string - in which case the lookahead trivially passes.
        // . If the lookahead expansion has a JAVACODE production that it directly
        //   expands to - in which case the lookahead trivially passes.
        if (la.getActionTokens().size() == 0) {
          // In addition, if there is no semantic lookahead, then the
          // lookahead trivially succeeds.  So break the main loop and
          // treat this case as the default last action.
          break;
        } else {
          // This case is when there is only semantic lookahead
          // (without any preceding syntactic lookahead).  In this
          // case, an "if" statement is generated.
          switch (state) {
          case NOOPENSTM:
            retval += "\n" + "if (";
            indentAmt++;
            break;
          case OPENIF:
            retval += "\u0002\n" + "} else if (";
            break;
          case OPENSWITCH:
            retval += "\u0002\n" + "default:" + "\u0001";
            if (Options.getErrorReporting()) {
              retval += "\njj_la1[" + maskindex + "] = jj_gen;";
              maskindex++;
            }
            maskVals.add(tokenMask);
            retval += "\n" + "if (";
            indentAmt++;
          }
          codeGenerator.printTokenSetup(la.getActionTokens().get(0));
          for (Iterator<Token> it = la.getActionTokens().iterator(); it.hasNext();) {
            t = it.next();
            retval += CodeGenHelper.getStringToPrint(t);
          }
          retval += codeGenerator.getTrailingComments(t);
          retval += ") {\u0001" + actions[index];
          state = OPENIF;
        }

      } else if (la.getAmount() == 1 && la.getActionTokens().size() == 0) {
        // Special optimal processing when the lookahead is exactly 1, and there
        // is no semantic lookahead.

        if (firstSet == null) {
          firstSet = new boolean[tokenCount];
        }
        for (int i = 0; i < tokenCount; i++) {
          firstSet[i] = false;
        }
        // jj2LA is set to false at the beginning of the containing "if" statement.
        // It is checked immediately after the end of the same statement to determine
        // if lookaheads are to be performed using calls to the jj2 methods.
        genFirstSet(la.getLaExpansion());
        // genFirstSet may find that semantic attributes are appropriate for the next
        // token.  In which case, it sets jj2LA to true.
        if (!jj2LA) {

          // This case is if there is no applicable semantic lookahead and the lookahead
          // is one (excluding the earlier cases such as JAVACODE, etc.).
          switch (state) {
          case OPENIF:
            retval += "\u0002\n" + "} else {\u0001";
            // Control flows through to next case.
          case NOOPENSTM:
            retval += "\n" + "switch (";
            if (Options.getCacheTokens()) {
              retval += "jj_nt->kind";
              retval += ") {\u0001";
            } else {
              retval += "(jj_ntk==-1)?jj_ntk_f():jj_ntk) {\u0001";
            }
            for (int i = 0; i < tokenCount; i++) {
              casedValues[i] = false;
            }
            indentAmt++;
            tokenMask = new int[tokenMaskSize];
            for (int i = 0; i < tokenMaskSize; i++) {
              tokenMask[i] = 0;
            }
            // Don't need to do anything if state is OPENSWITCH.
          }
          for (int i = 0; i < tokenCount; i++) {
            if (firstSet[i]) {
              if (!casedValues[i]) {
                casedValues[i] = true;
                retval += "\u0002\ncase ";
                int j1 = i/32;
                int j2 = i%32;
                tokenMask[j1] |= 1 << j2;
                String s = names_of_tokens.get(Integer.valueOf(i));
                if (s == null) {
                  retval += i;
                } else {
                  retval += s;
                }
                retval += ":\u0001";
              }
            }
          }
          retval += "{";
          retval += actions[index];
          retval += "\nbreak;\n}";
          state = OPENSWITCH;

        }

      } else {
        // This is the case when lookahead is determined through calls to
        // jj2 methods.  The other case is when lookahead is 1, but semantic
        // attributes need to be evaluated.  Hence this crazy control structure.

        jj2LA = true;

      }

      if (jj2LA) {
        // In this case lookahead is determined by the jj2 methods.

        switch (state) {
        case NOOPENSTM:
          retval += "\n" + "if (";
          indentAmt++;
          break;
        case OPENIF:
          retval += "\u0002\n" + "} else if (";
          break;
        case OPENSWITCH:
          retval += "\u0002\n" + "default:" + "\u0001";
          if (Options.getErrorReporting()) {
            retval += "\njj_la1[" + maskindex + "] = jj_gen;";
            maskindex++;
          }
          maskVals.add(tokenMask);
          retval += "\n" + "if (";
          indentAmt++;
        }
        jj2index++;
        // At this point, la.la_expansion.internal_name must be "".
        internalNames.put(la.getLaExpansion(), "_" + jj2index);
        internalIndexes.put(la.getLaExpansion(),  jj2index);
        phase2list.add(la);
        retval += "jj_2" + internalNames.get(la.getLaExpansion()) + "(" + la.getAmount() + ")";
        if (la.getActionTokens().size() != 0) {
          // In addition, there is also a semantic lookahead.  So concatenate
          // the semantic check with the syntactic one.
          retval += " && (";
          codeGenerator.printTokenSetup(la.getActionTokens().get(0));
          for (Iterator<Token> it = la.getActionTokens().iterator(); it.hasNext();) {
            t = it.next();
            retval += CodeGenHelper.getStringToPrint(t);
          }
          retval += codeGenerator.getTrailingComments(t);
          retval += ")";
        }
        retval += ") {\u0001" + actions[index];
        state = OPENIF;
      }

      index++;
    }

    // Generate code for the default case.  Note this may not
    // be the last entry of "actions" if any condition can be
    // statically determined to be always "true".

    switch (state) {
    case NOOPENSTM:
      retval += actions[index];
      break;
    case OPENIF:
      retval += "\u0002\n" + "} else {\u0001" + actions[index];
      break;
    case OPENSWITCH:
      retval += "\u0002\n" + "default:" + "\u0001";
      if (Options.getErrorReporting()) {
        retval += "\njj_la1[" + maskindex + "] = jj_gen;";
        maskVals.add(tokenMask);
        maskindex++;
      }
      retval += actions[index];
    }
    for (int i = 0; i < indentAmt; i++) {
      retval += "\u0002\n}";
    }

    return retval;

  }

  void dumpFormattedString(String str) {
    char ch = ' ';
    char prevChar;
    boolean indentOn = true;
    for (int i = 0; i < str.length(); i++) {
      prevChar = ch;
      ch = str.charAt(i);
      if (ch == '\n' && prevChar == '\r') {
        // do nothing - we've already printed a new line for the '\r'
        // during the previous iteration.
      } else if (ch == '\n' || ch == '\r') {
        if (indentOn) {
          phase1NewLine();
        } else {
          codeGenerator.genCodeLine("");
        }
      } else if (ch == '\u0001') {
        indentamt += 2;
      } else if (ch == '\u0002') {
        indentamt -= 2;
      } else if (ch == '\u0003') {
        indentOn = false;
      } else if (ch == '\u0004') {
        indentOn = true;
      } else {
        codeGenerator.genCode(ch);
      }
    }
  }

  // Print method header and return the ERROR_RETURN string.
  private String generateCPPMethodheader(BNFProduction p, Token t) {
    StringBuffer sig = new StringBuffer();
    String ret, params;

    String method_name = p.getLhs();
    boolean void_ret = false;
    boolean ptr_ret = false;

    codeGenerator.printTokenSetup(t); ccol = 1;
    String comment1 = codeGenerator.getLeadingComments(t);
    cline = t.beginLine;
    ccol = t.beginColumn;
    sig.append(t.image);
    if (t.kind == JavaCCParserConstants.VOID) void_ret = true;
    if (t.kind == JavaCCParserConstants.STAR) ptr_ret = true;

    for (int i = 1; i < p.getReturnTypeTokens().size(); i++) {
      t = p.getReturnTypeTokens().get(i);
      sig.append(CodeGenHelper.getStringToPrint(t));
      if (t.kind == JavaCCParserConstants.VOID) void_ret = true;
      if (t.kind == JavaCCParserConstants.STAR) ptr_ret = true;
    }

    String comment2 = codeGenerator.getTrailingComments(t);
    ret = sig.toString();

    sig.setLength(0);
    sig.append("(");
    if (p.getParameterListTokens().size() != 0) {
      codeGenerator.printTokenSetup(p.getParameterListTokens().get(0));
      for (Iterator<Token> it = p.getParameterListTokens().iterator(); it.hasNext();) {
        t = it.next();
        sig.append(CodeGenHelper.getStringToPrint(t));
      }
      sig.append(codeGenerator.getTrailingComments(t));
    }
    sig.append(")");
    params = sig.toString();

    // For now, just ignore comments
    codeGenerator.generateMethodDefHeader(ret, cu_name, p.getLhs()+params, sig.toString());

    // Generate a default value for error return.
    String default_return;
    if (ptr_ret) default_return = "NULL";
    else if (void_ret) default_return = "";
    else default_return = "0";  // 0 converts to most (all?) basic types.

    StringBuffer ret_val =
        new StringBuffer("\n#if !defined ERROR_RET_" + method_name + "\n");
    ret_val.append("#define ERROR_RET_" + method_name + " " +
                   default_return + "\n");
    ret_val.append("#endif\n");
    ret_val.append("#define __ERROR_RET__ ERROR_RET_" + method_name + "\n");

    return ret_val.toString();
  }


  void genStackCheck(boolean voidReturn) {
    if (Options.getDepthLimit() > 0) {
      if (!voidReturn) {
        codeGenerator.genCodeLine("if(jj_depth_error){ return __ERROR_RET__; }");
      } else {
        codeGenerator.genCodeLine("if(jj_depth_error){ return; }");
      }
      codeGenerator.genCodeLine("__jj_depth_inc __jj_depth_counter(this);");
      codeGenerator.genCodeLine("if(jj_depth > " + Options.getDepthLimit() + ") {");
      codeGenerator.genCodeLine("  jj_depth_error = true;");
      codeGenerator.genCodeLine("  jj_consume_token(-1);");
      codeGenerator.genCodeLine("  errorHandler->handleParseError(token, getToken(1), __FUNCTION__, this), hasError = true;");
      if (!voidReturn) {
        codeGenerator.genCodeLine("  return __ERROR_RET__;");  // Non-recoverable error
      } else {
        codeGenerator.genCodeLine("  return;");  // Non-recoverable error
      }
      codeGenerator.genCodeLine("}");
    }
  }

  void buildPhase1Routine(BNFProduction p) {
    Token t = p.getReturnTypeTokens().get(0);
    boolean voidReturn = false;
    if (t.kind == JavaCCParserConstants.VOID) {
      voidReturn = true;
    }
    String error_ret = null;
    error_ret = generateCPPMethodheader(p, t);

    codeGenerator.genCode(" {");

    if (Options.booleanValue(Options.USEROPTION__CPP_STOP_ON_FIRST_ERROR) && error_ret != null
        || Options.getDepthLimit() > 0 && !voidReturn) {
        codeGenerator.genCode(error_ret);
    } else {
      error_ret = null;
    }

    genStackCheck(voidReturn);

    indentamt = 4;
    if (Options.getDebugParser()) {
        codeGenerator.genCodeLine("");
        codeGenerator.genCodeLine("    JJEnter<std::function<void()>> jjenter([this]() {trace_call  (\"" + Types.addUnicodeEscapes(p.getLhs()) +"\"); });");
        codeGenerator.genCodeLine("    JJExit <std::function<void()>> jjexit ([this]() {trace_return(\"" + Types.addUnicodeEscapes(p.getLhs()) +"\"); });");
        codeGenerator.genCodeLine("    try {");
        indentamt = 6;
      }
    
    if (!Options.booleanValue(Options.USEROPTION__IGNORE_ACTIONS) &&
        p.getDeclarationTokens().size() != 0) {
      codeGenerator.printTokenSetup(p.getDeclarationTokens().get(0)); cline--;
      for (Iterator<Token> it = p.getDeclarationTokens().iterator(); it.hasNext();) {
        t = it.next();
        codeGenerator.printToken(t);
      }
      codeGenerator.printTrailingComments(t);
    }
    
    String code = phase1ExpansionGen(p.getExpansion());
    dumpFormattedString(code);
    codeGenerator.genCodeLine("");
    
    if (p.isJumpPatched() && !voidReturn) {
      codeGenerator.genCodeLine("    throw \"Missing return statement in function\";");
    }
    if (Options.getDebugParser()) {
      codeGenerator.genCodeLine("    } catch(...) { }");
    }
    codeGenerator.genCodeLine("assert(false);");

    if (error_ret != null) {
      codeGenerator.genCodeLine("\n#undef __ERROR_RET__\n");
    }
    codeGenerator.genCodeLine("}");
    codeGenerator.genCodeLine("");
  }

  void phase1NewLine() {
    codeGenerator.genCodeLine("");
    for (int i = 0; i < indentamt; i++) {
      codeGenerator.genCode(" ");
    }
  }

  String phase1ExpansionGen(Expansion e) {
    String retval = "";
    Token t = null;
    Lookahead[] conds;
    String[] actions;
    if (e instanceof RegularExpression) {
      RegularExpression e_nrw = (RegularExpression)e;
      retval += "\n";
      if (e_nrw.lhsTokens.size() != 0) {
        codeGenerator.printTokenSetup(e_nrw.lhsTokens.get(0));
        for (Iterator<Token> it = e_nrw.lhsTokens.iterator(); it.hasNext();) {
          t = it.next();
          retval += CodeGenHelper.getStringToPrint(t);
        }
        retval += codeGenerator.getTrailingComments(t);
        retval += " = ";
      }
      String tail = e_nrw.rhsToken == null ? ");" :
                ")->" + e_nrw.rhsToken.image + ";";
      if (e_nrw.label.equals("")) {
        Object label = names_of_tokens.get(Integer.valueOf(e_nrw.ordinal));
        if (label != null) {
          retval += "jj_consume_token(" + (String)label + tail;
        } else {
          retval += "jj_consume_token(" + e_nrw.ordinal + tail;
        }
      } else {
        retval += "jj_consume_token(" + e_nrw.label + tail;
      }

      if (Options.booleanValue(Options.USEROPTION__CPP_STOP_ON_FIRST_ERROR)) {
            retval += "\n    { if (hasError) { return __ERROR_RET__; } }\n";
      }

    } else if (e instanceof NonTerminal) {
      NonTerminal e_nrw = (NonTerminal)e;
      retval += "\n";
      if (e_nrw.getLhsTokens().size() != 0) {
        codeGenerator.printTokenSetup(e_nrw.getLhsTokens().get(0));
        for (Iterator<Token> it = e_nrw.getLhsTokens().iterator(); it.hasNext();) {
          t = it.next();
          retval += CodeGenHelper.getStringToPrint(t);
        }
        retval += codeGenerator.getTrailingComments(t);
        retval += " = ";
      }
      retval += e_nrw.getName() + "(";
      if (e_nrw.getArgumentTokens().size() != 0) {
        codeGenerator.printTokenSetup(e_nrw.getArgumentTokens().get(0));
        for (Iterator<Token> it = e_nrw.getArgumentTokens().iterator(); it.hasNext();) {
          t = it.next();
          retval += CodeGenHelper.getStringToPrint(t);
        }
        retval += codeGenerator.getTrailingComments(t);
      }
      retval += ");";
      if (Options.booleanValue(Options.USEROPTION__CPP_STOP_ON_FIRST_ERROR)) {
            retval += "\n    { if (hasError) { return __ERROR_RET__; } }\n";
      }
    } else if (e instanceof Action) {
      Action e_nrw = (Action)e;
      retval += "\u0003\n";
      if (!Options.booleanValue(Options.USEROPTION__IGNORE_ACTIONS) &&
          e_nrw.getActionTokens().size() != 0) {
        codeGenerator.printTokenSetup(e_nrw.getActionTokens().get(0)); ccol = 1;
        for (Iterator<Token> it = e_nrw.getActionTokens().iterator(); it.hasNext();) {
          t = it.next();
          retval += CodeGenHelper.getStringToPrint(t);
        }
        retval += codeGenerator.getTrailingComments(t);
      }
      retval += "\u0004";
    } else if (e instanceof Choice) {
      Choice e_nrw = (Choice)e;
      conds = new Lookahead[e_nrw.getChoices().size()];
      actions = new String[e_nrw.getChoices().size() + 1];
      actions[e_nrw.getChoices().size()] = "\n" + "jj_consume_token(-1);\n" +
              "errorHandler->handleParseError(token, getToken(1), __FUNCTION__, this), hasError = true;" +
             (Options.booleanValue(Options.USEROPTION__CPP_STOP_ON_FIRST_ERROR) ? "return __ERROR_RET__;\n" : "");

      // In previous line, the "throw" never throws an exception since the
      // evaluation of jj_consume_token(-1) causes ParseException to be
      // thrown first.
      Sequence nestedSeq;
      for (int i = 0; i < e_nrw.getChoices().size(); i++) {
        nestedSeq = (Sequence)e_nrw.getChoices().get(i);
        actions[i] = phase1ExpansionGen(nestedSeq);
        conds[i] = (Lookahead)nestedSeq.units.get(0);
      }
      retval = buildLookaheadChecker(conds, actions);
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      for (int i = 1; i < e_nrw.units.size(); i++) {
        // For C++, since we are not using exceptions, we will protect all the
        // expansion choices with if (!error)
        boolean wrap_in_block = false;
        if (!JavaCCGlobals.jjtreeGenerated) {
          // for the last one, if it's an action, we will not protect it.
          Expansion elem = e_nrw.units.get(i);
          if (!(elem instanceof Action) ||
              !(e.parent instanceof BNFProduction) ||
              i != e_nrw.units.size() - 1) {
            wrap_in_block = true;
            retval += "\nif (!hasError) {";
          }
        }
        retval += phase1ExpansionGen(e_nrw.units.get(i));
        if (wrap_in_block) {
          retval += "\n}";
        }
      }
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead)((Sequence)nested_e).units.get(0);
      } else {
        la = new Lookahead();
        la.setAmount(Options.getLookahead());
        la.setLaExpansion(nested_e);
      }
      retval += "\n";
      int labelIndex = ++gensymindex;
      retval += "while (!hasError) {\u0001";
      retval += phase1ExpansionGen(nested_e);
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = "\n;";
      actions[1] = "\ngoto end_label_" + labelIndex + ";";

      retval += buildLookaheadChecker(conds, actions);
      retval += "\u0002\n" + "}";
      retval += "\nend_label_" + labelIndex + ": ;";
    } else if (e instanceof ZeroOrMore) {
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead)((Sequence)nested_e).units.get(0);
      } else {
        la = new Lookahead();
        la.setAmount(Options.getLookahead());
        la.setLaExpansion(nested_e);
      }
      retval += "\n";
      int labelIndex = ++gensymindex;
      retval += "while (!hasError) {\u0001";
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = "\n;";
      actions[1] = "\ngoto end_label_" + labelIndex + ";";
      retval += buildLookaheadChecker(conds, actions);
      retval += phase1ExpansionGen(nested_e);
      retval += "\u0002\n" + "}";
      retval += "\nend_label_" + labelIndex + ": ;";
    } else if (e instanceof ZeroOrOne) {
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      Expansion nested_e = e_nrw.expansion;
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead)((Sequence)nested_e).units.get(0);
      } else {
        la = new Lookahead();
        la.setAmount(Options.getLookahead());
        la.setLaExpansion(nested_e);
      }
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = phase1ExpansionGen(nested_e);
      actions[1] = "\n;";
      retval += buildLookaheadChecker(conds, actions);
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      Expansion nested_e = e_nrw.exp;
      List<Token> list;
      retval += "\n";
      retval += "try {\u0001";
      retval += phase1ExpansionGen(nested_e);
      retval += "\u0002\n" + "}";
      for (int i = 0; i < e_nrw.catchblks.size(); i++) {
        retval += " catch (";
        list = e_nrw.catchblks.get(i);
        if (list.size() != 0) {
          codeGenerator.printTokenSetup(list.get(0)); ccol = 1;
          for (Iterator<Token> it = list.iterator(); it.hasNext();) {
            t = it.next();
            retval += CodeGenHelper.getStringToPrint(t);
          }
          retval += codeGenerator.getTrailingComments(t);
        }
        retval += "\u0004\n" + "}";
      }
      if (e_nrw.finallyblk != null) {
        retval += " finally {\u0003\n";

        if (e_nrw.finallyblk.size() != 0) {
          codeGenerator.printTokenSetup(e_nrw.finallyblk.get(0)); ccol = 1;
          for (Iterator<Token> it = e_nrw.finallyblk.iterator(); it.hasNext();) {
            t = it.next();
            retval += CodeGenHelper.getStringToPrint(t);
          }
          retval += codeGenerator.getTrailingComments(t);
        }
        retval += "\u0004\n" + "}";
      }
    }
    return retval;
  }

  void buildPhase2Routine(Lookahead la) {
    Expansion e = la.getLaExpansion();
    codeGenerator.genCodeLine(" inline bool ", "jj_2" + internalNames.get(e) + "(int xla)");
    codeGenerator.genCodeLine(" {");
    codeGenerator.genCodeLine("    jj_la = xla; jj_lastpos = jj_scanpos = token;");

    String ret_suffix = "";
    if (Options.getDepthLimit() > 0) {
      ret_suffix = " && !jj_depth_error";
    }

    codeGenerator.genCodeLine("    jj_done = false;");
    codeGenerator.genCodeLine("    return (!jj_3" + internalNames.get(e) + "() || jj_done)" + ret_suffix + ";");
    if (Options.getErrorReporting()) {
      codeGenerator.genCodeLine(" { jj_save(" + (Integer.parseInt(internalNames.get(e).substring(1))-1) + ", xla); }");
    }
    codeGenerator.genCodeLine("  }");
    codeGenerator.genCodeLine("");
    Phase3Data p3d = new Phase3Data(e, la.getAmount());
    phase3list.add(p3d);
    phase3table.put(e, p3d);
  }

  private boolean xsp_declared;

  Expansion jj3_expansion;

  String genReturn(boolean value) {
    String retval = value ? "true" : "false";
    if (Options.getDebugLookahead() && jj3_expansion != null) {
      String tracecode = "trace_return(\"" + Types.addUnicodeEscapes(((NormalProduction)jj3_expansion.parent).getLhs()) +
      "(LOOKAHEAD " + (value ? "FAILED" : "SUCCEEDED") + ")\");";
      if (Options.getErrorReporting()) {
        tracecode = "if (!jj_rescan) " + tracecode;
      }
      return "{ " + tracecode + " return " + retval + "; }";
    } else {
      return "return " + retval + ";";
    }
  }

  private void generate3R(Expansion e, Phase3Data inf)
  {
    Expansion seq = e;
    if (!internalNames.containsKey(e) || internalNames.get(e).equals(""))
    {
      while (true)
      {
        if (seq instanceof Sequence && ((Sequence)seq).units.size() == 2)
        {
          seq = ((Sequence)seq).units.get(1);
        }
        else if (seq instanceof NonTerminal)
        {
          NonTerminal e_nrw = (NonTerminal)seq;
          NormalProduction ntprod = production_table.get(e_nrw.getName());
          if (ntprod instanceof CodeProduction)
          {
            break; // nothing to do here
          }
          else
          {
            seq = ntprod.getExpansion();
          }
        }
        else
          break;
      }

      if (seq instanceof RegularExpression)
      {
        internalNames.put(e, "jj_scan_token(" + ((RegularExpression)seq).ordinal + ")");
        return;
      }

      gensymindex++;
//    if (gensymindex == 100)
//    {
//    new Error().codeGenerator.printStackTrace();
//    System.out.println(" ***** seq: " + seq.internal_name + "; size: " + ((Sequence)seq).units.size());
//    }
      internalNames.put(e, "R_" + gensymindex);
      internalIndexes.put(e, gensymindex);
    }
    Phase3Data p3d = phase3table.get(e);
    if (p3d == null || p3d.count < inf.count) {
      p3d = new Phase3Data(e, inf.count);
      phase3list.add(p3d);
      phase3table.put(e, p3d);
    }
  }

  void setupPhase3Builds(Phase3Data inf) {
    Expansion e = inf.exp;
    if (e instanceof RegularExpression) {
      ; // nothing to here
    } else if (e instanceof NonTerminal) {
      // All expansions of non-terminals have the "name" fields set.  So
      // there's no need to check it below for "e_nrw" and "ntexp".  In
      // fact, we rely here on the fact that the "name" fields of both these
      // variables are the same.
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod = production_table.get(e_nrw.getName());
      if (ntprod instanceof CodeProduction) {
        ; // nothing to do here
      } else {
        generate3R(ntprod.getExpansion(), inf);
      }
    } else if (e instanceof Choice) {
      Choice e_nrw = (Choice)e;
      for (int i = 0; i < e_nrw.getChoices().size(); i++) {
        generate3R(e_nrw.getChoices().get(i), inf);
      }
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      int cnt = inf.count;
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = e_nrw.units.get(i);
        setupPhase3Builds(new Phase3Data(eseq, cnt));
        cnt -= minimumSize(eseq);
        if (cnt <= 0) break;
      }
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      setupPhase3Builds(new Phase3Data(e_nrw.exp, inf.count));
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      generate3R(e_nrw.expansion, inf);
    } else if (e instanceof ZeroOrMore) {
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      generate3R(e_nrw.expansion, inf);
    } else if (e instanceof ZeroOrOne) {
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      generate3R(e_nrw.expansion, inf);
    }
  }

  private String getTypeForToken() {
    return "Token *";
  }

  private String genjj_3Call(Expansion e)
  {
    if (internalNames.containsKey(e) && internalNames.get(e).startsWith("jj_scan_token"))
      return internalNames.get(e);
    else
      return "jj_3" + internalNames.get(e) + "()";
  }

  Hashtable<String, Phase3Data> generated = new Hashtable<>();
  void buildPhase3Routine(Phase3Data inf, boolean recursive_call) {
    Expansion e = inf.exp;
    Token t = null;
    if (internalNames.containsKey(e) && internalNames.get(e).startsWith("jj_scan_token"))
      return;

    if (!recursive_call) {
      codeGenerator.genCodeLine(" inline bool ", "jj_3" + internalNames.get(e) + "()");

      codeGenerator.genCodeLine(" {");
      codeGenerator.genCodeLine("    if (jj_done) return true;");
      if (Options.getDepthLimit() > 0) {
        codeGenerator.genCodeLine("#define __ERROR_RET__ true");
      }
      genStackCheck(false);
      xsp_declared = false;
      if (Options.getDebugLookahead() && e.parent instanceof NormalProduction) {
        codeGenerator.genCode("    ");
        if (Options.getErrorReporting()) {
          codeGenerator.genCode("if (!jj_rescan) ");
        }
        codeGenerator.genCodeLine("trace_call(\"" + Types.addUnicodeEscapes(((NormalProduction)e.parent).getLhs()) + "(LOOKING AHEAD...)\");");
        jj3_expansion = e;
      } else {
        jj3_expansion = null;
      }
    }
    if (e instanceof RegularExpression) {
      RegularExpression e_nrw = (RegularExpression)e;
      if (e_nrw.label.equals("")) {
        Object label = names_of_tokens.get(Integer.valueOf(e_nrw.ordinal));
        if (label != null) {
          codeGenerator.genCodeLine("    if (jj_scan_token(" + (String)label + ")) " + genReturn(true));
        } else {
          codeGenerator.genCodeLine("    if (jj_scan_token(" + e_nrw.ordinal + ")) " + genReturn(true));
        }
      } else {
        codeGenerator.genCodeLine("    if (jj_scan_token(" + e_nrw.label + ")) " + genReturn(true));
      }
      //codeGenerator.genCodeLine("    if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
    } else if (e instanceof NonTerminal) {
      // All expansions of non-terminals have the "name" fields set.  So
      // there's no need to check it below for "e_nrw" and "ntexp".  In
      // fact, we rely here on the fact that the "name" fields of both these
      // variables are the same.
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod = production_table.get(e_nrw.getName());
      if (ntprod instanceof CodeProduction) {
        codeGenerator.genCodeLine("    if (true) { jj_la = 0; jj_scanpos = jj_lastpos; " + genReturn(false) + "}");
      } else {
        Expansion ntexp = ntprod.getExpansion();
        //codeGenerator.genCodeLine("    if (jj_3" + ntexp.internal_name + "()) " + genReturn(true));
        codeGenerator.genCodeLine("    if (" + genjj_3Call(ntexp)+ ") " + genReturn(true));
        //codeGenerator.genCodeLine("    if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
      }
    } else if (e instanceof Choice) {
      Sequence nested_seq;
      Choice e_nrw = (Choice)e;
      if (e_nrw.getChoices().size() != 1) {
        if (!xsp_declared) {
          xsp_declared = true;
          codeGenerator.genCodeLine("    " + getTypeForToken() + " xsp;");
        }
        codeGenerator.genCodeLine("    xsp = jj_scanpos;");
      }
      for (int i = 0; i < e_nrw.getChoices().size(); i++) {
        nested_seq = (Sequence)e_nrw.getChoices().get(i);
        Lookahead la = (Lookahead)nested_seq.units.get(0);
        if (la.getActionTokens().size() != 0) {
          // We have semantic lookahead that must be evaluated.
          lookaheadNeeded = true;
          codeGenerator.genCodeLine("    jj_lookingAhead = true;");
          codeGenerator.genCode("    jj_semLA = ");
          codeGenerator.printTokenSetup(la.getActionTokens().get(0));
          for (Iterator <Token> it = la.getActionTokens().iterator(); it.hasNext();) {
            t = it.next();
            codeGenerator.printToken(t);
          }
          codeGenerator.printTrailingComments(t);
          codeGenerator.genCodeLine(";");
          codeGenerator.genCodeLine("    jj_lookingAhead = false;");
        }
        codeGenerator.genCode("    if (");
        if (la.getActionTokens().size() != 0) {
          codeGenerator.genCode("!jj_semLA || ");
        }
        if (i != e_nrw.getChoices().size() - 1) {
          //codeGenerator.genCodeLine("jj_3" + nested_seq.internal_name + "()) {");
          codeGenerator.genCodeLine(genjj_3Call(nested_seq) + ") {");
          codeGenerator.genCodeLine("    jj_scanpos = xsp;");
        } else {
          //codeGenerator.genCodeLine("jj_3" + nested_seq.internal_name + "()) " + genReturn(true));
          codeGenerator.genCodeLine(genjj_3Call(nested_seq) + ") " + genReturn(true));
          //codeGenerator.genCodeLine("    if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
        }
      }
      for (int i = 1; i < e_nrw.getChoices().size(); i++) {
        //codeGenerator.genCodeLine("    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
        codeGenerator.genCodeLine("    }");
      }
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      int cnt = inf.count;
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = e_nrw.units.get(i);
        buildPhase3Routine(new Phase3Data(eseq, cnt), true);

//      System.out.println("minimumSize: line: " + eseq.line + ", column: " + eseq.column + ": " +
//      minimumSize(eseq));//Test Code

        cnt -= minimumSize(eseq);
        if (cnt <= 0) break;
      }
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      buildPhase3Routine(new Phase3Data(e_nrw.exp, inf.count), true);
    } else if (e instanceof OneOrMore) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.genCodeLine("    " + getTypeForToken() + " xsp;");
      }
      OneOrMore e_nrw = (OneOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      //codeGenerator.genCodeLine("    if (jj_3" + nested_internalNames.get(e) + "()) " + genReturn(true));
      codeGenerator.genCodeLine("    if (" + genjj_3Call(nested_e) + ") " + genReturn(true));
      //codeGenerator.genCodeLine("    if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
      codeGenerator.genCodeLine("    while (true) {");
      codeGenerator.genCodeLine("      xsp = jj_scanpos;");
      //codeGenerator.genCodeLine("      if (jj_3" + nested_internalNames.get(e) + "()) { jj_scanpos = xsp; break; }");
      codeGenerator.genCodeLine("      if (" + genjj_3Call(nested_e) + ") { jj_scanpos = xsp; break; }");
      //codeGenerator.genCodeLine("      if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
      codeGenerator.genCodeLine("    }");
    } else if (e instanceof ZeroOrMore) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.genCodeLine("    " + getTypeForToken() + " xsp;");
      }
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      codeGenerator.genCodeLine("    while (true) {");
      codeGenerator.genCodeLine("      xsp = jj_scanpos;");
      //codeGenerator.genCodeLine("      if (jj_3" + nested_internalNames.get(e) + "()) { jj_scanpos = xsp; break; }");
      codeGenerator.genCodeLine("      if (" + genjj_3Call(nested_e) + ") { jj_scanpos = xsp; break; }");
      //codeGenerator.genCodeLine("      if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
      codeGenerator.genCodeLine("    }");
    } else if (e instanceof ZeroOrOne) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.genCodeLine("    " + getTypeForToken() + " xsp;");
      }
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      Expansion nested_e = e_nrw.expansion;
      codeGenerator.genCodeLine("    xsp = jj_scanpos;");
      //codeGenerator.genCodeLine("    if (jj_3" + nested_internalNames.get(e) + "()) jj_scanpos = xsp;");
      codeGenerator.genCodeLine("    if (" + genjj_3Call(nested_e) + ") jj_scanpos = xsp;");
      //codeGenerator.genCodeLine("    else if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
    }
    if (!recursive_call) {
      codeGenerator.genCodeLine("    " + genReturn(false));
      if (Options.getDepthLimit() > 0) {
        codeGenerator.genCodeLine("#undef __ERROR_RET__");
      }
      codeGenerator.genCodeLine("  }");
      codeGenerator.genCodeLine("");
    }
  }

  int minimumSize(Expansion e) {
    return minimumSize(e, Integer.MAX_VALUE);
  }

  /*
   * Returns the minimum number of tokens that can parse to this expansion.
   */
  int minimumSize(Expansion e, int oldMin) {
    int retval = 0;  // should never be used.  Will be bad if it is.
    if (e.inMinimumSize) {
      // recursive search for minimum size unnecessary.
      return Integer.MAX_VALUE;
    }
    e.inMinimumSize = true;
    if (e instanceof RegularExpression) {
      retval = 1;
    } else if (e instanceof NonTerminal) {
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod = production_table.get(e_nrw.getName());
      if (ntprod instanceof CodeProduction) {
        retval = Integer.MAX_VALUE;
        // Make caller think this is unending (for we do not go beyond JAVACODE during
        // phase3 execution).
      } else {
        Expansion ntexp = ntprod.getExpansion();
        retval = minimumSize(ntexp);
      }
    } else if (e instanceof Choice) {
      int min = oldMin;
      Expansion nested_e;
      Choice e_nrw = (Choice)e;
      for (int i = 0; min > 1 && i < e_nrw.getChoices().size(); i++) {
        nested_e = e_nrw.getChoices().get(i);
        int min1 = minimumSize(nested_e, min);
        if (min > min1) min = min1;
      }
      retval = min;
    } else if (e instanceof Sequence) {
      int min = 0;
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = e_nrw.units.get(i);
        int mineseq = minimumSize(eseq);
        if (min == Integer.MAX_VALUE || mineseq == Integer.MAX_VALUE) {
          min = Integer.MAX_VALUE; // Adding infinity to something results in infinity.
        } else {
          min += mineseq;
          if (min > oldMin)
            break;
        }
      }
      retval = min;
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      retval = minimumSize(e_nrw.exp);
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      retval = minimumSize(e_nrw.expansion);
    } else if (e instanceof ZeroOrMore) {
      retval = 0;
    } else if (e instanceof ZeroOrOne) {
      retval = 0;
    } else if (e instanceof Lookahead) {
      retval = 0;
    } else if (e instanceof Action) {
      retval = 0;
    }
    e.inMinimumSize = false;
    return retval;
  }

  public void build(CodeGenHelper codeGenerator) {
    NormalProduction p;
    JavaCodeProduction jp;
    CppCodeProduction cp;

    this.codeGenerator = codeGenerator;
    for (Iterator<NormalProduction> prodIterator = bnfproductions.iterator(); prodIterator.hasNext();) {
      p = prodIterator.next();
      if (p instanceof CppCodeProduction) {
          cp = (CppCodeProduction)p;

          StringBuffer sig = new StringBuffer();
          String ret, params;
          Token t = null;
          
          String method_name = p.getLhs();
          boolean void_ret = false;
          boolean ptr_ret = false;

          for (int i = 0; i < p.getReturnTypeTokens().size(); i++) {
            t = p.getReturnTypeTokens().get(i);
            String s = CodeGenHelper.getStringToPrint(t);
            sig.append(t.toString());
            sig.append(" ");
            if (t.kind == JavaCCParserConstants.VOID) void_ret = true;
            if (t.kind == JavaCCParserConstants.STAR) ptr_ret = true;
          }

          String comment2 = "";
          if (t != null)
            comment2 = codeGenerator.getTrailingComments(t);
          ret = sig.toString();

          sig.setLength(0);
          sig.append("(");
          if (p.getParameterListTokens().size() != 0) {
            codeGenerator.printTokenSetup(p.getParameterListTokens().get(0));
            for (Iterator<Token> it = p.getParameterListTokens().iterator(); it.hasNext();) {
              t = it.next();
              sig.append(CodeGenHelper.getStringToPrint(t));
            }
            sig.append(codeGenerator.getTrailingComments(t));
          }
          sig.append(")");
          params = sig.toString();

          // For now, just ignore comments
          codeGenerator.generateMethodDefHeader(ret, cu_name, p.getLhs()+params, sig.toString());
          codeGenerator.genCodeLine(" {");
          if (Options.getDebugParser()) {
            codeGenerator.genCodeLine("");
            codeGenerator.genCodeLine("    JJEnter<std::function<void()>> jjenter([this]() {trace_call  (\"" + Types.addUnicodeEscapes(cp.getLhs()) +"\"); });");
            codeGenerator.genCodeLine("    JJExit <std::function<void()>> jjexit ([this]() {trace_return(\"" + Types.addUnicodeEscapes(cp.getLhs()) +"\"); });");
            codeGenerator.genCodeLine("    try {");
          }
          if (cp.getCodeTokens().size() != 0) {
            codeGenerator.printTokenSetup(cp.getCodeTokens().get(0)); cline--;
            codeGenerator.printTokenList(cp.getCodeTokens());
          }
          codeGenerator.genCodeLine("");
          if (Options.getDebugParser()) {
            codeGenerator.genCodeLine("    } catch(...) { }");
          }
          codeGenerator.genCodeLine("  }");
          codeGenerator.genCodeLine("");    
      } else
      if (p instanceof JavaCodeProduction) {
        JavaCCErrors.semantic_error("Cannot use JAVACODE productions with C++ output (yet).");
        continue;
      } else {
        buildPhase1Routine((BNFProduction)p);
      }
    }

    codeGenerator.switchToIncludeFile();
    for (int phase2index = 0; phase2index < phase2list.size(); phase2index++) {
      buildPhase2Routine(phase2list.get(phase2index));
    }

    int phase3index = 0;

    while (phase3index < phase3list.size()) {
      for (; phase3index < phase3list.size(); phase3index++) {
        setupPhase3Builds(phase3list.get(phase3index));
      }
    }

    for (Enumeration<Phase3Data> enumeration = phase3table.elements(); enumeration.hasMoreElements();) {
      buildPhase3Routine(enumeration.nextElement(), false);
    }
    codeGenerator.switchToMainFile();
  }

  public void reInit()
  {
    gensymindex = 0;
    indentamt = 0;
    jj2LA = false;
    phase2list = new ArrayList<>();
    phase3list = new ArrayList<>();
    phase3table = new Hashtable<>();
    firstSet = null;
    xsp_declared = false;
    jj3_expansion = null;
  }

  // Table driven.
  void buildPhase3Table(Phase3Data inf) {
    Expansion e = inf.exp;
//    Token t = null;
    if (e instanceof RegularExpression) {
      RegularExpression e_nrw = (RegularExpression)e;
      System.err.println("TOKEN, " + e_nrw.ordinal);
    } else if (e instanceof NonTerminal) {
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod =
          production_table.get(e_nrw.getName());
      if (ntprod instanceof CodeProduction) {
        // javacode, true - always (warn?)
        System.err.println("JAVACODE_PROD, true");
      } else {
        Expansion ntexp = ntprod.getExpansion();
        // nt exp's table.
        System.err.println("PRODUCTION, " + internalIndexes.get(ntexp));
        //buildPhase3Table(new Phase3Data(ntexp, inf.count));
      }
    } else if (e instanceof Choice) {
      Sequence nested_seq;
      Choice e_nrw = (Choice)e;
      System.err.print("CHOICE, ");
      for (int i = 0; i < e_nrw.getChoices().size(); i++) {
        if (i > 0) System.err.print("\n|");
        nested_seq = (Sequence)e_nrw.getChoices().get(i);
        Lookahead la = (Lookahead)nested_seq.units.get(0);
        if (la.getActionTokens().size() != 0) {
          System.err.print("SEMANTIC,");
        } else {
          buildPhase3Table(new Phase3Data(nested_seq, inf.count));
        }
      }
      System.err.println();
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      int cnt = inf.count;
      if (e_nrw.units.size() > 2) {
        System.err.println("SEQ, " + cnt);
        for (int i = 1; i < e_nrw.units.size(); i++) {
          System.err.print("   ");
          Expansion eseq = e_nrw.units.get(i);
          buildPhase3Table(new Phase3Data(eseq, cnt));
          cnt -= minimumSize(eseq);
          if (cnt <= 0) break;
        }
      } else {
        Expansion tmp = e_nrw.units.get(1);
        while (tmp instanceof NonTerminal) {
          NormalProduction ntprod =
              production_table.get(((NonTerminal)tmp).getName());
          if (ntprod instanceof CodeProduction) break;
          tmp = ntprod.getExpansion();
        }
        buildPhase3Table(new Phase3Data(tmp, cnt));
      }
      System.err.println();
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      buildPhase3Table(new Phase3Data(e_nrw.exp, inf.count));
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      System.err.println("SEQ PROD " + internalIndexes.get(e_nrw));
      System.err.println("ZEROORMORE " + internalIndexes.get(e_nrw));
    } else if (e instanceof ZeroOrMore) {
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      System.err.print("ZEROORMORE, " + internalIndexes.get(e_nrw));
    } else if (e instanceof ZeroOrOne) {
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      System.err.println("ZERORONE, " + internalIndexes.get(e_nrw));
    } else {
      assert false;
      // table for nested_e - optional
    }
  }
}

/**
 * This class stores information to pass from phase 2 to phase 3.
 */
class Phase3Data {

  /*
   * This is the expansion to generate the jj3 method for.
   */
  Expansion exp;

  /*
   * This is the number of tokens that can still be consumed.  This
   * number is used to limit the number of jj3 methods generated.
   */
  int count;

  Phase3Data(Expansion e, int c) {
    exp = e;
    count = c;
  }
}
