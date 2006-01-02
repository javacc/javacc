public class MyNode
{
  /** Symbol table */
  protected static java.util.Hashtable symtab = new java.util.Hashtable();

  /** Stack for calculations. */
  protected static Object[] stack = new Object[1024];
  protected static int top = -1;

  public void interpret()
  {
     throw new UnsupportedOperationException(); // It better not come here.
  }
}
