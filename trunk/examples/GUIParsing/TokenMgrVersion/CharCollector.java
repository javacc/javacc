
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


public class CharCollector implements CharStream {

  int bufsize;
  int available;
  int tokenBegin;
  public int bufpos = -1;
  private char[] buffer;
  private int maxNextCharInd = 0;

  private final void ExpandBuff(boolean wrapAround)
  {
     char[] newbuffer = new char[bufsize + 2048];

     try
     {
        if (wrapAround)
        {
           System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
           System.arraycopy(buffer, 0, newbuffer, bufsize - tokenBegin, bufpos);
           buffer = newbuffer;
           maxNextCharInd = (bufpos += (bufsize - tokenBegin));
        }
        else
        {
           System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
           buffer = newbuffer;
           maxNextCharInd = (bufpos -= tokenBegin);
        }
     }
     catch (Throwable t)
     {
        System.out.println("Error : " + t.getClass().getName());
        throw new Error();
     }

     bufsize += 2048;
     available = bufsize;
     tokenBegin = 0;
  }

  private final void FillBuff()
  {
     if (maxNextCharInd == available)
     {
        if (available == bufsize)
        {
           if (tokenBegin > 2048)
           {
              bufpos = maxNextCharInd = 0;
              available = tokenBegin;
           }
           else if (tokenBegin < 0)
              bufpos = maxNextCharInd = 0;
           else
              ExpandBuff(false);
        }
        else if (available > tokenBegin)
           available = bufsize;
        else if ((tokenBegin - available) < 2048)
           ExpandBuff(true);
        else
           available = tokenBegin;
     }

     try {
       wait();
     } catch (InterruptedException willNotHappen) {
       throw new Error();
     }
  }

  /** 
   * Puts a character into the buffer.
   */
  synchronized public final void put(char c)
  {
     buffer[maxNextCharInd++] = c;
     notify();
  }

  public char BeginToken() throws java.io.IOException
  {
     tokenBegin = -1;
     char c = readChar();
     tokenBegin = bufpos;

     return c;
  }

  private int inBuf = 0;
  synchronized public final char readChar() throws java.io.IOException
  {
     if (inBuf > 0)
     {
        --inBuf;
        return (char)((char)0xff & buffer[(bufpos == bufsize - 1) ? (bufpos = 0) : ++bufpos]);
     }

     if (++bufpos >= maxNextCharInd)
        FillBuff();

     return buffer[bufpos];
  }

  /**
   * @deprecated 
   * @see #getEndColumn
   */

  public final int getColumn() {
      return 0;
  }

  /**
   * @deprecated 
   * @see #getEndLine
   */

  public final int getLine() {
      return 0;
  }

  public final int getEndColumn() {
      return 0;
  }

  public final int getEndLine() {
      return 0;
  }

  public final int getBeginColumn() {
      return 0;
  }

  public final int getBeginLine() {
      return 0;
  }

  public final void backup(int amount) {

    inBuf += amount;
    if ((bufpos -= amount) < 0)
       bufpos += bufsize;
  }

  public CharCollector(int buffersize)
  {
    available = bufsize = buffersize;
    buffer = new char[buffersize];
  }

  public CharCollector()
  {
    available = bufsize = 4096;
    buffer = new char[4096];
  }

  public void Clear()
  {
     bufpos = -1;
     maxNextCharInd = 0;
     inBuf = 0;
  }

  public final String GetImage()
  {
     if (bufpos >= tokenBegin)
        return new String(buffer, tokenBegin, bufpos - tokenBegin + 1);
     else
        return new String(buffer, tokenBegin, bufsize - tokenBegin) +
                              new String(buffer, 0, bufpos + 1);
  }

  public final char[] GetSuffix(int len)
  {
     char[] ret = new char[len];

     if (bufpos + 1 >= len)
        System.arraycopy(buffer, bufpos - len + 1, ret, 0, len);
     else
     {
        System.arraycopy(buffer, bufsize - (len - bufpos - 1), ret, 0,
                                                          len - bufpos - 1);
        System.arraycopy(buffer, 0, ret, len - bufpos, bufpos + 1);
     }

     return ret;
  }

  public void Done()
  {
     buffer = null;
  }
}
