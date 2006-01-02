
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


package VTransformer;

import java.io.PrintStream;

public class UnparseVisitor implements JavaParserVisitor
{

  protected PrintStream out;


  public UnparseVisitor(PrintStream o)
  {
    out = o;
  }


  public Object print(SimpleNode node, Object data) {
    Token t1 = node.getFirstToken();
    Token t = new Token();
    t.next = t1;

    SimpleNode n;
    for (int ord = 0; ord < node.jjtGetNumChildren(); ord++) {
      n = (SimpleNode)node.jjtGetChild(ord);
      while (true) {
	t = t.next;
	if (t == n.getFirstToken()) break;
	print(t);
      }
      n.jjtAccept(this, data);
      t = n.getLastToken();
    }

    while (t != node.getLastToken()) {
      t = t.next;
      print(t);
    }
    return data;
  }
  
  
  protected void print(Token t) {
    Token tt = t.specialToken;
    if (tt != null) {
      while (tt.specialToken != null) tt = tt.specialToken;
      while (tt != null) {
        out.print(addUnicodeEscapes(tt.image));
        tt = tt.next;
      }
    }
    out.print(addUnicodeEscapes(t.image));
  }


  private String addUnicodeEscapes(String str) {
    String retval = "";
    char ch;
    for (int i = 0; i < str.length(); i++) {
      ch = str.charAt(i);
      if ((ch < 0x20 || ch > 0x7e) &&
	  ch != '\t' && ch != '\n' && ch != '\r' && ch != '\f') {
  	String s = "0000" + Integer.toString(ch, 16);
  	retval += "\\u" + s.substring(s.length() - 4, s.length());
      } else {
        retval += ch;
      }
    }
    return retval;
  }


  public Object visit(SimpleNode node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTCompilationUnit node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPackageDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTImportDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTTypeDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTClassDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTUnmodifiedClassDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTClassBody node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTNestedClassDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTClassBodyDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTMethodDeclarationLookahead node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTInterfaceDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTNestedInterfaceDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTUnmodifiedInterfaceDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTInterfaceMemberDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTFieldDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTVariableDeclarator node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTVariableDeclaratorId node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTVariableInitializer node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTArrayInitializer node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTMethodDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTMethodDeclarator node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTFormalParameters node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTFormalParameter node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTConstructorDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTExplicitConstructorInvocation node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTInitializer node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTType node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPrimitiveType node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTResultType node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTName node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTNameList node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTAssignmentOperator node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTConditionalExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTConditionalOrExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTConditionalAndExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTInclusiveOrExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTExclusiveOrExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTAndExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTEqualityExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTInstanceOfExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTRelationalExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTShiftExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTAdditiveExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTMultiplicativeExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTUnaryExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPreIncrementExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPreDecrementExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTUnaryExpressionNotPlusMinus node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTCastLookahead node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPostfixExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTCastExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPrimaryExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPrimaryPrefix node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTPrimarySuffix node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTLiteral node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTBooleanLiteral node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTNullLiteral node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTArguments node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTArgumentList node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTAllocationExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTArrayDimsAndInits node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTLabeledStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTBlock node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTBlockStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTLocalVariableDeclaration node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTEmptyStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTStatementExpression node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTSwitchStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTSwitchLabel node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTIfStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTWhileStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTDoStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTForStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTForInit node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTStatementExpressionList node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTForUpdate node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTBreakStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTContinueStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTReturnStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTThrowStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTSynchronizedStatement node, Object data)
  {
    return print(node, data);
  }

  public Object visit(ASTTryStatement node, Object data)
  {
    return print(node, data);
  }

}
