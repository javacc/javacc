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

public class JavaCCErrors {

  private static int parse_error_count = 0, semantic_error_count = 0, warning_count = 0;

  private static void printLocationInfo(Object node) {
    if (node instanceof NormalProduction) {
      NormalProduction n = (NormalProduction)node;
      System.err.print("Line " + n.line + ", Column " + n.column + ": ");
    } else if (node instanceof TokenProduction) {
      TokenProduction n = (TokenProduction)node;
      System.err.print("Line " + n.line + ", Column " + n.column + ": ");
    } else if (node instanceof Expansion) {
      Expansion n = (Expansion)node;
      System.err.print("Line " + n.line + ", Column " + n.column + ": ");
    } else if (node instanceof CharacterRange) {
      CharacterRange n = (CharacterRange)node;
      System.err.print("Line " + n.line + ", Column " + n.column + ": ");
    } else if (node instanceof SingleCharacter) {
      SingleCharacter n = (SingleCharacter)node;
      System.err.print("Line " + n.line + ", Column " + n.column + ": ");
    } else if (node instanceof Token) {
      Token t = (Token)node;
      System.err.print("Line " + t.beginLine + ", Column " + t.beginColumn + ": ");
    }
  }

  public static void parse_error(Object node, String mess) {
    System.err.print("Error: ");
    printLocationInfo(node);
    System.err.println(mess);
    parse_error_count++;
  }

  public static void parse_error(String mess) {
    System.err.print("Error: ");
    System.err.println(mess);
    parse_error_count++;
  }

  public static int get_parse_error_count() {
    return parse_error_count;
  }

  public static void semantic_error(Object node, String mess) {
    System.err.print("Error: ");
    printLocationInfo(node);
    System.err.println(mess);
    semantic_error_count++;
  }

  public static void semantic_error(String mess) {
    System.err.print("Error: ");
    System.err.println(mess);
    semantic_error_count++;
  }

  public static int get_semantic_error_count() {
    return semantic_error_count;
  }

  public static void warning(Object node, String mess) {
    System.err.print("Warning: ");
    printLocationInfo(node);
    System.err.println(mess);
    warning_count++;
  }

  public static void warning(String mess) {
    System.err.print("Warning: ");
    System.err.println(mess);
    warning_count++;
  }

  public static int get_warning_count() {
    return warning_count;
  }

  public static int get_error_count() {
    return parse_error_count + semantic_error_count;
  }

   public static void reInit()
   {
      parse_error_count = 0;
      semantic_error_count = 0;
      warning_count = 0;
   }

}
