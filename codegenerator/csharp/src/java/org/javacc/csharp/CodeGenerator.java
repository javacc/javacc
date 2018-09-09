package org.javacc.csharp;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.utils.OutputFileGenerator;

public class CodeGenerator implements org.javacc.parser.CodeGenerator
{
  /**
   * The name of the C# code generator.
   */
  @Override
  public String getName() 
  {
    return "C#";
  }

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
      if ((Boolean)settings.get("JAVA_UNICODE_ESCAPE")) {
        OutputFileGenerator.generateSimple("/templates/csharp/JavaCharStream.template", "JavaCharStream.cs", "/* JavaCC generated file. */", settings);
      } else {
        OutputFileGenerator.generateSimple("/templates/csharp/CharStream.template", "CharStream.cs", "/* JavaCC generated file. */", settings);
      }
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
    return new ParserCodeGenerator();
  }
  
  /**
   * TODO(sreeni): Fix this when we do tree annotations in the parser code generator.
   * The JJTree preprocesor.
   */
  @Override
  public org.javacc.jjtree.DefaultJJTreeVisitor getJJTreeCodeGenerator()
  {
    return new JJTreeCodeGenerator();
  }

}
