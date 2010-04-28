*****   THIS MOST IMPORTANT FILE IN THIS PROJECT IS   *****
*****                                                 *****
*****             dev-guide/dev-guide.pdf             *****
*****                                                 *****
*****        IF YOU ARE LOOKING FOR DEVELOPER         *****
*****           DOCUMENTATION, START THERE.           *****

Contents of this project:

dev-guide/
	Contains the Photran Developer's Guide.  The CVS
	instructions in Appendix A are also made into a
	separate PDF.
	
	Run Ant on build.xml to build the documentation
	using pdflatex.  (In Eclipse, right-click build.xml
	and click on Run As > Ant Build.)
	
	Note that the PDFs contain CVS revision information
	at the beginning of each chapter.  If you have write
	access to Photran's CVS repository, make sure you
		(1) first commit the LaTeX sources
		(2) then re-build the PDFs
		(3) then commit the PDFs
	The PDF links on the Web site retrieve the PDFs
	from the CVS repository; this ensures that those
	PDFs have up-to-date CVS revision information in
	each section.  (If you create the PDFs from modified
	sources without committing them to CVS first, the
	revision information will be for the last revision
	in CVS rather than the date the revised version was
	created.)

misc-notes/
	Various notes-to-self that aren't import enough to
	be in the Developer's Guide.

contributions/
    Contains correspondence and documentation related to
    third-party contributions in Photran.

eclipse-config/
    Contains settings for the Eclipse Java code formatter
    and code templates.  These have since been hard-coded
    into the settings for each project, so they will be
    applied by default when code is formatted or new classes
    are created.

--Jeff Overbey 8/19/05, updated 8/29/08, 11/12/09