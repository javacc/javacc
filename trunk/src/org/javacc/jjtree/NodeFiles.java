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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;
import org.javacc.parser.JavaCCGlobals;
final class NodeFiles {
  static final int outputBufferSize = 8096;
  private NodeFiles() {}
  static void ensure(IO io, String nodeType)
  {
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), nodeType + ".java");

    if (nodeType.equals("Node")) {
    } else if (nodeType.equals("SimpleNode")) {
      ensure(io, "Node");
    } else {
      ensure(io, "SimpleNode");
    }

    /* Only build the node file if we're dealing with Node.java, or
       the NODE_BUILD_FILES option is set. */
    if (!(nodeType.equals("Node") || JJTreeOptions.getBuildNodeFiles())) {
      return;
    }

    if (file.exists()) {
      return;
    }

    io.getMsg().println("File \"" + file +
      "\" does not exist.  Will create one.");

    PrintWriter ostr;

    try {
      ostr = new PrintWriter(new BufferedWriter(
          new FileWriter(file), outputBufferSize));

      if (nodeType.equals("Node")) {
        generateNode_java(ostr);
      } else if (nodeType.equals("SimpleNode")) {
        generateSimpleNode_java(ostr);
      } else {
        generateMULTINode_java(ostr, nodeType);
      }

      ostr.close();

    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }


  static void generatePrologue(PrintWriter ostr, String fileName)
  {
    ostr.println("/* " +
     JavaCCGlobals.getIdString(JJTreeGlobals.toolList, fileName) +
     " */");
    ostr.println();
    if (!JJTreeGlobals.packageName.equals("")) {
      ostr.println("package " + JJTreeGlobals.packageName + ";");
      ostr.println();
    }
  }


  static String nodeConstants()
  {
    return JJTreeGlobals.parserName + "TreeConstants";
  }

  static void generateTreeConstants_java()
  {
    String name = nodeConstants();
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), name + ".java");

    try {
      PrintWriter ostr = new PrintWriter(new BufferedWriter(
                new FileWriter(file),
                outputBufferSize));

      Vector nodeIds = ASTNodeDescriptor.getNodeIds();
      Vector nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(ostr, file.toString());
      ostr.println("public interface " + name);
      ostr.println("{");

      for (int i = 0; i < nodeIds.size(); ++i) {
        String n = (String)nodeIds.elementAt(i);
        ostr.println("  public int " + n + " = " + i + ";");
      }

      ostr.println();
      ostr.println();

      ostr.println("  public String[] jjtNodeName = {");
      for (int i = 0; i < nodeNames.size(); ++i) {
        String n = (String)nodeNames.elementAt(i);
        ostr.println("    \"" + n + "\",");
      }
      ostr.println("  };");

      ostr.println("}");
      ostr.close();

    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }


  static String visitorClass()
  {
    return JJTreeGlobals.parserName + "Visitor";
  }

  static void generateVisitor_java()
  {
    if (!JJTreeOptions.getVisitor()) {
      return;
    }

    String name = visitorClass();
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), name + ".java");

    try {
      PrintWriter ostr = new PrintWriter(new BufferedWriter(
                new FileWriter(file),
                outputBufferSize));

      Vector nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(ostr, file.toString());
      ostr.println("public interface " + name);
      ostr.println("{");

      String ve = mergeVisitorException();

      ostr.println("  public Object visit(SimpleNode node, Object data)" +
          ve + ";");
      if (JJTreeOptions.getMulti()) {
        for (int i = 0; i < nodeNames.size(); ++i) {
          String n = (String)nodeNames.elementAt(i);
          if (n.equals("void")) {
            continue;
          }
          String nodeType = JJTreeOptions.getNodePrefix() + n;
           ostr.println("  public Object visit(" + nodeType +
               " node, Object data)" + ve + ";");
        }
      }
      ostr.println("}");
      ostr.close();

    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }

  private static String mergeVisitorException() {
    String ve = JJTreeOptions.getVisitorException();
    if (!"".equals(ve)) {
      ve = " throws " + ve;
    }
      return ve;
  }


  private static void generateNode_java(PrintWriter ostr)
  {
    generatePrologue(ostr, "Node.java");

    ostr.println("/* All AST nodes must implement this interface.  It provides basic");
    ostr.println("   machinery for constructing the parent and child relationships");
    ostr.println("   between nodes. */");
    ostr.println("");
    ostr.println("public interface Node {");
    ostr.println("");
    ostr.println("  /** This method is called after the node has been made the current");
    ostr.println("    node.  It indicates that child nodes can now be added to it. */");
    ostr.println("  public void jjtOpen();");
    ostr.println("");
    ostr.println("  /** This method is called after all the child nodes have been");
    ostr.println("    added. */");
    ostr.println("  public void jjtClose();");
    ostr.println("");
    ostr.println("  /** This pair of methods are used to inform the node of its");
    ostr.println("    parent. */");
    ostr.println("  public void jjtSetParent(Node n);");
    ostr.println("  public Node jjtGetParent();");
    ostr.println("");
    ostr.println("  /** This method tells the node to add its argument to the node's");
    ostr.println("    list of children.  */");
    ostr.println("  public void jjtAddChild(Node n, int i);");
    ostr.println("");
    ostr.println("  /** This method returns a child node.  The children are numbered");
    ostr.println("     from zero, left to right. */");
    ostr.println("  public Node jjtGetChild(int i);");
    ostr.println("");
    ostr.println("  /** Return the number of children the node has. */");
    ostr.println("  public int jjtGetNumChildren();");

    if (JJTreeOptions.getVisitor()) {

      ostr.println("");
      ostr.println("  /** Accept the visitor. **/");
      ostr.println("  public Object jjtAccept(" + visitorClass() +
          " visitor, Object data)" + mergeVisitorException() + ";");
    }

    ostr.println("}");

    ostr.close();
  }


  private static void generateSimpleNode_java(PrintWriter ostr)
  {
    generatePrologue(ostr, "SimpleNode.java");

    ostr.print("public class SimpleNode");
    if (!JJTreeOptions.getNodeExtends().equals(""))
       ostr.print(" extends " + JJTreeOptions.getNodeExtends());
    ostr.println(" implements Node {");
    ostr.println("  protected Node parent;");
    ostr.println("  protected Node[] children;");
    ostr.println("  protected int id;");
    ostr.println("  protected " + JJTreeGlobals.parserName + " parser;");
    ostr.println("");
    ostr.println("  public SimpleNode(int i) {");
    ostr.println("    id = i;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  public SimpleNode(" + JJTreeGlobals.parserName + " p, int i) {");
    ostr.println("    this(i);");
    ostr.println("    parser = p;");
    ostr.println("  }");
    ostr.println("");

    if (JJTreeOptions.getNodeFactory()) {
      ostr.println("  public static Node jjtCreate(int id) {");
      ostr.println("    return new SimpleNode(id);");
      ostr.println("  }");
      ostr.println("");
      ostr.println("  public static Node jjtCreate(" + JJTreeGlobals.parserName + " p, int id) {");
      ostr.println("    return new SimpleNode(p, id);");
      ostr.println("  }");
      ostr.println("");
    }

    ostr.println("  public void jjtOpen() {");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  public void jjtClose() {");
    ostr.println("  }");
    ostr.println("  ");
    ostr.println("  public void jjtSetParent(Node n) { parent = n; }");
    ostr.println("  public Node jjtGetParent() { return parent; }");
    ostr.println("");
    ostr.println("  public void jjtAddChild(Node n, int i) {");
    ostr.println("    if (children == null) {");
    ostr.println("      children = new Node[i + 1];");
    ostr.println("    } else if (i >= children.length) {");
    ostr.println("      Node c[] = new Node[i + 1];");
    ostr.println("      System.arraycopy(children, 0, c, 0, children.length);");
    ostr.println("      children = c;");
    ostr.println("    }");
    ostr.println("    children[i] = n;");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  public Node jjtGetChild(int i) {");
    ostr.println("    return children[i];");
    ostr.println("  }");
    ostr.println("");
    ostr.println("  public int jjtGetNumChildren() {");
    ostr.println("    return (children == null) ? 0 : children.length;");
    ostr.println("  }");
    ostr.println("");

    if (JJTreeOptions.getVisitor()) {
      String ve = mergeVisitorException();
      ostr.println("  /** Accept the visitor. **/");
      ostr.println("  public Object jjtAccept(" + visitorClass() +
          " visitor, Object data)" + ve + " {");
      ostr.println("    return visitor.visit(this, data);");
      ostr.println("  }");
      ostr.println("");

      ostr.println("  /** Accept the visitor. **/");
      ostr.println("  public Object childrenAccept(" + visitorClass() +
          " visitor, Object data)" + ve + " {");
      ostr.println("    if (children != null) {");
      ostr.println("      for (int i = 0; i < children.length; ++i) {");
      ostr.println("        children[i].jjtAccept(visitor, data);");
      ostr.println("      }");
      ostr.println("    }");
      ostr.println("    return data;");
      ostr.println("  }");
      ostr.println("");
    }

    ostr.println("  /* You can override these two methods in subclasses of SimpleNode to");
    ostr.println("     customize the way the node appears when the tree is dumped.  If");
    ostr.println("     your output uses more than one line you should override");
    ostr.println("     toString(String), otherwise overriding toString() is probably all");
    ostr.println("     you need to do. */");
    ostr.println("");
    ostr.println("  public String toString() { return " + nodeConstants() + ".jjtNodeName[id]; }");
    ostr.println("  public String toString(String prefix) { return prefix + toString(); }");

    ostr.println("");
    ostr.println("  /* Override this method if you want to customize how the node dumps");
    ostr.println("     out its children. */");
    ostr.println("");
    ostr.println("  public void dump(String prefix) {");
    ostr.println("    System.out.println(toString(prefix));");
    ostr.println("    if (children != null) {");
    ostr.println("      for (int i = 0; i < children.length; ++i) {");
    ostr.println("  SimpleNode n = (SimpleNode)children[i];");
    ostr.println("  if (n != null) {");
    ostr.println("    n.dump(prefix + \" \");");
    ostr.println("  }");
    ostr.println("      }");
    ostr.println("    }");
    ostr.println("  }");
    ostr.println("}");
    ostr.println("");
  }


  private static void generateMULTINode_java(PrintWriter ostr, String nodeType)
  {
    generatePrologue(ostr, nodeType + ".java");

    ostr.println("public class " + nodeType + " extends SimpleNode {");
    ostr.println("  public " + nodeType + "(int id) {");
    ostr.println("    super(id);");
    ostr.println("  }");
    ostr.println();
    ostr.println("  public " + nodeType + "(" + JJTreeGlobals.parserName + " p, int id) {");
    ostr.println("    super(p, id);");
    ostr.println("  }");
    ostr.println();

    if (JJTreeOptions.getNodeFactory()) {
      ostr.println("  public static Node jjtCreate(int id) {");
      ostr.println("      return new " + nodeType + "(id);");
      ostr.println("  }");
      ostr.println();
      ostr.println("  public static Node jjtCreate(" +
          JJTreeGlobals.parserName + " p, int id) {");
      ostr.println("      return new " + nodeType + "(p, id);");
      ostr.println("  }");
    }

    if (JJTreeOptions.getVisitor()) {
      ostr.println("");
      ostr.println("  /** Accept the visitor. **/");
      ostr.println("  public Object jjtAccept(" + visitorClass() +
          " visitor, Object data)" + mergeVisitorException() + " {");
      ostr.println("    return visitor.visit(this, data);");
      ostr.println("  }");
    }

    ostr.println("}");
    ostr.close();
  }

}


/*end*/



