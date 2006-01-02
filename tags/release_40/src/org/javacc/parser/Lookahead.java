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
 * Describes lookahead rule for a particular expansion or expansion
 * sequence (See Sequence.java).  In case this describes the lookahead
 * rule for a single expansion unit, then a sequence is created with
 * this node as the first element, and the expansion unit as the second
 * and last element.
 */

public class Lookahead extends Expansion {

  /**
   * Contains the list of tokens that make up the semantic lookahead
   * if any.  If this node represents a different kind of lookahead (other
   * than semantic lookahead), then this vector contains nothing.  If
   * this vector contains something, then it is the boolean expression
   * that forms the semantic lookahead.  In this case, the following
   * fields "amount" and "la_expansion" are ignored.
   */
  public java.util.Vector action_tokens = new java.util.Vector();

  /**
   * The lookahead amount.  Its default value essentially gives us
   * infinite lookahead.
   */
  public int amount = Integer.MAX_VALUE;

  /**
   * The expansion used to determine whether or not to choose the
   * corresponding parse option.  This expansion is parsed upto
   * "amount" tokens of lookahead or until a complete match for it
   * is found.  Usually, this is the same as the expansion to be
   * parsed.
   */
  public Expansion la_expansion;

  /**
   * Is set to true if this is an explicit lookahead specification.
   */
  public boolean isExplicit;

}
