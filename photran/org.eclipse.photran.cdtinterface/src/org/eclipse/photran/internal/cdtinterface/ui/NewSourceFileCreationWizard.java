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
package org.eclipse.photran.internal.cdtinterface.ui;

import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.cdt.internal.ui.wizards.filewizard.AbstractFileCreationWizard;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewSourceFileCreationWizardPage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.photran.internal.cdtinterface.CDTInterfacePlugin;
import org.eclipse.photran.internal.core.FortranCorePlugin;

/**
 * Wizard to create a new source file.
 * 
 * Based on {@link org.eclipse.cdt.ui.wizards.NewSourceFileCreationWizard}
 * 
 * @author Jeff Overbey
 */
@SuppressWarnings("restriction")
public class NewSourceFileCreationWizard extends AbstractFileCreationWizard
{
    private static final String BANNER_IMAGE = "icons/wizban/newffile_wiz.gif"; //$NON-NLS-1$

    public NewSourceFileCreationWizard()
    {
        super();
        setDefaultPageImageDescriptor(CDTInterfacePlugin.getImageDescriptor(BANNER_IMAGE));
        setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
        setWindowTitle(Messages.NewSourceFileCreationWizard_WindowTitle);
    }

    @Override
    public void addPages()
    {
        super.addPages();

        fPage = new FortranSourceFileCreationWizardPage();
        addPage(fPage);
        fPage.init(getSelection());
    }

    public static class FortranSourceFileCreationWizardPage extends NewSourceFileCreationWizardPage
    {
        @Override
        protected Template[] getApplicableTemplates()
        {
            return StubUtility.getFileTemplatesForContentTypes(
                FortranCorePlugin.getAllFortranContentTypes(), null);
        }
    }
}
