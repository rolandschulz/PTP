/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.internal.ui.actions;

import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.managers.JobManager;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;
import org.eclipse.ptp.ui.views.ParallelJobsView;

public class RemoveSelectedJobAction extends ParallelAction {
	public static final String name = "Remove Selected Job";
	AbstractTableViewer jobViewer;

	public RemoveSelectedJobAction(AbstractParallelElementView view,
			AbstractTableViewer viewer) {
		super(name, view);
		jobViewer = viewer;
		setImageDescriptor(ParallelImages.ID_ICON_REMOVETERMINATED_NORMAL);
	}

	@Override
	/**
	 * Remove the specified job from the jobs view.
	 * @param elements - should contain the set of selected elements at the time the action was invoked
	 */
	public void run(IElement[] elements) {
		ISelection selection;
		IPJob selectedJob;

		selection = jobViewer.getSelection();
		if (!selection.isEmpty()) {
			selectedJob = (IPJob) ((IStructuredSelection) selection)
					.getFirstElement();
			((JobManager) view.getUIManager()).removeJob(selectedJob);
			((ParallelJobsView) view).changeJobRefresh(null);
		}
	}

}
