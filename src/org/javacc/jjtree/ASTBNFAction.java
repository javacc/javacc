/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
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
