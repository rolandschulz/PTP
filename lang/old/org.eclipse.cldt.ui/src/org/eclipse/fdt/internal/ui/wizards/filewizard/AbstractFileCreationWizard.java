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
package org.eclipse.fdt.internal.ui.wizards.filewizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.fdt.core.model.ITranslationUnit;
import org.eclipse.fdt.internal.ui.FortranPluginImages;
import org.eclipse.fdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.fdt.ui.FortranUIPlugin;

public abstract class AbstractFileCreationWizard extends NewElementWizard {
    
    protected AbstractFileCreationWizardPage fPage = null;
    
    public AbstractFileCreationWizard() {
        super();
        setDefaultPageImageDescriptor(FortranPluginImages.DESC_WIZBAN_NEW_FILE);
        setDialogSettings(FortranUIPlugin.getDefault().getDialogSettings());
        setWindowTitle(NewFileWizardMessages.getString("AbstractFileCreationWizard.title")); //$NON-NLS-1$
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fdt.internal.ui.wizards.NewElementWizard#canRunForked()
     */
    protected boolean canRunForked() {
    	return true;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
        fPage.createFile(monitor); // use the full progress monitor
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish() {
        boolean res = super.performFinish();
        if (res) {
            //TODO need prefs option for opening editor
            boolean openInEditor = true;
            
			ITranslationUnit headerTU = fPage.getCreatedFileTU();
			if (headerTU != null) {
				IResource resource= headerTU.getResource();
				selectAndReveal(resource);
				if (openInEditor) {
					openResource((IFile) resource);
				}
			}
        }
        return res;
    }
}
