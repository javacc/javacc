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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javacc.parser.Options;
import org.javacc.parser.OutputFile;
import org.javacc.utils.JavaFileGenerator;

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

      List nodeIds = ASTNodeDescriptor.getNodeIds();
      List nodeNames = ASTNodeDescriptor.getNodeNames();

      generatePrologue(ostr);
      ostr.println("public interface " + name);
      ostr.println("{");

      for (int i = 0; i < nodeIds.size(); ++i) {
        String n = (String)nodeIds.get(i);
        ostr.println("  public int " + n + " = " + i + ";");
      }

      ostr.println();
      ostr.println();

      ostr.println("  public String[] jjtNodeName = {");
      for (int i = 0; i < nodeNames.size(); ++i) {
        String n = (String)nodeNames.get(i);
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

      List nodeNames = ASTNodeDescriptor.getNodeNames();

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
          String n = (String)nodeNames.get(i);
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
    
    Map options = new HashMap(Options.getOptions());
    options.put("PARSER_NAME", JJTreeGlobals.parserName);
    
    JavaFileGenerator generator = new JavaFileGenerator(
        "/templates/Node.template", options);
    
    generator.generate(ostr);

    ostr.close();
  }


  private static void generateSimpleNode_java(OutputFile outputFile) throws IOException
  {
    PrintWriter ostr = outputFile.getPrintWriter();

    generatePrologue(ostr);
    
    Map options = new HashMap(Options.getOptions());
    options.put("PARSER_NAME", JJTreeGlobals.parserName);
    options.put("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(JJTreeOptions.getVisitorReturnType().equals("void")));
    
    JavaFileGenerator generator = new JavaFileGenerator(
        "/templates/SimpleNode.template", options);
    
    generator.generate(ostr);

    ostr.close();
  }


  private static void generateMULTINode_java(OutputFile outputFile, String nodeType) throws IOException
  {
    PrintWriter ostr = outputFile.getPrintWriter();

    generatePrologue(ostr);

    Map options = new HashMap(Options.getOptions());
    options.put("PARSER_NAME", JJTreeGlobals.parserName);
    options.put("NODE_TYPE", nodeType);
    options.put("VISITOR_RETURN_TYPE_VOID", Boolean.valueOf(JJTreeOptions.getVisitorReturnType().equals("void")));
    
    JavaFileGenerator generator = new JavaFileGenerator(
        "/templates/MultiNode.template", options);
    
    generator.generate(ostr);

    ostr.close();
  }

}
