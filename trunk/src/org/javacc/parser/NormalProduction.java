/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
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
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.javacc.parser;

import java.util.Set;

/**
 * Describes JavaCC productions.
 */

public class NormalProduction {

  /**
   * The line and column number of the construct that corresponds
   * most closely to this node.
   */
  public int line, column;

  /**
   * The NonTerminal nodes which refer to this production.
   */
  java.util.List parents = new java.util.ArrayList();

  /**
   * The access modifier of this production.
   */
  public String accessMod;

  /**
   * The name of the non-terminal of this production.
   */
  public String lhs;

  /**
   * The tokens that make up the return type of this production.
   */
  public java.util.List return_type_tokens = new java.util.ArrayList();

  /**
   * The tokens that make up the parameters of this production.
   */
  public java.util.List parameter_list_tokens = new java.util.ArrayList();

  /**
   * Each entry in this list is a list of tokens that represents an
   * exception in the throws list of this production.  This list does not
   * include ParseException which is always thrown.
   */
  public java.util.List throws_list = new java.util.ArrayList();

  /**
   * The RHS of this production.  Not used for JavaCodeProduction.
   */
  public Expansion expansion;

  /**
   * This boolean flag is true if this production can expand to empty.
   */
  boolean emptyPossible = false;

  /**
   * A list of all non-terminals that this one can expand to without
   * having to consume any tokens.  Also an index that shows how many
   * pointers exist.
   */
  NormalProduction[] leftExpansions = new NormalProduction[10];
  int leIndex = 0;

  /**
   * The following variable is used to maintain state information for the
   * left-recursion determination algorithm:  It is initialized to 0, and
   * set to -1 if this node has been visited in a pre-order walk, and then
   * it is set to 1 if the pre-order walk of the whole graph from this
   * node has been traversed.  i.e., -1 indicates partially processed,
   * and 1 indicates fully processed.
   */
  int walkStatus = 0;

  /**
   * The first and last tokens from the input stream that represent this
   * production.
   */
  public Token firstToken, lastToken;

  protected String eol = System.getProperty("line.separator", "\n");
  protected StringBuffer dumpPrefix(int indent) {
    StringBuffer sb = new StringBuffer(128);
    for (int i = 0; i < indent; i++)
      sb.append("  ");
    return sb;
  }

  protected String getSimpleName() {
    String name = getClass().getName();
    return name.substring(name.lastIndexOf(".")+1); // strip the package name
  }

  public StringBuffer dump(int indent, Set alreadyDumped) {
    StringBuffer sb = dumpPrefix(indent).append(System.identityHashCode(this)).append(' ').append(getSimpleName()).append(' ').append(lhs);
    if (!alreadyDumped.contains(this))
    {
      alreadyDumped.add(this);
      if (expansion != null)
      {
        sb.append(eol).append(expansion.dump(indent + 1, alreadyDumped));
      }
    }

    return sb;
  }

}
