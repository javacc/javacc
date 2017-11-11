// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)


package org.javacc.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.javacc.Version;
import org.javacc.utils.OutputFileGenerator;

/**
 * Generate CharStream, TokenManager and Exceptions.
 */
public class CPPFiles extends JavaCCGlobals implements JavaCCParserConstants
{
   /**
   * ID of the latest version (of JavaCC) in which one of the CharStream classes
   * or the CharStream interface is modified.
   */
  static final String charStreamVersion = Version.majorDotMinor;

  /**
   * ID of the latest version (of JavaCC) in which the TokenManager interface is modified.
   */
  static final String tokenManagerVersion = Version.majorDotMinor;

  /**
   * ID of the latest version (of JavaCC) in which the Token class is modified.
   */
  static final String tokenVersion = Version.majorDotMinor;

  /**
   * ID of the latest version (of JavaCC) in which the ParseException class is
   * modified.
   */
  static final String parseExceptionVersion = Version.majorDotMinor;

  /**
   * ID of the latest version (of JavaCC) in which the TokenMgrError class is
   * modified.
   */
  static final String tokenMgrErrorVersion = Version.majorDotMinor;

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
        String majorVersion = Version.versionNumber.replaceAll("[^0-9.]+.*", "");
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

  private static void genFile(String name, String version, String[] parameters, Map<String, Object> options) {
    final File file = new File(Options.getOutputDirectory(), name);
    try {
      final OutputFile outputFile = new OutputFile(file, version, parameters);

      if (!outputFile.needToWrite) {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();
      OutputFileGenerator generator = new OutputFileGenerator(
          "/templates/cpp/" + name + ".template", options);
      generator.generate(ostr);
      ostr.close();
    } catch (IOException e) {
      System.err.println("Failed to create file: " + file + e);
      JavaCCErrors.semantic_error("Could not open file: " + file + " for writing.");
      throw new Error();
    }
  }

  public static void gen_CharStream() {
    String[] parameters = new String[] {Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC};
    genFile("CharStream.h", charStreamVersion, parameters, Options.getOptions());
    genFile("CharStream.cc", charStreamVersion, parameters, Options.getOptions());
  }

  public static void gen_ParseException() {
    String[] parameters = new String[] {Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC};
    genFile("ParseException.h", parseExceptionVersion, parameters, Options.getOptions());
    genFile("ParseException.cc", parseExceptionVersion, parameters, Options.getOptions());
  }

  public static void gen_TokenMgrError() {
    String[] parameters = new String[] {Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC};
    genFile("TokenMgrError.h", tokenMgrErrorVersion, parameters, Options.getOptions());
    genFile("TokenMgrError.cc", tokenMgrErrorVersion, parameters, Options.getOptions());
  }

  public static void gen_Token(String tokenManagerClass) {
    String[] parameters = new String[] {Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC, Options.USEROPTION__CPP_TOKEN_INCLUDES, Options.USEROPTION__TOKEN_EXTENDS};
    Map<String, Object> options = Options.getOptions();
    options.put("TOKEN_MANAGER_CLASS", tokenManagerClass);
    genFile("Token.h", tokenMgrErrorVersion, parameters, options);
    genFile("Token.cc", tokenMgrErrorVersion, parameters, options);
  }

  public static void gen_TokenManager() {
    String[] parameters = new String[] {Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC};
    genFile("TokenManager.h", tokenManagerVersion, parameters, Options.getOptions());
  }

  public static void gen_JavaCCDefs() {
    String[] parameters = new String[] {Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC};
    genFile("JavaCC.h", tokenManagerVersion, parameters, Options.getOptions());
  }

  public static void gen_ErrorHandler() {
    String[] parameters = new String[] {Options.USEROPTION__STATIC, Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC, Options.USEROPTION__BUILD_PARSER, Options.USEROPTION__BUILD_TOKEN_MANAGER};
    genFile("ErrorHandler.h", parseExceptionVersion, parameters, Options.getOptions());
  }

  public static void reInit()
  {
  }

}
