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
package org.eclipse.ptp.internal.ui.views;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchManager;
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
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.internal.ui.DebugUtil;
import org.eclipse.ptp.internal.ui.IElementManager;
import org.eclipse.ptp.internal.ui.IJobManager;
import org.eclipse.ptp.internal.ui.IPTPUIConstants;
import org.eclipse.ptp.internal.ui.PTPUIPlugin;
import org.eclipse.ptp.internal.ui.actions.ParallelAction;
import org.eclipse.ptp.internal.ui.actions.RemoveAllTerminatedAction;
import org.eclipse.ptp.internal.ui.listeners.IJobChangedListener;
import org.eclipse.ptp.internal.ui.messages.Messages;
import org.eclipse.ptp.internal.ui.model.IElementHandler;
import org.eclipse.ptp.internal.ui.model.IElementSet;
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

	private class JobViewUpdateWorkbenchJob extends WorkbenchJob {
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

	private class JobListener implements IJobChangedListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.internal.ui.listeners.IJobChangedListener#jobChangedEvent(int, java.lang.String, java.lang.String)
		 */
		@Override
		public void jobChangedEvent(int type, String cur_job_id, String pre_job_id) {
			if (cur_job_id == null) {
				changeJobRefresh(null);
			}
		}
	}

	/*
	 * Debug flag
	 */
	private final boolean debug = DebugUtil.JOBS_VIEW_TRACING;

	/*
	 * Element selection
	 */
	private ISelection selection = null;
	private final ListenerList listeners = new ListenerList();
	protected String cur_selected_element_id = IElementManager.EMPTY_ID;

	/*
	 * UI components
	 */
	protected Menu jobPopupMenu = null;
	protected SashForm sashForm = null;
	protected TableViewer jobTableViewer = null;
	protected Composite elementViewComposite = null;

	protected JobViewUpdateWorkbenchJob jobViewUpdateJob = new JobViewUpdateWorkbenchJob();

	private final IJobChangedListener fJobListener = new JobListener();

	/*
	 * Actions
	 */
	protected ParallelAction terminateAllAction = null;

	public ParallelJobsView() {
		this(PTPUIPlugin.getDefault().getJobManager());
	}

	public ParallelJobsView(IJobManager manager) {
		super(manager);
		manager.addJobChangedListener(fJobListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener
	 * (org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/**
	 * @param job
	 * @since 7.0
	 */
	public void changeJobRefresh(IJobStatus job) {
		changeJobRefresh(job, false);
	}

	/**
	 * @param job
	 * @param force
	 * @since 7.0
	 */
	public void changeJobRefresh(IJobStatus job, boolean force) {
		IJobStatus cur_job = getJobManager().getJob();
		ISelection selection = null;
		if (cur_job == null && job != null) {
			doChangeJob(job);
			selection = new StructuredSelection(job);
		} else if (cur_job != null && job == null) {
			doChangeJob((IJobStatus) null);
			selection = new StructuredSelection();
		} else if (cur_job != null && job != null) {
			if (!cur_job.getJobId().equals(job.getJobId())) {
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
		getJobManager().removeJobChangedListener(fJobListener);
		elementViewComposite.dispose();
		super.dispose();
	}

	/**
	 * Change the currently selected job in the job viewer
	 * 
	 * @param job
	 * @since 7.0
	 */
	public void doChangeJob(final IJobStatus job) {
		syncExec(new Runnable() {
			@Override
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
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#doubleClick(int)
	 */
	@Override
	public void doubleClick(int element) {
		// Nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getCurrentID()
	 */
	@Override
	public synchronized String getCurrentID() {
		IJobStatus job = getJobManager().getJob();
		if (job != null) {
			return job.getJobId();
		}
		return IElementManager.EMPTY_ID;
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
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getToolTipText(int)
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public String[] getToolTipText(int element) {
		IElementHandler setManager = getCurrentElementHandler();
		if (setManager == null || cur_element_set == null || !cur_element_set.contains(element)) {
			return IToolTipProvider.NO_TOOLTIP;
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append(Messages.ParallelJobsView_1 + element);
		buffer.append(Messages.ParallelJobsView_2);

		IElementSet[] sets = setManager.getSetsContaining(element);
		if (sets.length > 1) {
			buffer.append(Messages.ParallelJobsView_4);
		}
		for (int i = 1; i < sets.length; i++) {
			buffer.append(sets[i].getID());
			if (i < sets.length - 1) {
				buffer.append(","); //$NON-NLS-1$
			}
		}
		return new String[] { buffer.toString() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener
	 * (org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
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
		refresh(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged( org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
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
		if (getJobManager().getJob() == null) {
			changeJobRefresh(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse .jface.viewers.ISelection)
	 */
	@Override
	public void setSelection(ISelection selection) {
		final SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
		Object[] array = listeners.getListeners();
		for (Object element : array) {
			final ISelectionChangedListener l = (ISelectionChangedListener) element;
			SafeRunnable.run(new SafeRunnable() {
				@Override
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
				IJobStatus job = (IJobStatus) ((IStructuredSelection) selection).getFirstElement();
				terminateAllAction.setEnabled(!(job.getLaunchMode().equals(ILaunchManager.DEBUG_MODE) || job.getState().equals(
						IJobStatus.COMPLETED)));
			}
		}
	}

	/**
	 * Update Job
	 */
	public void updateJobSet() {
		IElementHandler setManager = getCurrentElementHandler();
		selectSet(setManager == null ? null : setManager.getSet(IElementHandler.SET_ROOT_ID));
	}

	/**
	 * Create Job context menu
	 */
	protected void createJobContextMenu() {
		MenuManager menuMgr = new MenuManager("#jobpopupmenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
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
			@Override
			public void dispose() {
				// Nothing
			}

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof IJobManager) {
					return ((IJobManager) inputElement).getJobs();
				}
				return new Object[0];
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// Nothing
			}
		});
		jobTableViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object j1, Object j2) {
				return ((IJobStatus) j1).getJobId().compareTo(((IJobStatus) j2).getJobId());
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
					doChangeJob((IJobStatus) null);
				} else if (item != null) {
					IJobStatus job = (IJobStatus) item.getData();
					if (job == null) {
						doChangeJob((IJobStatus) null);
					} else if (selection.isEmpty()) {
						doChangeJob(job);
					} else {
						String cur_id = getCurrentID();
						if (cur_id == null || !cur_id.equals(job.getJobId())) {
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
			@Override
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
	 * @since 7.0
	 */
	protected void selectJob(IJobStatus job) {
		getJobManager().setJob(job);
		updateJobSet();
	}
}
