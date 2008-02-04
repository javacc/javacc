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

import org.javacc.Version;

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
      final OutputFile outputFile = new OutputFile(file, charStreamVersion, new String[] {"STATIC"});

      if (!outputFile.needToWrite)
      {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();

      if (cu_to_insertion_point_1.size() != 0 &&
          ((Token)cu_to_insertion_point_1.elementAt(0)).kind == PACKAGE
      ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.elementAt(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.elementAt(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }
      String prefix = (Options.getStatic() ? "  static " : "  ");
      ostr.println("/**");
      ostr.println(" * An implementation of interface CharStream, where the stream is assumed to");
      ostr.println(" * contain only ASCII characters (with java-like unicode escape processing).");
      ostr.println(" */");
      ostr.println("");
      ostr.println("public class JavaCharStream");
      ostr.println("{");
      ostr.println("/** Whether parser is static. */");
      ostr.println("  public static final boolean staticFlag = " +
          Options.getStatic() + ";");
      ostr.println("  static final int hexval(char c) throws java.io.IOException {");
      ostr.println("    switch(c)");
      ostr.println("    {");
      ostr.println("       case '0' :");
      ostr.println("          return 0;");
      ostr.println("       case '1' :");
      ostr.println("          return 1;");
      ostr.println("       case '2' :");
      ostr.println("          return 2;");
      ostr.println("       case '3' :");
      ostr.println("          return 3;");
      ostr.println("       case '4' :");
      ostr.println("          return 4;");
      ostr.println("       case '5' :");
      ostr.println("          return 5;");
      ostr.println("       case '6' :");
      ostr.println("          return 6;");
      ostr.println("       case '7' :");
      ostr.println("          return 7;");
      ostr.println("       case '8' :");
      ostr.println("          return 8;");
      ostr.println("       case '9' :");
      ostr.println("          return 9;");
      ostr.println("");
      ostr.println("       case 'a' :");
      ostr.println("       case 'A' :");
      ostr.println("          return 10;");
      ostr.println("       case 'b' :");
      ostr.println("       case 'B' :");
      ostr.println("          return 11;");
      ostr.println("       case 'c' :");
      ostr.println("       case 'C' :");
      ostr.println("          return 12;");
      ostr.println("       case 'd' :");
      ostr.println("       case 'D' :");
      ostr.println("          return 13;");
      ostr.println("       case 'e' :");
      ostr.println("       case 'E' :");
      ostr.println("          return 14;");
      ostr.println("       case 'f' :");
      ostr.println("       case 'F' :");
      ostr.println("          return 15;");
      ostr.println("    }");
      ostr.println("");
      ostr.println("    throw new java.io.IOException(); // Should never come here");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Position in buffer. */");
      ostr.println(prefix + "public int bufpos = -1;");
      ostr.println(prefix + "int bufsize;");
      ostr.println(prefix + "int available;");
      ostr.println(prefix + "int tokenBegin;");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println(prefix + "protected int bufline[];");
        ostr.println(prefix + "protected int bufcolumn[];");
        ostr.println("");
        ostr.println(prefix + "protected int column = 0;");
        ostr.println(prefix + "protected int line = 1;");
        ostr.println("");
        ostr.println(prefix + "protected boolean prevCharIsCR = false;");
        ostr.println(prefix + "protected boolean prevCharIsLF = false;");
      }

      ostr.println("");
      ostr.println(prefix + "protected java.io.Reader inputStream;");
      ostr.println("");
      ostr.println(prefix + "protected char[] nextCharBuf;");
      ostr.println(prefix + "protected char[] buffer;");
      ostr.println(prefix + "protected int maxNextCharInd = 0;");
      ostr.println(prefix + "protected int nextCharInd = -1;");
      ostr.println(prefix + "protected int inBuf = 0;");
      ostr.println(prefix + "protected int tabSize = 8;");
      ostr.println("");
      ostr.println(prefix + "protected void setTabSize(int i) { tabSize = i; }");
      ostr.println(prefix + "protected int getTabSize(int i) { return tabSize; }");
      ostr.println("");
      ostr.println(prefix + "protected void ExpandBuff(boolean wrapAround)");
      ostr.println("  {");
      ostr.println("     char[] newbuffer = new char[bufsize + 2048];");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("     int newbufline[] = new int[bufsize + 2048];");
        ostr.println("     int newbufcolumn[] = new int[bufsize + 2048];");
      }

      ostr.println("");
      ostr.println("     try");
      ostr.println("     {");
      ostr.println("        if (wrapAround)");
      ostr.println("        {");
      ostr.println("           System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);");
      ostr.println("           System.arraycopy(buffer, 0, newbuffer,");
      ostr.println("                                             bufsize - tokenBegin, bufpos);");
      ostr.println("           buffer = newbuffer;");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("");
        ostr.println("           System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);");
        ostr.println("           System.arraycopy(bufline, 0, newbufline, bufsize - tokenBegin, bufpos);");
        ostr.println("           bufline = newbufline;");
        ostr.println("");
        ostr.println("           System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);");
        ostr.println("           System.arraycopy(bufcolumn, 0, newbufcolumn, bufsize - tokenBegin, bufpos);");
        ostr.println("           bufcolumn = newbufcolumn;");
      }

      ostr.println("");
      ostr.println("           bufpos += (bufsize - tokenBegin);");
      ostr.println("        }");
      ostr.println("        else");
      ostr.println("        {");
      ostr.println("           System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);");
      ostr.println("           buffer = newbuffer;");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("");
        ostr.println("           System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);");
        ostr.println("           bufline = newbufline;");
        ostr.println("");
        ostr.println("           System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);");
        ostr.println("           bufcolumn = newbufcolumn;");
      }

      ostr.println("");
      ostr.println("           bufpos -= tokenBegin;");
      ostr.println("        }");
      ostr.println("     }");
      ostr.println("     catch (Throwable t)");
      ostr.println("     {");
      ostr.println("        throw new Error(t.getMessage());");
      ostr.println("     }");
      ostr.println("");
      ostr.println("     available = (bufsize += 2048);");
      ostr.println("     tokenBegin = 0;");
      ostr.println("  }");
      ostr.println("");
      ostr.println(prefix + "protected void FillBuff() throws java.io.IOException");
      ostr.println("  {");
      ostr.println("     int i;");
      ostr.println("     if (maxNextCharInd == 4096)");
      ostr.println("        maxNextCharInd = nextCharInd = 0;");
      ostr.println("");
      ostr.println("     try {");
      ostr.println("        if ((i = inputStream.read(nextCharBuf, maxNextCharInd,");
      ostr.println("                                            4096 - maxNextCharInd)) == -1)");
      ostr.println("        {");
      ostr.println("           inputStream.close();");
      ostr.println("           throw new java.io.IOException();");
      ostr.println("        }");
      ostr.println("        else");
      ostr.println("           maxNextCharInd += i;");
      ostr.println("        return;");
      ostr.println("     }");
      ostr.println("     catch(java.io.IOException e) {");
      ostr.println("        if (bufpos != 0)");
      ostr.println("        {");
      ostr.println("           --bufpos;");
      ostr.println("           backup(0);");
      ostr.println("        }");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("        else");
        ostr.println("        {");
        ostr.println("           bufline[bufpos] = line;");
        ostr.println("           bufcolumn[bufpos] = column;");
        ostr.println("        }");
      }

      ostr.println("        throw e;");
      ostr.println("     }");
      ostr.println("  }");
      ostr.println("");
      ostr.println(prefix + "protected char ReadByte() throws java.io.IOException");
      ostr.println("  {");
      ostr.println("     if (++nextCharInd >= maxNextCharInd)");
      ostr.println("        FillBuff();");
      ostr.println("");
      ostr.println("     return nextCharBuf[nextCharInd];");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** @return starting character for token. */");
      ostr.println(prefix + "public char BeginToken() throws java.io.IOException");
      ostr.println("  {     ");
      ostr.println("     if (inBuf > 0)");
      ostr.println("     {");
      ostr.println("        --inBuf;");
      ostr.println("");
      ostr.println("        if (++bufpos == bufsize)");
      ostr.println("           bufpos = 0;");
      ostr.println("");
      ostr.println("        tokenBegin = bufpos;");
      ostr.println("        return buffer[bufpos];");
      ostr.println("     }");
      ostr.println("");
      ostr.println("     tokenBegin = 0;");
      ostr.println("     bufpos = -1;");
      ostr.println("");
      ostr.println("     return readChar();");
      ostr.println("  }     ");
      ostr.println("");
      ostr.println(prefix + "protected void AdjustBuffSize()");
      ostr.println("  {");
      ostr.println("     if (available == bufsize)");
      ostr.println("     {");
      ostr.println("        if (tokenBegin > 2048)");
      ostr.println("        {");
      ostr.println("           bufpos = 0;");
      ostr.println("           available = tokenBegin;");
      ostr.println("        }");
      //ostr.println("        else if (tokenBegin < 0)");
      //ostr.println("           bufpos = 0;");
      ostr.println("        else");
      ostr.println("           ExpandBuff(false);");
      ostr.println("     }");
      ostr.println("     else if (available > tokenBegin)");
      ostr.println("        available = bufsize;");
      ostr.println("     else if ((tokenBegin - available) < 2048)");
      ostr.println("        ExpandBuff(true);");
      ostr.println("     else");
      ostr.println("        available = tokenBegin;");
      ostr.println("  }");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("");
        ostr.println(prefix + "protected void UpdateLineColumn(char c)");
        ostr.println("  {");
        ostr.println("     column++;");
        ostr.println("");
        ostr.println("     if (prevCharIsLF)");
        ostr.println("     {");
        ostr.println("        prevCharIsLF = false;");
        ostr.println("        line += (column = 1);");
        ostr.println("     }");
        ostr.println("     else if (prevCharIsCR)");
        ostr.println("     {");
        ostr.println("        prevCharIsCR = false;");
        ostr.println("        if (c == '\\n')");
        ostr.println("        {");
        ostr.println("           prevCharIsLF = true;");
        ostr.println("        }");
        ostr.println("        else");
        ostr.println("           line += (column = 1);");
        ostr.println("     }");
        ostr.println("");
        ostr.println("     switch (c)");
        ostr.println("     {");
        ostr.println("        case '\\r' :");
        ostr.println("           prevCharIsCR = true;");
        ostr.println("           break;");
        ostr.println("        case '\\n' :");
        ostr.println("           prevCharIsLF = true;");
        ostr.println("           break;");
        ostr.println("        case '\\t' :");
        ostr.println("           column--;");
        ostr.println("           column += (tabSize - (column % tabSize));");
        ostr.println("           break;");
        ostr.println("        default :");
        ostr.println("           break;");
        ostr.println("     }");
        ostr.println("");
        ostr.println("     bufline[bufpos] = line;");
        ostr.println("     bufcolumn[bufpos] = column;");
        ostr.println("  }");
      }

      ostr.println("");
      ostr.println("/** Read a character. */");
      ostr.println(prefix + "public char readChar() throws java.io.IOException");
      ostr.println("  {");
      ostr.println("     if (inBuf > 0)");
      ostr.println("     {");
      ostr.println("        --inBuf;");
      ostr.println("");
      ostr.println("        if (++bufpos == bufsize)");
      ostr.println("           bufpos = 0;");
      ostr.println("");
      ostr.println("        return buffer[bufpos];");
      ostr.println("     }");
      ostr.println("");
      ostr.println("     char c;");
      ostr.println("");
      ostr.println("     if (++bufpos == available)");
      ostr.println("        AdjustBuffSize();");
      ostr.println("");
      ostr.println("     if ((buffer[bufpos] = c = ReadByte()) == '\\\\')");
      ostr.println("     {");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("        UpdateLineColumn(c);");
      }

      ostr.println("");
      ostr.println("        int backSlashCnt = 1;");
      ostr.println("");
      ostr.println("        for (;;) // Read all the backslashes");
      ostr.println("        {");
      ostr.println("           if (++bufpos == available)");
      ostr.println("              AdjustBuffSize();");
      ostr.println("");
      ostr.println("           try");
      ostr.println("           {");
      ostr.println("              if ((buffer[bufpos] = c = ReadByte()) != '\\\\')");
      ostr.println("              {");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("                 UpdateLineColumn(c);");
      }

      ostr.println("                 // found a non-backslash char.");
      ostr.println("                 if ((c == 'u') && ((backSlashCnt & 1) == 1))");
      ostr.println("                 {");
      ostr.println("                    if (--bufpos < 0)");
      ostr.println("                       bufpos = bufsize - 1;");
      ostr.println("");
      ostr.println("                    break;");
      ostr.println("                 }");
      ostr.println("");
      ostr.println("                 backup(backSlashCnt);");
      ostr.println("                 return '\\\\';");
      ostr.println("              }");
      ostr.println("           }");
      ostr.println("           catch(java.io.IOException e)");
      ostr.println("           {");
      ostr.println("              if (backSlashCnt > 1)");
      // We are returning one backslash so we should only backup (count-1)
      ostr.println("                 backup(backSlashCnt-1);");
      ostr.println("");
      ostr.println("              return '\\\\';");
      ostr.println("           }");
      ostr.println("");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("           UpdateLineColumn(c);");
      }

      ostr.println("           backSlashCnt++;");
      ostr.println("        }");
      ostr.println("");
      ostr.println("        // Here, we have seen an odd number of backslash's followed by a 'u'");
      ostr.println("        try");
      ostr.println("        {");
      ostr.println("           while ((c = ReadByte()) == 'u')");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("              ++column;");
      }
      else
      {
        ostr.println("     ;");
      }

      ostr.println("");
      ostr.println("           buffer[bufpos] = c = (char)(hexval(c) << 12 |");
      ostr.println("                                       hexval(ReadByte()) << 8 |");
      ostr.println("                                       hexval(ReadByte()) << 4 |");
      ostr.println("                                       hexval(ReadByte()));");
      ostr.println("");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("           column += 4;");
      }

      ostr.println("        }");
      ostr.println("        catch(java.io.IOException e)");
      ostr.println("        {");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("           throw new Error(\"Invalid escape character at line \" + line +");
        ostr.println("                                         \" column \" + column + \".\");");
      }
      else
      {
        ostr.println("           throw new Error(\"Invalid escape character in input\");");
      }

      ostr.println("        }");
      ostr.println("");
      ostr.println("        if (backSlashCnt == 1)");
      ostr.println("           return c;");
      ostr.println("        else");
      ostr.println("        {");
      ostr.println("           backup(backSlashCnt - 1);");
      ostr.println("           return '\\\\';");
      ostr.println("        }");
      ostr.println("     }");
      ostr.println("     else");
      ostr.println("     {");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("        UpdateLineColumn(c);");
      }

      ostr.println("        return c;");
      ostr.println("     }");
      ostr.println("  }");
      ostr.println("");
      if (Options.jdkVersionAtLeast(1.5)) {
        ostr.println("  @Deprecated");
      }
      ostr.println("  /**");
      ostr.println("   * @deprecated ");
      ostr.println("   * @see #getEndColumn");
      ostr.println("   */");
      ostr.println(prefix + "public int getColumn() {");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("     return bufcolumn[bufpos];");
      }
      else
      {
        ostr.println("     return -1;");
      }

      ostr.println("  }");
      ostr.println("");
      if (Options.jdkVersionAtLeast(1.5)) {
        ostr.println("  @Deprecated");
      }
      ostr.println("  /**");
      ostr.println("   * @deprecated ");
      ostr.println("   * @see #getEndLine");
      ostr.println("   */");
      ostr.println(prefix + "public int getLine() {");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("     return bufline[bufpos];");
      }
      else
      {
        ostr.println("     return -1;");
      }

      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Get end column. */");
      ostr.println(prefix + "public int getEndColumn() {");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("     return bufcolumn[bufpos];");
      }
      else
      {
        ostr.println("     return -1;");
      }

      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Get end line. */");
      ostr.println(prefix + "public int getEndLine() {");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("     return bufline[bufpos];");
      }
      else
      {
        ostr.println("     return -1;");
      }

      ostr.println("  }");
      ostr.println("");
      ostr.println("/** @return column of token start */");
      ostr.println(prefix + "public int getBeginColumn() {");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("     return bufcolumn[tokenBegin];");
      }
      else
      {
        ostr.println("     return -1;");
      }

      ostr.println("  }");
      ostr.println("");
      ostr.println("/** @return line number of token start */");
      ostr.println(prefix + "public int getBeginLine() {");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("     return bufline[tokenBegin];");
      }
      else
      {
        ostr.println("     return -1;");
      }

      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Retreat. */");
      ostr.println(prefix + "public void backup(int amount) {");
      ostr.println("");
      ostr.println("    inBuf += amount;");
      ostr.println("    if ((bufpos -= amount) < 0)");
      ostr.println("       bufpos += bufsize;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Constructor. */");
      ostr.println("  public JavaCharStream(java.io.Reader dstream,");
      ostr.println("                 int startline, int startcolumn, int buffersize)");
      ostr.println("  {");

      if (Options.getStatic())
      {
        ostr.println("    if (inputStream != null)");
        ostr.println("       throw new Error(\"\\n   ERROR: Second call to the constructor " +
        "of a static JavaCharStream.\\n\" +");
        ostr.println("       \"       You must either use ReInit() or set the JavaCC option STATIC to false\\n\" +");
        ostr.println("       \"       during the generation of this class.\");");
      }

      ostr.println("    inputStream = dstream;");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("    line = startline;");
        ostr.println("    column = startcolumn - 1;");
      }

      ostr.println("");
      ostr.println("    available = bufsize = buffersize;");
      ostr.println("    buffer = new char[buffersize];");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("    bufline = new int[buffersize];");
        ostr.println("    bufcolumn = new int[buffersize];");
      }

      ostr.println("    nextCharBuf = new char[4096];");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Constructor. */");
      ostr.println("  public JavaCharStream(java.io.Reader dstream,");
      ostr.println("                                        int startline, int startcolumn)");
      ostr.println("  {");
      ostr.println("     this(dstream, startline, startcolumn, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Constructor. */");
      ostr.println("  public JavaCharStream(java.io.Reader dstream)");
      ostr.println("  {");
      ostr.println("     this(dstream, 1, 1, 4096);");
      ostr.println("  }");
      ostr.println("/** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.Reader dstream,");
      ostr.println("                 int startline, int startcolumn, int buffersize)");
      ostr.println("  {");
      ostr.println("    inputStream = dstream;");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("    line = startline;");
        ostr.println("    column = startcolumn - 1;");
      }

      ostr.println("");
      ostr.println("    if (buffer == null || buffersize != buffer.length)");
      ostr.println("    {");
      ostr.println("      available = bufsize = buffersize;");
      ostr.println("      buffer = new char[buffersize];");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("      bufline = new int[buffersize];");
        ostr.println("      bufcolumn = new int[buffersize];");
      }

      ostr.println("      nextCharBuf = new char[4096];");
      ostr.println("    }");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("    prevCharIsLF = prevCharIsCR = false;");
      }

      ostr.println("    tokenBegin = inBuf = maxNextCharInd = 0;");
      ostr.println("    nextCharInd = bufpos = -1;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.Reader dstream,");
      ostr.println("                                        int startline, int startcolumn)");
      ostr.println("  {");
      ostr.println("     ReInit(dstream, startline, startcolumn, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.Reader dstream)");
      ostr.println("  {");
      ostr.println("     ReInit(dstream, 1, 1, 4096);");
      ostr.println("  }");
      ostr.println("/** Constructor. */");
      ostr.println("  public JavaCharStream(java.io.InputStream dstream, String encoding, int startline,");
      ostr.println("  int startcolumn, int buffersize) throws java.io.UnsupportedEncodingException");
      ostr.println("  {");
      ostr.println("     this(encoding == null ? new java.io.InputStreamReader(dstream) : " +
      "new java.io.InputStreamReader(dstream, encoding), startline, startcolumn, buffersize);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Constructor. */");
      ostr.println("  public JavaCharStream(java.io.InputStream dstream, int startline,");
      ostr.println("  int startcolumn, int buffersize)");
      ostr.println("  {");
      ostr.println("     this(new java.io.InputStreamReader(dstream), startline, startcolumn, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Constructor. */");
      ostr.println("  public JavaCharStream(java.io.InputStream dstream, String encoding, int startline,");
      ostr.println("                        int startcolumn) throws java.io.UnsupportedEncodingException");
      ostr.println("  {");
      ostr.println("     this(dstream, encoding, startline, startcolumn, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Constructor. */");
      ostr.println("  public JavaCharStream(java.io.InputStream dstream, int startline,");
      ostr.println("                        int startcolumn)");
      ostr.println("  {");
      ostr.println("     this(dstream, startline, startcolumn, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Constructor. */");
      ostr.println("  public JavaCharStream(java.io.InputStream dstream, String encoding) " +
      "throws java.io.UnsupportedEncodingException");
      ostr.println("  {");
      ostr.println("     this(dstream, encoding, 1, 1, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Constructor. */");
      ostr.println("  public JavaCharStream(java.io.InputStream dstream)");
      ostr.println("  {");
      ostr.println("     this(dstream, 1, 1, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.InputStream dstream, String encoding, int startline,");
      ostr.println("  int startcolumn, int buffersize) throws java.io.UnsupportedEncodingException");
      ostr.println("  {");
      ostr.println("     ReInit(encoding == null ? new java.io.InputStreamReader(dstream) : " +
      "new java.io.InputStreamReader(dstream, encoding), startline, startcolumn, buffersize);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.InputStream dstream, int startline,");
      ostr.println("  int startcolumn, int buffersize)");
      ostr.println("  {");
      ostr.println("     ReInit(new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);");
      ostr.println("  }");
      ostr.println("/** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.InputStream dstream, String encoding, int startline,");
      ostr.println("                     int startcolumn) throws java.io.UnsupportedEncodingException");
      ostr.println("  {");
      ostr.println("     ReInit(dstream, encoding, startline, startcolumn, 4096);");
      ostr.println("  }");
      ostr.println("/** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.InputStream dstream, int startline,");
      ostr.println("                     int startcolumn)");
      ostr.println("  {");
      ostr.println("     ReInit(dstream, startline, startcolumn, 4096);");
      ostr.println("  }");
      ostr.println("/** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.InputStream dstream, String encoding) " +
      "throws java.io.UnsupportedEncodingException");
      ostr.println("  {");
      ostr.println("     ReInit(dstream, encoding, 1, 1, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.InputStream dstream)");
      ostr.println("  {");
      ostr.println("     ReInit(dstream, 1, 1, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** @return token image as String */");
      ostr.println(prefix + "public String GetImage()");
      ostr.println("  {");
      ostr.println("     if (bufpos >= tokenBegin)");
      ostr.println("        return new String(buffer, tokenBegin, bufpos - tokenBegin + 1);");
      ostr.println("     else");
      ostr.println("        return new String(buffer, tokenBegin, bufsize - tokenBegin) +");
      ostr.println("                              new String(buffer, 0, bufpos + 1);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** @return suffix */");
      ostr.println(prefix + "public char[] GetSuffix(int len)");
      ostr.println("  {");
      ostr.println("     char[] ret = new char[len];");
      ostr.println("");
      ostr.println("     if ((bufpos + 1) >= len)");
      ostr.println("        System.arraycopy(buffer, bufpos - len + 1, ret, 0, len);");
      ostr.println("     else");
      ostr.println("     {");
      ostr.println("        System.arraycopy(buffer, bufsize - (len - bufpos - 1), ret, 0,");
      ostr.println("                                                          len - bufpos - 1);");
      ostr.println("        System.arraycopy(buffer, 0, ret, len - bufpos - 1, bufpos + 1);");
      ostr.println("     }");
      ostr.println("");
      ostr.println("     return ret;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Set buffers back to null when finished. */");
      ostr.println(prefix + "public void Done()");
      ostr.println("  {");
      ostr.println("     nextCharBuf = null;");
      ostr.println("     buffer = null;");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("     bufline = null;");
        ostr.println("     bufcolumn = null;");
      }

      ostr.println("  }");

      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("");
        ostr.println("  /**");
        ostr.println("   * Method to adjust line and column numbers for the start of a token.");
        ostr.println("   */");
        ostr.println(prefix + "public void adjustBeginLineColumn(int newLine, int newCol)");
        ostr.println("  {");
        ostr.println("     int start = tokenBegin;");
        ostr.println("     int len;");
        ostr.println("");
        ostr.println("     if (bufpos >= tokenBegin)");
        ostr.println("     {");
        ostr.println("        len = bufpos - tokenBegin + inBuf + 1;");
        ostr.println("     }");
        ostr.println("     else");
        ostr.println("     {");
        ostr.println("        len = bufsize - tokenBegin + bufpos + 1 + inBuf;");
        ostr.println("     }");
        ostr.println("");
        ostr.println("     int i = 0, j = 0, k = 0;");
        ostr.println("     int nextColDiff = 0, columnDiff = 0;");
        ostr.println("");
        ostr.println("     while (i < len &&");
        ostr.println("            bufline[j = start % bufsize] == bufline[k = ++start % bufsize])");
        ostr.println("     {");
        ostr.println("        bufline[j] = newLine;");
        ostr.println("        nextColDiff = columnDiff + bufcolumn[k] - bufcolumn[j];");
        ostr.println("        bufcolumn[j] = newCol + columnDiff;");
        ostr.println("        columnDiff = nextColDiff;");
        ostr.println("        i++;");
        ostr.println("     } ");
        ostr.println("");
        ostr.println("     if (i < len)");
        ostr.println("     {");
        ostr.println("        bufline[j] = newLine++;");
        ostr.println("        bufcolumn[j] = newCol + columnDiff;");
        ostr.println("");
        ostr.println("        while (i++ < len)");
        ostr.println("        {");
        ostr.println("           if (bufline[j = start % bufsize] != bufline[++start % bufsize])");
        ostr.println("              bufline[j] = newLine++;");
        ostr.println("           else");
        ostr.println("              bufline[j] = newLine;");
        ostr.println("        }");
        ostr.println("     }");
        ostr.println("");
        ostr.println("     line = bufline[j];");
        ostr.println("     column = bufcolumn[j];");
        ostr.println("  }");
        ostr.println("");
      }

      ostr.println("}");
      outputFile.close();
    } catch (IOException e) {
      System.err.println("Failed to create JavaCharStream " + e);
      JavaCCErrors.semantic_error("Could not open file JavaCharStream.java for writing.");
      throw new Error();
    }
  }

  public static void gen_SimpleCharStream() {
    try {
      final File file = new File(Options.getOutputDirectory(), "SimpleCharStream.java");
      final OutputFile outputFile = new OutputFile(file, charStreamVersion, new String[] {"STATIC"});

      if (!outputFile.needToWrite)
      {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();

      if (cu_to_insertion_point_1.size() != 0 &&
          ((Token)cu_to_insertion_point_1.elementAt(0)).kind == PACKAGE
         ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.elementAt(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.elementAt(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }
      String prefix = (Options.getStatic() ? "  static " : "  ");
      ostr.println("/**");
      ostr.println(" * An implementation of interface CharStream, where the stream is assumed to");
      ostr.println(" * contain only ASCII characters (without unicode processing).");
      ostr.println(" */");
      ostr.println("");
      ostr.println("public class SimpleCharStream");
      ostr.println("{");
      ostr.println("/** Whether parser is static. */");
      ostr.println("  public static final boolean staticFlag = " +
                                           Options.getStatic() + ";");
      ostr.println(prefix + "int bufsize;");
      ostr.println(prefix + "int available;");
      ostr.println(prefix + "int tokenBegin;");
      ostr.println("/** Position in buffer. */");
      ostr.println(prefix + "public int bufpos = -1;");

      if (OtherFilesGen.keepLineCol)
      {
         ostr.println(prefix + "protected int bufline[];");
         ostr.println(prefix + "protected int bufcolumn[];");
         ostr.println("");
         ostr.println(prefix + "protected int column = 0;");
         ostr.println(prefix + "protected int line = 1;");
         ostr.println("");
         ostr.println(prefix + "protected boolean prevCharIsCR = false;");
         ostr.println(prefix + "protected boolean prevCharIsLF = false;");
      }

      ostr.println("");
      ostr.println(prefix + "protected java.io.Reader inputStream;");
      ostr.println("");
      ostr.println(prefix + "protected char[] buffer;");
      ostr.println(prefix + "protected int maxNextCharInd = 0;");
      ostr.println(prefix + "protected int inBuf = 0;");
      ostr.println(prefix + "protected int tabSize = 8;");
      ostr.println("");
      ostr.println(prefix + "protected void setTabSize(int i) { tabSize = i; }");
      ostr.println(prefix + "protected int getTabSize(int i) { return tabSize; }");
      ostr.println("");
      ostr.println("");
      ostr.println(prefix + "protected void ExpandBuff(boolean wrapAround)");
      ostr.println("  {");
      ostr.println("     char[] newbuffer = new char[bufsize + 2048];");

      if (OtherFilesGen.keepLineCol)
      {
         ostr.println("     int newbufline[] = new int[bufsize + 2048];");
         ostr.println("     int newbufcolumn[] = new int[bufsize + 2048];");
      }

      ostr.println("");
      ostr.println("     try");
      ostr.println("     {");
      ostr.println("        if (wrapAround)");
      ostr.println("        {");
      ostr.println("           System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);");
      ostr.println("           System.arraycopy(buffer, 0, newbuffer,");
      ostr.println("                                             bufsize - tokenBegin, bufpos);");
      ostr.println("           buffer = newbuffer;");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("");
      ostr.println("           System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);");
      ostr.println("           System.arraycopy(bufline, 0, newbufline, bufsize - tokenBegin, bufpos);");
      ostr.println("           bufline = newbufline;");
      ostr.println("");
      ostr.println("           System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);");
      ostr.println("           System.arraycopy(bufcolumn, 0, newbufcolumn, bufsize - tokenBegin, bufpos);");
      ostr.println("           bufcolumn = newbufcolumn;");
      }

      ostr.println("");
      ostr.println("           maxNextCharInd = (bufpos += (bufsize - tokenBegin));");
      ostr.println("        }");
      ostr.println("        else");
      ostr.println("        {");
      ostr.println("           System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);");
      ostr.println("           buffer = newbuffer;");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("");
      ostr.println("           System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);");
      ostr.println("           bufline = newbufline;");
      ostr.println("");
      ostr.println("           System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);");
      ostr.println("           bufcolumn = newbufcolumn;");
      }

      ostr.println("");
      ostr.println("           maxNextCharInd = (bufpos -= tokenBegin);");
      ostr.println("        }");
      ostr.println("     }");
      ostr.println("     catch (Throwable t)");
      ostr.println("     {");
      ostr.println("        throw new Error(t.getMessage());");
      ostr.println("     }");
      ostr.println("");
      ostr.println("");
      ostr.println("     bufsize += 2048;");
      ostr.println("     available = bufsize;");
      ostr.println("     tokenBegin = 0;");
      ostr.println("  }");
      ostr.println("");
      ostr.println(prefix + "protected void FillBuff() throws java.io.IOException");
      ostr.println("  {");
      ostr.println("     if (maxNextCharInd == available)");
      ostr.println("     {");
      ostr.println("        if (available == bufsize)");
      ostr.println("        {");
      ostr.println("           if (tokenBegin > 2048)");
      ostr.println("           {");
      ostr.println("              bufpos = maxNextCharInd = 0;");
      ostr.println("              available = tokenBegin;");
      ostr.println("           }");
      ostr.println("           else if (tokenBegin < 0)");
      ostr.println("              bufpos = maxNextCharInd = 0;");
      ostr.println("           else");
      ostr.println("              ExpandBuff(false);");
      ostr.println("        }");
      ostr.println("        else if (available > tokenBegin)");
      ostr.println("           available = bufsize;");
      ostr.println("        else if ((tokenBegin - available) < 2048)");
      ostr.println("           ExpandBuff(true);");
      ostr.println("        else");
      ostr.println("           available = tokenBegin;");
      ostr.println("     }");
      ostr.println("");
      ostr.println("     int i;");
      ostr.println("     try {");
      ostr.println("        if ((i = inputStream.read(buffer, maxNextCharInd,");
      ostr.println("                                    available - maxNextCharInd)) == -1)");
      ostr.println("        {");
      ostr.println("           inputStream.close();");
      ostr.println("           throw new java.io.IOException();");
      ostr.println("        }");
      ostr.println("        else");
      ostr.println("           maxNextCharInd += i;");
      ostr.println("        return;");
      ostr.println("     }");
      ostr.println("     catch(java.io.IOException e) {");
      ostr.println("        --bufpos;");
      ostr.println("        backup(0);");
      ostr.println("        if (tokenBegin == -1)");
      ostr.println("           tokenBegin = bufpos;");
      ostr.println("        throw e;");
      ostr.println("     }");
      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Start. */");
      ostr.println(prefix + "public char BeginToken() throws java.io.IOException");
      ostr.println("  {");
      ostr.println("     tokenBegin = -1;");
      ostr.println("     char c = readChar();");
      ostr.println("     tokenBegin = bufpos;");
      ostr.println("");
      ostr.println("     return c;");
      ostr.println("  }");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("");
      ostr.println(prefix + "protected void UpdateLineColumn(char c)");
      ostr.println("  {");
      ostr.println("     column++;");
      ostr.println("");
      ostr.println("     if (prevCharIsLF)");
      ostr.println("     {");
      ostr.println("        prevCharIsLF = false;");
      ostr.println("        line += (column = 1);");
      ostr.println("     }");
      ostr.println("     else if (prevCharIsCR)");
      ostr.println("     {");
      ostr.println("        prevCharIsCR = false;");
      ostr.println("        if (c == '\\n')");
      ostr.println("        {");
      ostr.println("           prevCharIsLF = true;");
      ostr.println("        }");
      ostr.println("        else");
      ostr.println("           line += (column = 1);");
      ostr.println("     }");
      ostr.println("");
      ostr.println("     switch (c)");
      ostr.println("     {");
      ostr.println("        case '\\r' :");
      ostr.println("           prevCharIsCR = true;");
      ostr.println("           break;");
      ostr.println("        case '\\n' :");
      ostr.println("           prevCharIsLF = true;");
      ostr.println("           break;");
      ostr.println("        case '\\t' :");
      ostr.println("           column--;");
      ostr.println("           column += (tabSize - (column % tabSize));");
      ostr.println("           break;");
      ostr.println("        default :");
      ostr.println("           break;");
      ostr.println("     }");
      ostr.println("");
      ostr.println("     bufline[bufpos] = line;");
      ostr.println("     bufcolumn[bufpos] = column;");
      ostr.println("  }");
      }

      ostr.println("");
      ostr.println("/** Read a character. */");
      ostr.println(prefix + "public char readChar() throws java.io.IOException");
      ostr.println("  {");
      ostr.println("     if (inBuf > 0)");
      ostr.println("     {");
      ostr.println("        --inBuf;");
      ostr.println("");
      ostr.println("        if (++bufpos == bufsize)");
      ostr.println("           bufpos = 0;");
      ostr.println("");
      ostr.println("        return buffer[bufpos];");
      ostr.println("     }");
      ostr.println("");
      ostr.println("     if (++bufpos >= maxNextCharInd)");
      ostr.println("        FillBuff();");
      ostr.println("");
      ostr.println("     char c = buffer[bufpos];");
      ostr.println("");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("     UpdateLineColumn(c);");
      }

      ostr.println("     return c;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * @deprecated ");
      ostr.println("   * @see #getEndColumn");
      ostr.println("   */");
      ostr.println("");
      ostr.println(prefix + "public int getColumn() {");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("     return bufcolumn[bufpos];");
      }
      else
      {
         ostr.println("     return -1;");
      }

      ostr.println("  }");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * @deprecated ");
      ostr.println("   * @see #getEndLine");
      ostr.println("   */");
      ostr.println("");
      ostr.println(prefix + "public int getLine() {");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("     return bufline[bufpos];");
      }
      else
      {
         ostr.println("     return -1;");
      }

      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Get token end column number. */");
      ostr.println(prefix + "public int getEndColumn() {");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("     return bufcolumn[bufpos];");
      }
      else
      {
         ostr.println("     return -1;");
      }

      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Get token end line number. */");
      ostr.println(prefix + "public int getEndLine() {");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("     return bufline[bufpos];");
      }
      else
      {
         ostr.println("     return -1;");
      }

      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Get token beginning column number. */");
      ostr.println(prefix + "public int getBeginColumn() {");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("     return bufcolumn[tokenBegin];");
      }
      else
      {
         ostr.println("     return -1;");
      }

      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Get token beginning line number. */");
      ostr.println(prefix + "public int getBeginLine() {");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("     return bufline[tokenBegin];");
      }
      else
      {
         ostr.println("     return -1;");
      }

      ostr.println("  }");
      ostr.println("");
      ostr.println("/** Backup a number of characters. */");
      ostr.println(prefix + "public void backup(int amount) {");
      ostr.println("");
      ostr.println("    inBuf += amount;");
      ostr.println("    if ((bufpos -= amount) < 0)");
      ostr.println("       bufpos += bufsize;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Constructor. */");
      ostr.println("  public SimpleCharStream(java.io.Reader dstream, int startline,");
      ostr.println("  int startcolumn, int buffersize)");
      ostr.println("  {");

      if (Options.getStatic())
      {
         ostr.println("    if (inputStream != null)");
         ostr.println("       throw new Error(\"\\n   " +
                 "ERROR: Second call to the constructor of a static SimpleCharStream.\\n\" +");
         ostr.println("       \"       You must either use ReInit() or set the JavaCC option STATIC to false\\n\" +");
         ostr.println("       \"       during the generation of this class.\");");
      }

      ostr.println("    inputStream = dstream;");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("    line = startline;");
      ostr.println("    column = startcolumn - 1;");
      }

      ostr.println("");
      ostr.println("    available = bufsize = buffersize;");
      ostr.println("    buffer = new char[buffersize];");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("    bufline = new int[buffersize];");
      ostr.println("    bufcolumn = new int[buffersize];");
      }

      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Constructor. */");
      ostr.println("  public SimpleCharStream(java.io.Reader dstream, int startline,");
      ostr.println("                          int startcolumn)");
      ostr.println("  {");
      ostr.println("     this(dstream, startline, startcolumn, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Constructor. */");
      ostr.println("  public SimpleCharStream(java.io.Reader dstream)");
      ostr.println("  {");
      ostr.println("     this(dstream, 1, 1, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.Reader dstream, int startline,");
      ostr.println("  int startcolumn, int buffersize)");
      ostr.println("  {");
      ostr.println("    inputStream = dstream;");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("    line = startline;");
      ostr.println("    column = startcolumn - 1;");
      }

      ostr.println("");
      ostr.println("    if (buffer == null || buffersize != buffer.length)");
      ostr.println("    {");
      ostr.println("      available = bufsize = buffersize;");
      ostr.println("      buffer = new char[buffersize];");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("      bufline = new int[buffersize];");
      ostr.println("      bufcolumn = new int[buffersize];");
      }

      ostr.println("    }");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("    prevCharIsLF = prevCharIsCR = false;");
      }

      ostr.println("    tokenBegin = inBuf = maxNextCharInd = 0;");
      ostr.println("    bufpos = -1;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.Reader dstream, int startline,");
      ostr.println("                     int startcolumn)");
      ostr.println("  {");
      ostr.println("     ReInit(dstream, startline, startcolumn, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.Reader dstream)");
      ostr.println("  {");
      ostr.println("     ReInit(dstream, 1, 1, 4096);");
      ostr.println("  }");
      ostr.println("  /** Constructor. */");
      ostr.println("  public SimpleCharStream(java.io.InputStream dstream, String encoding, int startline,");
      ostr.println("  int startcolumn, int buffersize) throws java.io.UnsupportedEncodingException");
      ostr.println("  {");
      ostr.println("     this(encoding == null ? new java.io.InputStreamReader(dstream) : " +
              "new java.io.InputStreamReader(dstream, encoding), startline, startcolumn, buffersize);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Constructor. */");
      ostr.println("  public SimpleCharStream(java.io.InputStream dstream, int startline,");
      ostr.println("  int startcolumn, int buffersize)");
      ostr.println("  {");
      ostr.println("     this(new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Constructor. */");
      ostr.println("  public SimpleCharStream(java.io.InputStream dstream, String encoding, int startline,");
      ostr.println("                          int startcolumn) throws java.io.UnsupportedEncodingException");
      ostr.println("  {");
      ostr.println("     this(dstream, encoding, startline, startcolumn, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Constructor. */");
      ostr.println("  public SimpleCharStream(java.io.InputStream dstream, int startline,");
      ostr.println("                          int startcolumn)");
      ostr.println("  {");
      ostr.println("     this(dstream, startline, startcolumn, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Constructor. */");
      ostr.println("  public SimpleCharStream(java.io.InputStream dstream, String encoding) " +
              "throws java.io.UnsupportedEncodingException");
      ostr.println("  {");
      ostr.println("     this(dstream, encoding, 1, 1, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Constructor. */");
      ostr.println("  public SimpleCharStream(java.io.InputStream dstream)");
      ostr.println("  {");
      ostr.println("     this(dstream, 1, 1, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.InputStream dstream, String encoding, int startline,");
      ostr.println("                          int startcolumn, int buffersize) " +
              "throws java.io.UnsupportedEncodingException");
      ostr.println("  {");
      ostr.println("     ReInit(encoding == null ? new java.io.InputStreamReader(dstream) : " +
              "new java.io.InputStreamReader(dstream, encoding), startline, startcolumn, buffersize);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.InputStream dstream, int startline,");
      ostr.println("                          int startcolumn, int buffersize)");
      ostr.println("  {");
      ostr.println("     ReInit(new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.InputStream dstream, String encoding) " +
              "throws java.io.UnsupportedEncodingException");
      ostr.println("  {");
      ostr.println("     ReInit(dstream, encoding, 1, 1, 4096);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.InputStream dstream)");
      ostr.println("  {");
      ostr.println("     ReInit(dstream, 1, 1, 4096);");
      ostr.println("  }");
      ostr.println("  /** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.InputStream dstream, String encoding, int startline,");
      ostr.println("                     int startcolumn) throws java.io.UnsupportedEncodingException");
      ostr.println("  {");
      ostr.println("     ReInit(dstream, encoding, startline, startcolumn, 4096);");
      ostr.println("  }");
      ostr.println("  /** Reinitialise. */");
      ostr.println("  public void ReInit(java.io.InputStream dstream, int startline,");
      ostr.println("                     int startcolumn)");
      ostr.println("  {");
      ostr.println("     ReInit(dstream, startline, startcolumn, 4096);");
      ostr.println("  }");
      ostr.println("  /** Get token literal value. */");
      ostr.println(prefix + "public String GetImage()");
      ostr.println("  {");
      ostr.println("     if (bufpos >= tokenBegin)");
      ostr.println("        return new String(buffer, tokenBegin, bufpos - tokenBegin + 1);");
      ostr.println("     else");
      ostr.println("        return new String(buffer, tokenBegin, bufsize - tokenBegin) +");
      ostr.println("                              new String(buffer, 0, bufpos + 1);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Get the suffix. */");
      ostr.println(prefix + "public char[] GetSuffix(int len)");
      ostr.println("  {");
      ostr.println("     char[] ret = new char[len];");
      ostr.println("");
      ostr.println("     if ((bufpos + 1) >= len)");
      ostr.println("        System.arraycopy(buffer, bufpos - len + 1, ret, 0, len);");
      ostr.println("     else");
      ostr.println("     {");
      ostr.println("        System.arraycopy(buffer, bufsize - (len - bufpos - 1), ret, 0,");
      ostr.println("                                                          len - bufpos - 1);");
      ostr.println("        System.arraycopy(buffer, 0, ret, len - bufpos - 1, bufpos + 1);");
      ostr.println("     }");
      ostr.println("");
      ostr.println("     return ret;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Reset buffer when finished. */");
      ostr.println(prefix + "public void Done()");
      ostr.println("  {");
      ostr.println("     buffer = null;");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("     bufline = null;");
      ostr.println("     bufcolumn = null;");
      }

      ostr.println("  }");

      if (OtherFilesGen.keepLineCol)
      {
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Method to adjust line and column numbers for the start of a token.");
      ostr.println("   */");
      ostr.println(prefix + "public void adjustBeginLineColumn(int newLine, int newCol)");
      ostr.println("  {");
      ostr.println("     int start = tokenBegin;");
      ostr.println("     int len;");
      ostr.println("");
      ostr.println("     if (bufpos >= tokenBegin)");
      ostr.println("     {");
      ostr.println("        len = bufpos - tokenBegin + inBuf + 1;");
      ostr.println("     }");
      ostr.println("     else");
      ostr.println("     {");
      ostr.println("        len = bufsize - tokenBegin + bufpos + 1 + inBuf;");
      ostr.println("     }");
      ostr.println("");
      ostr.println("     int i = 0, j = 0, k = 0;");
      ostr.println("     int nextColDiff = 0, columnDiff = 0;");
      ostr.println("");
      ostr.println("     while (i < len &&");
      ostr.println("            bufline[j = start % bufsize] == bufline[k = ++start % bufsize])");
      ostr.println("     {");
      ostr.println("        bufline[j] = newLine;");
      ostr.println("        nextColDiff = columnDiff + bufcolumn[k] - bufcolumn[j];");
      ostr.println("        bufcolumn[j] = newCol + columnDiff;");
      ostr.println("        columnDiff = nextColDiff;");
      ostr.println("        i++;");
      ostr.println("     } ");
      ostr.println("");
      ostr.println("     if (i < len)");
      ostr.println("     {");
      ostr.println("        bufline[j] = newLine++;");
      ostr.println("        bufcolumn[j] = newCol + columnDiff;");
      ostr.println("");
      ostr.println("        while (i++ < len)");
      ostr.println("        {");
      ostr.println("           if (bufline[j = start % bufsize] != bufline[++start % bufsize])");
      ostr.println("              bufline[j] = newLine++;");
      ostr.println("           else");
      ostr.println("              bufline[j] = newLine;");
      ostr.println("        }");
      ostr.println("     }");
      ostr.println("");
      ostr.println("     line = bufline[j];");
      ostr.println("     column = bufcolumn[j];");
      ostr.println("  }");
      ostr.println("");
      }

      ostr.println("}");
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
      final OutputFile outputFile = new OutputFile(file, charStreamVersion, new String[] {"STATIC"});

      if (!outputFile.needToWrite)
      {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();

      if (cu_to_insertion_point_1.size() != 0 &&
          ((Token)cu_to_insertion_point_1.elementAt(0)).kind == PACKAGE
         ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.elementAt(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.elementAt(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }
      ostr.println("/**");
      ostr.println(" * This interface describes a character stream that maintains line and");
      ostr.println(" * column number positions of the characters.  It also has the capability");
      ostr.println(" * to backup the stream to some extent.  An implementation of this");
      ostr.println(" * interface is used in the TokenManager implementation generated by");
      ostr.println(" * JavaCCParser.");
      ostr.println(" *");
      ostr.println(" * All the methods except backup can be implemented in any fashion. backup");
      ostr.println(" * needs to be implemented correctly for the correct operation of the lexer.");
      ostr.println(" * Rest of the methods are all used to get information like line number,");
      ostr.println(" * column number and the String that constitutes a token and are not used");
      ostr.println(" * by the lexer. Hence their implementation won't affect the generated lexer's");
      ostr.println(" * operation.");
      ostr.println(" */");
      ostr.println("");
      ostr.println("public interface CharStream {");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Returns the next character from the selected input.  The method");
      ostr.println("   * of selecting the input is the responsibility of the class");
      ostr.println("   * implementing this interface.  Can throw any java.io.IOException.");
      ostr.println("   */");
      ostr.println("  char readChar() throws java.io.IOException;");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Returns the column position of the character last read.");
      ostr.println("   * @deprecated ");
      ostr.println("   * @see #getEndColumn");
      ostr.println("   */");
      ostr.println("  int getColumn();");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Returns the line number of the character last read.");
      ostr.println("   * @deprecated ");
      ostr.println("   * @see #getEndLine");
      ostr.println("   */");
      ostr.println("  int getLine();");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Returns the column number of the last character for current token (being");
      ostr.println("   * matched after the last call to BeginTOken).");
      ostr.println("   */");
      ostr.println("  int getEndColumn();");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Returns the line number of the last character for current token (being");
      ostr.println("   * matched after the last call to BeginTOken).");
      ostr.println("   */");
      ostr.println("  int getEndLine();");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Returns the column number of the first character for current token (being");
      ostr.println("   * matched after the last call to BeginTOken).");
      ostr.println("   */");
      ostr.println("  int getBeginColumn();");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Returns the line number of the first character for current token (being");
      ostr.println("   * matched after the last call to BeginTOken).");
      ostr.println("   */");
      ostr.println("  int getBeginLine();");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Backs up the input stream by amount steps. Lexer calls this method if it");
      ostr.println("   * had already read some characters, but could not use them to match a");
      ostr.println("   * (longer) token. So, they will be used again as the prefix of the next");
      ostr.println("   * token and it is the implemetation's responsibility to do this right.");
      ostr.println("   */");
      ostr.println("  void backup(int amount);");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Returns the next character that marks the beginning of the next token.");
      ostr.println("   * All characters must remain in the buffer between two successive calls");
      ostr.println("   * to this method to implement backup correctly.");
      ostr.println("   */");
      ostr.println("  char BeginToken() throws java.io.IOException;");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Returns a string made up of characters from the marked token beginning ");
      ostr.println("   * to the current buffer position. Implementations have the choice of returning");
      ostr.println("   * anything that they want to. For example, for efficiency, one might decide");
      ostr.println("   * to just return null, which is a valid implementation.");
      ostr.println("   */");
      ostr.println("  String GetImage();");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Returns an array of characters that make up the suffix of length 'len' for");
      ostr.println("   * the currently matched token. This is used to build up the matched string");
      ostr.println("   * for use in actions in the case of MORE. A simple and inefficient");
      ostr.println("   * implementation of this is as follows :");
      ostr.println("   *");
      ostr.println("   *   {");
      ostr.println("   *      String t = GetImage();");
      ostr.println("   *      return t.substring(t.length() - len, t.length()).toCharArray();");
      ostr.println("   *   }");
      ostr.println("   */");
      ostr.println("  char[] GetSuffix(int len);");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * The lexer calls this function to indicate that it is done with the stream");
      ostr.println("   * and hence implementations can free any resources held by this class.");
      ostr.println("   * Again, the body of this function can be just empty and it will not");
      ostr.println("   * affect the lexer's operation.");
      ostr.println("   */");
      ostr.println("  void Done();");
      ostr.println("");
      ostr.println("}");
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
          ((Token)cu_to_insertion_point_1.elementAt(0)).kind == PACKAGE
         ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.elementAt(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.elementAt(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }
      ostr.println("/**");
      ostr.println(" * This exception is thrown when parse errors are encountered.");
      ostr.println(" * You can explicitly create objects of this exception type by");
      ostr.println(" * calling the method generateParseException in the generated");
      ostr.println(" * parser.");
      ostr.println(" *");
      ostr.println(" * You can modify this class to customize your error reporting");
      ostr.println(" * mechanisms so long as you retain the public fields.");
      ostr.println(" */");
      ostr.println("public class ParseException extends Exception {");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * This constructor is used by the method \"generateParseException\"");
      ostr.println("   * in the generated parser.  Calling this constructor generates");
      ostr.println("   * a new object of this type with the fields \"currentToken\",");
      ostr.println("   * \"expectedTokenSequences\", and \"tokenImage\" set.  The boolean");
      ostr.println("   * flag \"specialConstructor\" is also set to true to indicate that");
      ostr.println("   * this constructor was used to create this object.");
      ostr.println("   * This constructor calls its super class with the empty string");
      ostr.println("   * to force the \"toString\" method of parent class \"Throwable\" to");
      ostr.println("   * print the error message in the form:");
      ostr.println("   *     ParseException: <result of getMessage>");
      ostr.println("   */");
      ostr.println("  public ParseException(Token currentTokenVal,");
      ostr.println("                        int[][] expectedTokenSequencesVal,");
      ostr.println("                        String[] tokenImageVal");
      ostr.println("                       )");
      ostr.println("  {");
      ostr.println("    super(\"\");");
      ostr.println("    specialConstructor = true;");
      ostr.println("    currentToken = currentTokenVal;");
      ostr.println("    expectedTokenSequences = expectedTokenSequencesVal;");
      ostr.println("    tokenImage = tokenImageVal;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * The following constructors are for use by you for whatever");
      ostr.println("   * purpose you can think of.  Constructing the exception in this");
      ostr.println("   * manner makes the exception behave in the normal way - i.e., as");
      ostr.println("   * documented in the class \"Throwable\".  The fields \"errorToken\",");
      ostr.println("   * \"expectedTokenSequences\", and \"tokenImage\" do not contain");
      ostr.println("   * relevant information.  The JavaCC generated code does not use");
      ostr.println("   * these constructors.");
      ostr.println("   */");
      ostr.println("");
      ostr.println("  public ParseException() {");
      ostr.println("    super();");
      ostr.println("    specialConstructor = false;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /** Constructor with message. */");
      ostr.println("  public ParseException(String message) {");
      ostr.println("    super(message);");
      ostr.println("    specialConstructor = false;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * This variable determines which constructor was used to create");
      ostr.println("   * this object and thereby affects the semantics of the");
      ostr.println("   * \"getMessage\" method (see below).");
      ostr.println("   */");
      ostr.println("  protected boolean specialConstructor;");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * This is the last token that has been consumed successfully.  If");
      ostr.println("   * this object has been created due to a parse error, the token");
      ostr.println("   * followng this token will (therefore) be the first error token.");
      ostr.println("   */");
      ostr.println("  public Token currentToken;");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Each entry in this array is an array of integers.  Each array");
      ostr.println("   * of integers represents a sequence of tokens (by their ordinal");
      ostr.println("   * values) that is expected at this point of the parse.");
      ostr.println("   */");
      ostr.println("  public int[][] expectedTokenSequences;");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * This is a reference to the \"tokenImage\" array of the generated");
      ostr.println("   * parser within which the parse error occurred.  This array is");
      ostr.println("   * defined in the generated ...Constants interface.");
      ostr.println("   */");
      ostr.println("  public String[] tokenImage;");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * This method has the standard behavior when this object has been");
      ostr.println("   * created using the standard constructors.  Otherwise, it uses");
      ostr.println("   * \"currentToken\" and \"expectedTokenSequences\" to generate a parse");
      ostr.println("   * error message and returns it.  If this object has been created");
      ostr.println("   * due to a parse error, and you do not catch it (it gets thrown");
      ostr.println("   * from the parser), then this method is called during the printing");
      ostr.println("   * of the final stack trace, and hence the correct error message");
      ostr.println("   * gets displayed.");
      ostr.println("   */");
      ostr.println("  public String getMessage() {");
      ostr.println("    if (!specialConstructor) {");
      ostr.println("      return super.getMessage();");
      ostr.println("    }");
      ostr.println("    StringBuffer expected = new StringBuffer();");
      ostr.println("    int maxSize = 0;");
      ostr.println("    for (int i = 0; i < expectedTokenSequences.length; i++) {");
      ostr.println("      if (maxSize < expectedTokenSequences[i].length) {");
      ostr.println("        maxSize = expectedTokenSequences[i].length;");
      ostr.println("      }");
      ostr.println("      for (int j = 0; j < expectedTokenSequences[i].length; j++) {");
      ostr.println("        expected.append(tokenImage[expectedTokenSequences[i][j]]).append(' ');");
      ostr.println("      }");
      ostr.println("      if (expectedTokenSequences[i][expectedTokenSequences[i].length - 1] != 0) {");
      ostr.println("        expected.append(\"...\");");
      ostr.println("      }");
      ostr.println("      expected.append(eol).append(\"    \");");
      ostr.println("    }");
      ostr.println("    String retval = \"Encountered \\\"\";");
      ostr.println("    Token tok = currentToken.next;");
      ostr.println("    for (int i = 0; i < maxSize; i++) {");
      ostr.println("      if (i != 0) retval += \" \";");
      ostr.println("      if (tok.kind == 0) {");
      ostr.println("        retval += tokenImage[0];");
      ostr.println("        break;");
      ostr.println("      }");
      ostr.println("      retval += \" \" + tokenImage[tok.kind];");
      ostr.println("      retval += \" \\\"\";");
      ostr.println("      retval += add_escapes(tok.image);");
      ostr.println("      retval += \" \\\"\";");
      ostr.println("      tok = tok.next; ");
      ostr.println("    }");
      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("    retval += \"\\\" at line \" + currentToken.next.beginLine + \", " +
                "column \" + currentToken.next.beginColumn;");
      }
      ostr.println("    retval += \".\" + eol;");
      ostr.println("    if (expectedTokenSequences.length == 1) {");
      ostr.println("      retval += \"Was expecting:\" + eol + \"    \";");
      ostr.println("    } else {");
      ostr.println("      retval += \"Was expecting one of:\" + eol + \"    \";");
      ostr.println("    }");
      ostr.println("    retval += expected.toString();");
      ostr.println("    return retval;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * The end of line string for this machine.");
      ostr.println("   */");
      ostr.println("  protected String eol = System.getProperty(\"line.separator\", \"\\n\");");
      ostr.println(" ");
      ostr.println("  /**");
      ostr.println("   * Used to convert raw characters to their escaped version");
      ostr.println("   * when these raw version cannot be used as part of an ASCII");
      ostr.println("   * string literal.");
      ostr.println("   */");
      ostr.println("  protected String add_escapes(String str) {");
      ostr.println("      StringBuffer retval = new StringBuffer();");
      ostr.println("      char ch;");
      ostr.println("      for (int i = 0; i < str.length(); i++) {");
      ostr.println("        switch (str.charAt(i))");
      ostr.println("        {");
      ostr.println("           case 0 :");
      ostr.println("              continue;");
      ostr.println("           case '\\b':");
      ostr.println("              retval.append(\"\\\\b\");");
      ostr.println("              continue;");
      ostr.println("           case '\\t':");
      ostr.println("              retval.append(\"\\\\t\");");
      ostr.println("              continue;");
      ostr.println("           case '\\n':");
      ostr.println("              retval.append(\"\\\\n\");");
      ostr.println("              continue;");
      ostr.println("           case '\\f':");
      ostr.println("              retval.append(\"\\\\f\");");
      ostr.println("              continue;");
      ostr.println("           case '\\r':");
      ostr.println("              retval.append(\"\\\\r\");");
      ostr.println("              continue;");
      ostr.println("           case '\\\"':");
      ostr.println("              retval.append(\"\\\\\\\"\");");
      ostr.println("              continue;");
      ostr.println("           case '\\'':");
      ostr.println("              retval.append(\"\\\\\\'\");");
      ostr.println("              continue;");
      ostr.println("           case '\\\\':");
      ostr.println("              retval.append(\"\\\\\\\\\");");
      ostr.println("              continue;");
      ostr.println("           default:");
      ostr.println("              if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {");
      ostr.println("                 String s = \"0000\" + Integer.toString(ch, 16);");
      ostr.println("                 retval.append(\"\\\\u\" + s.substring(s.length() - 4, s.length()));");
      ostr.println("              } else {");
      ostr.println("                 retval.append(ch);");
      ostr.println("              }");
      ostr.println("              continue;");
      ostr.println("        }");
      ostr.println("      }");
      ostr.println("      return retval.toString();");
      ostr.println("   }");
      ostr.println("");
      ostr.println("}");
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
          ((Token)cu_to_insertion_point_1.elementAt(0)).kind == PACKAGE
         ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.elementAt(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.elementAt(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }
      ostr.println("/** Token Manager Error. */");
      if(Options.getJdkVersion().compareTo("1.5") >= 0)
        ostr.println("@SuppressWarnings(\"serial\")");
      ostr.println("public class TokenMgrError extends Error");
      ostr.println("{");
      ostr.println("");
      ostr.println("   /*");
      ostr.println("    * Ordinals for various reasons why an Error of this type can be thrown.");
      ostr.println("    */");
      ostr.println("");
      ostr.println("   /**");
      ostr.println("    * Lexical error occurred.");
      ostr.println("    */");
      ostr.println("   static final int LEXICAL_ERROR = 0;");
      ostr.println("");
      ostr.println("   /**");
      ostr.println("    * An attempt was made to create a second instance of a static token manager.");
      ostr.println("    */");
      ostr.println("   static final int STATIC_LEXER_ERROR = 1;");
      ostr.println("");
      ostr.println("   /**");
      ostr.println("    * Tried to change to an invalid lexical state.");
      ostr.println("    */");
      ostr.println("   static final int INVALID_LEXICAL_STATE = 2;");
      ostr.println("");
      ostr.println("   /**");
      ostr.println("    * Detected (and bailed out of) an infinite loop in the token manager.");
      ostr.println("    */");
      ostr.println("   static final int LOOP_DETECTED = 3;");
      ostr.println("");
      ostr.println("   /**");
      ostr.println("    * Indicates the reason why the exception is thrown. It will have");
      ostr.println("    * one of the above 4 values.");
      ostr.println("    */");
      ostr.println("   int errorCode;");
      ostr.println("");
      ostr.println("   /**");
      ostr.println("    * Replaces unprintable characters by their escaped (or unicode escaped)");
      ostr.println("    * equivalents in the given string");
      ostr.println("    */");
      ostr.println("   protected static final String addEscapes(String str) {");
      ostr.println("      StringBuffer retval = new StringBuffer();");
      ostr.println("      char ch;");
      ostr.println("      for (int i = 0; i < str.length(); i++) {");
      ostr.println("        switch (str.charAt(i))");
      ostr.println("        {");
      ostr.println("           case 0 :");
      ostr.println("              continue;");
      ostr.println("           case '\\b':");
      ostr.println("              retval.append(\"\\\\b\");");
      ostr.println("              continue;");
      ostr.println("           case '\\t':");
      ostr.println("              retval.append(\"\\\\t\");");
      ostr.println("              continue;");
      ostr.println("           case '\\n':");
      ostr.println("              retval.append(\"\\\\n\");");
      ostr.println("              continue;");
      ostr.println("           case '\\f':");
      ostr.println("              retval.append(\"\\\\f\");");
      ostr.println("              continue;");
      ostr.println("           case '\\r':");
      ostr.println("              retval.append(\"\\\\r\");");
      ostr.println("              continue;");
      ostr.println("           case '\\\"':");
      ostr.println("              retval.append(\"\\\\\\\"\");");
      ostr.println("              continue;");
      ostr.println("           case '\\'':");
      ostr.println("              retval.append(\"\\\\\\'\");");
      ostr.println("              continue;");
      ostr.println("           case '\\\\':");
      ostr.println("              retval.append(\"\\\\\\\\\");");
      ostr.println("              continue;");
      ostr.println("           default:");
      ostr.println("              if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {");
      ostr.println("                 String s = \"0000\" + Integer.toString(ch, 16);");
      ostr.println("                 retval.append(\"\\\\u\" + s.substring(s.length() - 4, s.length()));");
      ostr.println("              } else {");
      ostr.println("                 retval.append(ch);");
      ostr.println("              }");
      ostr.println("              continue;");
      ostr.println("        }");
      ostr.println("      }");
      ostr.println("      return retval.toString();");
      ostr.println("   }");
      ostr.println("");
      ostr.println("   /**");
      ostr.println("    * Returns a detailed message for the Error when it is thrown by the");
      ostr.println("    * token manager to indicate a lexical error.");
      ostr.println("    * Parameters : ");
      ostr.println("    *    EOFSeen     : indicates if EOF caused the lexical error");
      ostr.println("    *    curLexState : lexical state in which this error occurred");
      ostr.println("    *    errorLine   : line number when the error occurred");
      ostr.println("    *    errorColumn : column number when the error occurred");
      ostr.println("    *    errorAfter  : prefix that was seen before this error occurred");
      ostr.println("    *    curchar     : the offending character");
      ostr.println("    * Note: You can customize the lexical error message by modifying this method.");
      ostr.println("    */");
      ostr.println("   protected static String LexicalError(" +
              "boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, char curChar) {");
      ostr.println("      return(\"Lexical error at line \" +");
      ostr.println("           errorLine + \", column \" +");
      ostr.println("           errorColumn + \".  Encountered: \" +");
      ostr.println("           (EOFSeen ? \"<EOF> \" : " +
              "(\"\\\"\" + addEscapes(String.valueOf(curChar)) + \"\\\"\") + \" (\" + (int)curChar + \"), \") +");
      ostr.println("           \"after : \\\"\" + addEscapes(errorAfter) + \"\\\"\");");
      ostr.println("   }");
      ostr.println("");
      ostr.println("   /**");
      ostr.println("    * You can also modify the body of this method to customize your error messages.");
      ostr.println("    * For example, cases like LOOP_DETECTED and INVALID_LEXICAL_STATE are not");
      ostr.println("    * of end-users concern, so you can return something like : ");
      ostr.println("    *");
      ostr.println("    *     \"Internal Error : Please file a bug report .... \"");
      ostr.println("    *");
      ostr.println("    * from this method for such cases in the release version of your parser.");
      ostr.println("    */");
      ostr.println("   public String getMessage() {");
      ostr.println("      return super.getMessage();");
      ostr.println("   }");
      ostr.println("");
      ostr.println("   /*");
      ostr.println("    * Constructors of various flavors follow.");
      ostr.println("    */");
      ostr.println("");
      ostr.println("   /** No arg constructor. */");
      ostr.println("   public TokenMgrError() {");
      ostr.println("   }");
      ostr.println("");
      ostr.println("   /** Constructor with message and reason. */");
      ostr.println("   public TokenMgrError(String message, int reason) {");
      ostr.println("      super(message);");
      ostr.println("      errorCode = reason;");
      ostr.println("   }");
      ostr.println("");
      ostr.println("   /** Full Constructor. */");
     ostr.println("   public TokenMgrError(boolean EOFSeen, int lexState, int errorLine, int errorColumn, " +
              "String errorAfter, char curChar, int reason) {");
      ostr.println("      this(LexicalError(EOFSeen, lexState, errorLine, errorColumn, errorAfter, curChar), reason);");
      ostr.println("   }");
      ostr.println("}");
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
      final OutputFile outputFile = new OutputFile(file, tokenVersion, new String[] {"TOKEN_EXTENDS", "KEEP_LINE_COL"});

      if (!outputFile.needToWrite)
      {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();

      if (cu_to_insertion_point_1.size() != 0 &&
          ((Token)cu_to_insertion_point_1.elementAt(0)).kind == PACKAGE
         ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.elementAt(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.elementAt(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }
      ostr.println("/**");
      ostr.println(" * Describes the input token stream.");
      ostr.println(" */");
      ostr.println("");
      if (Options.getTokenExtends().equals(""))
      {
      	ostr.println("public class Token {");
      }
      else
      {
      	ostr.println("public class Token extends " + Options.getTokenExtends() + " {");
      }
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * An integer that describes the kind of this token.  This numbering");
      ostr.println("   * system is determined by JavaCCParser, and a table of these numbers is");
      ostr.println("   * stored in the file ...Constants.java.");
      ostr.println("   */");
      ostr.println("  public int kind;");
      if (OtherFilesGen.keepLineCol)
      {
        ostr.println("");
        ostr.println("  /** The line number of the first character of this Token. */");
        ostr.println("  public int beginLine;");
        ostr.println("  /** The column number of the first character of this Token. */");
        ostr.println("  public int beginColumn;");
        ostr.println("  /** The line number of the last character of this Token. */");
        ostr.println("  public int endLine;");
        ostr.println("  /** The column number of the last character of this Token. */");
        ostr.println("  public int endColumn;");
      }

      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * The string image of the token.");
      ostr.println("   */");
      ostr.println("  public String image;");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * A reference to the next regular (non-special) token from the input");
      ostr.println("   * stream.  If this is the last token from the input stream, or if the");
      ostr.println("   * token manager has not read tokens beyond this one, this field is");
      ostr.println("   * set to null.  This is true only if this token is also a regular");
      ostr.println("   * token.  Otherwise, see below for a description of the contents of");
      ostr.println("   * this field.");
      ostr.println("   */");
      ostr.println("  public Token next;");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * This field is used to access special tokens that occur prior to this");
      ostr.println("   * token, but after the immediately preceding regular (non-special) token.");
      ostr.println("   * If there are no such special tokens, this field is set to null.");
      ostr.println("   * When there are more than one such special token, this field refers");
      ostr.println("   * to the last of these special tokens, which in turn refers to the next");
      ostr.println("   * previous special token through its specialToken field, and so on");
      ostr.println("   * until the first special token (whose specialToken field is null).");
      ostr.println("   * The next fields of special tokens refer to other special tokens that");
      ostr.println("   * immediately follow it (without an intervening regular token).  If there");
      ostr.println("   * is no such token, this field is null.");
      ostr.println("   */");
      ostr.println("  public Token specialToken;");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * An optional attribute value of the Token.");
      ostr.println("   * Tokens which are not used as syntactic sugar will often contain");
      ostr.println("   * meaningful values that will be used later on by the compiler or");
      ostr.println("   * interpreter. This attribute value is often different from the image.");
      ostr.println("   * Any subclass of Token that actually wants to return a non-null value can");
      ostr.println("   * override this method as appropriate.");
      ostr.println("   */");
      ostr.println("  public Object getValue() {");
      ostr.println("    return null;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * No-argument constructor");
      ostr.println("   */");
      ostr.println("  public Token() {}");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Constructs a new token for the specified Image.");
      ostr.println("   */");
      ostr.println("  public Token(int kind)");
      ostr.println("  {");
      ostr.println("     this(kind, null);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Constructs a new token for the specified Image and Kind.");
      ostr.println("   */");
      ostr.println("  public Token(int kind, String image)");
      ostr.println("  {");
      ostr.println("     this.kind = kind;");
      ostr.println("     this.image = image;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Returns the image.");
      ostr.println("   */");
      ostr.println("  public String toString()");
      ostr.println("  {");
      ostr.println("     return image;");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  /**");
      ostr.println("   * Returns a new Token object, by default. However, if you want, you");
      ostr.println("   * can create and return subclass objects based on the value of ofKind.");
      ostr.println("   * Simply add the cases to the switch for all those special cases.");
      ostr.println("   * For example, if you have a subclass of Token called IDToken that");
      ostr.println("   * you want to create if ofKind is ID, simply add something like :");
      ostr.println("   *");
      ostr.println("   *    case MyParserConstants.ID : return new IDToken(ofKind, image);");
      ostr.println("   *");
      ostr.println("   * to the following switch statement. Then you can cast matchedToken");
      ostr.println("   * variable to the appropriate type and use sit in your lexical actions.");
      ostr.println("   */");
      ostr.println("  public static Token newToken(int ofKind, String image)");
      ostr.println("  {");
      ostr.println("     switch(ofKind)");
      ostr.println("     {");
      ostr.println("       default : return new Token(ofKind, image);");
      ostr.println("     }");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  public static Token newToken(int ofKind)");
      ostr.println("  {");
      ostr.println("     return newToken(ofKind, null);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("}");
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
      final OutputFile outputFile = new OutputFile(file, tokenManagerVersion, new String[0]);

      if (!outputFile.needToWrite)
      {
        return;
      }

      final PrintWriter ostr = outputFile.getPrintWriter();

      if (cu_to_insertion_point_1.size() != 0 &&
          ((Token)cu_to_insertion_point_1.elementAt(0)).kind == PACKAGE
         ) {
        for (int i = 1; i < cu_to_insertion_point_1.size(); i++) {
          if (((Token)cu_to_insertion_point_1.elementAt(i)).kind == SEMICOLON) {
            cline = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.elementAt(0))).beginColumn;
            for (int j = 0; j <= i; j++) {
              printToken((Token)(cu_to_insertion_point_1.elementAt(j)), ostr);
            }
            ostr.println("");
            ostr.println("");
            break;
          }
        }
      }
      ostr.println("/**");
      ostr.println(" * An implementation for this interface is generated by");
      ostr.println(" * JavaCCParser.  The user is free to use any implementation");
      ostr.println(" * of their choice.");
      ostr.println(" */");
      ostr.println("");
      ostr.println("public interface TokenManager {");
      ostr.println("");
      ostr.println("  /** This gets the next token from the input stream.");
      ostr.println("   *  A token of kind 0 (<EOF>) should be returned on EOF.");
      ostr.println("   */");
      ostr.println("  public Token getNextToken();");
      ostr.println("");
      ostr.println("}");
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
