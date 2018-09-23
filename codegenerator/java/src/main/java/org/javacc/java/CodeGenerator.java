
package org.javacc.java;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.JavaFiles;
import org.javacc.parser.JavaFiles.JavaResourceTemplateLocations;
import org.javacc.parser.Options;

public class CodeGenerator implements org.javacc.parser.CodeGenerator {

  public static final boolean IS_DEBUG = true;

  /**
   * The name of the Java code generator.
   */
  @Override
  public String getName() {
    return "Java";
  }

  /**
   * Generate any other support files you need.
   */
  @Override
  public boolean generateHelpers(CodeGeneratorSettings settings) {
    try {
      JavaResourceTemplateLocations templateLoc = JavaFiles.RESOURCES_JAVA_CLASSIC;
      JavaFiles.gen_TokenMgrError(templateLoc);
      JavaFiles.gen_ParseException(templateLoc);
      
      OtherFilesGen.start(Options.getJavaTemplateType().equals(Options.JAVA_TEMPLATE_TYPE_MODERN));
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
