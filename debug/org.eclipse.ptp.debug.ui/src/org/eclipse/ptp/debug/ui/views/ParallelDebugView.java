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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.internal.ui.UIDebugManager;
import org.eclipse.ptp.debug.internal.ui.actions.RegisterAction;
import org.eclipse.ptp.debug.internal.ui.actions.ResumeAction;
import org.eclipse.ptp.debug.internal.ui.actions.StepIntoAction;
import org.eclipse.ptp.debug.internal.ui.actions.StepOverAction;
import org.eclipse.ptp.debug.internal.ui.actions.StepReturnAction;
import org.eclipse.ptp.debug.internal.ui.actions.SuspendAction;
import org.eclipse.ptp.debug.internal.ui.actions.TerminateAction;
import org.eclipse.ptp.debug.internal.ui.actions.UnregisterAction;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.events.IDebugActionEvent;
import org.eclipse.ptp.debug.ui.events.IResumedDebugEvent;
import org.eclipse.ptp.debug.ui.events.ISuspendedDebugEvent;
import org.eclipse.ptp.debug.ui.events.ITerminatedDebugEvent;
import org.eclipse.ptp.debug.ui.listeners.IDebugActionUpdateListener;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.views.IIconCanvasActionListener;
import org.eclipse.ptp.ui.views.ParallelJobView;
import org.eclipse.ui.IActionBars;

/**
 * @author clement chu
 * 
 */
public class ParallelDebugView extends ParallelJobView implements IDebugActionUpdateListener {
	private static ParallelDebugView instance = null;
	// actions
	protected ParallelAction resumeAction = null;
	protected ParallelAction suspendAction = null;
	protected ParallelAction terminateAction = null;
	protected ParallelAction stepIntoAction = null;
	protected ParallelAction stepOverAction = null;
	protected ParallelAction stepReturnAction = null;
	protected ParallelAction registerAction = null;
	protected ParallelAction unregisterAction = null;
	
	/** Constructor
	 * 
	 */
	public ParallelDebugView() {
		instance = this;
		manager = PTPDebugUIPlugin.getDefault().getUIDebugManager();
		((UIDebugManager) manager).addDebugEventListener(this);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		((UIDebugManager) manager).removeDebugEventListener(this);
		super.dispose();
	}
	/** Get instance
	 * @return
	 */
	public static ParallelDebugView getDebugViewInstance() {
		if (instance == null)
			instance = new ParallelDebugView();
		return instance;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, resumeAction);
		manager.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, suspendAction);
		manager.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, terminateAction);
		manager.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, new Separator());
		manager.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, stepIntoAction);
		manager.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, stepOverAction);
		manager.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, stepReturnAction);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#createToolBarActions(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void createToolBarActions(IToolBarManager toolBarMgr) {
		resumeAction = new ResumeAction(this);
		suspendAction = new SuspendAction(this);
		terminateAction = new TerminateAction(this);
		stepIntoAction = new StepIntoAction(this);
		stepOverAction = new StepOverAction(this);
		stepReturnAction = new StepReturnAction(this);
		registerAction = new RegisterAction(this);
		unregisterAction = new UnregisterAction(this);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, resumeAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, suspendAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, terminateAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, new Separator());
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, stepIntoAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, stepOverAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, stepReturnAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, new Separator());
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, registerAction);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, unregisterAction);
		
		super.buildInToolBarActions(toolBarMgr);
		
		//createOrientationActions();
	}
	/** Create orientation actions
	 * 
	 */
	protected void createOrientationActions() {
		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager viewMenu = actionBars.getMenuManager();
		viewMenu.add(new Separator());
	}
	
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * IIconCanvasActionListener
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.IIconCanvasActionListener#handleAction(int, int)
	 */
	public void handleAction(int type, int index) {
		IElement element = canvas.getElement(index);
		if (type == IIconCanvasActionListener.SELECTION_ACTION) {
			if (element.isRegistered()) {
				IPJob job = ((UIDebugManager) manager).findJobById(getCurrentID());
				((UIDebugManager) manager).focusOnDebugTarget(job, Integer.parseInt(element.getName()));
			}
		}
		else if (type == IIconCanvasActionListener.DOUBLE_CLICK_ACTION) {
			doubleClick(element);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#doubleClick(org.eclipse.ptp.ui.model.IElement)
	 */
	public void doubleClick(IElement element) {
		try {
			registerElement(element);
		} catch (CoreException e) {
			PTPDebugUIPlugin.errorDialog(getViewSite().getShell(), "Error", e.getStatus());
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getToolTipText(java.lang.Object)
	 */
	public String[] getToolTipText(Object obj) {
		String[] header = super.getToolTipText(obj);
		IPJob job = ((UIDebugManager) manager).findJobById(getCurrentID());
		String variableText = ((UIDebugManager) manager).getValueText(job, ((IPProcess)obj).getTaskId());
		if (variableText != null && variableText.length() > 0) {
			return new String[] { header[0], variableText };
		}
		return header;
	}
	
	/** Register element
	 * @param element
	 * @throws CoreException
	 */
	public void registerElement(IElement element) throws CoreException {
		if (element.isRegistered())
			((UIDebugManager) manager).unregisterElements(new IElement[] { element });
		else
			((UIDebugManager) manager).registerElements(new IElement[] { element });
	}
	/** Register selected elements
	 * @throws CoreException
	 */
	public void registerSelectedElements() throws CoreException {
		if (cur_element_set != null) {
			((UIDebugManager) manager).registerElements(canvas.getSelectedElements());
		}
	}
	/** Unregister selected elements
	 * @throws CoreException
	 */
	public void unregisterSelectedElements() throws CoreException {
		if (cur_element_set != null) {
			((UIDebugManager) manager).unregisterElements(canvas.getSelectedElements());
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IParallelModelListener#start()
	 */
	public void start() {
		System.out.println("------------ debug start");
		refresh();
	}
	// Update button
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#updateAction()
	 */
	protected void updateAction() {
		super.updateAction();
		IPJob job = ((UIDebugManager) manager).findJobById(getCurrentID());
		boolean isDebugMode = ((UIDebugManager) manager).isDebugMode(job);
		boolean isRunning = ((UIDebugManager) manager).isRunning(job);
		registerAction.setEnabled(isRunning && isDebugMode);
		unregisterAction.setEnabled(isRunning && isDebugMode);
		suspendAction.setEnabled(isRunning && isDebugMode);
		terminateAction.setEnabled(isRunning);
		if (isRunning && isDebugMode) {
			IElementHandler elementHandler = getCurrentElementHandler();
			if (elementHandler != null) {
				IElementSet set = getCurrentSet();
				BitList suspendedTaskList = (BitList) job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY);
				BitList terminatedTaskList = (BitList) job.getAttribute(IAbstractDebugger.TERMINATED_PROC_KEY);
				updateSuspendResumeButton(suspendedTaskList, set, terminatedTaskList);
				updateTerminateButton(terminatedTaskList, set, suspendedTaskList);
			}
		} else
			setEnableResumeButtonGroup(false);
	}
	/** Update buttons to enable / disable
	 * @param tasks
	 * @param set
	 * @param targetTasks
	 */
	public void updateSuspendResumeButton(BitList tasks, IElementSet set, BitList targetTasks) {
		if (set == null || tasks == null)
			return;
		boolean isEnabled = false;
		if (set.isRootSet()) {
			isEnabled = !tasks.isEmpty();// tasks != 0: some processes suspended
			suspendAction.setEnabled(set.size() != tasks.cardinality()); // disable suspend Action if all tasks same as root size
		} else {
			try {
				BitList setTasks = ((UIDebugManager) manager).getTasks(getCurrentID(), set.getID());
				// this set contains some suspended processes
				isEnabled = setTasks.intersects(tasks);
				BitList refTasks = tasks.copy();
				refTasks.and(setTasks);
				// the size is not equal: there is some processes running
				suspendAction.setEnabled(set.size() != refTasks.cardinality());
			} catch (CoreException e) {
				PTPDebugUIPlugin.log(e);
			}
		}
		setEnableResumeButtonGroup(isEnabled);
	}
	/** Set buttons enable / disable
	 * has suspend = resume enable
	 * has running = suspend enable
	 * @param isEnabled
	 */
	private void setEnableResumeButtonGroup(boolean isEnabled) {
		resumeAction.setEnabled(isEnabled);
		stepIntoAction.setEnabled(isEnabled);
		stepOverAction.setEnabled(isEnabled);
		// stepReturnAction.setEnabled(isEnabled);
	}
	/** Updtae terminate button
	 * @param tasks
	 * @param set
	 * @param targetTasks
	 */
	public void updateTerminateButton(BitList tasks, IElementSet set, BitList targetTasks) {
		if (set == null || tasks == null)
			return;
		int setSize = set.size();
		int totalTerminatedSize = tasks.cardinality();
		int totalSuspendedSize = (targetTasks == null || targetTasks.isEmpty() ? 0 : targetTasks.cardinality());
		// size equals: all processes are terminated
		boolean isEnabled = (setSize != totalTerminatedSize);
		if (!set.isRootSet()) {
			try {
				BitList setTasks = ((UIDebugManager) manager).getTasks(getCurrentID(), set.getID());
				setSize = setTasks.cardinality();
				BitList refTasks = tasks.copy();
				refTasks.and(setTasks);
				// size equals: the set contains all terminated processes
				totalTerminatedSize = refTasks.cardinality();
				isEnabled = (setSize != totalTerminatedSize);
				if (isEnabled) {
					BitList tarRefTasks = targetTasks.copy();
					tarRefTasks.and(setTasks);
					totalSuspendedSize = tarRefTasks.cardinality();
				}
			} catch (CoreException e) {
				PTPDebugUIPlugin.log(e);
			}
		}
		terminateAction.setEnabled(isEnabled);
		if (isEnabled) {// not all processes terminated
			setEnableResumeButtonGroup(totalSuspendedSize > 0);
			// no suspended process or running process: total terminated + total suspended != set size
			suspendAction.setEnabled(totalSuspendedSize == 0 || (setSize != (totalTerminatedSize + totalSuspendedSize)));
		} else {// all process terminated
			setEnableResumeButtonGroup(false);
			suspendAction.setEnabled(false);
		}
	}
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * Debug Action Event
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.ui.listeners.IDebugActionUpdateListener#handleDebugActionEvent(org.eclipse.ptp.debug.ui.events.IDebugActionEvent)
	 */
	public void handleDebugActionEvent(IDebugActionEvent event) {
		String job_id = event.getJobId();
		// only take action with current job
		if (!job_id.equals(getCurrentID()) || !((UIDebugManager) manager).isDebugMode(job_id)) {
			return;
		}
		BitList tasks = (BitList) event.getSource();
		BitList targetTasks = (BitList) event.getTarget();
		IElementSet set = getCurrentSet();
		if (event instanceof ISuspendedDebugEvent || event instanceof IResumedDebugEvent)
			updateSuspendResumeButton(tasks, set, targetTasks);
		else if (event instanceof ITerminatedDebugEvent)
			updateTerminateButton(tasks, set, targetTasks);
	}
}
