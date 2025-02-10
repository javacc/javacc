package org.javacc.jls;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.javacc.jls.java8mate.Java8ExtMate;

/**
 * "Main" launcher for the different JavaCC "JLS" java parsers (corresponding to the JLS versions).
 * <br>
 * Holds code for parsing java files (in a directory or a zip file) for any parser.<br>
 *
 * <p>See {@link AbstractParserMate} for a general overview.<br>
 * Uses functional programming to factorize code for the different parsers.
 *
 * <p>As it uses the {@link Timings} class which uses the {@code jdk.management} module, to avoid
 * the exception that will be thrown, one must use in JDK 9+ the following jvmarg:<br>
 * {@code --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED}.
 *
 * <p>May 2024 - Jan 2025.
 *
 * @author Maͫzͣaͬsͨ
 */
public class ParsersMain {

  /** This class simple name */
  protected static final String CN = ParsersMain.class.getSimpleName();

  /** The parser's version to use in the command line arguments */
  public static String verParser = null;

  /** The directory name in the command line arguments */
  public static String dirName = null;

  /** The file path name in the command line arguments */
  public static String fileName = null;

  /** The flag indicating whether the file is a zip file or not */
  public static boolean isZipFile = false;

  /** The recurse flag in the command line arguments */
  public static boolean recurse = false;

  /** The sequential flag in the command line arguments */
  public static boolean sequential = false;

  /** The parser creation flag in the command line arguments */
  public static boolean oneParserPerFile = false;

  /** The OS path separator */
  protected static final String FILE_SEP = System.getProperty("file.separator");

  /** The timings */
  private final Timings timings;

  /** The total number of parsing errors */
  private int nbErrors = 0;

  /** The total number of parsed files */
  private int nbFiles = 0;

  /** The total number of parsed lines */
  private Integer nbLines = 0;

  /** Increments {@link #nbErrors}. */
  private void incNbErrors() {
    nbErrors++;
  }

  /** Increments {@link #nbFiles}. */
  private void incNbFiles() {
    nbFiles++;
  }

  /**
   * Updates {@link #nbLines}.
   *
   * @param aInc - the increment
   */
  private void updNbLines(final int aInc) {
    nbLines += aInc;
  }

  /** Standard constructor. Tries to find the method to get the process cpu time. */
  public ParsersMain() {
    timings = new Timings();
    nbErrors = nbFiles = nbLines = 0;
  }

  /**
   * Standard main method.
   *
   * @param aArgs - the command line arguments
   */
  public static void main(final String[] aArgs) {
    if (readArgs(aArgs, false) == -1) {
      System.exit(-1);
    }
    System.exit(new ParsersMain().doMain(aArgs));
  }

  /** Outputs program usage on System.out. */
  private static void printUsage() {
    System.out.println("");
    System.out.println("(JLS) Java parsers: usage");
    System.out.println(CN + " -V <ver> -d <dir> (-r | -a | -f <sfile>) (-s) (-n)");
    System.out.println(CN + " -V <ver> -z <zfile> (-s) (-n)");
    printParseUsage();
  }

  /** Outputs parse part program usage on System.out. */
  private static void printParseUsage() {
    System.out.println("-V <ver>   : use the parser of the specified version (8, 9, 10...");
    System.out.println("-d <dir>   : the path to a directory");
    System.out.println(
        "-r         : process all files in the specified directory and its subdirectories");
    System.out.println("-a         : process all files in the specified directory");
    System.out.println("-f <sfile> : process this specific file in the specified directory");
    System.out.println("-z <zfile> : the path to a zip file (including the extension)");
    System.out.println("-s         : use a sequential stream instead of a parallel one");
    System.out.println("-n         : create a new parser instance for each file");
  }

  /**
   * Reads the command line arguments and sets the corresponding fields.
   *
   * @param aArgs - the command-line arguments
   * @param areOthers - true if other args are expected (in that case this method will likely be
   *     called in a super class), false otherwise
   * @return -1 if arguments are ko, last read argument index otherwise
   */
  protected static int readArgs(final String[] aArgs, final boolean areOthers) {
    if (aArgs.length == 0) {
      System.out.println(CN + " Error : missing ArgumentList");
      ParsersMain.printUsage();
      return -1;
    }
    final int al = aArgs.length;
    int lrx = -1; // index of last read argument
    boolean done = false;
    for (int i = 0; i < al; i++) {
      if (done && areOthers) {
        return lrx;
      }
      final String ai = aArgs[i];
      if (!ai.startsWith("-") || (ai.length() <= 1)) {
        System.out.println(CN + " Error : bad argument : " + ai);
        printUsage();
        return -1;
      }
      switch (ai.charAt(1)) {
        case 'V':
          if (i >= al - 1) {
            System.out.println(CN + " Error : missing version number after : " + ai);
            printUsage();
            return -1;
          }
          verParser = aArgs[++i];
          break;
        case 'd':
          if (i >= al - 1) {
            System.out.println(CN + " Error : missing directory name after : " + ai);
            printUsage();
            return -1;
          }
          dirName = aArgs[++i];
          break;
        case 'f':
          if (i >= al - 1) {
            System.out.println(CN + " Error : missing path name after : " + ai);
            printUsage();
            return -1;
          }
          fileName = aArgs[++i];
          done = true;
          break;
        case 'a':
          if (fileName != null) {
            System.out.println(CN + " Error : incompatible arguments -f <path> and -a");
            printUsage();
            return -1;
          }
          recurse = false;
          done = true;
          break;
        case 'r':
          if (fileName != null) {
            System.out.println(CN + " Error : incompatible arguments -f <path> and -r");
            printUsage();
            return -1;
          }
          recurse = true;
          done = true;
          break;
        case 's':
          if (fileName != null && !isZipFile) {
            System.out.println(CN + " Error : incompatible arguments -f <path> and -s");
            printUsage();
            return -1;
          }
          sequential = true;
          done = true;
          break;
        case 'n':
          oneParserPerFile = true;
          done = true;
          break;
        case 'z':
          if (i >= al - 1) {
            System.out.println(CN + " Error : missing zip file name after : " + ai);
            printUsage();
            return -1;
          }
          fileName = aArgs[++i];
          isZipFile = true;
          done = true;
          break;
        default:
          if (done && !areOthers) {
            if (aArgs.length > i) {
              System.out.println(
                  CN + " Error : too many arguments : nbArgs = " + aArgs.length + ", i = " + i);
              printUsage();
            }
            return -1;
          }
          break;
      }
      lrx = i;
    } // end for

    // check -V <ver>
    if (verParser == null) {
      System.out.println(CN + " Error : missing mandatory argument -V <ver>");
      printUsage();
      return -1;
    }
    try {
      if (Integer.parseInt(verParser) != 8) {
        System.out.println(CN + " Error : missing mandatory argument -V <ver>");
        printUsage();
        return -1;
      }
    } catch (final NumberFormatException e) {
      System.out.println(CN + " Error : non numeric argument for -V <ver>: " + verParser);
      printUsage();
      return -1;
    }
    // check either mandatory argument
    if (dirName == null && !isZipFile) {
      System.out.println(CN + " Error : one of -d <dir> or -z <zfile> argument must be specified");
      printUsage();
      return -1;
    }
    if (dirName != null) {
      // check dirName is a a real directory
      File f = new File(dirName);
      if (!f.isDirectory()) {
        System.out.println(CN + " Error : " + dirName + " is not a directory");
        printUsage();
        return -1;
      }
    }
    // check fileName is a real path
    if (fileName != null) {
      final String fn = isZipFile ? fileName : dirName + FILE_SEP + fileName;
      File f = new File(fn);
      if (!f.isFile()) {
        System.out.println(CN + " Error : " + fn + " is not a valid file path");
        printUsage();
        return -1;
      }
    }

    return lrx;
  }

  /**
   * Non static "main" method. Handles single file or whole directory / zip file parsing.<br>
   *
   * @param aArgs - the command line arguments
   * @return a 0 return code
   */
  public int doMain(final String[] aArgs) {

    final Function<InputStream, AbstractParserMate<?>> φNewParser;
    // hard code parsers classes
    switch (verParser) {
      case "8":
        φNewParser = Java8ExtMate::new;
        break;
      default:
        System.out.println(CN + " Error : unsupported version parser " + verParser);
        return -1;
    }

    if (fileName == null) {
      // directory
      doAllDir(dirName, recurse, φNewParser);
    } else if (fileName.endsWith(".java")) {
      // single path
      final String fn = dirName + FILE_SEP + fileName;
      doOneFile(fn, φNewParser);
    } else if (isZipFile) {
      doZipFile(fileName, φNewParser);
    }
    return 0;
  }

  /**
   * Surrounds parsing all the files in a directory by computing and displaying the elapsed and cpu
   * times and other counters.
   *
   * @param aDirectory - the files directory to process
   * @param aRecurse - true to recurse in subdirectories, false otherwise
   * @param µNewParser - the 1-arg parser constructor
   */
  public void doAllDir(
      final String aDirectory,
      final boolean aRecurse, //
      final Function<InputStream, AbstractParserMate<?>> µNewParser) {

    System.out.flush();
    System.err.flush();

    timings.start();

    processAllDir(aDirectory, aRecurse, µNewParser);

    timings.stop();

    System.out.flush();
    System.err.flush();
    printSummary();
  }

  /**
   * Parses all the files in a directory, with a <code>forEach(lambda);</code> on a parallel or
   * sequential stream of all files.
   *
   * @param aDirectory - the directory to process
   * @param aRecurse - true to recurse in subdirectories, false otherwise
   * @param µNewParser - the 1-arg parser constructor
   */
  void processAllDir(
      final String aDirectory,
      final boolean aRecurse, //
      final Function<InputStream, AbstractParserMate<?>> µNewParser) {

    final Path dirPath = new File(aDirectory).toPath();

    // variables referenced in a lambda must be final or effectively final, so we'll use a relay;
    // in the lambda expression we must ensure thread safety on objects which maintain state,
    // as the parser does, so we use the ThreadLocal technique for it,
    // and (further) a synchronized block for updating the counters and timings
    // final AbstractParserMate<?> pm = µNewParser.apply(System.in);
    final ThreadLocal<AbstractParserMate<?>> tlpm =
        new ThreadLocal<AbstractParserMate<?>>() {
          @Override
          protected AbstractParserMate<?> initialValue() {
            return µNewParser.apply(System.in);
          }
        };

    try (Stream<Path> fs = Files.list(dirPath)) {

      // declare the lambda expression which will be used in 2 statements
      final Consumer<? super Path> lambda =
          eFilePath -> {
            final String fn = eFilePath.getFileName().toString();

            if (eFilePath.toFile().isDirectory()) {

              if (aRecurse) {
                processAllDir(aDirectory + FILE_SEP + fn, aRecurse, µNewParser);
              }

            } else if (fn.endsWith(".java")) {

              // local variable as a relay
              // AbstractParserMate<?> lpm = pm;
              AbstractParserMate<?> lpm = tlpm.get();
              if (oneParserPerFile) {
                lpm = µNewParser.apply(System.in);
              }
              lpm.reInitMate();

              final boolean rc = lpm.processOneFile(eFilePath.toString());

              updateCounters(lpm, rc);
            }
          };

      // process each element of the full stream (the Files.list(dirPath) is itself not parallel)
      if (sequential) {
        fs.sequential().forEach(lambda);
      } else {
        fs.parallel().forEach(lambda);
      }

    } // end try
    catch (final IOException ioex) {
      // what can we do?
      ioex.printStackTrace();
    }
  }

  /**
   * Surrounds asking a parser's mate to parse a single file by computing and displaying the elapsed
   * and cpu times and other counters.
   *
   * @param aFp - the file path name
   * @param aParserMate - the parser mate
   */
  public void doOneFile(
      final String aFp, //
      final Function<InputStream, AbstractParserMate<?>> µNewParser) {

    timings.start();

    nbErrors = nbFiles = 0;
    final AbstractParserMate<?> pm = µNewParser.apply(System.in);
    pm.reInitMate();
    final boolean rc = pm.processOneFile(aFp);
    if (!rc) {
      incNbErrors();
    }
    incNbFiles();
    updNbLines(pm.nbLines);

    timings.stop();

    System.out.flush();
    System.err.flush();
    System.err.println(
        aFp
            + " parsed, with "
            + nbErrors
            + " error, for a total of "
            + nbLines
            + " lines, in "
            + timings.getElapsedTime()
            + " elapsed sec and "
            + timings.getProcessCpuTime()
            + " cpu sec");
  }

  /**
   * Surrounds asking a parser's mate to parse a zip file by computing and displaying the elapsed
   * and cpu times and other counters.
   *
   * @param aFp - the zip file path name
   * @param aParserMate - the parser mate
   */
  public void doZipFile(
      final String aFp, //
      final Function<InputStream, AbstractParserMate<?>> µNewParser) {

    System.out.flush();
    System.err.flush();

    timings.start();

    processZipFile(aFp, µNewParser);

    timings.stop();

    System.out.flush();
    System.err.flush();
    printSummary();
  }

  /**
   * Parses all the files in a zip file, with a <code>forEach(lambda);</code> on a parallel or
   * sequential stream of all files.
   *
   * @param aFp - the zip file path name
   * @param µNewParser - the 1-arg parser constructor
   */
  void processZipFile(
      final String aFp, //
      final Function<InputStream, AbstractParserMate<?>> µNewParser) {

    // variables referenced in a lambda must be final or effectively final, so we'll use a relay;
    // in the lambda expression we must ensure thread safety on objects which maintain state,
    // as the parser does, so we use the ThreadLocal technique for it,
    // and (further) a synchronized block for updating the counters and timings
    // final AbstractParserMate<?> pm = µNewParser.apply(System.in);
    final ThreadLocal<AbstractParserMate<?>> tlpm =
        new ThreadLocal<AbstractParserMate<?>>() {
          @Override
          protected AbstractParserMate<?> initialValue() {
            return µNewParser.apply(System.in);
          }
        };

    try {

      // open the zip file
      try (ZipFile zipFile = new ZipFile(aFp)) {

        // get a stream of ZipEntries and filter it on files only
        @SuppressWarnings("unchecked")
        Stream<ZipEntry> zes = (Stream<ZipEntry>) zipFile.stream().filter(ze -> !ze.isDirectory());

        // declare the lambda expression which will be used in 2 statements
        final Consumer<? super ZipEntry> lambda =
            eZipEntry -> {
              final String fn = eZipEntry.getName();

              if (fn.endsWith(".java")) {

                // local variable as a relay
                // AbstractParserMate<?> lpm = pm;
                AbstractParserMate<?> lpm = tlpm.get();
                if (oneParserPerFile) {
                  lpm = µNewParser.apply(System.in);
                }
                lpm.reInitMate();

                final boolean rc = lpm.processOneZipEntry(zipFile, eZipEntry);

                updateCounters(lpm, rc);
              }
            };

        // process each element of the full stream (the Files.list(dirPath) is itself not parallel)
        if (sequential) {
          zes.sequential().forEach(lambda);
        } else {
          zes.parallel().forEach(lambda);
        }
      }

    } // end try
    catch (final IOException ioex) {
      // what can we do?
      ioex.printStackTrace();
    }
  }

  /**
   * Updates parsing statistics counters.
   *
   * @param lpm - the parser mate
   * @param rc - the parse return code
   */
  private void updateCounters(AbstractParserMate<?> lpm, final boolean rc) {
    synchronized ("inc&dots") {
      if (!rc) {
        incNbErrors();
      }
      incNbFiles();
      updNbLines(lpm.nbLines);
      // one dot for 100 files
      if (nbFiles % 100 == 0) {
        System.err.print(".");
      }
      // System.err.println(fmtTT());
      if (nbFiles % 1000 == 0) {
        // new line after 10 dots
        System.err.print(" (" + nbFiles + ")");
        //        System.err.printf(
        //            " (%2.0f%% process / %2.0f%% system CPU load)",
        //            100 * timings.getCurrentProcessCpuLoad(), 100 *
        // timings.getCurrentSystemCpuLoad());
        System.err.println();
      }
    }
  }

  /** Prints counters and timings. */
  private void printSummary() {
    System.err.printf(
        // FR grouping separator (in %,d) displays as EFBFBD under ant java task / jdk 11
        Locale.US,
        "%n%,d files parsed in %s, %d with error, for a total of %,d lines, " //
            + "in %.1f elapsed sec, %.1f process cpu sec" //
            //            + "+, %.0f%% process / %.0f%% system CPU load" //
            + "%n",
        Integer.valueOf(nbFiles), //
        sequential
            ? "sequence"
            : "parallel" + (oneParserPerFile ? " (with one parser per file)" : ""), //
        Integer.valueOf(nbErrors), //
        nbLines, //
        timings.getElapsedTime(), //
        timings.getProcessCpuTime() //
//        , //
//        100 * timings.getProcessCpuLoad(), //
//        100 * timings.getSystemCpuLoad() //
        );
  }

  /**
   * @return a short name for the current thread
   */
  static String fmtTT() {
    final String s = Thread.currentThread().toString();
    return s.replace("Thread[", "")
        .replace(",5,main]", "")
        .replace("ForkJoinPool.commonPool-worker", "wk");
  }

  /**
   * Functional interface of Supplier type with a void returning function.
   *
   * <p>May 2024.
   *
   * @author Maͫzͣaͬsͨ
   */
  @FunctionalInterface
  public interface VoidSupplier {
    /**
     * @return the result
     * @throws E the exception
     */
    void get();
  }
}
