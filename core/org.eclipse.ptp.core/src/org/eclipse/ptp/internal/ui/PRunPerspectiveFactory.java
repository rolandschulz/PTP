package org.eclipse.ptp.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

public class PRunPerspectiveFactory implements IPerspectiveFactory {

	/**
	 * @see IPerspectiveFactory#createInitialLayout
	 */
	public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
		
		IFolderLayout folder1= layout.createFolder("topLeft", IPageLayout.LEFT, (float)0.25, editorArea);
		folder1.addView(UIUtils.ParallelProcessesView_ID);
		folder1.addView(UIUtils.ParallelNodeStatusView_ID);
		//folder1.addView(IPageLayout.ID_RES_NAV);
		folder1.addPlaceholder(IPageLayout.ID_BOOKMARKS);
		
		IFolderLayout folder2= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea);
		folder2.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		folder2.addView(IPageLayout.ID_PROBLEM_VIEW);
		folder2.addView(IPageLayout.ID_PROP_SHEET);
		
		IFolderLayout folder3= layout.createFolder("topRight", IPageLayout.RIGHT,(float)0.75, editorArea);
		folder3.addView(IPageLayout.ID_OUTLINE);

		// set toolbar or menu icon
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet(UIUtils.PDT_ACTION_SET);

		// views - build console
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		
		// views - searching
		layout.addShowViewShortcut(NewSearchUI.SEARCH_VIEW_ID);
		
		// views - standard workbench
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(UIUtils.ParallelProcessesView_ID);
		layout.addShowViewShortcut(UIUtils.ParallelNodeStatusView_ID);
		//layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);

		// link - things we should do
		layout.addShowInPart(UIUtils.ParallelProcessesView_ID);
		layout.addShowInPart(UIUtils.ParallelNodeStatusView_ID);
		//layout.addShowInPart(IPageLayout.ID_RES_NAV);
	}
}
