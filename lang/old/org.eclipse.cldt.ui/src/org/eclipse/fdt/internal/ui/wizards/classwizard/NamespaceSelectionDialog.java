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
package org.eclipse.fdt.internal.ui.wizards.classwizard;

import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.ui.browser.typeinfo.TypeSelectionDialog;
import org.eclipse.swt.widgets.Shell;

public class NamespaceSelectionDialog extends TypeSelectionDialog {
    
    private static final String DIALOG_SETTINGS = NamespaceSelectionDialog.class.getName();
    private static final int[] VISIBLE_TYPES = { ICElement.C_NAMESPACE };
    
    public NamespaceSelectionDialog(Shell parent) {
        super(parent);
        setTitle(NewClassWizardMessages.getString("NamespaceSelectionDialog.title")); //$NON-NLS-1$
        setMessage(NewClassWizardMessages.getString("NamespaceSelectionDialog.message")); //$NON-NLS-1$
        setDialogSettings(DIALOG_SETTINGS);
        setVisibleTypes(VISIBLE_TYPES);
        setFilter("*", true); //$NON-NLS-1$
    }
}
