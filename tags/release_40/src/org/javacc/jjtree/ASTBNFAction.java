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


public class ASTBNFAction extends SimpleNode {
  ASTBNFAction(int id) {
    super(id);
  }

  private Node getScopingParent(NodeScope ns)
  {
    for (Node n = this.jjtGetParent(); n != null; n = n.jjtGetParent()) {
      if (n instanceof ASTBNFNodeScope) {
	if (((ASTBNFNodeScope)n).node_scope == ns) {
	  return n;
	}
      } else if (n instanceof ASTExpansionNodeScope) {
	if (((ASTExpansionNodeScope)n).node_scope == ns) {
	  return n;
	}
      }
    }
    return null;
  }


  public void print(IO io)
  {
    /* Assume that this action requires an early node close, and then
       try to decide whether this assumption is false.  Do this by
       looking outwards through the enclosing expansion units.  If we
       ever find that we are enclosed in a unit which is not the final
       unit in a sequence we know that an early close is not
       required. */

    NodeScope ns = NodeScope.getEnclosingNodeScope(this);
    if (ns != null && !ns.isVoid()) {
      boolean needClose = true;
      Node sp = getScopingParent(ns);

      SimpleNode n = this;
      while (true) {
	Node p = n.jjtGetParent();
	if (p instanceof ASTBNFSequence || p instanceof ASTBNFTryBlock) {
	  if (n.getOrdinal() != p.jjtGetNumChildren() - 1) {
	    /* We're not the final unit in the sequence. */
	    needClose = false;
	    break;
	  }
	} else if (p instanceof ASTBNFZeroOrOne ||
		     p instanceof ASTBNFZeroOrMore ||
		     p instanceof ASTBNFOneOrMore) {
	  needClose = false;
	  break;
	}
	if (p == sp) {
	  /* No more parents to look at. */
	  break;
	}
	n = (SimpleNode)p;
      }
      if (needClose) {
	openJJTreeComment(io, null);
	io.println();
	ns.insertCloseNodeAction(io, getIndentation(this));
	closeJJTreeComment(io);
      }
    }
    super.print(io);
  }


}

/*end*/
