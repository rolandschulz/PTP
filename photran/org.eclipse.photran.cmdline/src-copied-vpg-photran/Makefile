ROOT=../..

.PHONY: all copy

all:
	@echo "First, remove the org and bz directories, and commit"
	@echo "Then make copy to copy files from Eclipse workspace in " $(ROOT)

copy:
	rm -rf org bz
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
	#cp -R $(ROOT)/org.eclipse.photran.core.vpg/src/org/eclipse/photran/internal/core/parser org/eclipse/photran/internal/core/
	cp -R $(ROOT)/org.eclipse.photran.core.vpg/src/org/eclipse/photran/internal/core/refactoring/infrastructure org/eclipse/photran/internal/core/refactoring/
	#cp -R $(ROOT)/org.eclipse.photran.core.vpg/src/org/eclipse/photran/internal/core/properties org/eclipse/photran/internal/core/
	cp -R $(ROOT)/org.eclipse.photran.core.vpg/parser/org .
	cp -f $(ROOT)/org.eclipse.photran.core.vpg/parser/fortran2003.bnf .
	mkdir bz
	mkdir bz/over
	mkdir bz/over/vpg
	mkdir bz/over/vpg/db
	mkdir bz/over/vpg/eclipse
	cp -R $(ROOT)/bz.over.vpg/src/bz/over/vpg/*.* bz/over/vpg/
	cp -R $(ROOT)/bz.over.vpg/src/bz/over/vpg/db/caching bz/over/vpg/db/
	cp -R $(ROOT)/bz.over.vpg/src/bz/over/vpg/db/cdt bz/over/vpg/db/
	cp -R $(ROOT)/bz.over.vpg/src/bz/over/vpg/eclipse/EclipseVPGLog.java bz/over/vpg/eclipse/
	cp -R $(ROOT)/bz.over.vpg.cdtdb/src/bz .
	cp -R $(ROOT)/bz.over.vpg.cdtdb/src/org .
	rm -rf `find org -name CVS`
	rm -rf `find bz -name CVS`
	rm -rf `find . -name Activator.java`
	rm -rf `find . -name PhotranVPGStartup.java`
	rm -rf `find . -name FortranRefactoring.java`
	rm -rf `find . -name AbstractFortranRefactoring.java`
	rm -rf `find . -name SingleFileFortranRefactoring.java`
	rm -rf `find . -name MultipleFileFortranRefactoring.java`
