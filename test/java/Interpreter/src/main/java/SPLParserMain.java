/** Stupid Programming Language parser. */
public class SPLParserMain {

  /**
   * Returns the root node of the AST.  
   * It only makes sense to call this after a successful parse. 
   * @return the root node
   */
  public Node rootNode() {
    return SPLParser.jjtree.rootNode();
  }  

}
