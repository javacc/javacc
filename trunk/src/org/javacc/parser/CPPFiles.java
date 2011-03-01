
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
public class CPPFiles extends JavaCCGlobals implements JavaCCParserConstants
{
  /**
   * ID of the latest version (of JavaCC) in which one of the CharStream classes
   * or the CharStream interface is modified.
   */
  static final String charStreamVersion = "6.0";

  /**
   * ID of the latest version (of JavaCC) in which the TokenManager interface is modified.
   */
  static final String tokenManagerVersion = "6.0";

  /**
   * ID of the latest version (of JavaCC) in which the Token class is modified.
   */
  static final String tokenVersion = "6.0";

  /**
   * ID of the latest version (of JavaCC) in which the ParseException class is
   * modified.
   */
  static final String parseExceptionVersion = "6.0";

  /**
   * ID of the latest version (of JavaCC) in which the TokenMgrError class is
   * modified.
   */
  static final String tokenMgrErrorVersion = "6.0";

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

  private static String getNamespace() {
/* Check for namespace at somepoint
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
      }*/

    return null;
  }

  private static void genFile(String name, String namespace, String version) {
    final File file = new File(Options.getOutputDirectory(), name);
    try {
      final OutputFile outputFile = new OutputFile(file, version, new String[] {"STATIC", "SUPPORT_CLASS_VISIBILITY_PUBLIC"});

      if (!outputFile.needToWrite) {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();
      JavaFileGenerator generator = new JavaFileGenerator(
          "/templates/cpp/" + name + ".template", Options.getOptions());
      generator.generate(ostr);
      ostr.close();
    } catch (IOException e) {
      System.err.println("Failed to create file: " + file + e);
      JavaCCErrors.semantic_error("Could not open file: " + file + " for writing.");
      throw new Error();
    }
  }

  public static void gen_CharStream() {
    genFile("CharStream.h", null, charStreamVersion);
    genFile("CharStream.cc", null, charStreamVersion);
  }

  public static void gen_ParseException() {
    genFile("ParseException.h", null, parseExceptionVersion);
    genFile("ParseException.cc", null, parseExceptionVersion);
  }

  public static void gen_TokenMgrError() {
    genFile("TokenMgrError.h", null, tokenMgrErrorVersion);
    genFile("TokenMgrError.cc", null, tokenMgrErrorVersion);
  }

  public static void gen_Token() {
    genFile("Token.h", null, tokenVersion);
    genFile("Token.cc", null, tokenVersion);
  }

  public static void gen_TokenManager() {
    genFile("TokenManager.h", null, tokenManagerVersion);
  }

  public static void gen_JavaCCDefs() {
    genFile("JavaCC.h", null, tokenManagerVersion);
  }

  public static void reInit()
  {
  }

}
