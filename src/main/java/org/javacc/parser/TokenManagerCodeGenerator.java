package org.javacc.parser;

public interface TokenManagerCodeGenerator {
  /**
   * Genrate the code for the token manager. Note that the code generator just
   * produces a buffer.
   */
  void generateCode(CodeGeneratorSettings settings, TokenizerData tokenizerData);

  /**
   * Complete the code generation and save any output file(s).
   */
  void finish(CodeGeneratorSettings settings, TokenizerData tokenizerData);
}
