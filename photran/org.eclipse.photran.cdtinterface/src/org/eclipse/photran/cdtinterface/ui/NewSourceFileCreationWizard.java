/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     UIUC - Modification for Photran
 *******************************************************************************/
package org.eclipse.photran.cdtinterface.ui;

import org.eclipse.cdt.internal.ui.wizards.filewizard.AbstractFileCreationWizard;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.photran.cdtinterface.CDTInterfacePlugin;

/**
 * Wizard to create a new source file
 * 
 * Based on {@link org.eclipse.cdt.ui.wizards.NewSourceFileCreationWizard}
 */
public class NewSourceFileCreationWizard extends AbstractFileCreationWizard
{
    public NewSourceFileCreationWizard()
    {
        super();
		setDefaultPageImageDescriptor(CDTInterfacePlugin.getImageDescriptor("icons/wizban/newffile_wiz.gif"));
        setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setWindowTitle("New Fortran Source File");
    }

    public void addPages() {
        super.addPages();
        fPage = new NewSourceFileCreationWizardPage();
        addPage(fPage);
        fPage.init(getSelection());
    }
}
