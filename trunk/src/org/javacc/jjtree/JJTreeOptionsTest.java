package org.javacc.jjtree;

import java.io.File;

import org.javacc.parser.JavaCCErrors;
import org.javacc.parser.Options;

import junit.framework.TestCase;

/**
 * Test the JJTree-specific options.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public final class JJTreeOptionsTest extends TestCase {
    public void testOutputDirectory() {
        JJTreeOptions.init();
        JavaCCErrors.reInit();

        assertEquals(new File("."), JJTreeOptions.getOutputDirectory());
        assertEquals(new File("."), JJTreeOptions.getJJTreeOutputDirectory());

        Options.setInputFileOption(null, null, "OUTPUT_DIRECTORY",
        "test/output");
        assertEquals(new File("test/output"), JJTreeOptions.getOutputDirectory());
        assertEquals(new File("test/output"), JJTreeOptions.getJJTreeOutputDirectory());

        Options.setInputFileOption(null, null, "JJTREE_OUTPUT_DIRECTORY",
                "test/jjtreeoutput");
        assertEquals(new File("test/output"), JJTreeOptions.getOutputDirectory());
        assertEquals(new File("test/jjtreeoutput"), JJTreeOptions.getJJTreeOutputDirectory());

        assertEquals(0, JavaCCErrors.get_warning_count());
        assertEquals(0, JavaCCErrors.get_error_count());
        assertEquals(0, JavaCCErrors.get_parse_error_count());
        assertEquals(0, JavaCCErrors.get_semantic_error_count());
    }
}
