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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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
import org.eclipse.ptp.core.elements.events.IChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.IJobChangeEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;
import org.eclipse.ptp.core.elements.listeners.IJobListener;
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
import org.eclipse.ptp.ui.actions.ParallelAction;
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
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @author Clement chu
 * Additional changes Greg Watson
 * 
 */
public class ParallelJobsView extends AbstractParallelSetView {
	private final class JobChildListener implements IJobChildListener {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IChangedProcessEvent)
		 */
		public void handleEvent(IChangedProcessEvent e) {
			if (e.getSource() instanceof IPJob) {
				if (!((IPJob)e.getSource()).isDebug())
					refresh(true);
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent(org.eclipse.ptp.core.elements.events.INewProcessEvent)
		 */
		public void handleEvent(INewProcessEvent e) {
			if (e.getSource() instanceof IPJob) {
				if (debug) {
					System.err.println("----------------- IJobChildListener - INewProcessEvent: " + this);
				}
				for (IPProcess proc : e.getProcesses()) {
					getJobManager().addProcess(proc);
				}
				boolean isCurrent = e.getSource().getID().equals(getCurrentID());
				if (isCurrent) {
					updateJobSet();
					changeJobRefresh((IPJob)e.getSource());
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IRemoveProcessEvent)
		 */
		public void handleEvent(IRemoveProcessEvent e) {
			if (e.getSource() instanceof IPJob) {
				boolean isCurrent = e.getSource().getID().equals(getCurrentID());
				for (IPProcess proc : e.getProcesses()) {
					getJobManager().removeProcess(proc);
				}
				if (isCurrent) {
					updateJobSet();
					changeJobRefresh((IPJob)e.getSource());
				}
			}
		}
	}
	private final class JobListener implements IJobListener {
		public void handleEvent(IJobChangeEvent e) {
			if (debug) {
				System.err.println("----------------- JobListener - IJobChangeEvent: " + this);
			}
			changeJobRefresh(e.getSource(), true);			
		}
	}
	private final class MMChildListener implements IModelManagerChildListener {
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
			/*
			 * Add resource manager child listener so we get notified when new
			 * machines are added to the model.
			 */
			final IResourceManager rm = e.getResourceManager();
	        rm.addChildListener(resourceManagerListener);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent(org.eclipse.ptp.core.events.IRemoveResourceManagerEvent)
		 */
		public void handleEvent(IRemoveResourceManagerEvent e) {
			/*
			 * Removed resource manager child listener when resource manager is removed.
			 */
			e.getResourceManager().removeChildListener(resourceManagerListener);
		}		
	}
	private final class QueueChildListener implements IQueueChildListener {
		public void handleEvent(IChangedJobEvent e) {
			//refresh(true);
		}
		public void handleEvent(INewJobEvent e) {
			if (debug) {
				System.err.println("----------------- QueueChildListener - INewJobEvent: " + this);
			}
			for (IPJob job : e.getJobs()) {
				getJobManager().createElementHandler(job);
				changeJobRefresh(job, true);
			}
		}
		public void handleEvent(IRemoveJobEvent e) {
			if (debug) {
				System.err.println("----------------- QueueChildListener - IRemoveJobEvent: " + this);
			}
			for (IPJob job : e.getJobs()) {
				getJobManager().removeJob(job);
			}
			changeJobRefresh(null, true);
		}
	}
	private final class RMChildListener implements IResourceManagerChildListener {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedMachineEvent)
		 */
		public void handleEvent(IChangedMachineEvent e) {
			// Don't need to do anything
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedQueueEvent)
		 */
		public void handleEvent(IChangedQueueEvent e) {
			// Can safely ignore
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerNewMachineEvent)
		 */
		public void handleEvent(INewMachineEvent e) {
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.INewQueueEvent)
		 */
		public void handleEvent(INewQueueEvent e) {
			for (IPQueue queue : e.getQueues()) {
				queue.addChildListener(queueChildListener);
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerRemoveMachineEvent)
		 */
		public void handleEvent(IRemoveMachineEvent e) {
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerRemoveQueueEvent)
		 */
		public void handleEvent(IRemoveQueueEvent e) {
			for (IPQueue queue : e.getQueues()) {
				queue.removeChildListener(queueChildListener);
			}
		}
	}
	
	class JobViewUpdateWorkbenchJob extends WorkbenchJob {
		private final ReentrantLock	waitLock = new ReentrantLock();
		private List<ISelection> refreshJobList = new ArrayList<ISelection>();
		
		public JobViewUpdateWorkbenchJob() {
			super("Refreshing job view...");
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (size() == 0)
				return Status.CANCEL_STATUS;
			
			ISelection selection = getLastJobSelection();
			if (debug) {
				System.err.println("============= JobViewUpdateWorkbenchJob refresh: " + selection);
			}
			if (!jobTableViewer.getTable().isDisposed()) {
				jobTableViewer.setSelection(selection, true);
				jobTableViewer.refresh(true);
			}
			
			//if last refresh object is true and previous refresh is false, then refresh again 
			ISelection lastSelection = getLastJobSelection();
			waitLock.lock();
			try {
				refreshJobList.clear();
				if (!selection.equals(lastSelection)) {
					refreshJobList.add(lastSelection);
					schedule();
				}
			}
			finally {
				waitLock.unlock();
			}
			return Status.OK_STATUS;
		}
		
		/**
		 * @param selection
		 * @param force
		 */
		public void schedule(ISelection selection, boolean force) {
			waitLock.lock();
			try {
				if (force)
					refreshJobList.clear();
				if (!refreshJobList.contains(selection))
					refreshJobList.add(selection);
			}
			finally {
				waitLock.unlock();
			}
			schedule();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.progress.WorkbenchJob#shouldSchedule()
		 */
		public boolean shouldSchedule() {
			int size = size();
			if (debug) {
				System.err.println("============= JobViewUpdateWorkbenchJob: " + refreshJobList.size());
			}
			return (size == 1);
		}
		
		/**
		 * @return
		 */
		private ISelection getLastJobSelection() {
			waitLock.lock();
			try {
				return refreshJobList.get(refreshJobList.size()-1);
			}
			finally {
				waitLock.unlock();
			}
		}
		
		/**
		 * @return
		 */
		private int size() {
			waitLock.lock();
			try {
				return refreshJobList.size();
			}
			finally {
				waitLock.unlock();
			}
		}
	}
	// view flag
	public static final String BOTH_VIEW = "0";
	public static final String JOB_VIEW = "1";
	public static final String PRO_VIEW = "2";
	private final IModelManagerChildListener modelManagerListener = new MMChildListener();
	private final IResourceManagerChildListener resourceManagerListener = new RMChildListener();
	private final IJobChildListener jobChildListener = new JobChildListener();
	private final IQueueChildListener queueChildListener = new QueueChildListener();
	private final IJobListener jobListener = new JobListener();
	private boolean debug = false;
	
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

	protected JobViewUpdateWorkbenchJob jobViewUpdateJob = new JobViewUpdateWorkbenchJob();
	
	public ParallelJobsView() {
		this(PTPUIPlugin.getDefault().getJobManager());
	}
	
	/** Constructor
	 * 
	 */
	public ParallelJobsView(IManager manager) {
		super(manager);
	}
	
	/**
	 * @param job
	 */
	public void changeJobRefresh(IPJob job) {
		changeJobRefresh(job , false);
	}
	
	/**
	 * @param job
	 * @param force
	 */
	public void changeJobRefresh(IPJob job, boolean force) {
		IPJob cur_job = getJobManager().getJob();
		ISelection selection = null;
		if (cur_job == null && job != null) {
			doChangeJob(job);
			selection = new StructuredSelection(job);
		}
		else if (cur_job != null && job == null) {
			doChangeJob((IPJob)null);
			selection = new StructuredSelection();
		}
		else if (cur_job != null && job != null) {
			if (!cur_job.getID().equals(job.getID())) {
				doChangeJob(job);
			}
			selection = new StructuredSelection(job);
		}
		else { //cur_job == null && job == null
			selection = new StructuredSelection();
		}
		if (isVisible()) {
			jobViewUpdateJob.schedule(selection, force);
		}
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
		IModelManager mm = PTPCorePlugin.getDefault().getModelManager();
		synchronized (mm) {
		    for (IResourceManager rm : mm.getUniverse().getResourceManagers()) {
				for (IPQueue queue : rm.getQueues()) {
					for (IPJob job : queue.getJobs()) {
						job.removeChildListener(jobChildListener);
						job.removeElementListener(jobListener);
					}
					queue.removeChildListener(queueChildListener);
				}
		    	rm.removeChildListener(resourceManagerListener);
		    }
		    mm.removeListener(modelManagerListener);
		}
		elementViewComposite.dispose();
		super.dispose();
	}
	
	/**
	 * @param job
	 */
	public void doChangeJob(final IPJob job) {
		asyncExec(new Runnable() {
			public void run() {
				selectJob(job);
				update();
			}
		});
	}
	
	/**
	 * @param job_id
	 */
	public void doChangeJob(String job_id) {
		doChangeJob(((JobManager)manager).findJobById(job_id));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#doubleClick(org.eclipse.ptp.ui.model.IElement)
	 */
	public void doubleClick(IElement element) {
		openProcessViewer(getJobManager().findProcess(element.getID()));
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
	
	/**
	 * @return
	 */
	public IPQueue getQueue() {
		return getJobManager().getQueue();
	}
	
	/**
	 * @return
	 */
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
			return ((IElement)obj).getName();
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#setFocus()
	 */
	public void setFocus() {
		super.setFocus();
		IPJob job = getJobManager().getJob();
		if (job == null) {
			changeJobRefresh(null);
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
	
	/** 
	 * Update Job
	 */
	public void updateJobSet() {
		IElementHandler setManager = getCurrentElementHandler();
		selectSet(setManager == null ? null : setManager.getSetRoot());
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
				if (inputElement instanceof JobManager)
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
					doChangeJob((IPJob) null);
				}
				else if (item != null) {
					IPJob job = (IPJob)item.getData();
					if (job == null) {
						doChangeJob((IPJob) null);
					}
					else if (selection.isEmpty()) {
						doChangeJob(job);
					}
					else {
						String cur_id = getCurrentID();
						if (cur_id == null || !cur_id.equals(job.getID())) {
							doChangeJob(job);
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

		/*
		 * Wait until the view has been created before registering for events
		 */
		IModelManager mm = PTPCorePlugin.getDefault().getModelManager();
		synchronized (mm) {
		    /*
		     * Add us to any existing RM's. I guess it's possible we could
		     * miss a RM if a new event arrives while we're doing this, but is 
		     * it a problem?
		     */
		    for (IResourceManager rm : mm.getUniverse().getResourceManagers()) {
		        rm.addChildListener(resourceManagerListener);
		    }
		    mm.addListener(modelManagerListener);
		}
	}
	
	/** Create job context menu
	 * @param menuManager
	 */
	protected void fillJobContextMenu(IMenuManager menuManager) {
		ParallelAction removeAllTerminatedAction = new RemoveAllTerminatedAction(this);
		removeAllTerminatedAction.setEnabled(getJobManager().hasStoppedJob());
		menuManager.add(removeAllTerminatedAction);
	}
	
	/**
	 * @return
	 */
	protected JobManager getJobManager() {
		return ((JobManager) manager);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#initialElement()
	 */
	protected void initialElement() {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		/*
		 * Add us as a child listener to any existing queue
		 */
		for (IResourceManager rm : universe.getResourceManagers()) {
			for (IPQueue queue : rm.getQueues()) {
				queue.addChildListener(queueChildListener);
			}
		}
		
		manager.initial(universe);
		changeJobRefresh(null, true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#initialView()
	 */
	protected void initialView() {
		initialElement();
		//update();
	}
	
	/** Change job
	 * @param job_id Target job ID
	 */
	protected void selectJob(IPJob job) {
		IPJob old = getJobManager().getJob();
		if (old != null) {
			old.removeChildListener(jobChildListener);
			old.removeElementListener(jobListener);
		}
		getJobManager().setJob(job);
		if (job != null) {
			job.addChildListener(jobChildListener);
			job.addElementListener(jobListener);
		}
		updateJobSet();
	}
}
