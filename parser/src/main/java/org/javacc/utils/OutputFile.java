/*
 * Copyright (c) 2007, Paul Cager. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
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
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.javacc.utils;

import org.javacc.Version;
import org.javacc.parser.JavaCCGlobals;
import org.javacc.parser.Options;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.DigestOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * This class handles the creation and maintenance of the boiler-plate classes,
 * such as Token.java, JavaCharStream.java etc.
 *
 * It is responsible for:
 *
 * <ul> <li>Writing the JavaCC header lines to the file.</li> <li>Writing the
 * checksum line.</li> <li>Using the checksum to determine if an existing file
 * has been changed by the user (and so should be left alone).</li> <li>Checking
 * any existing file's version (if the file can not be overwritten).</li>
 * <li>Checking any existing file's creation options (if the file can not be
 * overwritten).</li> <li></li> </ul>
 *
 * @author Paul Cager
 *
 */
class OutputFile implements Closeable {

  private final File         file;
  private final List<String> options;

  private final String       toolName;
  private final String       compatibleVersion;
  private final boolean      needToWrite;


  private TrapClosePrintWriter writer;
  private DigestOutputStream   digestStream;

  /**
   * Create a new OutputFile.
   *
   * @param file the file to write to.
   * @param toolName the name of the generating tool.
   * @param compatibleVersion the minimum compatible JavaCC version.
   * @param options if the file already exists, and cannot be overwritten, this
   *        is a list of options (such s STATIC=false) to check for changes.
   * @throws IOException
   */
  OutputFile(File file, String toolName, String compatibleVersion, List<String> options) throws IOException {
    this.file = file;
    this.options = options;
    this.compatibleVersion = compatibleVersion;
    this.toolName = toolName;
    this.needToWrite = OutputFileDigest.check(file, toolName, compatibleVersion, options);
  }

  /**
   * Return <code>true</code> if the file needs to be written.
   */
  public final boolean isNeedToWrite() {
    return needToWrite;
  }

  /**
   * Return a PrintWriter object that may be used to write to this file. Any
   * necessary header information is written by this method.
   *
   * @throws IOException
   */
  public final PrintWriter getPrintWriter() throws IOException {
    if (writer == null) {
      try {
        String version = this.compatibleVersion == null ? Version.fullVersion : this.compatibleVersion;
        OutputStream ostream = new BufferedOutputStream(new FileOutputStream(file));

        this.digestStream = OutputFileDigest.getDigestStream(ostream);
        this.writer = new TrapClosePrintWriter(this.digestStream);
        this.writer
            .println("/* " + JavaCCGlobals.getIdString(this.toolName, file.getName()) + " Version " + version + " */");
        if (!this.options.isEmpty()) {
          this.writer.println(
              "/* JavaCCOptions:" + Options.getOptionsString(this.options.toArray(new String[options.size()])) + " */");
        }
      } catch (NoSuchAlgorithmException e) {
        throw (IOException) new IOException("No MD5 implementation").initCause(e);
      }
    }
    return writer;
  }

  /**
   * Close the OutputFile, writing any necessary trailer information (such as a
   * checksum).
   * 
   * @throws IOException
   */
  @Override
  public final void close() throws IOException {
    // Write the trailer (checksum).
    // Possibly rename the .java.tmp to .java??
    if (writer != null) {
      writer.flush();
      writer.println(OutputFileDigest.getDigestLine(digestStream));
      writer.closePrintWriter();
    }
  }

  /**
   * The {@link TrapClosePrintWriter} implements a {@link PrintWriter}, avoiding
   * to close the related {@link OutputStream} with an {@link #close()}.
   */
  private class TrapClosePrintWriter extends PrintWriter {

    public TrapClosePrintWriter(OutputStream os) {
      super(os);
    }

    public void closePrintWriter() {
      super.close();
    }

    @Override
    public void close() {
      try {
        OutputFile.this.close();
      } catch (IOException e) {
        System.err.println("Could not close " + file.getAbsolutePath());
      }
    }
  }
}
