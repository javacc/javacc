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
 * Describes JavaCC productions.
 */

public class NormalProduction {

  /**
   * The line and column number of the construct that corresponds
   * most closely to this node.
   */
  public int line, column;

  /**
   * The NonTerminal nodes which refer to this production.
   */
  java.util.Vector parents = new java.util.Vector();

  /**
   * The name of the non-terminal of this production.
   */
  public String lhs;

  /**
   * The tokens that make up the return type of this production.
   */
  public java.util.Vector return_type_tokens = new java.util.Vector();

  /**
   * The tokens that make up the parameters of this production.
   */
  public java.util.Vector parameter_list_tokens = new java.util.Vector();

  /**
   * Each entry in this vector is a vector of tokens that represents an
   * exception in the throws list of this production.  This list does not
   * include ParseException which is always thrown.
   */
  public java.util.Vector throws_list = new java.util.Vector();

  /**
   * The RHS of this production.  Not used for JavaCodeProduction.
   */
  public Expansion expansion;

  /**
   * This boolean flag is true if this production can expand to empty.
   */
  boolean emptyPossible = false;

  /**
   * A list of all non-terminals that this one can expand to without
   * having to consume any tokens.  Also an index that shows how many
   * pointers exist.
   */
  NormalProduction[] leftExpansions = new NormalProduction[10];
  int leIndex = 0;

  /**
   * The following variable is used to maintain state information for the
   * left-recursion determination algorithm:  It is initialized to 0, and
   * set to -1 if this node has been visited in a pre-order walk, and then
   * it is set to 1 if the pre-order walk of the whole graph from this
   * node has been traversed.  i.e., -1 indicates partially processed,
   * and 1 indicates fully processed.
   */
  int walkStatus = 0;

  /**
   * The first and last tokens from the input stream that represent this
   * production.
   */
  public Token firstToken, lastToken;

}
