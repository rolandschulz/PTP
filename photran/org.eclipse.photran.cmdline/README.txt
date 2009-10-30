This is a very rough attempt at extracting Photran's parser and virtual program graph (VPG)
to run outside Eclipse in an ordinary Java command line tool, without any Eclipse infrastructure.

Two disclaimers.
(1) Photran is fairly well-tested, but this un-Eclipsed code is NOT.  Most of it
    was hacked together in one morning in 2007.  We only use it to run some
    research experiments and to test the indexer's performance.
    It will have bugs that aren't present in Photran.  When it breaks, tell me.
(2) The AST design is NOT final.  The parts we actually use are generally
    in decent shape.  Others have never been touched and need work.
    Some parts were changed significantly within the last week.
    If parts of it are difficult to use, we should fix them.
    In other words, please give me feedback; don't just put up with
    things you don't like.
(3) This uses mock objects to emulate a minimal Eclipse API, enough that Photran's
    VPG source code will compile verbatim.  These are extremely incomplete and are
    modified when necessary to allow the VPG code to compile and run the sample
    programs.  Consequently, they are a very likely source of bugs.

Sample Programs (in samples/ and src-samples/):
==============================================

listdefs
	listdefs lists all of the declarations/definitions in a file.
	The listdefs shell script puts the Photran JAR on the class path and runs
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

Contents:
========

build/build.xml
distrib/photran-cmdline.jar
	Ant script and resulting JAR with Photran's parser and VPG, support code
	from CDT and the Rephraser Engine, and mock objects from the
	src-mock-objects/ folder which emulate a minimal Eclipse-like API.

src-mock-objects/
	Mock objects imitating just enough of the Eclipse framework,
	CDT, and some other parts of Photran and VPG
	to allow the Photran code to compile unmodified.  I hacked these
	together this morning and they are not well-tested.  If you
	get random NullPointerExceptions or CoreExceptions,
	it is probably because I did not implement one of these well enough.

--
Jeff Overbey  10/22/07, updated 07/01/08, 10/13/09
overbey2@illinois.edu

Photran, CDT, and VPG are distributed under the Eclipse Public License v1.0.