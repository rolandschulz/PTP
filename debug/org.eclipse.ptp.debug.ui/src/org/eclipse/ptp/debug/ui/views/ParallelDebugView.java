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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.viewers.model.InternalTreeModelViewer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.model.IPDebugElement;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.internal.ui.actions.RegisterAction;
import org.eclipse.ptp.debug.internal.ui.actions.ResumeAction;
import org.eclipse.ptp.debug.internal.ui.actions.StepIntoAction;
import org.eclipse.ptp.debug.internal.ui.actions.StepOverAction;
import org.eclipse.ptp.debug.internal.ui.actions.StepReturnAction;
import org.eclipse.ptp.debug.internal.ui.actions.SuspendAction;
import org.eclipse.ptp.debug.internal.ui.actions.TerminateAction;
import org.eclipse.ptp.debug.internal.ui.actions.UnregisterAction;
import org.eclipse.ptp.debug.internal.ui.views.AbstractPDebugViewEventHandler;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.UIDebugManager;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.views.IIconCanvasActionListener;
import org.eclipse.ptp.ui.views.ParallelJobsView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @author clement chu
 * 
 */
public class ParallelDebugView extends ParallelJobsView {
	// actions
	protected ParallelAction resumeAction = null;
	protected ParallelAction suspendAction = null;
	protected ParallelAction terminateAction = null;
	protected ParallelAction stepIntoAction = null;
	protected ParallelAction stepOverAction = null;
	protected ParallelAction stepReturnAction = null;
	protected ParallelAction registerAction = null;
	protected ParallelAction unregisterAction = null;
	protected AbstractPDebugViewEventHandler fEventHandler = null;
	protected Viewer launchViewer = null;
	
	protected ISelectionChangedListener debugViewSelectChangedListener = new ISelectionChangedListener() {
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			if (!selection.isEmpty()) {
				if (selection instanceof IStructuredSelection) {
					Object element = ((IStructuredSelection)selection).getFirstElement();
					if (element instanceof IPDebugElement) {
						if (canvas != null && !canvas.isDisposed()) {
							if (getCurrentSet() != null) {
								int index = getCurrentSet().findIndexByName(String.valueOf(((IPDebugElement)element).getID()));
								if (index > -1) {
									canvas.unselectAllElements();
									canvas.selectElement(index);
									canvas.setCurrentSelection(false);
									refresh(false);
								}
							}
						}
					}
				}
			}
		}
	};
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
		Viewer viewer = getDebugViewer();
		if (viewer != null)
			viewer.removeSelectionChangedListener(debugViewSelectChangedListener);
		if (getEventHandler() != null)
			getEventHandler().dispose();
		super.dispose();
	}
	
	/**
	 * @return
	 */
	protected Viewer getDebugViewer() {
		if (launchViewer == null) {
			IWorkbenchPage page = getViewSite().getPage();
			if (page == null)
				return null;
			IViewPart part = page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
			if (part == null) {
				if (page == null)
					return null;
				try {
					part = page.showView(IDebugUIConstants.ID_DEBUG_VIEW);
				} catch (PartInitException e) {
					return null;
				}
			}
			if (part != null && part instanceof AbstractDebugView) {
				launchViewer = ((AbstractDebugView)part).getViewer();
				if (launchViewer != null)
					launchViewer.addSelectionChangedListener(debugViewSelectChangedListener);
			}			
		}
		return launchViewer;
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
	protected void setEventHandler(AbstractPDebugViewEventHandler eventHandler) {
		this.fEventHandler = eventHandler;
	}
	
	/**
	 * Returns the event handler for this view
	 * 
	 * @return The event handler for this view
	 */
	protected AbstractPDebugViewEventHandler getEventHandler() {
		return this.fEventHandler;
	}	
		
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.ParallelJobView#createView(org.eclipse.swt.widgets.Composite)
	 */
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
		if (obj == null)
			return new String[] {"", ""};
		String[] header = super.getToolTipText(obj);
		String variableText = ((UIDebugManager) manager).getValueText(new Integer(((IPProcess)obj).getProcessIndex()).intValue(), this);
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
	// Update button
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#updateAction()
	 */
	public void updateAction() {
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
				updateDebugButtons(job);
			}
		} else {
			resumeAction.setEnabled(false);
			stepIntoAction.setEnabled(false);
			stepOverAction.setEnabled(false);
			stepReturnAction.setEnabled(false);
			suspendAction.setEnabled(false);
		}
	}
	/**
	 * Update debug button
	 */
	public void updateDebugButtons(IPJob job) {
		IPSession session = ((UIDebugManager) manager).getDebugSession(job);
		if (session == null)
			return;
		IElementSet set = getCurrentSet();
		if (set == null)
			return;

		BitList terminatedTasks = session.getPDISession().getTaskManager().getTerminatedTasks();
		BitList suspendedTasks = session.getPDISession().getTaskManager().getSuspendedTasks();
		BitList stepReturnTasks = session.getPDISession().getTaskManager().getCanStepReturnTasks();
		if (terminatedTasks == null || suspendedTasks == null || stepReturnTasks == null)
			return;

		int setSize = set.size();
		int totalTerminatedSize = 0;
		int totalSuspendedSize = 0;
		int totalStepReturnSize = 0;
		if (set.isRootSet()) {
			totalTerminatedSize = terminatedTasks.cardinality();
			totalSuspendedSize = suspendedTasks.isEmpty() ? 0 : suspendedTasks.cardinality();
			totalStepReturnSize = stepReturnTasks.isEmpty() ? 0 : stepReturnTasks.cardinality();
		}
		else {
			try {
				BitList setTasks = ((UIDebugManager) manager).getTasks(set.getID());
				if (setTasks == null)
					return;
				setSize = setTasks.cardinality();
				BitList setTerminatedTasks = session.getPDISession().getTaskManager().getTerminatedTasks(setTasks.copy());
				totalTerminatedSize = setTerminatedTasks.cardinality();
				// size equals: the set contains all terminated processes
				if (setSize != totalTerminatedSize) {
					BitList setSuspendedTasks = session.getPDISession().getTaskManager().getSuspendedTasks(setTasks.copy());
					totalSuspendedSize = setSuspendedTasks.cardinality();
					BitList setCanStepReturnTasks = session.getPDISession().getTaskManager().getCanStepReturnTasks(setTasks.copy());
					totalStepReturnSize = setCanStepReturnTasks.cardinality();
				}
			} catch (CoreException e) {
				PTPDebugUIPlugin.log(e);
			}
		}
//System.err.println("Set size: " + setSize + ", T: " + totalTerminatedSize + ", S: "+ totalSuspendedSize + ", Return: " + totalStepReturnSize);
		boolean enabledTerminatedButton = (setSize != totalTerminatedSize);
		terminateAction.setEnabled(enabledTerminatedButton);
		if (enabledTerminatedButton) {// not all processes terminated
			resumeAction.setEnabled(totalSuspendedSize > 0);
			boolean enableStepButtons = (setSize == totalSuspendedSize + totalTerminatedSize);
			stepIntoAction.setEnabled(enableStepButtons);
			stepOverAction.setEnabled(enableStepButtons);
			suspendAction.setEnabled(!enableStepButtons);
			stepReturnAction.setEnabled(enableStepButtons && totalStepReturnSize > 0);
		} else {// all process terminated
			resumeAction.setEnabled(false);
			stepIntoAction.setEnabled(false);
			stepOverAction.setEnabled(false);
			stepReturnAction.setEnabled(false);
			suspendAction.setEnabled(false);
		}
	}
    /* (non-Javadoc)
     * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
    	super.selectionChanged(event);
    	ISelection selection = event.getSelection();
    	if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
    		IStructuredSelection structSelection = (IStructuredSelection)selection;
    		if (structSelection.size() == 1 && structSelection.getFirstElement() instanceof IElement) {
	    		IElement element = (IElement)structSelection.getFirstElement();
	    		if (element.isRegistered()) {
					try {
		    			focusOnDebugTarget(getJobManager().getJob(), Integer.parseInt(element.getName()));
					} catch (NumberFormatException e) {
						// The element name had better be the process number
					}
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
	 * focus on debug target on debug view 
	 ******************************************************/
	private IDebugElement getDebugElement(IPJob job, int task_id) {
		IPSession session = ((UIDebugManager)manager).getDebugSession(job);
		if (session != null) {
			return session.getLaunch().getDebugTarget(task_id);
			/*
			IPDebugTarget debugTarget = session.getLaunch().getDebugTarget(task_id);
			if (debugTarget != null) {
				try {
					IThread[] threads = debugTarget.getThreads();
					for (int i=0; i<threads.length; i++) {
						IStackFrame frame = threads[i].getTopStackFrame();
						if (frame != null)
							return frame;
					}
					if (threads.length > 0) {
						return threads[0];
					}
				}
				catch (DebugException e) {
					return debugTarget;
				}
				return debugTarget;
			}
			*/
		}
		return null;
	}
	public void focusOnDebugTarget(final IPJob job, final int task_id) {
		WorkbenchJob wjob = new WorkbenchJob("Focusing on debug element...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				Viewer viewer = getDebugViewer();
				if (viewer != null) {
					ISelection selection = viewer.getSelection();
					//ISelection selection = getViewSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
					if (selection instanceof IStructuredSelection) {
						Object element = ((IStructuredSelection)selection).getFirstElement();
						if (element instanceof IPDebugElement) {
							if (((IPDebugElement)element).getID() != task_id) {
								//FIXME: only work if all elements are collapse
								//((TreeViewer)viewer).collapseAll();

								IDebugElement focusElement = getDebugElement(job, task_id); 
								if (focusElement == null) {
									//do nothing if cannot find debug target
									return Status.CANCEL_STATUS;
								}
								selectOnViewer(viewer, (IPDebugElement)focusElement);
								// set focus element to selected element
								element = focusElement;
							}
						}
						else {
							IDebugElement focusElement = getDebugElement(job, task_id); 
							if (focusElement == null) {
								//do nothing if cannot find debug target
								return Status.CANCEL_STATUS;
							}
							selectOnViewer(viewer, (IDebugElement)focusElement);
							// set focus element to selected element
							element = focusElement;
						}
						
						if (element instanceof IStackFrame) {
							//do nothing if selected element is stack frame
							return Status.CANCEL_STATUS;
						}
						if (element instanceof IThread) {
							//expand thread this thread
							expandOnViewer(viewer, (IThread)element);
							return Status.OK_STATUS;
						}
						if (element instanceof IDebugTarget) {
							//expand debug target this thread
							expandOnViewer(viewer, (IDebugTarget)element);
							return Status.OK_STATUS;
						}
						//if (!((TreeViewer)viewer).getExpandedState(element)) {
							//((TreeViewer)viewer).setExpandedState(element, true);
						//}
					}
					//viewer.setSelection(new StructuredSelection(focusElement), true);
					//viewer.setSelection(new StructuredSelection(focusElement.getDebugTarget()), true);
					//((TreeViewer)viewer).expandAll();
				}
				return Status.CANCEL_STATUS;
			}
        };
        //set job priority very low to make sure it is executed at last
        wjob.setPriority(Job.DECORATE);
        wjob.schedule(500);
	}
	private void expandOnViewer(final Viewer viewer, final IDebugElement element) {
		WorkbenchJob wjob = new WorkbenchJob("Expanding on viewer...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				((TreeViewer)viewer).setExpandedState(element, true);
				try {
					if (element instanceof IThread) {
						selectOnViewer(viewer, ((IThread)element).getTopStackFrame());
						return Status.OK_STATUS;
					}
					if (element instanceof IDebugTarget) {
						IThread[] threads = ((IPDebugTarget)element).getThreads();
						if (threads.length > 0) {
							expandOnViewer(viewer, threads[0]);
							return Status.OK_STATUS;
						}
						return Status.CANCEL_STATUS;
					}
				}
				catch (DebugException e) {
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
        };
        //set job priority very low to make sure it is executed at last
        wjob.setPriority(Job.DECORATE);
        wjob.schedule();
	}
	private void selectOnViewer(final Viewer viewer, final IDebugElement element) {
		if (element != null) {
			WorkbenchJob wjob = new WorkbenchJob("Selecting debug element...") {
				public IStatus runInUIThread(IProgressMonitor monitor) {
					/* FIXME 
					 * In Debug View, there is a Selection Policy.  If current selected element is stack frame and its status is suspended, then it cannot allow to change selection to others.
					 * Now I used internal class to avoid policy checking
					 */
					if (viewer instanceof InternalTreeModelViewer) {
						((InternalTreeModelViewer) viewer).setSelection(new StructuredSelection(element), true, true);
					} 
					else {
						viewer.setSelection(new StructuredSelection(element), true);
					}
					return Status.OK_STATUS;
				}
	        };
	        //set job priority very low to make sure it is executed at last
	        wjob.setPriority(Job.DECORATE);
	        wjob.schedule();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.ParallelJobsView#changeJobRefresh(org.eclipse.ptp.core.elements.IPJob, boolean)
	 */
	public void changeJobRefresh(IPJob job, boolean force) {
		if (job != null && job.isTerminated()) {
			IPSession session = ((UIDebugManager) manager).getDebugSession(job);
			if (session != null) {
				BitList tasks = session.getTasks();
				if (!session.getPDISession().getTaskManager().isAllTerminated(tasks)) {
					session.forceStoppedDebugger(ProcessAttributes.State.ERROR);
				}
			}
		}
		super.changeJobRefresh(job, force);
	}
}
