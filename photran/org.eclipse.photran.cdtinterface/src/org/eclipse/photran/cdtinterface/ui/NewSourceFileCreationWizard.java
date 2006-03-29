package org.eclipse.photran.cdtinterface.ui;

import org.eclipse.photran.cdtinterface.CDTInterfacePlugin;

public class NewSourceFileCreationWizard extends org.eclipse.cdt.ui.wizards.NewSourceFileCreationWizard
{
    public NewSourceFileCreationWizard()
    {
        super();
		setDefaultPageImageDescriptor(CDTInterfacePlugin.getImageDescriptor("icons/wizban/newffile_wiz.gif"));
		setWindowTitle("New Source File");
    }
}
