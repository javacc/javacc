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

package org.javacc.jjtree;

import org.javacc.parser.JavaCCErrors;

public class TokenUtils
{
  static void print(Token t, IO io, String in, String out) {
    Token tt = t.specialToken;
    if (tt != null) {
      while (tt.specialToken != null) tt = tt.specialToken;
      while (tt != null) {
	io.print(addUnicodeEscapes(tt.image));
	tt = tt.next;
      }
    }
    String i = t.image;
    if (in != null && i.equals(in)) {
      i = out;
    }
    io.print(addUnicodeEscapes(i));
  }

  static void print(Token t, IO io) {
    print(t, io, null, null);
  }

  static String addUnicodeEscapes(String str) {
    String retval = "";
    char ch;
    for (int i = 0; i < str.length(); i++) {
      ch = str.charAt(i);
      if ((ch < 0x20 || ch > 0x7e) && ch != '\t' && ch != '\n' && ch != '\r' && ch != '\f') {
	String s = "0000" + Integer.toString(ch, 16);
	retval += "\\u" + s.substring(s.length() - 4, s.length());
      } else {
	retval += ch;
      }
    }
    return retval;
  }


  static boolean hasTokens(SimpleNode n)
  {
    if (n.getLastToken().next == n.getFirstToken()) {
      return false;
    } else {
      return true;
    }
  }

  static String remove_escapes_and_quotes(Token t, String str) {
    String retval = "";
    int index = 1;
    char ch, ch1;
    int ordinal;
    while (index < str.length()-1) {
      if (str.charAt(index) != '\\') {
        retval += str.charAt(index); index++;
        continue;
      }
      index++;
      ch = str.charAt(index);
      if (ch == 'b') {
        retval += '\b'; index++;
        continue;
      }
      if (ch == 't') {
        retval += '\t'; index++;
        continue;
      }
      if (ch == 'n') {
        retval += '\n'; index++;
        continue;
      }
      if (ch == 'f') {
        retval += '\f'; index++;
        continue;
      }
      if (ch == 'r') {
        retval += '\r'; index++;
        continue;
      }
      if (ch == '"') {
        retval += '\"'; index++;
        continue;
      }
      if (ch == '\'') {
        retval += '\''; index++;
        continue;
      }
      if (ch == '\\') {
        retval += '\\'; index++;
        continue;
      }
      if (ch >= '0' && ch <= '7') {
        ordinal = ((int)ch) - ((int)'0'); index++;
        ch1 = str.charAt(index);
        if (ch1 >= '0' && ch1 <= '7') {
          ordinal = ordinal*8 + ((int)ch1) - ((int)'0'); index++;
          ch1 = str.charAt(index);
          if (ch <= '3' && ch1 >= '0' && ch1 <= '7') {
            ordinal = ordinal*8 + ((int)ch1) - ((int)'0'); index++;
          }
        }
        retval += (char)ordinal;
        continue;
      }
      if (ch == 'u') {
        index++; ch = str.charAt(index);
        if (hexchar(ch)) {
          ordinal = hexval(ch);
          index++; ch = str.charAt(index);
          if (hexchar(ch)) {
            ordinal = ordinal*16 + hexval(ch);
            index++; ch = str.charAt(index);
            if (hexchar(ch)) {
              ordinal = ordinal*16 + hexval(ch);
              index++; ch = str.charAt(index);
              if (hexchar(ch)) {
                ordinal = ordinal*16 + hexval(ch);
                index++;
                continue;
              }
            }
          }
        }
        JavaCCErrors.parse_error(t, "Encountered non-hex character '" + ch + "' at position " + index + " of string - Unicode escape must have 4 hex digits after it.");
        return retval;
      }
      JavaCCErrors.parse_error(t, "Illegal escape sequence '\\" + ch + "' at position " + index + " of string.");
      return retval;
    }
    return retval;
  }

  private static boolean hexchar(char ch) {
    if (ch >= '0' && ch <= '9') return true;
    if (ch >= 'A' && ch <= 'F') return true;
    if (ch >= 'a' && ch <= 'f') return true;
    return false;
  }

  private static int hexval(char ch) {
    if (ch >= '0' && ch <= '9') return ((int)ch) - ((int)'0');
    if (ch >= 'A' && ch <= 'F') return ((int)ch) - ((int)'A') + 10;
    return ((int)ch) - ((int)'a') + 10;
  }

}

/*end*/
