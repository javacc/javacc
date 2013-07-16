// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

package org.javacc.jjtree;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.javacc.parser.Options;
import org.javacc.parser.OutputFile;

/**
 * Generate the State of a tree.
 */
final class CPPJJTreeState
{

  static final String JJTStateVersion = "6.1";

  private CPPJJTreeState() {}

  static void generateTreeState() throws IOException
  {
    Map options = JJTreeOptions.getOptions();
    options.put("PARSER_NAME", JJTreeGlobals.parserName);
    String filePrefix = new File(JJTreeOptions.getJJTreeOutputDirectory(), "JJT" + JJTreeGlobals.parserName + "State").getAbsolutePath();

    OutputFile outputFile = new OutputFile(new File(filePrefix + ".h"), JJTStateVersion, new String[0]);
    CPPNodeFiles.generateFile(outputFile, "/templates/cpp/JJTTreeState.h.template", options);

    outputFile = new OutputFile(new File(filePrefix + ".cc"), JJTStateVersion, new String[0]);
    CPPNodeFiles.generateFile(outputFile, "/templates/cpp/JJTTreeState.cc.template", options);

  }

}

/*end*/
