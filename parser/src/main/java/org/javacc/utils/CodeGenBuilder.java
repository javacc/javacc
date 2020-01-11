// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

package org.javacc.utils;

import org.javacc.jjtree.TokenUtils;
import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.JavaCCErrors;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.JavaCCParserConstants;
import org.javacc.parser.Token;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CodeGenBuilder<B extends CodeGenBuilder<?>> {

  private final Map<String, Object> options;


  private File        file;
  private String      version;
  private Set<String> tools  = new LinkedHashSet<>();
  private Set<String> option = new LinkedHashSet<>();

  private int         cline;
  private int         ccol;

  /**
   * Constructs an instance of {@link CodeGenBuilder}.
   *
   * @param options
   */
  protected CodeGenBuilder(Map<String, Object> options) {
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
  @SuppressWarnings("unchecked")
  public final B printTemplate(String name, Object... additionalOptions) throws IOException {
    Map<String, Object> options = new HashMap<String, Object>(this.options);
    for (int i = 0; i < additionalOptions.length; i++) {
      Object o = additionalOptions[i];
      if (o instanceof Map<?, ?>) {
        options.putAll((Map<String, Object>) o);
      } else if (i == additionalOptions.length - 1) {
        throw new IllegalArgumentException("Must supply pairs of [name value] args");
      } else {
        options.put((String) o, additionalOptions[i + 1]);
        i++;
      }
    }

    StringWriter sw = new StringWriter();
    CodeGenBuilder.generateTemplate(new PrintWriter(sw), name, options);
    sw.close();
    print(sw.toString());

    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public final B printTemplate(String name, Map<String, Object> additionalOptions) throws IOException {
    Map<String, Object> options = new HashMap<String, Object>(this.options);
    options.putAll(additionalOptions);

    StringWriter sw = new StringWriter();
    CodeGenBuilder.generateTemplate(new PrintWriter(sw), name, options);
    sw.close();
    print(sw.toString());

    return (B) this;
  }

  /**
   * Write the buffer to the file.
   */
  public void build() {
    store(getFile(), getBuffer());
  }

  protected final void store(File file, StringBuffer buffer) {
    String tool = tools.isEmpty() ? JavaCCGlobals.toolName : String.join(",", tools);
    String[] options = option.isEmpty() ? null : option.toArray(new String[option.size()]);

    try (OutputFile output = new OutputFile(file, tool, version, options)) {
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
    print(GenericCodeBuilder.toString(t));
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
   * Return <code>true</code> if the char is a hex digit.
   *
   * @param c
   */
  public static boolean isHexDigit(char c) {
    return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
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
  public static class GenericCodeBuilder extends CodeGenBuilder<GenericCodeBuilder> {

    private final StringBuffer buffer = new StringBuffer();

    /**
     * Constructs an instance of {@link AbstractCodeGenBuilder2}.
     *
     * @param options
     */
    private GenericCodeBuilder(Map<String, Object> options) {
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
    public static GenericCodeBuilder of(Map<String, Object> options) {
      return new GenericCodeBuilder(options);
    }
  }


  /**
   * Generates a template to a file.
   *
   * @param template
   * @param filename
   * @param toolname
   * @param options
   * @throws IOException
   */
  public static void generateTemplate(String template, String filename, String toolname, 
      CodeGeneratorSettings settings, String ...options) throws IOException {
    File file = new File((String) settings.get("OUTPUT_DIRECTORY"), filename);
    if(options.length == 0) {
      try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
        writer.printf("/* %s generated file. */\n", toolname);
        TemplateGenerator generator = new TemplateGenerator(template, settings);
        generator.generate(writer);
      }
    } else {
      try(OutputFile output = new OutputFile(file, toolname, null, options)) {
        TemplateGenerator generator = new TemplateGenerator(template, settings);
        generator.generate(output.getPrintWriter());
      }
    }
  }

  /**
   * Generates a template to a {@link PrintWriter}.
   *
   * @param writer
   * @param template
   * @param options
   * @throws IOException
   */
  public static void generateTemplate(PrintWriter writer, String template, Map<String, Object> options)
      throws IOException {
    TemplateGenerator generator = new TemplateGenerator(template, options);
    generator.generate(writer);
  }
}