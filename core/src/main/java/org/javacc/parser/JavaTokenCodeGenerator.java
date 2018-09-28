package org.javacc.parser;

import org.javacc.parser.JavaFiles.JavaResourceTemplateLocations;

public class JavaTokenCodeGenerator implements TokenCodeGenerator
{
  /**
   * The Token class generator.
   */
  @Override
  public boolean generateCodeForToken(CodeGeneratorSettings settings)
  {
    try
    {
      JavaResourceTemplateLocations templateLoc = JavaFiles.RESOURCES_JAVA_CLASSIC;
      JavaFiles.gen_Token(templateLoc);
    }
    catch(Exception e)
    {
      return false;
    }

    return true;
  }
}
