/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.rm.ui.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class ResourceManagerPerspectiveFactory implements IPerspectiveFactory {

	private static final String RM_JOBS_VIEW_ID = "org.eclipse.ptp.rm.ui.views.JobsView";

	private static final String RM_MACHINES_VIEW_ID = "org.eclipse.ptp.rm.ui.views.MachinesView";

	private static final String RM_NODES_VIEW_ID = "org.eclipse.ptp.rm.ui.views.NodesView";

	private static final String RM_QUEUES_VIEW_ID = "org.eclipse.ptp.rm.ui.views.QueuesView";

	private static final String RM_RESOURCEMANAGERS_VIEW_ID = "org.eclipse.ptp.rm.ui.views.ResourceManagersView";

	public void createInitialLayout(IPageLayout layout) {
		// Get the editor area.
		String editorArea = layout.getEditorArea();
		
		// Put the Outline view on the left
		layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.LEFT, 0.25f, editorArea);
		
		// Put the ResourceManagersView on the bottom with the Tasks view.
		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.66f, editorArea);
		bottom.addView(RM_RESOURCEMANAGERS_VIEW_ID);
		bottom.addView(RM_MACHINES_VIEW_ID);
		bottom.addView(RM_NODES_VIEW_ID);
		bottom.addView(RM_QUEUES_VIEW_ID);
		bottom.addView(RM_JOBS_VIEW_ID);
		bottom.addView(IPageLayout.ID_TASK_LIST);
		bottom.addPlaceholder(IPageLayout.ID_PROBLEM_VIEW);
		
		// No actions to add yet.
		// TODO add actions
	}

}
