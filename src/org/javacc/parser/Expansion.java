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
 * Describes expansions - entities that may occur on the
 * right hand sides of productions.  This is the base class of
 * a bunch of other more specific classes.
 */

public class Expansion {

  /**
   * The line and column number of the construct that corresponds
   * most closely to this node.
   */
  int line, column;

  /**
   * A reimplementing of Object.hashCode() to be deterministic.  This uses
   * the line and column fields to generate an arbitrary number - we assume
   * that this method is called only after line and column are set to
   * their actual values.
   */
  public int hashCode() {
    return line + column;
  }

  /**
   * An internal name for this expansion.  This is used to generate parser
   * routines.
   */
  String internal_name = "";

  /**
   * The parser routines are generated in three phases.  The generation
   * of the second and third phase are on demand only, and the third phase
   * can be recursive.  This variable is used to keep track of the
   * expansions for which phase 3 generations have been already added to
   * a list so that the recursion can be terminated.
   */
  boolean phase3done = false;

  /**
   * The parent of this expansion node.  In case this is the top level
   * expansion of the production it is a reference to the production node
   * otherwise it is a reference to another Expansion node.  In case this
   * is the top level of a lookahead expansion,then the parent is null.
   */
  public Object parent;

  /**
   * The ordinal of this node with respect to its parent.
   */
  int ordinal;

  /**
   * To avoid right-recursive loops when calculating follow sets, we use
   * a generation number which indicates if this expansion was visited
   * by LookaheadWalk.genFollowSet in the same generation.  New generations
   * are obtained by incrementing the static counter below, and the current
   * generation is stored in the non-static variable below.
   */
  public static long nextGenerationIndex = 1;
  public long myGeneration = 0;

  /**
   * This flag is used for bookkeeping by the minimumSize method in class
   * ParseEngine.
   */
  public boolean inMinimumSize = false;

   public static void reInit()
   {
      nextGenerationIndex = 1;
   }

}
