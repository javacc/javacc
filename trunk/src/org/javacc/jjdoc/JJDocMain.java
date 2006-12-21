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
    ostr.println("    CSS");
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
    ostr.println("    For more information, see the online JJDoc documentation at");
    ostr.println("    https://javacc.dev.java.net/doc/JJDoc.html");
    ostr.flush();
  }

  /**
   * A main program that exercises the parser.
   */
  public static void main(String args[]) throws Exception {
    int errorcode = mainProgram(args);
    System.exit(errorcode);
  }

  /**
   * The method to call to exercise the parser from other Java programs.
   * It returns an error code.  See how the main program above uses
   * this method.
   */
  public static int mainProgram(String args[]) throws Exception {

    JavaCCGlobals.bannerLine("Documentation Generator", "0.1.4");

    JavaCCParser parser = null;
    if (args.length == 0) {
      System.out.println("");
      help_message();
      return 1;
    } else {
      System.out.println("(type \"jjdoc\" with no arguments for help)");
    }
    // FIXME If we are running in a jvm which has just used javacc
    // Then there is a problem with static variables. 
    Main.reInitAll();
    JJDocOptions.init();

    if (JJDocOptions.isOption(args[args.length-1])) {
      System.out.println("Last argument \"" + args[args.length-1] + "\" is not a filename or \"-\".  ");
      return 1;
    }
    for (int arg = 0; arg < args.length-1; arg++) {
      if (!JJDocOptions.isOption(args[arg])) {
        System.out.println("Argument \"" + args[arg] + "\" must be an option setting.  ");
        return 1;
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
           return 1;
        }
        if (fp.isDirectory()) {
           System.out.println(args[args.length-1] + " is a directory. Please use a valid file name.");
           return 1;
        }
  JJDocGlobals.input_file = fp.getName();
        parser = new JavaCCParser(new java.io.FileReader(args[args.length-1]));
      } catch (SecurityException se) {
        System.out.println("Security voilation while trying to open " + args[args.length-1]);
        return 1;
      } catch (java.io.FileNotFoundException e) {
        System.out.println("File " + args[args.length-1] + " not found.");
        return 1;
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
        return 0;
      } else {
        System.out.println("Detected " + JavaCCErrors.get_error_count() + " errors and "
                           + JavaCCErrors.get_warning_count() + " warnings.");
        return (JavaCCErrors.get_error_count()==0)?0:1;
      }
    } catch (org.javacc.parser.MetaParseException e) {
      System.out.println(e.toString());
      System.out.println("Detected " + JavaCCErrors.get_error_count() + " errors and "
                         + JavaCCErrors.get_warning_count() + " warnings.");
      return 1;
    } catch (org.javacc.parser.ParseException e) {
      System.out.println(e.toString());
      System.out.println("Detected " + (JavaCCErrors.get_error_count()+1) + " errors and "
                         + JavaCCErrors.get_warning_count() + " warnings.");
      return 1;
    }
  }
}
