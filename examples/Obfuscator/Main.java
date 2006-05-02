
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


import java.io.*;
import java.util.*;

public class Main extends Globals {

  /*
   * This main program collects the input arguments.  If one or more of the three
   * latter arguments are present, then these files are opened and parsed with the
   * appropriate parsers.  Then the method Obfuscator.start() is called that does
   * the actual obfuscation.  Finally, the file map.log is generated.
   */
  public static void main(String[] args) throws FileNotFoundException {
    IdsFile idparser = null;
    if (args.length < 2 || args.length > 5) {
      System.out.println("Usage is \"java Main <inputdir> <outputdir> <mapsfile> <nochangeidsfile> <useidsfile>\"");
      System.out.println("  <inputdir> must be the CLASSPATH directory.");
      System.out.println("  <mapsfile>, <nochangeidsfile>, and <useidsfile> are optional, but if any of");
      System.out.println("  these are present, then the ones preceding them must also be present.");
      return;
    }
    inpDir = new File(args[0]);
    if (!inpDir.isDirectory()) {
      System.out.println("Error: " + args[0] + " is not a directory.");
      return;
    }
    outDir = new File(args[1]);
    if (outDir.exists()) {
      if (!outDir.isDirectory()) {
        System.out.println("Error: " + args[1] + " is not a directory.");
        return;
      }
    } else {
      System.out.println(args[1] + " does not exist.  Will create it.");
      if (!outDir.mkdirs()) {
        System.out.println("Could not create directory " + args[1]);
        return;
      }
    }
    if (args.length >= 4) {
      try {
        idparser = new IdsFile(new FileInputStream(args[3]));
        idparser.input(true, args[3]);
      } catch (ParseException e) {
        System.out.println("Parse error in " + args[3]);
        return;
      }
    }
    if (args.length >= 3) {
      try {
        MapFile mapparser = new MapFile(new FileInputStream(args[2]));
        mapparser.input();
      } catch (ParseException e) {
        System.out.println("Parse error in " + args[2]);
        return;
      }
    }
    if (args.length == 5) {
      try {
        idparser.ReInit(new FileInputStream(args[4]));
        idparser.input(false, args[4]);
      } catch (ParseException e) {
        System.out.println("Parse error in " + args[4]);
        return;
      }
    }
    mappings.put("main", "main");
    Obfuscator.start();
    System.out.println("Dumping mappings used into map.log.");
    PrintWriter mstr;
    try {
      mstr = new PrintWriter(new FileWriter("map.log"));
    } catch (IOException e) {
      System.out.println("Could not create file map.log");
      throw new Error();
    }
    for (Enumeration enumeration = mappings.keys(); enumeration.hasMoreElements();) {
      String from = (String)enumeration.nextElement();
      String to = (String)mappings.get(from);
      mstr.println(from + " -> " + to + ";");
    }
    mstr.close();
  }

}
