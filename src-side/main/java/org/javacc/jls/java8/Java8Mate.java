package org.javacc.jls.java8;

import java.io.Reader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.javacc.jls.AbstractParserMate;
import org.javacc.jls.java8mate.Java8ExtMate;

/**
 * Superclass for the JavaCC JLS Java8ExtMate parser, holding most of the gluing java code.<br>
 * Java8ExtMate.jj is generated from Java8.jj through some ant's copy / replace tasks.
 *
 * <p>May 2024 - Jan 2025.
 *
 * @author Maͫzͣaͬsͨ
 */
public class Java8Mate extends AbstractParserMate<Java8ExtMate> {

  /** {@inheritDoc} */
  @Override
  public void reInitMate() {
    parser = (Java8ExtMate) this;
    parserName = Java8ExtMate.class.getSimpleName();
    nbLines = 0;
    parser.disable_tracing();
  }

  /** {@inheritDoc} */
  @Override
  protected void reInitParser(final Reader aRdr) {
    parser.ReInit(aRdr);
  }

  /** {@inheritDoc} */
  @Override
  public boolean processOneFile(final String aFileName) {
    // System.err.println("parser = " + parser);
    final boolean rc = parseFile(aFileName, parser::reInitParser, parser::CompilationUnit);
    if (rc) {
      updNbLines();
    }
    return rc;
  }

  /** {@inheritDoc} */
  @Override
  public boolean processOneZipEntry(final ZipFile aZipFile, final ZipEntry aZipEntry) {
    // System.err.println("parser = " + parser);
    final boolean rc = parseZipEntry(aZipFile, aZipEntry, parser::reInitParser, parser::CompilationUnit);
    if (rc) {
      updNbLines();
    }
    return rc;
  }

  /** {@inheritDoc} */
  @Override
  public void updNbLines() {
    // trick to get the number of lines of the file: use the last parsed token
    nbLines += parser.token.endLine;
  }
}
