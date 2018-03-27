package org.javacc.parser;

import org.javacc.parser.JavaFiles.JavaResourceTemplateLocations;

public class JavaCodeGenerator implements CodeGenerator
{
  /**
   * Generate any other support files you need.
   */
  @Override
  public boolean generateHelpers(CodeGeneratorSettings settings)
  {
    try
    {
      JavaResourceTemplateLocations templateLoc = JavaFiles.RESOURCES_JAVA_CLASSIC;
      JavaFiles.gen_TokenMgrError(templateLoc);
      JavaFiles.gen_ParseException(templateLoc);
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
    return new JavaTokenCodeGenerator();
  }

  /**
   * The TokenManager class generator.
   */
  @Override
  public TokenManagerCodeGenerator getTokenManagerCodeGenerator()
  {
    return new TableDrivenJavaCodeGenerator();
  }

  /**
   * The Parser class generator.
   */
  @Override
  public ParserCodeGenerator getParserCodeGenerator()
  {
    return null;
  }

  /**
   * TODO(sreeni): Fix this when we do tree annotations in the parser code generator.
   * The JJTree preprocesor.
   */
  @Override
  public org.javacc.jjtree.DefaultJJTreeVisitor getJJTreeCodeGenerator()
  {
    return null;
  }

}
