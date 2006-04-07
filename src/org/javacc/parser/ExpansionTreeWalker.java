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
 * A set of routines that walk down the Expansion tree in
 * various ways.
 */
public class ExpansionTreeWalker {

  /**
   * Visits the nodes of the tree rooted at "node" in pre-order.
   * i.e., it executes opObj.action first and then visits the
   * children.
   */
  static void preOrderWalk(Expansion node, TreeWalkerOp opObj) {
    opObj.action(node);
    if (opObj.goDeeper(node)) {
      if (node instanceof Choice) {
        for (java.util.Enumeration enumeration = ((Choice)node).choices.elements(); enumeration.hasMoreElements();) {
          preOrderWalk((Expansion)enumeration.nextElement(), opObj);
        }
      } else if (node instanceof Sequence) {
        for (java.util.Enumeration enumeration = ((Sequence)node).units.elements(); enumeration.hasMoreElements();) {
          preOrderWalk((Expansion)enumeration.nextElement(), opObj);
        }
      } else if (node instanceof OneOrMore) {
        preOrderWalk(((OneOrMore)node).expansion, opObj);
      } else if (node instanceof ZeroOrMore) {
        preOrderWalk(((ZeroOrMore)node).expansion, opObj);
      } else if (node instanceof ZeroOrOne) {
        preOrderWalk(((ZeroOrOne)node).expansion, opObj);
      } else if (node instanceof Lookahead) {
        Expansion nested_e = ((Lookahead)node).la_expansion;
        if (!(nested_e instanceof Sequence && (Expansion)(((Sequence)nested_e).units.elementAt(0)) == node)) {
          preOrderWalk(nested_e, opObj);
        }
      } else if (node instanceof TryBlock) {
        preOrderWalk(((TryBlock)node).exp, opObj);
      } else if (node instanceof RChoice) {
        for (java.util.Enumeration enumeration = ((RChoice)node).choices.elements(); enumeration.hasMoreElements();) {
          preOrderWalk((Expansion)enumeration.nextElement(), opObj);
        }
      } else if (node instanceof RSequence) {
        for (java.util.Enumeration enumeration = ((RSequence)node).units.elements(); enumeration.hasMoreElements();) {
          preOrderWalk((Expansion)enumeration.nextElement(), opObj);
        }
      } else if (node instanceof ROneOrMore) {
        preOrderWalk(((ROneOrMore)node).regexpr, opObj);
      } else if (node instanceof RZeroOrMore) {
        preOrderWalk(((RZeroOrMore)node).regexpr, opObj);
      } else if (node instanceof RZeroOrOne) {
        preOrderWalk(((RZeroOrOne)node).regexpr, opObj);
      } else if (node instanceof RRepetitionRange) {
        preOrderWalk(((RRepetitionRange)node).regexpr, opObj);
      }
    }
  }

  /**
   * Visits the nodes of the tree rooted at "node" in post-order.
   * i.e., it visits the children first and then executes
   * opObj.action.
   */
  static void postOrderWalk(Expansion node, TreeWalkerOp opObj) {
    if (opObj.goDeeper(node)) {
      if (node instanceof Choice) {
        for (java.util.Enumeration enumeration = ((Choice)node).choices.elements(); enumeration.hasMoreElements();) {
          postOrderWalk((Expansion)enumeration.nextElement(), opObj);
        }
      } else if (node instanceof Sequence) {
        for (java.util.Enumeration enumeration = ((Sequence)node).units.elements(); enumeration.hasMoreElements();) {
          postOrderWalk((Expansion)enumeration.nextElement(), opObj);
        }
      } else if (node instanceof OneOrMore) {
        postOrderWalk(((OneOrMore)node).expansion, opObj);
      } else if (node instanceof ZeroOrMore) {
        postOrderWalk(((ZeroOrMore)node).expansion, opObj);
      } else if (node instanceof ZeroOrOne) {
        postOrderWalk(((ZeroOrOne)node).expansion, opObj);
      } else if (node instanceof Lookahead) {
        Expansion nested_e = ((Lookahead)node).la_expansion;
        if (!(nested_e instanceof Sequence && (Expansion)(((Sequence)nested_e).units.elementAt(0)) == node)) {
          postOrderWalk(nested_e, opObj);
        }
      } else if (node instanceof TryBlock) {
        postOrderWalk(((TryBlock)node).exp, opObj);
      } else if (node instanceof RChoice) {
        for (java.util.Enumeration enumeration = ((RChoice)node).choices.elements(); enumeration.hasMoreElements();) {
          postOrderWalk((Expansion)enumeration.nextElement(), opObj);
        }
      } else if (node instanceof RSequence) {
        for (java.util.Enumeration enumeration = ((RSequence)node).units.elements(); enumeration.hasMoreElements();) {
          postOrderWalk((Expansion)enumeration.nextElement(), opObj);
        }
      } else if (node instanceof ROneOrMore) {
        postOrderWalk(((ROneOrMore)node).regexpr, opObj);
      } else if (node instanceof RZeroOrMore) {
        postOrderWalk(((RZeroOrMore)node).regexpr, opObj);
      } else if (node instanceof RZeroOrOne) {
        postOrderWalk(((RZeroOrOne)node).regexpr, opObj);
      } else if (node instanceof RRepetitionRange) {
        postOrderWalk(((RRepetitionRange)node).regexpr, opObj);
      }
    }
    opObj.action(node);
  }

}
