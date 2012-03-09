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
import org.eclipse.ui.console.IConsoleConstants;

public class LMLRuntimePerspectiveFactory implements IPerspectiveFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui .IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		final String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		final IFolderLayout mainFolder = layout.createFolder("mainFolder", IPageLayout.LEFT, (float) 0.4, editorArea); //$NON-NLS-1$
		final IFolderLayout jobsFolder = layout.createFolder("jobsFolder", IPageLayout.BOTTOM, (float) 0.15, "mainFolder"); //$NON-NLS-1$ //$NON-NLS-2$
		final IFolderLayout miscFolder = layout.createFolder("miscFolder", IPageLayout.BOTTOM, (float) 0.5, "jobsFolder"); //$NON-NLS-1$//$NON-NLS-2$
		final IFolderLayout machinesFolder = layout.createFolder("machinesFolder", IPageLayout.BOTTOM, 0, editorArea); //$NON-NLS-1$

		mainFolder.addView("org.eclipse.ptp.ui.views.resourceManagerView"); //$NON-NLS-1$

		jobsFolder.addView(ILMLUIConstants.VIEW_TABLE_1);
		jobsFolder.addView(ILMLUIConstants.VIEW_TABLE_2);

		machinesFolder.addView(ILMLUIConstants.VIEW_PARALLELNODES);

		miscFolder.addView(ILMLUIConstants.VIEW_INFO);
		miscFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		miscFolder.addView(IPageLayout.ID_PROP_SHEET);
		miscFolder.addView(IPageLayout.ID_PROBLEM_VIEW);
		miscFolder.addView(IPageLayout.ID_TASK_LIST);
	}
}
