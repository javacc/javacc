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

import java.util.*;

/**
 * A class with static state that stores all option information.
 */

public class Options {

  /**
   * A mapping of option names (String's) to values (Integer, Boolean, String).
   * This table is initialized by the main program.  Its contents define the
   * set of legal options, its initial values define the default option values,
   * and the option types can be determined from these values too.
   */
  public static java.util.Hashtable optionValues = new java.util.Hashtable();

  /**
   * The following three functions extract option values in a convenient manner
   * for users.
   */
  public static int I(String s) {
    Integer i = (Integer)optionValues.get(s);
    return i.intValue();
  }

  public static boolean B(String s) {
    Boolean b = (Boolean)optionValues.get(s);
    return b != null && b.booleanValue();
  }

  public static String S(String s) {
    return (String)optionValues.get(s);
  }

  /**
   * These tables store information on whether the option has been explicitly
   * set on the command line or input file respectively.  The table contains
   * String's, their mappings are not important.
   */
  public static java.util.Hashtable cmdLineSetting = new java.util.Hashtable();
  public static java.util.Hashtable inputFileSetting = new java.util.Hashtable();

  /**
   * Initialize for JavaCC
   */
  public static void JavaCCInit() {
    {
      optionValues.put("LOOKAHEAD", new Integer(1));
      optionValues.put("CHOICE_AMBIGUITY_CHECK", new Integer(2));
      optionValues.put("OTHER_AMBIGUITY_CHECK", new Integer(1));
      optionValues.put("STATIC", Boolean.TRUE);
      optionValues.put("DEBUG_PARSER", Boolean.FALSE);
      optionValues.put("DEBUG_LOOKAHEAD", Boolean.FALSE);
      optionValues.put("DEBUG_TOKEN_MANAGER", Boolean.FALSE);
      optionValues.put("OPTIMIZE_TOKEN_MANAGER", Boolean.TRUE);
      optionValues.put("ERROR_REPORTING", Boolean.TRUE);
      optionValues.put("JAVA_UNICODE_ESCAPE", Boolean.FALSE);
      optionValues.put("UNICODE_INPUT", Boolean.FALSE);
      optionValues.put("IGNORE_CASE", Boolean.FALSE);
      optionValues.put("USER_TOKEN_MANAGER", Boolean.FALSE);
      optionValues.put("USER_CHAR_STREAM", Boolean.FALSE);
      optionValues.put("BUILD_PARSER", Boolean.TRUE);
      optionValues.put("BUILD_TOKEN_MANAGER", Boolean.TRUE);
      optionValues.put("SANITY_CHECK", Boolean.TRUE);
      optionValues.put("FORCE_LA_CHECK", Boolean.FALSE);
      optionValues.put("COMMON_TOKEN_ACTION", Boolean.FALSE);
      optionValues.put("CACHE_TOKENS", Boolean.FALSE);
      optionValues.put("KEEP_LINE_COLUMN", Boolean.TRUE);
      optionValues.put("OUTPUT_DIRECTORY", ".");
    }
  }

  public static void setInputFileOption(Object nameloc, Object valueloc, String name, int value) {
    String s = name.toUpperCase();
    Object Val = optionValues.get(s);
    if (Val == null) {
      JavaCCErrors.warning(nameloc, "Bad option name \"" + name + "\".  Option setting will be ignored.");
      return;
    }
    if (!(Val instanceof Integer) || value <= 0) {
      JavaCCErrors.warning(valueloc, "Bad option value \"" + value + "\" for \"" + name + "\".  Option setting will be ignored.");
      return;
    }
    if (inputFileSetting.get(s) != null) {
      JavaCCErrors.warning(nameloc, "Duplicate option setting for \"" + name + "\" will be ignored.");
      return;
    }
    if (cmdLineSetting.get(s) != null) {
      if (((Integer)Val).intValue() != value) {
        JavaCCErrors.warning(nameloc, "Command line setting of \"" + name + "\" modifies option value in file.");
      }
      return;
    }
    optionValues.put(s, new Integer(value));
    inputFileSetting.put(s, "");
  }

  public static void setInputFileOption(Object nameloc, Object valueloc, String name, boolean value) {
    String s = name.toUpperCase();
    Object Val = optionValues.get(s);
    if (Val == null) {
      JavaCCErrors.warning(nameloc, "Bad option name \"" + name + "\".  Option setting will be ignored.");
      return;
    }
    if (!(Val instanceof Boolean)) {
      JavaCCErrors.warning(valueloc, "Bad option value \"" + value + "\" for \"" + name + "\".  Option setting will be ignored.");
      return;
    }
    if (inputFileSetting.get(s) != null) {
      JavaCCErrors.warning(nameloc, "Duplicate option setting for \"" + name + "\" will be ignored.");
      return;
    }
    if (cmdLineSetting.get(s) != null) {
      if (((Boolean)Val).booleanValue() != value) {
        JavaCCErrors.warning(nameloc, "Command line setting of \"" + name + "\" modifies option value in file.");
      }
      return;
    }
    optionValues.put(s, (value ? Boolean.TRUE : Boolean.FALSE ));
    inputFileSetting.put(s, "");
  }

  public static void setInputFileOption(Object nameloc, Object valueloc, String name, String value) {
    String s = name.toUpperCase();
    Object Val = optionValues.get(s);
    if (Val == null) {
      JavaCCErrors.warning(nameloc, "Bad option name \"" + name + "\".  Option setting will be ignored.");
      return;
    }
    if (!(Val instanceof String)) {
      JavaCCErrors.warning(valueloc, "Bad option value \"" + value + "\" for \"" + name + "\".  Option setting will be ignored.");
      return;
    }
    if (inputFileSetting.get(s) != null) {
      JavaCCErrors.warning(nameloc, "Duplicate option setting for \"" + name + "\" will be ignored.");
      return;
    }
    if (cmdLineSetting.get(s) != null) {
      if (!((String)Val).equals(value)) {
        JavaCCErrors.warning(nameloc, "Command line setting of \"" + name + "\" modifies option value in file.");
      }
      return;
    }
    if (name.equals("OUTPUT_DIRECTORY")) {
       JavaCCGlobals.storeOutputDirSpec(valueloc, (String)value);
    }
    optionValues.put(s, value);
    inputFileSetting.put(s, "");
  }

  public static void setCmdLineOption(String arg) {
    String s = arg.toUpperCase();
    int index = 0;
    String name;
    Object Val;
    while (index < s.length() && s.charAt(index) != '=' && s.charAt(index) != ':') {
      index++;
    }
    if (index < 2 || index >= s.length()-1) {
      if (index == s.length()) {
        if (s.length() > 3 && s.charAt(1) == 'N' && s.charAt(2) == 'O') {
          name = s.substring(3);
          Val = Boolean.FALSE;
        } else {
          name = s.substring(1);
          Val = Boolean.TRUE;
        }
      } else {
        System.out.println("Warning: Bad option \"" + arg + "\" will be ignored.");
        return;
      }
    } else {
      if (s.substring(index+1).equals("TRUE")) {
        Val = Boolean.TRUE;
      } else if (s.substring(index+1).equals("FALSE")) {
        Val = Boolean.FALSE;
      } else {
        try {
          int i = Integer.parseInt(s.substring(index+1));
          if (i <= 0) {
            System.out.println("Warning: Bad option value in \"" + arg + "\" will be ignored.");
            return;
          }
          Val = new Integer(i);
        } catch (NumberFormatException e) {
          Val = arg.substring(index+1);
          if (arg.length() > index + 2) { // i.e., there is space for two '"'s in value
            if (arg.charAt(index+1) == '"' && arg.charAt(arg.length()-1) == '"') {
              Val = arg.substring(index+2, arg.length()-1); // remove the two '"'s.
            }
          }
        }
      }
      name = s.substring(1, index);
    }
    Object valOrig = optionValues.get(name);
    if (valOrig == null) {
      System.out.println("Warning: Bad option \"" + arg + "\" will be ignored.");
      return;
    }
    if (Val.getClass() != valOrig.getClass()) {
      System.out.println("Warning: Bad option value in \"" + arg + "\" will be ignored.");
      return;
    }
    if (cmdLineSetting.get(name) != null) {
      System.out.println("Warning: Duplicate option setting \"" + arg + "\" will be ignored.");
      return;
    }
    if (name.equals("OUTPUT_DIRECTORY")) {
       JavaCCGlobals.storeOutputDirSpec(null, (String)Val);
    }
    optionValues.put(name, Val);
    cmdLineSetting.put(name, "");
  }

  public static void normalize() {
    if (B("DEBUG_LOOKAHEAD") && !B("DEBUG_PARSER")) {
      if (cmdLineSetting.get("DEBUG_PARSER") != null || inputFileSetting.get("DEBUG_PARSER") != null) {
        JavaCCErrors.warning("True setting of option DEBUG_LOOKAHEAD overrides false setting of option DEBUG_PARSER.");
      }
      optionValues.put("DEBUG_PARSER", Boolean.TRUE);
    }
    if (B("DEBUG_TOKEN_MANAGER") && B("OPTIMIZE_TOKEN_MANAGER")) {
      if (cmdLineSetting.get("OPTIMIZE_TOKEN_MANAGER") != null ||
          inputFileSetting.get("OPTIMIZE_TOKEN_MANAGER") != null) {
        JavaCCErrors.warning("True setting of option DEBUG_TOKEN_MANAGER forces the setting of option OPTIMIZE_TOKEN_MANAGER to be false.");
      }
      optionValues.put("OPTIMIZE_TOKEN_MANAGER", Boolean.FALSE);
    }
  }

   public static void reInit()
   {
      optionValues = new java.util.Hashtable();
      cmdLineSetting = new java.util.Hashtable();
      inputFileSetting = new java.util.Hashtable();
   }

}
