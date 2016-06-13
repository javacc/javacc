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


//package VTransformer;

import java.io.PrintStream;

public class AddAcceptVisitor extends UnparseVisitor
{

  public AddAcceptVisitor(PrintStream o)
  {
    super(o);
  }


  public Object visit(ASTClassBodyDeclaration node, Object data)
  {
    /* Are we the first child of our parent? */
    if (node == node.jjtGetParent().jjtGetChild(0)) {

      /** Attempt to make the new code match the indentation of the
          node. */
      StringBuffer pre = new StringBuffer("");
      for (int i = 1; i < node.getFirstToken().beginColumn; ++i) {
	pre.append(' ');
      }

      out.println(pre + "");
      out.println(pre + "/** Accept the visitor. **/");
      out.println(pre + "public Object jjtAccept(JavaParserVisitor visitor, Object data) {");
      out.println(pre + "  return visitor.visit(this, data);");
      out.println(pre + "}");
    }
    return super.visit(node, data);
  }

}
