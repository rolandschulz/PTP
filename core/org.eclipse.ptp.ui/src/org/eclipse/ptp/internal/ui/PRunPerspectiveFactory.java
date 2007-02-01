/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.ui;

import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

public class PRunPerspectiveFactory implements IPerspectiveFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
		
		IFolderLayout folder1= layout.createFolder("topLeft", IPageLayout.LEFT, (float)0.25, editorArea);
		folder1.addView(IPTPUIConstants.VIEW_PARALLELMACHINE);
		folder1.addView(IPTPUIConstants.VIEW_PARALLELJOB);
		folder1.addView(IPTPUIConstants.VIEW_RESOURCEMANAGER);

		folder1.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		
		IFolderLayout folder2= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea);
		folder2.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		folder2.addView(IPageLayout.ID_PROBLEM_VIEW);
		folder2.addView(IPageLayout.ID_PROP_SHEET);
		folder2.addView(IPageLayout.ID_TASK_LIST);
		folder2.addView("org.eclipse.pde.runtime.LogView");
		
		IFolderLayout folder3= layout.createFolder("topRight", IPageLayout.RIGHT,(float)0.75, editorArea);
		folder3.addView(IPageLayout.ID_OUTLINE);

		// set toolbar or menu icon
		//layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		//layout.addActionSet(IPTPUIConstants.ACTION_SET);

		// views - build console
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		
		// views - searching
		layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);
		
		// views - standard workbench
		layout.addShowViewShortcut(IPTPUIConstants.VIEW_PARALLELMACHINE);
		layout.addShowViewShortcut(IPTPUIConstants.VIEW_PARALLELJOB);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView");
		
		// link - things we should do
		layout.addShowInPart(IPTPUIConstants.VIEW_PARALLELMACHINE);
		layout.addShowInPart(IPTPUIConstants.VIEW_PARALLELJOB);
		//layout.addShowInPart(IPageLayout.ID_RES_NAV);
		//layout.addShowInPart("org.eclipse.pde.runtime.LogView");
	}
}
