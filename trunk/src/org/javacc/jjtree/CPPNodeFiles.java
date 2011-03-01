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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javacc.parser.Options;
import org.javacc.parser.OutputFile;
import org.javacc.utils.JavaFileGenerator;

final class CPPNodeFiles {
  private CPPNodeFiles() {}

  private static List headersForJJTreeH = new ArrayList();
  /**
   * ID of the latest version (of JJTree) in which one of the Node classes
   * was modified.
   */
  static final String nodeVersion = "6.0";

  static Set nodesGenerated = new HashSet();

  static void ensure(IO io, String nodeType)
  {
    String filePrefix = new File(JJTreeOptions.getJJTreeOutputDirectory(), nodeType).getAbsolutePath();
    File file = new File(filePrefix + (nodeType.equals("Node") ? ".h" : ".cc"));

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
      String[] options = new String[] {"MULTI", "NODE_USES_PARSER", "VISITOR", "TRACK_TOKENS", "NODE_PREFIX", "NODE_EXTENDS", "NODE_FACTORY", "SUPPORT_CLASS_VISIBILITY_PUBLIC"};
      OutputFile outputFile = new OutputFile(file, nodeVersion, options);
      outputFile.setToolName("JJTree");

      nodesGenerated.add(file.getName());

      if (!outputFile.needToWrite) {
        return;
      }

      Map optionMap = new HashMap(Options.getOptions());
      optionMap.put("PARSER_NAME", JJTreeGlobals.parserName);
      optionMap.put("NODE_TYPE", nodeType);
      optionMap.put("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(JJTreeOptions.getVisitorReturnType().equals("void")));
      if (nodeType.equals("Node")) {
        generateFile(outputFile, "/templates/cpp/Node.h.template", optionMap);
      } else if (nodeType.equals("SimpleNode")) {
        generateFile(outputFile, "/templates/cpp/SimpleNode.cc.template", optionMap);
        generateFile(new OutputFile(new File(filePrefix + ".h"), nodeVersion, options), "/templates/cpp/SimpleNode.h.template", optionMap);
        //headersForJJTreeH.add("SimpleNode.h");
      } else {
        generateFile(outputFile, "/templates/cpp/MultiNode.cc.template", optionMap);
        generateFile(new OutputFile(new File(filePrefix + ".h"), nodeVersion, options), "/templates/cpp/MultiNode.h.template", optionMap);
        headersForJJTreeH.add(nodeType + ".h");
      }

      outputFile.close();

    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }


  static void generatePrologue(PrintWriter ostr)
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

      List nodeIds = ASTNodeDescriptor.getNodeIds();
      List nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(ostr);
      ostr.println("#ifndef " + file.getName().replace('.', '_'));
      ostr.println("#define " + file.getName().replace('.', '_'));

      ostr.println("  enum {");
      for (int i = 0; i < nodeIds.size(); ++i) {
        String n = (String)nodeIds.get(i);
        ostr.println("  " + n + " = " + i + ",");
      }

      ostr.println("};");
      ostr.println();

      ostr.println("  static String jjtNodeName[] = {");
      for (int i = 0; i < nodeNames.size(); ++i) {
        String n = (String)nodeNames.get(i);
        ostr.println("    \"" + n + "\",");
      }
      ostr.println("  };");

      ostr.println("#endif");
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
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), name + ".h");

    try {
      OutputFile outputFile = new OutputFile(file);
      PrintWriter ostr = outputFile.getPrintWriter();

      List nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(ostr);
      ostr.println("#ifndef " + file.getName().replace('.', '_').toUpperCase());
      ostr.println("#define " + file.getName().replace('.', '_').toUpperCase());

      ostr.println("typedef class _" + name + " *name;");
      ostr.println("class _" + name);
      ostr.println("{");

      String ve = mergeVisitorException();

      String argumentType = "Object";
      if (!JJTreeOptions.getVisitorDataType().equals("")) {
        argumentType = JJTreeOptions.getVisitorDataType();
      }

      ostr.println("  public: virtual " + JJTreeOptions.getVisitorReturnType() + " visit(SimpleNode node, " + argumentType + " data)" + ve + " = 0;");
      if (JJTreeOptions.getMulti()) {
        for (int i = 0; i < nodeNames.size(); ++i) {
          String n = (String)nodeNames.get(i);
          if (n.equals("void")) {
            continue;
          }
          String nodeType = JJTreeOptions.getNodePrefix() + n;
          ostr.println("  public: virtual " + JJTreeOptions.getVisitorReturnType() + " visit(" + nodeType +
              " node, " + argumentType + " data)" + ve + " = 0;");
        }
      }

      ostr.println(" public: virtual ~_" + name + "() { }");
      ostr.println("};");
      ostr.println("#endif");
      ostr.close();

    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }

  static String defaultVisitorClass()
  {
    return JJTreeGlobals.parserName + "DefaultVisitor";
  }

  static void generateDefaultVisitor() {
    generateDefaultVisitorHeader();
    generateDefaultVisitorImpl();
  }

  static void generateDefaultVisitorHeader()
  {
    if (!JJTreeOptions.getVisitor()) {
      return;
    }

    String className = defaultVisitorClass();
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), className + ".h");

    try {
      OutputFile outputFile = new OutputFile(file);
      PrintWriter ostr = outputFile.getPrintWriter();

      List nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(ostr);
      ostr.println("#ifndef " + file.getName().replace('.', '_').toUpperCase());
      ostr.println("#define " + file.getName().replace('.', '_').toUpperCase());

      ostr.println("#include \"JJTree.h\"");
      ostr.println("#include \"SimpleNode.h\"");
      ostr.println("#include \"" + new File(visitorClass() + ".h").getName() + "\"");
      ostr.println("class _" + className + " : public _" + visitorClass() + "{");

      String ve = mergeVisitorException();

      String argumentType = "Object";
      if (!JJTreeOptions.getVisitorDataType().equals("")) {
        argumentType = JJTreeOptions.getVisitorDataType();
      }

      String ret = JJTreeOptions.getVisitorReturnType();
      ostr.println("  " + ret + " defaultVisit(SimpleNode node, " + argumentType + " data)" +
          ve + ";");

      ostr.println("  " + ret + " visit(SimpleNode node, " + argumentType + " data)" +
          ve + ";");

      if (JJTreeOptions.getMulti()) {
        for (int i = 0; i < nodeNames.size(); ++i) {
          String n = (String)nodeNames.get(i);
          if (n.equals("void")) {
            continue;
          }
          String nodeType = JJTreeOptions.getNodePrefix() + n;
          ostr.println("  " + JJTreeOptions.getVisitorReturnType() + " visit(" + nodeType +
              " node, " + argumentType + " data)" + ve + ";");
        }
      }

      ostr.println("public: virtual ~_" + className + "();");
      ostr.println("};");
      ostr.println("#endif");
      ostr.close();

    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }

  static void generateDefaultVisitorImpl()
  {
    if (!JJTreeOptions.getVisitor()) {
      return;
    }

    String className = defaultVisitorClass();
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), className + ".cc");

    try {
      OutputFile outputFile = new OutputFile(file);
      PrintWriter ostr = outputFile.getPrintWriter();

      List nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(ostr);

      ostr.println("#include \"" + new File(defaultVisitorClass() + ".h").getName() + "\"");
      String ve = mergeVisitorException();

      String argumentType = "Object";
      if (!JJTreeOptions.getVisitorDataType().equals("")) {
        argumentType = JJTreeOptions.getVisitorDataType();
      }

      String ret = JJTreeOptions.getVisitorReturnType();
      ostr.println("  " + ret + " _" + className  + "::defaultVisit(SimpleNode node, " + argumentType + " data)" +
          ve + "{");
      ostr.println("    node->childrenAccept(this, data);");
      ostr.println("    return" + (ret.trim().equals("void") ? "" : " data") + ";");
      ostr.println("  }");

      ostr.println("  " + ret + " _" + className  + "::visit(SimpleNode node, " + argumentType + " data)" +
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
          ostr.println("  " + JJTreeOptions.getVisitorReturnType() + " _" + className + "::visit(" + nodeType +
              " node, " + argumentType + " data)" + ve + "{");
          ostr.println("    " + (ret.trim().equals("void") ? "" : "return ") + "defaultVisit(node, data);");
          ostr.println("  }");
        }
      }
      ostr.println("_" + className + "::~_" + className + "() { }");
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

  public static void generateFile(OutputFile outputFile, String template, Map options) throws IOException
  {
    PrintWriter ostr = outputFile.getPrintWriter();
    generatePrologue(ostr);
    JavaFileGenerator generator = new JavaFileGenerator(
        template, options);
    generator.generate(ostr);
    ostr.close();
  }

  public static void generateJJTreeH() {
    File file = new File(JJTreeOptions.getJJTreeOutputDirectory(), "JJTree.h");

    try {
      OutputFile outputFile = new OutputFile(file);
      PrintWriter ostr = outputFile.getPrintWriter();

      generatePrologue(ostr);
      ostr.println("#ifndef " + file.getName().replace('.', '_'));
      ostr.println("#define " + file.getName().replace('.', '_'));

      for (int i = 0; i < headersForJJTreeH.size(); i++) {
        ostr.println("#include \"" + headersForJJTreeH.get(i) + "\"");
      }

      ostr.println("#include \"JJT" + JJTreeGlobals.parserName + "State.h\"");
      ostr.println("#endif");
      ostr.close();
    } catch (IOException e) {
      throw new Error(e.toString());
    }
  }
}
