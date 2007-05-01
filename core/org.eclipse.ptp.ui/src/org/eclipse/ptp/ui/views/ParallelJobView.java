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
import org.eclipse.ptp.core.IProcessListener;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.events.IModelEvent;
import org.eclipse.ptp.core.events.IModelRuntimeNotifierEvent;
import org.eclipse.ptp.core.events.IProcessEvent;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.internal.ui.actions.ChangeQueueAction;
import org.eclipse.ptp.internal.ui.actions.RemoveAllTerminatedAction;
import org.eclipse.ptp.internal.ui.actions.TerminateAllAction;
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
 * 
 */
public class ParallelJobView extends AbstractParallelSetView implements IProcessListener {
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
	// view flag
	public static final String BOTH_VIEW = "0";
	public static final String JOB_VIEW = "1";
	public static final String PRO_VIEW = "2";
	protected String current_view = BOTH_VIEW;
	private ChangeQueueAction changeQueueAction = null;

	/** Constructor
	 * 
	 */
	public ParallelJobView(IManager manager) {
		super(manager);
		PTPCorePlugin.getDefault().getModelPresentation().addProcessListener(this);
	}
	public ParallelJobView() {
		this(PTPUIPlugin.getDefault().getJobManager());
	}
	public void dispose() {
		elementViewComposite.dispose();
		PTPCorePlugin.getDefault().getModelPresentation().removeProcessListener(this);		
		super.dispose();
	}
	/** Get current view flag
	 * @return flag of view
	 */
	public String getCurrentView() {
		return current_view;
	}
	/** Change view
	 * @param view_flag
	 */
	public void changeView(String view_flag) {
		current_view = view_flag;
		if (current_view.equals(ParallelJobView.JOB_VIEW)) {
			jobTableViewer.getTable().setVisible(true);
			elementViewComposite.setVisible(false);
			sashForm.setWeights(new int[] { 1, 0 });
		} else if (current_view.equals(ParallelJobView.PRO_VIEW)) {
			jobTableViewer.getTable().setVisible(false);
			elementViewComposite.setVisible(true);
			sashForm.setWeights(new int[] { 0, 1 });
		} else {
			jobTableViewer.getTable().setVisible(true);
			elementViewComposite.setVisible(true);
			sashForm.setWeights(new int[] { 1, 2 });
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#initialElement()
	 */
	protected void initialElement() {
		changeJobRefresh((IPJob) manager.initial());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#initialView()
	 */
	protected void initialView() {
		initialElement();
		update();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getImage(int, int)
	 */
	public Image getImage(int index1, int index2) {
		return ParallelImages.procImages[index1][index2];
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
			public String getText(Object element) {
				if (element instanceof IPJob) {
					return ((IPJob) element).getName();
				}
				return "";
			}
			public Image getImage(Object element) {
				if (element instanceof IPJob) {
					IPJob job = (IPJob) element;
					if (job.isTerminated())
						return ParallelImages.jobImages[2];
					if (job.isDebug())
						return ParallelImages.jobImages[1];
					return ParallelImages.jobImages[0];
				}
				return null;
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
		createJobContextMenu();
		elementViewComposite = createElementView(sashForm);
		changeView(current_view);
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
	/** Create job context menu
	 * @param menuManager
	 */
	protected void fillJobContextMenu(IMenuManager menuManager) {
		ParallelAction removeAllTerminatedAction = new RemoveAllTerminatedAction(this);
		removeAllTerminatedAction.setEnabled(getJobManager().hasStoppedJob());
		menuManager.add(removeAllTerminatedAction);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#createToolBarActions(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void createToolBarActions(IToolBarManager toolBarMgr) {
		terminateAllAction = new TerminateAllAction(this);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, terminateAllAction);
		changeQueueAction = new ChangeQueueAction(this);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUINAVIGATORGROUP, changeQueueAction);
		super.buildInToolBarActions(toolBarMgr);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#doubleClick(org.eclipse.ptp.ui.model.IElement)
	 */
	public void doubleClick(IElement element) {
		openProcessViewer(getJobManager().findProcess(element.getID()));
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#convertElementObject(org.eclipse.ptp.ui.model.IElement)
	 */
	protected Object convertElementObject(IElement element) {
		if (element == null)
			return null;
		
		return getJobManager().findProcess(element.getID());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.IContentProvider#getRulerIndex(java.lang.Object, int)
	 */
	public String getRulerIndex(Object obj, int index) {
		if (obj instanceof IElement) {
			Object procObj = convertElementObject((IElement)obj);
			if (procObj instanceof IPProcess) {
				return ((IPProcess)procObj).getProcessNumber();
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
		String num = proc.getProcessNumber();
		if (num != null) {
			buffer.append("Number: " + num);
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
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getCurrentID()
	 */
	public String getCurrentID() {
		IPJob job = getJobManager().getJob();
		if (job != null) {
			return job.getID();
		}
		return IManager.EMPTY_ID;
	}
	/** Change job
	 * @param job_id Target job ID
	 */
	protected void selectJob(IPJob job) {
		getJobManager().setJob(job);
		updateJob();
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
	/** Change job
	 * @param job_id Job ID
	 */
	public void changeJob(final String job_id) {
		if (!elementViewComposite.isDisposed()) {
			getDisplay().syncExec(new Runnable() {
				public void run() {
					IPJob job = ((JobManager)manager).findJobById(job_id);
					changeJob(job);
					jobTableViewer.refresh(true);
					jobTableViewer.setSelection(job == null ? new StructuredSelection() : new StructuredSelection(job), true);
				}
			});
		}
	}
	/** Change job
	 * @param job
	 */
	protected void changeJob(final IPJob job) {
		selectJob(job);
		updateAction();
		update();
	}
	
	public void changeJobRefresh(final IPJob job) {
		if (!elementViewComposite.isDisposed()) {
			getDisplay().syncExec(new Runnable() {
				public void run() {
					changeJob(job);
					jobTableViewer.refresh(true);
					jobTableViewer.setSelection(job == null ? new StructuredSelection() : new StructuredSelection(job), true);
				}
			});
		}
	}
	/** Update Job
	 * 
	 */
	public void updateJob() {
		IElementHandler setManager = getCurrentElementHandler();
		selectSet(setManager == null ? null : setManager.getSetRoot());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#updateAction()
	 */
	public void updateAction() {
		super.updateAction();
		if (changeQueueAction != null) {
			changeQueueAction.setEnabled(((AbstractUIManager) manager).getResourceManagers().length > 0);
		}
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
	 * @see org.eclipse.ptp.core.IProcessListener#processEvent(org.eclipse.ptp.core.events.IProcessEvent)
	 */
	public void processEvent(final IProcessEvent event) {
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				safeProcessEvent(event);
			}
		});	
	}
	public void safeProcessEvent(final IProcessEvent event) {
		// only redraw if the current set contain the process
		IPProcess process = event.getProcess();
		getJobManager().addProcess(process);
		if (getJobManager().isCurrentSetContainProcess(getCurrentID(), process.getID())) {
			if (event.getType() != IProcessEvent.ADD_OUTPUT_TYPE)
				refresh(false);
		}
	}
	
	private JobManager getJobManager() {
		return ((JobManager) manager);
	}
	
	public void setFocus() {
		super.setFocus();
		IPJob job = getCheckedJob();
		if (job == null) {
			changeJob((String)null);
		}
	}
	
	public void selectQueue(IPQueue queue) {
		JobManager jobManager = getJobManager();
		jobManager.setQueue(queue);
		updateJob();
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
	
	public void modelEvent(final IModelEvent event) {
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				safeModelEvent(event);
			}
		});	
	}
	private void safeModelEvent(final IModelEvent event) {
		if (event instanceof IModelRuntimeNotifierEvent) {
			IModelRuntimeNotifierEvent runtimeEvent = (IModelRuntimeNotifierEvent)event;
			if (runtimeEvent.getType() == IModelRuntimeNotifierEvent.TYPE_JOB) {
				switch (runtimeEvent.getStatus()) {
				case IModelRuntimeNotifierEvent.RUNNING:
					build();
					break;
				case IModelRuntimeNotifierEvent.STARTED:
					build();
					break;
				case IModelRuntimeNotifierEvent.STOPPED:
				case IModelRuntimeNotifierEvent.ABORTED:
					break;
				}
			}
		}
		super.modelEvent(event);
	}
}
