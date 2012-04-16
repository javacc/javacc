all: javaparser wjavaparser

javaparser: main.cc gen/*.cc
	$(CXX) $(CXXFLAGS) -o javaparser -I. main.cc gen/*.cc

wjavaparser: wmain.cc gen/*.cc
	$(CXX) $(CXXFLAGS) -DJAVACC_CHAR_TYPE=wchar_t -o wjavaparser -I. wmain.cc gen/*.cc

gen/*.cc: Java1.1.jj ../../../bin/lib/javacc.jar
	java -cp ../../../bin/lib/javacc.jar javacc $(JAVACC_OPTS) $<

clean:
	/bin/rm -rf gen javaparser wjavaparser
