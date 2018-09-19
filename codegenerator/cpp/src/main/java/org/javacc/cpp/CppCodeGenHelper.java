/*
 * Copyright (c) 2001-2018 Territorium Online Srl / TOL GmbH. All Rights Reserved.
 *
 * This file contains Original Code and/or Modifications of Original Code as defined in and that are
 * subject to the Territorium Online License Version 1.0. You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at http://www.tol.info/license/
 * and read it before using this file.
 *
 * The Original Code and all software distributed under the License are distributed on an 'AS IS'
 * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TERRITORIUM ONLINE HEREBY
 * DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT. Please see the License for
 * the specific language governing rights and limitations under the License.
 */

package org.javacc.cpp;

import static org.javacc.parser.JavaCCGlobals.cu_name;
import static org.javacc.parser.JavaCCGlobals.jjtreeGenerated;

import org.javacc.parser.CodeGenHelper;
import org.javacc.parser.JavaCCErrors;
import org.javacc.parser.Options;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * The {@link CppCodeGenHelper} class.
 */
public class CppCodeGenHelper extends CodeGenHelper {

  /**
   * Generate a class with a given name, an array of superclass and another array of super interfaes
   */
  public void genClassStart(String mod, String name, String[] superClasses, String[] superInterfaces) {
    genCode("class " + name);
    if (superClasses.length > 0 || superInterfaces.length > 0) {
      genCode(" : ");
    }

    genCommaSeperatedString(superClasses);
    genCommaSeperatedString(superInterfaces);
    genCodeLine(" {");
    genCodeLine("public:");
  }

  /**
   * Generate a modifier
   */
  public void genModifier(String mod) {
    String origMod = mod.toLowerCase();
    if (origMod.equals("public") || origMod.equals("private")) {
      genCode(origMod + ": ");
    }
  }

  public void saveOutput(String fileName, StringBuffer sb) {

    fixupLongLiterals(sb);
    File tmp = new File(fileName);
    try (PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter(tmp), 8092))) {
      fw.print(sb.toString());
    } catch (IOException ioe) {
      JavaCCErrors.fatal("Could not create output file: " + fileName);
    }
  }

  public void saveOutput(String fileName) {
    String incfilePath = fileName.replace(".cc", ".h");
    String incfileName = new File(incfilePath).getName();
    includeBuffer.insert(0, "#pragma once\n");
      // includeBuffer.insert(0, "#define " + new File(incfileName).getName().replace('.',
      // '_').toUpperCase() + "\n");
      // includeBuffer.insert(0, "#ifndef " + new File(incfileName).getName().replace('.',
      // '_').toUpperCase() + "\n");


    // dump the statics into the main file with the code.
    mainBuffer.insert(0, staticsBuffer);

    // Finally enclose the whole thing in the namespace, if specified.
    if (Options.stringValue(Options.USEROPTION__NAMESPACE).length() > 0) {
      mainBuffer.insert(0, "namespace " + Options.stringValue("NAMESPACE_OPEN") + "\n");
      mainBuffer.append(Options.stringValue("NAMESPACE_CLOSE") + "\n");
      includeBuffer.append(Options.stringValue("NAMESPACE_CLOSE") + "\n");
    }

    if (jjtreeGenerated) {
      mainBuffer.insert(0, "#include \"" + cu_name + "Tree.h\"\n");
    }
    if (Options.getTokenManagerUsesParser())
      mainBuffer.insert(0, "#include \"" + cu_name + ".h\"\n");
    mainBuffer.insert(0, "#include \"TokenMgrError.h\"\n");
    mainBuffer.insert(0, "#include \"" + incfileName + "\"\n");
    // includeBuffer.append("#endif\n");
    saveOutput(incfilePath, includeBuffer);

    mainBuffer.insert(0, "/* " + new File(fileName).getName() + " */\n");
    saveOutput(fileName, mainBuffer);
  }

  public void generateMethodDefHeader(String qualifiedModsAndRetType, String className, String nameAndParams,
      String exceptions) {
    // for C++, we generate the signature in the header file and body in main file
    includeBuffer.append(qualifiedModsAndRetType + " " + nameAndParams);
    // if (exceptions != null)
    // includeBuffer.append(" throw(" + exceptions + ")");
    includeBuffer.append(";\n");

    String modsAndRetType = null;
    int i = qualifiedModsAndRetType.lastIndexOf(':');
    if (i >= 0)
      modsAndRetType = qualifiedModsAndRetType.substring(i + 1);

    if (modsAndRetType != null) {
      i = modsAndRetType.lastIndexOf("virtual");
      if (i >= 0)
        modsAndRetType = modsAndRetType.substring(i + "virtual".length());
    }
  if (qualifiedModsAndRetType != null) {
    i = qualifiedModsAndRetType.lastIndexOf("virtual");
      if (i >= 0)
        qualifiedModsAndRetType = qualifiedModsAndRetType.substring(i + "virtual".length());
    }
    mainBuffer.append("\n" + qualifiedModsAndRetType + " " + getClassQualifier(className) + nameAndParams);
    // if (exceptions != null)
    // mainBuffer.append(" throw( " + exceptions + ")");
    switchToMainFile();
  }

  /**
   * Generate annotation. @XX syntax for java, comments in C++
   */
  public void genAnnotation(String ann) {
    genCode( "/*" + ann + "*/");
  }

  public void switchToStaticsFile() {
    outputBuffer = staticsBuffer;
  }

  public void switchToIncludeFile() {
    outputBuffer = includeBuffer;
  }

  public void genStringLiteralArrayCPP(String varName, String[] arr) {
    // First generate char array vars
    for (int i = 0; i < arr.length; i++) {
      genCodeLine("static const JJChar " + varName + "_arr_" + i + "[] = ");
      genStringLiteral(arr[i]);
      genCodeLine(";");
    }

    genCodeLine("static const JJString " + varName + "[] = {");
    for (int i = 0; i < arr.length; i++) {
      genCodeLine(varName + "_arr_" + i + ", ");
    }
    genCodeLine("};");
  }

  private void genStringLiteral(String s) {
    // String literals in CPP become char arrays
    outputBuffer.append("{");
    for (int i = 0; i < s.length(); i++) {
      outputBuffer.append("0x" + Integer.toHexString(s.charAt(i)) + ", ");
    }
    outputBuffer.append("0}");
  }
  


  // HACK
  private void fixupLongLiterals(StringBuffer sb) {
    for (int i = 0; i < sb.length() - 1; i++) {
      // int beg = i;
      char c1 = sb.charAt(i);
      char c2 = sb.charAt(i + 1);
      if (Character.isDigit(c1) || (c1 == '0' && c2 == 'x')) {
        i += c1 == '0' ? 2 : 1;
        while (isHexDigit(sb.charAt(i))) i++;
        if (sb.charAt(i) == 'L') {
          sb.insert(i, "UL");
        }
        i++;
      }
    }
  }
}
