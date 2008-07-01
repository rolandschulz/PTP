This is a very rough first attempt at extracting Photran's parser and virtual
program graph (VPG) to run outside Eclipse, based on a pre-4.0b4 version of
the Photran AST and VPG.

Two disclaimers.
(1) Photran is fairly well-tested (1500+ test cases), but obviously this
    un-Eclipsed code is NOT, since I hacked it together in one morning.
    It will have bugs that aren't present in Photran.  When it breaks, tell me.
(2) The AST design is NOT final.  The parts we actually use are generally
    in decent shape.  Others have never been touched and need work.
    Some parts were changed significantly within the last week.
    If parts of it are difficult to use, we should fix them.
    In other words, please give me feedback; don't just put up with
    things you don't like.

Contents:
=========

listdefs*
src-samples/
test/
	listdefs lists all of the declarations/definitions in a file.
	The listdefs shell script puts the bin directory on the class path and runs
	the ListDefs command line tool from the src-samples directory.
	To use it,
		cd test
		../listdefs hello.f90
	Note that the program database (.db-photran40b4vpg) is stored in the current
	directory (test, if you use the commands above) and is constructed by
	a recursive traversal of that directory.  Modules are searched
	for only in the current directory (no subdirectories), and the filename
	extensions .f, .f77, .f90, .f95, and .f03 are associated with fixed-
	and free-format source as expected.  These options can be made
	configurable from the command line later (they're configured via
	the Eclipse GUI in Photran); they're hard-coded into some of the classes
	in src-mock-objects for now.

bin/
	Compiled class files are stored here.  When you write your
	own code, put this on the classpath.

src-copied-vpg-photran/
	Photran's lexer, parser, and VPG, and support code from CDT
	and VPG.  (VPG is a separate project of mine that happens to be
	used in Photran.)  All of the sources in these folders are copied
	verbatim from the originals.

src-copied-vpg-photran/fortran95.bnf
	The parsing grammar with AST annotations.  This is critical for
	navigating the hundreds of classes in the Fortran AST.

src-mock-objects/
	Mock objects imitating just enough of the Eclipse framework,
	CDT, and some other parts of Photran and VPG
	to allow the Photran code to compile unmodified.  I hacked these
	together this morning and they are not well-tested.  If you
	get random NullPointerExceptions or CoreExceptions,
	it is probably because I did not implement one of these well enough.
	(The VPG and AST are fairly robust in Eclipse.  Really.)

--
Jeff Overbey  10/22/07, updated 07/01/08
overbey2@illinois.edu

Photran, CDT, and VPG are distributed under the Eclipse Public License v1.0.