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

/**
 * Describes the various regular expression productions.
 */

public class TokenProduction {

  /**
   * Definitions of constants that identify the kind of regular
   * expression production this is.
   */
  public static final int TOKEN  = 0,
                          SKIP   = 1,
                          MORE   = 2,
                          SPECIAL = 3;

  /**
   * The image of the above constants.
   */
  public static final String[] kindImage = {
    "TOKEN", "SKIP", "MORE", "SPECIAL"
  };

  /**
   * The starting line and column of this token production.
   */
  public int line, column;

  /**
   * The states in which this regular expression production exists.  If
   * this array is null, then "<*>" has been specified and this regular
   * expression exists in all states.  However, this null value is
   * replaced by a String array that includes all lexical state names
   * during the semanticization phase.
   */
  public String[] lexStates;

  /**
   * The kind of this token production - TOKEN, SKIP, MORE, or SPECIAL.
   */
  public int kind;

  /**
   * The list of regular expression specifications that comprise this
   * production.  Each entry is a "RegExprSpec".
   */
  public java.util.Vector respecs = new java.util.Vector();

  /**
   * This is true if this corresponds to a production that actually
   * appears in the input grammar.  Otherwise (if this is created to
   * describe a regular expression that is part of the BNF) this is set
   * to false.
   */
  public boolean isExplicit = true;

  /**
   * This is true if case is to be ignored within the regular expressions
   * of this token production.
   */
  public boolean ignoreCase = false;

  /**
   * The first and last tokens from the input stream that represent this
   * production.
   */
  public Token firstToken, lastToken;

}
