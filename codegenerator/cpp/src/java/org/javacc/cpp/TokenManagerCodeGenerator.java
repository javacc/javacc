
package org.javacc.cpp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.JavaCCErrors;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.JavaCCParserConstants;
import org.javacc.parser.JavaFiles;
import org.javacc.parser.LexGen;
import org.javacc.parser.NfaState;
import org.javacc.parser.Action;
import org.javacc.parser.CodeGenHelper;
import org.javacc.parser.Options;
import org.javacc.parser.RStringLiteral;
import org.javacc.parser.Token;
import org.javacc.parser.TokenizerData;
import org.javacc.utils.OutputFileGenerator;

import static org.javacc.parser.JavaCCGlobals.*;

/**
 * Class that implements a table driven code generator for the token manager in java.
 */
public class TokenManagerCodeGenerator implements org.javacc.parser.TokenManagerCodeGenerator {

  private final CodeGenHelper codeGenerator   = new CodeGenHelper();

  @Override
  public void generateCode(CodeGeneratorSettings settings, TokenizerData tokenizerData) {
    settings.put("maxOrdinal", tokenizerData.allMatches.size());
    settings.put("maxLexStates", tokenizerData.lexStateNames.length);
    settings.put("hasEmptyMatch", Boolean.valueOf(LexGen.hasEmptyMatch));
    settings.put("hasSkip", Boolean.valueOf(LexGen.hasSkip));
    settings.put("hasMore", Boolean.valueOf(LexGen.hasMore));
    settings.put("hasSpecial", Boolean.valueOf(LexGen.hasSpecial));
    settings.put("hasMoreActions", Boolean.valueOf(LexGen.hasMoreActions));
    settings.put("hasSkipActions", Boolean.valueOf(LexGen.hasSkipActions));
    settings.put("hasTokenActions", Boolean.valueOf(LexGen.hasTokenActions));
    settings.put("stateSetSize", LexGen.stateSetSize);
    settings.put("hasActions", LexGen.hasMoreActions || LexGen.hasSkipActions || LexGen.hasTokenActions);
    settings.put("tokMgrClassName", LexGen.tokMgrClassName);
    settings.put("maxLongs", tokenizerData.allMatches.size()/64 + 1);
    settings.put("cu_name", tokenizerData.parserName);

   
    PrintClassHead();

    try {
      RStringLiteral.DumpDfaCode(codeGenerator);
      if (LexGen.hasNfa[LexGen.lexStateIndex]) {
        NfaState.DumpMoveNfa(codeGenerator);
      }
      NfaState.DumpStateSets(codeGenerator);

      NfaState.DumpNonAsciiMoveMethods(codeGenerator);
      RStringLiteral.DumpStrLiteralImages(codeGenerator);
      DumpFillToken();
      DumpGetNextToken();


      if (Options.getDebugTokenManager()) {
        NfaState.DumpStatesForKind(codeGenerator);
        DumpDebugMethods(settings);
      }

      if (LexGen.hasLoop) {
        codeGenerator.switchToStaticsFile();
        codeGenerator.genCodeLine("static int  jjemptyLineNo[" + LexGen.maxLexStates + "];");
        codeGenerator.genCodeLine("static int  jjemptyColNo[" + LexGen.maxLexStates + "];");
        codeGenerator.genCodeLine("static bool jjbeenHere[" + LexGen.maxLexStates + "];");
        codeGenerator.switchToMainFile();
      }

      if (LexGen.hasSkipActions)
        DumpSkipActions();
      if (LexGen.hasMoreActions)
        DumpMoreActions();
      if (LexGen.hasTokenActions)
        DumpTokenActions();

      NfaState.PrintBoilerPlateCPP(codeGenerator);

      String charStreamName;
      if (Options.getUserCharStream())
        charStreamName = "CharStream";
      else {
        if (Options.getJavaUnicodeEscape())
          charStreamName = "JavaCharStream";
        else
          charStreamName = "SimpleCharStream";
      }

      writeTemplate("/templates/cpp/TokenManagerBoilerPlateMethods.template", settings,"charStreamName", "CharStream",
          "parserClassName", cu_name, "defaultLexState", "defaultLexState", "lexStateNameLength", LexGen.lexStateName.length);

      dumpBoilerPlateInHeader();

      // in the include file close the class signature
      DumpStaticVarDeclarations(); // static vars actually inst

      codeGenerator.switchToIncludeFile(); // remaining variables
      writeTemplate("/templates/cpp/DumpVarDeclarations.template", settings, "charStreamName", "CharStream", "lexStateNameLength",
          LexGen.lexStateName.length);
      codeGenerator.genCodeLine(/* { */ "};");

      codeGenerator.switchToStaticsFile();
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  public void finish(CodeGeneratorSettings settings, TokenizerData tokenizerData) {
    if (!Options.getBuildTokenManager())
      return;
    String fileName = Options.getOutputDirectory() + File.separator + tokenizerData.parserName + "TokenManager.cc";
    codeGenerator.saveOutput(fileName);
  }

  void PrintClassHead() {
    int i, j;

    List<String> tn = new ArrayList<>(toolNames);
    tn.add(toolName);

    codeGenerator.switchToStaticsFile();

    // standard includes
    codeGenerator.switchToIncludeFile();
    codeGenerator.genCodeLine("#include \"stdio.h\"");
    codeGenerator.genCodeLine("#include \"JavaCC.h\"");
    codeGenerator.genCodeLine("#include \"CharStream.h\"");
    codeGenerator.genCodeLine("#include \"Token.h\"");
    codeGenerator.genCodeLine("#include \"ErrorHandler.h\"");
    codeGenerator.genCodeLine("#include \"TokenManager.h\"");
    codeGenerator.genCodeLine("#include \"" + cu_name + "Constants.h\"");

    if (Options.stringValue(Options.USEROPTION__CPP_TOKEN_MANAGER_INCLUDES).length() > 0) {
      codeGenerator
          .genCodeLine("#include \"" + Options.stringValue(Options.USEROPTION__CPP_TOKEN_MANAGER_INCLUDES) + "\"\n");
    }

    codeGenerator.genCodeLine("");

    if (Options.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
      codeGenerator.genCodeLine("namespace " + Options.stringValue("NAMESPACE_OPEN"));
    }

    codeGenerator.genCodeLine("class " + cu_name + ";");

    int l = 0, kind;
    i = 1;

    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("/** Token Manager. */");
    String superClass = Options.stringValue(Options.USEROPTION__TOKEN_MANAGER_SUPER_CLASS);
    codeGenerator.genClassStart(null, LexGen.tokMgrClassName, new String[] {},
        new String[] { "public TokenManager" + (superClass == null ? "" : ", public " + superClass) });

    if (token_mgr_decls != null && token_mgr_decls.size() > 0) {
      Token t = (Token) token_mgr_decls.get(0);
      boolean commonTokenActionSeen = false;
      boolean commonTokenActionNeeded = Options.getCommonTokenAction();

      codeGenerator.printTokenSetup((Token) token_mgr_decls.get(0));
      codeGenerator.ccol = 1;

      // switchToMainFile();
      codeGenerator.switchToIncludeFile();
      for (j = 0; j < token_mgr_decls.size(); j++) {
        t = (Token) token_mgr_decls.get(j);
        if (t.kind == JavaCCParserConstants.IDENTIFIER && commonTokenActionNeeded && !commonTokenActionSeen) {
          commonTokenActionSeen = t.image.equals("CommonTokenAction");
          if (commonTokenActionSeen)
            t.image = cu_name + "TokenManager::" + t.image;
        }

        codeGenerator.printToken(t);
      }

      codeGenerator.switchToIncludeFile();
      codeGenerator.genCodeLine("  void CommonTokenAction(Token* token);");

      if (Options.getTokenManagerUsesParser()) {
        codeGenerator.genCodeLine("  void setParser(void* parser) {");
        codeGenerator.genCodeLine("      this->parser = (" + cu_name + "*) parser;");
        codeGenerator.genCodeLine("  }");
      }
      codeGenerator.genCodeLine("");

      if (commonTokenActionNeeded && !commonTokenActionSeen)
        JavaCCErrors.warning(
            "You have the COMMON_TOKEN_ACTION option set. " + "But it appears you have not defined the method :\n"
                + "      " + LexGen.staticString + "void CommonTokenAction(Token *t)\n"
                + "in your TOKEN_MGR_DECLS. The generated token manager will not compile.");

    } else if (Options.getCommonTokenAction()) {
      JavaCCErrors.warning("You have the COMMON_TOKEN_ACTION option set. " + "But you have not defined the method :\n"
          + "      " + LexGen.staticString + "void CommonTokenAction(Token *t)\n"
          + "in your TOKEN_MGR_DECLS. The generated token manager will not compile.");
    }

    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("  FILE *debugStream;");

    codeGenerator.generateMethodDefHeader("  void ", LexGen.tokMgrClassName, "setDebugStream(FILE *ds)");
    codeGenerator.genCodeLine("{ debugStream = ds; }");

    codeGenerator.switchToIncludeFile();
    if (Options.getTokenManagerUsesParser()) {
      codeGenerator.genCodeLine("");
      codeGenerator.genCodeLine("private:");
      codeGenerator.genCodeLine("  " + cu_name + "* parser = nullptr;");
    }
    codeGenerator.switchToMainFile();
  }

  void DumpDebugMethods(CodeGeneratorSettings settings) throws IOException {
    writeTemplate("/templates/cpp/DumpDebugMethods.template", settings,"maxOrdinal", LexGen.maxOrdinal, "stateSetSize", LexGen.stateSetSize);
  }


  private void dumpBoilerPlateInHeader() {
    codeGenerator.switchToIncludeFile();
    codeGenerator.genCodeLine("#ifndef JAVACC_CHARSTREAM");
    codeGenerator.genCodeLine("#define JAVACC_CHARSTREAM CharStream");
    codeGenerator.genCodeLine("#endif");
    codeGenerator.genCodeLine("");

    codeGenerator.genCodeLine("private:");
    codeGenerator.genCodeLine("  void ReInitRounds();");
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("public:");
    codeGenerator
        .genCodeLine("  " + LexGen.tokMgrClassName + "(JAVACC_CHARSTREAM *stream, int lexState = " + LexGen.defaultLexState + ");");
    codeGenerator.genCodeLine("  virtual ~" + LexGen.tokMgrClassName + "();");
    codeGenerator.genCodeLine("  void ReInit(JAVACC_CHARSTREAM *stream, int lexState = " + LexGen.defaultLexState + ");");
    codeGenerator.genCodeLine("  void SwitchTo(int lexState);");
    codeGenerator.genCodeLine("  void clear();");
    codeGenerator.genCodeLine("  const JJSimpleString jjKindsForBitVector(int i, " + Options.getLongType() + " vec);");
    codeGenerator
        .genCodeLine("  const JJSimpleString jjKindsForStateVector(int lexState, int vec[], int start, int end);");
    codeGenerator.genCodeLine("");
  }

  private void DumpStaticVarDeclarations() throws IOException {
    int i;

    codeGenerator.switchToStaticsFile(); // remaining variables
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("/** Lexer state names. */");
    codeGenerator.genStringLiteralArrayCPP("lexStateNames", LexGen.lexStateName);

    if (LexGen.maxLexStates > 1) {
      codeGenerator.genCodeLine("");
      codeGenerator.genCodeLine("/** Lex State array. */");
      codeGenerator.genCode("static const int jjnewLexState[] = {");

      for (i = 0; i < LexGen.maxOrdinal; i++) {
        if (i % 25 == 0)
          codeGenerator.genCode("\n   ");

        if (LexGen.newLexState[i] == null)
          codeGenerator.genCode("-1, ");
        else
          codeGenerator.genCode(GetIndex(LexGen.newLexState[i]) + ", ");
      }
      codeGenerator.genCodeLine("\n};");
    }

    if (LexGen.hasSkip || LexGen.hasMore || LexGen.hasSpecial) {
      // Bit vector for TOKEN
      codeGenerator.genCode("static const " + Options.getLongType() + " jjtoToken[] = {");
      for (i = 0; i < LexGen.maxOrdinal / 64 + 1; i++) {
        if (i % 4 == 0)
          codeGenerator.genCode("\n   ");
        codeGenerator.genCode("0x" + Long.toHexString(LexGen.toToken[i]) + "L, ");
      }
      codeGenerator.genCodeLine("\n};");
    }

    if (LexGen.hasSkip || LexGen.hasSpecial) {
      // Bit vector for SKIP
      codeGenerator.genCode("static const " + Options.getLongType() + " jjtoSkip[] = {");
      for (i = 0; i < LexGen.maxOrdinal / 64 + 1; i++) {
        if (i % 4 == 0)
          codeGenerator.genCode("\n   ");
        codeGenerator.genCode("0x" + Long.toHexString(LexGen.toSkip[i]) + "L, ");
      }
      codeGenerator.genCodeLine("\n};");
    }

    if (LexGen.hasSpecial) {
      // Bit vector for SPECIAL
      codeGenerator.genCode("static const " + Options.getLongType() + " jjtoSpecial[] = {");
      for (i = 0; i < LexGen.maxOrdinal / 64 + 1; i++) {
        if (i % 4 == 0)
          codeGenerator.genCode("\n   ");
        codeGenerator.genCode("0x" + Long.toHexString(LexGen.toSpecial[i]) + "L, ");
      }
      codeGenerator.genCodeLine("\n};");
    }
  }

  void DumpFillToken() {
    final double tokenVersion = JavaFiles.getVersion("Token.java");
    final boolean hasBinaryNewToken = tokenVersion > 4.09;

    codeGenerator.generateMethodDefHeader("Token *", LexGen.tokMgrClassName, "jjFillToken()");
    codeGenerator.genCodeLine("{");
    codeGenerator.genCodeLine("   Token *t;");
    codeGenerator.genCodeLine("   JJString curTokenImage;");
    if (LexGen.keepLineCol) {
      codeGenerator.genCodeLine("   int beginLine   = -1;");
      codeGenerator.genCodeLine("   int endLine     = -1;");
      codeGenerator.genCodeLine("   int beginColumn = -1;");
      codeGenerator.genCodeLine("   int endColumn   = -1;");
    }

    if (LexGen.hasEmptyMatch) {
      codeGenerator.genCodeLine("   if (jjmatchedPos < 0)");
      codeGenerator.genCodeLine("   {");
      codeGenerator.genCodeLine("       curTokenImage = image.c_str();");

      if (LexGen.keepLineCol) {
        codeGenerator.genCodeLine("   if (input_stream->getTrackLineColumn()) {");
        codeGenerator.genCodeLine("      beginLine = endLine = input_stream->getEndLine();");
        codeGenerator.genCodeLine("      beginColumn = endColumn = input_stream->getEndColumn();");
        codeGenerator.genCodeLine("   }");
      }

      codeGenerator.genCodeLine("   }");
      codeGenerator.genCodeLine("   else");
      codeGenerator.genCodeLine("   {");
      codeGenerator.genCodeLine("      JJString im = jjstrLiteralImages[jjmatchedKind];");
      codeGenerator.genCodeLine("      curTokenImage = (im.length() == 0) ? input_stream->GetImage() : im;");

      if (LexGen.keepLineCol) {
        codeGenerator.genCodeLine("   if (input_stream->getTrackLineColumn()) {");
        codeGenerator.genCodeLine("      beginLine = input_stream->getBeginLine();");
        codeGenerator.genCodeLine("      beginColumn = input_stream->getBeginColumn();");
        codeGenerator.genCodeLine("      endLine = input_stream->getEndLine();");
        codeGenerator.genCodeLine("      endColumn = input_stream->getEndColumn();");
        codeGenerator.genCodeLine("   }");
      }

      codeGenerator.genCodeLine("   }");
    } else {
      codeGenerator.genCodeLine("   JJString im = jjstrLiteralImages[jjmatchedKind];");
      codeGenerator.genCodeLine("   curTokenImage = (im.length() == 0) ? input_stream->GetImage() : im;");
      if (LexGen.keepLineCol) {
        codeGenerator.genCodeLine("   if (input_stream->getTrackLineColumn()) {");
        codeGenerator.genCodeLine("     beginLine = input_stream->getBeginLine();");
        codeGenerator.genCodeLine("     beginColumn = input_stream->getBeginColumn();");
        codeGenerator.genCodeLine("     endLine = input_stream->getEndLine();");
        codeGenerator.genCodeLine("     endColumn = input_stream->getEndColumn();");
        codeGenerator.genCodeLine("   }");
      }
    }

    if (Options.getTokenFactory().length() > 0) {
      codeGenerator.genCodeLine("   t = " + codeGenerator.getClassQualifier(Options.getTokenFactory())
          + "newToken(jjmatchedKind, curTokenImage);");
    } else if (hasBinaryNewToken) {
      codeGenerator.genCodeLine(
          "   t = " + codeGenerator.getClassQualifier("Token") + "newToken(jjmatchedKind, curTokenImage);");
    } else {
      codeGenerator.genCodeLine("   t = " + codeGenerator.getClassQualifier("Token") + "newToken(jjmatchedKind);");
      codeGenerator.genCodeLine("   t->kind = jjmatchedKind;");
      codeGenerator.genCodeLine("   t->image = curTokenImage;");
    }
    codeGenerator.genCodeLine("   t->specialToken = nullptr;");
    codeGenerator.genCodeLine("   t->next = nullptr;");

    if (LexGen.keepLineCol) {
      codeGenerator.genCodeLine("");
      codeGenerator.genCodeLine("   if (input_stream->getTrackLineColumn()) {");
      codeGenerator.genCodeLine("   t->beginLine = beginLine;");
      codeGenerator.genCodeLine("   t->endLine = endLine;");
      codeGenerator.genCodeLine("   t->beginColumn = beginColumn;");
      codeGenerator.genCodeLine("   t->endColumn = endColumn;");
      codeGenerator.genCodeLine("   }");
    }

    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("   return t;");
    codeGenerator.genCodeLine("}");
  }

  void DumpGetNextToken() {
    int i;

    codeGenerator.switchToIncludeFile();
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("public:");
    codeGenerator.genCodeLine("    int curLexState;");
    codeGenerator.genCodeLine("    int jjnewStateCnt;");
    codeGenerator.genCodeLine("    int jjround;");
    codeGenerator.genCodeLine("    int jjmatchedPos;");
    codeGenerator.genCodeLine("    int jjmatchedKind;");
    codeGenerator.genCodeLine("");
    codeGenerator.switchToMainFile();
    codeGenerator.genCodeLine("const int defaultLexState = " + LexGen.defaultLexState + ";");
    codeGenerator.genCodeLine("/** Get the next Token. */");
    codeGenerator.generateMethodDefHeader("Token *", LexGen.tokMgrClassName, "getNextToken()");
    codeGenerator.genCodeLine("{");
    if (LexGen.hasSpecial) {
      codeGenerator.genCodeLine("  Token *specialToken = nullptr;");
    }
    codeGenerator.genCodeLine("  Token *matchedToken = nullptr;");
    codeGenerator.genCodeLine("  int curPos = 0;");
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("  for (;;)");
    codeGenerator.genCodeLine("  {");
    codeGenerator.genCodeLine("   EOFLoop: ");
    // codeGenerator.genCodeLine(" {");
    // codeGenerator.genCodeLine(" curChar = input_stream->BeginToken();");
    // codeGenerator.genCodeLine(" }");
    codeGenerator.genCodeLine("   if (input_stream->endOfInput())");
    codeGenerator.genCodeLine("   {");
    // codeGenerator.genCodeLine(" input_stream->backup(1);");

    if (Options.getDebugTokenManager())
      codeGenerator.genCodeLine("      fprintf(debugStream, \"Returning the <EOF> token.\\n\");");

    codeGenerator.genCodeLine("      jjmatchedKind = 0;");
    codeGenerator.genCodeLine("      jjmatchedPos = -1;");
    codeGenerator.genCodeLine("      matchedToken = jjFillToken();");

    if (LexGen.hasSpecial)
      codeGenerator.genCodeLine("      matchedToken->specialToken = specialToken;");

    if (JavaCCGlobals.nextStateForEof != null || JavaCCGlobals.actForEof != null)
      codeGenerator.genCodeLine("      TokenLexicalActions(matchedToken);");

    if (Options.getCommonTokenAction())
      codeGenerator.genCodeLine("      CommonTokenAction(matchedToken);");

    codeGenerator.genCodeLine("      return matchedToken;");
    codeGenerator.genCodeLine("   }");
    codeGenerator.genCodeLine("   curChar = input_stream->BeginToken();");

    if (LexGen.hasMoreActions || LexGen.hasSkipActions || LexGen.hasTokenActions) {
      codeGenerator.genCodeLine("   image = jjimage;");
      codeGenerator.genCodeLine("   image.clear();");
      codeGenerator.genCodeLine("   jjimageLen = 0;");
    }

    codeGenerator.genCodeLine("");

    String prefix = "";
    if (LexGen.hasMore) {
      codeGenerator.genCodeLine("   for (;;)");
      codeGenerator.genCodeLine("   {");
      prefix = "  ";
    }

    String endSwitch = "";
    String caseStr = "";
    // this also sets up the start state of the nfa
    if (LexGen.maxLexStates > 1) {
      codeGenerator.genCodeLine(prefix + "   switch(curLexState)");
      codeGenerator.genCodeLine(prefix + "   {");
      endSwitch = prefix + "   }";
      caseStr = prefix + "     case ";
      prefix += "    ";
    }

    prefix += "   ";
    for (i = 0; i < LexGen.maxLexStates; i++) {
      if (LexGen.maxLexStates > 1)
        codeGenerator.genCodeLine(caseStr + i + ":");

      if (LexGen.singlesToSkip[i].HasTransitions()) {
        // added the backup(0) to make JIT happy
        codeGenerator.genCodeLine(prefix + "{ input_stream->backup(0);");
        if (LexGen.singlesToSkip[i].asciiMoves[0] != 0L && LexGen.singlesToSkip[i].asciiMoves[1] != 0L) {
          codeGenerator.genCodeLine(
              prefix + "   while ((curChar < 64" + " && (0x" + Long.toHexString(LexGen.singlesToSkip[i].asciiMoves[0])
                  + "L & (1L << curChar)) != 0L) || \n" + prefix + "          (curChar >> 6) == 1" + " && (0x"
                  + Long.toHexString(LexGen.singlesToSkip[i].asciiMoves[1]) + "L & (1L << (curChar & 077))) != 0L)");
        } else if (LexGen.singlesToSkip[i].asciiMoves[1] == 0L) {
          codeGenerator.genCodeLine(prefix + "   while (curChar <= " + (int) MaxChar(LexGen.singlesToSkip[i].asciiMoves[0])
              + " && (0x" + Long.toHexString(LexGen.singlesToSkip[i].asciiMoves[0]) + "L & (1L << curChar)) != 0L)");
        } else if (LexGen.singlesToSkip[i].asciiMoves[0] == 0L) {
          codeGenerator.genCodeLine(prefix + "   while (curChar > 63 && curChar <= "
              + (MaxChar(LexGen.singlesToSkip[i].asciiMoves[1]) + 64) + " && (0x"
              + Long.toHexString(LexGen.singlesToSkip[i].asciiMoves[1]) + "L & (1L << (curChar & 077))) != 0L)");
        }

        codeGenerator.genCodeLine(prefix + "{");
        if (Options.getDebugTokenManager()) {
          if (LexGen.maxLexStates > 1) {
            codeGenerator.genCodeLine(
                "      fprintf(debugStream, \"<%s>\" , addUnicodeEscapes(lexStateNames[curLexState]).c_str());");
          }

          codeGenerator
              .genCodeLine("      fprintf(debugStream, \"Skipping character : %c(%d)\\n\", curChar, (int)curChar);");
        }

        codeGenerator.genCodeLine(prefix + "if (input_stream->endOfInput()) { goto EOFLoop; }");
        codeGenerator.genCodeLine(prefix + "curChar = input_stream->BeginToken();");
        codeGenerator.genCodeLine(prefix + "}");
        codeGenerator.genCodeLine(prefix + "}");
      }

      if (LexGen.initMatch[i] != Integer.MAX_VALUE && LexGen.initMatch[i] != 0) {
        if (Options.getDebugTokenManager())
          codeGenerator.genCodeLine(
              "      fprintf(debugStream, \"   Matched the empty string as %s token.\\n\", addUnicodeEscapes(tokenImage["
                  + LexGen.initMatch[i] + "]).c_str());");

        codeGenerator.genCodeLine(prefix + "jjmatchedKind = " + LexGen.initMatch[i] + ";");
        codeGenerator.genCodeLine(prefix + "jjmatchedPos = -1;");
        codeGenerator.genCodeLine(prefix + "curPos = 0;");
      } else {
        codeGenerator.genCodeLine(prefix + "jjmatchedKind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");
        codeGenerator.genCodeLine(prefix + "jjmatchedPos = 0;");
      }

      if (Options.getDebugTokenManager()) {
        codeGenerator
            .genCodeLine("   fprintf(debugStream, " + "\"<%s>Current character : %c(%d) at line %d column %d\\n\","
                + "addUnicodeEscapes(lexStateNames[curLexState]).c_str(), curChar, (int)curChar, "
                + "input_stream->getEndLine(), input_stream->getEndColumn());");
      }

      codeGenerator.genCodeLine(prefix + "curPos = jjMoveStringLiteralDfa0_" + i + "();");

      if (LexGen.canMatchAnyChar[i] != -1) {
        if (LexGen.initMatch[i] != Integer.MAX_VALUE && LexGen.initMatch[i] != 0)
          codeGenerator.genCodeLine(
              prefix + "if (jjmatchedPos < 0 || (jjmatchedPos == 0 && jjmatchedKind > " + LexGen.canMatchAnyChar[i] + "))");
        else
          codeGenerator.genCodeLine(prefix + "if (jjmatchedPos == 0 && jjmatchedKind > " + LexGen.canMatchAnyChar[i] + ")");
        codeGenerator.genCodeLine(prefix + "{");

        if (Options.getDebugTokenManager()) {
          codeGenerator.genCodeLine(
              "           fprintf(debugStream, \"   Current character matched as a %s token.\\n\", addUnicodeEscapes(tokenImage["
                  + LexGen.canMatchAnyChar[i] + "]).c_str());");
        }
        codeGenerator.genCodeLine(prefix + "   jjmatchedKind = " + LexGen.canMatchAnyChar[i] + ";");

        if (LexGen.canMatchAnyChar[i] != Integer.MAX_VALUE && LexGen.initMatch[i] != 0)
          codeGenerator.genCodeLine(prefix + "   jjmatchedPos = 0;");

        codeGenerator.genCodeLine(prefix + "}");
      }

      if (LexGen.maxLexStates > 1)
        codeGenerator.genCodeLine(prefix + "break;");
    }

    if (LexGen.maxLexStates > 1)
      codeGenerator.genCodeLine(endSwitch);
    else if (LexGen.maxLexStates == 0)
      codeGenerator.genCodeLine("       jjmatchedKind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");

    if (LexGen.maxLexStates > 1)
      prefix = "  ";
    else
      prefix = "";

    if (LexGen.maxLexStates > 0) {
      codeGenerator.genCodeLine(prefix + "   if (jjmatchedKind != 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
      codeGenerator.genCodeLine(prefix + "   {");
      codeGenerator.genCodeLine(prefix + "      if (jjmatchedPos + 1 < curPos)");

      if (Options.getDebugTokenManager()) {
        codeGenerator.genCodeLine(prefix + "      {");
        codeGenerator.genCodeLine(prefix + "         fprintf(debugStream, "
            + "\"   Putting back %d characters into the input stream.\\n\", (curPos - jjmatchedPos - 1));");
      }

      codeGenerator.genCodeLine(prefix + "         input_stream->backup(curPos - jjmatchedPos - 1);");

      if (Options.getDebugTokenManager()) {
        codeGenerator.genCodeLine(prefix + "      }");
      }

      if (Options.getDebugTokenManager()) {
        codeGenerator.genCodeLine("    fprintf(debugStream, "
            + "\"****** FOUND A %d(%s) MATCH (%s) ******\\n\", jjmatchedKind, addUnicodeEscapes(tokenImage[jjmatchedKind]).c_str(), addUnicodeEscapes(input_stream->GetSuffix(jjmatchedPos + 1)).c_str());");
      }

      if (LexGen.hasSkip || LexGen.hasMore || LexGen.hasSpecial) {
        codeGenerator.genCodeLine(
            prefix + "      if ((jjtoToken[jjmatchedKind >> 6] & " + "(1L << (jjmatchedKind & 077))) != 0L)");
        codeGenerator.genCodeLine(prefix + "      {");
      }

      codeGenerator.genCodeLine(prefix + "         matchedToken = jjFillToken();");

      if (LexGen.hasSpecial)
        codeGenerator.genCodeLine(prefix + "         matchedToken->specialToken = specialToken;");

      if (LexGen.hasTokenActions)
        codeGenerator.genCodeLine(prefix + "         TokenLexicalActions(matchedToken);");

      if (LexGen.maxLexStates > 1) {
        codeGenerator.genCodeLine("       if (jjnewLexState[jjmatchedKind] != -1)");
        codeGenerator.genCodeLine(prefix + "       curLexState = jjnewLexState[jjmatchedKind];");
      }

      if (Options.getCommonTokenAction())
        codeGenerator.genCodeLine(prefix + "         CommonTokenAction(matchedToken);");

      codeGenerator.genCodeLine(prefix + "         return matchedToken;");

      if (LexGen.hasSkip || LexGen.hasMore || LexGen.hasSpecial) {
        codeGenerator.genCodeLine(prefix + "      }");

        if (LexGen.hasSkip || LexGen.hasSpecial) {
          if (LexGen.hasMore) {
            codeGenerator.genCodeLine(
                prefix + "      else if ((jjtoSkip[jjmatchedKind >> 6] & " + "(1L << (jjmatchedKind & 077))) != 0L)");
          } else
            codeGenerator.genCodeLine(prefix + "      else");

          codeGenerator.genCodeLine(prefix + "      {");

          if (LexGen.hasSpecial) {
            codeGenerator.genCodeLine(
                prefix + "         if ((jjtoSpecial[jjmatchedKind >> 6] & " + "(1L << (jjmatchedKind & 077))) != 0L)");
            codeGenerator.genCodeLine(prefix + "         {");

            codeGenerator.genCodeLine(prefix + "            matchedToken = jjFillToken();");

            codeGenerator.genCodeLine(prefix + "            if (specialToken == nullptr)");
            codeGenerator.genCodeLine(prefix + "               specialToken = matchedToken;");
            codeGenerator.genCodeLine(prefix + "            else");
            codeGenerator.genCodeLine(prefix + "            {");
            codeGenerator.genCodeLine(prefix + "               matchedToken->specialToken = specialToken;");
            codeGenerator.genCodeLine(prefix + "               specialToken = (specialToken->next = matchedToken);");
            codeGenerator.genCodeLine(prefix + "            }");

            if (LexGen.hasSkipActions)
              codeGenerator.genCodeLine(prefix + "            SkipLexicalActions(matchedToken);");

            codeGenerator.genCodeLine(prefix + "         }");

            if (LexGen.hasSkipActions) {
              codeGenerator.genCodeLine(prefix + "         else");
              codeGenerator.genCodeLine(prefix + "            SkipLexicalActions(nullptr);");
            }
          } else if (LexGen.hasSkipActions)
            codeGenerator.genCodeLine(prefix + "         SkipLexicalActions(nullptr);");

          if (LexGen.maxLexStates > 1) {
            codeGenerator.genCodeLine("         if (jjnewLexState[jjmatchedKind] != -1)");
            codeGenerator.genCodeLine(prefix + "         curLexState = jjnewLexState[jjmatchedKind];");
          }

          codeGenerator.genCodeLine(prefix + "         goto EOFLoop;");
          codeGenerator.genCodeLine(prefix + "      }");
        }

        if (LexGen.hasMore) {
          if (LexGen.hasMoreActions)
            codeGenerator.genCodeLine(prefix + "      MoreLexicalActions();");
          else if (LexGen.hasSkipActions || LexGen.hasTokenActions)
            codeGenerator.genCodeLine(prefix + "      jjimageLen += jjmatchedPos + 1;");

          if (LexGen.maxLexStates > 1) {
            codeGenerator.genCodeLine("      if (jjnewLexState[jjmatchedKind] != -1)");
            codeGenerator.genCodeLine(prefix + "      curLexState = jjnewLexState[jjmatchedKind];");
          }
          codeGenerator.genCodeLine(prefix + "      curPos = 0;");
          codeGenerator.genCodeLine(prefix + "      jjmatchedKind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");

          codeGenerator.genCodeLine(prefix + "   if (!input_stream->endOfInput()) {");
          codeGenerator.genCodeLine(prefix + "         curChar = input_stream->readChar();");

          if (Options.getDebugTokenManager()) {
            codeGenerator
                .genCodeLine("   fprintf(debugStream, " + "\"<%s>Current character : %c(%d) at line %d column %d\\n\","
                    + "addUnicodeEscapes(lexStateNames[curLexState]).c_str(), curChar, (int)curChar, "
                    + "input_stream->getEndLine(), input_stream->getEndColumn());");
          }
          codeGenerator.genCodeLine(prefix + "   continue;");
          codeGenerator.genCodeLine(prefix + " }");
        }
      }

      codeGenerator.genCodeLine(prefix + "   }");
      codeGenerator.genCodeLine(prefix + "   int error_line = input_stream->getEndLine();");
      codeGenerator.genCodeLine(prefix + "   int error_column = input_stream->getEndColumn();");
      codeGenerator.genCodeLine(prefix + "   JJString error_after;");
      codeGenerator.genCodeLine(prefix + "   bool EOFSeen = false;");
      codeGenerator.genCodeLine(prefix + "   if (input_stream->endOfInput()) {");
      codeGenerator.genCodeLine(prefix + "      EOFSeen = true;");
      codeGenerator.genCodeLine(prefix + "      error_after = curPos <= 1 ? EMPTY : input_stream->GetImage();");
      codeGenerator.genCodeLine(prefix + "      if (curChar == '\\n' || curChar == '\\r') {");
      codeGenerator.genCodeLine(prefix + "         error_line++;");
      codeGenerator.genCodeLine(prefix + "         error_column = 0;");
      codeGenerator.genCodeLine(prefix + "      }");
      codeGenerator.genCodeLine(prefix + "      else");
      codeGenerator.genCodeLine(prefix + "         error_column++;");
      codeGenerator.genCodeLine(prefix + "   }");
      codeGenerator.genCodeLine(prefix + "   if (!EOFSeen) {");
      codeGenerator.genCodeLine(prefix + "      error_after = curPos <= 1 ? EMPTY : input_stream->GetImage();");
      codeGenerator.genCodeLine(prefix + "   }");
      codeGenerator.genCodeLine(prefix
          + "   errorHandler->lexicalError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, this);");
    }

    if (LexGen.hasMore)
      codeGenerator.genCodeLine(prefix + " }");

    codeGenerator.genCodeLine("  }");
    codeGenerator.genCodeLine("}");
    codeGenerator.genCodeLine("");
  }

  public void DumpSkipActions() {
    Action act;

    codeGenerator.generateMethodDefHeader("void ", LexGen.tokMgrClassName, "SkipLexicalActions(Token *matchedToken)");
    codeGenerator.genCodeLine("{");
    codeGenerator.genCodeLine("   switch(jjmatchedKind)");
    codeGenerator.genCodeLine("   {");

    Outer:
    for (int i = 0; i < LexGen.maxOrdinal; i++) {
      if ((LexGen.toSkip[i / 64] & (1L << (i % 64))) == 0L)
        continue;

      for (;;) {
        if (((act = LexGen.actions[i]) == null || act.getActionTokens() == null || act.getActionTokens().size() == 0)
            && !LexGen.canLoop[LexGen.lexStates[i]])
          continue Outer;

        codeGenerator.genCodeLine("      case " + i + " : {");

        if (LexGen.initMatch[LexGen.lexStates[i]] == i && LexGen.canLoop[LexGen.lexStates[i]]) {
          codeGenerator.genCodeLine("         if (jjmatchedPos == -1)");
          codeGenerator.genCodeLine("         {");
          codeGenerator.genCodeLine("            if (jjbeenHere[" + LexGen.lexStates[i] + "] &&");
          codeGenerator
              .genCodeLine("                jjemptyLineNo[" + LexGen.lexStates[i] + "] == input_stream->getBeginLine() &&");
          codeGenerator
              .genCodeLine("                jjemptyColNo[" + LexGen.lexStates[i] + "] == input_stream->getBeginColumn())");
          codeGenerator.genCodeLine(
              "               errorHandler->lexicalError(JJString(\"(\"Error: Bailing out of infinite loop caused by repeated empty string matches \" + \"at line \" + input_stream->getBeginLine() + \", \" + \"column \" + input_stream->getBeginColumn() + \".\")), this);");
          codeGenerator.genCodeLine("            jjemptyLineNo[" + LexGen.lexStates[i] + "] = input_stream->getBeginLine();");
          codeGenerator.genCodeLine("            jjemptyColNo[" + LexGen.lexStates[i] + "] = input_stream->getBeginColumn();");
          codeGenerator.genCodeLine("            jjbeenHere[" + LexGen.lexStates[i] + "] = true;");
          codeGenerator.genCodeLine("         }");
        }

        if ((act = LexGen.actions[i]) == null || act.getActionTokens().size() == 0)
          break;

        codeGenerator.genCode("         image.append");
        if (RStringLiteral.allImages[i] != null) {
          codeGenerator.genCodeLine("(jjstrLiteralImages[" + i + "]);");
          codeGenerator.genCodeLine("        lengthOfMatch = jjstrLiteralImages[" + i + "].length();");
        } else {
          codeGenerator.genCodeLine("(input_stream->GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));");
        }

        codeGenerator.printTokenSetup(act.getActionTokens().get(0));
        codeGenerator.ccol = 1;

        for (int j = 0; j < act.getActionTokens().size(); j++)
          codeGenerator.printToken(act.getActionTokens().get(j));
        codeGenerator.genCodeLine("");

        break;
      }

      codeGenerator.genCodeLine("         break;");
      codeGenerator.genCodeLine("       }");
    }

    codeGenerator.genCodeLine("      default :");
    codeGenerator.genCodeLine("         break;");
    codeGenerator.genCodeLine("   }");
    codeGenerator.genCodeLine("}");
  }

  public void DumpMoreActions() {
    Action act;

    codeGenerator.generateMethodDefHeader("void ", LexGen.tokMgrClassName, "MoreLexicalActions()");
    codeGenerator.genCodeLine("{");
    codeGenerator.genCodeLine("   jjimageLen += (lengthOfMatch = jjmatchedPos + 1);");
    codeGenerator.genCodeLine("   switch(jjmatchedKind)");
    codeGenerator.genCodeLine("   {");

    Outer:
    for (int i = 0; i < LexGen.maxOrdinal; i++) {
      if ((LexGen.toMore[i / 64] & (1L << (i % 64))) == 0L)
        continue;

      for (;;) {
        if (((act = LexGen.actions[i]) == null || act.getActionTokens() == null || act.getActionTokens().size() == 0)
            && !LexGen.canLoop[LexGen.lexStates[i]])
          continue Outer;

        codeGenerator.genCodeLine("      case " + i + " : {");

        if (LexGen.initMatch[LexGen.lexStates[i]] == i && LexGen.canLoop[LexGen.lexStates[i]]) {
          codeGenerator.genCodeLine("         if (jjmatchedPos == -1)");
          codeGenerator.genCodeLine("         {");
          codeGenerator.genCodeLine("            if (jjbeenHere[" + LexGen.lexStates[i] + "] &&");
          codeGenerator
              .genCodeLine("                jjemptyLineNo[" + LexGen.lexStates[i] + "] == input_stream->getBeginLine() &&");
          codeGenerator
              .genCodeLine("                jjemptyColNo[" + LexGen.lexStates[i] + "] == input_stream->getBeginColumn())");
          codeGenerator.genCodeLine(
              "               errorHandler->lexicalError(JJString(\"(\"Error: Bailing out of infinite loop caused by repeated empty string matches \" + \"at line \" + input_stream->getBeginLine() + \", \" + \"column \" + input_stream->getBeginColumn() + \".\")), this);");
          codeGenerator.genCodeLine("            jjemptyLineNo[" + LexGen.lexStates[i] + "] = input_stream->getBeginLine();");
          codeGenerator.genCodeLine("            jjemptyColNo[" + LexGen.lexStates[i] + "] = input_stream->getBeginColumn();");
          codeGenerator.genCodeLine("            jjbeenHere[" + LexGen.lexStates[i] + "] = true;");
          codeGenerator.genCodeLine("         }");
        }

        if ((act = LexGen.actions[i]) == null || act.getActionTokens().size() == 0) {
          break;
        }

        codeGenerator.genCode("         image.append");

        if (RStringLiteral.allImages[i] != null)
          codeGenerator.genCodeLine("(jjstrLiteralImages[" + i + "]);");
        else
          codeGenerator.genCodeLine("(input_stream->GetSuffix(jjimageLen));");

        codeGenerator.genCodeLine("         jjimageLen = 0;");
        codeGenerator.printTokenSetup(act.getActionTokens().get(0));
        codeGenerator.ccol = 1;

        for (int j = 0; j < act.getActionTokens().size(); j++)
          codeGenerator.printToken(act.getActionTokens().get(j));
        codeGenerator.genCodeLine("");

        break;
      }

      codeGenerator.genCodeLine("         break;");
      codeGenerator.genCodeLine("       }");
    }

    codeGenerator.genCodeLine("      default :");
    codeGenerator.genCodeLine("         break;");

    codeGenerator.genCodeLine("   }");
    codeGenerator.genCodeLine("}");
  }

  public void DumpTokenActions() {
    Action act;
    int i;

    codeGenerator.generateMethodDefHeader("void ", LexGen.tokMgrClassName, "TokenLexicalActions(Token *matchedToken)");
    codeGenerator.genCodeLine("{");
    codeGenerator.genCodeLine("   switch(jjmatchedKind)");
    codeGenerator.genCodeLine("   {");

    Outer:
    for (i = 0; i < LexGen.maxOrdinal; i++) {
      if ((LexGen.toToken[i / 64] & (1L << (i % 64))) == 0L)
        continue;

      for (;;) {
        if (((act = LexGen.actions[i]) == null || act.getActionTokens() == null || act.getActionTokens().size() == 0)
            && !LexGen.canLoop[LexGen.lexStates[i]])
          continue Outer;

        codeGenerator.genCodeLine("      case " + i + " : {");

        if (LexGen.initMatch[LexGen.lexStates[i]] == i && LexGen.canLoop[LexGen.lexStates[i]]) {
          codeGenerator.genCodeLine("         if (jjmatchedPos == -1)");
          codeGenerator.genCodeLine("         {");
          codeGenerator.genCodeLine("            if (jjbeenHere[" + LexGen.lexStates[i] + "] &&");
          codeGenerator
              .genCodeLine("                jjemptyLineNo[" + LexGen.lexStates[i] + "] == input_stream->getBeginLine() &&");
          codeGenerator
              .genCodeLine("                jjemptyColNo[" + LexGen.lexStates[i] + "] == input_stream->getBeginColumn())");
          codeGenerator.genCodeLine(
              "               errorHandler->lexicalError(JJString(\"Error: Bailing out of infinite loop caused by repeated empty string matches "
                  + "at line \" + input_stream->getBeginLine() + \", "
                  + "column \" + input_stream->getBeginColumn() + \".\"), this);");
          codeGenerator.genCodeLine("            jjemptyLineNo[" + LexGen.lexStates[i] + "] = input_stream->getBeginLine();");
          codeGenerator.genCodeLine("            jjemptyColNo[" + LexGen.lexStates[i] + "] = input_stream->getBeginColumn();");
          codeGenerator.genCodeLine("            jjbeenHere[" + LexGen.lexStates[i] + "] = true;");
          codeGenerator.genCodeLine("         }");
        }

        if ((act = LexGen.actions[i]) == null || act.getActionTokens().size() == 0)
          break;

        if (i == 0) {
          codeGenerator.genCodeLine("      image.setLength(0);"); // For EOF no image is there
        } else {
          codeGenerator.genCode("        image.append");

          if (RStringLiteral.allImages[i] != null) {
            codeGenerator.genCodeLine("(jjstrLiteralImages[" + i + "]);");
            codeGenerator.genCodeLine("        lengthOfMatch = jjstrLiteralImages[" + i + "].length();");
          } else {
            codeGenerator.genCodeLine("(input_stream->GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));");
          }
        }

        codeGenerator.printTokenSetup(act.getActionTokens().get(0));
        codeGenerator.ccol = 1;

        for (int j = 0; j < act.getActionTokens().size(); j++)
          codeGenerator.printToken(act.getActionTokens().get(j));
        codeGenerator.genCodeLine("");

        break;
      }

      codeGenerator.genCodeLine("         break;");
      codeGenerator.genCodeLine("       }");
    }

    codeGenerator.genCodeLine("      default :");
    codeGenerator.genCodeLine("         break;");
    codeGenerator.genCodeLine("   }");
    codeGenerator.genCodeLine("}");
  }

  static int GetIndex(String name) {
    for (int i = 0; i < LexGen.lexStateName.length; i++)
      if (LexGen.lexStateName[i] != null && LexGen.lexStateName[i].equals(name))
        return i;

    throw new Error(); // Should never come here
  }

  // Assumes l != 0L
  static char MaxChar(long l) {
    for (int i = 64; i-- > 0;)
      if ((l & (1L << i)) != 0L)
        return (char) i;

    return 0xffff;
  }


  @SuppressWarnings("unchecked")
  protected void writeTemplate(String name, CodeGeneratorSettings settings, Object... additionalOptions) throws IOException {
    Map<String, Object> options = new HashMap<>(settings);

    for (int i = 0; i < additionalOptions.length; i++) {
      Object o = additionalOptions[i];

      if (o instanceof Map<?, ?>) {
        options.putAll((Map<String, Object>) o);
      } else {
        if (i == additionalOptions.length - 1)
          throw new IllegalArgumentException("Must supply pairs of [name value] args");

        options.put((String) o, additionalOptions[i + 1]);
        i++;
      }
    }

    OutputFileGenerator gen = new OutputFileGenerator(name, options);
    StringWriter sw = new StringWriter();
    gen.generate(new PrintWriter(sw));
    sw.close();
    codeGenerator.genCode(sw.toString());
  }

}
