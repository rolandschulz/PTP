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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.ptp.debug.internal.ui.views.AbstractPDebugEventHandler;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.views.IIconCanvasActionListener;
import org.eclipse.ptp.ui.views.ParallelJobView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @author clement chu
 * 
 */
public class ParallelDebugView extends ParallelJobView {
	// actions
	protected ParallelAction resumeAction = null;
	protected ParallelAction suspendAction = null;
	protected ParallelAction terminateAction = null;
	protected ParallelAction stepIntoAction = null;
	protected ParallelAction stepOverAction = null;
	protected ParallelAction stepReturnAction = null;
	protected ParallelAction registerAction = null;
	protected ParallelAction unregisterAction = null;
	protected AbstractPDebugEventHandler fEventHandler;
	
	/*
	private MouseAdapter debugViewMouseAdapter = new MouseAdapter() {
		public void mouseUp(MouseEvent event) {
			Object test = event.getSource();
			if (test instanceof Tree) {
				TreeItem[] items = ((Tree)test).getSelection();
				Object[] targets = new Object[items.length];
				for (int i=0; i<items.length; i++) {
					targets[i] = items[i].getData();
				}
				selectElements(targets);
			}
		}
	};
	*/
	/** Constructor
	 * 
	 */
	public ParallelDebugView(IManager manager) {
		super(manager);
	}
	public ParallelDebugView() {
		this(PTPDebugUIPlugin.getUIDebugManager());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		if (getEventHandler() != null) {
			getEventHandler().dispose();
		}
		super.dispose();
	}
	protected Viewer getViewer(String view_id) {
		//IViewPart part = PTPDebugUIPlugin.getActiveWorkbenchWindow().getActivePage().findView(IDebugUIConstants.ID_DEBUG_VIEW);
		IViewPart part = getViewSite().getPage().findView(view_id);
		if (part != null && part instanceof AbstractDebugView) {
			return ((AbstractDebugView)part).getViewer();
		}
		return null;
	}
	protected Viewer getDebugViewer() {
		return getViewer(IDebugUIConstants.ID_DEBUG_VIEW);
	}
	/*
	 * FIXME does not work if create a new set.  Currently we can task id to identify icon, but viewer is using order 
	private void selectElements(final Object[] objects) {
        SafeRunnable.run(new SafeRunnable() {
            public void run() {
				if (!canvas.isDisposed()) {
					canvas.unselectAllElements();
					for (int i=0; i<objects.length; i++) {
						Object obj = objects[i];
						int id = ((UIDebugManager) manager).getSelectedRegisteredTasks(obj);
						if (id > -1) {
							if (!canvas.isSelected(id))
								canvas.selectElement(id);
						}
					}
					canvas.redraw();
					canvas.setCurrentSelection(false);
				}
			}
		});
	}
	*/
		
	/**
	 * Sets the event handler for this view
	 * 
	 * @param eventHandler event handler
	 */
	protected void setEventHandler(AbstractPDebugEventHandler eventHandler) {
		this.fEventHandler = eventHandler;
	}
	/**
	 * Returns the event handler for this view
	 * 
	 * @return The event handler for this view
	 */
	protected AbstractPDebugEventHandler getEventHandler() {
		return this.fEventHandler;
	}	
		
	public void createView(Composite parent) {
		super.createView(parent);
		setEventHandler(new ParallelDebugViewEventHandler(this));
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(new Separator(IPTPDebugUIConstants.THREAD_GROUP));
		manager.add(new Separator(IPTPDebugUIConstants.STEP_GROUP));
		manager.add(new GroupMarker(IPTPDebugUIConstants.STEP_INTO_GROUP));
		manager.add(new GroupMarker(IPTPDebugUIConstants.STEP_OVER_GROUP));
		manager.add(new GroupMarker(IPTPDebugUIConstants.STEP_RETURN_GROUP));
		manager.add(new GroupMarker(IPTPDebugUIConstants.EMPTY_STEP_GROUP));
		manager.add(new Separator(IPTPDebugUIConstants.REG_GROUP));

		manager.appendToGroup(IPTPDebugUIConstants.THREAD_GROUP, resumeAction);
		manager.appendToGroup(IPTPDebugUIConstants.THREAD_GROUP, suspendAction);
		manager.appendToGroup(IPTPDebugUIConstants.THREAD_GROUP, terminateAction);
		manager.appendToGroup(IPTPDebugUIConstants.STEP_INTO_GROUP, stepIntoAction);
		manager.appendToGroup(IPTPDebugUIConstants.STEP_OVER_GROUP, stepOverAction);
		manager.appendToGroup(IPTPDebugUIConstants.EMPTY_STEP_GROUP, stepReturnAction);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#createToolBarActionGroup(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void createToolBarActionGroup(IToolBarManager toolBarMgr) {
		toolBarMgr.add(new Separator(IPTPDebugUIConstants.THREAD_GROUP));
		toolBarMgr.add(new Separator(IPTPDebugUIConstants.STEP_GROUP));
		toolBarMgr.add(new GroupMarker(IPTPDebugUIConstants.STEP_INTO_GROUP));
		toolBarMgr.add(new GroupMarker(IPTPDebugUIConstants.STEP_OVER_GROUP));
		toolBarMgr.add(new GroupMarker(IPTPDebugUIConstants.STEP_RETURN_GROUP));
		toolBarMgr.add(new GroupMarker(IPTPDebugUIConstants.EMPTY_STEP_GROUP));
		toolBarMgr.add(new Separator(IPTPDebugUIConstants.REG_GROUP));
		super.createToolBarActionGroup(toolBarMgr);
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
				
		toolBarMgr.appendToGroup(IPTPDebugUIConstants.THREAD_GROUP, resumeAction);
		toolBarMgr.appendToGroup(IPTPDebugUIConstants.THREAD_GROUP, suspendAction);
		toolBarMgr.appendToGroup(IPTPDebugUIConstants.THREAD_GROUP, terminateAction);
		toolBarMgr.appendToGroup(IPTPDebugUIConstants.STEP_INTO_GROUP, stepIntoAction);
		toolBarMgr.appendToGroup(IPTPDebugUIConstants.STEP_OVER_GROUP, stepOverAction);
		toolBarMgr.appendToGroup(IPTPDebugUIConstants.STEP_RETURN_GROUP, stepReturnAction);
		toolBarMgr.appendToGroup(IPTPDebugUIConstants.REG_GROUP, registerAction);
		toolBarMgr.appendToGroup(IPTPDebugUIConstants.REG_GROUP, unregisterAction);
		
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
		if (type == IIconCanvasActionListener.DOUBLE_CLICK_ACTION) {
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
		String variableText = ((UIDebugManager) manager).getValueText(((IPProcess)obj).getTaskId());
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
	//overwrite change job method
	protected void changeJob(final IPJob job) {
		super.changeJob(job);
		if (job != null) {
			((StepReturnAction)stepReturnAction).resetTask();
			BitList suspendedTaskList = (BitList) job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY);
			if (suspendedTaskList != null) {
				updateStepReturnButton(suspendedTaskList.copy());
			}
		}
	}
	//overwrite change set method
	public void selectSet(IElementSet set) {
		super.selectSet(set);
		if (set != null) {
			((StepReturnAction)stepReturnAction).resetTask();
			IPJob job = ((UIDebugManager) manager).findJobById(getCurrentID());
			BitList suspendedTaskList = (BitList) job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY);
			if (suspendedTaskList != null) {
				updateStepReturnButton(suspendedTaskList.copy());
			}
		}
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
				BitList suspendedTaskList = (BitList) job.getAttribute(IAbstractDebugger.SUSPENDED_PROC_KEY);
				BitList terminatedTaskList = (BitList) job.getAttribute(IAbstractDebugger.TERMINATED_PROC_KEY);
				updateSuspendResumeButton(suspendedTaskList, terminatedTaskList);
				updateTerminateButton(terminatedTaskList, suspendedTaskList);
			}
		} else
			setEnableResumeButtonGroup(false);
	}
	/** Update buttons to enable / disable
	 * @param source
	 * @param set
	 * @param target
	 */
	public void updateSuspendResumeButton(BitList source, BitList target) {
		IElementSet set = getCurrentSet();
		if (set == null || source == null)
			return;
		boolean isEnabled = false;
		if (set.isRootSet()) {
			isEnabled = !source.isEmpty();// tasks != 0: some processes suspended
			suspendAction.setEnabled(set.size() != source.cardinality()); // disable suspend Action if all tasks same as root size
		} else {
			try {
				BitList setTasks = ((UIDebugManager) manager).getTasks(getCurrentID(), set.getID());
				// this set contains some suspended processes
				isEnabled = setTasks.intersects(source);
				BitList refTasks = source.copy();
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
		if (!isEnabled)
			stepReturnAction.setEnabled(false);
	}
	public void updateStepReturnButton(BitList source) {
		IElementSet set = getCurrentSet();
		if (set == null || source == null) {
			return;
		}

		try {
			BitList setTasks = ((UIDebugManager) manager).getTasks(getCurrentID(), set.getID());
			if (setTasks != null) {
				setTasks.and(source);
				if (!setTasks.isEmpty()) {
					BitList[] t = ((UIDebugManager) manager).filterStepReturnTasks(setTasks);
					if (t.length == 2) {
						((StepReturnAction)stepReturnAction).addTask(t[0]);
						((StepReturnAction)stepReturnAction).delTask(t[1]);
					}
					((StepReturnAction)stepReturnAction).update();
				}
			}
		} catch (CoreException e) {
			PTPDebugUIPlugin.log(e);
		}
	}
	/** Updtae terminate button
	 * @param source
	 * @param set
	 * @param target
	 */
	public void updateTerminateButton(BitList source, BitList target) {
		IElementSet set = getCurrentSet();
		if (set == null || source == null)
			return;
		int setSize = set.size();
		int totalTerminatedSize = source.cardinality();
		int totalSuspendedSize = (target == null || target.isEmpty() ? 0 : target.cardinality());
		// size equals: all processes are terminated
		boolean isEnabled = (setSize != totalTerminatedSize);
		if (!set.isRootSet()) {
			try {
				BitList setTasks = ((UIDebugManager) manager).getTasks(getCurrentID(), set.getID());
				setSize = setTasks.cardinality();
				BitList refTasks = source.copy();
				refTasks.and(setTasks);
				// size equals: the set contains all terminated processes
				totalTerminatedSize = refTasks.cardinality();
				isEnabled = (setSize != totalTerminatedSize);
				if (isEnabled) {
					BitList tarRefTasks = target.copy();
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

    public void selectionChanged(SelectionChangedEvent event) {
    	super.selectionChanged(event);
    	ISelection selection = event.getSelection();
    	if (!selection.isEmpty() && selection instanceof StructuredSelection) {
    		StructuredSelection structSelection = (StructuredSelection)selection;
    		if (structSelection.size() == 1) {
	    		IElement element = (IElement)structSelection.getFirstElement();
	    		if (element.isRegistered()) {
	    			focusOnDebugTarget(getCheckedJob(), element.getIDNum());
	    		}
    		}
    	}
    }
    /*
	public void drawSpecial(Object obj, GC gc, int x_loc, int y_loc, int width, int height) {
		super.drawSpecial(obj, gc, x_loc, y_loc, width, height);
		if (cur_element_set != null && obj instanceof DebugElement) {
			DebugElement element =  (DebugElement)obj;
			switch(element.getType()) {
			case DebugElement.VALUE_DIFF:
				gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
				gc.drawLine(x_loc, y_loc, x_loc+width, y_loc+height);
				gc.drawLine(x_loc, y_loc+height, x_loc+width, y_loc);
				break;
			}
		}
	}
	*/
	
	/******************************************************
	 * the focus on debug target on debug view 
	 ******************************************************/
	public void focusOnDebugTarget(final IPJob job, final int task_id) {
		final UIDebugManager uimanager = ((UIDebugManager) manager);
		WorkbenchJob workjob = new WorkbenchJob("Focus on Debug View") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				focusOnDebugView(uimanager.getDebugObject(job, task_id));
				return Status.OK_STATUS;
			}
		};
		workjob.setPriority(Job.DECORATE);
		workjob.schedule();
	}
	private void focusOnDebugView(final Object selection) {
		if (selection == null) {
			return;
		}
		if (PTPDebugUIPlugin.getDisplay().getThread() == Thread.currentThread()) {
			doOnFocusDebugView(selection);
		} else {
			WorkbenchJob job = new WorkbenchJob("Focus on Debug View") {
				public IStatus runInUIThread(IProgressMonitor monitor) {
					doOnFocusDebugView(selection);
					return Status.OK_STATUS;
				}
			};
			job.setPriority(Job.INTERACTIVE);
			job.setSystem(true);
			job.schedule();
		}
	}
	private void expendDebugView(AsynchronousTreeViewer asynViewer, Object selection) {
		TreePath[] treePaths = asynViewer.getTreePaths(selection);
		if (treePaths.length == 0) {
			if (selection instanceof IStackFrame) {
				expendDebugView(asynViewer, ((IStackFrame)selection).getLaunch());
			}
			else if (selection instanceof IThread) {
				expendDebugView(asynViewer, ((IThread)selection).getLaunch());
			}
			else if (selection instanceof IDebugTarget) {
				expendDebugView(asynViewer, ((IDebugTarget)selection).getLaunch());
			}
		}
		if (treePaths.length > 0) {
			asynViewer.expand(new TreeSelection(treePaths[0]));
		}
	}
	private void doOnFocusDebugView(Object selection) {
		Viewer viewer = getDebugViewer();
		if (viewer instanceof AsynchronousTreeViewer) {
			AsynchronousTreeViewer asynViewer = (AsynchronousTreeViewer)viewer;
			expendDebugView(asynViewer, selection);
			focusOnDebugTarget(asynViewer, selection);
		}
	}
	private void focusOnDebugTarget(final AsynchronousTreeViewer asynViewer, final Object selection) {
		WorkbenchJob job = new WorkbenchJob("Focus on Debug Target") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				final TreePath[] treePaths = asynViewer.getTreePaths(selection);
				if (treePaths.length > 0) {
					asynViewer.setSelection(new TreeSelection(treePaths[0]), true, true);
   					//selectElements(new Object[] { selection });
				}
				return Status.OK_STATUS;
			}
        };
        job.setSystem(true);
        job.setPriority(Job.DECORATE);
        job.schedule();
	}
}
