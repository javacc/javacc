
/*
 * Copyright © 2002 Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * California 95054, U.S.A. All rights reserved.  Sun Microsystems, Inc. has
 * intellectual property rights relating to technology embodied in the product
 * that is described in this document. In particular, and without limitation,
 * these intellectual property rights may include one or more of the U.S.
 * patents listed at http://www.sun.com/patents and one or more additional
 * patents or pending patent applications in the U.S. and in other countries.
 * U.S. Government Rights - Commercial software. Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and applicable
 * provisions of the FAR and its supplements.  Use is subject to license terms.
 * Sun,  Sun Microsystems,  the Sun logo and  Java are trademarks or registered
 * trademarks of Sun Microsystems, Inc. in the U.S. and other countries.  This
 * product is covered and controlled by U.S. Export Control laws and may be
 * subject to the export or import laws in other countries.  Nuclear, missile,
 * chemical biological weapons or nuclear maritime end uses or end users,
 * whether direct or indirect, are strictly prohibited.  Export or reexport
 * to countries subject to U.S. embargo or to entities identified on U.S.
 * export exclusion lists, including, but not limited to, the denied persons
 * and specially designated nationals lists is strictly prohibited.
 */


package org.javacc.jjdoc;

import org.javacc.parser.*;

public class JJDocMain {

  static void help_message() {
    java.io.PrintWriter ostr = new java.io.PrintWriter(new java.io.OutputStreamWriter(System.out));

    ostr.println("    jjdoc option-settings - (to read from standard input)");
    ostr.println("OR");
    ostr.println("    jjdoc option-settings inputfile (to read from a file)");
    ostr.println("");
    ostr.println("WHERE");
    ostr.println("    \"option-settings\" is a sequence of settings separated by spaces.");
    ostr.println("");

    ostr.println("Each option setting must be of one of the following forms:");
    ostr.println("");
    ostr.println("    -optionname=value (e.g., -TEXT=false)");
    ostr.println("    -optionname:value (e.g., -TEXT:false)");
    ostr.println("    -optionname       (equivalent to -optionname=true.  e.g., -TEXT)");
    ostr.println("    -NOoptionname     (equivalent to -optionname=false. e.g., -NOTEXT)");
    ostr.println("");
    ostr.println("Option settings are not case-sensitive, so one can say \"-nOtExT\" instead");
    ostr.println("of \"-NOTEXT\".  Option values must be appropriate for the corresponding");
    ostr.println("option, and must be either an integer, boolean or string value.");
    ostr.println("");
    ostr.println("The string valued options are:");
    ostr.println("");
    ostr.println("    OUTPUT_FILE");
    ostr.println("");
    ostr.println("The boolean valued options are:");
    ostr.println("");
    ostr.println("    ONE_TABLE              (default true)");
    ostr.println("    TEXT                   (default false)");
    ostr.println("");

    ostr.println("");
    ostr.println("EXAMPLES:");
    ostr.println("    jjdoc -ONE_TABLE=false mygrammar.jj");
    ostr.println("    jjdoc - < mygrammar.jj");
    ostr.println("");
    ostr.println("ABOUT JJDoc:");
    ostr.println("    JJDoc generates JavaDoc documentation from JavaCC grammar files.");
    ostr.println("");
    ostr.println("    For more information, ???");
  }

  /**
   * A main program that exercises the parser.
   */
  public static void main(String args[]) throws Exception {

    JavaCCGlobals.bannerLine("Documentation Generator", "0.1.4");

    JavaCCParser parser = null;
    if (args.length == 0) {
      System.out.println("");
      help_message();
      System.exit(1);
    } else {
      System.out.println("(type \"jjdoc\" with no arguments for help)");
    }

    JJDocOptions.init();

    if (JJDocOptions.isOption(args[args.length-1])) {
      System.out.println("Last argument \"" + args[args.length-1] + "\" is not a filename or \"-\".  ");
      System.exit(1);
    }
    for (int arg = 0; arg < args.length-1; arg++) {
      if (!JJDocOptions.isOption(args[arg])) {
        System.out.println("Argument \"" + args[arg] + "\" must be an option setting.  ");
        System.exit(1);
      }
      JJDocOptions.setCmdLineOption(args[arg]);
    }

    if (args[args.length-1].equals("-")) {
      System.out.println("Reading from standard input . . .");
      parser = new JavaCCParser(new java.io.DataInputStream(System.in));
      JJDocGlobals.input_file = "standard input";
      JJDocGlobals.output_file = "standard output";
    } else {
      System.out.println("Reading from file " + args[args.length-1] + " . . .");
      try {
        java.io.File fp = new java.io.File(args[args.length-1]);
        if (!fp.exists()) {
           System.out.println("File " + args[args.length-1] + " not found.");
           System.exit(1);
        }
        if (fp.isDirectory()) {
           System.out.println(args[args.length-1] + " is a directory. Please use a valid file name.");
           System.exit(1);
        }
	JJDocGlobals.input_file = fp.getName();
        parser = new JavaCCParser(new java.io.FileReader(args[args.length-1]));
      } catch (NullPointerException ne) { // Should never happen
      } catch (SecurityException se) {
        System.out.println("Security voilation while trying to open " + args[args.length-1]);
        System.exit(1);
      } catch (java.io.FileNotFoundException e) {
        System.out.println("File " + args[args.length-1] + " not found.");
        System.exit(1);
      }
    }
    try {

      parser.javacc_input();
      JJDoc.start();

      if (JavaCCErrors.get_error_count() == 0) {
        if (JavaCCErrors.get_warning_count() == 0) {
          System.out.println("Grammar documentation generated successfully in " + JJDocGlobals.output_file);
        } else {
          System.out.println("Grammar documentation generated with 0 errors and "
                             + JavaCCErrors.get_warning_count() + " warnings.");
        }
        System.exit(0);
      } else {
        System.out.println("Detected " + JavaCCErrors.get_error_count() + " errors and "
                           + JavaCCErrors.get_warning_count() + " warnings.");
        System.exit((JavaCCErrors.get_error_count()==0)?0:1);
      }
    } catch (org.javacc.parser.MetaParseException e) {
      System.out.println(e.toString());
      System.out.println("Detected " + JavaCCErrors.get_error_count() + " errors and "
                         + JavaCCErrors.get_warning_count() + " warnings.");
      System.exit(1);
    } catch (org.javacc.parser.ParseException e) {
      System.out.println(e.toString());
      System.out.println("Detected " + (JavaCCErrors.get_error_count()+1) + " errors and "
                         + JavaCCErrors.get_warning_count() + " warnings.");
      System.exit(1);
    }
  }
}
