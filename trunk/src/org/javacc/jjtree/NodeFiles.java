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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.javacc.parser.OutputFile;
final class NodeFiles {
  private NodeFiles() {}

  /**
   * ID of the latest version (of JJTree) in which one of the Node classes
   * was modified.
   */
  static final String nodeVersion = "4.1";

  static Set nodesGenerated = new HashSet();

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

    if (file.exists() && nodesGenerated.contains(file.getName())) {
      return;
    }

    try {
      String[] options = new String[] {"MULTI", "NODE_USES_PARSER", "VISITOR", "TRACK_TOKENS", "NODE_PREFIX", "NODE_EXTENDS", "NODE_FACTORY"};
      OutputFile outputFile = new OutputFile(file, nodeVersion, options);
      outputFile.setToolName("JJTree");

      nodesGenerated.add(file.getName());

      if (!outputFile.needToWrite) {
        return;
      }

      if (nodeType.equals("Node")) {
        generateNode_java(outputFile);
      } else if (nodeType.equals("SimpleNode")) {
        generateSimpleNode_java(outputFile);
      } else {
        generateMULTINode_java(outputFile, nodeType);
      }

      outputFile.close();

    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }


  static void generatePrologue(PrintWriter ostr)
  {
    // Output the node's package name. JJTreeGlobals.nodePackageName
    // will be the value of NODE_PACKAGE in OPTIONS; if that wasn't set it
    // will default to the parser's package name.
    // If the package names are different we will need to import classes
    // from the parser's package.
    if (!JJTreeGlobals.nodePackageName.equals("")) {
      ostr.println("package " + JJTreeGlobals.nodePackageName + ";");
      ostr.println();
      if (!JJTreeGlobals.nodePackageName.equals(JJTreeGlobals.packageName)) {
        ostr.println("import " + JJTreeGlobals.packageName + ".*;");
        ostr.println();
      }

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
      OutputFile outputFile = new OutputFile(file);
      PrintWriter ostr = outputFile.getPrintWriter();

      Vector nodeIds = ASTNodeDescriptor.getNodeIds();
      Vector nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(ostr);
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
      OutputFile outputFile = new OutputFile(file);
      PrintWriter ostr = outputFile.getPrintWriter();

      Vector nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(ostr);
      ostr.println("public interface " + name);
      ostr.println("{");

      String ve = mergeVisitorException();

      String argumentType = "Object";
      if (!JJTreeOptions.getVisitorDataType().equals("")) {
        argumentType = JJTreeOptions.getVisitorDataType();
      }

      ostr.println("  public " + JJTreeOptions.getVisitorReturnType() + " visit(SimpleNode node, " + argumentType + " data)" +
          ve + ";");
      if (JJTreeOptions.getMulti()) {
        for (int i = 0; i < nodeNames.size(); ++i) {
          String n = (String)nodeNames.elementAt(i);
          if (n.equals("void")) {
            continue;
          }
          String nodeType = JJTreeOptions.getNodePrefix() + n;
          ostr.println("  public " + JJTreeOptions.getVisitorReturnType() + " visit(" + nodeType +
              " node, " + argumentType + " data)" + ve + ";");
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


  private static void generateNode_java(OutputFile outputFile) throws IOException
  {
    PrintWriter ostr = outputFile.getPrintWriter();

    generatePrologue(ostr);

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
      String argumentType = "Object";
      if (!JJTreeOptions.getVisitorDataType().equals("")) {
        argumentType = JJTreeOptions.getVisitorDataType();
      }

      ostr.println("");
      ostr.println("  /** Accept the visitor. **/");
      ostr.println("  public " + JJTreeOptions.getVisitorReturnType() + " jjtAccept(" + visitorClass() +
          " visitor, " + argumentType + " data)" + mergeVisitorException() + ";");
    }

    ostr.println("}");

    ostr.close();
  }


  private static void generateSimpleNode_java(OutputFile outputFile) throws IOException
  {
    PrintWriter ostr = outputFile.getPrintWriter();

    generatePrologue(ostr);

    ostr.print("public class SimpleNode");
    if (!JJTreeOptions.getNodeExtends().equals(""))
      ostr.print(" extends " + JJTreeOptions.getNodeExtends());
    ostr.println(" implements Node {");
    ostr.println("  protected Node parent;");
    ostr.println("  protected Node[] children;");
    ostr.println("  protected int id;");
    ostr.println("  protected Object value;");
    ostr.println("  protected " + JJTreeGlobals.parserName + " parser;");

    if (JJTreeOptions.getTrackTokens()) {
      ostr.println("  protected Token firstToken;");
      ostr.println("  protected Token lastToken;");
    }

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

    if (JJTreeOptions.getNodeFactory().length() > 0) {
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
    ostr.println("");
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
    ostr.println("  public void jjtSetValue(Object value) { this.value = value; }");
    ostr.println("  public Object jjtGetValue() { return value; }");
    ostr.println("");

    if (JJTreeOptions.getTrackTokens()) {
      ostr.println("  public Token jjtGetFirstToken() { return firstToken; }");
      ostr.println("  public void jjtSetFirstToken(Token token) { this.firstToken = token; }");
      ostr.println("  public Token jjtGetLastToken() { return lastToken; }");
      ostr.println("  public void jjtSetLastToken(Token token) { this.lastToken = token; }");
      ostr.println("");
    }

    if (JJTreeOptions.getVisitor()) {
      String ve = mergeVisitorException();
      String argumentType = "Object";
      if (!JJTreeOptions.getVisitorDataType().equals("")) {
        argumentType = JJTreeOptions.getVisitorDataType();
      }

      ostr.println("  /** Accept the visitor. **/");
      ostr.println("  public " + JJTreeOptions.getVisitorReturnType() + " jjtAccept(" + visitorClass() +
          " visitor, " + argumentType + " data)" + ve + " {");
      ostr.println("    " + (JJTreeOptions.getVisitorReturnType().equals("void") ? "" : "return ") + "visitor.visit(this, data);");
      ostr.println("  }");
      ostr.println("");

      ostr.println("  /** Accept the visitor. **/");
      ostr.println("  public Object childrenAccept(" + visitorClass() +
          " visitor, " + argumentType + " data)" + ve + " {");
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


  private static void generateMULTINode_java(OutputFile outputFile, String nodeType) throws IOException
  {
    PrintWriter ostr = outputFile.getPrintWriter();

    generatePrologue(ostr);

    if (JJTreeOptions.getNodeClass().length() > 0) {
      ostr.println("public class " + nodeType + " extends " + JJTreeOptions.getNodeClass() + "{");
    } else {
      ostr.println("public class " + nodeType + " extends SimpleNode {");
    }

    ostr.println("  public " + nodeType + "(int id) {");
    ostr.println("    super(id);");
    ostr.println("  }");
    ostr.println();
    ostr.println("  public " + nodeType + "(" + JJTreeGlobals.parserName + " p, int id) {");
    ostr.println("    super(p, id);");
    ostr.println("  }");
    ostr.println();

    if (JJTreeOptions.getNodeFactory().length() > 0) {
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
      String argumentType = "Object";
      if (!JJTreeOptions.getVisitorDataType().equals("")) {
        argumentType = JJTreeOptions.getVisitorDataType();
      }

      ostr.println("");
      ostr.println("  /** Accept the visitor. **/");
      ostr.println("  public " + JJTreeOptions.getVisitorReturnType() + " jjtAccept(" + visitorClass() +
          " visitor, " + argumentType + " data)" + mergeVisitorException() + " {");
      ostr.println("    " + (JJTreeOptions.getVisitorReturnType().equals("void") ? "" : "return ") + "visitor.visit(this, data);");
      ostr.println("  }");
    }

    ostr.println("}");
    ostr.close();
  }

}


/*end*/



