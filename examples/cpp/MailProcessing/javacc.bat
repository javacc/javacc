setlocal
set M2=C:\Users\fandre\.m2\repository\org\javacc
set PARSER=%M2%\parser\8.0.0\parser-8.0.0.jar
set CODGEN=%M2%\codegen\cpp\8.0.0\cpp-8.0.0.jar
set CP=%PARSER%;%CODGEN%
java -cp %CP% javacc %*
endlocal
