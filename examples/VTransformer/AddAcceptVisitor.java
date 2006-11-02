
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
