/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.ui.wizards;

import org.eclipse.cldt.internal.ui.FortranPluginImages;
import org.eclipse.cldt.internal.ui.wizards.filewizard.AbstractFileCreationWizard;
import org.eclipse.cldt.internal.ui.wizards.filewizard.NewFileWizardMessages;
import org.eclipse.cldt.internal.ui.wizards.filewizard.NewSourceFileCreationWizardPage;
import org.eclipse.cldt.ui.FortranUIPlugin;

public class NewSourceFileCreationWizard extends AbstractFileCreationWizard {
    
    public NewSourceFileCreationWizard() {
        super();
        setDefaultPageImageDescriptor(FortranPluginImages.DESC_WIZBAN_NEW_SOURCEFILE);
        setDialogSettings(FortranUIPlugin.getDefault().getDialogSettings());
        setWindowTitle(NewFileWizardMessages.getString("NewSourceFileCreationWizard.title")); //$NON-NLS-1$
    }
    
    /*
     * @see Wizard#createPages
     */
    public void addPages() {
        super.addPages();
        fPage = new NewSourceFileCreationWizardPage();
        addPage(fPage);
        fPage.init(getSelection());
    }
}