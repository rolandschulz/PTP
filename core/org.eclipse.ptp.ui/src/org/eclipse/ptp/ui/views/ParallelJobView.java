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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.internal.ui.JobManager;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.internal.ui.actions.RemoveAllTerminatedAction;
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
public class ParallelJobView extends AbstractParallelSetView {
	private static ParallelJobView instance = null;
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
	protected void initialElement() {
		changeJob(manager.initial());
	}
	protected void initialView() {
		initialElement();
	}
	public Image getImage(int index1, int index2) {
		return ParallelImages.procImages[index1][index2];
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
		jobTableViewer = new TableViewer(sashForm, SWT.SINGLE | SWT.BORDER);
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
				if (inputElement instanceof JobManager)
					return ((JobManager) inputElement).getJobs();
				return new Object[0];
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		jobTableViewer.setInput(manager);
		jobTableViewer.getTable().addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Point point = new Point(e.x, e.y);
				TableItem item = jobTableViewer.getTable().getItem(point);
				if (item == null) {
					jobTableViewer.getTable().deselectAll();
					changeJob((IPJob) null);
				} else {
					IPJob job = manager.findJob(item.getText());
					changeJob(job);
				}
			}
		});
		createJobContextMenu();
		elementViewComposite = createElementView(sashForm);
		changeView(current_view);
	}
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
	protected void fillJobContextMenu(IMenuManager menuManager) {
		ParallelAction removeAllTerminatedAction = new RemoveAllTerminatedAction(this);
		removeAllTerminatedAction.setEnabled(manager.hasStoppedJob());
		menuManager.add(removeAllTerminatedAction);
	}
	protected void createToolBarActions(IToolBarManager toolBarMgr) {
		terminateAllAction = new TerminateAllAction(this);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, terminateAllAction);
		super.buildInToolBarActions(toolBarMgr);
	}
	protected void setActionEnable() {}
	public void doubleClick(IElement element) {
		openProcessViewer(((JobManager) manager).findProcess(getCurrentID(), element.getID()));
	}
	protected Object convertElementObject(IElement element) {
		if (element == null)
			return null;
		
		return ((JobManager) manager).findProcess(getCurrentID(), element.getID());
	}
	public String getRulerIndex(Object obj, int index) {
		if (obj instanceof IElement) {
			return ((IElement)obj).getName();
		}
		return super.getRulerIndex(obj, index);
	}
	public String getToolTipText(Object obj) {
		IElementHandler setManager = getCurrentElementHandler();
		if (obj == null || !(obj instanceof IPProcess) || setManager == null || cur_element_set == null)
			return " Unknown element";

		IPProcess proc = (IPProcess)obj;
		StringBuffer buffer = new StringBuffer();
		buffer.append(" Task ID: " + proc.getTaskId());
		buffer.append("\n");
		buffer.append(" Process ID: " + proc.getPid());
		IElementSet[] sets = setManager.getSetsWithElement(proc.getIDString());
		if (sets.length > 1)
			buffer.append("\n Set: ");
		for (int i = 1; i < sets.length; i++) {
			buffer.append(sets[i].getID());
			if (i < sets.length - 1)
				buffer.append(",");
		}
		// buffer.append("\nStatus: " + getJobManager().getProcessStatusText(proc));
		return buffer.toString();
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
		if (!manager.isNoJob(job_id))
			return manager.findJobById(job_id);
		return null;
	}
	public void changeJob(final String job_id) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				jobTableViewer.refresh(true);
				IPJob job = manager.findJobById(job_id);
				changeJob(job);
				jobTableViewer.setSelection(job == null ? new StructuredSelection() : new StructuredSelection(job));
			}
		});
	}
	protected void changeJob(final IPJob job) {
		String cur_id = getCurrentID();
		if (cur_id != null && job != null && cur_id.equals(job.getIDString()))
			return;
		selectJob((job == null ? IManager.EMPTY_ID : job.getIDString()));
		update();
		refresh();
	}
	public void updateJob() {
		IElementHandler setManager = getCurrentElementHandler();
		selectSet(setManager == null ? null : setManager.getSetRoot());
	}
	protected void updateAction() {
		super.updateAction();
		if (terminateAllAction != null) {
			ISelection selection = jobTableViewer.getSelection();
			if (selection.isEmpty()) {
				terminateAllAction.setEnabled(false);
			} else {
				IPJob job = (IPJob) ((IStructuredSelection) selection).getFirstElement();
				terminateAllAction.setEnabled(!(job.isDebug() || job.isAllStop()));
			}
		}
	}
	public void updateView(Object condition) {
		if (condition != null) {
			if (!jobTableViewer.getTable().isDisposed()) {
				jobTableViewer.refresh(true);
				update();
			}
		}
	}
	public void run(final String arg) {
		System.out.println("------------ job run: " + arg);
		initialView();
		/*
		getDisplay().syncExec(new Runnable() {
			public void run() {
				jobTableViewer.refresh(true);
				IPJob job = manager.findJob(arg);
				jobTableViewer.setSelection(job == null ? new StructuredSelection() : new StructuredSelection(job));
				changeJob(job);
			}
		});
		*/
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
		manager.clear();
		initialView();
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
