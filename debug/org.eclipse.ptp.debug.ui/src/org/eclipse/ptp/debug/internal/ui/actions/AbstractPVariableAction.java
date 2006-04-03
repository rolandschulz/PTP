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
package org.eclipse.ptp.debug.internal.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.model.IPVariableManager.IPVariableListener;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.ui.listeners.IJobChangeListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Clement chu
 * 
 */
public abstract class AbstractPVariableAction implements IObjectActionDelegate, IActionDelegate2, IViewActionDelegate, IPVariableListener, IJobChangeListener {
	protected IViewPart view = null;
	protected IAction action = null;

	public void init(IViewPart view) {
		this.view = view;
	}
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.action = action;
	}
	public void dispose() {
		PTPDebugCorePlugin.getPVariableManager().removeListener(this);
		PTPDebugUIPlugin.getDefault().getUIDebugManager().removeJobChangeListener(this);
	}
	public void init(IAction action) {
		this.action = action;
		PTPDebugCorePlugin.getPVariableManager().addListener(this);
		PTPDebugUIPlugin.getDefault().getUIDebugManager().addJobChangeListener(this);
		setEnable();
	}
	public void update(IPJob job) {
		if (PTPDebugUIPlugin.getDefault().getUIDebugManager().getCurrentJob().equals(job)) {
			setEnable();
		}
	}
	public void changeJobEvent(String cur_job_id, String pre_job_id) {
		if (cur_job_id == null || cur_job_id == "") {
			if (action != null)
				action.setEnabled(false);
		}
		else {
			setEnable();
		}
	}
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
	public void selectionChanged(IAction action, ISelection selection) {}
	protected static IPJob getCurrentRunningJob() {
		IPJob job = PTPDebugUIPlugin.getDefault().getUIDebugManager().getCurrentJob();
		return isRunning(job)?job:null;
	}
	protected static boolean isRunning(IPJob job) {
		return (job != null && !job.isAllStop());
	}
	public void setEnable() {
		if (action != null) {
			IPJob job = getCurrentRunningJob();
			action.setEnabled(job != null && PTPDebugCorePlugin.getPVariableManager().hasVariable(job));
		}
	}
}

