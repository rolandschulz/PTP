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

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.internal.ui.JobManager;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.internal.ui.actions.TerminateAllAction;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Clement chu
 * 
 */
public class ParallelJobView extends AbstractParallelSetView {
	private static ParallelJobView instance = null;
	// selected element
	protected String cur_selected_element_id = IManager.EMPTY_ID;
	// composite
	protected SashForm sashForm = null;
	protected CheckboxTableViewer jobTableViewer = null;
	protected Composite elementViewComposite = null;
	// action
	// protected ParallelAction changeJobViewAction = null;
	protected ParallelAction terminateAllAction = null;
	// view flag
	public static final String BOTH_VIEW = "0";
	public static final String JOB_VIEW = "1";
	public static final String PRO_VIEW = "2";
	protected String current_view = BOTH_VIEW;
	public static Image[] jobImages = { ParallelImages.getImage(ParallelImages.ICON_RUNMODE_NORMAL), ParallelImages.getImage(ParallelImages.ICON_DEBUGMODE_NORMAL), ParallelImages.getImage(ParallelImages.ICON_TERMINATE_ALL_NORMAL) };
	public static Image[][] procImages = { { ParallelImages.getImage(ParallelImages.IMG_PROC_ERROR), ParallelImages.getImage(ParallelImages.IMG_PROC_ERROR_SEL) }, { ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED), ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED_SEL) },
			{ ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED_SIGNAL), ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED_SIGNAL_SEL) }, { ParallelImages.getImage(ParallelImages.IMG_PROC_RUNNING), ParallelImages.getImage(ParallelImages.IMG_PROC_RUNNING_SEL) },
			{ ParallelImages.getImage(ParallelImages.IMG_PROC_STARTING), ParallelImages.getImage(ParallelImages.IMG_PROC_STARTING_SEL) }, { ParallelImages.getImage(ParallelImages.IMG_PROC_STOPPED), ParallelImages.getImage(ParallelImages.IMG_PROC_STOPPED_SEL) } };

	public ParallelJobView() {
		instance = this;
		manager = PTPUIPlugin.getDefault().getJobManager();
	}
	public String getCurrentView() {
		return current_view;
	}
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
	protected void initElementAttribute() {
		e_offset_x = 5;
		e_spacing_x = 4;
		e_offset_y = 5;
		e_spacing_y = 4;
		e_width = 16;
		e_height = 16;
	}
	protected void initialElement() {
		manager.initial();
	}
	protected void initialView() {
		initialElement();
	}
	public static ParallelJobView getJobViewInstance() {
		if (instance == null)
			instance = new ParallelJobView();
		return instance;
	}
	protected void createView(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayout(new FillLayout(SWT.VERTICAL));
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		jobTableViewer = CheckboxTableViewer.newCheckList(sashForm, SWT.SINGLE | SWT.BORDER);
		jobTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		jobTableViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof IPJob) {
					
					return ((IPJob) element).getElementName();
				}
				return "";
			}
			public Image getImage(Object element) {
				if (element instanceof IPJob) {
					IPJob job = (IPJob) element;
					if (job.isAllStop())
						return jobImages[2];
					if (job.isDebug())
						return jobImages[1];
					return jobImages[0];
				}
				return null;
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
		jobTableViewer.setInput(manager);
		jobTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				jobTableViewer.setAllChecked(false);
				if (event.getChecked()) {
					jobTableViewer.setChecked(event.getElement(), true);
					changeJob((IPJob) event.getElement());
				}
				else {
					changeJob((IPJob)null);
				}
			}
		});
		jobTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				jobTableViewer.getTable().deselectAll();
				ISelection selection = event.getSelection();
				if (selection.isEmpty()) {
					changeJob((IPJob)null);
				}
				else {
					if (selection instanceof IStructuredSelection) {
						Object selectedElement = ((IStructuredSelection)selection).getFirstElement();						
						if (selectedElement instanceof IPJob) {							
							IPJob job = (IPJob)selectedElement;							
							boolean isSelected = jobTableViewer.getChecked(job);
							jobTableViewer.setAllChecked(false);
							if (!isSelected) {
								jobTableViewer.setChecked(job, true);
								changeJob(job);
							}
							else {
								changeJob((IPJob)null);								
							}
						}
					}
				}
			}
		});
		elementViewComposite = createElementView(sashForm);
		changeView(current_view);
	}
	protected void createToolBarActions(IToolBarManager toolBarMgr) {
		terminateAllAction = new TerminateAllAction(this);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, terminateAllAction);
		super.buildInToolBarActions(toolBarMgr);
	}
	protected void setActionEnable() {}
	protected void doubleClickAction(int element_num) {
		if (cur_element_set != null) {
			IElement element = cur_element_set.get(element_num);
			if (element != null) {
				openProcessViewer(((JobManager) manager).findProcess(getCurrentID(), element.getID()));
			}
		}
	}
	protected String getToolTipText(int element_num) {
		IElementHandler setManager = getCurrentElementHandler();
		if (setManager == null || cur_element_set == null)
			return "Unknown element";
		IElement element = cur_element_set.get(element_num);
		if (element == null)
			return "Unknown element";
		IPProcess proc = ((JobManager) manager).findProcess(getCurrentID(), element.getID());
		if (proc == null)
			return "Unknown process";
		StringBuffer buffer = new StringBuffer();
		buffer.append("Task ID: " + proc.getTaskId());
		buffer.append("\n");
		buffer.append("Process ID: " + proc.getPid());
		IElementSet[] groups = setManager.getSetsWithElement(element.getID());
		if (groups.length > 1)
			buffer.append("\nGroup: ");
		for (int i = 1; i < groups.length; i++) {
			buffer.append(groups[i].getID());
			if (i < groups.length - 1)
				buffer.append(",");
		}
		// buffer.append("\nStatus: " + getJobManager().getProcessStatusText(proc));
		return buffer.toString();
	}
	protected Image getStatusIcon(IElement element) {
		int status = ((JobManager) manager).getProcessStatus(getCurrentID(), element.getID());
		return procImages[status][element.isSelected() ? 1 : 0];
	}
	public String getCurrentID() {
		return ((JobManager) manager).getCurrentJobId();
	}
	protected void selectJob(String job_id) {
		((JobManager) manager).setCurrentJobId(job_id);
		updateJob();
	}
	public IPJob getCheckedJob() {
		String job_id = getCurrentID();
		if (!((JobManager) manager).isNoJob(job_id))
			return ((JobManager) manager).findJobById(job_id);

		return null;
	}
	public void changeJob(final String job_id) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				IPJob job = ((JobManager) manager).findJobById(job_id);
				jobTableViewer.setAllChecked(false);
				if (job != null)
					jobTableViewer.setChecked(job, true);

				changeJob(job);
			}
		});
	}
	protected void changeJob(final IPJob job) {
		selectJob((job==null?IManager.EMPTY_ID:job.getIDString()));
		update();
		refresh();
	}
	public void updateJob() {
		IElementHandler setManager = getCurrentElementHandler();
		selectSet(setManager==null?null:setManager.getSetRoot());
	}
	protected void updateAction() {
		super.updateAction();
		if (terminateAllAction != null) {
			Object[] checkedElements = jobTableViewer.getCheckedElements();
			if (checkedElements.length == 0) {
				terminateAllAction.setEnabled(false);
			}
			else {
				IPJob job = (IPJob)checkedElements[0];
				terminateAllAction.setEnabled(!(job.isDebug() || job.isAllStop()));
			}
		}
	}	
	public void repaint(Object condition) {
		if (condition != null) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!jobTableViewer.getTable().isDisposed()) {
						jobTableViewer.refresh(true);
						update();
					}
				}
			});
		}
		refresh();
	}
	public void run(final String arg) {
		System.out.println("------------ job run: " + arg);
		initialView();
		getDisplay().syncExec(new Runnable() {
			public void run() {
				jobTableViewer.refresh(true);
				IPJob job = ((JobManager) manager).findJob(arg);
				jobTableViewer.setSelection(job==null?new StructuredSelection():new StructuredSelection(job));
			}
		});
		refresh();
	}
	public void start() {
		System.out.println("------------ job start");
		refresh();
	}
	public void stopped() {
		System.out.println("------------ job stop");
		refresh();
	}
	public void exit() {
		System.out.println("------------ job exit");
		refresh();
	}
	public void abort() {
		System.out.println("------------ job abort");
		refresh();
	}
	public void monitoringSystemChangeEvent(Object object) {
		System.out.println("------------ job monitoringSystemChangeEvent");
		refresh();
	}
	public void execStatusChangeEvent(Object object) {
		System.out.println("------------ job execStatusChangeEvent");
		refresh();
	}
	public void sysStatusChangeEvent(Object object) {
		System.out.println("------------ job sysStatusChangeEvent");
		refresh();
	}
	public void processOutputEvent(Object object) {
		System.out.println("------------ job processOutputEvent");
		refresh();
	}
	public void errorEvent(Object object) {
		System.out.println("------------ job errorEvent");
		refresh();
	}
	public void updatedStatusEvent() {
		System.out.println("------------ job updatedStatusEvent");
		refresh();
	}
}
