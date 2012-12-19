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
package org.eclipse.ptp.debug.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.event.IPDebugErrorInfo;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.debug.core.event.IPDebugRegisterInfo;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.internal.ui.PDebugUIUtils;
import org.eclipse.ptp.debug.internal.ui.views.AbstractPDebugViewEventHandler;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.UIDebugManager;
import org.eclipse.ptp.debug.ui.messages.Messages;
import org.eclipse.ptp.ui.model.IElementHandler;

/**
 * @author Clement chu
 */
public class ParallelDebugViewEventHandler extends AbstractPDebugViewEventHandler {
	private long time_record = 0;

	/**
	 * Constructs a new event handler on the given view
	 */
	public ParallelDebugViewEventHandler(ParallelDebugView view) {
		super(view);
	}

	public ParallelDebugView getPView() {
		return (ParallelDebugView) getView();
	}

	@Override
	public void refresh(boolean all) {
		if (getPView().isVisible()) {
			getPView().refresh(all);
		}
	}

	@Override
	protected void doHandleDebugEvent(IPDebugEvent event, IProgressMonitor monitor) {
		IPDebugInfo info = event.getInfo();
		final String jobId = info.getLaunch().getJobId();
		switch (event.getKind()) {
		case IPDebugEvent.CREATE:
			switch (event.getDetail()) {
			case IPDebugEvent.REGISTER:
				boolean refresh = true;
				if (info instanceof IPDebugRegisterInfo) {
					refresh = ((IPDebugRegisterInfo) info).isRefresh();
				}
				int[] c_regTask_array = info.getAllTasks().toArray();
				if (refresh) {
					IElementHandler elementHandler = getPView().getElementHandler(jobId);
					if (elementHandler != null) {
						elementHandler.register(info.getAllTasks());
					}
					refresh();
				}
				if (c_regTask_array.length > 0) {
					getPView().focusOnDebugTarget(jobId, c_regTask_array[0]);
				}
				break;
			case IPDebugEvent.DEBUGGER:
				IJobStatus job = JobManager.getInstance().getJob(info.getLaunch().getJobControl().getControlId(), jobId);
				if (job != null) {
					getPView().changeJobRefresh(job);
				}
				break;
			case IPDebugEvent.BREAKPOINT:
				break;
			}
			break;
		case IPDebugEvent.TERMINATE:
			IElementHandler elementHandler = getPView().getElementHandler(jobId);
			switch (event.getDetail()) {
			case IPDebugEvent.DEBUGGER:
				if (elementHandler != null) {
					elementHandler.removeAllRegistered();
				}
				refresh(true);
				break;
			case IPDebugEvent.REGISTER:
				boolean refresh = true;
				if (info instanceof IPDebugRegisterInfo) {
					refresh = ((IPDebugRegisterInfo) info).isRefresh();
				}
				if (refresh) {
					if (elementHandler != null) {
						elementHandler.unRegister(info.getAllTasks());
					}
					refresh();
				}
				break;
			case IPDebugEvent.BREAKPOINT:
				break;
			case IPDebugEvent.PROCESS_SPECIFIC:
				UIDebugManager uiMgr = (UIDebugManager) getPView().getUIManager();
				IPSession session = uiMgr.getDebugSession(jobId);
				if (session != null) {
					uiMgr.unregisterTasks(session, info.getAllRegisteredTasks());
				}
				refresh(true);
				break;
			default:
				refresh(true);
				break;
			}
			break;
		case IPDebugEvent.RESUME:
			time_record = System.currentTimeMillis();
			System.err.println("================= TIME RESUME: " + time_record); //$NON-NLS-1$
			// ((UIDebugManager)
			// getPView().getUIManager()).updateVariableValue(false,
			// info.getAllRegisteredTasks());
			refresh(true);
			break;
		case IPDebugEvent.SUSPEND:
			IPSession s = ((UIDebugManager) getPView().getUIManager()).getDebugSession(jobId);
			if (s != null) {
				if (s.getTasks().cardinality() == s.getPDISession().getTaskManager().getSuspendedTasks().cardinality()) {
					System.err.println("================= TIME ALL SUSPENDED: " + (System.currentTimeMillis() - time_record)); //$NON-NLS-1$
					time_record = System.currentTimeMillis();
				}
			}

			int[] processes = info.getAllRegisteredTasks().toArray();
			if (processes.length > 0) {
				getPView().focusOnDebugTarget(jobId, processes[0]);
			}
			// if (job.getID().equals(getPView().getCurrentID())) {
			// PTPDebugUIPlugin.getUIDebugManager().updateVariableValueOnSuspend(info.getAllTasks());
			// }
			refresh(true);
			break;
		case IPDebugEvent.CHANGE:
			switch (event.getDetail()) {
			case IPDebugEvent.PROCESS_SPECIFIC:
				UIDebugManager uiMgr = (UIDebugManager) getPView().getUIManager();
				if (uiMgr.isEnabledDefaultRegister()) {
					IPSession session = uiMgr.getDebugSession(jobId);
					if (session != null) {
						uiMgr.registerTasks(session, session.getTasks(0));
					}
				}
				break;
			case IPDebugEvent.EVALUATION:
			case IPDebugEvent.CONTENT:
				/*
				 * int[] diffTasks = info.getAllProcesses().toArray(); IElementHandler elementHandler =
				 * getPView().getElementHandler(job.getID()); for (int j=0; j<diffTasks.length; j++) { IElement element =
				 * elementHandler.getSetRoot ().get(String.valueOf(diffTasks[j])); if (element instanceof DebugElement) { if (detail
				 * == IPDebugEvent.EVALUATION) { ((DebugElement)element).setType(DebugElement.VALUE_DIFF); } else {
				 * ((DebugElement)element).resetType(); } } }
				 */
				break;
			}
			break;
		case IPDebugEvent.ERROR:
			final IPDebugErrorInfo errInfo = (IPDebugErrorInfo) info;
			if (event.getDetail() != IPDebugEvent.ERR_NORMAL) {
				PTPDebugUIPlugin.getDisplay().asyncExec(new Runnable() {
					public void run() {
						String msg = NLS.bind(
								Messages.ParallelDebugViewEventHandler_2,
								new Object[] { PDebugUIUtils.showBitList(errInfo.getAllTasks()), errInfo.getMsg(),
										errInfo.getDetailsMsg() });
						IStatus status = new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, msg, null);
						PTPDebugUIPlugin.errorDialog(Messages.ParallelDebugViewEventHandler_3, status);
					}
				});
				// only change process icon and unregister for fatal error
				if (event.getDetail() == IPDebugEvent.ERR_FATAL) {
					IElementHandler eHandler = getPView().getElementHandler(jobId);
					if (eHandler != null) {
						eHandler.unRegister(info.getAllRegisteredTasks());
					}
					refresh(true);
				}
			}
			break;
		}
	}

	private String getProcessId(IPLaunch launch, int task) {
		return Integer.toString(task);
	}
}
