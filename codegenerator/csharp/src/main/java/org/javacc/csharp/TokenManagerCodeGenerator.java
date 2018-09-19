package org.javacc.csharp;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.CodeGenHelper;
import org.javacc.parser.Options;
import org.javacc.parser.TokenizerData;

/**
 * Class that implements a table driven code generator for the token manager in
 * java.
 */
public class TokenManagerCodeGenerator implements org.javacc.parser.TokenManagerCodeGenerator {
  private static final String tokenManagerTemplate =
      "/templates/csharp/TokenManagerDriver.template";
  private final CodeGenHelper codeGenerator = new CodeGenHelper();

  @Override
  public void generateCode(CodeGeneratorSettings settings, TokenizerData tokenizerData) {
    String superClass = (String)settings.get(
                             Options.USEROPTION__TOKEN_MANAGER_SUPER_CLASS);
    settings.put("maxOrdinal", tokenizerData.allMatches.size());
    settings.put("maxLexStates", tokenizerData.lexStateNames.length);
    settings.put("nfaSize", tokenizerData.nfa.size());
    settings.put("charsVectorSize", ((Character.MAX_VALUE >> 6) + 1));
    settings.put("stateSetSize", tokenizerData.nfa.size());
    settings.put("parserName", tokenizerData.parserName);
    settings.put("maxLongs", tokenizerData.allMatches.size()/64 + 1);
    settings.put("parserName", tokenizerData.parserName);
    settings.put("charStreamName", "ICharStream");
    settings.put("defaultLexState", tokenizerData.defaultLexState);
    settings.put("decls", tokenizerData.decls);
    settings.put("superClass", (superClass == null || superClass.equals(""))
                      ? "" : " :  " + superClass);
    settings.put("noDfa", Options.getNoDfa());
    if (Options.getNamespace() != null) {
      settings.put("NAMESPACE", Options.getNamespace());
    }
    settings.put("generatedStates", tokenizerData.nfa.size());
    try {
      if (Options.getNamespace() != null) {
        codeGenerator.genCodeLine("namespace " + Options.getNamespace() + " {\n");
      }

      generateConstantsClass(tokenizerData);

      codeGenerator.writeTemplate(tokenManagerTemplate, settings);
      dumpDfaTables(codeGenerator, tokenizerData);
      dumpNfaTables(codeGenerator, tokenizerData);
      dumpMatchInfo(codeGenerator, tokenizerData);
      codeGenerator.genCode("static " + tokenizerData.parserName + "TokenManager() {\n  InitStringLiteralData();\n  InitNfaData(); } ");
    } catch(IOException ioe) {
      assert(false);
    }
  }

  @Override
  public void finish(CodeGeneratorSettings settings, TokenizerData tokenizerData) {
    // TODO(sreeni) : Fix this mess.
    codeGenerator.genCodeLine("\n}");

    if (Options.getNamespace() != null) {
      codeGenerator.genCodeLine("\n}");
    }
    if (!Options.getBuildTokenManager()) return;
    String fileName = Options.getOutputDirectory() + File.separator +
                      tokenizerData.parserName + "TokenManager.cs";
    codeGenerator.saveOutput(fileName);
  }

  private void dumpDfaTables(
      CodeGenHelper codeGenerator, TokenizerData tokenizerData) {
    Map<Integer, int[]> startAndSize = new HashMap<Integer, int[]>();
    int i = 0;

    codeGenerator.genCodeLine(
        "private static readonly int[] stringLiterals = {");
    for (int key : tokenizerData.literalSequence.keySet()) {
      int[] arr = new int[2];
      List<String> l = tokenizerData.literalSequence.get(key);
      List<Integer> kinds = tokenizerData.literalKinds.get(key);
      arr[0] = i;
      arr[1] = l.size();
      int j = 0;
      if (i > 0) codeGenerator.genCodeLine(", ");
      for (String s : l) {
        if (j > 0) codeGenerator.genCodeLine(", ");
        codeGenerator.genCode(s.length());
        for (int k = 0; k < s.length(); k++) {
          codeGenerator.genCode(", ");
          codeGenerator.genCode((int)s.charAt(k));
          i++;
        }
        int kind = kinds.get(j);
        codeGenerator.genCode(", " + kind);
        codeGenerator.genCode(
            ", " + tokenizerData.kindToNfaStartState.get(kind));
        i += 3;
        j++;
      }
      startAndSize.put(key, arr);
    }
    codeGenerator.genCodeLine("};");

    // Static block to actually initialize the map from the int array above.
    codeGenerator.genCodeLine("static void InitStringLiteralData() {");
    for (int key : tokenizerData.literalSequence.keySet()) {
      int[] arr = startAndSize.get(key);
      codeGenerator.genCodeLine("startAndSize[" + key + "] = new int[]{" +
                                 arr[0] + ", " + arr[1] + "};");
    }
    codeGenerator.genCodeLine("}");
  }

  private void dumpNfaTables(
      CodeGenHelper codeGenerator, TokenizerData tokenizerData) {
    // WE do the following for java so that the generated code is reasonable
    // size and can be compiled. May not be needed for other languages.
    codeGenerator.genCodeLine("private static readonly long[][] jjCharData = {");
    Map<Integer, TokenizerData.NfaState> nfa = tokenizerData.nfa;
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) codeGenerator.genCodeLine(",");
      if (tmp == null) {
        codeGenerator.genCode("new long[] {}");
        continue;
      }
      codeGenerator.genCode("new long[] {");
      BitSet bits = new BitSet();
      for (char c : tmp.characters) {
        bits.set(c);
      }
      long[] longs = bits.toLongArray();
      for (int k = 0; k < longs.length; k++) {
        int rep = 1;
        while (k + rep < longs.length && longs[k + rep] == longs[k]) rep++;
        if (k > 0) codeGenerator.genCode(", ");
        codeGenerator.genCode(rep + ", ");
        //codeGenerator.genCode("0x" + Long.toHexString(longs[k]) + "L");
        codeGenerator.genCode("" + Long.toString(longs[k]) + "L");
        k += rep - 1;
      }
      codeGenerator.genCode("}");
    }
    codeGenerator.genCodeLine("};");

    codeGenerator.genCodeLine(
        "private static readonly int[][] jjcompositeState = {");
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) codeGenerator.genCodeLine(", ");
      if (tmp == null) {
        codeGenerator.genCode("new int[]{}");
        continue;
      }
      codeGenerator.genCode("new int[]{");
      int k = 0;
      for (int st : tmp.compositeStates) {
        if (k++ > 0) codeGenerator.genCode(", ");
        codeGenerator.genCode(st);
      }
      codeGenerator.genCode("}");
    }
    codeGenerator.genCodeLine("};");

    codeGenerator.genCodeLine("private static readonly int[] jjmatchKinds = {");
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) codeGenerator.genCodeLine(", ");
      // TODO(sreeni) : Fix this mess.
      if (tmp == null) {
        codeGenerator.genCode(Integer.MAX_VALUE);
        continue;
      }
      codeGenerator.genCode(tmp.kind);
    }
    codeGenerator.genCodeLine("};");

    codeGenerator.genCodeLine(
        "private static readonly int[][]  jjnextStateSet = {");
    for (int i = 0; i < nfa.size(); i++) {
      TokenizerData.NfaState tmp = nfa.get(i);
      if (i > 0) codeGenerator.genCodeLine(", ");
      if (tmp == null) {
        codeGenerator.genCode("new int[]{}");
        continue;
      }
      int k = 0;
      codeGenerator.genCode("new int[]{");
      for (int s : tmp.nextStates) {
        if (k++ > 0) codeGenerator.genCode(", ");
        codeGenerator.genCode(s);
      }
      codeGenerator.genCode("}");
    }
    codeGenerator.genCodeLine("};");

    codeGenerator.genCodeLine(
        "private static readonly int[] jjInitStates  = {");
    int k = 0;
    for (int i : tokenizerData.initialStates.keySet()) {
      if (k++ > 0) codeGenerator.genCode(", ");
      codeGenerator.genCode(tokenizerData.initialStates.get(i));
    }
    codeGenerator.genCodeLine("};");

    codeGenerator.genCodeLine(
        "private static readonly int[] canMatchAnyChar = {");
    k = 0;
    for (int i = 0; i < tokenizerData.wildcardKind.size(); i++) {
      if (k++ > 0) codeGenerator.genCode(", ");
      codeGenerator.genCode(tokenizerData.wildcardKind.get(i));
    }
    codeGenerator.genCodeLine("};");
  }

  private void dumpMatchInfo(
      CodeGenHelper codeGenerator, TokenizerData tokenizerData) {
    Map<Integer, TokenizerData.MatchInfo> allMatches =
        tokenizerData.allMatches;

    // A bit ugly.

    BitSet toSkip = new BitSet(allMatches.size());
    BitSet toSpecial = new BitSet(allMatches.size());
    BitSet toMore = new BitSet(allMatches.size());
    BitSet toToken = new BitSet(allMatches.size());
    int[] newStates = new int[allMatches.size()];
    toSkip.set(allMatches.size() + 1, true);
    toToken.set(allMatches.size() + 1, true);
    toMore.set(allMatches.size() + 1, true);
    toSpecial.set(allMatches.size() + 1, true);
    // Kind map.
    codeGenerator.genCodeLine(
        "public static readonly string[] jjstrLiteralImages = {");

    int k = 0;
    for (int i = 0; i < allMatches.size(); i++) {
      TokenizerData.MatchInfo matchInfo = allMatches.get(i);
      switch(matchInfo.matchType) {
        case SKIP: toSkip.set(i); break;
        case SPECIAL_TOKEN: toSkip.set(i); toSpecial.set(i); break;
        case MORE: toMore.set(i); break;
        case TOKEN: toToken.set(i); break;
      }
      newStates[i] = matchInfo.newLexState;
      String image = matchInfo.image;
      if (k++ > 0) codeGenerator.genCodeLine(", ");
      if (image != null) {
        codeGenerator.genCode("\"");
        for (int j = 0; j < image.length(); j++) {
          if (image.charAt(j) <= 0xff) {
            codeGenerator.genCode(
                "\\0" + Integer.toOctalString(image.charAt(j)));
          } else {
            String hexVal = Integer.toHexString(image.charAt(j));
            if (hexVal.length() == 3)
              hexVal = "0" + hexVal;
            codeGenerator.genCode("\\u" + hexVal);
          }
        }
        codeGenerator.genCode("\"");
      } else {
        codeGenerator.genCodeLine("null");
      }
    }
    codeGenerator.genCodeLine("};");

    // Now generate the bit masks.
    generateBitVector("jjtoSkip", toSkip, codeGenerator);
    generateBitVector("jjtoSpecial", toSpecial, codeGenerator);
    generateBitVector("jjtoMore", toMore, codeGenerator);
    generateBitVector("jjtoToken", toToken, codeGenerator);

    codeGenerator.genCodeLine("private static readonly int[] jjnewLexState = {");
    for (int i = 0; i < newStates.length; i++) {
      if (i > 0) codeGenerator.genCode(", ");
      //codeGenerator.genCode("0x" + Integer.toHexString(newStates[i]));
      codeGenerator.genCode("" + Integer.toString(newStates[i]));
    }
    codeGenerator.genCodeLine("};");

    // Action functions.

    final String staticString = "";
    // Token actions.
    codeGenerator.genCodeLine(
        staticString + "void TokenLexicalActions(Token matchedToken) {");
    dumpLexicalActions(allMatches, TokenizerData.MatchType.TOKEN,
                       "matchedToken.kind", codeGenerator);
    codeGenerator.genCodeLine("}");

    // Skip actions.
    // TODO(sreeni) : Streamline this mess.

    codeGenerator.genCodeLine(
        staticString + "void SkipLexicalActions(Token matchedToken) {");
    dumpLexicalActions(allMatches, TokenizerData.MatchType.SKIP,
                       "jjmatchedKind", codeGenerator);
    dumpLexicalActions(allMatches, TokenizerData.MatchType.SPECIAL_TOKEN,
                       "jjmatchedKind", codeGenerator);
    codeGenerator.genCodeLine("}");

    // More actions.
    codeGenerator.genCodeLine(
        staticString + "void MoreLexicalActions() {");
    codeGenerator.genCodeLine(
        "jjimageLen += (lengthOfMatch = jjmatchedPos + 1);");
    dumpLexicalActions(allMatches, TokenizerData.MatchType.MORE,
                       "jjmatchedKind", codeGenerator);
    codeGenerator.genCodeLine("}");
  }

  private void dumpLexicalActions(
      Map<Integer, TokenizerData.MatchInfo> allMatches,
      TokenizerData.MatchType matchType, String kindString,
      CodeGenHelper codeGenerator) {
    codeGenerator.genCodeLine("  switch(" + kindString + ") {");
    for (int i : allMatches.keySet()) {
      TokenizerData.MatchInfo matchInfo = allMatches.get(i);
      if (matchInfo.action == null ||
          matchInfo.matchType != matchType) {
        continue;
      }
      codeGenerator.genCodeLine("    case " + i + ": {\n");
      codeGenerator.genCodeLine("      " + matchInfo.action);
      codeGenerator.genCodeLine("      break;");
      codeGenerator.genCodeLine("    }");
    }
    codeGenerator.genCodeLine("    default: break;");
    codeGenerator.genCodeLine("  }");
  }

  private static void generateBitVector(
      String name, BitSet bits, CodeGenHelper codeGenerator) {
    codeGenerator.genCodeLine("private static readonly long[] " + name + " = {");
    long[] longs = bits.toLongArray();
    for (int i = 0; i < longs.length; i++) {
      if (i > 0) codeGenerator.genCode(", ");
      //codeGenerator.genCode("0x" + Long.toHexString(longs[i]) + "L");
      codeGenerator.genCode("" + Long.toString(longs[i]) + "L");
    }
    codeGenerator.genCodeLine("};");
  }

  private void generateConstantsClass(TokenizerData tokenizerData) {
    codeGenerator.genCodeLine("public class " + tokenizerData.parserName + "Constants {");

    codeGenerator.genCodeLine("public const int EOF  = 0;");
    for (Integer i: tokenizerData.labels.keySet()) {
      codeGenerator.genCodeLine("public const int " + tokenizerData.labels.get(i) + " = " + i + ";");
    }

    codeGenerator.genCode("public static string[] tokenImage = { ");
    for (int i = 0; i < tokenizerData.images.length; i++) {
      if (i > 0) codeGenerator.genCodeLine(", ");
      if (tokenizerData.images[i] == null)
        codeGenerator.genCode("null");
      else
        codeGenerator.genCode("@\"" + tokenizerData.images[i].replace("\"", "\"\"") + "\"");
    }
    codeGenerator.genCodeLine("};");

    codeGenerator.genCodeLine("public static string[] lexStateNames = {");
    for (int i = 0; i < tokenizerData.lexStateNames.length; i++) {
      if (i > 0) codeGenerator.genCodeLine(", ");
      codeGenerator.genCodeLine("\"" + tokenizerData.lexStateNames[i] + "\"");
    }
    codeGenerator.genCodeLine("};");

    for (int i = 0; i < tokenizerData.lexStateNames.length; i++) {
      codeGenerator.genCodeLine("public const int " + tokenizerData.lexStateNames[i] + " = " + i + ";");
    }

    codeGenerator.genCodeLine("};");
  }
}
