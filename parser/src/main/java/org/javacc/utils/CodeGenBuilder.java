// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

package org.javacc.utils;

import org.javacc.parser.CodeGeneratorSettings;
import org.javacc.parser.JavaCCErrors;
import org.javacc.parser.JavaCCParserConstants;
import org.javacc.parser.Token;

import java.io.*;
import java.util.*;

public class CodeGenBuilder {

  private final StringBuffer          buffer;
  private final String                fileName;
  private final CodeGeneratorSettings settings;


  private int cline;
  private int ccol;

  /**
   * Constructs an instance of {@link CodeGenBuilder}.
   *
   * @param fileName
   * @param settings
   */
  public CodeGenBuilder(String fileName, CodeGeneratorSettings settings) {
    this(new StringBuffer(), fileName, settings);
  }

  /**
   * Constructs an instance of {@link CodeGenBuilder}.
   *
   * @param buffer
   * @param fileName
   * @param settings
   */
  protected CodeGenBuilder(StringBuffer buffer, String fileName, CodeGeneratorSettings settings) {
    this.buffer = buffer;
    this.fileName = fileName;
    this.settings = settings;
  }

  /**
   * Gets the target file name.
   */
  protected final String getFileName() {
    return fileName;
  }

  /**
   * Get the {@link StringBuffer}
   */
  protected StringBuffer getBuffer() {
    return buffer;
  }

  /**
   * Append code snippet to the builder.
   *
   * @param code
   */
  public final CodeGenBuilder genCode(Object... code) {
    for (Object s : code) {
      getBuffer().append(s);
    }
    return this;
  }

  /**
   * Append code snippet to the builder & a new line.
   *
   * @param code
   */
  public final CodeGenBuilder genCodeLine(Object... code) {
    genCode(code);
    genCode("\n");
    return this;
  }

  /**
   * Append the processed template, optionally provides additional options.
   *
   * @param name
   * @param additionalOptions
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public final CodeGenBuilder genTemplate(String name, Object... additionalOptions) throws IOException {
    Map<String, Object> options = new HashMap<String, Object>(settings);
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
    TemplateGenerator.generateTemplate(new PrintWriter(sw), name, options);
    sw.close();
    genCode(sw.toString());

    return this;
  }

  /**
   * Write the buffer to the file.
   */
  public void build() {
    write(fileName, getBuffer());
  }

  protected final void write(String fileName, StringBuffer buffer) {
    try (OutputFile file = new OutputFile(new File(fileName))) {
      file.getPrintWriter().print(buffer.toString());
    } catch (IOException ioe) {
      JavaCCErrors.fatal("Could not create output file: " + fileName);
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
    genCode(getStringForTokenOnly(t));
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

  protected String escapeToUnicode(String text) {
    return CodeGenBuilder.escapeUnicode(text);
  }

  public final void printToken(Token t) {
    genCode(CodeGenBuilder.toString(t));
  }

  public final void printLeadingComments(Token t) {
    genCode(getLeadingComments(t));
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
   * Escape the unicode characters.
   *
   * @param text
   */
  public static String escapeUnicode(String text) {
    String retval = "";
    char ch;
    for (int i = 0; i < text.length(); i++) {
      ch = text.charAt(i);
      if (ch < 0x20
          || ch > 0x7e /* || ch == '\\' -- cba commented out 20140305 */ ) {
        String s = "0000" + Integer.toString(ch, 16);
        retval += "\\u" + s.substring(s.length() - 4, s.length());
      } else {
        retval += ch;
      }
    }
    return retval;
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
}
