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
import java.util.Vector;

/**
 * Describes regular expressions which are choices from
 * from among included regular expressions.
 */

public class RChoice extends RegularExpression {

  /**
   * The list of choices of this regular expression.  Each
   * Vector component will narrow to RegularExpression.
   */
  public java.util.Vector choices = new java.util.Vector();

  public Nfa GenerateNfa(boolean ignoreCase)
  {
     CompressCharLists();

     if (choices.size() == 1)
        return ((RegularExpression)choices.elementAt(0)).GenerateNfa(ignoreCase);

     Nfa retVal = new Nfa();
     NfaState startState = retVal.start;
     NfaState finalState = retVal.end;

     for (int i = 0; i < choices.size(); i++)
     {
        Nfa temp;
        RegularExpression curRE = (RegularExpression)choices.elementAt(i);

        temp = curRE.GenerateNfa(ignoreCase);

        startState.AddMove(temp.start);
        temp.end.AddMove(finalState);
     }

     return retVal;
  }

  void CompressCharLists()
  { 
     CompressChoices(); // Unroll nested choices
     RegularExpression curRE;
     RCharacterList curCharList = null;

     for (int i = 0; i < choices.size(); i++)
     {
        curRE = (RegularExpression)choices.elementAt(i);;

        while (curRE instanceof RJustName)
           curRE = ((RJustName)curRE).regexpr;

        if (curRE instanceof RStringLiteral &&
            ((RStringLiteral)curRE).image.length() == 1)
           choices.setElementAt(curRE = new RCharacterList(
                      ((RStringLiteral)curRE).image.charAt(0)), i);

        if (curRE instanceof RCharacterList)
        {
           if (((RCharacterList)curRE).negated_list)
              ((RCharacterList)curRE).RemoveNegation();

           Vector tmp = ((RCharacterList)curRE).descriptors;

           if (curCharList == null)
              choices.setElementAt(curRE = curCharList =
                                    new RCharacterList(), i);
           else
              choices.removeElementAt(i--);

           for (int j = tmp.size(); j-- > 0;)
              curCharList.descriptors.addElement(tmp.elementAt(j));
         }

     }
  }

  void CompressChoices()
  {
     RegularExpression curRE;

     for (int i = 0; i < choices.size(); i++)
     {
        curRE = (RegularExpression)choices.elementAt(i);

        while (curRE instanceof RJustName)
           curRE = ((RJustName)curRE).regexpr;

        if (curRE instanceof RChoice)
        {
           choices.removeElementAt(i--);
           for (int j = ((RChoice)curRE).choices.size(); j-- > 0;)
              choices.addElement(((RChoice)curRE).choices.elementAt(j));
        }
     }
  }

  public void CheckUnmatchability()
  {
     RegularExpression curRE;
     int numStrings = 0;

     for (int i = 0; i < choices.size(); i++)
     {
        if (!(curRE = (RegularExpression)choices.elementAt(i)).private_rexp &&
            //curRE instanceof RJustName &&
            curRE.ordinal > 0 && curRE.ordinal < ordinal &&
            LexGen.lexStates[curRE.ordinal] == LexGen.lexStates[ordinal])
        {
           if (label != null)
              JavaCCErrors.warning(this, "Regular Expression choice : " +
                 curRE.label + " can never be matched as : " + label);
           else
              JavaCCErrors.warning(this, "Regular Expression choice : " +
                 curRE.label + " can never be matched as token of kind : " +
                                                                      ordinal);
        }

        if (!curRE.private_rexp && curRE instanceof RStringLiteral)
           numStrings++;
     }
  }

}
