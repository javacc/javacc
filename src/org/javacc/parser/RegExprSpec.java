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

/**
 * The object type of entries in the vector "respecs" of class
 * "TokenProduction".
 */

public class RegExprSpec {

  /**
   * The regular expression of this specification.
   */
  public RegularExpression rexp;

  /**
   * The action corresponding to this specification.
   */
  public Action act;

  /**
   * The next state corresponding to this specification.  If no
   * next state has been specified, this field is set to "null".
   */
  public String nextState;

  /**
   * If the next state specification was explicit in the previous
   * case, then this token is that of the identifier denoting
   * the next state.  This is used for location information, etc.
   * in error reporting.
   */
  public Token nsTok;

}
