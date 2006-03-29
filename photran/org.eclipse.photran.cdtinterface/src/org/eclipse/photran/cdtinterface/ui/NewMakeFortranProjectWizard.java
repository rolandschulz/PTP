package org.eclipse.photran.cdtinterface.ui;

import org.eclipse.cdt.make.ui.wizards.NewMakeCProjectWizard;
import org.eclipse.photran.cdtinterface.CDTInterfacePlugin;

public class NewMakeFortranProjectWizard extends NewMakeCProjectWizard
{
	public NewMakeFortranProjectWizard()
	{
		super("Standard Make Fortran Project", "Create a new Fortran project.  You must supply your own makefile.");
	}
	
	protected void initializeDefaultPageImageDescriptor()
	{
		setDefaultPageImageDescriptor(CDTInterfacePlugin.getImageDescriptor("icons/wizban/newfprj_wiz.gif"));
	}
}
