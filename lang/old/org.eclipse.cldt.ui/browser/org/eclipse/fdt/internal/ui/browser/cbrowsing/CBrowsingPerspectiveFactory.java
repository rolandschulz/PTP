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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.internal.ui.wizards.CWizardRegistry;
import org.eclipse.fdt.ui.FortranUIPlugin;
import org.eclipse.fdt.ui.PreferenceConstants;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.console.IConsoleConstants;

public class CBrowsingPerspectiveFactory implements IPerspectiveFactory {
	
	/*
	 * XXX: This is a workaround for: http://dev.eclipse.org/bugs/show_bug.cgi?id=13070
	 */
	static ICElement fgCElementFromAction;
	
	/**
	 * Constructs a new Default layout engine.
	 */
	public CBrowsingPerspectiveFactory() {
		super();
	}

	/**
	 * @see IPerspectiveFactory#createInitialLayout
	 */
	public void createCViewInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
		
		IFolderLayout folder1= layout.createFolder("topLeft", IPageLayout.LEFT, (float)0.25, editorArea); //$NON-NLS-1$
		folder1.addView(FortranUIPlugin.CVIEW_ID);
		folder1.addView(IPageLayout.ID_RES_NAV);
		folder1.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		
		IFolderLayout folder2= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
		folder2.addView(IPageLayout.ID_PROBLEM_VIEW);
		folder2.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		folder2.addView(IPageLayout.ID_PROP_SHEET);
		
		IFolderLayout folder3= layout.createFolder("topRight", IPageLayout.RIGHT,(float)0.75, editorArea); //$NON-NLS-1$
		folder3.addView(IPageLayout.ID_OUTLINE);

		layout.addActionSet(FortranUIPlugin.SEARCH_ACTION_SET_ID);
		layout.addActionSet(FortranUIPlugin.ID_CELEMENT_CREATION_ACTION_SET);
		
		// views - build console
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		
		// views - searching
		layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);
		
		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(FortranUIPlugin.CVIEW_ID);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);

		// link - things we should do
		layout.addShowInPart(FortranUIPlugin.CVIEW_ID);
		layout.addShowInPart(IPageLayout.ID_RES_NAV);

		addCWizardShortcuts(layout);
	}

	public void createInitialLayout(IPageLayout layout) {
		if (stackBrowsingViewsVertically())
			createVerticalLayout(layout);
		else
			createHorizontalLayout(layout);
		
		// action sets
		layout.addActionSet(FortranUIPlugin.SEARCH_ACTION_SET_ID);
//		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
//		layout.addActionSet(JavaUI.ID_ACTION_SET);
		layout.addActionSet(FortranUIPlugin.ID_CELEMENT_CREATION_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		
		// views - java
		layout.addShowViewShortcut(FortranUIPlugin.ID_TYPE_HIERARCHY);
		layout.addShowViewShortcut(FortranUIPlugin.CVIEW_ID);
		layout.addShowViewShortcut(FortranUIPlugin.ID_PROJECTS_VIEW);
		layout.addShowViewShortcut(FortranUIPlugin.ID_NAMESPACES_VIEW);
		layout.addShowViewShortcut(FortranUIPlugin.ID_TYPES_VIEW);
		layout.addShowViewShortcut(FortranUIPlugin.ID_MEMBERS_VIEW);
//		layout.addShowViewShortcut(FortranUIPlugin.ID_SOURCE_VIEW);
//		layout.addShowViewShortcut(FortranUIPlugin.ID_JAVADOC_VIEW);

		// views - search		
		layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);

		// views - debugging
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		
		addCWizardShortcuts(layout);
	}
	
	private void addCWizardShortcuts(IPageLayout layout) {
		// new actions - C project creation wizard
		String[] wizIDs = CWizardRegistry.getProjectWizardIDs();
		for (int i = 0; i < wizIDs.length; ++i) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}
		// new actions - C folder creation wizard
		wizIDs = CWizardRegistry.getFolderWizardIDs();
		for (int i = 0; i < wizIDs.length; ++i) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}
		// new actions - C file creation wizard
		wizIDs = CWizardRegistry.getFileWizardIDs();
		for (int i = 0; i < wizIDs.length; ++i) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}
		// new actions - C type creation wizard
		wizIDs = CWizardRegistry.getTypeWizardIDs();
		for (int i = 0; i < wizIDs.length; ++i) {
			layout.addNewWizardShortcut(wizIDs[i]);
		}
	}

	private void createVerticalLayout(IPageLayout layout) {
		String relativePartId= IPageLayout.ID_EDITOR_AREA;
		int relativePos= IPageLayout.LEFT;
		
		IPlaceholderFolderLayout placeHolderLeft= layout.createPlaceholderFolder("left", IPageLayout.LEFT, (float)0.25, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		placeHolderLeft.addPlaceholder(FortranUIPlugin.ID_TYPE_HIERARCHY); 
		placeHolderLeft.addPlaceholder(IPageLayout.ID_OUTLINE);
		placeHolderLeft.addPlaceholder(FortranUIPlugin.CVIEW_ID);
		placeHolderLeft.addPlaceholder(IPageLayout.ID_RES_NAV);
		
		if (shouldShowProjectsView()) {
			layout.addView(FortranUIPlugin.ID_PROJECTS_VIEW, IPageLayout.LEFT, (float)0.25, IPageLayout.ID_EDITOR_AREA);
			relativePartId= FortranUIPlugin.ID_PROJECTS_VIEW;
			relativePos= IPageLayout.BOTTOM;
		}				
		if (shouldShowNamespacesView()) {
			layout.addView(FortranUIPlugin.ID_NAMESPACES_VIEW, relativePos, (float)0.25, relativePartId);
			relativePartId= FortranUIPlugin.ID_NAMESPACES_VIEW;
			relativePos= IPageLayout.BOTTOM;
		}
		layout.addView(FortranUIPlugin.ID_TYPES_VIEW, relativePos, (float)0.33, relativePartId);
		layout.addView(FortranUIPlugin.ID_MEMBERS_VIEW, IPageLayout.BOTTOM, (float)0.50, FortranUIPlugin.ID_TYPES_VIEW);
		
		IPlaceholderFolderLayout placeHolderBottom= layout.createPlaceholderFolder("bottom", IPageLayout.BOTTOM, (float)0.75, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		placeHolderBottom.addPlaceholder(IPageLayout.ID_PROBLEM_VIEW);
		placeHolderBottom.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
		placeHolderBottom.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
		placeHolderBottom.addPlaceholder(IPageLayout.ID_BOOKMARKS);		
//		placeHolderBottom.addPlaceholder(JavaUI.ID_SOURCE_VIEW);
//		placeHolderBottom.addPlaceholder(JavaUI.ID_JAVADOC_VIEW);
	}

	private void createHorizontalLayout(IPageLayout layout) {
		String relativePartId= IPageLayout.ID_EDITOR_AREA;		
		int relativePos= IPageLayout.TOP;
		
		if (shouldShowProjectsView()) {
			layout.addView(FortranUIPlugin.ID_PROJECTS_VIEW, IPageLayout.TOP, (float)0.25, IPageLayout.ID_EDITOR_AREA);
			relativePartId= FortranUIPlugin.ID_PROJECTS_VIEW;
			relativePos= IPageLayout.RIGHT;
		}
		if (shouldShowNamespacesView()) {
			layout.addView(FortranUIPlugin.ID_NAMESPACES_VIEW, relativePos, (float)0.25, relativePartId);
			relativePartId= FortranUIPlugin.ID_NAMESPACES_VIEW;
			relativePos= IPageLayout.RIGHT;
		}
		layout.addView(FortranUIPlugin.ID_TYPES_VIEW, relativePos, (float)0.33, relativePartId);
		layout.addView(FortranUIPlugin.ID_MEMBERS_VIEW, IPageLayout.RIGHT, (float)0.50, FortranUIPlugin.ID_TYPES_VIEW);
		
		IPlaceholderFolderLayout placeHolderLeft= layout.createPlaceholderFolder("left", IPageLayout.LEFT, (float)0.25, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		placeHolderLeft.addPlaceholder(FortranUIPlugin.ID_TYPE_HIERARCHY); 
		placeHolderLeft.addPlaceholder(IPageLayout.ID_OUTLINE);
		placeHolderLeft.addPlaceholder(FortranUIPlugin.CVIEW_ID);
		placeHolderLeft.addPlaceholder(IPageLayout.ID_RES_NAV);
		
		IPlaceholderFolderLayout placeHolderBottom= layout.createPlaceholderFolder("bottom", IPageLayout.BOTTOM, (float)0.75, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		placeHolderBottom.addPlaceholder(IPageLayout.ID_PROBLEM_VIEW);
		placeHolderBottom.addPlaceholder(NewSearchUI.SEARCH_VIEW_ID);
		placeHolderBottom.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);
		placeHolderBottom.addPlaceholder(IPageLayout.ID_BOOKMARKS);		
//		placeHolderBottom.addPlaceholder(JavaUI.ID_SOURCE_VIEW);
//		placeHolderBottom.addPlaceholder(JavaUI.ID_JAVADOC_VIEW);
	}
	
	private boolean shouldShowProjectsView() {
		return true;
//		RETURN FGCELEMENTFROMACTION == NULL || FGCELEMENTFROMACTION.GETELEMENTTYPE() == ICELEMENT.C_MODEL;
	}

	private boolean shouldShowNamespacesView() {
		return true;
//		if (fgCElementFromAction == null)
//			return true;
//		int type= fgCElementFromAction.getElementType();
//		return type == ICElement.C_MODEL || type == ICElement.C_PROJECT;
////		return type == ICElement.C_MODEL || type == ICElement.C_PROJECT || type == ICElement.PACKAGE_FRAGMENT_ROOT;
	}

	private boolean stackBrowsingViewsVertically() {
		return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.BROWSING_STACK_VERTICALLY);
	}

	/*
	 * XXX: This is a workaround for: http://dev.eclipse.org/bugs/show_bug.cgi?id=13070
	 */
	static void setInputFromAction(IAdaptable input) {
		if (input instanceof ICElement)
			fgCElementFromAction= (ICElement)input;
		else
			fgCElementFromAction= null;
	}
}


