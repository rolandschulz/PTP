JAVAC=javac
JAVA=java

GENERATED_JAVA_FILES=FortranLexer.java FortranParser.java
CLASS_FILES=FortranLexer.class FortranParser.class \
            FortranStream.class FortranTokenStream.class \
            FortranMain.class 
GENERATED_TOKEN_FILES=FortranLexer.tokens FortranParser.tokens

all: $(GENERATED_JAVA_FILES) $(CLASS_FILES) 

runTests: $(CLASS_FILES)
	@for i in `ls test_files` ; do \
           $(JAVA) FortranMain test_files/$$i; \
           if test $$? != 0 ; then \
              echo -e test_files/$$i "\t\t\tFAILED!"; \
           else \
              echo -e test_files/$$i "\t\t\tPassed."; \
           fi; \
        done;


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

FortranTokenStream.class: FortranTokenStream.java
	$(JAVAC) $<

FortranMain.class: FortranMain.java
	$(JAVAC) $<


clean:
	rm -f $(CLASS_FILES) 
	rm -f $(GENERATED_JAVA_FILES) 
	rm -f $(GENERATED_TOKEN_FILES)
