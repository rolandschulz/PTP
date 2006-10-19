CLASSPATH = /Users/rasmussn/Languages/compilers/antlr/antlr-3.0b4/lib/antlr-2.7.6.jar:/Users/rasmussn/Languages/compilers/antlr/antlr-3.0b4/lib/antlr-3.0b4.jar:/Users/rasmussn/Languages/compilers/antlr/antlr-3.0b4/lib/stringtemplate-2.3b9.jar

all: FortranParser.java

FortranParser.java: fortran.g
	java -classpath $(CLASSPATH) org.antlr.Tool fortran.g

lexer: Fortran03Lexer.java

parser: Fortran03Parser.java

Fortran03Lexer.java: fortran03_lexer.g
	java -classpath $(CLASSPATH) org.antlr.Tool fortran03_lexer.g

Fortran03Parser.java: fortran03_parser.g
	java -classpath $(CLASSPATH) org.antlr.Tool fortran03_parser.g

F03: F03.class Fortran03Parser.class Fortran03Lexer.class

Fortran03Lexer.class: Fortran03Lexer.java
	javac -cp $(CLASSPATH) Fortran03Lexer.java

Fortran03Parser.class: Fortran03Parser.java
	javac -cp $(CLASSPATH) Fortran03Parser.java

F03.class: F03.java
	javac -cp $(CLASSPATH) F03.java

clean:
	rm -f Fortran03Lexer.java Fortran03Parser.java *.tokens *.class

