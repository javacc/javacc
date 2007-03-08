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
package org.javacc.jjtree;

import java.util.Hashtable;
import java.util.Vector;

class JJTreeGlobals
{

  /**
   * This hashtable stores the JJTree options to distinguish them
   * from JavaCC options.  The values are not important, only the
   * keys.
   */
  static Hashtable jjtreeOptions = new Hashtable();

  static Vector toolList = new Vector();

  /**
   * Use this like className.
   **/
  public static String parserName;

  /**
   * The package that the parser lives in.  If the grammar doesn't
   * specify a package it is the empty string.
   **/
  public static String packageName = "";

  /** The <code>implements</code> token of the parser class.  If the
   * parser doesn't have one then it is the first "{" of the parser
   * class body.
   **/
  public static Token parserImplements;

  /** The first token of the parser class body (the <code>{</code>).
   * The JJTree state is inserted after this token.
   **/
  public static Token parserClassBodyStart;

  /**
   * This is mapping from production names to ASTProduction objects.
   **/
  static Hashtable productions = new Hashtable();

}

/*end*/
