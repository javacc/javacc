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

package org.javacc.jjtree;

public class SimpleNode implements Node {
  private Node parent;
  private Node[] children;
  private int id;
  private int myOrdinal;
  
  public SimpleNode(int i) {
    id = i;
  }

  public SimpleNode(JJTreeParser p, int i) {
    this(i);
  }

  public void jjtOpen() {}
  public void jjtClose() {}
  
  public void jjtSetParent(Node n) { parent = n; }
  public Node jjtGetParent() { return parent; }

  public void jjtAddChild(Node n, int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
    ((SimpleNode)n).setOrdinal(i);
  }

  public Node jjtGetChild(int i) {
    return children[i];
  }

  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */

  public String toString() { return JJTreeParserTreeConstants.jjtNodeName[id]; }
  public String toString(String prefix) { return prefix + toString(); }

  /* Override this method if you want to customize how the node dumps
     out its children. */

  public void dump(String prefix) {
    System.out.println(toString(prefix));
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
	SimpleNode n = (SimpleNode)children[i];
	if (n != null) {
	  n.dump(prefix + " ");
	}
      }
    }
  }

  public int getOrdinal()
  {
    return myOrdinal;
  }

  public void setOrdinal(int o)
  {
    myOrdinal = o;
  }


  /*****************************************************************
   *
   * The following is added manually to enhance all tree nodes with
   * attributes that store the first and last tokens corresponding to
   * each node, as well as to print the tokens back to the specified
   * output stream.
   *
   *****************************************************************/
  
  private Token first, last;
  
  public Token getFirstToken() { return first; }
  public void setFirstToken(Token t) { first = t; }
  public Token getLastToken() { return last;  }
  public void setLastToken(Token t) { last = t; }
  
  /* This method prints the tokens corresponding to this node
     recursively calling the print methods of its children.
     Overriding this print method in appropriate nodes gives the
     output the added stuff not in the input.  */

  public void print(IO io) {
    /* Some productions do not consume any tokens.  In that case their
       first and last tokens are a bit strange. */
    if (getLastToken().next == getFirstToken()) {
      return;
    }

    Token t1 = getFirstToken();
    Token t = new Token();
    t.next = t1;
    SimpleNode n;
    for (int ord = 0; ord < jjtGetNumChildren(); ord++) {
      n = (SimpleNode)jjtGetChild(ord);
      while (true) {
	t = t.next;
	if (t == n.getFirstToken()) break;
	print(t, io);
      }
      n.print(io);
      t = n.getLastToken();
    }
    while (t != getLastToken()) {
      t = t.next;
      print(t, io);
    }
  }
  
  
  String translateImage(Token t)
  {
    return t.image;
  }
  
  String whiteOut(Token t)
  {
    String s = "";

    for (int i = 0; i < t.image.length(); ++i) {
      s += " ";
    }
    return s;
  }



  /* Indicates whether the token should be replaced by white space or
     replaced with the actual node variable. */
  private boolean whitingOut = false;

  protected void print(Token t, IO io) {
    Token tt = t.specialToken;
    if (tt != null) {
      while (tt.specialToken != null) tt = tt.specialToken;
      while (tt != null) {
	io.print(addUnicodeEscapes(translateImage(tt)));
	tt = tt.next;
      }
    }

    /* If we're within a node scope we modify the source in the
       following ways:

       1) we rename all references to `jjtThis' to be references to
       the actual node variable.

       2) we replace all calls to `jjtree.currentNode()' with
       references to the node variable. */

    NodeScope s = NodeScope.getEnclosingNodeScope(this);
    if (s == null) {
      /* Not within a node scope so we don't need to modify the
         source. */
      io.print(addUnicodeEscapes(translateImage(t)));
      return;
    }

    if (t.image.equals("jjtThis")) {
      io.print(s.getNodeVariable());
      return;
    } else if (t.image.equals("jjtree")) {
      if (t.next.image.equals(".")) {
	if (t.next.next.image.equals("currentNode")) {
	  if (t.next.next.next.image.equals("(")) {
	    if (t.next.next.next.next.image.equals(")")) {
	      /* Found `jjtree.currentNode()' so go into white out
                 mode.  We'll stay in this mode until we find the
                 closing parenthesis. */
	      whitingOut = true;
	    }
	  }
	}
      }
    }
    if (whitingOut) {
      if (t.image.equals("jjtree")) {
	io.print(s.getNodeVariable());
	io.print(" ");
      } else if (t.image.equals(")")) {
	io.print(" ");
	whitingOut = false;
      } else {
	for (int i = 0; i < t.image.length(); ++i) {
	  io.print(" ");
	}
      }
      return;
    }
      
    io.print(addUnicodeEscapes(translateImage(t)));
  }


  protected String addUnicodeEscapes(String str) {
    String retval = "";
    char ch;
    for (int i = 0; i < str.length(); i++) {
      ch = str.charAt(i);
      if ((ch < 0x20 || ch > 0x7e) && ch != '\t' && ch != '\n' && ch != '\r' && ch != '\f') {
	String s = "0000" + Integer.toString(ch, 16);
	retval += "\\u" + s.substring(s.length() - 4, s.length());
      } else {
	retval += ch;
      }
    }
    return retval;
  }


  static void openJJTreeComment(IO io, String arg)
  {
    if (arg != null) {
      io.print("/*@bgen(jjtree) " + arg + " */");
    } else {
      io.print("/*@bgen(jjtree)*/");
    }
  }


  static void closeJJTreeComment(IO io)
  {
    io.print("/*@egen*/");
  }


  String getIndentation(SimpleNode n)
  {
    return getIndentation(n, 0);
  }


  String getIndentation(SimpleNode n, int offset)
  {
    String s = "";
    for (int i = offset + 1; i < n.getFirstToken().beginColumn; ++i) {
      s += " ";
    }
    return s;
  }

}

/*end*/
