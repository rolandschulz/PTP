ROOT=../..

.PHONY: all copy clean

all:
	@echo "From the command line, cd to this directory, then"
	@echo "run make copy to copy files from Eclipse workspace in " $(ROOT)

copy:
	rm -rf org *.bnf
	mkdir org
	mkdir org/eclipse
	mkdir org/eclipse/photran
	mkdir org/eclipse/photran/cdtinterface
	mkdir org/eclipse/photran/cdtinterface/natures
	mkdir org/eclipse/photran/core
	mkdir org/eclipse/photran/internal
	mkdir org/eclipse/photran/internal/core
	mkdir org/eclipse/photran/internal/core/refactoring
	cp -R $(ROOT)/org.eclipse.photran.cdtinterface/src/org/eclipse/photran/cdtinterface/natures/* org/eclipse/photran/cdtinterface/natures
	cp -R $(ROOT)/org.eclipse.photran.core/lexer/org/eclipse/photran/internal/core/lexer org/eclipse/photran/internal/core/
	cp -R $(ROOT)/org.eclipse.photran.core.vpg/src/org/eclipse/photran/core/vpg org/eclipse/photran/core/
	cp -R $(ROOT)/org.eclipse.photran.core.vpg/src/org/eclipse/photran/internal/core/analysis org/eclipse/photran/internal/core/
	cp -R $(ROOT)/org.eclipse.photran.core.vpg/src/org/eclipse/photran/internal/core/refactoring/infrastructure org/eclipse/photran/internal/core/refactoring/
	cp -R $(ROOT)/org.eclipse.photran.core.vpg/parser/org .
	cp -f $(ROOT)/org.eclipse.photran.core.vpg/parser/fortran2003.bnf .
	cp -R $(ROOT)/org.eclipse.rephraserengine-support/src-cdtdb-4.0.3/org .
	cp -R $(ROOT)/org.eclipse.rephraserengine-support/src-cdtdb-mock-objects/org .
	cp -R $(ROOT)/org.eclipse.rephraserengine.core/src/org .
	cp -R $(ROOT)/org.eclipse.rephraserengine.core.vpg/src/org .
	cp -R $(ROOT)/org.eclipse.rephraserengine.core.refactoring/src/org .
	rm -rf `find org -name CVS`
	rm -f org/eclipse/photran/core/vpg/Activator.java

clean:
	rm -rf org *.bnf
