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
package org.eclipse.ptp.ui.views;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.events.IChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;
import org.eclipse.ptp.core.elements.listeners.IQueueChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.events.IChangedResourceManagerEvent;
import org.eclipse.ptp.core.events.INewResourceManagerEvent;
import org.eclipse.ptp.core.events.IRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerChildListener;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.internal.ui.actions.RemoveAllTerminatedAction;
import org.eclipse.ptp.internal.ui.actions.TerminateJobAction;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.managers.AbstractUIManager;
import org.eclipse.ptp.ui.managers.JobManager;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Clement chu
 * Additional changes Greg Watson
 * 
 */
public class ParallelJobsView extends AbstractParallelSetView implements 
	IModelManagerChildListener, IResourceManagerChildListener, 
	IQueueChildListener, IJobChildListener {
	// view flag
	public static final String BOTH_VIEW = "0";
	public static final String JOB_VIEW = "1";
	public static final String PRO_VIEW = "2";
	// selected element
	protected String cur_selected_element_id = IManager.EMPTY_ID;
	// composite
	protected Menu jobPopupMenu = null;
	protected SashForm sashForm = null;
	protected TableViewer jobTableViewer = null;
	protected Composite elementViewComposite = null;
	// action
	// protected ParallelAction changeJobViewAction = null;
	protected ParallelAction terminateAllAction = null;
	protected String current_view = BOTH_VIEW;

	public ParallelJobsView() {
		this(PTPUIPlugin.getDefault().getJobManager());
	}
	
	/** Constructor
	 * 
	 */
	public ParallelJobsView(IManager manager) {
		super(manager);
		
		IModelManager mm = PTPCorePlugin.getDefault().getModelManager();
		
		/*
		 * Add us to any existing RM's and queues. I guess it's possible we could
		 * miss a RM or queue if a new event arrives while we're doing this, but is 
		 * it a problem?
		 */
		for (IResourceManager rm : mm.getUniverse().getResourceManagers()) {
			for (IPQueue queue : rm.getQueues()) {
				queue.addChildListener(this);
			}
			rm.addChildListener(this);
		}
		mm.addListener(this);
	}
	
	/** Change job
	 * @param job_id Job ID
	 */
	public void changeJob(final String job_id) {
		IPJob job = ((JobManager)manager).findJobById(job_id);
		changeJobRefresh(job);
	}
	
	public void changeJobRefresh(final IPJob job) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				if (!elementViewComposite.isDisposed()) {
					changeJob(job);
					jobTableViewer.refresh(true);
					jobTableViewer.setSelection(job == null ? new StructuredSelection() : new StructuredSelection(job), true);
				}
			}
		});
	}
	
	/** Change view
	 * @param view_flag
	 */
	public void changeView(String view_flag) {
		current_view = view_flag;
		if (current_view.equals(ParallelJobsView.JOB_VIEW)) {
			jobTableViewer.getTable().setVisible(true);
			elementViewComposite.setVisible(false);
			sashForm.setWeights(new int[] { 1, 0 });
		} else if (current_view.equals(ParallelJobsView.PRO_VIEW)) {
			jobTableViewer.getTable().setVisible(false);
			elementViewComposite.setVisible(true);
			sashForm.setWeights(new int[] { 0, 1 });
		} else {
			jobTableViewer.getTable().setVisible(true);
			elementViewComposite.setVisible(true);
			sashForm.setWeights(new int[] { 1, 2 });
		}
	}
	
	public void dispose() {
		PTPCorePlugin.getDefault().getModelManager().removeListener(this);
		elementViewComposite.dispose();
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#doubleClick(org.eclipse.ptp.ui.model.IElement)
	 */
	public void doubleClick(IElement element) {
		openProcessViewer(getJobManager().findProcess(element.getID()));
	}
	
	/** Get selected job
	 * @return selected job
	 */
	public IPJob getCheckedJob() {
		String job_id = getCurrentID();
		if (!((JobManager)manager).isNoJob(job_id))
			return ((JobManager)manager).findJobById(job_id);
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getCurrentID()
	 */
	public synchronized String getCurrentID() {
		IPJob job = getJobManager().getJob();
		if (job != null) {
			return job.getID();
		}
		return IManager.EMPTY_ID;
	}
	
	/** Get current view flag
	 * @return flag of view
	 */
	public String getCurrentView() {
		return current_view;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getImage(int, int)
	 */
	public Image getImage(int index1, int index2) {
		return ParallelImages.procImages[index1][index2];
	}
	
	public IPQueue getQueue() {
		return getJobManager().getQueue();
	}
	
	public String getQueueID() {
		IPQueue queue = getQueue();
		if (queue != null) {
			return queue.getID();
		}
		return IManager.EMPTY_ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.IContentProvider#getRulerIndex(java.lang.Object, int)
	 */
	public String getRulerIndex(Object obj, int index) {
		if (obj instanceof IElement) {
			Object procObj = convertElementObject((IElement)obj);
			if (procObj instanceof IPProcess) {
				return ((IPProcess)procObj).getProcessIndex();
			}
		}
		return super.getRulerIndex(obj, index);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getToolTipText(java.lang.Object)
	 */
	public String[] getToolTipText(Object obj) {
		IElementHandler setManager = getCurrentElementHandler();
		if (obj == null || !(obj instanceof IPProcess) || setManager == null || cur_element_set == null)
			return IToolTipProvider.NO_TOOLTIP;

		IPProcess proc = (IPProcess)obj;
		StringBuffer buffer = new StringBuffer();
		String num = proc.getProcessIndex();
		if (num != null) {
			buffer.append("Index: " + num);
			buffer.append("\n");
		}
		buffer.append("PID: " + proc.getPid());
		IElementSet[] sets = setManager.getSetsWithElement(proc.getID());
		if (sets.length > 1)
			buffer.append("\nSet: ");
		for (int i = 1; i < sets.length; i++) {
			buffer.append(sets[i].getID());
			if (i < sets.length - 1)
				buffer.append(",");
		}
		// buffer.append("\nStatus: " + getJobManager().getProcessStatusText(proc));
		return new String[] { buffer.toString() };
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IJobProcessListener#handleEvent(org.eclipse.ptp.core.elements.events.IChangedProcessEvent)
	 */
	public void handleEvent(final IChangedProcessEvent e) {
		refresh(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IRemoveProcessEvent)
	 */
	public void handleEvent(final IRemoveProcessEvent e) {
		if (e.getSource() instanceof IPJob) {
			final boolean isCurrent = e.getSource().equals(getCurrentID());
			
			for (IPProcess process : e.getProcesses()) {
				getJobManager().removeProcess(process);
			}
			
			if (isCurrent) {
				UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
					public void run() {
						updateJobSet();
						repaint(true);
					}
				});
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent(org.eclipse.ptp.core.events.IChangedResourceManagerEvent)
	 */
	public void handleEvent(IChangedResourceManagerEvent e) {
		// Don't need to do anything
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent(org.eclipse.ptp.core.events.INewResourceManagerEvent)
	 */
	public void handleEvent(INewResourceManagerEvent e) {
		e.getResourceManager().addChildListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent(org.eclipse.ptp.core.events.IRemoveResourceManagerEvent)
	 */
	public void handleEvent(IRemoveResourceManagerEvent e) {
		e.getResourceManager().removeChildListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IQueueJobListener#handleEvent(org.eclipse.ptp.core.elements.events.IQueueNewJobEvent)
	 */
	public void handleEvent(final INewJobEvent e) {
		for (IPJob job : e.getJobs()) {
			changeJobRefresh(job);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.INewMachineEvent)
	 */
	public void handleEvent(INewMachineEvent e) {
		// Don't need to do anything
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IJobProcessListener#handleEvent(org.eclipse.ptp.core.elements.events.IJobNewProcessEvent)
	 */
	public void handleEvent(final INewProcessEvent e) {
		final boolean isCurrent = e.getSource().getID().equals(getCurrentID());
		
		for (IPProcess process : e.getProcesses()) {
			getJobManager().addProcess(process);
		}
		
		// FIXME: make JobManager thread safe so we can get rid
		// of this safeRunAsyncInUIThread stuff!
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				if (isCurrent) {
					updateJobSet();
					repaint(true);
				}
			}
		});	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerQueueListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerNewQueueEvent)
	 */
	public void handleEvent(INewQueueEvent e) {
		for (IPQueue queue : e.getQueues()) {
			queue.addChildListener(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IQueueJobListener#handleEvent(org.eclipse.ptp.core.elements.events.IQueueChangedJobEvent)
	 */
	public void handleEvent(final IChangedJobEvent e) {
		refresh(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IQueueJobListener#handleEvent(org.eclipse.ptp.core.elements.events.IQueueRemoveJobEvent)
	 */
	public void handleEvent(final IRemoveJobEvent e) {
		for (IPJob job : e.getJobs()) {
			getJobManager().removeJob(job);
		}
		
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				changeJobRefresh(null);
			}
		});	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedMachineEvent)
	 */
	public void handleEvent(IChangedMachineEvent e) {
		// Don't need to do anything
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerQueueListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedQueueEvent)
	 */
	public void handleEvent(IChangedQueueEvent e) {
		// Don't need to do anything
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerRemoveMachineEvent)
	 */
	public void handleEvent(IRemoveMachineEvent e) {
		// Don't need to do anything
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerQueueListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerRemoveQueueEvent)
	 */
	public void handleEvent(IRemoveQueueEvent e) {
		for (IPQueue queue : e.getQueues()) {
			queue.removeChildListener(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#updateView(java.lang.Object)
	 */
	public void repaint(boolean all) {
		if (all) {
			if (!jobTableViewer.getTable().isDisposed()) {
				jobTableViewer.refresh(true);
			}
		}
		update();
	}
	
	public void setFocus() {
		super.setFocus();
		IPJob job = getCheckedJob();
		if (job == null) {
			changeJob((String)null);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#updateAction()
	 */
	public void updateAction() {
		super.updateAction();
		if (terminateAllAction != null) {
			ISelection selection = jobTableViewer.getSelection();
			if (selection.isEmpty()) {
				terminateAllAction.setEnabled(false);
			} else {
				IPJob job = (IPJob) ((IStructuredSelection) selection).getFirstElement();
				terminateAllAction.setEnabled(!(job.isDebug() || job.isTerminated()));
			}
		}
	}
	
	/** Update Job
	 * 
	 */
	public void updateJobSet() {
		IElementHandler setManager = getCurrentElementHandler();
		selectSet(setManager == null ? null : setManager.getSetRoot());
	}
	
	private JobManager getJobManager() {
		return ((JobManager) manager);
	}
	
	/** Change job
	 * @param job
	 */
	protected void changeJob(final IPJob job) {
		selectJob(job);
		//updateAction();
		update();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#convertElementObject(org.eclipse.ptp.ui.model.IElement)
	 */
	protected Object convertElementObject(IElement element) {
		if (element == null)
			return null;
		
		return getJobManager().findProcess(element.getID());
	}
	
	/** Create Job context menu
	 * 
	 */
	protected void createJobContextMenu() {
		MenuManager menuMgr = new MenuManager("#jobpopupmenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(IPTPUIConstants.IUIACTIONGROUP));
				manager.add(new Separator(IPTPUIConstants.IUIEMPTYGROUP));
				fillJobContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(jobTableViewer.getTable());
		jobTableViewer.getTable().setMenu(menu);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#createToolBarActions(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void createToolBarActions(IToolBarManager toolBarMgr) {
		terminateAllAction = new TerminateJobAction(this);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, terminateAllAction);
		super.buildInToolBarActions(toolBarMgr);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#createView(org.eclipse.swt.widgets.Composite)
	 */
	protected void createView(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayout(new FillLayout(SWT.VERTICAL));
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		jobTableViewer = new TableViewer(sashForm, SWT.SINGLE | SWT.BORDER);
		jobTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		jobTableViewer.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				if (element instanceof IPJob) {
					IPJob job = (IPJob) element;
					return ParallelImages.jobImages[job.getState().ordinal()][job.isDebug() ? 1 : 0];
				}
				return null;
			}
			public String getText(Object element) {
				if (element instanceof IPJob) {
					return ((IPJob) element).getName();
				}
				return "";
			}
		});
		jobTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {}
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof AbstractUIManager)
					return ((JobManager) inputElement).getJobs();
				return new Object[0];
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		jobTableViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object j1, Object j2) {
				return ((IPJob)j1).getName().compareTo(((IPJob)j2).getName());
			}
		});
		jobTableViewer.setInput(manager);
		jobTableViewer.getTable().addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				ISelection selection = jobTableViewer.getSelection();
				TableItem item = jobTableViewer.getTable().getItem(new Point(e.x, e.y));
				if (item == null && !selection.isEmpty()) {
					jobTableViewer.getTable().deselectAll();
					changeJob((IPJob) null);
				}
				else if (item != null) {
					IPJob job = (IPJob)item.getData();
					if (job == null) {
						changeJob((IPJob) null);
					}
					else if (selection.isEmpty()) {
						changeJob(job);
					}
					else {
						String cur_id = getCurrentID();
						if (cur_id == null || !cur_id.equals(job.getID())) {
							changeJob(job);
						}
					}
				}
			}
		});
		// ----------------------------------------------------------------------
		// Enable property sheet updates when tree items are selected.
		// Note for this to work each item in the tree must either implement
		// IPropertySource, or support IPropertySource.class as an adapter type
		// in its AdapterFactory.
		// ----------------------------------------------------------------------
		getSite().setSelectionProvider(jobTableViewer);
		
		createJobContextMenu();
		elementViewComposite = createElementView(sashForm);
		changeView(current_view);
	}
	
	/** Create job context menu
	 * @param menuManager
	 */
	protected void fillJobContextMenu(IMenuManager menuManager) {
		ParallelAction removeAllTerminatedAction = new RemoveAllTerminatedAction(this);
		removeAllTerminatedAction.setEnabled(getJobManager().hasStoppedJob());
		menuManager.add(removeAllTerminatedAction);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#initialElement()
	 */
	protected void initialElement() {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		changeJobRefresh((IPJob) manager.initial(universe));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#initialView()
	 */
	protected void initialView() {
		initialElement();
		//update();
		refresh(true);
	}

	/** Change job
	 * @param job_id Target job ID
	 */
	protected void selectJob(IPJob job) {
		IPJob old = getJobManager().getJob();
		if (old != null) {
			old.removeChildListener(this);
		}
		getJobManager().setJob(job);
		if (job != null) {
			job.addChildListener(this);			
		}
		updateJobSet();
	}
}
