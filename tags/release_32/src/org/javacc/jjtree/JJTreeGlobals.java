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

import java.util.Hashtable;
import java.util.Vector;

class JJTreeGlobals
{

  /**
   * This hashtable stores the JJTree options to distinguish them
   * from JavaCC options.  The values are not important, only the
   * keys.
   */
  static Hashtable jjtreeOptions = new Hashtable();

  static Vector toolList = new Vector();

  /**
   * Use this like className.
   **/
  public static String parserName;

  /**
   * The package that the parser lives in.  If the grammar doesn't
   * specify a package it is the empty string.
   **/
  public static String packageName = "";

  /** The <code>implements</code> token of the parser class.  If the
   * parser doesn't have one then it is the first "{" of the parser
   * class body.
   **/
  public static Token parserImplements;

  /** The first token of the parser class body (the <code>{</code>).
   * The JJTree state is inserted after this token.
   **/
  public static Token parserClassBodyStart;

  /**
   * This is mapping from production names to ASTProduction objects.
   **/
  static Hashtable productions = new Hashtable();

}

/*end*/
