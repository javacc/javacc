public class IDLParserMain {

  public static void main(String args[]) {
    IDLParser parser;
    if (args.length == 0) {
      System.out.println("IDL Parser Version 0.1:  Reading from standard input . . .");
      parser = new IDLParser(System.in);
    } else if (args.length == 1) {
      System.out.println("IDL Parser Version 0.1:  Reading from file " + args[0] + " . . .");
      try {
        parser = new IDLParser(new java.io.FileInputStream(args[0]));
      } catch (java.io.FileNotFoundException e) {
        System.out.println("IDL Parser Version 0.1:  File " + args[0] + " not found.");
        return;
      }
    } else {
      System.out.println("IDL Parser Version 0.1:  Usage is one of:");
      System.out.println("         java IDL < inputfile");
      System.out.println("OR");
      System.out.println("         java IDL inputfile");
      return;
    }
    try {
      parser.specification();
      System.out.println("IDL Parser Version 0.1:  IDL file parsed successfully.");
    } catch (ParseException e) {
      System.out.println("IDL Parser Version 0.1:  Encountered errors during parse.");
    }
  }

}
