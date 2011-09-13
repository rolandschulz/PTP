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
package org.eclipse.ptp.internal.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.ui.IElementManager;
import org.eclipse.ptp.ui.IJobManager;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;

/**
 * @author Clement chu
 * 
 */
public class TerminateJobAction extends ParallelAction {
	public static final String name = Messages.TerminateJobAction_0;

	/**
	 * Constructor
	 * 
	 * @param view
	 */
	public TerminateJobAction(AbstractParallelElementView view) {
		super(name, view);
		setImageDescriptor(ParallelImages.ID_ICON_TERMINATE_JOB_NORMAL);
		setDisabledImageDescriptor(ParallelImages.ID_ICON_TERMINATE_JOB_DISABLE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		IElementManager manager = view.getUIManager();
		if (manager instanceof IJobManager) {
			boolean terminate = MessageDialog.openConfirm(getShell(), Messages.TerminateJobAction_1, NLS.bind(
					Messages.TerminateJobAction_2, ((IJobManager) manager).getJob() != null ? ((IJobManager) manager).getJob()
							.getName() : "<empty>")); //$NON-NLS-1$
			if (terminate) {
				try {
					((IJobManager) manager).terminateJob();
				} catch (CoreException e) {
					ErrorDialog.openError(getShell(), Messages.TerminateJobAction_3, Messages.TerminateJobAction_4, e.getStatus());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.actions.ParallelAction#run(org.eclipse.ptp.ui.model.IElement[])
	 */
	@Override
	public void run(IElement[] elements) {
	}
}
