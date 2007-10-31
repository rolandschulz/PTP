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
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.event.IPDebugErrorInfo;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.debug.core.event.IPDebugRegisterInfo;
import org.eclipse.ptp.debug.internal.ui.PDebugUIUtils;
import org.eclipse.ptp.debug.internal.ui.UIDebugManager;
import org.eclipse.ptp.debug.internal.ui.views.AbstractPDebugViewEventHandler;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;

/**
 * @author Clement chu
 */
public class ParallelDebugViewEventHandler extends AbstractPDebugViewEventHandler  {
	/**
	 * Constructs a new event handler on the given view
	 */
	public ParallelDebugViewEventHandler(ParallelDebugView view) {
		super(view);
	}
	public ParallelDebugView getPView() {
		return (ParallelDebugView)getView();
	}
	public void refresh(boolean all) {
		if (getPView().isVisible())
			getPView().refresh(all);
	}
	protected void doHandleDebugEvent(IPDebugEvent event, IProgressMonitor monitor) {
		IPDebugInfo info = event.getInfo();
		final IPJob job = info.getJob();
		switch(event.getKind()) {
			case IPDebugEvent.CREATE:
				switch (event.getDetail()) {
				case IPDebugEvent.REGISTER:
					boolean refresh = true;
					if (info instanceof IPDebugRegisterInfo) {
						refresh = ((IPDebugRegisterInfo)info).isRefresh();
					}
					int[] c_regTask_array = info.getAllTasks().toArray();
					if (refresh) {
						IElementHandler elementHandler = getPView().getElementHandler(job.getID());
						if (elementHandler != null) {
							IElement[] regElementArray = new IElement[c_regTask_array.length];
							for (int j=0; j<c_regTask_array.length; j++) {
								IPProcess proc = job.getProcessByIndex(c_regTask_array[j]);
								((UIDebugManager) getPView().getUIManager()).addConsoleWindow(job, proc);
								regElementArray[j] = elementHandler.getSetRoot().getElementByID(proc.getID());
							}
							elementHandler.addToRegister(regElementArray);
						}
						refresh();
					}
					if (c_regTask_array.length > 0) {
						getPView().focusOnDebugTarget(job, c_regTask_array[0]);
					}
					break;
				case IPDebugEvent.DEBUGGER:
					break;
				case IPDebugEvent.BREAKPOINT:
					break;
				}
				break;
			case IPDebugEvent.TERMINATE:
				IElementHandler elementHandler = getPView().getElementHandler(job.getID());
				switch (event.getDetail()) {
				case IPDebugEvent.DEBUGGER:
					if (elementHandler != null) {
						elementHandler.removeElements(elementHandler.getRegistered());
					}
					refresh(true);
					break;
				case IPDebugEvent.REGISTER:
					boolean refresh = true;
					if (info instanceof IPDebugRegisterInfo) {
						refresh = ((IPDebugRegisterInfo)info).isRefresh();
					}
					if (refresh) {
						if (elementHandler != null) {
							int[] t_regTask_array = info.getAllTasks().toArray();
							IElement[] regElementArray = new IElement[t_regTask_array.length];
							for (int j = 0; j < t_regTask_array.length; j++) {
								IPProcess proc = job.getProcessByIndex(t_regTask_array[j]);
								((UIDebugManager) getPView().getUIManager()).removeConsoleWindow(job, proc);
								regElementArray[j] = elementHandler.getSetRoot().getElementByID(proc.getID());
							}
							elementHandler.removeFromRegister(regElementArray);
						}
						refresh();
					}
					break;
				case IPDebugEvent.BREAKPOINT:
					break;
				case IPDebugEvent.PROCESS_SPECIFIC:
					UIDebugManager uiMgr = (UIDebugManager)getPView().getUIManager();
					IPSession session = uiMgr.getDebugSession(job);
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
				//((UIDebugManager) getPView().getUIManager()).updateVariableValue(false, info.getAllRegisteredTasks());
				refresh(true);
				break;
			case IPDebugEvent.SUSPEND:
				int[] processes = info.getAllRegisteredTasks().toArray();
				if (processes.length > 0) {
					getPView().focusOnDebugTarget(job, processes[0]);
				}
				//if (job.getID().equals(getPView().getCurrentID())) {
					//PTPDebugUIPlugin.getUIDebugManager().updateVariableValueOnSuspend(info.getAllTasks());
				//}
				refresh(true);
				break;
			case IPDebugEvent.CHANGE:
				switch (event.getDetail()) {
				case IPDebugEvent.PROCESS_SPECIFIC:
					UIDebugManager uiMgr = (UIDebugManager)getPView().getUIManager();
					if (uiMgr.isEnabledDefaultRegister()) {
						IPSession session = uiMgr.getDebugSession(job);
						if (session != null) {
							uiMgr.registerTasks(session, session.getTasks(0));
						}
					}
					break;
				case IPDebugEvent.EVALUATION:
				case IPDebugEvent.CONTENT:
					/*
					int[] diffTasks = info.getAllProcesses().toArray();
					IElementHandler elementHandler = getPView().getElementHandler(job.getID());
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
					*/
					break;
				}
				break;
			case IPDebugEvent.ERROR:
				final IPDebugErrorInfo errInfo = (IPDebugErrorInfo)info;
				if (event.getDetail() != IPDebugEvent.ERR_NORMAL) {
					PTPDebugUIPlugin.getDisplay().asyncExec(new Runnable() {
						public void run() {
							String msg = "Error on tasks: "+ PDebugUIUtils.showBitList(errInfo.getAllTasks()) + " - " + errInfo.getMsg() + "\nReason: " + errInfo.getDetailsMsg();
							IStatus status = new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, msg, null);
							PTPDebugUIPlugin.errorDialog("Error", status);
						}
					});
					//only change process icon and unregister for fatal error
					if (event.getDetail() == IPDebugEvent.ERR_FATAL) { 
						IElementHandler eHandler = getPView().getElementHandler(job.getID());
						if (eHandler != null) {
							int[] e_regTask_array = info.getAllRegisteredTasks().toArray();
							if (e_regTask_array.length > 0) {
								IElement[] regElementArray = new IElement[e_regTask_array.length];
								for (int j = 0; j < e_regTask_array.length; j++) {
									IPProcess proc = job.getProcessByIndex(e_regTask_array[j]);
									((UIDebugManager) getPView().getUIManager()).removeConsoleWindow(job, proc);
									regElementArray[j] = eHandler.getSetRoot().getElementByID(proc.getID());
								}
								eHandler.removeFromRegister(regElementArray);
							}
						}
						refresh(true);
					}
				}
				break;
		}
	}
}
