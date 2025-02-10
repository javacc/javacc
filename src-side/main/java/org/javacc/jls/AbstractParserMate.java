package org.javacc.jls;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Superclass for java companions (mates) of parsers. Factorizes code common to parsers' mates.
 *
 * <p>JavaCC JLS Java parsers embed some java code along with the pure grammar syntax, but we do not
 * want to write too much java code there, especially if this code is for other needs (like code for
 * parsing a whole directory instead of just a single file).<br>
 * And along with this, the IDE's corresponding plugins (e.g. JavaCC Plugin for Eclipse at
 * SourceForge) do not propose all the IDE's java plugin (e.g. JDT for Eclipse) features.<br>
 * So we set up a solution for using different flavors of the parsers by making each of our JavaCC
 * parsers extend a Java class called its mate, and put java code as much as possible in the mate
 * and not in the parser. We use ant's copy / replace tasks and javacc generation task with some
 * options overwriting to create new flavors of the (.jj / .java) parsers that will extend their
 * corresponding mates, trying not needing to add code, just replacing identifiers or comment lines
 * (like debug lines).
 *
 * <p>Uses multi-threading through parallel streams and a thread safe lambda, and functional
 * programming.
 *
 * <p>May 2024 - Jan 2025.
 *
 * @author Maͫzͣaͬsͨ
 * @param <P> - the parser's type
 */
public abstract class AbstractParserMate<P> {

  /** The parser */
  public P parser;

  /** The parser (simple) name */
  public static String parserName;

  /** The input path name the parser is working on */
  public String inputFileName = null;

  /** The number of parsed lines */
  protected Integer nbLines;

  /** The OS path separator */
  private static final String PATH_SEP = System.getProperty("path.separator");

  /** Reinitializes the mate. */
  protected abstract void reInitMate();

  /**
   * Reinitializes the parser.
   *
   * @param aRdr - the reader
   */
  protected abstract void reInitParser(final Reader aRdr);

  /**
   * Reinitializes the mate (resets the source input path name).
   *
   * @param aFileName - the source input path name
   */
  public void reInitFileName(final String aFileName) {
    inputFileName = aFileName;
  }

  /**
   * Parses one file. <br>
   *
   * @param aFileName - the file name
   * @return true if parsing with no errors, false otherwise
   */
  public abstract boolean processOneFile(final String aFileName);

  /**
   * Parses one zip file entry. <br>
   *
   * @param aZipFile - the zip file
   * @param aZipEntry - the zip entry
   * @return true if parsing with no errors, false otherwise
   */
  public abstract boolean processOneZipEntry(final ZipFile aZipFile, final ZipEntry aZipEntry);

  /** Updates the number of parsed lines. */
  public abstract void updNbLines();

  /**
   * Parser's path parsing method, to be used by the mate or by the junit tester. Just parses the
   * path.
   *
   * @param aFileName - the path name
   * @param µReInitParser - the method to call (on the appropriate object, i.e. the corresponding
   *     parser) to reinitialize the parser
   * @param µStartParse - the method to call (on the appropriate object, i.e. the corresponding
   *     parser) to start parsing
   * @return true if parsing ok, false otherwise
   */
  public boolean parseFile(
      final String aFileName, //
      final Consumer<BufferedReader> µReInitParser, //
      final ThrowingSupplier<Exception> µStartParse) {

    // System.out.println(parserName + " Reading from path " + inputFileName);
    // System.out.println(" at " + java.text.DateFormat.getDateTimeInstance().format(new
    // java.util.Date()));

    try {
      // open the path through a FileInputStream for buffered input (try-with-resource)
      final InputStream is = new FileInputStream(aFileName);
      final String encoding = "UTF-8";
      try (BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding))) {
        return parseContents(aFileName, br, µReInitParser, µStartParse);
      } catch (final UnsupportedEncodingException uee) {
        System.err.println(uee.getMessage());
        System.err.println(parserName + " invalid encoding < " + encoding + " >");
      } catch (final IOException ioe) {
        System.err.println(ioe.getMessage());
        System.err.println(parserName + " IOException on path " + aFileName);
      }
    } catch (final FileNotFoundException fnfe) {
      System.err.println(fnfe.getMessage());
      System.err.println(parserName + " File " + aFileName + " not found");
    }
    System.err.flush();
    return false;
  }

  /**
   * Parser's zip entry parsing method, to be used by the mate or by the junit tester. Just parses
   * the zip entry.
   *
   * @param aZipFile - the zip file
   * @param aZipEntry - the zip entry
   * @param µReInitParser - the method to call (on the appropriate object, i.e. the corresponding
   *     parser) to reinitialize the parser
   * @param µStartParse - the method to call (on the appropriate object, i.e. the corresponding
   *     parser) to start parsing
   * @return true if parsing ok, false otherwise
   */
  public boolean parseZipEntry(
      final ZipFile aZipFile, //
      final ZipEntry aZipEntry, //
      final Consumer<BufferedReader> µReInitParser, //
      final ThrowingSupplier<Exception> µStartParse) {

    final String aFileName = aZipEntry.getName();
    // System.out.println(parserName + " Reading from zip entry " + aFileName);
    // System.out.println(" at " + java.text.DateFormat.getDateTimeInstance().format(new
    // java.util.Date()));

    try {
      // open the zip entry through an InputStream for buffered input (try-with-resource)
      final InputStream is = aZipFile.getInputStream(aZipEntry);
      final String encoding = "UTF-8";
      try (BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding))) {
        return parseContents(aFileName, br, µReInitParser, µStartParse);
      } catch (final UnsupportedEncodingException uee) {
        System.err.println(uee.getMessage());
        System.err.println(parserName + " invalid encoding < " + encoding + " >");
      }
    } catch (final IOException ioe) {
      System.err.println(ioe.getMessage());
      System.err.println(parserName + " IOException on path " + aFileName);
    }
    System.err.flush();
    return false;
  }

  /**
   * Parser's private commond parsing method. Just parses the buffered reader.
   *
   * @param aZipFile - the zip file
   * @param aZipEntry - the zip entry
   * @param µReInitParser - the method to call (on the appropriate object, i.e. the corresponding
   *     parser) to reinitialize the parser
   * @param µStartParse - the method to call (on the appropriate object, i.e. the corresponding
   *     parser) to start parsing
   * @return true if parsing ok, false otherwise
   */
  private boolean parseContents(
      final String aFileName,
      BufferedReader aBr,
      final Consumer<BufferedReader> µReInitParser,
      final ThrowingSupplier<Exception> µStartParse) {
    
    final String fileShortName = aFileName.substring(1 + aFileName.lastIndexOf(PATH_SEP));
    // reinitialize the parser
    µReInitParser.accept(aBr);
    // reinitialize the mate
    reInitFileName(fileShortName);
    // parse directly through µStartParse
    try {
      // call the parsing function
      µStartParse.get();
      // System.out.println(parserName + " program " + inputFileName + " parsed successfully");
      return true;
    } catch (final Exception ex) {
      // may be ParseExceptions, so combine them in Exception but do not print it twice
      final String msg = ex.getMessage();
      if (msg != null) {
        System.err.println(msg);
      }
      System.err.println(
          parserName
              + "(AbstractParserMate.parseFile) Exception raised during parsing of "
              + aFileName);
      ex.printStackTrace();
    } catch (final Error er) {
      // may be LexicalErrors, so combine them in Error but do not print it twice
      final String msg = er.getMessage();
      if (msg != null) {
        System.err.println(msg);
        // er.printStackTrace();
      }
      System.err.println(
          parserName
              + "(AbstractParserMate.parseFile) Error raised during parsing of "
              + aFileName);
    }
    return false;
  }

  /**
   * Functional interface of Supplier type throwing an exception.
   *
   * <p>May 2024.
   *
   * @author Maͫzͣaͬsͨ
   * @param <E> - the thrown exception type
   */
  @FunctionalInterface
  public interface ThrowingSupplier<E extends Exception> {
    /**
     * @return the result
     * @throws E the exception
     */
    void get() throws E;
  }
}
