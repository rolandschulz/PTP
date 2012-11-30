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
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.events.IChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.jobs.IJobListener;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.internal.ui.actions.RemoveAllTerminatedAction;
import org.eclipse.ptp.internal.ui.model.PProcessUI;
import org.eclipse.ptp.ui.IElementManager;
import org.eclipse.ptp.ui.IJobManager;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.utils.DebugUtil;
import org.eclipse.ptp.utils.core.BitSetIterable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @author Clement chu Additional changes Greg Watson
 * 
 */
public class ParallelJobsView extends AbstractParallelSetView implements ISelectionProvider {
	private final class JobChildListener implements IJobChildListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IChangedProcessEvent)
		 */
		public void handleEvent(IChangedProcessEvent e) {
			if (e.getSource() instanceof IPJob) {
				if (debug) {
					System.err.println("----------------- IJobChildListener - IChangedProcessEvent: " + this); //$NON-NLS-1$
				}
				if (!((IPJob) e.getSource()).isDebug()) {
					refresh(true);
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.INewProcessEvent)
		 */
		public void handleEvent(INewProcessEvent e) {
			if (e.getSource() instanceof IPJob) {
				final IPJob job = (IPJob) e.getSource();
				if (debug) {
					System.err.println("----------------- IJobChildListener - INewProcessEvent: " + this); //$NON-NLS-1$
				}
				final BitSet procRanks = e.getProcesses();
				for (Integer procRank : new BitSetIterable(procRanks)) {
					getJobManager().addProcess(job, procRank);
				}
				boolean isCurrent = e.getSource().getID().equals(getCurrentID());
				if (isCurrent) {
					updateJobSet();
					changeJobRefresh((IPJob) e.getSource());
					// should this just be refreshJobView()?
					// refresh will need to be batched in the future
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.elements.listeners.IJobChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IRemoveProcessEvent)
		 */
		public void handleEvent(IRemoveProcessEvent e) {
			if (e.getSource() instanceof IPJob) {
				final IPJob job = (IPJob) e.getSource();
				if (debug) {
					System.err.println("----------------- IJobChildListener - IRemoveProcessEvent: " + this); //$NON-NLS-1$
				}
				boolean isCurrent = e.getSource().getID().equals(getCurrentID());
				final BitSet procRanks = e.getProcesses();
				for (Integer procRank : new BitSetIterable(procRanks)) {
					getJobManager().removeProcess(job, procRank);
				}
				if (isCurrent) {
					updateJobSet();
					changeJobRefresh((IPJob) e.getSource());
					// should this just be refreshJobView()?
					// refresh will need to be batched in the future
				}
			}
		}
	}

	private final class JobListener implements IJobListener {
		public void jobAdded(IJobStatus status) {
			// nothing to do
		}

		public void jobChanged(IJobStatus status) {
			refreshJobView();
		}
	}

	private final class RMChildListener implements IResourceManagerChildListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events. IResourceManagerChangedMachineEvent)
		 */
		public void handleEvent(IChangedMachineEvent e) {
			// Don't need to do anything
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events. IResourceManagerChangedQueueEvent)
		 */
		public void handleEvent(IChangedQueueEvent e) {
			// Can safely ignore
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.INewJobEvent)
		 */
		public void handleEvent(INewJobEvent e) {
			if (debug) {
				System.err.println("----------------- QueueChildListener - INewJobEvent: " + this); //$NON-NLS-1$
			}
			IPJob lastJob = null;
			for (IPJob job : e.getJobs()) {
				getJobManager().createElementHandler(job);
				lastJob = job;
			}
			if (lastJob != null) {
				if (jobFocus) {
					changeJobRefresh(lastJob, true);
				} else {
					refreshJobView();
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events. IResourceManagerNewMachineEvent)
		 */
		public void handleEvent(INewMachineEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.INewQueueEvent)
		 */
		public void handleEvent(INewQueueEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.IRemoveJobEvent)
		 */
		public void handleEvent(IRemoveJobEvent e) {
			if (debug) {
				System.err.println("----------------- QueueChildListener - IRemoveJobEvent: " + this); //$NON-NLS-1$
			}
			for (IPJob job : e.getJobs()) {
				getJobManager().removeJob(job);
			}
			changeJobRefresh(null, true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events. IResourceManagerRemoveMachineEvent)
		 */
		public void handleEvent(IRemoveMachineEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events. IResourceManagerRemoveQueueEvent)
		 */
		public void handleEvent(IRemoveQueueEvent e) {
		}
	};

	class JobViewUpdateWorkbenchJob extends WorkbenchJob {
		private final ReentrantLock waitLock = new ReentrantLock();
		private final List<ISelection> refreshJobList = new ArrayList<ISelection>();

		public JobViewUpdateWorkbenchJob() {
			super(Messages.ParallelJobsView_0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime .IProgressMonitor)
		 */
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (size() == 0) {
				return Status.CANCEL_STATUS;
			}

			ISelection selection = getLastJobSelection();
			if (debug) {
				System.err.println("============= JobViewUpdateWorkbenchJob refresh: " + selection); //$NON-NLS-1$
			}
			if (!jobTableViewer.getTable().isDisposed()) {
				jobTableViewer.setSelection(selection, true);
				jobTableViewer.refresh(true);
			}

			// if last refresh object is true and previous refresh is false,
			// then refresh again
			ISelection lastSelection = getLastJobSelection();
			waitLock.lock();
			try {
				refreshJobList.clear();
				if (!selection.equals(lastSelection)) {
					refreshJobList.add(lastSelection);
					schedule();
				}
			} finally {
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
				if (force) {
					refreshJobList.clear();
				}
				if (!refreshJobList.contains(selection)) {
					refreshJobList.add(selection);
				}
			} finally {
				waitLock.unlock();
			}
			schedule();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.progress.WorkbenchJob#shouldSchedule()
		 */
		@Override
		public boolean shouldSchedule() {
			int size = size();
			if (debug) {
				System.err.println("============= JobViewUpdateWorkbenchJob: " + refreshJobList.size()); //$NON-NLS-1$
			}
			return (size == 1);
		}

		/**
		 * @return
		 */
		private ISelection getLastJobSelection() {
			waitLock.lock();
			try {
				return refreshJobList.get(refreshJobList.size() - 1);
			} finally {
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
			} finally {
				waitLock.unlock();
			}
		}
	}

	/*
	 * Job focus flag
	 */
	private boolean jobFocus = true;

	/*
	 * Model listeners
	 */
	private final IResourceManagerChildListener resourceManagerChildListener = new RMChildListener();
	private final IJobChildListener jobChildListener = new JobChildListener();
	private final IJobListener jobListener = new JobListener();

	/*
	 * Debug flag
	 */
	private final boolean debug = DebugUtil.JOBS_VIEW_TRACING;

	/*
	 * Element selection
	 */
	private ISelection selection = null;
	private final ListenerList listeners = new ListenerList();
	private final Action jobFocusAction = null;

	protected String cur_selected_element_id = IElementManager.EMPTY_ID;
	/*
	 * UI components
	 */
	protected Menu jobPopupMenu = null;
	protected SashForm sashForm = null;
	protected TableViewer jobTableViewer = null;
	protected Composite elementViewComposite = null;

	protected JobViewUpdateWorkbenchJob jobViewUpdateJob = new JobViewUpdateWorkbenchJob();
	/*
	 * Actions
	 */
	protected ParallelAction terminateAllAction = null;

	public ParallelJobsView() {
		this(PTPUIPlugin.getDefault().getJobManager());
	}

	public ParallelJobsView(IElementManager manager) {
		super(manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener
	 * (org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/**
	 * @param job
	 */
	public void changeJobRefresh(IPJob job) {
		changeJobRefresh(job, false);
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
		} else if (cur_job != null && job == null) {
			doChangeJob((IPJob) null);
			selection = new StructuredSelection();
		} else if (cur_job != null && job != null) {
			if (!cur_job.getID().equals(job.getID())) {
				doChangeJob(job);
			}
			selection = new StructuredSelection(job);
		} else { // cur_job == null && job == null
			selection = new StructuredSelection();
		}
		if (isVisible()) {
			jobViewUpdateJob.schedule(selection, force);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#dispose()
	 */
	@Override
	public void dispose() {
		JobManager.getInstance().removeListener(jobListener);
		for (IPResourceManager rm : ModelManager.getInstance().getUniverse().getResourceManagers()) {
			for (IPJob job : rm.getJobs()) {
				job.removeChildListener(jobChildListener);
			}
			rm.removeChildListener(resourceManagerChildListener);
		}
		elementViewComposite.dispose();
		super.dispose();
	}

	/**
	 * Change the currently selected job in the job viewer
	 * 
	 * @param job
	 */
	public void doChangeJob(final IPJob job) {
		syncExec(new Runnable() {
			public void run() {
				selectJob(job);
				update();
			}
		});
	}

	/**
	 * Change the currently selected job in the job viewer
	 * 
	 * @param job_id
	 */
	public void doChangeJob(String job_id) {
		doChangeJob(((IJobManager) manager).findJobById(job_id));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#doubleClick(org. eclipse.ptp.ui.model.IElement)
	 */
	@Override
	public void doubleClick(IElement element) {
		IPElement pelement = element.getPElement();
		// FIXME PProcessUI goes away when we address UI scalability. See Bug
		// 311057
		if (pelement instanceof PProcessUI) {
			openProcessViewer((PProcessUI) pelement);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getCurrentID()
	 */
	@Override
	public synchronized String getCurrentID() {
		IPJob job = getJobManager().getJob();
		if (job != null) {
			return job.getID();
		}
		return IElementManager.EMPTY_ID;
	}

	/**
	 * Get the queue of the currently selected job
	 * 
	 * @return queue
	 */
	public IPQueue getQueue() {
		return getJobManager().getQueue();
	}

	/**
	 * Get the queue ID of the currently selected job
	 * 
	 * @return queue ID
	 */
	public String getQueueID() {
		IPQueue queue = getQueue();
		if (queue != null) {
			return queue.getID();
		}
		return IElementManager.EMPTY_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.IContentProvider#getRulerIndex(java.lang.Object, int)
	 */
	@Override
	public String getRulerIndex(Object obj, int index) {
		if (obj instanceof IElement) {
			return ((IElement) obj).getName();
		}
		return super.getRulerIndex(obj, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		if (selection == null) {
			return StructuredSelection.EMPTY;
		}
		return selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getToolTipText(java .lang.Object)
	 */
	@Override
	public String[] getToolTipText(Object obj) {
		IElementHandler setManager = getCurrentElementHandler();
		if (obj == null || !(obj instanceof PProcessUI) || setManager == null || cur_element_set == null) {
			return IToolTipProvider.NO_TOOLTIP;
		}

		// FIXME PProcessUI goes away when we address UI scalability. See Bug
		// 311057
		PProcessUI proc = (PProcessUI) obj;
		StringBuffer buffer = new StringBuffer();
		int num = proc.getJobRank();
		buffer.append(Messages.ParallelJobsView_1 + num);
		buffer.append(Messages.ParallelJobsView_2);

		if (proc.getPid() == 0) {
			buffer.append(Messages.ParallelJobsView_3 + "N/A"); //$NON-NLS-1$
		} else {
			buffer.append(Messages.ParallelJobsView_3 + proc.getPid());
		}
		IElementSet[] sets = setManager.getSetsWithElement(proc.getID());
		if (sets.length > 1) {
			buffer.append(Messages.ParallelJobsView_4);
		}
		for (int i = 1; i < sets.length; i++) {
			buffer.append(sets[i].getID());
			if (i < sets.length - 1) {
				buffer.append(","); //$NON-NLS-1$
			}
		}
		// buffer.append("\nStatus: " +
		// getJobManager().getProcessStatusText(proc));
		return new String[] { buffer.toString() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener
	 * (org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#updateView(java. lang.Object)
	 */
	@Override
	public void repaint(boolean all) {
		if (all) {
			if (!jobTableViewer.getTable().isDisposed()) {
				jobTableViewer.refresh(true);
			}
		}
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged( org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		// Selection change could come from either the jobTableViewer of the
		// elementViewComposite
		selection = event.getSelection();
		setSelection(selection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#setFocus()
	 */
	@Override
	public void setFocus() {
		super.setFocus();
		IPJob job = getJobManager().getJob();
		if (job == null) {
			changeJobRefresh(null);
		}
	}

	/**
	 * Set flag that determines if new jobs are give focus in the jobs view.
	 * 
	 * @param focus
	 *            a value of true will cause new jobs to be displayed in the jobs view
	 */
	public void setJobFocus(boolean focus) {
		jobFocus = focus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse .jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		final SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
		Object[] array = listeners.getListeners();
		for (Object element : array) {
			final ISelectionChangedListener l = (ISelectionChangedListener) element;
			SafeRunnable.run(new SafeRunnable() {
				public void run() {
					l.selectionChanged(e);
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#updateAction()
	 */
	@Override
	public void updateAction() {
		super.updateAction();
		if (terminateAllAction != null) {
			ISelection selection = jobTableViewer.getSelection();
			if (selection.isEmpty()) {
				terminateAllAction.setEnabled(false);
			} else {
				IPJob job = (IPJob) ((IStructuredSelection) selection).getFirstElement();
				terminateAllAction.setEnabled(!(job.isDebug() || job.getState() == JobAttributes.State.COMPLETED));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#convertElementObject (org.eclipse.ptp.ui.model.IElement)
	 */
	@Override
	protected Object convertElementObject(IElement element) {
		if (element == null) {
			return null;
		}

		// FIXME PProcessUI goes away when we address UI scalability. See Bug
		// 311057
		if (element.getPElement() instanceof PProcessUI) {
			return element.getPElement();
		}
		return null;
	}

	/**
	 * Create Job context menu
	 */
	protected void createJobContextMenu() {
		MenuManager menuMgr = new MenuManager("#jobpopupmenu"); //$NON-NLS-1$
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#createToolBarActions (org.eclipse.jface.action.IToolBarManager)
	 */
	@Override
	protected void createToolBarActions(IToolBarManager toolBarMgr) {
		super.buildInToolBarActions(toolBarMgr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#createView(org.eclipse .swt.widgets.Composite)
	 */
	@Override
	protected void createView(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayout(new FillLayout(SWT.VERTICAL));
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		jobTableViewer = new TableViewer(sashForm, SWT.SINGLE | SWT.BORDER);
		jobTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		jobTableViewer.setLabelProvider(new WorkbenchLabelProvider());
		jobTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof IJobManager) {
					return ((IJobManager) inputElement).getJobs();
				}
				return new Object[0];
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		jobTableViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object j1, Object j2) {
				return ((IPJob) j1).getName().compareTo(((IPJob) j2).getName());
			}
		});
		jobTableViewer.setInput(manager);
		jobTableViewer.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				ISelection selection = jobTableViewer.getSelection();
				TableItem item = jobTableViewer.getTable().getItem(new Point(e.x, e.y));
				if (item == null && !selection.isEmpty()) {
					jobTableViewer.getTable().deselectAll();
					doChangeJob((IPJob) null);
				} else if (item != null) {
					IPJob job = (IPJob) item.getData();
					if (job == null) {
						doChangeJob((IPJob) null);
					} else if (selection.isEmpty()) {
						doChangeJob(job);
					} else {
						String cur_id = getCurrentID();
						if (cur_id == null || !cur_id.equals(job.getID())) {
							doChangeJob(job);
						}
					}
					update(); // Added to ensure that terminate button is
								// updated correctly
				}
			}
		});
		// ----------------------------------------------------------------------
		// Enable property sheet updates when tree items are selected.
		// Note for this to work each item in the tree must either implement
		// IPropertySource, or support IPropertySource.class as an adapter type
		// in its AdapterFactory.
		// ----------------------------------------------------------------------
		jobTableViewer.addSelectionChangedListener(this);
		getSite().setSelectionProvider(this);

		createJobContextMenu();
		elementViewComposite = createElementView(sashForm);

		jobTableViewer.getTable().setVisible(true);
		elementViewComposite.setVisible(true);
		sashForm.setWeights(new int[] { 1, 2 });

		/*
		 * Wait until the view has been created before registering for events
		 */
		/*
		 * Add us to any existing RM's. I guess it's possible we could miss a RM if a new event arrives while we're doing this,
		 * but is it a problem?
		 */
		for (IPResourceManager rm : ModelManager.getInstance().getUniverse().getResourceManagers()) {
			rm.addChildListener(resourceManagerChildListener);
		}
	}

	/**
	 * Create job context menu
	 * 
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
	protected IJobManager getJobManager() {
		return ((IJobManager) manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#initialElement()
	 */
	@Override
	protected void initialElement() {
		IPUniverse universe = ModelManager.getInstance().getUniverse();
		manager.initial(universe);
		changeJobRefresh(null, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#initialView()
	 */
	@Override
	protected void initialView() {
		initialElement();
	}

	/**
	 * Refresh the job view. Called to notify the job view that the model has changed and it needs to update the view.
	 */
	protected void refreshJobView() {
		syncExec(new Runnable() {
			public void run() {
				if (!jobTableViewer.getTable().isDisposed()) {
					jobTableViewer.refresh(true);
				}
			}
		});
	}

	/**
	 * Change job
	 * 
	 * @param job_id
	 *            Target job ID
	 */
	protected void selectJob(IPJob job) {
		IPJob old = getJobManager().getJob();
		if (old != null) {
			old.removeChildListener(jobChildListener);
		}
		if (job != null) {
			job.addChildListener(jobChildListener);
		}
		getJobManager().setJob(job);
		updateJobSet();
	}
}
