
package org.javacc.cpp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

  private static List<String> headersForJJTreeH = new ArrayList<>();
  /**
   * ID of the latest version (of JJTree) in which one of the Node classes was modified.
   */
  static final String        nodeVersion  = Version.majorDotMinor;

  private static Set<String> nodesToBuild = new HashSet<>();

  static void generateNodeType(String nodeType) {
    if (!nodeType.equals("Node") && !nodeType.equals("SimpleNode")) {
      nodesToBuild.add(nodeType);
    }
  }

  public static String nodeIncludeFile() {
    return new File(JJTreeOptions.getJJTreeOutputDirectory(), "Node.h").getAbsolutePath();
  }

  public static String jjtreeIncludeFile() {
    return new File(JJTreeOptions.getJJTreeOutputDirectory(), JJTreeGlobals.parserName + "Tree.h").getAbsolutePath();
  }

  public static String jjtreeImplFile() {
    return new File(JJTreeOptions.getJJTreeOutputDirectory(), JJTreeGlobals.parserName + "Tree.cc").getAbsolutePath();
  }

  private static String visitorIncludeFile() {
    String name = visitorClass();
    return new File(JJTreeOptions.getJJTreeOutputDirectory(), name + ".h").getAbsolutePath();
  }

  private static void generateNodeHeader()
  {
    File file = new File(nodeIncludeFile());
    OutputFile outputFile = null;

    try {
      String[] options = new String[] {"MULTI", "NODE_USES_PARSER", "VISITOR", "TRACK_TOKENS", "NODE_PREFIX", "NODE_EXTENDS", "NODE_FACTORY", "SUPPORT_CLASS_VISIBILITY_PUBLIC"};
      outputFile = new OutputFile(file, nodeVersion, options);
      outputFile.setToolName("JJTree");

      if (file.exists() && !outputFile.needToWrite) {
        return;
      }

      Map<String, Object> optionMap = new HashMap<>(Options.getOptions());
      optionMap.put("PARSER_NAME", JJTreeGlobals.parserName);
      optionMap.put("VISITOR_RETURN_TYPE", getVisitorReturnType());
      optionMap.put("VISITOR_DATA_TYPE", getVisitorArgumentType());
      optionMap.put("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(getVisitorReturnType().equals("void")));
      generateFile(outputFile, "/templates/cpp/Node.h.template", optionMap, false);
    } catch (IOException e) {
      throw new Error(e.toString());
    }
    finally {
      if (outputFile != null) { try { outputFile.close();  } catch(IOException ioe) {} }
    }
  }

  private static void generateTreeInterface()
  {
    File file = new File(jjtreeIncludeFile());
    OutputFile outputFile = null;

    try {
      String[] options = new String[] {"MULTI", "NODE_USES_PARSER", "VISITOR", "TRACK_TOKENS", "NODE_PREFIX", "NODE_EXTENDS", "NODE_FACTORY", "SUPPORT_CLASS_VISIBILITY_PUBLIC"};
      outputFile = new OutputFile(file, nodeVersion, options);
      outputFile.setToolName("JJTree");

      if (file.exists() && !outputFile.needToWrite) {
        return;
      }

      Map<String, Object> optionMap = new HashMap<>(Options.getOptions());
      optionMap.put("PARSER_NAME", JJTreeGlobals.parserName);
      optionMap.put("VISITOR_RETURN_TYPE", getVisitorReturnType());
      optionMap.put("VISITOR_DATA_TYPE", getVisitorArgumentType());
      optionMap.put("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(getVisitorReturnType().equals("void")));

      PrintWriter ostr = outputFile.getPrintWriter();
      ostr.println("#ifndef " + file.getName().replace('.', '_').toUpperCase());
      ostr.println("#define " + file.getName().replace('.', '_').toUpperCase());
      generateFile(outputFile, "/templates/cpp/TreeIncludeHeader.template", optionMap, false);

      boolean hasNamespace = Options.stringValue("NAMESPACE").length() > 0;
      if (hasNamespace) {
        ostr.println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
      }

      generateFile(outputFile, "/templates/cpp/SimpleNodeInterface.template", optionMap, false);
      for (Iterator<String> i = nodesToBuild.iterator(); i.hasNext(); ) {
        String s = i.next();
        optionMap.put("NODE_TYPE", s);
        generateFile(outputFile, "/templates/cpp/MultiNodeInterface.template", optionMap, false);
      }

      if (hasNamespace) {
        ostr.println(Options.stringValue("NAMESPACE_CLOSE"));
      }
      ostr.println("#endif ");
    } catch (IOException e) {
      throw new Error(e.toString());
    }
    finally {
      if (outputFile != null) { try { outputFile.close();  } catch(IOException ioe) {} }
    }
  }

  private static void generateTreeImpl()
  {
    File file = new File(jjtreeImplFile());
    OutputFile outputFile = null;

    try {
      String[] options = new String[] {"MULTI", "NODE_USES_PARSER", "VISITOR", "TRACK_TOKENS", "NODE_PREFIX", "NODE_EXTENDS", "NODE_FACTORY", "SUPPORT_CLASS_VISIBILITY_PUBLIC"};
      outputFile = new OutputFile(file, nodeVersion, options);
      outputFile.setToolName("JJTree");

      if (file.exists() && !outputFile.needToWrite) {
        return;
      }

      Map<String, Object> optionMap = new HashMap<>(Options.getOptions());
      optionMap.put("PARSER_NAME", JJTreeGlobals.parserName);
      optionMap.put("VISITOR_RETURN_TYPE", getVisitorReturnType());
      optionMap.put("VISITOR_DATA_TYPE", getVisitorArgumentType());
      optionMap.put("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(getVisitorReturnType().equals("void")));
      generateFile(outputFile, "/templates/cpp/TreeImplHeader.template", optionMap, false);

      boolean hasNamespace = Options.stringValue("NAMESPACE").length() > 0;
      if (hasNamespace) {
        outputFile.getPrintWriter().println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
      }

      generateFile(outputFile, "/templates/cpp/SimpleNodeImpl.template", optionMap, false);
      for (Iterator<String> i = nodesToBuild.iterator(); i.hasNext(); ) {
        String s = i.next();
        optionMap.put("NODE_TYPE", s);
        generateFile(outputFile, "/templates/cpp/MultiNodeImpl.template", optionMap, false);
      }

      if (hasNamespace) {
        outputFile.getPrintWriter().println(Options.stringValue("NAMESPACE_CLOSE"));
      }
    } catch (IOException e) {
      throw new Error(e.toString());
    }
    finally {
      if (outputFile != null) { try { outputFile.close();  } catch(IOException ioe) {} }
    }
  }

  static void generatePrologue(PrintWriter pw)
  {
    // Output the node's namespace name?
  }


  static String nodeConstants()
  {
    return JJTreeGlobals.parserName + "TreeConstants";
  }

  static void generateTreeConstants()
  {
    String name = nodeConstants();
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), name + ".h");
    headersForJJTreeH.add(file.getName());

    try {
      OutputFile outputFile = new OutputFile(file);
      PrintWriter ostr = outputFile.getPrintWriter();

      List<String> nodeIds = ASTNodeDescriptor.getNodeIds();
      List<String> nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(ostr);
      ostr.println("#ifndef " + file.getName().replace('.', '_').toUpperCase());
      ostr.println("#define " + file.getName().replace('.', '_').toUpperCase());

      ostr.println("\n#include \"JavaCC.h\"");
      boolean hasNamespace = Options.stringValue("NAMESPACE").length() > 0;
      if (hasNamespace) {
        ostr.println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
      }
      ostr.println("  enum {");
      for (int i = 0; i < nodeIds.size(); ++i) {
        String n = nodeIds.get(i);
        ostr.println("  " + n + " = " + i + ",");
      }

      ostr.println("};");
      ostr.println();

      for (int i = 0; i < nodeNames.size(); ++i) {
        ostr.println("  static JAVACC_CHAR_TYPE jjtNodeName_arr_" + i + "[] = ");
        String n = nodeNames.get(i);
        //ostr.println("    (JAVACC_CHAR_TYPE*)\"" + n + "\",");
        OtherFilesGenCPP.printCharArray(ostr, n);
        ostr.println(";");
      }
      ostr.println("  static JAVACC_STRING_TYPE jjtNodeName[] = {");
      for (int i = 0; i < nodeNames.size(); i++) {
        ostr.println("jjtNodeName_arr_" + i + ", ");
      }
      ostr.println("  };");

      if (hasNamespace) {
        ostr.println(Options.stringValue("NAMESPACE_CLOSE"));
      }

      ostr.println("#endif");
      ostr.close();

    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }


  static String visitorClass() {
    return JJTreeGlobals.parserName + "Visitor";
  }

  private static String getVisitMethodName(String className) {
    StringBuffer sb = new StringBuffer("visit");
    if (Options.booleanValue("VISITOR_METHOD_NAME_INCLUDES_TYPE_NAME")) {
      sb.append(Character.toUpperCase(className.charAt(0)));
      for (int i = 1; i < className.length(); i++) {
        sb.append(className.charAt(i));
      }
    }

    return sb.toString();
  }

  private static String getVisitorArgumentType() {
    String ret = Options.stringValue("VISITOR_DATA_TYPE");
    return ret == null || ret.equals("") || ret.equals("Object") ? "void *" : ret;
  }

  private static String getVisitorReturnType() {
    String ret = Options.stringValue("VISITOR_RETURN_TYPE");
    return ret == null || ret.equals("") || ret.equals("Object") ? "void" : ret;
  }

  static void generateVisitors() {
    if (!JJTreeOptions.getVisitor()) {
      return;
    }

    try {
      //String name = visitorClass();
      File file = new File(visitorIncludeFile());
      OutputFile outputFile = new OutputFile(file);
      PrintWriter ostr = outputFile.getPrintWriter();

      generatePrologue(ostr);
      ostr.println("#ifndef " + file.getName().replace('.', '_').toUpperCase().toUpperCase());
      ostr.println("#define " + file.getName().replace('.', '_').toUpperCase().toUpperCase());
      ostr.println("\n#include \"JavaCC.h\"");
      ostr.println("#include \"" + JJTreeGlobals.parserName + "Tree.h" + "\"");

      boolean hasNamespace = Options.stringValue("NAMESPACE").length() > 0;
      if (hasNamespace) {
        ostr.println("namespace " + Options.stringValue("NAMESPACE_OPEN"));
      }

      generateVisitorInterface(ostr);
      generateDefaultVisitor(ostr);

      if (hasNamespace) {
        ostr.println(Options.stringValue("NAMESPACE_CLOSE"));
      }

      ostr.println("#endif");
      ostr.close();
    } catch(IOException ioe) {
      throw new Error(ioe.toString());
    }
  }

  private static void generateVisitorInterface(PrintWriter ostr) {
    String name = visitorClass();
    List<String> nodeNames = ASTNodeDescriptor.getNodeNames();

    ostr.println("class " + name);
    ostr.println("{");

    String argumentType = getVisitorArgumentType();
    String returnType = getVisitorReturnType();
    if (!JJTreeOptions.getVisitorDataType().equals("")) {
      argumentType = JJTreeOptions.getVisitorDataType();
    }

    ostr.println("  public: virtual " + returnType + " visit(const SimpleNode *node, " + argumentType + " data) = 0;");
    if (JJTreeOptions.getMulti()) {
      for (int i = 0; i < nodeNames.size(); ++i) {
        String n = nodeNames.get(i);
        if (n.equals("void")) {
          continue;
        }
        String nodeType = JJTreeOptions.getNodePrefix() + n;
        ostr.println("  public: virtual " + returnType + " " + getVisitMethodName(nodeType) + "(const " + nodeType +
            " *node, " + argumentType + " data) = 0;");
      }
    }

    ostr.println("  public: virtual ~" + name + "() { }");
    ostr.println("};");
  }

  static String defaultVisitorClass() {
    return JJTreeGlobals.parserName + "DefaultVisitor";
  }

  private static void generateDefaultVisitor(PrintWriter ostr) {
    String className = defaultVisitorClass();
    List<String> nodeNames = ASTNodeDescriptor.getNodeNames();

    ostr.println("class " + className + " : public " + visitorClass() + " {");

    String argumentType = getVisitorArgumentType();
    String ret = getVisitorReturnType();

    ostr.println("  public:");
    ostr.println("  virtual " + ret + " defaultVisit(const SimpleNode *node, " + argumentType + " data) = 0;");
    //ostr.println("    node->childrenAccept(this, data);");
    //ostr.println("    return" + (ret.trim().equals("void") ? "" : " data") + ";");
    //ostr.println("  }");

    ostr.println("  virtual " + ret + " visit(const SimpleNode *node, " + argumentType + " data) {");
    ostr.println("    " + (ret.trim().equals("void") ? "" : "return ") + "defaultVisit(node, data);");
    ostr.println("  }");

    if (JJTreeOptions.getMulti()) {
      for (int i = 0; i < nodeNames.size(); ++i) {
        String n = nodeNames.get(i);
        if (n.equals("void")) {
          continue;
        }
        String nodeType = JJTreeOptions.getNodePrefix() + n;
        ostr.println("  virtual " + ret + " " + getVisitMethodName(nodeType) + "(const " + nodeType +
            " *node, " + argumentType + " data) {");
        ostr.println("    " + (ret.trim().equals("void") ? "" : "return ") + "defaultVisit(node, data);");
        ostr.println("  }");
      }
    }
    ostr.println("  public: ~" + className + "() { }");
    ostr.println("};");
  }

  public static void generateFile(OutputFile outputFile, String template, Map<String, Object> options) throws IOException
  {
    generateFile(outputFile, template, options, true);
  }

  public static void generateFile(OutputFile outputFile, String template, Map<String, Object> options, boolean close) throws IOException
  {
    PrintWriter ostr = outputFile.getPrintWriter();
    generatePrologue(ostr);
    OutputFileGenerator generator = new OutputFileGenerator(
        template, options);
    generator.generate(ostr);
    if (close) ostr.close();
  }

  static void generateOutputFiles() throws IOException {
    generateNodeHeader();
    generateTreeInterface();
    generateTreeImpl();
    generateTreeConstants();
    generateVisitors();
  }

}
