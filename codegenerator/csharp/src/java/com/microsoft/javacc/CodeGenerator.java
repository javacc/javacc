package com.microsoft.javacc;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.utils.OutputFileGenerator;
import org.javacc.parser.ParserCodeGenerator;

public class CodeGenerator implements org.javacc.parser.CodeGenerator
{
  /**
   * Generate any other support files you need.
   */
  @Override
  public boolean generateHelpers(CodeGeneratorSettings settings)
  {
    try
    {
      OutputFileGenerator.generateSimple("/templates/csharp/CharStream.template", "CharStream.cs", "/* JavaCC generated file. */", settings);
      OutputFileGenerator.generateSimple("/templates/csharp/TokenMgrError.template", "TokenMgrError.cs", "/* JavaCC generated file. */", settings);
      OutputFileGenerator.generateSimple("/templates/csharp/ParseException.template", "ParseException.cs", "/* JavaCC generated file. */", settings);
    }
    catch(Exception e)
    {
      return false;
    }

    return true;
  }

  /**
   * The Token class generator.
   */
  @Override
  public TokenCodeGenerator getTokenCodeGenerator()
  {
    return new TokenCodeGenerator();
  }

  /**
   * The TokenManager class generator.
   */
  @Override
  public TokenManagerCodeGenerator getTokenManagerCodeGenerator()
  {
    return new TokenManagerCodeGenerator();
  }

  /**
   * The Parser class generator.
   */
  @Override
  public ParserCodeGenerator getParserCodeGenerator()
  {
    return null;
  }
}
