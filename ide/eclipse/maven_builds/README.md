Maven build outputs (under Eclipse ) can be redirected in this directory for builds recording
but files must exist (they are not created by m2e)

Color sequences can be suppressed, once written in the output, using the \e\[[0-9;]*m pattern in Eclipse or Notepad++

Output colors generation can be suppressed:
- on the command line with ``mvn -l <mvn_file>.txt [<goal(s)>] [<phase(s)>]``
- in the Eclipse launch configuration with the Main tab / Color Output checkbox or dropdown set to Never