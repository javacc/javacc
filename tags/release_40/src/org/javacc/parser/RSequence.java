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

package org.javacc.parser;

import java.util.*;

/**
 * Describes regular expressions which are sequences of
 * other regular expressions.
 */

public class RSequence extends RegularExpression {

  /**
   * The list of units in this regular expression sequence.  Each
   * Vector component will narrow to RegularExpression.
   */
  public java.util.Vector units = new java.util.Vector();

  public Nfa GenerateNfa(boolean ignoreCase)
  {
     if (units.size() == 1)
        return ((RegularExpression)units.elementAt(0)).GenerateNfa(ignoreCase);

     Nfa retVal = new Nfa();
     NfaState startState = retVal.start;
     NfaState finalState = retVal.end;
     Nfa temp1;
     Nfa temp2 = null;

     RegularExpression curRE;

     curRE = (RegularExpression)units.elementAt(0);
     temp1 = curRE.GenerateNfa(ignoreCase);
     startState.AddMove(temp1.start);

     for (int i = 1; i < units.size(); i++)
     {
        curRE = (RegularExpression)units.elementAt(i);

        temp2 = curRE.GenerateNfa(ignoreCase);
        temp1.end.AddMove(temp2.start);
        temp1 = temp2;
     }

     temp2.end.AddMove(finalState);

     return retVal;
  }

  RSequence()
  {
  }

  RSequence(Vector seq)
  {
     ordinal = Integer.MAX_VALUE;
     units = seq;
  }
}
