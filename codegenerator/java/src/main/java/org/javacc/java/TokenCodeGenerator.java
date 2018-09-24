
package org.javacc.java;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.JavaFiles;
import org.javacc.parser.JavaFiles.JavaResourceTemplateLocations;

public class TokenCodeGenerator implements org.javacc.parser.TokenCodeGenerator {

  /**
   * The Token class generator.
   */
  @Override
  public boolean generateCodeForToken(CodeGeneratorSettings settings) {
    try {
      JavaResourceTemplateLocations templateLoc = JavaFiles.RESOURCES_JAVA_CLASSIC;
      JavaFiles.gen_Token(templateLoc);
    } catch (Exception e) {
      return false;
    }

    return true;
  }
}
