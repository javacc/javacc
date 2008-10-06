/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.javacc.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.javacc.Version;
import org.javacc.utils.JavaFileGenerator;

/**
 * Generate CharStream, TokenManager and Exceptions.
 */
public class JavaFiles extends JavaCCGlobals implements JavaCCParserConstants
{
  /**
   * ID of the latest version (of JavaCC) in which one of the CharStream classes
   * or the CharStream interface is modified.
   */
  static final String charStreamVersion = "4.1";

  /**
   * ID of the latest version (of JavaCC) in which the TokenManager interface is modified.
   */
  static final String tokenManagerVersion = "4.1";

  /**
   * ID of the latest version (of JavaCC) in which the Token class is modified.
   */
  static final String tokenVersion = "4.1";

  /**
   * ID of the latest version (of JavaCC) in which the ParseException class is
   * modified.
   */
  static final String parseExceptionVersion = "4.1";

  /**
   * ID of the latest version (of JavaCC) in which the TokenMgrError class is
   * modified.
   */
  static final String tokenMgrErrorVersion = "4.1";

  /**
   * Replaces all backslahes with double backslashes.
   */
  static String replaceBackslash(String str)
  {
    StringBuffer b;
    int i = 0, len = str.length();

    while (i < len && str.charAt(i++) != '\\') ;

    if (i == len)  // No backslash found.
      return str;

    char c;
    b = new StringBuffer();
    for (i = 0; i < len; i++)
      if ((c = str.charAt(i)) == '\\')
        b.append("\\\\");
      else
        b.append(c);

    return b.toString();
  }

  /**
   * Read the version from the comment in the specified file.
   * This method does not try to recover from invalid comment syntax, but
   * rather returns version 0.0 (which will always be taken to mean the file
   * is out of date).
   * @param fileName eg Token.java
   * @return The version as a double, eg 4.1
   * @since 4.1
   */
  static double getVersion(String fileName)
  {
    final String commentHeader = "/* " + getIdString(toolName, fileName) + " Version ";
    File file = new File(Options.getOutputDirectory(), replaceBackslash(fileName));

    if (!file.exists()) {
      // Has not yet been created, so it must be up to date.
      try {
        String majorVersion = Version.version.replaceAll("[^0-9.]+.*", "");
        return Double.parseDouble(majorVersion);
      } catch (NumberFormatException e) {
        return 0.0; // Should never happen
      }
    }

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
      String str;
      double version = 0.0;

      // Although the version comment should be the first line, sometimes the
      // user might have put comments before it.
      while ( (str = reader.readLine()) != null) {
        if (str.startsWith(commentHeader)) {
          str = str.substring(commentHeader.length());
          int pos = str.indexOf(' ');
          if (pos >= 0) str = str.substring(0, pos);
          if (str.length() > 0) {
            try {
              version = Double.parseDouble(str);
            }
            catch (NumberFormatException nfe) {
              // Ignore - leave version as 0.0
            }
          }

          break;
        }
      }

      return version;
    }
    catch (IOException ioe)
    {
      return 0.0;
    }
    finally {
      if (reader != null)
      {
        try { reader.close(); } catch (IOException e) {}
      }
    }
  }



  public static void gen_JavaCharStream() {
    try {
      final File file = new File(Options.getOutputDirectory(), "JavaCharStream.java");
      final OutputFile outputFile = new OutputFile(file, charStreamVersion, new String[] {"STATIC", "SUPPORT_CLASS_VISIBILITY_PUBLIC"});

      if (!outputFile.needToWrite)
      {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();

      if (cu_to_insertion_point_1.size() != 0 &&
          ((Token)cu_to_insertion_point_1.get(0)).kind == PACKAGE
      ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.get(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.get(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.get(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.get(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }
      String prefix = (Options.getStatic() ? "static " : "");
      Map options = new HashMap(Options.getOptions());
      options.put("PREFIX", prefix);
      
      JavaFileGenerator generator = new JavaFileGenerator(
          "/templates/JavaCharStream.template", options);
      
      generator.generate(ostr);

      ostr.close();
    } catch (IOException e) {
      System.err.println("Failed to create JavaCharStream " + e);
      JavaCCErrors.semantic_error("Could not open file JavaCharStream.java for writing.");
      throw new Error();
    }
  }

  public static void gen_SimpleCharStream() {
    try {
      final File file = new File(Options.getOutputDirectory(), "SimpleCharStream.java");
      final OutputFile outputFile = new OutputFile(file, charStreamVersion, new String[] {"STATIC", "SUPPORT_CLASS_VISIBILITY_PUBLIC"});

      if (!outputFile.needToWrite)
      {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();

      if (cu_to_insertion_point_1.size() != 0 &&
          ((Token)cu_to_insertion_point_1.get(0)).kind == PACKAGE
      ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.get(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.get(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.get(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.get(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }
      String prefix = (Options.getStatic() ? "static " : "");
      Map options = new HashMap(Options.getOptions());
      options.put("PREFIX", prefix);
      
      JavaFileGenerator generator = new JavaFileGenerator(
          "/templates/SimpleCharStream.template", options);
      
      generator.generate(ostr);

      ostr.close();
    } catch (IOException e) {
      System.err.println("Failed to create SimpleCharStream " + e);
      JavaCCErrors.semantic_error("Could not open file SimpleCharStream.java for writing.");
      throw new Error();
    }
  }

  public static void gen_CharStream() {
    try {
      final File file = new File(Options.getOutputDirectory(), "CharStream.java");
      final OutputFile outputFile = new OutputFile(file, charStreamVersion, new String[] {"STATIC", "SUPPORT_CLASS_VISIBILITY_PUBLIC"});

      if (!outputFile.needToWrite)
      {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();

      if (cu_to_insertion_point_1.size() != 0 &&
          ((Token)cu_to_insertion_point_1.get(0)).kind == PACKAGE
      ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.get(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.get(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.get(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.get(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }
      
      JavaFileGenerator generator = new JavaFileGenerator(
          "/templates/CharStream.template", Options.getOptions());
      
      generator.generate(ostr);

      ostr.close();
    } catch (IOException e) {
      System.err.println("Failed to create CharStream " + e);
      JavaCCErrors.semantic_error("Could not open file CharStream.java for writing.");
      throw new Error();
    }
  }

  public static void gen_ParseException() {
    try {
      final File file = new File(Options.getOutputDirectory(), "ParseException.java");
      final OutputFile outputFile = new OutputFile(file, parseExceptionVersion, new String[] {"KEEP_LINE_COL"});

      if (!outputFile.needToWrite)
      {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();

      if (cu_to_insertion_point_1.size() != 0 &&
          ((Token)cu_to_insertion_point_1.get(0)).kind == PACKAGE
      ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.get(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.get(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.get(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.get(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }
      
      JavaFileGenerator generator = new JavaFileGenerator(
          "/templates/ParseException.template", Options.getOptions());
      
      generator.generate(ostr);

      ostr.close();
    } catch (IOException e) {
      System.err.println("Failed to create ParseException " + e);
      JavaCCErrors.semantic_error("Could not open file ParseException.java for writing.");
      throw new Error();
    }
  }

  public static void gen_TokenMgrError() {
    try {
      final File file = new File(Options.getOutputDirectory(), "TokenMgrError.java");
      final OutputFile outputFile = new OutputFile(file, tokenMgrErrorVersion, new String[0]);

      if (!outputFile.needToWrite)
      {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();

      if (cu_to_insertion_point_1.size() != 0 &&
          ((Token)cu_to_insertion_point_1.get(0)).kind == PACKAGE
      ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.get(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.get(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.get(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.get(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }
      
      JavaFileGenerator generator = new JavaFileGenerator(
          "/templates/TokenMgrError.template", Options.getOptions());
      
      generator.generate(ostr);

      ostr.close();
    } catch (IOException e) {
      System.err.println("Failed to create TokenMgrError " + e);
      JavaCCErrors.semantic_error("Could not open file TokenMgrError.java for writing.");
      throw new Error();
    }
  }

  public static void gen_Token() {
    try {
      final File file = new File(Options.getOutputDirectory(), "Token.java");
      final OutputFile outputFile = new OutputFile(file, tokenVersion, new String[] {"TOKEN_EXTENDS", "KEEP_LINE_COL", "SUPPORT_CLASS_VISIBILITY_PUBLIC"});

      if (!outputFile.needToWrite)
      {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();

      if (cu_to_insertion_point_1.size() != 0 &&
          ((Token)cu_to_insertion_point_1.get(0)).kind == PACKAGE
      ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.get(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.get(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.get(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.get(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }
      
      JavaFileGenerator generator = new JavaFileGenerator(
          "/templates/Token.template", Options.getOptions());
      
      generator.generate(ostr);
 
      ostr.close();
    } catch (IOException e) {
      System.err.println("Failed to create Token " + e);
      JavaCCErrors.semantic_error("Could not open file Token.java for writing.");
      throw new Error();
    }
  }

  public static void gen_TokenManager() {
    try {
      final File file = new File(Options.getOutputDirectory(), "TokenManager.java");
      final OutputFile outputFile = new OutputFile(file, tokenManagerVersion, new String[] {"SUPPORT_CLASS_VISIBILITY_PUBLIC"});

      if (!outputFile.needToWrite)
      {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();

      if (cu_to_insertion_point_1.size() != 0 &&
          ((Token)cu_to_insertion_point_1.get(0)).kind == PACKAGE
      ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.get(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.get(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.get(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.get(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }

      JavaFileGenerator generator = new JavaFileGenerator(
          "/templates/TokenManager.template", Options.getOptions());
      
      generator.generate(ostr);
      
      ostr.close();
    } catch (IOException e) {
      System.err.println("Failed to create TokenManager " + e);
      JavaCCErrors.semantic_error("Could not open file TokenManager.java for writing.");
      throw new Error();
    }
  }

  public static void reInit()
  {
  }

}
