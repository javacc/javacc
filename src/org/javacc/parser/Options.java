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
        optionValues.put("TOKEN_MANAGER_USES_PARSER", Boolean.FALSE);
        optionValues.put("SANITY_CHECK", Boolean.TRUE);
        optionValues.put("FORCE_LA_CHECK", Boolean.FALSE);
        optionValues.put("COMMON_TOKEN_ACTION", Boolean.FALSE);
        optionValues.put("CACHE_TOKENS", Boolean.FALSE);
        optionValues.put("KEEP_LINE_COLUMN", Boolean.TRUE);

        optionValues.put("OUTPUT_DIRECTORY", ".");
        optionValues.put("JDK_VERSION", "1.4");
        optionValues.put("TOKEN_EXTENDS", "");
        optionValues.put("TOKEN_FACTORY", "");
    }
    
    /**
     * Returns a string representation of the specified options of interest.
     * Used when, for example, generating Token.java to record the JavaCC options
     * that were used to generate the file. All of the options must be
     * boolean values.
     * @param interestingOptions the options of interest, eg {"STATIC", "CACHE_TOKENS"} 
     * @return the string representation of the options, eg "STATIC=true,CACHE_TOKENS=false"
     */
    public static String getOptionsString(String[] interestingOptions) {
      StringBuffer sb = new StringBuffer();
      
      for (int i = 0; i < interestingOptions.length; i++) {
        String key = interestingOptions[i];
        sb.append(key);
        sb.append('=');
        sb.append(optionValues.get(key));
        if (i != interestingOptions.length -1) {
          sb.append(',');
        }
      }
      
      return sb.toString();
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

    /**
     * Help function to handle cases where the meaning of an option has changed
     * over time. If the user has supplied an option in the old format, it will
     * be converted to the new format.
     * 
     * @param name The name of the option being checked.
     * @param value The option's value.
     * @return The upgraded value.
     */
    public static Object upgradeValue(final String name, Object value) {
      if (name.equalsIgnoreCase("NODE_FACTORY") && value.getClass() == Boolean.class) {
          if (((Boolean)value).booleanValue()) {
            value = "*";
          } else {
            value = "";
          }
      }
      
      return value;
    }
    
    public static void setInputFileOption(Object nameloc, Object valueloc,
            String name, Object value) {
        String s = name.toUpperCase();
        if (!optionValues.containsKey(s)) {
            JavaCCErrors.warning(nameloc, "Bad option name \"" + name
                    + "\".  Option setting will be ignored.");
            return;
        }
        final Object existingValue = optionValues.get(s);
        
        value = upgradeValue(name, value);
        
        if (existingValue != null) {
            if ((existingValue.getClass() != value.getClass()) ||
                    (value instanceof Integer && ((Integer)value).intValue() <= 0)) {
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
                if (!existingValue.equals(value)) {
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
     * Process a single command-line option.
     * The option is parsed and stored in the optionValues map.
     * @param arg
     */
    public static void setCmdLineOption(String arg) {
        final String s;
        
        if (arg.charAt(0) == '-') {
            s = arg.substring(1);
        } else {
            s = arg;
        }
        
        String name;
        Object Val;

	// Look for the first ":" or "=", which will separate the option name
	// from its value (if any).
        final int index1 = s.indexOf('=');
        final int index2 = s.indexOf(':');
        final int index;

        if (index1 < 0)
          index = index2;
        else if (index2 < 0)
          index = index1;
        else if (index1 < index2)
          index = index1;
        else
          index = index2;
        
        if (index < 0) {
            name = s.toUpperCase();
            if (optionValues.containsKey(name))
            {
              Val = Boolean.TRUE;
            } else if (name.length() > 2 && name.charAt(0) == 'N' && name.charAt(1) == 'O') {
              Val = Boolean.FALSE;
              name = name.substring(2);
            } else {
                System.out.println("Warning: Bad option \"" + arg
                        + "\" will be ignored.");
                return;
            }
        } else {
            name = s.substring(0, index).toUpperCase();
            if (s.substring(index + 1).equalsIgnoreCase("TRUE")) {
                Val = Boolean.TRUE;
            } else if (s.substring(index + 1).equalsIgnoreCase("FALSE")) {
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
                    Val = s.substring(index + 1);
                    if (s.length() > index + 2) {
                        // i.e., there is space for two '"'s in value
                        if (s.charAt(index + 1) == '"'
                                && s.charAt(s.length() - 1) == '"') {
                            // remove the two '"'s.
                            Val = s.substring(index + 2, s.length() - 1);
                        }
                    }
                }
            }
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

        Val = upgradeValue(name, Val);
        
        optionValues.put(name, Val);
        cmdLineSetting.add(name);
    }

    public static void normalize() {
        if (getDebugLookahead() && !getDebugParser()) {
            if (cmdLineSetting.contains("DEBUG_PARSER")
                    || inputFileSetting.contains("DEBUG_PARSER")) {
                JavaCCErrors
                        .warning("True setting of option DEBUG_LOOKAHEAD overrides " + 
                                 "false setting of option DEBUG_PARSER.");
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
     * Find the token manager uses parser value.
     *
     * @return The requested token manager uses parser value;
     */
     public static boolean getTokenManagerUsesParser(){
         return booleanValue("TOKEN_MANAGER_USES_PARSER");
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
     * Find the JDK version.
     * 
     * @return The requested jdk version.
     */
    public static String getJdkVersion() {
        return stringValue("JDK_VERSION");
    }
    
    /**
     * Determine if the output language is at least the specified
     * version.
     * @param version the version to check against. E.g. <code>1.5</code>
     * @return true if the output version is at least the specified version.
     */
    public static boolean jdkVersionAtLeast(double version) {
      double jdkVersion = Double.parseDouble(getJdkVersion());
      
      // Comparing doubles is safe here, as it is two simple assignments.
      return jdkVersion >= version;
    }

    /**
     * Return the Token's superclass.
     * 
     * @return The required base class for Token.
     */
    public static String getTokenExtends()
    {
      return stringValue("TOKEN_EXTENDS");
    }
    
    /**
     * Return the Token's factory class.
     * 
     * @return The required factory class for Token.
     */
    public static String getTokenFactory()
    {
      return stringValue("TOKEN_FACTORY");
    }
    
    /**
     * Find the output directory.
     * 
     * @return The requested output directory.
     */
    public static File getOutputDirectory() {
        return new File(stringValue("OUTPUT_DIRECTORY"));
    }

    public static String stringBufOrBuild() {
        if (Options.getJdkVersion().equals("1.5") || Options.getJdkVersion().equals("1.6")) {
            return "StringBuilder";
        } else {
            return "StringBuffer";
        }
    }

}
