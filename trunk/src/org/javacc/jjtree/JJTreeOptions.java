/* Copyright (c) 2005-2006, Kees Jan Koster kjkoster@kjkoster.org
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
package org.javacc.jjtree;

import java.io.File;

import org.javacc.parser.Options;

/**
 * The JJTree-specific options.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
class JJTreeOptions extends Options {

    /**
     * Limit subclassing to derived classes.
     */
    protected JJTreeOptions() {
        super();
    }

    /**
     * Initialize the JJTree-specific options.
     */
    public static void init() {
        Options.init();

        Options.optionValues.put("JDK_VERSION", "1.4");
        Options.optionValues.put("MULTI", Boolean.FALSE);
        Options.optionValues.put("NODE_DEFAULT_VOID", Boolean.FALSE);
        Options.optionValues.put("NODE_SCOPE_HOOK", Boolean.FALSE);
        Options.optionValues.put("NODE_FACTORY", Boolean.FALSE);
        Options.optionValues.put("NODE_USES_PARSER", Boolean.FALSE);
        Options.optionValues.put("BUILD_NODE_FILES", Boolean.TRUE);
        Options.optionValues.put("VISITOR", Boolean.FALSE);

        Options.optionValues.put("NODE_PREFIX", "AST");
        Options.optionValues.put("NODE_PACKAGE", "");
        Options.optionValues.put("NODE_EXTENDS", "");
        Options.optionValues.put("OUTPUT_FILE", "");
        Options.optionValues.put("VISITOR_EXCEPTION", "");

        Options.optionValues.put("JJTREE_OUTPUT_DIRECTORY", "");
    }

    /**
     * Find the JDK version.
     * 
     * @return The specified JDK version.
     */
    public static String getJdkVersion() {
        return stringValue("JDK_VERSION");
    }

    /**
     * Find the multi value.
     * 
     * @return The requested multi value.
     */
    public static boolean getMulti() {
        return booleanValue("MULTI");
    }

    /**
     * Find the node default void value.
     * 
     * @return The requested node default void value.
     */
    public static boolean getNodeDefaultVoid() {
        return booleanValue("NODE_DEFAULT_VOID");
    }

    /**
     * Find the node scope hook value.
     * 
     * @return The requested node scope hook value.
     */
    public static boolean getNodeScopeHook() {
        return booleanValue("NODE_SCOPE_HOOK");
    }

    /**
     * Find the node factory value.
     * 
     * @return The requested node factory value.
     */
    public static boolean getNodeFactory() {
        return booleanValue("NODE_FACTORY");
    }

    /**
     * Find the node uses parser value.
     * 
     * @return The requested node uses parser value.
     */
    public static boolean getNodeUsesParser() {
        return booleanValue("NODE_USES_PARSER");
    }

    /**
     * Find the build node files value.
     * 
     * @return The requested build node files value.
     */
    public static boolean getBuildNodeFiles() {
        return booleanValue("BUILD_NODE_FILES");
    }

    /**
     * Find the visitor value.
     * 
     * @return The requested visitor value.
     */
    public static boolean getVisitor() {
        return booleanValue("VISITOR");
    }

    /**
     * Find the node prefix value.
     * 
     * @return The requested node prefix value.
     */
    public static String getNodePrefix() {
        return stringValue("NODE_PREFIX");
    }

    /**
     * Find the node super class name.
     * 
     * @return The requested node super class
     */
    public static String getNodeExtends() {
        return stringValue("NODE_EXTENDS");
    }

    /**
     * Find the node package value.
     * 
     * @return The requested node package value.
     */
    public static String getNodePackage() {
        return stringValue("NODE_PACKAGE");
    }

    /**
     * Find the output file value.
     * 
     * @return The requested output file value.
     */
    public static String getOutputFile() {
        return stringValue("OUTPUT_FILE");
    }

    /**
     * Find the visitor exception value
     * 
     * @return The requested visitor exception value.
     */
    public static String getVisitorException() {
        return stringValue("VISITOR_EXCEPTION");
    }

    /**
     * Find the output directory to place the generated <code>.jj</code> files
     * into. If none is configured, use the value of
     * <code>getOutputDirectory()</code>.
     * 
     * @return The requested JJTree output directory
     */
    public static File getJJTreeOutputDirectory() {
        final String dirName = stringValue("JJTREE_OUTPUT_DIRECTORY");
        File dir = null;
        
        if ("".equals(dirName)) {
            dir = getOutputDirectory();
        } else {
            dir = new File(dirName);
        } 
        
        return dir;
    }
}
