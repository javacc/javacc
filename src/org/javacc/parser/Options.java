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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A class with static state that stores all option information.
 */
public class Options {

    /**
     * Limit subclassing to derived classes.
     */
    protected Options() {
    }

    /**
     * A mapping of option names (Strings) to values (Integer, Boolean, String).
     * This table is initialized by the main program. Its contents defines the
     * set of legal options. Its initial values define the default option
     * values, and the option types can be determined from these values too.
     */
    protected static Map optionValues = null;

    /**
     * Convenience method to retrieve integer options.
     */
    protected static int intValue(final String option) {
        return ((Integer) optionValues.get(option)).intValue();
    }

    /**
     * Convenience method to retrieve boolean options.
     */
    protected static boolean booleanValue(final String option) {
        return ((Boolean) optionValues.get(option)).booleanValue();
    }

    /**
     * Convenience method to retrieve string options.
     */
    protected static String stringValue(final String option) {
        return (String) optionValues.get(option);
    }

    /**
     * Keep track of what options were set as a command line argument. We use
     * this to see if the options set from the command line and the ones set in
     * the input files clash in any way.
     */
    private static Set cmdLineSetting = null;

    /**
     * Keep track of what options were set from the grammar file. We use this to
     * see if the options set from the command line and the ones set in the
     * input files clash in any way.
     */
    private static Set inputFileSetting = null;

    /**
     * Initialize for JavaCC
     */
    public static void init() {
        optionValues = new HashMap();
        cmdLineSetting = new HashSet();
        inputFileSetting = new HashSet();

        optionValues.put("LOOKAHEAD", new Integer(1));
        optionValues.put("CHOICE_AMBIGUITY_CHECK", new Integer(2));
        optionValues.put("OTHER_AMBIGUITY_CHECK", new Integer(1));

        optionValues.put("STATIC", Boolean.TRUE);
        optionValues.put("DEBUG_PARSER", Boolean.FALSE);
        optionValues.put("DEBUG_LOOKAHEAD", Boolean.FALSE);
        optionValues.put("DEBUG_TOKEN_MANAGER", Boolean.FALSE);
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

    /**
     * Determine if a given command line argument might be an option flag.
     * Command line options start with a dash&nbsp;(-).
     * 
     * @param opt
     *            The command line argument to examine.
     * @return True when the argument looks like an option flag.
     */
    public static boolean isOption(final String opt) {
        return opt != null && opt.length() > 1 && opt.charAt(0) == '-';
    }

    public static void setInputFileOption(Object nameloc, Object valueloc,
            String name, int value) {
        String s = name.toUpperCase();
        if (!optionValues.containsKey(s)) {
            JavaCCErrors.warning(nameloc, "Bad option name \"" + name
                    + "\".  Option setting will be ignored.");
            return;
        }
        Object Val = optionValues.get(s);
        if (Val != null) {
            if (!(Val instanceof Integer) || value <= 0) {
                JavaCCErrors.warning(valueloc, "Bad option value \"" + value
                        + "\" for \"" + name
                        + "\".  Option setting will be ignored.");
                return;
            }
            if (inputFileSetting.contains(s)) {
                JavaCCErrors.warning(nameloc, "Duplicate option setting for \""
                        + name + "\" will be ignored.");
                return;
            }
            if (cmdLineSetting.contains(s)) {
                if (((Integer) Val).intValue() != value) {
                    JavaCCErrors.warning(nameloc, "Command line setting of \""
                            + name + "\" modifies option value in file.");
                }
                return;
            }
        }

        optionValues.put(s, new Integer(value));
        inputFileSetting.add(s);
    }

    public static void setInputFileOption(Object nameloc, Object valueloc,
            String name, boolean value) {
        String s = name.toUpperCase();
        if (!optionValues.containsKey(s)) {
            JavaCCErrors.warning(nameloc, "Bad option name \"" + name
                    + "\".  Option setting will be ignored.");
            return;
        }
        Object Val = optionValues.get(s);
        if (Val != null) {
            if (!(Val instanceof Boolean)) {
                JavaCCErrors.warning(valueloc, "Bad option value \"" + value
                        + "\" for \"" + name
                        + "\".  Option setting will be ignored.");
                return;
            }
            if (inputFileSetting.contains(s)) {
                JavaCCErrors.warning(nameloc, "Duplicate option setting for \""
                        + name + "\" will be ignored.");
                return;
            }
            if (cmdLineSetting.contains(s)) {
                if (((Boolean) Val).booleanValue() != value) {
                    JavaCCErrors.warning(nameloc, "Command line setting of \""
                            + name + "\" modifies option value in file.");
                }
                return;
            }
        }

        optionValues.put(s, (value ? Boolean.TRUE : Boolean.FALSE));
        inputFileSetting.add(s);
    }

    public static void setInputFileOption(Object nameloc, Object valueloc,
            String name, String value) {
        String s = name.toUpperCase();
        if (!optionValues.containsKey(s)) {
            JavaCCErrors.warning(nameloc, "Bad option name \"" + name
                    + "\".  Option setting will be ignored.");
            return;
        }
        Object Val = optionValues.get(s);
        if (Val != null) {
            if (!(Val instanceof String)) {
                JavaCCErrors.warning(valueloc, "Bad option value \"" + value
                        + "\" for \"" + name
                        + "\".  Option setting will be ignored.");
                return;
            }
            if (inputFileSetting.contains(s)) {
                JavaCCErrors.warning(nameloc, "Duplicate option setting for \""
                        + name + "\" will be ignored.");
                return;
            }
            if (cmdLineSetting.contains(s)) {
                if (!Val.equals(value)) {
                    JavaCCErrors.warning(nameloc, "Command line setting of \""
                            + name + "\" modifies option value in file.");
                }
                return;
            }
        }

        optionValues.put(s, value);
        inputFileSetting.add(s);
    }

    /**
     * 
     * @param arg
     */
    public static void setCmdLineOption(String arg) {
        String s = arg.toUpperCase();
        int index = 0;
        String name;
        Object Val;
        while (index < s.length() && s.charAt(index) != '='
                && s.charAt(index) != ':') {
            index++;
        }
        if (index < 2 || index >= s.length() - 1) {
            if (index == s.length()) {
                if (s.length() > 3 && s.charAt(1) == 'N' && s.charAt(2) == 'O') {
                    name = s.substring(3);
                    Val = Boolean.FALSE;
                } else {
                    name = s.substring(1);
                    Val = Boolean.TRUE;
                }
            } else {
                System.out.println("Warning: Bad option \"" + arg
                        + "\" will be ignored.");
                return;
            }
        } else {
            if (s.substring(index + 1).equals("TRUE")) {
                Val = Boolean.TRUE;
            } else if (s.substring(index + 1).equals("FALSE")) {
                Val = Boolean.FALSE;
            } else {
                try {
                    int i = Integer.parseInt(s.substring(index + 1));
                    if (i <= 0) {
                        System.out.println("Warning: Bad option value in \""
                                + arg + "\" will be ignored.");
                        return;
                    }
                    Val = new Integer(i);
                } catch (NumberFormatException e) {
                    Val = arg.substring(index + 1);
                    if (arg.length() > index + 2) {
                        // i.e., there is space for two '"'s in value
                        if (arg.charAt(index + 1) == '"'
                                && arg.charAt(arg.length() - 1) == '"') {
                            // remove the two '"'s.
                            Val = arg.substring(index + 2, arg.length() - 1);
                        }
                    }
                }
            }
            name = s.substring(1, index);
        }
        if (!optionValues.containsKey(name)) {
            System.out.println("Warning: Bad option \"" + arg
                    + "\" will be ignored.");
            return;
        }
        Object valOrig = optionValues.get(name);
        if (Val.getClass() != valOrig.getClass()) {
            System.out.println("Warning: Bad option value in \"" + arg
                    + "\" will be ignored.");
            return;
        }
        if (cmdLineSetting.contains(name)) {
            System.out.println("Warning: Duplicate option setting \"" + arg
                    + "\" will be ignored.");
            return;
        }

        optionValues.put(name, Val);
        cmdLineSetting.add(name);
    }

    public static void normalize() {
        if (getDebugLookahead() && !getDebugParser()) {
            if (cmdLineSetting.contains("DEBUG_PARSER")
                    || inputFileSetting.contains("DEBUG_PARSER")) {
                JavaCCErrors
                        .warning("True setting of option DEBUG_LOOKAHEAD overrides false setting of option DEBUG_PARSER.");
            }
            optionValues.put("DEBUG_PARSER", Boolean.TRUE);
        }
    }

    /**
     * Find the lookahead setting.
     * 
     * @return The requested lookahead value.
     */
    public static int getLookahead() {
        return intValue("LOOKAHEAD");
    }

    /**
     * Find the choice ambiguity check value.
     * 
     * @return The requested choice ambiguity check value.
     */
    public static int getChoiceAmbiguityCheck() {
        return intValue("CHOICE_AMBIGUITY_CHECK");
    }

    /**
     * Find the other ambiguity check value.
     * 
     * @return The requested other ambiguity check value.
     */
    public static int getOtherAmbiguityCheck() {
        return intValue("OTHER_AMBIGUITY_CHECK");
    }

    /**
     * Find the static value.
     * 
     * @return The requested static value.
     */
    public static boolean getStatic() {
        return booleanValue("STATIC");
    }

    /**
     * Find the debug parser value.
     * 
     * @return The requested debug parser value.
     */
    public static boolean getDebugParser() {
        return booleanValue("DEBUG_PARSER");
    }

    /**
     * Find the debug lookahead value.
     * 
     * @return The requested debug lookahead value.
     */
    public static boolean getDebugLookahead() {
        return booleanValue("DEBUG_LOOKAHEAD");
    }

    /**
     * Find the debug tokenmanager value.
     * 
     * @return The requested debug tokenmanager value.
     */
    public static boolean getDebugTokenManager() {
        return booleanValue("DEBUG_TOKEN_MANAGER");
    }

    /**
     * Find the error reporting value.
     * 
     * @return The requested error reporting value.
     */
    public static boolean getErrorReporting() {
        return booleanValue("ERROR_REPORTING");
    }

    /**
     * Find the Java unicode escape value.
     * 
     * @return The requested Java unicode escape value.
     */
    public static boolean getJavaUnicodeEscape() {
        return booleanValue("JAVA_UNICODE_ESCAPE");
    }

    /**
     * Find the unicode input value.
     * 
     * @return The requested unicode input value.
     */
    public static boolean getUnicodeInput() {
        return booleanValue("UNICODE_INPUT");
    }

    /**
     * Find the ignore case value.
     * 
     * @return The requested ignore case value.
     */
    public static boolean getIgnoreCase() {
        return booleanValue("IGNORE_CASE");
    }

    /**
     * Find the user tokenmanager value.
     * 
     * @return The requested user tokenmanager value.
     */
    public static boolean getUserTokenManager() {
        return booleanValue("USER_TOKEN_MANAGER");
    }

    /**
     * Find the user charstream value.
     * 
     * @return The requested user charstream value.
     */
    public static boolean getUserCharStream() {
        return booleanValue("USER_CHAR_STREAM");
    }

    /**
     * Find the build parser value.
     * 
     * @return The requested build parser value.
     */
    public static boolean getBuildParser() {
        return booleanValue("BUILD_PARSER");
    }

    /**
     * Find the build token manager value.
     * 
     * @return The requested build token manager value.
     */
    public static boolean getBuildTokenManager() {
        return booleanValue("BUILD_TOKEN_MANAGER");
    }

    /**
     * Find the sanity check value.
     * 
     * @return The requested sanity check value.
     */
    public static boolean getSanityCheck() {
        return booleanValue("SANITY_CHECK");
    }

    /**
     * Find the force lookahead check value.
     * 
     * @return The requested force lookahead value.
     */
    public static boolean getForceLaCheck() {
        return booleanValue("FORCE_LA_CHECK");
    }

    /**
     * Find the common token action value.
     * 
     * @return The requested common token action value.
     */

    public static boolean getCommonTokenAction() {
        return booleanValue("COMMON_TOKEN_ACTION");
    }

    /**
     * Find the cache tokens value.
     * 
     * @return The requested cache tokens value.
     */
    public static boolean getCacheTokens() {
        return booleanValue("CACHE_TOKENS");
    }

    /**
     * Find the keep line column value.
     * 
     * @return The requested keep line column value.
     */
    public static boolean getKeepLineColumn() {
        return booleanValue("KEEP_LINE_COLUMN");
    }

    /**
     * Find the output directory.
     * 
     * @return The requested output directory.
     */
    public static File getOutputDirectory() {
        return new File(stringValue("OUTPUT_DIRECTORY"));
    }
}