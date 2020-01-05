
package org.javacc.utils;

import org.javacc.parser.JavaCCErrors;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.Options;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The {@link OutputFileDigest} class.
 */
abstract class OutputFileDigest {

  private static final String MD5_LINE_PART_1  = "/* JavaCC - OriginalChecksum=";
  private static final String MD5_LINE_PART_1q = "/\\* JavaCC - OriginalChecksum=";
  private static final String MD5_LINE_PART_2  = " (do not edit this line) */";
  private static final String MD5_LINE_PART_2q = " \\(do not edit this line\\) \\*/";

  private static final char[] HEX_DIGITS       =
      new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };


  /**
   * Constructs an instance of {@link OutputFileDigest}.
   */
  private OutputFileDigest() {}

  /**
   * Creates the Digest Line.
   *
   * @param digestStream
   */
  public static String getDigestLine(DigestOutputStream digestStream) {
    return MD5_LINE_PART_1 + OutputFileDigest.toHexString(digestStream) + MD5_LINE_PART_2;
  }

  /**
   * Create an MD5 {@link DigestOutputStream} for the provided {@link OutputStream}.
   * 
   * @param stream
   * @throws NoSuchAlgorithmException
   */
  public static DigestOutputStream getDigestStream(OutputStream stream) throws NoSuchAlgorithmException {
    return new DigestOutputStream(stream, MessageDigest.getInstance("MD5"));
  }

  /**
   * Check if the File already exists.
   *
   * @param file
   * @param toolName
   * @param compatibleVersion
   * @param options
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static boolean check(File file, String toolName, String compatibleVersion, String[] options)
      throws FileNotFoundException, IOException {
    // File does not exist
    if (!file.exists()) {
      System.out.println("File \"" + file.getName() + "\" does not exist.  Will create one.");
      return true;
    }

    // Generate the checksum of the file, and compare with any value
    // stored in the file.
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      String existingMD5 = null;
      DigestOutputStream digestStream = getDigestStream(new NullOutputStream());
      try (PrintWriter pw = new PrintWriter(digestStream)) {
        while ((line = reader.readLine()) != null) {
          if (line.startsWith(MD5_LINE_PART_1)) {
            existingMD5 = line.replaceAll(MD5_LINE_PART_1q, "").replaceAll(MD5_LINE_PART_2q, "");
          } else {
            pw.println(line);
          }
        }
      }

      String calculatedDigest = OutputFileDigest.toHexString(digestStream);
      if (existingMD5 == null || !existingMD5.equals(calculatedDigest)) {
        if (compatibleVersion != null) {
          OutputFileDigest.checkVersion(file, toolName, compatibleVersion);
        }

        if (options != null) {
          OutputFileDigest.checkOptions(file, options);
        }

        // No checksum in file, or checksum differs.
        return false;
      }
    } catch (NoSuchAlgorithmException e) {
      throw (IOException) new IOException("No MD5 implementation").initCause(e);
    }

    // The file has not been altered since JavaCC created it.
    // Rebuild it.
    System.out.println("File \"" + file.getName() + "\" is being rebuilt.");
    return true;
  }


  /**
   * Output a warning if the file was created with an incompatible version of
   * JavaCC.
   * 
   * @param file
   * @param toolName
   * @param versionId
   */
  private static void checkVersion(File file, String toolName, String versionId) {
    String firstLine = "/* " + JavaCCGlobals.getIdString(toolName, file.getName()) + " Version ";

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(firstLine)) {
          String version = line.replaceFirst(".*Version ", "").replaceAll(" \\*/", "");
          if (!version.equals(versionId)) {
            JavaCCErrors.warning(file.getName() + ": File is obsolete.  Please rename or delete this file so"
                + " that a new one can be generated for you.");
            JavaCCErrors.warning(file.getName() + " file   version: " + version + " javacc version: " + versionId);
          }
          return;
        }
      }
      // If no version line is found, do not output the warning.
    } catch (FileNotFoundException e1) {
      // This should never happen
      JavaCCErrors.semantic_error("Could not open file " + file.getName() + " for writing.");
      throw new Error();
    } catch (IOException e2) {}
  }

  /**
   * Read the options line from the file and compare to the options currently in
   * use. Output a warning if they are different.
   *
   * @param file
   * @param options
   */
  private static void checkOptions(File file, String[] options) {
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("/* JavaCCOptions:")) {
          String currentOptions = Options.getOptionsString(options);
          if (line.indexOf(currentOptions) == -1) {
            JavaCCErrors
                .warning(file.getName() + ": Generated using incompatible options. Please rename or delete this file so"
                    + " that a new one can be generated for you.");
          }
          return;
        }
      }
    } catch (FileNotFoundException e1) {
      // This should never happen
      JavaCCErrors.semantic_error("Could not open file " + file.getName() + " for writing.");
      throw new Error();
    } catch (IOException e2) {}
    // Not found so cannot check
  }

  private static String toHexString(DigestOutputStream digestStream) {
    StringBuffer buffer = new StringBuffer(32);
    for (byte b : digestStream.getMessageDigest().digest()) {
      buffer.append(HEX_DIGITS[(b & 0xF0) >> 4]).append(HEX_DIGITS[b & 0x0F]);
    }
    return buffer.toString();
  }

  /**
   * The {@link NullOutputStream} implements an {@link OutputStream} to the null
   * device.
   */
  private static class NullOutputStream extends OutputStream {

    @Override
    public void write(byte[] arg0, int arg1, int arg2) throws IOException {}

    @Override
    public void write(byte[] arg0) throws IOException {}

    @Override
    public void write(int arg0) throws IOException {}
  }
}
