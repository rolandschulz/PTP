/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ptp.ui.views.ParallelJobsView;

public class JobFocusAction extends Action {
	private ParallelJobsView view = null;
	
	public JobFocusAction(ParallelJobsView view) {
		super(Messages.JobFocusAction_0, IAction.AS_CHECK_BOX);
		this.view = view;
		setToolTipText(Messages.JobFocusAction_1);
	    setImageDescriptor(ParallelImages.ID_ICON_JOB_FOCUS_ENABLE);
	    setDisabledImageDescriptor(ParallelImages.ID_ICON_JOB_FOCUS_DISABLE);
	    this.setChecked(true);
	}

	public void run() {
		view.setJobFocus(isChecked());
	}
}
