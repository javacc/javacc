
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

public class Globals {

  // The mappings from old id's to new id's.
  static Hashtable mappings = new Hashtable();

  // A table of targets of all known mappings.
  static Hashtable mapTargets = new Hashtable();

  // These id's may not be changed.
  static Hashtable noChangeIds = new Hashtable();

  // These id's should be used for mappings.
  static Hashtable useIds = new Hashtable();

  // The location of the input and output directories.
  static File inpDir, outDir;

  // Set to true by Java parser if class has a main program.
  static boolean mainExists;

  // Returns the map of old to obfuscated id.  If map does not
  // exist, it is created.
  static String map(String str) {
    Object obj = mappings.get(str);
    if (obj != null) {
      return (String)obj;
    }
    if (useIds.isEmpty()) {
      String newId = "O0" + counter++;
      mappings.put(str, newId);
      return newId;
    } else {
      obj = useIds.keys().nextElement();
      useIds.remove(obj);
      String newId = (String)obj;
      mappings.put(str, newId);
      return newId;
    }
  }

  // A counter used to generate new identifiers
  static int counter = 0;

}
