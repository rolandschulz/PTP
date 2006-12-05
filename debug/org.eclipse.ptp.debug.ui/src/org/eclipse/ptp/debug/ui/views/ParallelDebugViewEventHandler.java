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
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.events.IPDebugErrorInfo;
import org.eclipse.ptp.debug.core.events.IPDebugEvent;
import org.eclipse.ptp.debug.core.events.IPDebugInfo;
import org.eclipse.ptp.debug.core.events.IPDebugRegisterInfo;
import org.eclipse.ptp.debug.internal.ui.PDebugUIUtils;
import org.eclipse.ptp.debug.internal.ui.UIDebugManager;
import org.eclipse.ptp.debug.internal.ui.views.AbstractPDebugEventHandler;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;

/**
 * @author Clement chu
 */
public class ParallelDebugViewEventHandler extends AbstractPDebugEventHandler {
	/**
	 * Constructs a new event handler on the given view
	 * 
	 * @param view signals view
	 */
	public ParallelDebugViewEventHandler(ParallelDebugView view) {
		super(view);
	}
	public ParallelDebugView getPView() {
		return (ParallelDebugView)getView();
	}
	public void refresh(boolean all) {
		getPView().refresh(all);
	}
	protected void doHandleDebugEvent(IPDebugEvent event, IProgressMonitor monitor) {
		IPDebugInfo info = event.getInfo();
		IPJob job = info.getJob();
		switch(event.getKind()) {
			case IPDebugEvent.CREATE:
				switch (event.getDetail()) {
				case IPDebugEvent.DEBUGGER:
					PTPDebugUIPlugin.getUIDebugManager().defaultRegister((IPCDISession)event.getSource());
					refresh();
					break;
				case IPDebugEvent.REGISTER:
					boolean refresh = true;
					if (info instanceof IPDebugRegisterInfo) {
						refresh = ((IPDebugRegisterInfo)info).isRefresh();
					}
					int[] processes = info.getAllRegisteredProcesses().toArray();
					if (refresh) {
						IElementHandler elementHandler = getPView().getElementHandler(job.getIDString());
						for (int j=0; j<processes.length; j++) {
							//IPProcess proc = job.findProcessByTaskId(processes[j]);
							IElement element = elementHandler.getSetRoot().get(String.valueOf(processes[j]));
							element.setRegistered(true);
							elementHandler.addRegisterElement(element);
						}
						refresh();
					}
					
					//if (processes.length > 0) {
						//getPView().focusOnDebugTarget(job, processes[0]);
					//}
					break;
				}
				break;
			case IPDebugEvent.TERMINATE:
				switch (event.getDetail()) {
				case IPDebugEvent.DEBUGGER:
					refresh(true);
					break;
				case IPDebugEvent.REGISTER:
					boolean refresh = true;
					if (info instanceof IPDebugRegisterInfo) {
						refresh = ((IPDebugRegisterInfo)info).isRefresh();
					}
					int[] processes = info.getAllUnregisteredProcesses().toArray();
					if (refresh) {
						IElementHandler elementHandler = getPView().getElementHandler(job.getIDString());
						for (int j = 0; j < processes.length; j++) {
							//IPProcess proc = job.findProcessByTaskId(processes[j]);
							IElement element = elementHandler.getSetRoot().get(String.valueOf(processes[j]));
							element.setRegistered(false);
							elementHandler.removeRegisterElement(element);
						}
						refresh();
					}
					break;
				default:
					if (job.getIDString().equals(getPView().getCurrentID())) {
						BitList tsource = (BitList) job.getAttribute(IAbstractDebugger.TERMINATED_PROC_KEY);
						BitList ttarget = (BitList) job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY);
						getPView().updateTerminateButton(tsource, ttarget);
					}
					refresh(true);
					break;
				}
				break;
			case IPDebugEvent.RESUME:
			case IPDebugEvent.SUSPEND:
				if (job.getIDString().equals(getPView().getCurrentID())) {
					BitList ssource = (BitList)job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY);
					BitList starget = (BitList)job.getAttribute(IAbstractDebugger.TERMINATED_PROC_KEY);
					getPView().updateSuspendResumeButton(ssource, starget);

					((UIDebugManager) getPView().getUIManager()).updateVariableValue(false);
					if (event.getKind() == IPDebugEvent.SUSPEND) {
						PTPDebugUIPlugin.getUIDebugManager().updateVariableValueOnSuspend();
					}
				}
				refresh();
				break;
			case IPDebugEvent.CHANGE:
				/*
				int detail = event.getDetail();
				if (detail == IPDebugEvent.EVALUATION || detail == IPDebugEvent.CONTENT) {
					int[] diffTasks = info.getAllProcesses().toArray();
					IElementHandler elementHandler = getPView().getElementHandler(job.getIDString());
					for (int j=0; j<diffTasks.length; j++) {
						IElement element = elementHandler.getSetRoot().get(String.valueOf(diffTasks[j]));
						if (element instanceof DebugElement) {
							if (detail == IPDebugEvent.EVALUATION) {
								((DebugElement)element).setType(DebugElement.VALUE_DIFF);
							}
							else {
								((DebugElement)element).resetType();
							}
						}
					}
				}
				*/
				refresh();
				break;
			case IPDebugEvent.ERROR:
				final IPDebugErrorInfo errInfo = (IPDebugErrorInfo)info;
				PTPDebugUIPlugin.getDisplay().asyncExec(new Runnable() {
					public void run() {
						String msg = "Error on tasks: "+ PDebugUIUtils.showBitList(errInfo.getAllProcesses()) + " - " + errInfo.getMsg();
						IStatus status = new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, msg, null);
						PTPDebugUIPlugin.errorDialog("Error", status);
					}
				});
				refresh(true);
				break;
		}
	}
}
