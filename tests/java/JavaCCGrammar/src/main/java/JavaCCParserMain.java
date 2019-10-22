
import java.text.ParseException;

public class JavaCCParserMain {

	public static void main(String args[]) {
		JavaCCParser parser;
		if (args.length == 0) {
			System.out.println("JavaCC Parser:  Reading from standard input . . .");
			parser = new JavaCCParser(System.in);
		} else if (args.length == 1) {
			System.out.println("JavaCC Parser:  Reading from file " + args[0] + " . . .");
			try {
				parser = new JavaCCParser(new java.io.FileInputStream(args[0]));
			} catch (java.io.FileNotFoundException e) {
				System.out.println("JavaCC Parser:  File " + args[0] + " not found.");
				return;
			}
		} else {
			System.out.println("JavaCC Parser:  Usage is one of:");
			System.out.println("         java JavaCCParser < inputfile");
			System.out.println("OR");
			System.out.println("         java JavaCCParser inputfile");
			return;
		}
		try {
			parser.javacc_input();
			System.out.println("JavaCC Parser:  Java program parsed successfully.");
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			System.out.println("JavaCC Parser:  Encountered errors during parse.");
		}
	}

	/*
	 * Returns true if the next token is not in the FOLLOW list of "expansion". It
	 * is used to decide when the end of an "expansion" has been reached.
	 */
	static public boolean notTailOfExpansionUnit() {
		Token t;
		t = JavaCCParser.getToken(1);
		if (t.kind == JavaCCParser.BIT_OR || t.kind == JavaCCParser.COMMA || t.kind == JavaCCParser.RPAREN || t.kind == JavaCCParser.RBRACE || t.kind == JavaCCParser.RBRACKET)
			return false;
		return true;
	}

}
