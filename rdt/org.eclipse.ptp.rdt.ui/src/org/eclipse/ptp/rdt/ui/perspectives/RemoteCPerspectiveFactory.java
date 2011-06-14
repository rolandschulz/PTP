/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.perspectives;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

/**
 * 
 * Perspective factory for the Remote C/C++ Development Perspective.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 *
 */
public class RemoteCPerspectiveFactory implements IPerspectiveFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
		
		IFolderLayout folder1= layout.createFolder("topLeft", IPageLayout.LEFT, (float)0.25, editorArea); //$NON-NLS-1$
		folder1.addView(ProjectExplorer.VIEW_ID);
		folder1.addPlaceholder(CUIPlugin.CVIEW_ID);
		folder1.addPlaceholder(IPageLayout.ID_RES_NAV);
		folder1.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		
		IFolderLayout folder2= layout.createFolder("bottomLeft", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
		folder2.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		folder2.addView(IPageLayout.ID_PROBLEM_VIEW);
		//task view not yet implemented
//		folder2.addView(IPageLayout.ID_TASK_LIST);
//		folder2.addView(IPageLayout.ID_PROP_SHEET);
		folder2.addView(UIPlugin.CALL_HIERARCHY_VIEW_ID);
		folder2.addView(UIPlugin.TYPE_HIERARCHY_VIEW_ID);
		//folder2.addView(UIPlugin.INCLUDE_BROWSER_VIEW_ID);
		
		IFolderLayout folder3= layout.createFolder("topRight", IPageLayout.RIGHT,(float)0.75, editorArea); //$NON-NLS-1$
		folder3.addView(IPageLayout.ID_OUTLINE);

		layout.addActionSet(UIPlugin.REMOTE_SEARCH_ACTION_SET_ID);
		layout.addActionSet(CUIPlugin.ID_CELEMENT_CREATION_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		
		// add a new folder for various tools for interacting with the remote system
//		IFolderLayout folder4 = layout.createFolder("bottomRight", IPageLayout.RIGHT, (float)0.5, "bottomLeft"); //$NON-NLS-1$ //$NON-NLS-2$b
		
		// views - build console
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		
		// views - searching
		layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);
		
		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(CUIPlugin.CVIEW_ID);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(UIPlugin.CALL_HIERARCHY_VIEW_ID);
		layout.addShowViewShortcut(UIPlugin.TYPE_HIERARCHY_VIEW_ID);
		layout.addShowViewShortcut(UIPlugin.INCLUDE_BROWSER_VIEW_ID);

	}

}
