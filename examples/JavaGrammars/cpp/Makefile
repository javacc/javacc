all: javaparser wjavaparser

javaparser: main.cc gen/*.cc
	$(CXX) $(CXXFLAGS) -Wall -o javaparser -I. main.cc gen/*.cc

wjavaparser: main.cc gen/*.cc
	$(CXX) $(CXXFLAGS) -Wall -DWIDE_CHAR=1 -DJAVACC_CHAR_TYPE=wchar_t -o wjavaparser -I. main.cc gen/*.cc

gen/Java1.1.jj: Java1.1.jjt ../../../bin/lib/javacc.jar
	java -cp ../../../bin/lib/javacc.jar jjtree $(JAVACC_OPTS) $<

gen/*.cc: gen/Java1.1.jj ../../../bin/lib/javacc.jar
	java -cp ../../../bin/lib/javacc.jar javacc $(JAVACC_OPTS) $<

clean:
	/bin/rm -rf gen javaparser wjavaparser
