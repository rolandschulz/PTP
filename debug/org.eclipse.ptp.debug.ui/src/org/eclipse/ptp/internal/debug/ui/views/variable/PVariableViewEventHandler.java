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
package org.eclipse.ptp.internal.debug.ui.views.variable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.internal.debug.ui.PVariableManager;
import org.eclipse.ptp.internal.debug.ui.views.AbstractPDebugViewEventHandler;
import org.eclipse.ptp.internal.ui.listeners.IJobChangedListener;

/**
 * @author Clement chu
 */
public class PVariableViewEventHandler extends AbstractPDebugViewEventHandler implements IJobChangedListener {
	private final PVariableManager varMgr;

	/**
	 * Constructs a new event handler on the given view
	 * 
	 * @param view
	 *            variable viewer
	 */
	public PVariableViewEventHandler(PVariableView view) {
		super(view);
		varMgr = PTPDebugUIPlugin.getUIDebugManager().getJobVariableManager();
		PTPDebugUIPlugin.getUIDebugManager().addJobChangedListener(this);
	}

	@Override
	public void dispose() {
		PTPDebugUIPlugin.getUIDebugManager().removeJobChangedListener(this);
		super.dispose();
	}

	public PVariableView getPVariableView() {
		return (PVariableView) getView();
	}

	@Override
	public void refresh(boolean all) {
		if (getPVariableView().isVisible()) {
			getPVariableView().refresh();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.ui.views.AbstractPDebugViewEventHandler
	 * #doHandleDebugEvent(org.eclipse.ptp.debug.core.event.IPDebugEvent,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doHandleDebugEvent(IPDebugEvent event, IProgressMonitor monitor) {
		switch (event.getKind()) {
		case IPDebugEvent.RESUME:
			IPDebugInfo info = event.getInfo();
			varMgr.resetValue(info.getLaunch().getJobId(), info.getAllTasks());
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.ui.listeners.IJobChangedListener#jobChangedEvent(java
	 * .lang.String, java.lang.String)
	 */
	public void jobChangedEvent(int type, String cur_job_id, String pre_job_id) {
		switch (type) {
		case IJobChangedListener.CHANGED:
			// refresh();
			break;
		case IJobChangedListener.REMOVED:
			if (pre_job_id != null) {
				varMgr.removeVariable(pre_job_id);
				refresh();
			}
			break;
		}
		getPVariableView().updateActionsEnable();
	}
}
