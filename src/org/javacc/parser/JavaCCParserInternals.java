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

public abstract class JavaCCParserInternals extends JavaCCGlobals {

  static protected void initialize() {
    Integer i = new Integer(0);
    lexstate_S2I.put("DEFAULT", i);
    lexstate_I2S.put(i, "DEFAULT");
    simple_tokens_table.put("DEFAULT", new java.util.Hashtable());
  }

  static protected void addcuname(String id) {
    cu_name = id;
  }

  static protected void compare(Token t, String id1, String id2) {
    if (!id2.equals(id1)) {
      JavaCCErrors.parse_error(t, "Name " + id2 + " must be the same as that used at PARSER_BEGIN (" + id1 + ")");
    }
  }

  static private java.util.Vector add_cu_token_here = cu_to_insertion_point_1;
  static private Token first_cu_token;
  static private boolean insertionpoint1set = false;
  static private boolean insertionpoint2set = false;

  static protected void setinsertionpoint(Token t, int no) {
    do {
      add_cu_token_here.addElement(first_cu_token);
      first_cu_token = first_cu_token.next;
    } while (first_cu_token != t);
    if (no == 1) {
      if (insertionpoint1set) {
        JavaCCErrors.parse_error(t, "Multiple declaration of parser class.");
      } else {
        insertionpoint1set = true;
        add_cu_token_here = cu_to_insertion_point_2;
      }
    } else {
      add_cu_token_here = cu_from_insertion_point_2;
      insertionpoint2set = true;
    }
    first_cu_token = t;
  }

  static protected void insertionpointerrors(Token t) {
    while (first_cu_token != t) {
      add_cu_token_here.addElement(first_cu_token);
      first_cu_token = first_cu_token.next;
    }
    if (!insertionpoint1set || !insertionpoint2set) {
      JavaCCErrors.parse_error(t, "Parser class has not been defined between PARSER_BEGIN and PARSER_END.");
    }
  }

  static protected void set_initial_cu_token(Token t) {
    first_cu_token = t;
  }

  static protected void addproduction(NormalProduction p) {
    bnfproductions.addElement(p);
  }

  static protected void production_addexpansion(BNFProduction p, Expansion e) {
    e.parent = p;
    p.expansion = e;
  }

  static private int nextFreeLexState = 1;

  static protected void addregexpr(TokenProduction p) {
    Integer ii;
    rexprlist.addElement(p);
    if (Options.B("USER_TOKEN_MANAGER")) {
      if (p.lexStates == null || p.lexStates.length != 1 || !p.lexStates[0].equals("DEFAULT")) {
        JavaCCErrors.warning(p, "Ignoring lexical state specifications since option USER_TOKEN_MANAGER has been set to true.");
      }
    }
    if (p.lexStates == null) {
      return;
    }
    for (int i = 0; i < p.lexStates.length; i++) {
      for (int j = 0; j < i; j++) {
        if (p.lexStates[i].equals(p.lexStates[j])) {
          JavaCCErrors.parse_error(p, "Multiple occurrence of \"" + p.lexStates[i] + "\" in lexical state list.");
        }
      }
      if (lexstate_S2I.get(p.lexStates[i]) == null) {
        ii = new Integer(nextFreeLexState++);
        lexstate_S2I.put(p.lexStates[i], ii);
        lexstate_I2S.put(ii, p.lexStates[i]);
        simple_tokens_table.put(p.lexStates[i], new java.util.Hashtable());
      }
    }
  }

  static protected void add_token_manager_decls(Token t, java.util.Vector decls) {
    if (token_mgr_decls != null) {
      JavaCCErrors.parse_error(t, "Multiple occurrence of \"TOKEN_MGR_DECLS\".");
    } else {
      token_mgr_decls = decls;
      if (Options.B("USER_TOKEN_MANAGER")) {
        JavaCCErrors.warning(t, "Ignoring declarations in \"TOKEN_MGR_DECLS\" since option USER_TOKEN_MANAGER has been set to true.");
      }
    }
  }

  static protected void add_inline_regexpr(RegularExpression r) {
    if (!(r instanceof REndOfFile)) {
      TokenProduction p = new TokenProduction();
      p.isExplicit = false;
      p.lexStates = new String[1];
      p.lexStates[0] = "DEFAULT";
      p.kind = TokenProduction.TOKEN;
      RegExprSpec res = new RegExprSpec();
      res.rexp = r;
      res.rexp.tpContext = p;
      res.act = new Action();
      res.nextState = null;
      res.nsTok = null;
      p.respecs.addElement(res);
      rexprlist.addElement(p);
    }
  }

  static protected boolean hexchar(char ch) {
    if (ch >= '0' && ch <= '9') return true;
    if (ch >= 'A' && ch <= 'F') return true;
    if (ch >= 'a' && ch <= 'f') return true;
    return false;
  }

  static protected int hexval(char ch) {
    if (ch >= '0' && ch <= '9') return ((int)ch) - ((int)'0');
    if (ch >= 'A' && ch <= 'F') return ((int)ch) - ((int)'A') + 10;
    return ((int)ch) - ((int)'a') + 10;
  }

  static protected String remove_escapes_and_quotes(Token t, String str) {
    String retval = "";
    int index = 1;
    char ch, ch1;
    int ordinal;
    while (index < str.length()-1) {
      if (str.charAt(index) != '\\') {
        retval += str.charAt(index); index++;
        continue;
      }
      index++;
      ch = str.charAt(index);
      if (ch == 'b') {
        retval += '\b'; index++;
        continue;
      }
      if (ch == 't') {
        retval += '\t'; index++;
        continue;
      }
      if (ch == 'n') {
        retval += '\n'; index++;
        continue;
      }
      if (ch == 'f') {
        retval += '\f'; index++;
        continue;
      }
      if (ch == 'r') {
        retval += '\r'; index++;
        continue;
      }
      if (ch == '"') {
        retval += '\"'; index++;
        continue;
      }
      if (ch == '\'') {
        retval += '\''; index++;
        continue;
      }
      if (ch == '\\') {
        retval += '\\'; index++;
        continue;
      }
      if (ch >= '0' && ch <= '7') {
        ordinal = ((int)ch) - ((int)'0'); index++;
        ch1 = str.charAt(index);
        if (ch1 >= '0' && ch1 <= '7') {
          ordinal = ordinal*8 + ((int)ch1) - ((int)'0'); index++;
          ch1 = str.charAt(index);
          if (ch <= '3' && ch1 >= '0' && ch1 <= '7') {
            ordinal = ordinal*8 + ((int)ch1) - ((int)'0'); index++;
          }
        }
        retval += (char)ordinal;
        continue;
      }
      if (ch == 'u') {
        index++; ch = str.charAt(index);
        if (hexchar(ch)) {
          ordinal = hexval(ch);
          index++; ch = str.charAt(index);
          if (hexchar(ch)) {
            ordinal = ordinal*16 + hexval(ch);
            index++; ch = str.charAt(index);
            if (hexchar(ch)) {
              ordinal = ordinal*16 + hexval(ch);
              index++; ch = str.charAt(index);
              if (hexchar(ch)) {
                ordinal = ordinal*16 + hexval(ch);
                index++;
                continue;
              }
            }
          }
        }
        JavaCCErrors.parse_error(t, "Encountered non-hex character '" + ch + "' at position " + index + " of string - Unicode escape must have 4 hex digits after it.");
        return retval;
      }
      JavaCCErrors.parse_error(t, "Illegal escape sequence '\\" + ch + "' at position " + index + " of string.");
      return retval;
    }
    return retval;
  }

  static protected char character_descriptor_assign(Token t, String s) {
    if (s.length() != 1) {
      JavaCCErrors.parse_error(t, "String in character list may contain only one character.");
      return ' ';
    } else {
      return s.charAt(0);
    }
  }

  static protected char character_descriptor_assign(Token t, String s, String left) {
    if (s.length() != 1) {
      JavaCCErrors.parse_error(t, "String in character list may contain only one character.");
      return ' ';
    } else if ((int)(left.charAt(0)) > (int)(s.charAt(0))) {
      JavaCCErrors.parse_error(t, "Right end of character range \'" + s + "\' has a lower ordinal value than the left end of character range \'" + left + "\'.");
      return left.charAt(0);
    } else {
      return s.charAt(0);
    }
  }

  static protected void makeTryBlock(
    Token tryLoc,
    Container result,
    Container nestedExp,
    java.util.Vector types,
    java.util.Vector ids,
    java.util.Vector catchblks,
    java.util.Vector finallyblk
  )
  {
    if (catchblks.size() == 0 && finallyblk == null) {
      JavaCCErrors.parse_error(tryLoc, "Try block must contain at least one catch or finally block.");
      result = nestedExp;
      return;
    }
    TryBlock tblk = new TryBlock();
    tblk.line = tryLoc.beginLine;
    tblk.column = tryLoc.beginColumn;
    tblk.exp = (Expansion)(nestedExp.member);
    tblk.exp.parent = tblk;
    tblk.exp.ordinal = 0;
    tblk.types = types;
    tblk.ids = ids;
    tblk.catchblks = catchblks;
    tblk.finallyblk = finallyblk;
    result.member = tblk;
  }

   public static void reInit()
   {
      add_cu_token_here = cu_to_insertion_point_1;
      first_cu_token = null;
      insertionpoint1set = false;
      insertionpoint2set = false;
      nextFreeLexState = 1;
   }

}
