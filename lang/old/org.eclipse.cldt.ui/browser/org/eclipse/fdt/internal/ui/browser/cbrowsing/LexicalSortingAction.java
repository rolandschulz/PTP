/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.internal.ui.browser.cbrowsing;

import org.eclipse.fdt.internal.ui.FortranPluginImages;
import org.eclipse.fdt.internal.ui.ICHelpContextIds;
import org.eclipse.fdt.ui.FortranUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.help.WorkbenchHelp;

/*
 * XXX: This class should become part of the MemberFilterActionGroup
 *      which should be renamed to MemberActionsGroup
 */
public class LexicalSortingAction extends Action {
	CBrowsingViewerSorter fSorter= new CBrowsingViewerSorter();
	StructuredViewer fViewer;
	private String fPreferenceKey;

	public LexicalSortingAction(StructuredViewer viewer, String id) {
		super();
		fViewer= viewer;
		fPreferenceKey= "LexicalSortingAction." + id + ".isChecked"; //$NON-NLS-1$ //$NON-NLS-2$
		setText(CBrowsingMessages.getString("LexicalSortingAction.label")); //$NON-NLS-1$
		FortranPluginImages.setImageDescriptors(this, FortranPluginImages.T_LCL, FortranPluginImages.IMG_ALPHA_SORTING); //$NON-NLS-1$ //$NON-NLS-2$
		setToolTipText(CBrowsingMessages.getString("LexicalSortingAction.tooltip")); //$NON-NLS-1$
		setDescription(CBrowsingMessages.getString("LexicalSortingAction.description")); //$NON-NLS-1$
		boolean checked= FortranUIPlugin.getDefault().getPreferenceStore().getBoolean(fPreferenceKey); //$NON-NLS-1$
		valueChanged(checked, false);
		WorkbenchHelp.setHelp(this, ICHelpContextIds.LEXICAL_SORTING_BROWSING_ACTION);
	}

	public void run() {
		valueChanged(isChecked(), true);
	}

	private void valueChanged(final boolean on, boolean store) {
		setChecked(on);
		BusyIndicator.showWhile(fViewer.getControl().getDisplay(), new Runnable() {
			public void run() {
				if (on)
					fViewer.setSorter(fSorter);
				else
					fViewer.setSorter(null);
			}
		});
		
		if (store)
			FortranUIPlugin.getDefault().getPreferenceStore().setValue(fPreferenceKey, on);
	}
}
