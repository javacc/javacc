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

public class ASTNodeDescriptor extends SimpleNode {
  ASTNodeDescriptor(int id) {
    super(id);
  }

  private boolean faked = false;

  static ASTNodeDescriptor indefinite(String s)
  {
    ASTNodeDescriptor nd = new ASTNodeDescriptor(JJTreeParserTreeConstants.JJTNODEDESCRIPTOR);
    nd.name = s;
    nd.setNodeIdValue();
    nd.faked = true;
    return nd;
  }


  static Vector nodeIds = new Vector();
  static Vector nodeNames = new Vector();
  static Hashtable nodeSeen = new Hashtable();

  static Vector getNodeIds()
  {
    return nodeIds;
  }

  static Vector getNodeNames()
  {
    return nodeNames;
  }

  void setNodeIdValue()
  {
    String k = getNodeId();
    if (!nodeSeen.containsKey(k)) {
      nodeSeen.put(k, k);
      nodeNames.addElement(name);
      nodeIds.addElement(k);
    }
  }

  String getNodeId()
  {
    return "JJT" + name.toUpperCase();
  }


  String name;
  boolean isGT;
  ASTNodeDescriptorExpression expression;


  boolean isVoid()
  {
    return name.equals("void");
  }

  public String toString()
  {
    if (faked) {
      return "(faked) " + name;
    } else {
	return super.toString() + ": " + name;
    }
  }


  String getDescriptor()
  {
    if (expression == null) {
      return name;
    } else {
      return "#" + name + "(" + (isGT ? ">" : "") + expression_text() + ")";
    }
  }


  String getNodeType()
  {
    if (JJTreeOptions.getMulti()) {
      return JJTreeOptions.getNodePrefix() + name;
    } else {
      return "SimpleNode";
    }
  }


  String getNodeName()
  {
    return name;
  }


  String openNode(String nodeVar)
  {
    return "jjtree.openNodeScope(" + nodeVar + ");";
  }


  private String expression_text()
  {
    if (expression.getFirstToken().image.equals(")") &&
	expression.getLastToken().image.equals("(")) {
      return "true";
    }

    String s = "";
    Token t = expression.getFirstToken();
    while (true) {
       s += " " + t.image;
       if (t == expression.getLastToken()) {
	 break;
       }
       t = t.next;
    }
    return s;
  }


  String closeNode(String nodeVar)
  {
    if (expression == null) {
      return "jjtree.closeNodeScope(" + nodeVar + ", true);";
    } else if (isGT) {
      return "jjtree.closeNodeScope(" + nodeVar + ", jjtree.nodeArity() >" +
	expression_text() + ");";
    } else {
      return "jjtree.closeNodeScope(" + nodeVar + ", " +
	expression_text() + ");";
    }
  }


  String translateImage(Token t)
  {
    return whiteOut(t);
  }

}

/*end*/
