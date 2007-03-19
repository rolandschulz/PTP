# Copyright (c) 2005, 2006 Los Alamos National Security, LLC.  This
# material was produced under U.S. Government contract DE-
# AC52-06NA25396 for Los Alamos National Laboratory (LANL), which is
# operated by the Los Alamos National Security, LLC (LANS) for the
# U.S. Department of Energy. The U.S. Government has rights to use,
# reproduce, and distribute this software. NEITHER THE GOVERNMENT NOR
# LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY
# LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified to
# produce derivative works, such modified software should be clearly
# marked, so as not to confuse it with the version available from
# LANL.

# Additionally, this program and the accompanying materials are made
# available under the terms of the Eclipse Public License v1.0 which
# accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

JAVAC=javac
JAVA=java
JAR=jar

JARFILE=Fortran2003Parser.jar

GENERATED_JAVA_FILES=FortranLexer.java FortranParser.java
CLASS_FILES=FortranLexer.class FortranParser.class \
            FortranStream.class FortranTokenStream.class \
            FortranLexicalPrepass.class FortranMain.class 
GENERATED_CLASS_FILES=FortranLexer*DFA*.class
GENERATED_TOKEN_FILES=FortranLexer.tokens FortranParser.tokens

DRAFT_TEST_DIRS="annex_c" "chapter_11" "chapter_15" "chapter_16" "chapter_4" \
               "chapter_5" "chapter_6" "chapter_7" "chapter_8" "chapter_9" \
               "chapter_10" "chapter_12" "chapter_2"
F90_TEST_DIRS="misc" "program-unit" 

all: $(GENERATED_JAVA_FILES) $(CLASS_FILES) 

runTests: $(CLASS_FILES)
	$(JAVA) FortranMain test_files/*.f03 test_files/*.f90

runDraftTests: $(CLASS_FILES)
	@for dir in $(DRAFT_TEST_DIRS) ; do \
           $(JAVA) FortranMain $$dir/*.f03; \
        done;

runAllTests: $(CLASS_FILES)
	$(MAKE) runTests
	$(MAKE) runDraftTests
	@for dir in $(F90_TEST_DIRS) ; do \
           $(JAVA) FortranMain $$dir/*.f90; \
        done;

FortranLexer.java: FortranLexer.g  FortranStream.class
	$(JAVA) org.antlr.Tool $<
FortranParser.java: FortranParser.g FortranLexer.tokens
	$(JAVA) org.antlr.Tool $<

FortranLexer.class: FortranLexer.java FortranLexer.tokens
	$(JAVAC) $<

FortranParser.class: FortranParser.java FortranLexer.tokens
	$(JAVAC) $<

FortranStream.class: FortranStream.java
	$(JAVAC) $<

FortranTokenStream.class: FortranTokenStream.java
	$(JAVAC) $<

FortranLexicalPrepass.class: FortranLexicalPrepass.java \
                             FortranTokenStream.class
	$(JAVAC) $<

FortranMain.class: FortranMain.java FortranLexer.class \
                   FortranParser.class FortranTokenStream.class \
                   FortranStream.class FortranLexicalPrepass.class
	$(JAVAC) $<

jarFile: $(CLASS_FILES)
	$(JAR) cf $(JARFILE) $(CLASS_FILES) $(GENERATED_CLASS_FILES)


clean:
	rm -f $(CLASS_FILES) 
	rm -f $(GENERATED_JAVA_FILES) 
	rm -f $(GENERATED_TOKEN_FILES)
	rm -f $(GENERATED_CLASS_FILES)

allclean:
	$(MAKE) clean
	rm -f $(JARFILE)

