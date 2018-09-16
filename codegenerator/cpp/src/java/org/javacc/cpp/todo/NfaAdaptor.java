
package org.javacc.cpp.todo;

import org.javacc.parser.CharacterRange;
import org.javacc.parser.JavaCCErrors;
import org.javacc.parser.Options;
import org.javacc.parser.RCharacterList;
import org.javacc.parser.RChoice;
import org.javacc.parser.REndOfFile;
import org.javacc.parser.RJustName;
import org.javacc.parser.ROneOrMore;
import org.javacc.parser.RRepetitionRange;
import org.javacc.parser.RSequence;
import org.javacc.parser.RStringLiteral;
import org.javacc.parser.RZeroOrMore;
import org.javacc.parser.RZeroOrOne;
import org.javacc.parser.RegularExpression;
import org.javacc.parser.SingleCharacter;

import java.util.ArrayList;
import java.util.List;

public class NfaAdaptor {

  public static boolean transformed = false;


  public static Nfa toNfa(boolean ignoreCase, RegularExpression re) {
    if(re instanceof RCharacterList)
      return generateRCharacterList(ignoreCase, (RCharacterList) re);
    else if (re instanceof RChoice)
      return generateRChoice(ignoreCase, (RChoice) re);
    else if (re instanceof REndOfFile)
      return generateREndOfFile(ignoreCase, (REndOfFile) re);
    else if (re instanceof RJustName)
      return generateRJustName(ignoreCase, (RJustName) re);
    else if (re instanceof ROneOrMore)
      return generateROneOrMore(ignoreCase, (ROneOrMore) re);
    else if (re instanceof RRepetitionRange)
      return generateRRepetitionRange(ignoreCase, (RRepetitionRange) re);
    else if (re instanceof RSequence)
      return generateRSequence(ignoreCase, (RSequence) re);
    else if (re instanceof RStringLiteral)
      return generateRStringLiteral(ignoreCase, (RStringLiteral) re);
    else if (re instanceof RZeroOrMore)
      return generateRZeroOrMore(ignoreCase, (RZeroOrMore) re);
    else if (re instanceof RZeroOrOne)
      return generateRZeroOrOne(ignoreCase, (RZeroOrOne) re);
    else
      return null;
  }

  public static Nfa generateRCharacterList(boolean ignoreCase, RCharacterList re) {
    if (!transformed) {
      if (Options.getIgnoreCase() || ignoreCase) {
        re.ToCaseNeutral();
        re.SortDescriptors();

      }

      if (re.negated_list)
        re.RemoveNegation(); // This also sorts the list
      else
        re.SortDescriptors();
    }

    if (re.descriptors.size() == 0 && !re.negated_list) {
      JavaCCErrors.semantic_error(re, "Empty character set is not allowed as it will not match any character.");
      return new Nfa();
    }

    transformed = true;
    Nfa retVal = new Nfa();
    NfaState startState = retVal.start;
    NfaState finalState = retVal.end;
    int i;

    for (i = 0; i < re.descriptors.size(); i++) {
      if (re.descriptors.get(i) instanceof SingleCharacter)
        startState.AddChar(((SingleCharacter) re.descriptors.get(i)).ch);
      else // if (descriptors.get(i) instanceof CharacterRange)
      {
        CharacterRange cr = (CharacterRange) re.descriptors.get(i);

        if (cr.getLeft() == cr.getRight())
          startState.AddChar(cr.getLeft());
        else
          startState.AddRange(cr.getLeft(), cr.getRight());
      }
    }

    startState.next = finalState;
    return retVal;
  }

  public static Nfa generateRChoice(boolean ignoreCase, RChoice re) {
    re.CompressCharLists();

    if (re.getChoices().size() == 1)
       return toNfa(ignoreCase, re.getChoices().get(0));

    Nfa retVal = new Nfa();
    NfaState startState = retVal.start;
    NfaState finalState = retVal.end;

    for (int i = 0; i < re.getChoices().size(); i++)
    {
       Nfa temp;
       RegularExpression curRE = re.getChoices().get(i);

       temp = toNfa(ignoreCase, curRE);

       startState.AddMove(temp.start);
       temp.end.AddMove(finalState);
    }

    return retVal;
  }

  public static Nfa generateREndOfFile(boolean ignoreCase, REndOfFile re) {
    return null;
  }

  public static Nfa generateRJustName(boolean ignoreCase, RJustName re) {
    return toNfa(ignoreCase, re.regexpr);
  }

  public static Nfa generateROneOrMore(boolean ignoreCase, ROneOrMore re) {
    Nfa retVal = new Nfa();
    NfaState startState = retVal.start;
    NfaState finalState = retVal.end;

    Nfa temp = toNfa(ignoreCase, re.regexpr);

    startState.AddMove(temp.start);
    temp.end.AddMove(temp.start);
    temp.end.AddMove(finalState);

    return retVal;
  }

  public static Nfa generateRRepetitionRange(boolean ignoreCase, RRepetitionRange re) {

    List<RegularExpression> units = new ArrayList<>();
    RSequence seq;
    int i;

    for (i = 0; i < re.min; i++)
    {
       units.add(re.regexpr);
    }

    if (re.hasMax && re.max == -1) // Unlimited
    {
       RZeroOrMore zoo = new RZeroOrMore();
       zoo.regexpr = re.regexpr;
       units.add(zoo);
    }

    while (i++ < re.max)
    {
       RZeroOrOne zoo = new RZeroOrOne();
       zoo.regexpr = re.regexpr;
       units.add(zoo);
    }
    seq = new RSequence(units);
    return toNfa(ignoreCase, seq);
  }

  public static Nfa generateRSequence(boolean ignoreCase, RSequence re) {
    if (re.units.size() == 1)
      return toNfa(ignoreCase, re.units.get(0));

   Nfa retVal = new Nfa();
   NfaState startState = retVal.start;
   NfaState finalState = retVal.end;
   Nfa temp1;
   Nfa temp2 = null;

   RegularExpression curRE;

   curRE = re.units.get(0);
   temp1 = toNfa(ignoreCase, curRE);
   startState.AddMove(temp1.start);

   for (int i = 1; i < re.units.size(); i++)
   {
      curRE = re.units.get(i);

      temp2 = toNfa(ignoreCase, curRE);
      temp1.end.AddMove(temp2.start);
      temp1 = temp2;
   }

   temp2.end.AddMove(finalState);

   return retVal;
  }


  public static Nfa generateRStringLiteral(boolean ignoreCase, RStringLiteral re) {
    if (re.image.length() == 1) {
      RCharacterList temp = new RCharacterList(re.image.charAt(0));
      return generateRCharacterList(ignoreCase, temp);
    }

    NfaState startState = new NfaState();
    NfaState theStartState = startState;
    NfaState finalState = null;

    if (re.image.length() == 0)
      return new Nfa(theStartState, theStartState);

    int i;

    for (i = 0; i < re.image.length(); i++) {
      finalState = new NfaState();
      startState.charMoves = new char[1];
      startState.AddChar(re.image.charAt(i));

      if (Options.getIgnoreCase() || ignoreCase) {
        startState.AddChar(Character.toLowerCase(re.image.charAt(i)));
        startState.AddChar(Character.toUpperCase(re.image.charAt(i)));
      }

      startState.next = finalState;
      startState = finalState;
    }

    return new Nfa(theStartState, finalState);
  }

  public static Nfa generateRZeroOrMore(boolean ignoreCase, RZeroOrMore re) {
    Nfa retVal = new Nfa();
    NfaState startState = retVal.start;
    NfaState finalState = retVal.end;

    Nfa temp = toNfa(ignoreCase, re.regexpr);

    startState.AddMove(temp.start);
    startState.AddMove(finalState);
    temp.end.AddMove(finalState);
    temp.end.AddMove(temp.start);

    return retVal;
  }

  public static Nfa generateRZeroOrOne(boolean ignoreCase, RZeroOrOne re) {
    Nfa retVal = new Nfa();
    NfaState startState = retVal.start;
    NfaState finalState = retVal.end;

    Nfa temp = toNfa(ignoreCase, re.regexpr);

    startState.AddMove(temp.start);
    startState.AddMove(finalState);
    temp.end.AddMove(finalState);

    return retVal;
  }
}
