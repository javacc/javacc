/* Copyright (c) 2008, Paul Cager.
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

package org.javacc.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates boiler-plate files from templates. Only very basic
 * template processing is supplied - if we need something more
 * sophisticated I suggest we use a third-party library.
 * 
 * @author paulcager
 * @since 4.2
 */
public class JavaFileGenerator {

  /**
   * @param templateName the name of the template. E.g. 
   *        "/templates/Token.template".
   * @param options the processing options in force, such
   *        as "STATIC=yes" 
   */
  public JavaFileGenerator(String templateName, Map options) {
    this.templateName = templateName;
    this.options = options;
  }

  private final String templateName;
  private final Map options; 
  
  private String currentLine;

  /**
   * Generate the output file.
   * @param out
   * @throws IOException
   */
  public void generate(PrintWriter out) throws IOException
  {
    InputStream is = getClass().getResourceAsStream(templateName);
    if (is == null)
      throw new IOException("Invalid template name: " + templateName);
    BufferedReader in = new BufferedReader(new InputStreamReader(is)); 
    process(in, out, false);
  }
  
  private String peekLine(BufferedReader in) throws IOException
  {
    if (currentLine == null)
      currentLine = in.readLine();
    
    return currentLine;
  }
  
  private String getLine(BufferedReader in) throws IOException
  {
    String line = currentLine;
    currentLine = null;
    
    if (line == null)
      in.readLine();
    
    return line;
  }
  
  private boolean evaluate(String condition)
  {
    condition = condition.trim();
    
    Object obj = options.get(condition);
    
    if (obj == null)
    {
      return condition.equalsIgnoreCase("true") || condition.equalsIgnoreCase("yes");
    }
    
    if (obj instanceof Boolean)
    {
      return ((Boolean)obj).booleanValue();
    }
    else if (obj instanceof String)
    {
      String string = ((String)obj).trim();
      return string.length() > 0 && !string.equalsIgnoreCase("false") && !string.equalsIgnoreCase("no");
    }
    
    return false;
  }
  
  private String substitute(String variable)
  {
    String varName = variable;
    String defaultValue = "";
    
    int pos = variable.indexOf(":-");
    
    if (pos != -1)
    {
      varName = variable.substring(0, pos);
      defaultValue = variable.substring(pos + 2);
    }
    
    Object obj = options.get(varName.trim());
    if (obj == null || obj.toString().length() == 0)
      return defaultValue;
    
    return obj.toString();
  }
  
  private void write(PrintWriter out, String text) throws IOException
  {
    if (text.indexOf("${") == -1)
    {
      out.println(text);
      return;
    }
    
    StringBuffer buff = new StringBuffer(text);
    int pos;
    
    while ( (pos = buff.indexOf("${")) != -1)
    {
      int closingBrace = buff.indexOf("}", pos);
      if (closingBrace == -1)
        throw new IOException("Missing '}' in template");
      
      String var = buff.substring(pos+2, closingBrace);
      
      buff.replace(pos, closingBrace + 1, substitute(var));
    }
    
    out.println(buff.toString());
  }
  
  private void process(BufferedReader in, PrintWriter out, boolean ignoring)  throws IOException
  {
//    out.println("*** process ignore=" + ignoring + " : " + peekLine(in));
    while ( peekLine(in) != null)
    {
      if (peekLine(in).trim().startsWith("#if"))
      {
        String line = getLine(in).trim();
        final boolean condition = evaluate(line.substring(3).trim());
        
        process(in, out, ignoring || !condition);
        
        if (peekLine(in) != null && peekLine(in).trim().startsWith("#else"))
        {
          getLine(in);   // Discard the #else line
          process(in, out, ignoring || condition);
        }
        
        line = getLine(in);
        
        if (line == null)
          throw new IOException("Missing \"#fi\"");
        
        if (!line.trim().startsWith("#fi"))
          throw new IOException("Expected \"#fi\", got: " + line);
      }
      else if (peekLine(in).trim().startsWith("#")) 
      {
        break;
      }
      else
      {
        String line = getLine(in);
        if (!ignoring) write(out, line);
      }
    }
    
    out.flush();
  }
  
  public static void main(String[] args) throws Exception
  {
    Map map = new HashMap();
    map.put("falseArg", Boolean.FALSE);
    map.put("trueArg", Boolean.TRUE);
    map.put("stringValue", "someString");
    
    new JavaFileGenerator(args[0], map).generate(new PrintWriter(args[1]));
  }
}
