/**
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 */

package org.eclipse.ptp.rm.lml.internal.ui;

import org.eclipse.ptp.rm.lml.ui.ILMLUIConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class LMLRuntimePerspectiveFactory implements IPerspectiveFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui
	 * .IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		IFolderLayout lguiFolder = layout.createFolder("lguiFolder", IPageLayout.LEFT, (float) 0.4, editorArea); //$NON-NLS-1$
		IFolderLayout tableFolder = layout.createFolder("jobsFolder", IPageLayout.BOTTOM, (float) 0.25, "lguiFolder"); //$NON-NLS-1$ //$NON-NLS-2$
		IFolderLayout machinesFolder = layout.createFolder("machinesFolder", IPageLayout.BOTTOM, (float) 0, editorArea); //$NON-NLS-1$ //$NON-NLS-2$
		
//		lguiFolder.addView(ILMLUIConstants.VIEW_LML);
		lguiFolder.addView("org.eclipse.ptp.ui.views.resourceManagerView");

		tableFolder.addPlaceholder(ILMLUIConstants.VIEW_TABLE + ":*");
		tableFolder.addView(ILMLUIConstants.VIEW_TABLE);
		machinesFolder.addPlaceholder(ILMLUIConstants.VIEW_PARALLELNODES + ":*");
		machinesFolder.addView(ILMLUIConstants.VIEW_PARALLELNODES);
	}
}
