The documentation is built using Ant.
Right-click build.xml and click on Run As > Ant Build.

Contents:

dev-guide
	Contains the Photran Developer's Guide.  The CVS
	instructions in Appendix A are also made into a
	separate PDF.
	
	If you edit this, make sure your name is added
	to authors.tex!
	
	Run Ant on build.xml to build the documentation
	using pdflateex.

misc-notes
	Various notes-to-self that aren't import enough to
	be in the developer's guide.

contributions
    Contains correspondence and documentation related to
    third-party contributions in Photran.

eclipse-config
    Contains settings for the Eclipse Java code formatter
    and code templates.  These have since been hard-coded
    into the settings for each project, so they will be
    applied by default when code is formatted or new classes
    are created.

--Jeff Overbey 8/19/05, updated 8/29/08, 11/12/09