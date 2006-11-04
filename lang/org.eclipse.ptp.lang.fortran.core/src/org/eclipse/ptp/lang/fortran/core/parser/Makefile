JAVAC=javac
JAVA=java

GENERATED_JAVA_FILES=FortranLexer.java FortranParser.java
CLASS_FILES=FortranLexer.class FortranParser.class \
            FortranStream.class 
GENERATED_TOKEN_FILES=FortranLexer.tokens FortranParser.tokens

all: $(GENERATED_JAVA_FILES) $(CLASS_FILES) 

FortranLexer.java: FortranLexer.g
	$(JAVA) org.antlr.Tool $<

FortranParser.java: FortranParser.g
	$(JAVA) org.antlr.Tool $<

FortranLexer.class: FortranLexer.java 
	$(JAVAC) $<

FortranParser.class: FortranParser.java
	$(JAVAC) $<

FortranStream.class: FortranStream.java
	$(JAVAC) $<

clean:
	rm -f $(CLASS_FILES) 
	rm -f $(GENERATED_JAVA_FILES) 
	rm -f $(GENERATED_TOKEN_FILES)
