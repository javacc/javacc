package c_jjtree_examples.java.eg3;

/**
 * An ID.
 */
public class ASTMyID extends SimpleNode {
  private String name;

  /**
   * Constructor.
   * @param id the id
   */
  public ASTMyID(int id) {
    super(id);
  }


  /**
   * Set the name.
   * @param n the name
   */
  public void setName(String n) {
    name = n;
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.examples.jjtree.eg2.SimpleNode#toString()
   */
  public String toString() {
    return "Identifier: " + name;
  }

}
