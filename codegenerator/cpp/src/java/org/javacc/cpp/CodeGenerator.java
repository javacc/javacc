
package org.javacc.cpp;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.utils.OutputFileGenerator;

public class CodeGenerator implements org.javacc.parser.CodeGenerator {
  
  public static final boolean IS_DEBUG = true;

  /**
   * The name of the C# code generator.
   */
  @Override
  public String getName() {
    return "C++";
  }

  /**
   * Generate any other support files you need.
   */
  @Override
  public boolean generateHelpers(CodeGeneratorSettings settings) {
    try {
      OutputFileGenerator.generateSimple("/templates/cpp/CharStream.h.template", "CharStream.h", "/* JavaCC generated file. */", settings);
      OutputFileGenerator.generateSimple("/templates/cpp/CharStream.cc.template", "CharStream.cc", "/* JavaCC generated file. */", settings);

      OutputFileGenerator.generateSimple("/templates/cpp/TokenMgrError.h.template", "TokenMgrError.h", "/* JavaCC generated file. */", settings);
      OutputFileGenerator.generateSimple("/templates/cpp/TokenMgrError.cc.template", "TokenMgrError.cc", "/* JavaCC generated file. */", settings);

      OutputFileGenerator.generateSimple("/templates/cpp/ParseException.h.template", "ParseException.h", "/* JavaCC generated file. */", settings);
      OutputFileGenerator.generateSimple("/templates/cpp/ParseException.cc.template", "ParseException.cc", "/* JavaCC generated file. */", settings);

      OutputFileGenerator.generateSimple("/templates/cpp/TokenManager.h.template", "TokenManager.h", "/* JavaCC generated file. */", settings);

      OutputFileGenerator.generateSimple("/templates/cpp/JavaCC.h.template", "JavaCC.h", "/* JavaCC generated file. */", settings);
      OutputFileGenerator.generateSimple("/templates/cpp/ErrorHandler.h.template", "ErrorHandler.h", "/* JavaCC generated file. */", settings);

//      if ((Boolean) settings.get("JAVA_UNICODE_ESCAPE")) {
//        OutputFileGenerator.generateSimple("/templates/cpp/JavaCharStream.template", "JavaCharStream.cs", "/* JavaCC generated file. */", settings);
//      } else {
//        OutputFileGenerator.generateSimple("/templates/cpp/CharStream.template", "CharStream.cs", "/* JavaCC generated file. */", settings);
//      }

      OtherFilesGenCPP.start();
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  /**
   * The Token class generator.
   */
  @Override
  public TokenCodeGenerator getTokenCodeGenerator() {
    return new TokenCodeGenerator();
  }

  /**
   * The TokenManager class generator.
   */
  @Override
  public TokenManagerCodeGenerator getTokenManagerCodeGenerator() {
    return new TokenManagerCodeGenerator();
  }

  /**
   * The Parser class generator.
   */
  @Override
  public ParserCodeGenerator getParserCodeGenerator() {
    return new ParserCodeGenerator();
  }

  /**
   * TODO(sreeni): Fix this when we do tree annotations in the parser code generator. The JJTree
   * preprocesor.
   */
  @Override
  public org.javacc.jjtree.DefaultJJTreeVisitor getJJTreeCodeGenerator() {
    return new JJTreeCodeGenerator();
  }

}
