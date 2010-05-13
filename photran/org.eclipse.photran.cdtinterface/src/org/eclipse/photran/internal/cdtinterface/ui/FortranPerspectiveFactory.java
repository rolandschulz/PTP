/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.photran.internal.cdtinterface.ui;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

/**
 * The Fortran perspective.
 * <p>
 * This is identical to CPerspectiveFactory, except we replace the C/C++ Projects View with the
 * Fortran Projects view.
 * 
 * @author Jeff Overbey
 */
/*
 * In this project, this is the only place where we have to actually copy code from the CDT.
 * Inheritance doesn't work since there is no way I know of to let the CDT add the C/C++ Projects
 * View (super.createInitialLayout()) and then remove it in our own implementation.
 */
public class FortranPerspectiveFactory implements IPerspectiveFactory
{
	@SuppressWarnings("deprecation")
    public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
		
		IFolderLayout folder1= layout.createFolder("topLeft", IPageLayout.LEFT, (float)0.25, editorArea); //$NON-NLS-1$
		folder1.addView(FortranView.FORTRAN_VIEW_ID);
		//folder1.addView(IPageLayout.ID_RES_NAV);
		//folder1.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		
		IFolderLayout folder2= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
		folder2.addView(IPageLayout.ID_PROBLEM_VIEW);
		folder2.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		//folder2.addView(IPageLayout.ID_PROP_SHEET);
		folder2.addView("org.eclipse.photran.ui.DeclarationView");
        folder2.addView("org.eclipse.photran.ui.VGPProblemView");
        folder2.addView(IPageLayout.ID_BOOKMARKS);
		
		IFolderLayout folder3= layout.createFolder("topRight", IPageLayout.RIGHT, (float)0.75, editorArea); //$NON-NLS-1$
		folder3.addView(IPageLayout.ID_OUTLINE);
        
        IFolderLayout folder4= layout.createFolder("bottomLeft", IPageLayout.BOTTOM, (float)0.75, "topLeft"); //$NON-NLS-1$ $NON-NLS-2$
        folder4.addView(IPageLayout.ID_PROGRESS_VIEW);

		layout.addActionSet(CUIPlugin.SEARCH_ACTION_SET_ID);	// This is the "Open Type" search toolbar action
		layout.addActionSet("org.eclipse.photran.ui.SearchActionSet");
		layout.addActionSet("org.eclipse.photran.cdtinterface.FortranElementCreationActionSet");
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		
		// views - build console
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		
		// views - searching
		layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);
		
		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(FortranView.FORTRAN_VIEW_ID);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);

		// link - things we should do
		layout.addShowInPart(FortranView.FORTRAN_VIEW_ID);
		layout.addShowInPart(IPageLayout.ID_RES_NAV);
		
		addFortranWizardShortcuts(layout);
	}
	
	private void addFortranWizardShortcuts(IPageLayout layout) {
		// new actions - Fortran project creation wizard
		String[] wizIDs = FortranWizardRegistry.getProjectWizardIDs();
		for (int i = 0; i < wizIDs.length; ++i) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}
		
		// new actions - Fortran folder creation wizard
		wizIDs = FortranWizardRegistry.getFolderWizardIDs();
		for (int i = 0; i < wizIDs.length; ++i) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}
		// new actions - Fortran file creation wizard
		wizIDs = FortranWizardRegistry.getFileWizardIDs();
		for (int i = 0; i < wizIDs.length; ++i) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}
		// new actions - Fortran type creation wizard
		wizIDs = FortranWizardRegistry.getTypeWizardIDs();
		for (int i = 0; i < wizIDs.length; ++i) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}
	}
}
