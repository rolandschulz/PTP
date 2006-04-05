package org.eclipse.photran.cdtinterface.ui;

import org.eclipse.cdt.managedbuilder.ui.wizards.NewManagedCProjectWizard;
import org.eclipse.photran.cdtinterface.CDTInterfacePlugin;

public class NewManagedFortranProjectWizard extends NewManagedCProjectWizard {

	public NewManagedFortranProjectWizard()
	{
		super("Managed Make Fortran Project", "Create a new Fortran project and let Eclipse create and manage the makefile.");
	}
	
	protected void initializeDefaultPageImageDescriptor()
	{
		setDefaultPageImageDescriptor(CDTInterfacePlugin.getImageDescriptor("icons/wizban/newfprj_wiz.gif"));
	}

}
