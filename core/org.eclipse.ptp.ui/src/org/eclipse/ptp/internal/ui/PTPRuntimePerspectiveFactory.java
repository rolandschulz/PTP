/**
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.ptp.internal.ui;

import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

public class PTPRuntimePerspectiveFactory implements IPerspectiveFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		IFolderLayout rmFolder= layout.createFolder("rmFolder", IPageLayout.LEFT, (float)0.50, editorArea); //$NON-NLS-1$
		rmFolder.addView(IPTPUIConstants.VIEW_RESOURCEMANAGER);

		IFolderLayout jobsFolder= layout.createFolder("jobsFolder", IPageLayout.BOTTOM, (float)0.75, "rmFolder"); //$NON-NLS-1$ //$NON-NLS-2$
		jobsFolder.addView(IPTPUIConstants.VIEW_JOBSLIST);

		IFolderLayout machinesFolder = layout.createFolder("machinesFolder", IPageLayout.BOTTOM, (float)0.25, "rmFolder"); //$NON-NLS-1$ //$NON-NLS-2$
		machinesFolder.addView(IPTPUIConstants.VIEW_PARALLELMACHINES);
		machinesFolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		
		IFolderLayout consoleFolder= layout.createFolder("consoleFolder", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
		consoleFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);

		IFolderLayout propertiesFolder= layout.createFolder("propertiesFolder", IPageLayout.BOTTOM, (float)0.75, "consoleFolder"); //$NON-NLS-1$ //$NON-NLS-2$
		propertiesFolder.addView(IPageLayout.ID_PROP_SHEET);
		propertiesFolder.addView(IPageLayout.ID_PROBLEM_VIEW);
		propertiesFolder.addView(IPageLayout.ID_TASK_LIST);
		propertiesFolder.addView("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
		
		// views - build console
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		
		// views - searching
		layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);
		
		// views - standard workbench
		layout.addShowViewShortcut(IPTPUIConstants.VIEW_PARALLELMACHINES);
		layout.addShowViewShortcut(IPTPUIConstants.VIEW_JOBSLIST);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
		
		// link - things we should do
		layout.addShowInPart(IPTPUIConstants.VIEW_PARALLELMACHINES);
		layout.addShowInPart(IPTPUIConstants.VIEW_JOBSLIST);
	}
}
