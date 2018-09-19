
package org.javacc.parser;

public interface TokenCodeGenerator
{
  /**
   * The Token class generator.
   */
  boolean generateCodeForToken(CodeGeneratorSettings settings);
}
