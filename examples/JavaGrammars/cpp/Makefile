javaparser: *.cc gen/*.cc
	$(CXX) $(CXXFLAGS) -o javaparser -I. *.cc gen/*.cc

gen/*.cc: Java1.1.jj ../../../bin/lib/javacc.jar
	java -cp ../../../bin/lib/javacc.jar javacc $(JAVACC_OPTS) $<

clean:
	/bin/rm -rf gen javaparser
