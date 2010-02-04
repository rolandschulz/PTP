***************************************************************************************************
*                      O R G . E C L I P S E . P H O T R A N . C M D L I N E                      *
***************************************************************************************************

This is a very rough attempt at extracting Photran's parser and virtual program graph (VPG)
to run outside Eclipse in an ordinary Java command line tool, without any Eclipse infrastructure.

Three disclaimers.

(1) Photran is fairly well-tested, but this command line code is NOT.  Most of it
    was hacked together in one morning in 2007.  We only use it to run some
    research experiments and to test the indexer's performance.
    It will have bugs that aren't present in Photran.  When it breaks, tell me.

(2) The AST design is NOT final.  The parts we actually use are generally
    in decent shape.  Others have never been touched and need work.
    If parts of it are difficult to use, we should fix them.
    In other words, please give me feedback; don't just put up with
    things you don't like.

(3) This uses mock objects to emulate a minimal Eclipse API, enough that Photran's
    VPG source code will compile verbatim.  These are extremely incomplete and are
    modified when necessary to allow the VPG code to compile and run the sample
    programs.  Consequently, they are a very likely source of bugs.  (All of this
    code is in the src-mock-objects/ directory.)  *If you use this code for anything
    serious, you will probably need to change some classes in src-mock-objects/ --
    and if you do, please submit a patch so we can take advantage of your changes!*
    
    (a) The program database (.db-photran60vpg) is stored in the current
    directory (i.e., wherever the program is launched from) and is constructed by
    a recursive traversal of that directory.  Modules are searched for only in the
    current directory, and the filename extensions
                           .f, .f77, .f90, .f95, and .f03
    are associated with fixed- and free-format source as expected.  Currently,
    these options are hard-coded into some of the classes in src-mock-objects/
    (although we should make them configurable from the command line if anyone
    uses this code for something serious; they're configured via the Eclipse GUI in
    Photran).
    
    (b) In Eclipse, a "resource change listener" monitors files and re-indexes
    them when they change.  This has NOT been implemented in this command-line
    version; programs are indexed only once, when the program starts.  We assume
    that command line tools will read a Fortran program, process it in some way,
    and then exit immediately.


Sample Programs (in samples/ and src-samples/):
==============================================

listdefs
	listdefs lists all of the declarations/definitions in a file.
	The listdefs shell script puts the Photran JAR on the class path and runs
	the ListDefs command line tool from the src-samples directory.
	To use it,
		cd samples
		./listdefs fortran-code/main.f90
	Note that this creates a .db-photran60vpgÊfile in the current directory
	(samples), and the Fortran sources are only re-indexed on the next run
	if they have changed since the database was created.


Contents:
========

distrib/photran-cmdline.jar
	The complete JAR with Photran's parser and VPG, support code from CDT and the
	Rephraser Engine, and mock objects from the src-mock-objects/ folder which
	emulate a minimal Eclipse-like API.

src-copied/
    Source code copied from other Photran projects.  This directory is initially
    empty; after you run the build script (see below), it will be populated with code
    copied from other Photran projects.  The copied code is NOT stored in CVS.

src-mock-objects/
	Mock objects imitating just enough of the Eclipse Platform, LTK, CDT, and
	other parts of Photran to allow the code in src-copied/ to compile without
	modification.  Most of these classes were hacked together in one morning,
	and they are not well-tested.  If you get random NullPointerExceptions or
	CoreExceptions, it is probably because I did not implement one of these well
	enough.

src-samples/
	Source code for the sample applications in the samples/ directory.

src-util/
	Base class/utility code for the sample applications in src-samples/

build/build.xml
    Apache Ant script that creates distrib/photran-cmdline.jar; see usage below.


Building:
========

1. Download the Eclipse Java compiler (i.e., "JDT Core Batch Compiler") from
   http://download.eclipse.org/eclipse/downloads/drops/R-3.5.1-200909170800/index.php#JDTCORE

2. Place the downloaded ecj-3.5.1.jar in the build/ directory (or create a symlink)

3. Run Apache Ant in the build/ directory
   cd build && ant

4. If you run Ant from inside Eclipse (Run As > Ant Build), be sure to refresh
   (right click > Refresh) the org.eclipse.photran.cmdline project after running Ant.
   The Ant script copies new files into src-copied/ folder and creates the jar in the
   distrib/ directory.

(The sources must be compiled with ecj because Sun javac has several bugs in its
implementation of generics.)

--
Jeff Overbey  10/22/07, updated 07/01/08, 10/13/09, 1/14/10
overbey2@illinois.edu

Photran, CDT, and VPG are distributed under the Eclipse Public License v1.0.