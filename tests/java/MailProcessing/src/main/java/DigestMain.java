

import java.io.*;

public class DigestMain {

	static int count = 0;

	static String buffer = "";

	public static void main(String args[]) throws ParseException {
		Digest parser = new Digest(System.in);
		System.out.println("DIGEST OF RECENT MESSAGES FROM THE JAVACC MAILING LIST");
		System.out.println("----------------------------------------------------------------------");
		System.out.println("");
		System.out.println("MESSAGE SUMMARY:");
		System.out.println("");
		parser.MailFile();
		if (count == 0) {
			System.out.println("There have been no messages since the last digest posting.");
			System.out.println("");
			System.out.println("----------------------------------------------------------------------");
		} else {
			System.out.println("");
			System.out.println("----------------------------------------------------------------------");
			System.out.println("");
			System.out.println(buffer);
		}
	}

}
