

import java.io.*;

public class FaqMain {

  static int count = 0;

  static int beginAt = 1;

  static PrintWriter indstr;

  static {
    try {
      indstr = new PrintWriter(new FileWriter("index.html"));
      indstr.println("<title>Selected list of emails from the JavaCC mailing list</title>");
      indstr.println("<h2>Selected list of emails from the JavaCC mailing list</h2>");
    } catch (IOException e) {
      throw new Error();
    }
  }

  static String fix(String s) {
    String retval = "";
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '<') {
        retval += "&lt;";
      } else if (c == '>') {
        retval += "&gt;";
      } else {
        retval += c;
      }
    }
    return retval;
  }

  public static void main(String args[]) throws ParseException {
    if (args.length == 1) {
      beginAt = Integer.parseInt(args[0]);
    }
    Faq parser = new Faq(System.in);
    parser.MailFile();
  }

}

