package org.eclipse.photran.cdtinterface.ui;

import org.eclipse.photran.cdtinterface.CDTInterfacePlugin;

public class NewSourceFolderCreationWizard extends org.eclipse.cdt.ui.wizards.NewSourceFolderCreationWizard {

	public NewSourceFolderCreationWizard()
	{
		super();
		setDefaultPageImageDescriptor(CDTInterfacePlugin.getImageDescriptor("icons/wizban/newsrcfldr_wiz.gif"));
		setWindowTitle("New Source Folder");
	}
}
