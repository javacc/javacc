/* Generated By:JJTree: Do not edit this line. ASTGrammar.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.javacc.jjtree;

public
class ASTGrammar extends JJTreeNode {
  public ASTGrammar(int id) {
    super(id);
  }

  public ASTGrammar(JJTreeParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(JJTreeParserVisitor visitor, Object data) {
    return visitor.visitASTGrammar(this, data);
  }
}
/* JavaCC - OriginalChecksum=20b1cab7291b4fe38f26909640aa808e (do not edit this line) */
