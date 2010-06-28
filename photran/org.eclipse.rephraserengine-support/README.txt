-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
REPHRASER ENGINE SUPPORT CODE
-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

This project is used to build JARs containing the database infrastructure used
in CDT's indexer.  These JARs are:

cdtdb-4.0.3-eclipse.jar
    Copied into org.eclipse.rephraserengine.core; used by the Rephraser engine
    Depends on the Eclipse Platform to provide CoreException and similar classes

cdtdb-4.0.3-cmdline.jar
    Currently unused
    Uses mock objects to provide CoreException and similar classes

To build the JAR files:
1. Make sure the code has compiled successfully within the Eclipse IDE.  (The build script simply JARs the .class files
   from the project's bin directory; it does not recompile them.)
2. Right-click build/cdtdb-4.0.3.xml in the Package Explorer, and choose Run As > Ant Build
3. Right click the org.eclipse.rephraserengine-support project, and click Refresh.
4. Right-click distrib/cdtdb-4.0.3-eclipse.jar, and click Copy.
5. Right-click on the org.eclipse.rephraserengine.core.vpg project, and click Paste to paste it into the project root,
   overwriting the existing JAR.



-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
Version information:

JO 10/4/07:

  CDT files copied from CDT 4.0.1 (:pserver:anonymous@dev.eclipse.org:/cvsroot/tools/org.eclipse.cdt/org.eclipse.cdt.core
  Eclipse files copied from Eclipse 3.3.0 (Build id: I20070621-1340) for Mac OS X
  Only the necessary fields/methods from CCorePlugin were copied; all other files were copied verbatim in their entirety.

JO 06/09/08:
  Classes in internal.core.pdom.db updated to CDT 4.0.3 (from cdt_4_0 branch).  Changes to Database class (and file
  format?) in CDT 5.0 are causing problems.

JO 0628/10:
  Modified the Database class to fix Bug 318197.  (The modified block is labeled JO.)