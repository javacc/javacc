package org.javacc.csharp;

import java.io.IOException;
import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.utils.OutputFileGenerator;

public class TokenCodeGenerator implements org.javacc.parser.TokenCodeGenerator
{
  /**
   * The Token class generator.
   */
  @Override
  public boolean generateCodeForToken(CodeGeneratorSettings settings)
  {
    try
    {
      OutputFileGenerator.generateSimple("/templates/csharp/Token.template", "Token.cs", "/* JavaCC generated file. */", settings);
    }
    catch(IOException e)
    {
      return false;
    }

    return true;
  }
}
