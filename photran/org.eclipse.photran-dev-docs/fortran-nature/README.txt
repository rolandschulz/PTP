The submitted patch serves two purposes: it assigns an 'F' icon to all Fortran source files (*.f, *.f77, etc.) and assigns a project nature image for Fortran projects. This is shown in the attached screenshot.

The first part is easy to understand. The Fortran file navigator (represented by org.eclipse.photran.cdtinterface.ui.FortranView), uses a label provider to assign text/images to resources in the workspace. The CDT view uses a CViewLabelProvider for this, and I created a subclass called FViewLabelProvider. This checks the suffix of source files in the Fortran navigator and assigns f_file_obj.gif to any Fortran files.

The second part is more involved. To ensure that Fortran projects can be distinguished from other projects, I took the following steps:

1. Created a class called org.eclipse.photran.core.FProjectNature, which is essentially a copy of the CDT's CProjectNature. Defined an extension for the nature (org.eclipse.core.resources.natures) in plugin.xml.

2. Defined an extension for org.eclipse.ui.ide.projectNatureImages in org.eclipse.photran.ui. This tells Eclipse to display the Fortran project icon (f_ovr.gif) whenever a project's *first* nature is org.eclipse.photran.core.fnature.

3. Added code to FortranProjectWizard so that all Photran created projects have Fortran nature in addition to C nature. Added a lot of code so that this nature is the project's *first* nature. This doesn't affect any of the other natures, but it ensures that the Fortran icon will be displayed whenever the project is shown.

Two last notes. The FViewLabelProvider only works if f_file_obj.gif is placed in the icons folder in the org.eclipse.photran.cdtinterface plug-in. I created my own icon for the project nature (f_ovr.gif), and it must be placed in the icons/full/obj16 folder in the org.eclipse.photran.ui plug-in.

If you have any questions or comments, please e-mail me at matt.scarpino@eclipseengineer.com.