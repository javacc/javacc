package org.javacc.parser;

public interface CodeGenerator
{
  /**
   * Generate any other support files you need.
   */
  boolean generateHelpers(CodeGeneratorSettings settings);

  /**
   * The Token class generator.
   */
  TokenCodeGenerator getTokenCodeGenerator();

  /**
   * The TokenManager class generator.
   */
  TokenManagerCodeGenerator getTokenManagerCodeGenerator();

  /**
   * The Parser class generator.
   */
  ParserCodeGenerator getParserCodeGenerator();

  /**
   * TODO(sreeni): Fix this when we do tree annotations in the parser code generator.
   * The JJTree preprocesor.
   */
  org.javacc.jjtree.DefaultJJTreeVisitor getJJTreeCodeGenerator();
}
