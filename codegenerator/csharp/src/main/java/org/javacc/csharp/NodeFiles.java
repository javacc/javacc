package org.javacc.csharp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javacc.Version;
import org.javacc.parser.Options;
import org.javacc.parser.OutputFile;
import org.javacc.utils.OutputFileGenerator;
import org.javacc.jjtree.*;

final class NodeFiles {
  private NodeFiles() {}

  /**
   * ID of the latest version (of JJTree) in which one of the Node classes
   * was modified.
   */
  static final String nodeVersion = Version.majorDotMinor;

  private static Set<String> nodesToBuild = new HashSet<String>();

  static void generateNodeType(String nodeType)
  {
    if (!nodeType.equals("Node") && !nodeType.equals("SimpleNode")) {
      nodesToBuild.add(nodeType);
    }
  }

  private static void generateTreeNodes() {
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), JJTreeGlobals.parserName + "Tree.cs");
    try {
      String[] options = new String[] {"MULTI", "NODE_USES_PARSER", "VISITOR", "TRACK_TOKENS", "NODE_PREFIX", "NODE_EXTENDS", "NODE_FACTORY", Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC};

      OutputFile outputFile = new OutputFile(file, nodeVersion, options);
      outputFile.setToolName("JJTree");
      PrintWriter pw = outputFile.getPrintWriter();

      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        pw.println("namespace " + JJTreeOptions.stringValue("NAMESPACE_OPEN"));
      }

      for (String node: nodesToBuild) {
        generateMULTINode(pw, node);
      }

      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        pw.println(JJTreeOptions.stringValue("NAMESPACE_CLOSE"));
      }
      pw.close();
    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }

  static void generatePrologue(PrintWriter pw)
  {
  }


  static String nodeConstants()
  {
    return JJTreeGlobals.parserName + "TreeConstants";
  }

  static void generateTreeConstants()
  {
    String name = nodeConstants();
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), name + ".cs");

    try {
      OutputFile outputFile = new OutputFile(file);
      PrintWriter pw = outputFile.getPrintWriter();

      List<String> nodeIds = ASTNodeDescriptor.getNodeIds();
      List<String> nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(pw);
      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        pw.println("namespace " + JJTreeOptions.stringValue("NAMESPACE_OPEN"));
      }
      pw.println("public class " + name);
      pw.println("{");

      for (int i = 0; i < nodeIds.size(); ++i) {
        String n = nodeIds.get(i);
        pw.println("  public const int " + n + " = " + i + ";");
      }

      pw.println();
      pw.println();

      pw.println("  public static string[] jjtNodeName = {");
      for (int i = 0; i < nodeNames.size(); ++i) {
        String n = nodeNames.get(i);
        pw.println("    \"" + n + "\",");
      }
      pw.println("  };");

      pw.println("}");
      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        pw.println(JJTreeOptions.stringValue("NAMESPACE_CLOSE"));
      }
      pw.close();

    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }


  static String visitorClass()
  {
    return JJTreeGlobals.parserName + "Visitor";
  }

  static void generateVisitor()
  {
    if (!JJTreeOptions.getVisitor()) {
      return;
    }

    String name = visitorClass();
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), name + ".cs");

    try {
      OutputFile outputFile = new OutputFile(file);
      PrintWriter pw = outputFile.getPrintWriter();

      List<String> nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(pw);
      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        pw.println("namespace " + JJTreeOptions.stringValue("NAMESPACE_OPEN"));
      }
      pw.println("public interface " + name);
      pw.println("{");

      String ve = mergeVisitorException();

      String argumentType = "object";
      if (!JJTreeOptions.getVisitorDataType().equals("")) {
        argumentType = JJTreeOptions.getVisitorDataType();
      }

      pw.println("  " + JJTreeOptions.getVisitorReturnType() + " Visit(SimpleNode node, " + argumentType + " data)" +
          ve + ";");
      if (JJTreeOptions.getMulti()) {
        for (int i = 0; i < nodeNames.size(); ++i) {
          String n = nodeNames.get(i);
          if (n.equals("void")) {
            continue;
          }
          String nodeType = JJTreeOptions.getNodePrefix() + n;
          pw.println("  " + JJTreeOptions.getVisitorReturnType() + " " + getVisitMethodName(nodeType) +
          "(" + nodeType +
              " node, " + argumentType + " data)" + ve + ";");
        }
      }
      pw.println("}");
      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        pw.println(JJTreeOptions.stringValue("NAMESPACE_CLOSE"));
      }
      pw.close();

    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }

  static String defaultVisitorClass()
  {
    return JJTreeGlobals.parserName + "DefaultVisitor";
  }

  private static String getVisitMethodName(String className) {
    StringBuffer sb = new StringBuffer("Visit");
    if (JJTreeOptions.booleanValue("VISITOR_METHOD_NAME_INCLUDES_TYPE_NAME")) {
      sb.append(Character.toUpperCase(className.charAt(0)));
      for (int i = 1; i < className.length(); i++) {
        sb.append(className.charAt(i));
      }
    }

    return sb.toString();
  }

  static void generateDefaultVisitor()
  {
    if (!JJTreeOptions.getVisitor()) {
      return;
    }

    String className = defaultVisitorClass();
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), className + ".cs");

    try {
      PrintWriter pw = new OutputFile(file).getPrintWriter();

      List<String> nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(pw);
      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        pw.println("namespace " + JJTreeOptions.stringValue("NAMESPACE_OPEN"));
      }
      pw.println("public class " + className + " : " + visitorClass() + "{");

      String ve = mergeVisitorException();

      String argumentType = "object";
      if (!JJTreeOptions.getVisitorDataType().equals("")) {
        argumentType = JJTreeOptions.getVisitorDataType();
      }

      String ret = JJTreeOptions.getVisitorReturnType();
      pw.println("  public virtual " + ret + " defaultVisit(SimpleNode node, " + argumentType + " data)" +
          ve + "{");
      pw.println("    node.childrenAccept(this, data);");
      pw.println("    return" + (ret.trim().equals("void") ? "" : " data") + ";");
      pw.println("  }");

      pw.println("  public virtual " + ret + " Visit(SimpleNode node, " + argumentType + " data)" +
          ve + "{");
      pw.println("    " + (ret.trim().equals("void") ? "" : "return ") + "defaultVisit(node, data);");
      pw.println("  }");

      if (JJTreeOptions.getMulti()) {
        for (int i = 0; i < nodeNames.size(); ++i) {
          String n = nodeNames.get(i);
          if (n.equals("void")) {
            continue;
          }
          String nodeType = JJTreeOptions.getNodePrefix() + n;
          pw.println("  public virtual " + ret + " " + getVisitMethodName(nodeType) +
          "(" + nodeType +
              " node, " + argumentType + " data)" + ve + "{");
          pw.println("    " + (ret.trim().equals("void") ? "" : "return ") + "defaultVisit(node, data);");
          pw.println("  }");
        }
      }
      pw.println("}");
      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        pw.println(JJTreeOptions.stringValue("NAMESPACE_CLOSE"));
      }
      pw.close();

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

  private static void generateDefaultNode() throws IOException
  {
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), "Node.cs");
    PrintWriter pw = new OutputFile(file).getPrintWriter();

    generatePrologue(pw);

    Map<String, Object> options = new HashMap<>(Options.getOptions());
    options.put(Options.NONUSER_OPTION__PARSER_NAME, JJTreeGlobals.parserName);
    options.put("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(JJTreeOptions.getVisitorReturnType().equals("void")));

    OutputFileGenerator generator = new OutputFileGenerator(
        "/templates/csharp/Node.template", options);

    generator.generate(pw);

    pw.close();
  }

  private static void generateMULTINode(PrintWriter pw, String nodeType) throws IOException
  {
    generatePrologue(pw);

    Map<String, Object> options = new HashMap<>(Options.getOptions());
    options.put(Options.NONUSER_OPTION__PARSER_NAME, JJTreeGlobals.parserName);
    options.put("NODE_TYPE", nodeType);
    options.put("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(JJTreeOptions.getVisitorReturnType().equals("void")));

    OutputFileGenerator generator = new OutputFileGenerator(
        "/templates/csharp/MultiNode.template", options);

    generator.generate(pw);
  }

  static void generateOutputFiles() throws IOException {
    generateDefaultNode();
    generateTreeNodes();
    generateTreeConstants();
    generateVisitor();
    generateDefaultVisitor();
  }

}
