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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

class JJTreeState
{

  static void insertParserMembers(IO io) {
    String s;

    if (JJTreeOptions.getStatic()) {
      s = "static ";
    } else {
      s = "";
    }

    io.println();
    io.println("  protected " + s + nameState() +
		 " jjtree = new " + nameState() + "();");
    io.println();
  }


  private static String nameState() {
    return "JJT" + JJTreeGlobals.parserName + "State";
  }


  static void generateTreeState_java()
  {
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), nameState() + ".java");

    if (file.exists()) {
      return;
    }

    try {
      PrintWriter ostr = new PrintWriter(new BufferedWriter(
					      new FileWriter(file),
					      8096));
      NodeFiles.generatePrologue(ostr, file.toString());
      insertState(ostr);
      ostr.close();
    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }

  
  private static void insertState(PrintWriter ostr) {
    ostr.println("class " + nameState() + " {");

    if (!JJTreeOptions.getJdkVersion().equals("1.5"))
       ostr.println("  private java.util.Stack nodes;");
    else
       ostr.println("  private java.util.Stack<Node> nodes;");

    if (!JJTreeOptions.getJdkVersion().equals("1.5"))
       ostr.println("  private java.util.Stack marks;");
    else
       ostr.println("  private java.util.Stack<Integer> marks;");

    ostr.println("");
    ostr.println("  private int sp;		// number of nodes on stack");
    ostr.println("  private int mk;		// current mark");
    ostr.println("  private boolean node_created;");
    ostr.println("");
    ostr.println("  " + nameState() + "() {");

    if (!JJTreeOptions.getJdkVersion().equals("1.5"))
       ostr.println("    nodes = new java.util.Stack();");
    else
       ostr.println("    nodes = new java.util.Stack<Node>();");

    if (!JJTreeOptions.getJdkVersion().equals("1.5"))
       ostr.println("    marks = new java.util.Stack();");
    else
       ostr.println("    marks = new java.util.Stack<Integer>();");

    ostr.println("    sp = 0;");
    ostr.println("    mk = 0;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Determines whether the current node was actually closed and");
    ostr.println("     pushed.  This should only be called in the final user action of a");
    ostr.println("     node scope.  */");
    ostr.println("  boolean nodeCreated() {");
    ostr.println("    return node_created;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Call this to reinitialize the node stack.  It is called");
    ostr.println("     automatically by the parser's ReInit() method. */");
    ostr.println("  void reset() {");
    ostr.println("    nodes.removeAllElements();");
    ostr.println("    marks.removeAllElements();");
    ostr.println("    sp = 0;");
    ostr.println("    mk = 0;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Returns the root node of the AST.  It only makes sense to call");
    ostr.println("     this after a successful parse. */");
    ostr.println("  Node rootNode() {");
    ostr.println("    return (Node)nodes.elementAt(0);");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Pushes a node on to the stack. */");
    ostr.println("  void pushNode(Node n) {");
    ostr.println("    nodes.push(n);");
    ostr.println("    ++sp;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Returns the node on the top of the stack, and remove it from the");
    ostr.println("     stack.  */");
    ostr.println("  Node popNode() {");
    ostr.println("    if (--sp < mk) {");
    ostr.println("      mk = ((Integer)marks.pop()).intValue();");
    ostr.println("    }");
    ostr.println("    return (Node)nodes.pop();");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Returns the node currently on the top of the stack. */");
    ostr.println("  Node peekNode() {");
    ostr.println("    return (Node)nodes.peek();");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  /* Returns the number of children on the stack in the current node");
    ostr.println("     scope. */");
    ostr.println("  int nodeArity() {");
    ostr.println("    return sp - mk;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("");
    ostr.println("  void clearNodeScope(Node n) {");
    ostr.println("    while (sp > mk) {");
    ostr.println("      popNode();");
    ostr.println("    }");
    ostr.println("    mk = ((Integer)marks.pop()).intValue();");
    ostr.println("  }");
    ostr.println("");
    ostr.println("");
    ostr.println("  void openNodeScope(Node n) {");
    ostr.println("    marks.push(new Integer(mk));");
    ostr.println("    mk = sp;");
    ostr.println("    n.jjtOpen();");
    ostr.println("  }");
    ostr.println("");
    ostr.println("");
    ostr.println("  /* A definite node is constructed from a specified number of");
    ostr.println("     children.  That number of nodes are popped from the stack and");
    ostr.println("     made the children of the definite node.  Then the definite node");
    ostr.println("     is pushed on to the stack. */");
    ostr.println("  void closeNodeScope(Node n, int num) {");
    ostr.println("    mk = ((Integer)marks.pop()).intValue();");
    ostr.println("    while (num-- > 0) {");
    ostr.println("      Node c = popNode();");
    ostr.println("      c.jjtSetParent(n);");
    ostr.println("      n.jjtAddChild(c, num);");
    ostr.println("    }");
    ostr.println("    n.jjtClose();");
    ostr.println("    pushNode(n);");
    ostr.println("    node_created = true;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("");
    ostr.println("  /* A conditional node is constructed if its condition is true.  All");
    ostr.println("     the nodes that have been pushed since the node was opened are");
    ostr.println("     made children of the the conditional node, which is then pushed");
    ostr.println("     on to the stack.  If the condition is false the node is not");
    ostr.println("     constructed and they are left on the stack. */");
    ostr.println("  void closeNodeScope(Node n, boolean condition) {");
    ostr.println("    if (condition) {");
    ostr.println("      int a = nodeArity();");
    ostr.println("      mk = ((Integer)marks.pop()).intValue();");
    ostr.println("      while (a-- > 0) {");
    ostr.println("	Node c = popNode();");
    ostr.println("	c.jjtSetParent(n);");
    ostr.println("	n.jjtAddChild(c, a);");
    ostr.println("      }");
    ostr.println("      n.jjtClose();");
    ostr.println("      pushNode(n);");
    ostr.println("      node_created = true;");
    ostr.println("    } else {");
    ostr.println("      mk = ((Integer)marks.pop()).intValue();");
    ostr.println("      node_created = false;");
    ostr.println("    }");
    ostr.println("  }");
    ostr.println("}");
  }

}

/*end*/
