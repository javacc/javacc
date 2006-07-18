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

package org.javacc.parser;

public class Main {

  static void help_message() {
    System.out.println("Usage:");
    System.out.println("    javacc option-settings inputfile");
    System.out.println("");
    System.out.println("\"option-settings\" is a sequence of settings separated by spaces.");
    System.out.println("Each option setting must be of one of the following forms:");
    System.out.println("");
    System.out.println("    -optionname=value (e.g., -STATIC=false)");
    System.out.println("    -optionname:value (e.g., -STATIC:false)");
    System.out.println("    -optionname       (equivalent to -optionname=true.  e.g., -STATIC)");
    System.out.println("    -NOoptionname     (equivalent to -optionname=false. e.g., -NOSTATIC)");
    System.out.println("");
    System.out.println("Option settings are not case-sensitive, so one can say \"-nOsTaTiC\" instead");
    System.out.println("of \"-NOSTATIC\".  Option values must be appropriate for the corresponding");
    System.out.println("option, and must be either an integer, a boolean, or a string value.");
    System.out.println("");
    System.out.println("The integer valued options are:");
    System.out.println("");
    System.out.println("    LOOKAHEAD              (default 1)");
    System.out.println("    CHOICE_AMBIGUITY_CHECK (default 2)");
    System.out.println("    OTHER_AMBIGUITY_CHECK  (default 1)");
    System.out.println("");
    System.out.println("The boolean valued options are:");
    System.out.println("");
    System.out.println("    STATIC                 (default true)");
    System.out.println("    DEBUG_PARSER           (default false)");
    System.out.println("    DEBUG_LOOKAHEAD        (default false)");
    System.out.println("    DEBUG_TOKEN_MANAGER    (default false)");
    System.out.println("    ERROR_REPORTING        (default true)");
    System.out.println("    JAVA_UNICODE_ESCAPE    (default false)");
    System.out.println("    UNICODE_INPUT          (default false)");
    System.out.println("    IGNORE_CASE            (default false)");
    System.out.println("    COMMON_TOKEN_ACTION    (default false)");
    System.out.println("    USER_TOKEN_MANAGER     (default false)");
    System.out.println("    USER_CHAR_STREAM       (default false)");
    System.out.println("    BUILD_PARSER           (default true)");
    System.out.println("    BUILD_TOKEN_MANAGER    (default true)");
    System.out.println("    TOKEN_MANAGER_USES_PARSER (default false)");
    System.out.println("    SANITY_CHECK           (default true)");
    System.out.println("    FORCE_LA_CHECK         (default false)");
    System.out.println("    CACHE_TOKENS           (default false)");
    System.out.println("    KEEP_LINE_COLUMN       (default true)");
    System.out.println("");
    System.out.println("The string valued options are:");
    System.out.println("");
    System.out.println("    OUTPUT_DIRECTORY       (default Current Directory)");
    System.out.println("    JDK_VERSION       (default 1.4)");
    System.out.println("");
    System.out.println("EXAMPLE:");
    System.out.println("    javacc -STATIC=false -LOOKAHEAD:2 -debug_parser mygrammar.jj");
    System.out.println("");
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

    // Initialize all static state
    reInitAll();

    JavaCCGlobals.bannerLine("Parser Generator", "");

    JavaCCParser parser = null;
    if (args.length == 0) {
      System.out.println("");
      help_message();
      return 1;
    } else {
      System.out.println("(type \"javacc\" with no arguments for help)");
    }

    if (Options.isOption(args[args.length-1])) {
      System.out.println("Last argument \"" + args[args.length-1] + "\" is not a filename.");
      return 1;
    }
    for (int arg = 0; arg < args.length-1; arg++) {
      if (!Options.isOption(args[arg])) {
        System.out.println("Argument \"" + args[arg] + "\" must be an option setting.");
        return 1;
      }
      Options.setCmdLineOption(args[arg]);
    }

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
      parser = new JavaCCParser(new java.io.FileReader(args[args.length-1]));
    } catch (NullPointerException ne) { // Should never happen
    } catch (SecurityException se) {
      System.out.println("Security violation while trying to open " + args[args.length-1]);
      return 1;
    } catch (java.io.FileNotFoundException e) {
      System.out.println("File " + args[args.length-1] + " not found.");
      return 1;
    }

    try {
      System.out.println("Reading from file " + args[args.length-1] + " . . .");
      JavaCCGlobals.fileName = JavaCCGlobals.origFileName = args[args.length-1];
      JavaCCGlobals.jjtreeGenerated = JavaCCGlobals.isGeneratedBy("JJTree", args[args.length-1]);
      JavaCCGlobals.jjcovGenerated = JavaCCGlobals.isGeneratedBy("JJCov", args[args.length-1]);
      JavaCCGlobals.toolNames = JavaCCGlobals.getToolNames(args[args.length-1]);
      parser.javacc_input();
      JavaCCGlobals.createOutputDir(Options.getOutputDirectory());

      if (Options.getUnicodeInput())
      {
         NfaState.unicodeWarningGiven = true;
         System.out.println("Note: UNICODE_INPUT option is specified. " +
              "Please make sure you create the parser/lexer using a Reader with the correct character encoding.");
      }

      Semanticize.start();
      ParseGen.start();
      LexGen.start();
      OtherFilesGen.start();

      if ((JavaCCErrors.get_error_count() == 0) && (Options.getBuildParser() || Options.getBuildTokenManager())) {
        if (JavaCCErrors.get_warning_count() == 0) {
          System.out.println("Parser generated successfully.");
        } else {
          System.out.println("Parser generated with 0 errors and "
                             + JavaCCErrors.get_warning_count() + " warnings.");
        }
        return 0;
      } else {
        System.out.println("Detected " + JavaCCErrors.get_error_count() + " errors and "
                           + JavaCCErrors.get_warning_count() + " warnings.");
        return (JavaCCErrors.get_error_count()==0)?0:1;
      }
    } catch (MetaParseException e) {
      System.out.println("Detected " + JavaCCErrors.get_error_count() + " errors and "
                         + JavaCCErrors.get_warning_count() + " warnings.");
      return 1;
    } catch (ParseException e) {
      System.out.println(e.toString());
      System.out.println("Detected " + (JavaCCErrors.get_error_count()+1) + " errors and "
                         + JavaCCErrors.get_warning_count() + " warnings.");
      return 1;
    }
  }

   public static void reInitAll()
   {
      org.javacc.parser.Expansion.reInit();
      org.javacc.parser.JavaCCErrors.reInit();
      org.javacc.parser.JavaCCGlobals.reInit();
      Options.init();
      org.javacc.parser.JavaCCParserInternals.reInit();
      org.javacc.parser.RStringLiteral.reInit();
      org.javacc.parser.JavaFiles.reInit();
      org.javacc.parser.LexGen.reInit();
      org.javacc.parser.NfaState.reInit();
      org.javacc.parser.MatchInfo.reInit();
      org.javacc.parser.LookaheadWalk.reInit();
      org.javacc.parser.Semanticize.reInit();
      org.javacc.parser.ParseGen.reInit();
      org.javacc.parser.OtherFilesGen.reInit();
      org.javacc.parser.ParseEngine.reInit();
   }

}
