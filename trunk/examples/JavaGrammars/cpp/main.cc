#include <fstream>
#include <iomanip>
#include <iostream>
#include <string>
#include <stdlib.h>

#include "gen/CharStream.h"
#include "gen/JavaParser.h"
#include "gen/JavaParserTokenManager.h"

using namespace java_parser;
using namespace std;

string ReadFileFully(char *file_name) {
  string s("");
  ifstream fp_in;
  fp_in.open(file_name, ios::in);
  // Very inefficient.
  while (!fp_in.eof()) {
   s += fp_in.get();
  }
  return s;
}

int main(int argc, char **argv) {
  if (argc < 2) {
    cout << "Usage: javaparser <java inputfile>" << endl;
    exit(1);
  }
  string s = ReadFileFully(argv[1]);
  CharStream *stream = new CharStream(s.c_str(), s.size() - 1, 1, 1);
  JavaParserTokenManager *scanner = new JavaParserTokenManager(stream);
  JavaParser parser(scanner);
  parser.CompilationUnit();
}
