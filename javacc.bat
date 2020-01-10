setlocal
set M2=C:\Users\fandre\.m2\repository\org\javacc
set PARSER=%M2%\parser\8.0.0\parser-8.0.0.jar
set CPP=%M2%\codegen\cpp\8.0.0\cpp-8.0.0.jar
set JAVA=%M2%\codegen\java\8.0.0\java-8.0.0.jar
set CSHARP=%M2%\codegen\csharp\8.0.0\csharp-8.0.0.jar
set CP=%PARSER%;%JAVA%;%CPP%
java -cp %CP% javacc %*
endlocal
