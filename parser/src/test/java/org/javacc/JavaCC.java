package org.javacc;

import java.util.ArrayList;
import java.util.List;

public class JavaCC {
	  public static void main(String[] args) throws Exception {
	    try {
	      List<String> arguments = new ArrayList<String>();
	      arguments.add("-CODE_GENERATOR=org.javacc.java.CodeGenerator");
	      arguments.add("-OUTPUT_DIRECTORY=" + System.getProperty("user.dir") + "/src/gen");
	      arguments.add("-JJTREE_OUTPUT_DIRECTORY=" + System.getProperty("user.dir") + "/src/gen");
	      arguments.add("/data/Repository/javacc-8.0.0/tests/java/Interpreter/src/main/jjtree/SPL.jjt");
	      new org.javacc.jjtree.JJTree().main(arguments.toArray(new String[arguments.size()]));
	      arguments.set(arguments.size() - 1, System.getProperty("user.dir") + "/src/gen/SPL.jj");
	      org.javacc.parser.Main.mainProgram(arguments.toArray(new String[arguments.size()]));
	    } catch (Throwable e) {
	      e.printStackTrace();
	    }
	  }
	}