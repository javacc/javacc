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

  static Set nodesGenerated = new HashSet();

  static void ensure(IO io, String nodeType)
  {
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), nodeType + ".cs");

    if (nodeType.equals("Node")) {
    } else if (nodeType.equals("SimpleNode")) {
      ensure(io, "Node");
    } else {
      ensure(io, "SimpleNode");
    }

    /* Only build the node file if we're dealing with Node.cs, or
       the NODE_BUILD_FILES option is set. */
    if (!(nodeType.equals("Node") || JJTreeOptions.getBuildNodeFiles())) {
      return;
    }

    if (file.exists() && nodesGenerated.contains(file.getName())) {
      return;
    }

    try {
      String[] options = new String[] {"MULTI", "NODE_USES_PARSER", "VISITOR", "TRACK_TOKENS", "NODE_PREFIX", "NODE_EXTENDS", "NODE_FACTORY", Options.USEROPTION__SUPPORT_CLASS_VISIBILITY_PUBLIC};
      OutputFile outputFile = new OutputFile(file, nodeVersion, options);
      outputFile.setToolName("JJTree");

      nodesGenerated.add(file.getName());

      if (!outputFile.needToWrite) {
        return;
      }

      if (nodeType.equals("Node")) {
        generateNode(outputFile);
      } else if (nodeType.equals("SimpleNode")) {
        generateSimpleNode(outputFile);
      } else {
        generateMULTINode(outputFile, nodeType);
      }

      outputFile.close();

    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }


  static void generatePrologue(PrintWriter ostr)
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
      PrintWriter ostr = outputFile.getPrintWriter();

      List nodeIds = ASTNodeDescriptor.getNodeIds();
      List nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(ostr);
      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        ostr.println("namespace " + JJTreeOptions.stringValue("NAMESPACE_OPEN"));
      }
      ostr.println("public class " + name);
      ostr.println("{");

      for (int i = 0; i < nodeIds.size(); ++i) {
        String n = (String)nodeIds.get(i);
        ostr.println("  public const int " + n + " = " + i + ";");
      }

      ostr.println();
      ostr.println();

      ostr.println("  public static string[] jjtNodeName = {");
      for (int i = 0; i < nodeNames.size(); ++i) {
        String n = (String)nodeNames.get(i);
        ostr.println("    \"" + n + "\",");
      }
      ostr.println("  };");

      ostr.println("}");
      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        ostr.println(JJTreeOptions.stringValue("NAMESPACE_CLOSE"));
      }
      ostr.close();

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
      PrintWriter ostr = outputFile.getPrintWriter();

      List nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(ostr);
      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        ostr.println("namespace " + JJTreeOptions.stringValue("NAMESPACE_OPEN"));
      }
      ostr.println("public interface " + name);
      ostr.println("{");

      String ve = mergeVisitorException();

      String argumentType = "System.Object";
      if (!JJTreeOptions.getVisitorDataType().equals("")) {
        argumentType = JJTreeOptions.getVisitorDataType();
      }

      ostr.println("  " + JJTreeOptions.getVisitorReturnType() + " Visit(SimpleNode node, " + argumentType + " data)" +
          ve + ";");
      if (JJTreeOptions.getMulti()) {
        for (int i = 0; i < nodeNames.size(); ++i) {
          String n = (String)nodeNames.get(i);
          if (n.equals("void")) {
            continue;
          }
          String nodeType = JJTreeOptions.getNodePrefix() + n;
          ostr.println("  " + JJTreeOptions.getVisitorReturnType() + " " + getVisitMethodName(nodeType) +
          "(" + nodeType +
              " node, " + argumentType + " data)" + ve + ";");
        }
      }
      ostr.println("}");
      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        ostr.println(JJTreeOptions.stringValue("NAMESPACE_CLOSE"));
      }
      ostr.close();

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
      OutputFile outputFile = new OutputFile(file);
      PrintWriter ostr = outputFile.getPrintWriter();

      List nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(ostr);
      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        ostr.println("namespace " + JJTreeOptions.stringValue("NAMESPACE_OPEN"));
      }
      ostr.println("public class " + className + " : " + visitorClass() + "{");

      String ve = mergeVisitorException();

      String argumentType = "System.Object";
      if (!JJTreeOptions.getVisitorDataType().equals("")) {
        argumentType = JJTreeOptions.getVisitorDataType();
      }

      String ret = JJTreeOptions.getVisitorReturnType();
      ostr.println("  public virtual " + ret + " defaultVisit(SimpleNode node, " + argumentType + " data)" +
          ve + "{");
      ostr.println("    node.childrenAccept(this, data);");
      ostr.println("    return" + (ret.trim().equals("void") ? "" : " data") + ";");
      ostr.println("  }");

      ostr.println("  public virtual " + ret + " Visit(SimpleNode node, " + argumentType + " data)" +
          ve + "{");
      ostr.println("    " + (ret.trim().equals("void") ? "" : "return ") + "defaultVisit(node, data);");
      ostr.println("  }");

      if (JJTreeOptions.getMulti()) {
        for (int i = 0; i < nodeNames.size(); ++i) {
          String n = (String)nodeNames.get(i);
          if (n.equals("void")) {
            continue;
          }
          String nodeType = JJTreeOptions.getNodePrefix() + n;
          ostr.println("  public " + ret + " " + getVisitMethodName(nodeType) +
          "(" + nodeType +
              " node, " + argumentType + " data)" + ve + "{");
          ostr.println("    " + (ret.trim().equals("void") ? "" : "return ") + "defaultVisit(node, data);");
          ostr.println("  }");
        }
      }
      ostr.println("}");
      if (JJTreeOptions.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
        ostr.println(JJTreeOptions.stringValue("NAMESPACE_CLOSE"));
      }
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


  private static void generateNode(OutputFile outputFile) throws IOException
  {
    PrintWriter ostr = outputFile.getPrintWriter();

    generatePrologue(ostr);
    
    Map options = new HashMap(Options.getOptions());
    options.put(Options.NONUSER_OPTION__PARSER_NAME, JJTreeGlobals.parserName);
    
    OutputFileGenerator generator = new OutputFileGenerator(
        "/templates/csharp/Node.template", options);
    
    generator.generate(ostr);

    ostr.close();
  }


  private static void generateSimpleNode(OutputFile outputFile) throws IOException
  {
    PrintWriter ostr = outputFile.getPrintWriter();

    generatePrologue(ostr);
    
    Map options = new HashMap(Options.getOptions());
    options.put(Options.NONUSER_OPTION__PARSER_NAME, JJTreeGlobals.parserName);
    options.put("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(JJTreeOptions.getVisitorReturnType().equals("void")));
    
    OutputFileGenerator generator = new OutputFileGenerator(
        "/templates/csharp/SimpleNode.template", options);
    
    generator.generate(ostr);

    ostr.close();
  }


  private static void generateMULTINode(OutputFile outputFile, String nodeType) throws IOException
  {
    PrintWriter ostr = outputFile.getPrintWriter();

    generatePrologue(ostr);

    Map options = new HashMap(Options.getOptions());
    options.put(Options.NONUSER_OPTION__PARSER_NAME, JJTreeGlobals.parserName);
    options.put("NODE_TYPE", nodeType);
    options.put("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(JJTreeOptions.getVisitorReturnType().equals("void")));
    
    OutputFileGenerator generator = new OutputFileGenerator(
        "/templates/csharp/MultiNode.template", options);
    
    generator.generate(ostr);

    ostr.close();
  }

}
