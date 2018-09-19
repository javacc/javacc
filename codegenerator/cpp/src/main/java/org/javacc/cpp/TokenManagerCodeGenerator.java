
package org.javacc.cpp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.JavaCCErrors;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.JavaCCParserConstants;
import org.javacc.parser.JavaFiles;
import org.javacc.parser.LexGen;
import org.javacc.parser.Action;
import org.javacc.parser.CodeGenHelper;
import org.javacc.parser.Options;
import org.javacc.parser.RChoice;
import org.javacc.parser.RegExprSpec;
import org.javacc.parser.RegularExpression;
import org.javacc.parser.Token;
import org.javacc.parser.TokenProduction;
import org.javacc.parser.TokenizerData;
import org.javacc.utils.OutputFileGenerator;

import static org.javacc.parser.JavaCCGlobals.*;

import org.javacc.cpp.todo.LexGenCPP;
import org.javacc.cpp.todo.Nfa;
import org.javacc.cpp.todo.NfaAdaptor;
import org.javacc.cpp.todo.NfaState;
import org.javacc.cpp.todo.RStringLiteralHelper;

/**
 * Class that implements a table driven code generator for the token manager in
 * java.
 */
public class TokenManagerCodeGenerator implements org.javacc.parser.TokenManagerCodeGenerator {

  private String staticString;
  private String tokMgrClassName;
  
  private final CodeGenHelper codeGenerator   = new CppCodeGenHelper();
  private CodeGeneratorSettings settings;
  

  @Override
  public void generateCode(CodeGeneratorSettings settings, TokenizerData tokenizerData) {
    staticString = (Options.getStatic() ? "static " : "");
    tokMgrClassName = cu_name + "TokenManager";

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
    settings.put("tokMgrClassName", tokMgrClassName);
    settings.put("maxLongs", tokenizerData.allMatches.size()/64 + 1);
    settings.put("cu_name", tokenizerData.parserName);
    this.settings = settings;

    PrintClassHead(tokenizerData);

// ######################### BEGIN
    LexGenCPP.reInit();
    LexGenCPP.keepLineCol = Options.getKeepLineColumn();
    List<RegularExpression> choices = new ArrayList<>();
    Enumeration<String> e;
    TokenProduction tp;
    int i, j;

    LexGenCPP.staticString = (Options.getStatic() ? "static " : "");
    LexGenCPP.tokMgrClassName = cu_name + "TokenManager";

    BuildLexStatesTable();

    e = LexGenCPP.allTpsForState.keys();

    boolean ignoring = false;

    while (e.hasMoreElements())
    {
      NfaState.ReInit();
      RStringLiteralHelper.ReInit();
      NfaAdaptor.transformed = false;

      String key = e.nextElement();

      LexGenCPP.lexStateIndex = GetIndex(key, tokenizerData);
      LexGenCPP.lexStateSuffix = "_" + LexGenCPP.lexStateIndex;
      List<TokenProduction> allTps = LexGenCPP.allTpsForState.get(key);
      LexGenCPP.initStates.put(key, LexGenCPP.initialState = new NfaState());
      ignoring = false;

      LexGenCPP.singlesToSkip[LexGenCPP.lexStateIndex] = new NfaState();
      LexGenCPP.singlesToSkip[LexGenCPP.lexStateIndex].dummy = true;

      if (key.equals("DEFAULT"))
        LexGenCPP.defaultLexState = LexGenCPP.lexStateIndex;

      for (i = 0; i < allTps.size(); i++)
      {
        tp = allTps.get(i);
        int kind = tp.kind;
        boolean ignore = tp.ignoreCase;
        List<RegExprSpec> rexps = tp.respecs;

        if (i == 0)
          ignoring = ignore;

        for (j = 0; j < rexps.size(); j++)
        {
          RegExprSpec respec = rexps.get(j);
          LexGenCPP.curRE = respec.rexp;

          LexGenCPP.rexprs[LexGenCPP.curKind = LexGenCPP.curRE.ordinal] = LexGenCPP.curRE;
          LexGenCPP.lexStates[LexGenCPP.curRE.ordinal] = LexGenCPP.lexStateIndex;
          LexGenCPP.ignoreCase[LexGenCPP.curRE.ordinal] = ignore;

          if (LexGenCPP.curRE.private_rexp)
          {
            LexGenCPP.kinds[LexGenCPP.curRE.ordinal] = -1;
            continue;
          }

          if (LexGenCPP.curRE instanceof org.javacc.parser.RStringLiteral &&
              !((org.javacc.parser.RStringLiteral)LexGenCPP.curRE).image.equals(""))
          {
            RStringLiteralHelper.GenerateDfa(codeGenerator, LexGenCPP.curRE.ordinal, (org.javacc.parser.RStringLiteral)LexGenCPP.curRE);
            if (i != 0 && !LexGenCPP.mixed[LexGenCPP.lexStateIndex] && ignoring != ignore) {
              LexGenCPP.mixed[LexGenCPP.lexStateIndex] = true;
            }
          }
          else if (LexGenCPP.curRE.CanMatchAnyChar())
          {
            if (LexGenCPP.canMatchAnyChar[LexGenCPP.lexStateIndex] == -1 ||
                LexGenCPP.canMatchAnyChar[LexGenCPP.lexStateIndex] > LexGenCPP.curRE.ordinal)
              LexGenCPP.canMatchAnyChar[LexGenCPP.lexStateIndex] = LexGenCPP.curRE.ordinal;
          }
          else
          {
            Nfa temp;

            if (LexGenCPP.curRE instanceof RChoice)
              choices.add(LexGenCPP.curRE);

            temp = NfaAdaptor.toNfa(ignore, LexGenCPP.curRE);
            temp.end.isFinal = true;
            temp.end.kind = LexGenCPP.curRE.ordinal;
            LexGenCPP.initialState.AddMove(temp.start);
          }

          if (LexGenCPP.kinds.length < LexGenCPP.curRE.ordinal)
          {
            int[] tmp = new int[LexGenCPP.curRE.ordinal + 1];

            System.arraycopy(LexGenCPP.kinds, 0, tmp, 0, LexGenCPP.kinds.length);
            LexGenCPP.kinds = tmp;
          }
          //System.out.println("   ordina : " + LexGenCPP.curRE.ordinal);

          LexGenCPP.kinds[LexGenCPP.curRE.ordinal] = kind;

          if (respec.nextState != null &&
              !respec.nextState.equals(LexGenCPP.lexStateName[LexGenCPP.lexStateIndex]))
            LexGenCPP.newLexState[LexGenCPP.curRE.ordinal] = respec.nextState;

          if (respec.act != null && respec.act.getActionTokens() != null &&
              respec.act.getActionTokens().size() > 0)
            LexGenCPP.actions[LexGenCPP.curRE.ordinal] = respec.act;

          switch(kind)
          {
          case TokenProduction.SPECIAL :
            LexGenCPP.hasSkipActions |= (LexGenCPP.actions[LexGenCPP.curRE.ordinal] != null) ||
            (LexGenCPP.newLexState[LexGenCPP.curRE.ordinal] != null);
            LexGenCPP.hasSpecial = true;
            LexGenCPP.toSpecial[LexGenCPP.curRE.ordinal / 64] |= 1L << (LexGenCPP.curRE.ordinal % 64);
            LexGenCPP.toSkip[LexGenCPP.curRE.ordinal / 64] |= 1L << (LexGenCPP.curRE.ordinal % 64);
            break;
          case TokenProduction.SKIP :
            LexGenCPP.hasSkipActions |= (LexGenCPP.actions[LexGenCPP.curRE.ordinal] != null);
            LexGenCPP.hasSkip = true;
            LexGenCPP.toSkip[LexGenCPP.curRE.ordinal / 64] |= 1L << (LexGenCPP.curRE.ordinal % 64);
            break;
          case TokenProduction.MORE :
            LexGenCPP.hasMoreActions |= (LexGenCPP.actions[LexGenCPP.curRE.ordinal] != null);
            LexGenCPP.hasMore = true;
            LexGenCPP.toMore[LexGenCPP.curRE.ordinal / 64] |= 1L << (LexGenCPP.curRE.ordinal % 64);

            if (LexGenCPP.newLexState[LexGenCPP.curRE.ordinal] != null)
              LexGenCPP.canReachOnMore[GetIndex(LexGenCPP.newLexState[LexGenCPP.curRE.ordinal], tokenizerData)] = true;
            else
              LexGenCPP.canReachOnMore[LexGenCPP.lexStateIndex] = true;

            break;
          case TokenProduction.TOKEN :
            LexGenCPP.hasTokenActions |= (LexGenCPP.actions[LexGenCPP.curRE.ordinal] != null);
            LexGenCPP.toToken[LexGenCPP.curRE.ordinal / 64] |= 1L << (LexGenCPP.curRE.ordinal % 64);
            break;
          }
        }
      }

      // Generate a static block for initializing the nfa transitions
      NfaState.ComputeClosures();

      for (i = 0; i < LexGenCPP.initialState.epsilonMoves.size(); i++)
        LexGenCPP.initialState.epsilonMoves.elementAt(i).GenerateCode();

      if (LexGenCPP.hasNfa[LexGenCPP.lexStateIndex] = (NfaState.generatedStates != 0))
      {
        LexGenCPP.initialState.GenerateCode();
        LexGenCPP.initialState.GenerateInitMoves(codeGenerator);
      }

      if (LexGenCPP.initialState.kind != Integer.MAX_VALUE && LexGenCPP.initialState.kind != 0)
      {
        if ((LexGenCPP.toSkip[LexGenCPP.initialState.kind / 64] & (1L << LexGenCPP.initialState.kind)) != 0L ||
            (LexGenCPP.toSpecial[LexGenCPP.initialState.kind / 64] & (1L << LexGenCPP.initialState.kind)) != 0L)
          LexGenCPP.hasSkipActions = true;
        else if ((LexGenCPP.toMore[LexGenCPP.initialState.kind / 64] & (1L << LexGenCPP.initialState.kind)) != 0L)
          LexGenCPP.hasMoreActions = true;
        else
          LexGenCPP.hasTokenActions = true;

        if (LexGenCPP.initMatch[LexGenCPP.lexStateIndex] == 0 ||
            LexGenCPP.initMatch[LexGenCPP.lexStateIndex] > LexGenCPP.initialState.kind)
        {
          LexGenCPP.initMatch[LexGenCPP.lexStateIndex] = LexGenCPP.initialState.kind;
          LexGenCPP.hasEmptyMatch = true;
        }
      }
      else if (LexGenCPP.initMatch[LexGenCPP.lexStateIndex] == 0)
        LexGenCPP.initMatch[LexGenCPP.lexStateIndex] = Integer.MAX_VALUE;

      RStringLiteralHelper.FillSubString();

      if (LexGenCPP.hasNfa[LexGenCPP.lexStateIndex] && !LexGenCPP.mixed[LexGenCPP.lexStateIndex]) {
        RStringLiteralHelper.GenerateNfaStartStates(codeGenerator, LexGenCPP.initialState);
        RStringLiteralHelper.DumpNfaStartStatesCode(RStringLiteralHelper.statesForPos, codeGenerator);
      }

      RStringLiteralHelper.DumpDfaCode(codeGenerator);

      if (LexGenCPP.hasNfa[LexGenCPP.lexStateIndex])
        NfaState.DumpMoveNfa(codeGenerator);

      if (LexGenCPP.stateSetSize < NfaState.generatedStates)
        LexGenCPP.stateSetSize = NfaState.generatedStates;
    }

    for (i = 0; i < choices.size(); i++)
      ((RChoice)choices.get(i)).CheckUnmatchability();
    CheckEmptyStringMatch(tokenizerData);
// ######################### END

    NfaState.DumpStateSets(codeGenerator);
    NfaState.DumpNonAsciiMoveMethods(codeGenerator);
    RStringLiteralHelper.DumpStrLiteralImages(codeGenerator);
    DumpFillToken(tokenizerData);
    DumpGetNextToken(tokenizerData);

    if (Options.getDebugTokenManager())
    {
      NfaState.DumpStatesForKind(codeGenerator);
      DumpDebugMethods(tokenizerData);
    }

    if (LexGenCPP.hasLoop)
    {
      codeGenerator.switchToStaticsFile();
      codeGenerator.genCodeLine("static int  jjemptyLineNo[" + tokenizerData.lexStateNames.length + "];");
      codeGenerator.genCodeLine("static int  jjemptyColNo[" + tokenizerData.lexStateNames.length + "];");
      codeGenerator.genCodeLine("static bool jjbeenHere[" + tokenizerData.lexStateNames.length + "];");
      codeGenerator.switchToMainFile();
    }

    if (LexGenCPP.hasSkipActions)
      DumpSkipActions(tokenizerData);
    if (LexGenCPP.hasMoreActions)
      DumpMoreActions(tokenizerData);
    if (LexGenCPP.hasTokenActions)
      DumpTokenActions(tokenizerData);

    NfaState.PrintBoilerPlateCPP(codeGenerator);

//    String charStreamName;
//    if (Options.getUserCharStream())
//      charStreamName = "CharStream";
//    else if (Options.getJavaUnicodeEscape())
//      charStreamName = "JavaCharStream";
//    else
//      charStreamName = "SimpleCharStream";

    writeTemplate("/templates/cpp/TokenManagerBoilerPlateMethods.template", "charStreamName", "CharStream",
        "parserClassName", cu_name, "defaultLexState", "defaultLexState", "lexStateNameLength", tokenizerData.lexStateNames.length);

    dumpBoilerPlateInHeader(tokenizerData);

    // in the include file close the class signature
    DumpStaticVarDeclarations(tokenizerData); // static vars actually inst

    codeGenerator.switchToIncludeFile(); // remaining variables
    writeTemplate("/templates/cpp/DumpVarDeclarations.template", "charStreamName", "CharStream", "lexStateNameLength",
        tokenizerData.lexStateNames.length);
    codeGenerator.genCodeLine(/* { */ "};");

    codeGenerator.switchToStaticsFile();
  }

  @Override
  public void finish(CodeGeneratorSettings settings, TokenizerData tokenizerData) {
    if (!Options.getBuildTokenManager())
      return;
    // TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
    String fileName = Options.getOutputDirectory() + File.separator + cu_name + "TokenManager.cc";
    codeGenerator.saveOutput(fileName);
  }

  void PrintClassHead(TokenizerData tokenizerData)
  {
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
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("/** Token Manager. */");

    String superClass = Options.stringValue(Options.USEROPTION__TOKEN_MANAGER_SUPER_CLASS);
    codeGenerator.genClassStart(null, tokMgrClassName, new String[] {},
        new String[] { "public TokenManager" + (superClass == null ? "" : ", public " + superClass) });

    if (token_mgr_decls != null && token_mgr_decls.size() > 0) {
      Token t = token_mgr_decls.get(0);
      boolean commonTokenActionSeen = false;
      boolean commonTokenActionNeeded = Options.getCommonTokenAction();

      codeGenerator.printTokenSetup(token_mgr_decls.get(0));
      codeGenerator.ccol = 1;

      codeGenerator.switchToMainFile();
      for (int j = 0; j < token_mgr_decls.size(); j++) {
        t = token_mgr_decls.get(j);
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
                + "      " + staticString + "void CommonTokenAction(Token *t)\n"
                + "in your TOKEN_MGR_DECLS. The generated token manager will not compile.");

    } else if (Options.getCommonTokenAction()) {
      JavaCCErrors.warning("You have the COMMON_TOKEN_ACTION option set. " + "But you have not defined the method :\n"
          + "      " + staticString + "void CommonTokenAction(Token *t)\n"
          + "in your TOKEN_MGR_DECLS. The generated token manager will not compile.");
    }

    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("  FILE *debugStream;");

    codeGenerator.generateMethodDefHeader("  void ", tokMgrClassName, "setDebugStream(FILE *ds)");
    codeGenerator.genCodeLine("{ debugStream = ds; }");

    codeGenerator.switchToIncludeFile();
    if(Options.getTokenManagerUsesParser()){
      codeGenerator.genCodeLine("");
      codeGenerator.genCodeLine("private:");
      codeGenerator.genCodeLine("  " + cu_name + "* parser = nullptr;");
    }
    codeGenerator.switchToMainFile();
  }

  void DumpDebugMethods(TokenizerData tokenizerData)
  {
    writeTemplate("/templates/cpp/DumpDebugMethods.template",
           "maxOrdinal", tokenizerData.images.length, 
           "stateSetSize", LexGenCPP.stateSetSize);
  }


  private void dumpBoilerPlateInHeader(TokenizerData tokenizerData) {
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
        .genCodeLine("  " + tokMgrClassName + "(JAVACC_CHARSTREAM *stream, int lexState = " + tokenizerData.defaultLexState + ");");
    codeGenerator.genCodeLine("  virtual ~" + tokMgrClassName + "();");
    codeGenerator.genCodeLine("  void ReInit(JAVACC_CHARSTREAM *stream, int lexState = " + tokenizerData.defaultLexState + ");");
    codeGenerator.genCodeLine("  void SwitchTo(int lexState);");
    codeGenerator.genCodeLine("  void clear();");
    codeGenerator.genCodeLine("  const JJSimpleString jjKindsForBitVector(int i, " + Types.getLongType() + " vec);");
    codeGenerator
        .genCodeLine("  const JJSimpleString jjKindsForStateVector(int lexState, int vec[], int start, int end);");
    codeGenerator.genCodeLine("");
  }

  private void DumpStaticVarDeclarations(TokenizerData tokenizerData) 
  {
    int i;

    codeGenerator.switchToStaticsFile(); // remaining variables
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("/** Lexer state names. */");
    codeGenerator.genStringLiteralArrayCPP("lexStateNames", tokenizerData.lexStateNames);

    if (tokenizerData.lexStateNames.length > 1)
    {
      codeGenerator.genCodeLine("");
      codeGenerator.genCodeLine("/** Lex State array. */");
      codeGenerator.genCode("static const int jjnewLexState[] = {");

      for (i = 0; i < tokenizerData.images.length; i++)
      {
        if (i % 25 == 0)
          codeGenerator.genCode("\n   ");

        if (LexGenCPP.newLexState[i] == null)
          codeGenerator.genCode("-1, ");
        else
          codeGenerator.genCode(GetIndex(LexGenCPP.newLexState[i], tokenizerData) + ", ");
      }
      codeGenerator.genCodeLine("\n};");
    }

    if (LexGenCPP.hasSkip || LexGenCPP.hasMore || LexGenCPP.hasSpecial)
    {
      // Bit vector for TOKEN
      codeGenerator.genCode("static const " + Types.getLongType() + " jjtoToken[] = {");
      for (i = 0; i < tokenizerData.images.length / 64 + 1; i++)
      {
        if (i % 4 == 0)
          codeGenerator.genCode("\n   ");
        codeGenerator.genCode("0x" + Long.toHexString(LexGenCPP.toToken[i]) + "L, ");
      }
      codeGenerator.genCodeLine("\n};");
    }

    if (LexGenCPP.hasSkip || LexGenCPP.hasSpecial)
    {
      // Bit vector for SKIP
      codeGenerator.genCode("static const " + Types.getLongType() + " jjtoSkip[] = {");
      for (i = 0; i < tokenizerData.images.length / 64 + 1; i++)
      {
        if (i % 4 == 0)
          codeGenerator.genCode("\n   ");
        codeGenerator.genCode("0x" + Long.toHexString(LexGenCPP.toSkip[i]) + "L, ");
      }
      codeGenerator.genCodeLine("\n};");
    }

    if (LexGenCPP.hasSpecial)
    {
      // Bit vector for SPECIAL
      codeGenerator.genCode("static const " + Types.getLongType() + " jjtoSpecial[] = {");
      for (i = 0; i < tokenizerData.images.length / 64 + 1; i++)
      {
        if (i % 4 == 0)
          codeGenerator.genCode("\n   ");
        codeGenerator.genCode("0x" + Long.toHexString(LexGenCPP.toSpecial[i]) + "L, ");
      }
      codeGenerator.genCodeLine("\n};");
    }
  }

  void DumpFillToken(TokenizerData tokenizerData)
  {
    final double tokenVersion = JavaFiles.getVersion("Token.java");
    final boolean hasBinaryNewToken = tokenVersion > 4.09;

    codeGenerator.generateMethodDefHeader("Token *", tokMgrClassName, "jjFillToken()");
    codeGenerator.genCodeLine("{");
    codeGenerator.genCodeLine("   Token *t;");
    codeGenerator.genCodeLine("   JJString curTokenImage;");
    if (LexGenCPP.keepLineCol)
    {
      codeGenerator.genCodeLine("   int beginLine   = -1;");
      codeGenerator.genCodeLine("   int endLine     = -1;");
      codeGenerator.genCodeLine("   int beginColumn = -1;");
      codeGenerator.genCodeLine("   int endColumn   = -1;");
    }

    if (LexGenCPP.hasEmptyMatch)
    {
      codeGenerator.genCodeLine("   if (jjmatchedPos < 0)");
      codeGenerator.genCodeLine("   {");
      codeGenerator.genCodeLine("       curTokenImage = image.c_str();");

      if (LexGenCPP.keepLineCol)
      {
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

      if (LexGenCPP.keepLineCol)
      {
        codeGenerator.genCodeLine("   if (input_stream->getTrackLineColumn()) {");
        codeGenerator.genCodeLine("      beginLine = input_stream->getBeginLine();");
        codeGenerator.genCodeLine("      beginColumn = input_stream->getBeginColumn();");
        codeGenerator.genCodeLine("      endLine = input_stream->getEndLine();");
        codeGenerator.genCodeLine("      endColumn = input_stream->getEndColumn();");
        codeGenerator.genCodeLine("   }");
      }

      codeGenerator.genCodeLine("   }");
    }
    else
    {
      codeGenerator.genCodeLine("   JJString im = jjstrLiteralImages[jjmatchedKind];");
      codeGenerator.genCodeLine("   curTokenImage = (im.length() == 0) ? input_stream->GetImage() : im;");
      if (LexGenCPP.keepLineCol)
      {
        codeGenerator.genCodeLine("   if (input_stream->getTrackLineColumn()) {");
        codeGenerator.genCodeLine("     beginLine = input_stream->getBeginLine();");
        codeGenerator.genCodeLine("     beginColumn = input_stream->getBeginColumn();");
        codeGenerator.genCodeLine("     endLine = input_stream->getEndLine();");
        codeGenerator.genCodeLine("     endColumn = input_stream->getEndColumn();");
        codeGenerator.genCodeLine("   }");
      }
    }

    if (Options.getTokenFactory().length() > 0) {
      codeGenerator.genCodeLine("   t = " + codeGenerator.getClassQualifier(Options.getTokenFactory()) + "newToken(jjmatchedKind, curTokenImage);");
    } else if (hasBinaryNewToken)
    {
      codeGenerator.genCodeLine("   t = " + codeGenerator.getClassQualifier("Token") + "newToken(jjmatchedKind, curTokenImage);");
    }
    else
    {
      codeGenerator.genCodeLine("   t = " + codeGenerator.getClassQualifier("Token") + "newToken(jjmatchedKind);");
      codeGenerator.genCodeLine("   t->kind = jjmatchedKind;");
      codeGenerator.genCodeLine("   t->image = curTokenImage;");
    }
    codeGenerator.genCodeLine("   t->specialToken = nullptr;");
    codeGenerator.genCodeLine("   t->next = nullptr;");

    if (LexGenCPP.keepLineCol) {
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

  void DumpGetNextToken(TokenizerData tokenizerData)
  {
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
    codeGenerator.genCodeLine("const int defaultLexState = " + tokenizerData.defaultLexState + ";");
    codeGenerator.genCodeLine("/** Get the next Token. */");
    codeGenerator.generateMethodDefHeader("Token *", tokMgrClassName, "getNextToken()");
    codeGenerator.genCodeLine("{");
    if (LexGenCPP.hasSpecial) {
      codeGenerator.genCodeLine("  Token *specialToken = nullptr;");
    }
    codeGenerator.genCodeLine("  Token *matchedToken = nullptr;");
    codeGenerator.genCodeLine("  int curPos = 0;");
    codeGenerator.genCodeLine("");
    codeGenerator.genCodeLine("  for (;;)");
    codeGenerator.genCodeLine("  {");
    codeGenerator.genCodeLine("   EOFLoop: ");
    //codeGenerator.genCodeLine("   {");
    //codeGenerator.genCodeLine("      curChar = input_stream->BeginToken();");
    //codeGenerator.genCodeLine("   }");
    codeGenerator.genCodeLine("   if (input_stream->endOfInput())");
    codeGenerator.genCodeLine("   {");
    //codeGenerator.genCodeLine("     input_stream->backup(1);");

    if (Options.getDebugTokenManager())
      codeGenerator.genCodeLine("      fprintf(debugStream, \"Returning the <EOF> token.\\n\");");

    codeGenerator.genCodeLine("      jjmatchedKind = 0;");
    codeGenerator.genCodeLine("      jjmatchedPos = -1;");
    codeGenerator.genCodeLine("      matchedToken = jjFillToken();");

    if (LexGenCPP.hasSpecial)
      codeGenerator.genCodeLine("      matchedToken->specialToken = specialToken;");

    if (JavaCCGlobals.nextStateForEof != null || JavaCCGlobals.actForEof != null)
      codeGenerator.genCodeLine("      TokenLexicalActions(matchedToken);");

    if (Options.getCommonTokenAction())
      codeGenerator.genCodeLine("      CommonTokenAction(matchedToken);");

    codeGenerator.genCodeLine("      return matchedToken;");
    codeGenerator.genCodeLine("   }");
    codeGenerator.genCodeLine("   curChar = input_stream->BeginToken();");

    if (LexGenCPP.hasMoreActions || LexGenCPP.hasSkipActions || LexGenCPP.hasTokenActions)
    {
      codeGenerator.genCodeLine("   image = jjimage;");
      codeGenerator.genCodeLine("   image.clear();");
      codeGenerator.genCodeLine("   jjimageLen = 0;");
    }

    codeGenerator.genCodeLine("");

    String prefix = "";
    if (LexGenCPP.hasMore)
    {
      codeGenerator.genCodeLine("   for (;;)");
      codeGenerator.genCodeLine("   {");
      prefix = "  ";
    }

    String endSwitch = "";
    String caseStr = "";
    // this also sets up the start state of the nfa
    if (tokenizerData.lexStateNames.length > 1)
    {
      codeGenerator.genCodeLine(prefix + "   switch(curLexState)");
      codeGenerator.genCodeLine(prefix + "   {");
      endSwitch = prefix + "   }";
      caseStr = prefix + "     case ";
      prefix += "    ";
    }

    prefix += "   ";
    for(i = 0; i < tokenizerData.lexStateNames.length; i++)
    {
      if (tokenizerData.lexStateNames.length > 1)
        codeGenerator.genCodeLine(caseStr + i + ":");

      if (LexGenCPP.singlesToSkip[i].HasTransitions())
      {
        // added the backup(0) to make JIT happy
        codeGenerator.genCodeLine(prefix + "{ input_stream->backup(0);");
        if (LexGenCPP.singlesToSkip[i].asciiMoves[0] != 0L && LexGenCPP.singlesToSkip[i].asciiMoves[1] != 0L) {
          codeGenerator.genCodeLine(
              prefix + "   while ((curChar < 64" + " && (0x" + Long.toHexString(LexGenCPP.singlesToSkip[i].asciiMoves[0])
                  + "L & (1L << curChar)) != 0L) || \n" + prefix + "          (curChar >> 6) == 1" + " && (0x"
                  + Long.toHexString(LexGenCPP.singlesToSkip[i].asciiMoves[1]) + "L & (1L << (curChar & 077))) != 0L)");
        } else if (LexGenCPP.singlesToSkip[i].asciiMoves[1] == 0L) {
          codeGenerator.genCodeLine(prefix + "   while (curChar <= " + (int) MaxChar(LexGenCPP.singlesToSkip[i].asciiMoves[0])
              + " && (0x" + Long.toHexString(LexGenCPP.singlesToSkip[i].asciiMoves[0]) + "L & (1L << curChar)) != 0L)");
        } else if (LexGenCPP.singlesToSkip[i].asciiMoves[0] == 0L) {
          codeGenerator.genCodeLine(prefix + "   while (curChar > 63 && curChar <= "
              + (MaxChar(LexGenCPP.singlesToSkip[i].asciiMoves[1]) + 64) + " && (0x"
              + Long.toHexString(LexGenCPP.singlesToSkip[i].asciiMoves[1]) + "L & (1L << (curChar & 077))) != 0L)");
        }

        codeGenerator.genCodeLine(prefix + "{");
        if (Options.getDebugTokenManager())
        {
          if (tokenizerData.lexStateNames.length > 1) {
            codeGenerator.genCodeLine("      fprintf(debugStream, \"<%s>\" , addUnicodeEscapes(lexStateNames[curLexState]).c_str());");
          }

          codeGenerator.genCodeLine("      fprintf(debugStream, \"Skipping character : %c(%d)\\n\", curChar, (int)curChar);");
        }

        codeGenerator.genCodeLine(prefix + "if (input_stream->endOfInput()) { goto EOFLoop; }");
        codeGenerator.genCodeLine(prefix + "curChar = input_stream->BeginToken();");
        codeGenerator.genCodeLine(prefix + "}");
        codeGenerator.genCodeLine(prefix + "}");
      }

      if (LexGenCPP.initMatch[i] != Integer.MAX_VALUE && LexGenCPP.initMatch[i] != 0)
      {
        if (Options.getDebugTokenManager())
          codeGenerator.genCodeLine("      fprintf(debugStream, \"   Matched the empty string as %s token.\\n\", addUnicodeEscapes(tokenImage[" + LexGenCPP.initMatch[i] + "]).c_str());");

        codeGenerator.genCodeLine(prefix + "jjmatchedKind = " + LexGenCPP.initMatch[i] + ";");
        codeGenerator.genCodeLine(prefix + "jjmatchedPos = -1;");
        codeGenerator.genCodeLine(prefix + "curPos = 0;");
      }
      else
      {
        codeGenerator.genCodeLine(prefix + "jjmatchedKind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");
        codeGenerator.genCodeLine(prefix + "jjmatchedPos = 0;");
      }

      if (Options.getDebugTokenManager()) {
        codeGenerator.genCodeLine("   fprintf(debugStream, " +
          "\"<%s>Current character : %c(%d) at line %d column %d\\n\","+
          "addUnicodeEscapes(lexStateNames[curLexState]).c_str(), curChar, (int)curChar, " +
          "input_stream->getEndLine(), input_stream->getEndColumn());");
      }

      codeGenerator.genCodeLine(prefix + "curPos = jjMoveStringLiteralDfa0_" + i + "();");

      if (LexGenCPP.canMatchAnyChar[i] != -1)
      {
        if (LexGenCPP.initMatch[i] != Integer.MAX_VALUE && LexGenCPP.initMatch[i] != 0)
          codeGenerator.genCodeLine(prefix + "if (jjmatchedPos < 0 || (jjmatchedPos == 0 && jjmatchedKind > " +
              LexGenCPP.canMatchAnyChar[i] + "))");
        else
          codeGenerator.genCodeLine(prefix + "if (jjmatchedPos == 0 && jjmatchedKind > " +
              LexGenCPP.canMatchAnyChar[i] + ")");
        codeGenerator.genCodeLine(prefix + "{");

        if (Options.getDebugTokenManager()) {
          codeGenerator.genCodeLine("           fprintf(debugStream, \"   Current character matched as a %s token.\\n\", addUnicodeEscapes(tokenImage[" + LexGenCPP.canMatchAnyChar[i] + "]).c_str());");
        }
        codeGenerator.genCodeLine(prefix + "   jjmatchedKind = " + LexGenCPP.canMatchAnyChar[i] + ";");

        if (LexGenCPP.initMatch[i] != Integer.MAX_VALUE && LexGenCPP.initMatch[i] != 0)
          codeGenerator.genCodeLine(prefix + "   jjmatchedPos = 0;");

        codeGenerator.genCodeLine(prefix + "}");
      }

      if (tokenizerData.lexStateNames.length > 1)
        codeGenerator.genCodeLine(prefix + "break;");
    }

    if (tokenizerData.lexStateNames.length > 1)
      codeGenerator.genCodeLine(endSwitch);
    else if (tokenizerData.lexStateNames.length == 0)
      codeGenerator.genCodeLine("       jjmatchedKind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");

    if (tokenizerData.lexStateNames.length > 1)
      prefix = "  ";
    else
      prefix = "";

    if (tokenizerData.lexStateNames.length > 0)
    {
      codeGenerator.genCodeLine(prefix + "   if (jjmatchedKind != 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
      codeGenerator.genCodeLine(prefix + "   {");
      codeGenerator.genCodeLine(prefix + "      if (jjmatchedPos + 1 < curPos)");

      if (Options.getDebugTokenManager()) {
        codeGenerator.genCodeLine(prefix + "      {");
        codeGenerator.genCodeLine(prefix + "         fprintf(debugStream, " +
        "\"   Putting back %d characters into the input stream.\\n\", (curPos - jjmatchedPos - 1));");
      }

      codeGenerator.genCodeLine(prefix + "         input_stream->backup(curPos - jjmatchedPos - 1);");

      if (Options.getDebugTokenManager()) {
        codeGenerator.genCodeLine(prefix + "      }");
      }

      if (Options.getDebugTokenManager())
      {
          codeGenerator.genCodeLine("    fprintf(debugStream, " +
              "\"****** FOUND A %d(%s) MATCH (%s) ******\\n\", jjmatchedKind, addUnicodeEscapes(tokenImage[jjmatchedKind]).c_str(), addUnicodeEscapes(input_stream->GetSuffix(jjmatchedPos + 1)).c_str());");
      }

      if (LexGenCPP.hasSkip || LexGenCPP.hasMore || LexGenCPP.hasSpecial)
      {
        codeGenerator.genCodeLine(prefix + "      if ((jjtoToken[jjmatchedKind >> 6] & " +
        "(1L << (jjmatchedKind & 077))) != 0L)");
        codeGenerator.genCodeLine(prefix + "      {");
      }

      codeGenerator.genCodeLine(prefix + "         matchedToken = jjFillToken();");

      if (LexGenCPP.hasSpecial)
        codeGenerator.genCodeLine(prefix + "         matchedToken->specialToken = specialToken;");

      if (LexGenCPP.hasTokenActions)
        codeGenerator.genCodeLine(prefix + "         TokenLexicalActions(matchedToken);");

      if (tokenizerData.lexStateNames.length > 1)
      {
        codeGenerator.genCodeLine("       if (jjnewLexState[jjmatchedKind] != -1)");
        codeGenerator.genCodeLine(prefix + "       curLexState = jjnewLexState[jjmatchedKind];");
      }

      if (Options.getCommonTokenAction())
        codeGenerator.genCodeLine(prefix + "         CommonTokenAction(matchedToken);");

      codeGenerator.genCodeLine(prefix + "         return matchedToken;");

      if (LexGenCPP.hasSkip || LexGenCPP.hasMore || LexGenCPP.hasSpecial)
      {
        codeGenerator.genCodeLine(prefix + "      }");

        if (LexGenCPP.hasSkip || LexGenCPP.hasSpecial)
        {
          if (LexGenCPP.hasMore)
          {
            codeGenerator.genCodeLine(prefix + "      else if ((jjtoSkip[jjmatchedKind >> 6] & " +
            "(1L << (jjmatchedKind & 077))) != 0L)");
          }
          else
            codeGenerator.genCodeLine(prefix + "      else");

          codeGenerator.genCodeLine(prefix + "      {");

          if (LexGenCPP.hasSpecial)
          {
            codeGenerator.genCodeLine(prefix + "         if ((jjtoSpecial[jjmatchedKind >> 6] & " +
            "(1L << (jjmatchedKind & 077))) != 0L)");
            codeGenerator.genCodeLine(prefix + "         {");

            codeGenerator.genCodeLine(prefix + "            matchedToken = jjFillToken();");

            codeGenerator.genCodeLine(prefix + "            if (specialToken == nullptr)");
            codeGenerator.genCodeLine(prefix + "               specialToken = matchedToken;");
            codeGenerator.genCodeLine(prefix + "            else");
            codeGenerator.genCodeLine(prefix + "            {");
            codeGenerator.genCodeLine(prefix + "               matchedToken->specialToken = specialToken;");
            codeGenerator.genCodeLine(prefix + "               specialToken = (specialToken->next = matchedToken);");
            codeGenerator.genCodeLine(prefix + "            }");

            if (LexGenCPP.hasSkipActions)
              codeGenerator.genCodeLine(prefix + "            SkipLexicalActions(matchedToken);");

            codeGenerator.genCodeLine(prefix + "         }");

            if (LexGenCPP.hasSkipActions)
            {
              codeGenerator.genCodeLine(prefix + "         else");
              codeGenerator.genCodeLine(prefix + "            SkipLexicalActions(nullptr);");
            }
          }
          else if (LexGenCPP.hasSkipActions)
            codeGenerator.genCodeLine(prefix + "         SkipLexicalActions(nullptr);");

          if (tokenizerData.lexStateNames.length > 1)
          {
            codeGenerator.genCodeLine("         if (jjnewLexState[jjmatchedKind] != -1)");
            codeGenerator.genCodeLine(prefix + "         curLexState = jjnewLexState[jjmatchedKind];");
          }

          codeGenerator.genCodeLine(prefix + "         goto EOFLoop;");
          codeGenerator.genCodeLine(prefix + "      }");
        }

        if (LexGenCPP.hasMore)
        {
          if (LexGenCPP.hasMoreActions)
            codeGenerator.genCodeLine(prefix + "      MoreLexicalActions();");
          else if (LexGenCPP.hasSkipActions || LexGenCPP.hasTokenActions)
            codeGenerator.genCodeLine(prefix + "      jjimageLen += jjmatchedPos + 1;");

          if (tokenizerData.lexStateNames.length > 1)
          {
            codeGenerator.genCodeLine("      if (jjnewLexState[jjmatchedKind] != -1)");
            codeGenerator.genCodeLine(prefix + "      curLexState = jjnewLexState[jjmatchedKind];");
          }
          codeGenerator.genCodeLine(prefix + "      curPos = 0;");
          codeGenerator.genCodeLine(prefix + "      jjmatchedKind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");

          codeGenerator.genCodeLine(prefix + "   if (!input_stream->endOfInput()) {");
          codeGenerator.genCodeLine(prefix + "         curChar = input_stream->readChar();");

          if (Options.getDebugTokenManager()) {
            codeGenerator.genCodeLine("   fprintf(debugStream, " +
             "\"<%s>Current character : %c(%d) at line %d column %d\\n\","+
             "addUnicodeEscapes(lexStateNames[curLexState]).c_str(), curChar, (int)curChar, " +
             "input_stream->getEndLine(), input_stream->getEndColumn());");
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
      codeGenerator.genCodeLine(prefix + "   errorHandler->lexicalError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, this);");
    }

    if (LexGenCPP.hasMore)
      codeGenerator.genCodeLine(prefix + " }");

    codeGenerator.genCodeLine("  }");
    codeGenerator.genCodeLine("}");
    codeGenerator.genCodeLine("");
  }

  public void DumpSkipActions(TokenizerData tokenizerData)
  {
    Action act;

    codeGenerator.generateMethodDefHeader("void ", tokMgrClassName, "SkipLexicalActions(Token *matchedToken)");
    codeGenerator.genCodeLine("{");
    codeGenerator.genCodeLine("   switch(jjmatchedKind)");
    codeGenerator.genCodeLine("   {");

    Outer:
      for (int i = 0; i < tokenizerData.images.length; i++)
      {
        if ((LexGenCPP.toSkip[i / 64] & (1L << (i % 64))) == 0L)
          continue;

        for (;;)
        {
          if (((act = LexGenCPP.actions[i]) == null ||
              act.getActionTokens() == null ||
              act.getActionTokens().size() == 0) && !LexGenCPP.canLoop[LexGenCPP.lexStates[i]])
            continue Outer;

          codeGenerator.genCodeLine("      case " + i + " : {");

          if (LexGenCPP.initMatch[LexGenCPP.lexStates[i]] == i && LexGenCPP.canLoop[LexGenCPP.lexStates[i]])
          {
            codeGenerator.genCodeLine("         if (jjmatchedPos == -1)");
            codeGenerator.genCodeLine("         {");
            codeGenerator.genCodeLine("            if (jjbeenHere[" + LexGenCPP.lexStates[i] + "] &&");
            codeGenerator.genCodeLine("                jjemptyLineNo[" + LexGenCPP.lexStates[i] + "] == input_stream->getBeginLine() &&");
            codeGenerator.genCodeLine("                jjemptyColNo[" + LexGenCPP.lexStates[i] + "] == input_stream->getBeginColumn())");
            codeGenerator.genCodeLine("               errorHandler->lexicalError(JJString(\"(\"Error: Bailing out of infinite loop caused by repeated empty string matches \" + \"at line \" + input_stream->getBeginLine() + \", \" + \"column \" + input_stream->getBeginColumn() + \".\")), this);");
            codeGenerator.genCodeLine("            jjemptyLineNo[" + LexGenCPP.lexStates[i] + "] = input_stream->getBeginLine();");
            codeGenerator.genCodeLine("            jjemptyColNo[" + LexGenCPP.lexStates[i] + "] = input_stream->getBeginColumn();");
            codeGenerator.genCodeLine("            jjbeenHere[" + LexGenCPP.lexStates[i] + "] = true;");
            codeGenerator.genCodeLine("         }");
          }

          if ((act = LexGenCPP.actions[i]) == null ||
              act.getActionTokens().size() == 0)
            break;

          codeGenerator.genCode(  "         image.append");
          if (RStringLiteralHelper.allImages[i] != null) {
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

  public void DumpMoreActions(TokenizerData tokenizerData)
  {
    Action act;

    codeGenerator.generateMethodDefHeader("void ", tokMgrClassName, "MoreLexicalActions()");
    codeGenerator.genCodeLine("{");
    codeGenerator.genCodeLine("   jjimageLen += (lengthOfMatch = jjmatchedPos + 1);");
    codeGenerator.genCodeLine("   switch(jjmatchedKind)");
    codeGenerator.genCodeLine("   {");

    Outer:
    for (int i = 0; i < tokenizerData.images.length; i++)
    {
      if ((LexGenCPP.toMore[i / 64] & (1L << (i % 64))) == 0L)
        continue;

      for (;;)
      {
        if (((act = LexGenCPP.actions[i]) == null ||
            act.getActionTokens() == null ||
            act.getActionTokens().size() == 0) && !LexGenCPP.canLoop[LexGenCPP.lexStates[i]])
          continue Outer;

        codeGenerator.genCodeLine("      case " + i + " : {");

        if (LexGenCPP.initMatch[LexGenCPP.lexStates[i]] == i && LexGenCPP.canLoop[LexGenCPP.lexStates[i]])
        {
          codeGenerator.genCodeLine("         if (jjmatchedPos == -1)");
          codeGenerator.genCodeLine("         {");
          codeGenerator.genCodeLine("            if (jjbeenHere[" + LexGenCPP.lexStates[i] + "] &&");
          codeGenerator
              .genCodeLine("                jjemptyLineNo[" + LexGenCPP.lexStates[i] + "] == input_stream->getBeginLine() &&");
          codeGenerator
              .genCodeLine("                jjemptyColNo[" + LexGenCPP.lexStates[i] + "] == input_stream->getBeginColumn())");
          codeGenerator.genCodeLine(
              "               errorHandler->lexicalError(JJString(\"(\"Error: Bailing out of infinite loop caused by repeated empty string matches \" + \"at line \" + input_stream->getBeginLine() + \", \" + \"column \" + input_stream->getBeginColumn() + \".\")), this);");
          codeGenerator.genCodeLine("            jjemptyLineNo[" + LexGenCPP.lexStates[i] + "] = input_stream->getBeginLine();");
          codeGenerator.genCodeLine("            jjemptyColNo[" + LexGenCPP.lexStates[i] + "] = input_stream->getBeginColumn();");
          codeGenerator.genCodeLine("            jjbeenHere[" + LexGenCPP.lexStates[i] + "] = true;");
          codeGenerator.genCodeLine("         }");
        }

        if ((act = LexGenCPP.actions[i]) == null ||
            act.getActionTokens().size() == 0)
        {
          break;
        }

        codeGenerator.genCode("         image.append");

        if (RStringLiteralHelper.allImages[i] != null)
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

  public void DumpTokenActions(TokenizerData tokenizerData)
  {
    Action act;
    int i;

    codeGenerator.generateMethodDefHeader("void ", tokMgrClassName, "TokenLexicalActions(Token *matchedToken)");
    codeGenerator.genCodeLine("{");
    codeGenerator.genCodeLine("   switch(jjmatchedKind)");
    codeGenerator.genCodeLine("   {");

    Outer:
    for (i = 0; i < tokenizerData.images.length; i++)
    {
      if ((LexGenCPP.toToken[i / 64] & (1L << (i % 64))) == 0L)
        continue;

      for (;;)
      {
        if (((act = LexGenCPP.actions[i]) == null ||
            act.getActionTokens() == null ||
            act.getActionTokens().size() == 0) && !LexGenCPP.canLoop[LexGenCPP.lexStates[i]])
          continue Outer;

        codeGenerator.genCodeLine("      case " + i + " : {");

        if (LexGenCPP.initMatch[LexGenCPP.lexStates[i]] == i && LexGenCPP.canLoop[LexGenCPP.lexStates[i]])
        {
          codeGenerator.genCodeLine("         if (jjmatchedPos == -1)");
          codeGenerator.genCodeLine("         {");
          codeGenerator.genCodeLine("            if (jjbeenHere[" + LexGenCPP.lexStates[i] + "] &&");
          codeGenerator.genCodeLine("                jjemptyLineNo[" + LexGenCPP.lexStates[i] + "] == input_stream->getBeginLine() &&");
          codeGenerator.genCodeLine("                jjemptyColNo[" + LexGenCPP.lexStates[i] + "] == input_stream->getBeginColumn())");
          codeGenerator.genCodeLine("               errorHandler->lexicalError(JJString(\"Error: Bailing out of infinite loop caused by repeated empty string matches " + "at line \" + input_stream->getBeginLine() + \", " + "column \" + input_stream->getBeginColumn() + \".\"), this);");
          codeGenerator.genCodeLine("            jjemptyLineNo[" + LexGenCPP.lexStates[i] + "] = input_stream->getBeginLine();");
          codeGenerator.genCodeLine("            jjemptyColNo[" + LexGenCPP.lexStates[i] + "] = input_stream->getBeginColumn();");
          codeGenerator.genCodeLine("            jjbeenHere[" + LexGenCPP.lexStates[i] + "] = true;");
          codeGenerator.genCodeLine("         }");
        }

        if ((act = LexGenCPP.actions[i]) == null ||
            act.getActionTokens().size() == 0)
          break;

        if (i == 0)
        {
          codeGenerator.genCodeLine("      image.setLength(0);"); // For EOF no image is there
        }
        else
        {
          codeGenerator.genCode("        image.append");

          if (RStringLiteralHelper.allImages[i] != null) {
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

  static int GetIndex(String name, TokenizerData tokenizerData) {
    for (int i = 0; i < tokenizerData.lexStateNames.length; i++)
      if (tokenizerData.lexStateNames[i] != null && tokenizerData.lexStateNames[i].equals(name))
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
  protected void writeTemplate(String name, Object... additionalOptions)
  {
    Map<String, Object> options = new HashMap<>(settings);

    for (int i = 0; i < additionalOptions.length; i++)
    {
      Object o = additionalOptions[i];

      if (o instanceof Map<?,?>)
      {
        options.putAll((Map<String,Object>) o);
      }
      else
      {
        if (i == additionalOptions.length - 1)
          throw new IllegalArgumentException("Must supply pairs of [name value] args");

        options.put((String) o, additionalOptions[i+1]);
        i++;
      }
    }

    OutputFileGenerator gen = new OutputFileGenerator(name, options);
    StringWriter sw = new StringWriter();
    try {
      gen.generate(new PrintWriter(sw));
      sw.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    codeGenerator.genCode(sw.toString());
  }


// ####################### END
  void BuildLexStatesTable()
  {
    Iterator<TokenProduction> it = rexprlist.iterator();
    TokenProduction tp;
    int i;

    String[] tmpLexStateName = new String[lexstate_I2S.size()];
    while (it.hasNext())
    {
      tp = it.next();
      List<RegExprSpec> respecs = tp.respecs;
      List<TokenProduction> tps;

      for (i = 0; i < tp.lexStates.length; i++)
      {
        if ((tps = LexGenCPP.allTpsForState.get(tp.lexStates[i])) == null)
        {
          tmpLexStateName[LexGenCPP.maxLexStates++] = tp.lexStates[i];
          LexGenCPP.allTpsForState.put(tp.lexStates[i], tps = new ArrayList<>());
        }

        tps.add(tp);
      }

      if (respecs == null || respecs.size() == 0)
        continue;

      RegularExpression re;
      for (i = 0; i < respecs.size(); i++)
        if (LexGenCPP.maxOrdinal <= (re = respecs.get(i).rexp).ordinal)
          LexGenCPP.maxOrdinal = re.ordinal + 1;
    }

    LexGenCPP.kinds = new int[LexGenCPP.maxOrdinal];
    LexGenCPP.toSkip = new long[LexGenCPP.maxOrdinal / 64 + 1];
    LexGenCPP.toSpecial = new long[LexGenCPP.maxOrdinal / 64 + 1];
    LexGenCPP.toMore = new long[LexGenCPP.maxOrdinal / 64 + 1];
    LexGenCPP.toToken = new long[LexGenCPP.maxOrdinal / 64 + 1];
    LexGenCPP.toToken[0] = 1L;
    LexGenCPP.actions = new Action[LexGenCPP.maxOrdinal];
    LexGenCPP.actions[0] = actForEof;
    LexGenCPP.hasTokenActions = actForEof != null;
    LexGenCPP.initStates = new Hashtable<>();
    LexGenCPP.canMatchAnyChar = new int[LexGenCPP.maxLexStates];
    LexGenCPP.canLoop = new boolean[LexGenCPP.maxLexStates];

    LexGenCPP.lexStateName = new String[LexGenCPP.maxLexStates];
    LexGenCPP.singlesToSkip = new NfaState[LexGenCPP.maxLexStates];
    //System.arraycopy(tmpLexStateName, 0, lexStateName, 0, LexGenCPP.maxLexStates);

    for (int l: lexstate_I2S.keySet()) {
      LexGenCPP.lexStateName[l] = lexstate_I2S.get(l);
    }

    for (i = 0; i < LexGenCPP.maxLexStates; i++)
      LexGenCPP.canMatchAnyChar[i] = -1;

    LexGenCPP.hasNfa = new boolean[LexGenCPP.maxLexStates];
    LexGenCPP.mixed = new boolean[LexGenCPP.maxLexStates];
    LexGenCPP.maxLongsReqd = new int[LexGenCPP.maxLexStates];
    LexGenCPP.initMatch = new int[LexGenCPP.maxLexStates];
    LexGenCPP.newLexState = new String[LexGenCPP.maxOrdinal];
    LexGenCPP.newLexState[0] = nextStateForEof;
    LexGenCPP.hasEmptyMatch = false;
    LexGenCPP.lexStates = new int[LexGenCPP.maxOrdinal];
    LexGenCPP.ignoreCase = new boolean[LexGenCPP.maxOrdinal];
    LexGenCPP.rexprs = new RegularExpression[LexGenCPP.maxOrdinal];
    RStringLiteralHelper.allImages = new String[LexGenCPP.maxOrdinal];
    LexGenCPP.canReachOnMore = new boolean[LexGenCPP.maxLexStates];
  }

  void AddCharToSkip(char c, int kind)
  {
    LexGenCPP.singlesToSkip[LexGenCPP.lexStateIndex].AddChar(c);
    LexGenCPP.singlesToSkip[LexGenCPP.lexStateIndex].kind = kind;
  }

  public void CheckEmptyStringMatch(TokenizerData tokenizerData)
  {
    int i, j, k, len;
    boolean[] seen = new boolean[LexGenCPP.maxLexStates];
    boolean[] done = new boolean[LexGenCPP.maxLexStates];
    String cycle;
    String reList;

    Outer:
      for (i = 0; i < LexGenCPP.maxLexStates; i++)
      {
        if (done[i] || LexGenCPP.initMatch[i] == 0 || LexGenCPP.initMatch[i] == Integer.MAX_VALUE ||
            LexGenCPP.canMatchAnyChar[i] != -1)
          continue;

        done[i] = true;
        len = 0;
        cycle = "";
        reList = "";

        for (k = 0; k < LexGenCPP.maxLexStates; k++)
          seen[k] = false;

        j = i;
        seen[i] = true;
        cycle += LexGenCPP.lexStateName[j] + "-->";
        while (LexGenCPP.newLexState[LexGenCPP.initMatch[j]] != null)
        {
          cycle += LexGenCPP.newLexState[LexGenCPP.initMatch[j]];
          if (seen[j = GetIndex(LexGenCPP.newLexState[LexGenCPP.initMatch[j]], tokenizerData)])
            break;

          cycle += "-->";
          done[j] = true;
          seen[j] = true;
          if (LexGenCPP.initMatch[j] == 0 || LexGenCPP.initMatch[j] == Integer.MAX_VALUE ||
              LexGenCPP.canMatchAnyChar[j] != -1)
            continue Outer;
          if (len != 0)
            reList += "; ";
          reList += "line " + LexGenCPP.rexprs[LexGenCPP.initMatch[j]].getLine() + ", column " +
          LexGenCPP.rexprs[LexGenCPP.initMatch[j]].getColumn();
          len++;
        }

        if (LexGenCPP.newLexState[LexGenCPP.initMatch[j]] == null)
          cycle += LexGenCPP.lexStateName[LexGenCPP.lexStates[LexGenCPP.initMatch[j]]];

        for (k = 0; k < LexGenCPP.maxLexStates; k++)
          LexGenCPP.canLoop[k] |= seen[k];

        LexGenCPP.hasLoop = true;
        if (len == 0)
          JavaCCErrors.warning(LexGenCPP.rexprs[LexGenCPP.initMatch[i]],
              "Regular expression" + ((LexGenCPP.rexprs[LexGenCPP.initMatch[i]].label.equals(""))
                  ? "" : (" for " + LexGenCPP.rexprs[LexGenCPP.initMatch[i]].label)) +
                  " can be matched by the empty string (\"\") in lexical state " +
                  LexGenCPP.lexStateName[i] + ". This can result in an endless loop of " +
          "empty string matches.");
        else
        {
          JavaCCErrors.warning(LexGenCPP.rexprs[LexGenCPP.initMatch[i]],
              "Regular expression" + ((LexGenCPP.rexprs[LexGenCPP.initMatch[i]].label.equals(""))
                  ? "" : (" for " + LexGenCPP.rexprs[LexGenCPP.initMatch[i]].label)) +
                  " can be matched by the empty string (\"\") in lexical state " +
                  LexGenCPP.lexStateName[i] + ". This regular expression along with the " +
                  "regular expressions at " + reList + " forms the cycle \n   " +
                  cycle + "\ncontaining regular expressions with empty matches." +
          " This can result in an endless loop of empty string matches.");
        }
      }
  }
}
