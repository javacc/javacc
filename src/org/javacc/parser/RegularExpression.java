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
 * Describes regular expressions.
 */

abstract public class RegularExpression extends Expansion {

  /**
   * The label of the regular expression (if any).  If no label is
   * present, this is set to "".
   */
  public String label = "";

  /**
   * The ordinal value assigned to the regular expression.  It is
   * used for internal processing and passing information between
   * the parser and the lexical analyzer.
   */
  int ordinal;

  /**
   * The LHS to which the token value of the regular expression
   * is assigned.  In case there is no LHS, then the vector
   * remains empty.
   */
  public java.util.Vector lhsTokens = new java.util.Vector();

  /**
   * This flag is set if the regular expression has a label prefixed
   * with the # symbol - this indicates that the purpose of the regular
   * expression is solely for defining other regular expressions.
   */
  public boolean private_rexp = false;

  /**
   * If this is a top-level regular expression (nested directly
   * within a TokenProduction), then this field point to that
   * TokenProduction object.
   */
  public TokenProduction tpContext = null;

  abstract public Nfa GenerateNfa(boolean ignoreCase);

  public boolean CanMatchAnyChar()
  {
     return false;
  }

  /**
   * The following variable is used to maintain state information for the
   * loop determination algorithm:  It is initialized to 0, and
   * set to -1 if this node has been visited in a pre-order walk, and then
   * it is set to 1 if the pre-order walk of the whole graph from this
   * node has been traversed.  i.e., -1 indicates partially processed,
   * and 1 indicates fully processed.
   */
  int walkStatus = 0;

}
