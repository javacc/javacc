// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

package org.javacc.utils;

import org.javacc.jjtree.TokenUtils;
import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.JavaCCErrors;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.JavaCCParserConstants;
import org.javacc.parser.Token;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class CodeBuilder<B extends CodeBuilder<?>> implements Closeable {

  private final CodeGeneratorSettings options;


  private File         file;
  private String       version;
  private Set<String>  tools  = new LinkedHashSet<>();
  private List<String> option = new ArrayList<>();

  private int          cline;
  private int          ccol;

  /**
   * Constructs an instance of {@link CodeBuilder}.
   *
   * @param options
   */
  protected CodeBuilder(CodeGeneratorSettings options) {
    this.options = options;
  }

  /**
   * Get the {@link StringBuffer}
   */
  protected abstract StringBuffer getBuffer();

  /**
   * Gets the target {@link File}.
   */
  protected final File getFile() {
    return file;
  }

  /**
   * Sets the target {@link File}.
   * 
   * @param file
   */
  @SuppressWarnings("unchecked")
  public final B setFile(File file) {
    this.file = file;
    return (B) this;
  }

  /**
   * Sets the compatible version.
   * 
   * @param version
   */
  @SuppressWarnings("unchecked")
  public final B setVersion(String version) {
    this.version = version;
    return (B) this;
  }

  /**
   * Add a tool.
   * 
   * @param tool
   */
  @SuppressWarnings("unchecked")
  public final B addTools(String... tools) {
    for (String tool : tools)
      this.tools.add(tool);
    return (B) this;
  }

  /**
   * Add a tool.
   * 
   * @param tool
   */
  @SuppressWarnings("unchecked")
  public final B addOption(String... options) {
    for (String option : options)
      this.option.add(option);
    return (B) this;
  }

  /**
   * Append code snippet to the builder.
   *
   * @param code
   */
  @SuppressWarnings("unchecked")
  public final B print(Object... code) {
    for (Object s : code) {
      getBuffer().append(s);
    }
    return (B) this;
  }

  /**
   * Append code snippet to the builder & a new line.
   *
   * @param code
   */
  @SuppressWarnings("unchecked")
  public final B println(Object... code) {
    print(code);
    print("\n");
    return (B) this;
  }

  /**
   * Append the processed template, optionally provides additional options.
   *
   * @param name
   * @param additionalOptions
   * @throws IOException
   */
  public final B printTemplate(String name) throws IOException {
    return printTemplate(name, CodeGeneratorSettings.create());
  }

  @SuppressWarnings("unchecked")
  public final B printTemplate(String name, CodeGeneratorSettings additionalOptions) throws IOException {
    CodeGeneratorSettings options =
        additionalOptions.isEmpty() ? this.options : CodeGeneratorSettings.of(this.options).add(additionalOptions);

    try (StringWriter writer = new StringWriter()) {
      TemplateBuilder generator = new TemplateBuilder(name, options);
      generator.generate(new PrintWriter(writer));
      writer.flush();
      print(writer.toString());
    }
    return (B) this;
  }

  /**
   * Write the buffer to the file.
   */
  protected void build() {
    store(getFile(), getBuffer());
  }

  public final void close() throws IOException {
    build();
  }

  protected final void store(File file, StringBuffer buffer) {
    String tool = tools.isEmpty() ? JavaCCGlobals.toolName : String.join(",", tools);

    try (OutputFile output = new OutputFile(file, tool, version, option)) {
      output.getPrintWriter().print(buffer.toString());
    } catch (IOException ioe) {
      JavaCCErrors.fatal("Could not create output file: " + file.getAbsolutePath());
    }
  }

  public final void printTokenSetup(Token token) {
    Token tt = token;
    while (tt.specialToken != null) {
      tt = tt.specialToken;
    }
    cline = tt.beginLine;
    ccol = tt.beginColumn;
  }

  public final void printTokenList(List<Token> list) {
    Token t = null;
    for (Iterator<Token> it = list.iterator(); it.hasNext();) {
      t = it.next();
      printToken(t);
    }

    if (t != null)
      printTrailingComments(t);
  }

  public final void printTokenOnly(Token t) {
    print(getStringForTokenOnly(t));
  }

  private String getStringForTokenOnly(Token t) {
    String retval = "";
    for (; cline < t.beginLine; cline++) {
      retval += "\n";
      ccol = 1;
    }
    for (; ccol < t.beginColumn; ccol++) {
      retval += " ";
    }
    if (t.kind == JavaCCParserConstants.STRING_LITERAL || t.kind == JavaCCParserConstants.CHARACTER_LITERAL)
      retval += escapeToUnicode(t.image);
    else
      retval += t.image;
    cline = t.endLine;
    ccol = t.endColumn + 1;
    if (t.image.length() > 0) {
      char last = t.image.charAt(t.image.length() - 1);
      if (last == '\n' || last == '\r') {
        cline++;
        ccol = 1;
      }
    }

    return retval;
  }

  public String escapeToUnicode(String text) {
    return TokenUtils.addUnicodeEscapes(text);
  }

  public final void printToken(Token t) {
    print(CodeBuilder.toString(t));
  }

  public final void printLeadingComments(Token t) {
    print(getLeadingComments(t));
  }

  public final String getLeadingComments(Token t) {
    String retval = "";
    if (t.specialToken == null)
      return retval;
    Token tt = t.specialToken;
    while (tt.specialToken != null)
      tt = tt.specialToken;
    while (tt != null) {
      retval += getStringForTokenOnly(tt);
      tt = tt.next;
    }
    if (ccol != 1 && cline != t.beginLine) {
      retval += "\n";
      cline++;
      ccol = 1;
    }

    return retval;
  }

  public final void printTrailingComments(Token token) {
    getBuffer().append(getTrailingComments(token));
  }

  public final String getTrailingComments(Token token) {
    if (token.next == null)
      return "";

    return getLeadingComments(token.next);
  }

  /**
   * Get the string representation of a {@link Token}.
   *
   * @param token
   */
  public static String toString(Token token) {
    StringBuilder builder = new StringBuilder();
    Token sToken = token.specialToken;
    if (sToken != null) {
      while (sToken.specialToken != null) {
        sToken = sToken.specialToken;
      }
      while (sToken != null) {
        builder.append(sToken.image);
        sToken = sToken.next;
      }
    }
    builder.append(token.image);
    return builder.toString();
  }

  /**
   * The {@link GenericCodeBuilder} class.
   */
  public static class GenericCodeBuilder extends CodeBuilder<GenericCodeBuilder> {

    private final StringBuffer buffer = new StringBuffer();

    /**
     * Constructs an instance of {@link AbstractCodeGenBuilder2}.
     *
     * @param options
     */
    private GenericCodeBuilder(CodeGeneratorSettings options) {
      super(options);
    }

    /**
     * Get the {@link StringBuffer}
     */
    protected final StringBuffer getBuffer() {
      return buffer;
    }

    /**
     * Constructs an instance of {@link GenericCodeBuilder}.
     *
     * @param options
     */
    public static GenericCodeBuilder of(CodeGeneratorSettings options) {
      return new GenericCodeBuilder(options);
    }
  }
}