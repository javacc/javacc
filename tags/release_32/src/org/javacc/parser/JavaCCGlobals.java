/*
 * Copyright © 2002 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * California 95054, U.S.A. All rights reserved.  Sun Microsystems, Inc. has
 * intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation,
 * these intellectual property rights may include one or more of the U.S.
 * patents listed at http://www.sun.com/patents and one or more additional
 * patents or pending patent applications in the U.S. and in other countries.
 * U.S. Government Rights - Commercial software. Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and applicable
 * provisions of the FAR and its supplements.  Use is subject to license terms.
 * Sun,  Sun Microsystems,  the Sun logo and  Java are trademarks or registered
 * trademarks of Sun Microsystems, Inc. in the U.S. and other countries.  This
 * product is covered and controlled by U.S. Export Control laws and may be
 * subject to the export or import laws in other countries.  Nuclear, missile,
 * chemical biological weapons or nuclear maritime end uses or end users,
 * whether direct or indirect, are strictly prohibited.  Export or reexport
 * to countries subject to U.S. embargo or to entities identified on U.S.
 * export exclusion lists, including, but not limited to, the denied persons
 * and specially designated nationals lists is strictly prohibited.
 */

package org.javacc.parser;

import java.util.Vector;
import java.io.*;
import org.javacc.Version;

/**
 * This package contains data created as a result of parsing and semanticizing
 * a JavaCC input file.  This data is what is used by the back-ends of JavaCC as
 * well as any other back-end of JavaCC related tools such as JJTree.
 */
public class JavaCCGlobals {

  /**
   * String that identifies the JavaCC generated files.
   */
  protected static final String toolName = "JavaCC";

  /**
   * The name of the grammar file being processed.
   */
  static public String fileName;

  /**
   * The name of the original file (before processing by JJTree and JJCov).
   * Currently this is the same as fileName.
   */
  static public String origFileName;

  /**
   * The File object for the output directory where all the generated files will
   * be placed. It defaults to the current directory, but may be set by the
   * user using the OUTPUT_DIRECTORY option.
   */
  static public File outputDir = new File(System.getProperty("user.dir"));
  static private String outputDirName = null;
  static private Object outputDirNameLoc = null;

  /**
   * Set to true if this file has been processed by JJTree.
   */
  static public boolean jjtreeGenerated;

  /**
   * Set to true if this file has been processed by JJCov.
   */
  static public boolean jjcovGenerated;

  /**
   * The list of tools that have participated in generating the
   * input grammar file.
   */
  static public Vector toolNames;

  /**
   * This prints the banner line when the various tools are invoked.  This
   * takes as argument the tool's full name and its version.
   */
  static public void bannerLine(String fullName, String ver) {
    System.out.print("Java Compiler Compiler Version " + Version.version + " (" + fullName);
    if (!ver.equals("")) {
      System.out.print(" Version " + ver);
    }
    System.out.println(")");
    //System.out.println("Copyright © 2002 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.");
  }

  /**
   * The name of the parser class (what appears in PARSER_BEGIN and PARSER_END).
   */
  static public String cu_name;

  /**
   * This is a list of tokens that appear after "PARSER_BEGIN(name)" all the
   * way until (but not including) the opening brace "{" of the class "name".
   */
  static public java.util.Vector cu_to_insertion_point_1 = new java.util.Vector();

  /**
   * This is the list of all tokens that appear after the tokens in
   * "cu_to_insertion_point_1" and until (but not including) the closing brace "}"
   * of the class "name".
   */
  static public java.util.Vector cu_to_insertion_point_2 = new java.util.Vector();

  /**
   * This is the list of all tokens that appear after the tokens in
   * "cu_to_insertion_point_2" and until "PARSER_END(name)".
   */
  static public java.util.Vector cu_from_insertion_point_2 = new java.util.Vector();

  /**
   * A list of all grammar productions - normal and JAVACODE - in the order
   * they appear in the input file.  Each entry here will be a subclass of
   * "NormalProduction".
   */
  static public java.util.Vector bnfproductions = new java.util.Vector();

  /**
   * A symbol table of all grammar productions - normal and JAVACODE.  The
   * symbol table is indexed by the name of the left hand side non-terminal.
   * Its contents are of type "NormalProduction".
   */
  static public java.util.Hashtable production_table = new java.util.Hashtable();

  /**
   * A mapping of lexical state strings to their integer internal representation.
   * Integers are stored as java.lang.Integer's.
   */
  static public java.util.Hashtable lexstate_S2I = new java.util.Hashtable();

  /**
   * A mapping of the internal integer representations of lexical states to
   * their strings.  Integers are stored as java.lang.Integer's.
   */
  static public java.util.Hashtable lexstate_I2S = new java.util.Hashtable();

  /**
   * The declarations to be inserted into the TokenManager class.
   */
  static public java.util.Vector token_mgr_decls;

  /**
   * The list of all TokenProductions from the input file.  This list includes
   * implicit TokenProductions that are created for uses of regular expressions
   * within BNF productions.
   */
  static public java.util.Vector rexprlist = new java.util.Vector();

  /**
   * The total number of distinct tokens.  This is therefore one more than the
   * largest assigned token ordinal.
   */
  static public int tokenCount;

  /**
   * This is a symbol table that contains all named tokens (those that are
   * defined with a label).  The index to the table is the image of the label
   * and the contents of the table are of type "RegularExpression".
   */
  static public java.util.Hashtable named_tokens_table = new java.util.Hashtable();

  /**
   * Contains the same entries as "named_tokens_table", but this is an ordered
   * list which is ordered by the order of appearance in the input file.
   */
  static public java.util.Vector ordered_named_tokens = new java.util.Vector();

  /**
   * A mapping of ordinal values (represented as objects of type "Integer") to
   * the corresponding labels (of type "String").  An entry exists for an ordinal
   * value only if there is a labeled token corresponding to this entry.
   * If there are multiple labels representing the same ordinal value, then
   * only one label is stored.
   */
  static public java.util.Hashtable names_of_tokens = new java.util.Hashtable();

  /**
   * A mapping of ordinal values (represented as objects of type "Integer") to
   * the corresponding RegularExpression's.
   */
  static public java.util.Hashtable rexps_of_tokens = new java.util.Hashtable();

  /**
   * This is a three-level symbol table that contains all simple tokens (those
   * that are defined using a single string (with or without a label).  The index
   * to the first level table is a lexical state which maps to a second level
   * hashtable.  The index to the second level hashtable is the string of the
   * simple token converted to upper case, and this maps to a third level hashtable.
   * This third level hashtable contains the actual string of the simple token
   * and maps it to its RegularExpression.
   */
  static public java.util.Hashtable simple_tokens_table = new java.util.Hashtable();

  /**
   * maskindex, jj2index, maskVals are variables that are shared between
   * ParseEngine and ParseGen.
   */
  static protected int maskindex = 0;
  static protected int jj2index = 0;
  static protected Vector maskVals = new Vector();

  static Action actForEof;
  static String nextStateForEof;


  // Some general purpose utilities follow.

  /**
   * Returns the identifying string for the file name, given a toolname
   * used to generate it.
   */
  public static String getIdString(String toolName, String fileName) {
     Vector toolNames = new Vector();
     toolNames.addElement(toolName);
     return getIdString(toolNames, fileName);
  }

  /**
   * Returns the identifying string for the file name, given a set of tool
   * names that are used to generate it.
   */
  public static String getIdString(Vector toolNames, String fileName) {
     int i;
     String toolNamePrefix = "Generated By:";

     for (i = 0; i < toolNames.size() - 1; i++)
        toolNamePrefix += (String)toolNames.elementAt(i) + "&";
     toolNamePrefix += (String)toolNames.elementAt(i) + ":";

     if (toolNamePrefix.length() > 200)
     {
        System.out.println("Tool names too long.");
        throw new Error();
     }

     return toolNamePrefix + " Do not edit this line. " + fileName;
  }

  /**
   * Returns true if tool name passed is one of the tool names returned
   * by getToolNames(fileName).
   */
  public static boolean isGeneratedBy(String toolName, String fileName) {
     Vector v = getToolNames(fileName);

     for (int i = 0; i < v.size(); i++)
        if (toolName.equals((String)v.elementAt(i)))
           return true;

     return false;
  }

  private static Vector makeToolNameVector(String str) {
     Vector retVal = new Vector();

     int limit1 = str.indexOf('\n');
     if (limit1 == -1) limit1 = 1000;
     int limit2 = str.indexOf('\r');
     if (limit2 == -1) limit2 = 1000;
     int limit = (limit1 < limit2) ? limit1 : limit2;

     String tmp;
     if (limit == 1000) {
       tmp = str;
     } else {
       tmp = str.substring(0, limit);
     }

     if (tmp.indexOf(':') == -1)
        return retVal;

     tmp = tmp.substring(tmp.indexOf(':') + 1);

     if (tmp.indexOf(':') == -1)
        return retVal;

     tmp = tmp.substring(0, tmp.indexOf(':'));

     int i = 0, j = 0;

     while (j < tmp.length() && (i = tmp.indexOf('&', j)) != -1)
     {
        retVal.addElement(tmp.substring(j, i));
        j = i + 1;
     }

     if (j < tmp.length())
        retVal.addElement(tmp.substring(j));

     return retVal;
  }

  /**
   * Returns a Vector of names of the tools that have been used to generate
   * the given file.
   */
  public static Vector getToolNames(String fileName) {
     char[] buf = new char[256];
     java.io.FileReader stream = null;
     int read, total = 0;

     try {
       stream = new java.io.FileReader(fileName);

       for (;;)
          if ((read = stream.read(buf, total, buf.length - total)) != -1)
          {
             if ((total += read) == buf.length)
                break;
          }
          else
             break;

       return makeToolNameVector(new String(buf, 0, total));
    } catch(java.io.FileNotFoundException e1) {
    } catch(java.io.IOException e2) {
       if (total > 0)
          return makeToolNameVector(new String(buf, 0, total));
    }
    finally {
       try { stream.close(); }
       catch (Exception e3) { }
    }

    return new Vector();
  }

  public static void storeOutputDirSpec(Object loc, String val) {
    outputDirName = val;
    outputDirNameLoc = loc;
  }

  public static void setOutputDir() {
     if (outputDirName == null)
        return;

     File tmp = new File(outputDirName);

     if (!tmp.exists()) {
        if (outputDirNameLoc == null)
           JavaCCErrors.warning("Output directory \"" + outputDirName + "\" does not exist. Creating the directory.");
        else
           JavaCCErrors.warning(outputDirNameLoc, "Output directory \"" + outputDirName + "\" does not exist. Creating the directory.");

        if (!tmp.mkdir()) {
           if (outputDirNameLoc == null)
              JavaCCErrors.semantic_error("Cannot create the output directory : " + outputDirName);
           else
              JavaCCErrors.semantic_error(outputDirNameLoc, "Cannot create the output directory : " + outputDirName);

           return;
        }
     }

     if (!tmp.isDirectory()) {
        if (outputDirNameLoc == null)
           JavaCCErrors.semantic_error("\"" + outputDirName + " is not a valid output directory.");
        else
           JavaCCErrors.semantic_error(outputDirNameLoc, "\"" + outputDirName + " is not a valid output directory.");

        return;
     }

     if (!tmp.canWrite()) {
        if (outputDirNameLoc == null)
           JavaCCErrors.semantic_error("Cannot write to the output output directory : \"" + outputDirName + "\"");
        else
           JavaCCErrors.semantic_error(outputDirNameLoc, "Cannot write to the output output directory : \"" + outputDirName + "\"");

        return;
     }

     outputDir = tmp;
  }

  static public String staticOpt() {
    if (Options.B("STATIC")) {
      return "static ";
    } else {
      return "";
    }
  }

  static public String add_escapes(String str) {
    String retval = "";
    char ch;
    for (int i = 0; i < str.length(); i++) {
      ch = str.charAt(i);
      if (ch == '\b') {
        retval += "\\b";
      } else if (ch == '\t') {
        retval += "\\t";
      } else if (ch == '\n') {
        retval += "\\n";
      } else if (ch == '\f') {
        retval += "\\f";
      } else if (ch == '\r') {
        retval += "\\r";
      } else if (ch == '\"') {
        retval += "\\\"";
      } else if (ch == '\'') {
        retval += "\\\'";
      } else if (ch == '\\') {
        retval += "\\\\";
      } else if (ch < 0x20 || ch > 0x7e) {
	String s = "0000" + Integer.toString(ch, 16);
	retval += "\\u" + s.substring(s.length() - 4, s.length());
      } else {
        retval += ch;
      }
    }
    return retval;
  }

  static public String addUnicodeEscapes(String str) {
    String retval = "";
    char ch;
    for (int i = 0; i < str.length(); i++) {
      ch = str.charAt(i);
      if (ch < 0x20 || ch > 0x7e) {
	String s = "0000" + Integer.toString(ch, 16);
	retval += "\\u" + s.substring(s.length() - 4, s.length());
      } else {
        retval += ch;
      }
    }
    return retval;
  }

  static protected int cline, ccol;

  static protected void printTokenSetup(Token t) {
    Token tt = t;
    while (tt.specialToken != null) tt = tt.specialToken;
    cline = tt.beginLine;
    ccol = tt.beginColumn;
  }

  static protected void printTokenOnly(Token t, java.io.PrintWriter ostr) {
    for (; cline < t.beginLine; cline++) {
      ostr.println(""); ccol = 1;
    }
    for (; ccol < t.beginColumn; ccol++) {
      ostr.print(" ");
    }
    if (t.kind == JavaCCParserConstants.STRING_LITERAL ||
        t.kind == JavaCCParserConstants.CHARACTER_LITERAL)
       ostr.print(addUnicodeEscapes(t.image));
    else
       ostr.print(t.image);
    cline = t.endLine;
    ccol = t.endColumn+1;
    char last = t.image.charAt(t.image.length()-1);
    if (last == '\n' || last == '\r') {
      cline++; ccol = 1;
    }
  }

  static protected void printToken(Token t, java.io.PrintWriter ostr) {
    Token tt = t.specialToken;
    if (tt != null) {
      while (tt.specialToken != null) tt = tt.specialToken;
      while (tt != null) {
        printTokenOnly(tt, ostr);
        tt = tt.next;
      }
    }
    printTokenOnly(t, ostr);
  }

  static protected void printLeadingComments(Token t, java.io.PrintWriter ostr) {
    if (t.specialToken == null) return;
    Token tt = t.specialToken;
    while (tt.specialToken != null) tt = tt.specialToken;
    while (tt != null) {
      printTokenOnly(tt, ostr);
      tt = tt.next;
    }
    if (ccol != 1 && cline != t.beginLine) {
      ostr.println("");
      cline++; ccol = 1;
    }
  }

  static protected void printTrailingComments(Token t, java.io.PrintWriter ostr) {
    if (t.next == null) return;
    printLeadingComments(t.next);
  }

  static protected String printTokenOnly(Token t) {
    String retval = "";
    for (; cline < t.beginLine; cline++) {
      retval += "\n"; ccol = 1;
    }
    for (; ccol < t.beginColumn; ccol++) {
      retval += " ";
    }
    if (t.kind == JavaCCParserConstants.STRING_LITERAL ||
        t.kind == JavaCCParserConstants.CHARACTER_LITERAL)
       retval += addUnicodeEscapes(t.image);
    else
       retval += t.image;
    cline = t.endLine;
    ccol = t.endColumn+1;
    char last = t.image.charAt(t.image.length()-1);
    if (last == '\n' || last == '\r') {
      cline++; ccol = 1;
    }
    return retval;
  }

  static protected String printToken(Token t) {
    String retval = "";
    Token tt = t.specialToken;
    if (tt != null) {
      while (tt.specialToken != null) tt = tt.specialToken;
      while (tt != null) {
        retval += printTokenOnly(tt);
        tt = tt.next;
      }
    }
    retval += printTokenOnly(t);
    return retval;
  }

  static protected String printLeadingComments(Token t) {
    String retval = "";
    if (t.specialToken == null) return retval;
    Token tt = t.specialToken;
    while (tt.specialToken != null) tt = tt.specialToken;
    while (tt != null) {
      retval += printTokenOnly(tt);
      tt = tt.next;
    }
    if (ccol != 1 && cline != t.beginLine) {
      retval += "\n";
      cline++; ccol = 1;
    }
    return retval;
  }

  static protected String printTrailingComments(Token t) {
    if (t.next == null) return "";
    return printLeadingComments(t.next);
  }

   public static void reInit()
   {
      fileName = null;
      origFileName = null;
      outputDir = new File(System.getProperty("user.dir"));
      outputDirName = null;
      outputDirNameLoc = null;
      jjtreeGenerated = false;
      jjcovGenerated = false;
      toolNames = null;
      cu_name = null;
      cu_to_insertion_point_1 = new java.util.Vector();
      cu_to_insertion_point_2 = new java.util.Vector();
      cu_from_insertion_point_2 = new java.util.Vector();
      bnfproductions = new java.util.Vector();
      production_table = new java.util.Hashtable();
      lexstate_S2I = new java.util.Hashtable();
      lexstate_I2S = new java.util.Hashtable();
      token_mgr_decls = null;
      rexprlist = new java.util.Vector();
      tokenCount = (int)0;
      named_tokens_table = new java.util.Hashtable();
      ordered_named_tokens = new java.util.Vector();
      names_of_tokens = new java.util.Hashtable();
      rexps_of_tokens = new java.util.Hashtable();
      simple_tokens_table = new java.util.Hashtable();
      maskindex = 0;
      jj2index = 0;
      maskVals = new Vector();
      cline = (int)0;
      ccol = (int)0;
      actForEof = null;
      nextStateForEof = null;
   }

}
