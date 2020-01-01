#include <iostream>
using namespace std;
#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>
#include "SPLParser.h"

#include "JavaCC.h"
#include "SPLParserTokenManager.h"
#include "ParseException.h"
#include "DumpVisitor.h"
#include "Interpret.h"

JJString ReadFileFully(char *file_name) {
	return "int n;\n n = 3 + 4 * 5 + 6;\nwrite n;\n";
}

int main(int argc, char**argv) {
	JJString s = ReadFileFully(argv[1]);
	try {
		CharStream *stream = new CharStream(s.c_str(), s.size() - 1, 1, 1);
		SPLParserTokenManager *scanner = new SPLParserTokenManager(stream);
		SPLParser parser(scanner);
		parser.CompilationUnit();
		Node* n = parser.rootNode();
		DumpVisitor dumpVisitor;
		Interpret interpret;
		n->jjtAccept(&interpret, nullptr);
	} catch (const ParseException& e) {
		clog << e.expectedTokenSequences << endl;
	}
return 0;
}
