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
package org.eclipse.ptp.debug.internal.ui;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
/**
 * @author clement chu
 *
 */
public class PTPDebugPerspectiveFactory implements IPerspectiveFactory {	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();

		IFolderLayout folder1= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.8, editorArea);
		folder1.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		folder1.addView("org.eclipse.debug.ui.MemoryView");
		folder1.addView("org.eclipse.pde.runtime.LogView");
		
		IFolderLayout folder2= layout.createFolder("topLeftUp", IPageLayout.TOP, (float)0.5, editorArea);
		folder2.addView(IPTPDebugUIConstants.ID_VIEW_PARALLELDEBUG);

		IFolderLayout folder4= layout.createFolder("topRight", IPageLayout.RIGHT, (float)0.6, "topLeftUp");
		folder4.addView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
		folder4.addView(IDebugUIConstants.ID_EXPRESSION_VIEW);
		folder4.addView(IDebugUIConstants.ID_VARIABLE_VIEW);
		folder4.addView(IPTPDebugUIConstants.ID_VIEW_SIGNAL);
		folder4.addView(IPTPDebugUIConstants.ID_VIEW_ARRAY);
		folder4.addView(IPTPDebugUIConstants.ID_VIEW_VARIABLE);

		IFolderLayout folder3= layout.createFolder("topLeftDown", IPageLayout.BOTTOM, (float)0.6, "topLeftUp");
		folder3.addView(IDebugUIConstants.ID_DEBUG_VIEW);

		IFolderLayout folder5= layout.createFolder("right", IPageLayout.RIGHT,(float)0.8, editorArea);
		folder5.addView(IPageLayout.ID_OUTLINE);
		
		// set toolbar or menu icon
		//layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		//layout.addActionSet(IPTPUIConstants.ACTION_SET);
		
		// views - short cut
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		layout.addShowViewShortcut("org.eclipse.debug.ui.MemoryView");
		layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);
		layout.addShowViewShortcut(IPTPDebugUIConstants.ID_VIEW_PARALLELDEBUG);
		layout.addShowViewShortcut(IPTPDebugUIConstants.ID_VIEW_ARRAY);
		layout.addShowViewShortcut(IPTPDebugUIConstants.ID_VIEW_SIGNAL);
		layout.addShowViewShortcut(IPTPDebugUIConstants.ID_VIEW_VARIABLE);
		layout.addShowViewShortcut(IDebugUIConstants.ID_BREAKPOINT_VIEW);
		layout.addShowViewShortcut(IDebugUIConstants.ID_VARIABLE_VIEW);
		layout.addShowViewShortcut(IDebugUIConstants.ID_DEBUG_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		//layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView");

		// link - things we should do
		layout.addShowInPart(IPTPDebugUIConstants.ID_VIEW_PARALLELDEBUG);
		layout.addShowInPart(IPTPDebugUIConstants.ID_VIEW_ARRAY);
		layout.addShowInPart(IPTPDebugUIConstants.ID_VIEW_SIGNAL);
		layout.addShowInPart(IPTPDebugUIConstants.ID_VIEW_VARIABLE);
		layout.addShowInPart(IDebugUIConstants.ID_DEBUG_VIEW);
		//layout.addShowInPart(IPageLayout.ID_RES_NAV);
	}
}
