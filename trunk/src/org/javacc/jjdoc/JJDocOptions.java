package org.javacc.jjdoc;

import org.javacc.parser.Options;

/**
 * The options, specific to JJDoc.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
class JJDocOptions extends Options {

    /**
     * Limit subclassing to derived classes.
     */
    protected JJDocOptions() {
        super();
    }

    /**
     * Initialize the options.
     */
    public static void init() {
        Options.init();

        Options.optionValues.put("ONE_TABLE", Boolean.TRUE);
        Options.optionValues.put("TEXT", Boolean.FALSE);

        Options.optionValues.put("OUTPUT_FILE", "");
        Options.optionValues.put("CSS", "");
    }

    /**
     * Find the one table value.
     * 
     * @return The requested one table value.
     */
    public static boolean getOneTable() {
        return booleanValue("ONE_TABLE");
    }

    /**
     * Find the CSS value.
     *
     * @return The requested CSS value.
     */
    public static String getCSS() {
        return stringValue("CSS");
    }

    /**
     * Find the text value.
     * 
     * @return The requested text value.
     */
    public static boolean getText() {
        return booleanValue("TEXT");
    }

    /**
     * Find the output file value.
     * 
     * @return The requested output value.
     */
    public static String getOutputFile() {
        return stringValue("OUTPUT_FILE");
    }
}