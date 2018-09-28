// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

package org.javacc.parser;

import static org.javacc.parser.JavaCCGlobals.*;

import org.javacc.utils.OutputFileGenerator;

import java.io.*;
import java.util.*;

public class CodeGenHelper {
  protected StringBuffer mainBuffer = new StringBuffer();
  protected StringBuffer includeBuffer = new StringBuffer();
  protected StringBuffer staticsBuffer = new StringBuffer();
  protected StringBuffer outputBuffer = mainBuffer;

  public void genStringLiteralArrayCPP(String varName, String[] arr) {
    // First generate char array vars
    for (int i = 0; i < arr.length; i++) {
      genCodeLine("static const JJChar " + varName + "_arr_" + i + "[] = ");
      genStringLiteralInCPP(arr[i]);
      genCodeLine(";");
    }

    genCodeLine("static const JJString " + varName + "[] = {");
    for (int i = 0; i < arr.length; i++) {
      genCodeLine(varName + "_arr_" + i + ", ");
    }
    genCodeLine("};");
  }
  public void genStringLiteralInCPP(String s) {
    // String literals in CPP become char arrays
    outputBuffer.append("{");
    for (int i = 0; i < s.length(); i++) {
      outputBuffer.append("0x" + Integer.toHexString(s.charAt(i)) + ", ");
    }
    outputBuffer.append("0}");
  }
  public void genCodeLine(Object... code) {
    genCode(code);
    genCode("\n");
  }

  public void genCode(Object... code) {
    for (Object s: code) {
      outputBuffer.append(s);
    }
  }

  public void saveOutput(String fileName) {
    mainBuffer.insert(0, "/* " + new File(fileName).getName() + " */\n");
    saveOutput(fileName, mainBuffer);
  }

  public static boolean isHexDigit(char c) {
    return (c >= '0' && c <= '9') ||
           (c >= 'a' && c <= 'f') ||
           (c >= 'A' && c <= 'F');
  }

  // HACK
  private void fixupLongLiterals(StringBuffer sb) {
    for (int i = 0; i < sb.length() - 1; i++) {
      // int beg = i;
      char c1 = sb.charAt(i);
      char c2 = sb.charAt(i + 1);
      if (Character.isDigit(c1) || (c1 == '0' && c2 == 'x')) {
        i += c1 == '0' ? 2 : 1;
        while (isHexDigit(sb.charAt(i))) i++;
        if (sb.charAt(i) == 'L') {
          sb.insert(i, "UL");
        }
        i++;
      }
    }
  }

  public void saveOutput(String fileName, StringBuffer sb) {
    PrintWriter fw = null;
    try {
      File tmp = new File(fileName);
      fw = new PrintWriter(
              new BufferedWriter(
              new FileWriter(tmp),
              8092
          )
      );

      fw.print(sb.toString());
    } catch(IOException ioe) {
      JavaCCErrors.fatal("Could not create output file: " + fileName);
    } finally {
      if (fw != null) {
        fw.close();
      }
    }
  }

  public int cline, ccol;

  public void printTokenSetup(Token t) {
		Token tt = t;
		
		while (tt.specialToken != null) {
			tt = tt.specialToken;
		}
		
		cline = tt.beginLine;
		ccol = tt.beginColumn;
  }

  public void printTokenList(List<Token> list) {
    Token t = null;
    for (Iterator<Token> it = list.iterator(); it.hasNext();) {
      t = it.next();
      printToken(t);
    }
    
    if (t != null)
      printTrailingComments(t);
  }

  public void printTokenOnly(Token t) {
    genCode(getStringForTokenOnly(t));
  }

  public String getStringForTokenOnly(Token t) {
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
    if (t.image.length() > 0) {
      char last = t.image.charAt(t.image.length()-1);
      if (last == '\n' || last == '\r') {
        cline++; ccol = 1;
      }
    } 

    return retval;
  }

  public String addUnicodeEscapes(String str) {
      String retval = "";
      char ch;
      for (int i = 0; i < str.length(); i++) {
        ch = str.charAt(i);
        if (ch < 0x20 || ch > 0x7e /*|| ch == '\\' -- cba commented out 20140305*/ ) {
          String s = "0000" + Integer.toString(ch, 16);
          retval += "\\u" + s.substring(s.length() - 4, s.length());
        } else {
          retval += ch;
        }
      }
      return retval;
  }

  public void printToken(Token t) {
    genCode(getStringToPrint(t));
  }

  public static String getStringToPrint(Token t) {
    String retval = "";
    Token tt = t.specialToken;
    if (tt != null) {
      while (tt.specialToken != null) tt = tt.specialToken;
      while (tt != null) {
        retval += tt.image;
        tt = tt.next;
      }
    }

    return retval + t.image;
  }

  public void printLeadingComments(Token t) {
    genCode(getLeadingComments(t));
  }

  public String getLeadingComments(Token t) {
    String retval = "";
    if (t.specialToken == null) return retval;
    Token tt = t.specialToken;
    while (tt.specialToken != null) tt = tt.specialToken;
    while (tt != null) {
      retval += getStringForTokenOnly(tt);
      tt = tt.next;
    }
    if (ccol != 1 && cline != t.beginLine) {
      retval += "\n";
      cline++; ccol = 1;
    }

    return retval;
  }

  public void printTrailingComments(Token t) {
    outputBuffer.append(getTrailingComments(t));
  }

  public String getTrailingComments(Token t) {
    if (t.next == null) return "";
    return getLeadingComments(t.next);
  }

  /**
   * for testing
   */
  public String getGeneratedCode() {
    return outputBuffer.toString() + "\n";
  }

  /**
   * Generate a modifier
   */
  public void genModifier(String mod) {
    genCode(mod);
  }

  /**
   * Generate a class with a given name, an array of superclass and
   * another array of super interfaes
   */
  public void genClassStart(String mod, String name, String[] superClasses, String[] superInterfaces) {
    if (mod != null) {
       genModifier(mod);
    }
    genCode("class " + name);
    if (superClasses.length == 1 && superClasses[0] != null) {
      genCode(" extends " + superClasses[0]);
    }
    if (superInterfaces.length != 0) {
      genCode(" implements ");
    }

    genCommaSeperatedString(superInterfaces);
    genCodeLine(" {");
  }

  protected void genCommaSeperatedString(String[] strings) {
    for (int i = 0; i < strings.length; i++) {
      if (i > 0) {
        genCode(", ");
      }

      genCode(strings[i]);
    }
  }

  public void switchToMainFile() {
    outputBuffer = mainBuffer; 
  }

  public void switchToStaticsFile() {}

  public void switchToIncludeFile() {}

  public void generateMethodDefHeader(String modsAndRetType, String className, String nameAndParams) {
    generateMethodDefHeader(modsAndRetType, className, nameAndParams, null);
  }

  public void generateMethodDefHeader(String qualifiedModsAndRetType, String className, String nameAndParams, String exceptions) {
    genCode(qualifiedModsAndRetType + " " + nameAndParams);
    if (exceptions != null) {
      genCode(" throws " + exceptions);
    }
    genCodeLine("");
  }

  public String getClassQualifier(String className) {
    return className == null ? "" : className + "::";
  }

  public static String getCharStreamName() {
    if (Options.getUserCharStream()) {
      return "CharStream";
    } else {
      return Options.getJavaUnicodeEscape() ? "JavaCharStream"
                                            : "SimpleCharStream";
    }
  }
  @SuppressWarnings("unchecked")
  public void writeTemplate(String name, Map<String, Object> options, Object... additionalOptions) throws IOException
  {

    // options.put("", .valueOf(maxOrdinal));
    
    
    for (int i = 0; i < additionalOptions.length; i++)
    {
      Object o = additionalOptions[i];
    
      if (o instanceof Map<?,?>)
      {
        options.putAll((Map<String,Object>) o);
      }
      else
      {
        if (i == additionalOptions.length - 1)
          throw new IllegalArgumentException("Must supply pairs of [name value] args");
        
        options.put((String) o, additionalOptions[i+1]);
        i++;
      }
    }
    
    OutputFileGenerator gen = new OutputFileGenerator(name, options);
    StringWriter sw = new StringWriter();
    gen.generate(new PrintWriter(sw));
    sw.close();
    genCode(sw.toString());
  }
}
