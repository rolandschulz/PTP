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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.ui.JobManager;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.ParallelImages;
import org.eclipse.ptp.ui.actions.ChangeJobViewAction;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.ISetManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Clement chu
 *
 */
public class ParallelJobView extends AbstractParallelSetView {
	public static final String VIEW_ID = "org.eclipse.ptp.ui.views.parallelJobView";

	private static ParallelJobView instance = null;
	protected JobManager jobManager = null;
	
	//job
	protected String cur_job_id = "0";
	
	//selected element
	protected String cur_selected_element_id = "";
	
	//composite
	protected SashForm sashForm = null;
	protected Table jobTable = null;
	protected Composite elementViewComposite = null;
	
	//action
	protected ParallelAction changeJobViewAction = null;
	
	//view flag
	public static final String BOTH_VIEW = "0";
	public static final String JOB_VIEW = "1";
	public static final String PRO_VIEW = "2";
	protected String current_view = BOTH_VIEW;
	
	public static Image[][] jobImages = {
		{
			ParallelImages.getImage(ParallelImages.IMG_PROC_ERROR),
			ParallelImages.getImage(ParallelImages.IMG_PROC_ERROR_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED),
			ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED_SEL) }
	};
	public static Image[][] procImages = {
		{
			ParallelImages.getImage(ParallelImages.IMG_PROC_ERROR),
			ParallelImages.getImage(ParallelImages.IMG_PROC_ERROR_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED),
			ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED_SIGNAL),
			ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED_SIGNAL_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_PROC_RUNNING),
			ParallelImages.getImage(ParallelImages.IMG_PROC_RUNNING_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_PROC_STARTING),
			ParallelImages.getImage(ParallelImages.IMG_PROC_STARTING_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_PROC_STOPPED),
			ParallelImages.getImage(ParallelImages.IMG_PROC_STOPPED_SEL) }
	};
	
	public ParallelJobView() {
		jobManager = PTPUIPlugin.getDefault().getJobManager();
	}
	public JobManager getJobManager() {
		return jobManager;
	}
	public String getCurrentView() {
		return current_view;
	}
	public void changeView(String view_flag) {
		current_view = view_flag;
		if (current_view.equals(ParallelJobView.JOB_VIEW)) {
			jobTable.setVisible(true);
			elementViewComposite.setVisible(false);
			sashForm.setWeights(new int[] { 1, 0 });
		}
		else if (current_view.equals(ParallelJobView.PRO_VIEW)) {
			jobTable.setVisible(false);
			elementViewComposite.setVisible(true);
			sashForm.setWeights(new int[] { 0, 1 });
		}
		else {
			jobTable.setVisible(true);
			elementViewComposite.setVisible(true);
			sashForm.setWeights(new int[] { 1, 3 });
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
		cur_job_id = jobManager.initial();
	}
	protected void initialView() {
		initialElement();
		if (jobManager.size() > 0) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					jobTable.removeAll();
					IPJob[] jobs = jobManager.getJobs();
					TableItem item = null;
					for (int i=0; i<jobs.length; i++) {
						item = new TableItem(jobTable, SWT.NONE);
						item.setImage(0, jobImages[jobs[i].isDebug()?1:0][0]);
						item.setText(1, jobs[i].getElementName());
					}
					jobTable.setSelection(0);
				}
			});
			updateJob();
			refresh();
		}
		update();
	}
	public ISetManager getCurrentSetManager() {
		return jobManager.getSetManager(cur_job_id);
	}

	public static ParallelJobView getInstance() {
		if (instance == null)
			instance = new ParallelJobView();
		return instance;
	}
	
	protected void createView(Composite parent) {
		parent.setLayout(new FillLayout());
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayout(new FillLayout());
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		jobTable = new Table(sashForm, SWT.FULL_SELECTION | SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		jobTable.setLayout(new FillLayout());
		jobTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		jobTable.setHeaderVisible(false);
		jobTable.setLinesVisible(false);
		TableColumn col1 = new TableColumn(jobTable, SWT.LEFT);
		col1.setWidth(25);
		TableColumn col2 = new TableColumn(jobTable, SWT.LEFT);
		col2.setWidth(90);	

		jobTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String jobName = jobTable.getItem(jobTable.getSelectionIndex()).getText(1);
				IPJob job = jobManager.findJob(jobName);
				
				if (job != null) {
					//new TableItem(jobInfoTable, 0).setText(new String[] { "Total procsses", String.valueOf(job.totalProcesses()) });
					//new TableItem(jobInfoTable, 0).setText(new String[] { "Used nodes", String.valueOf(job.totalNodes()) });
	
					selectJob(job.getKeyString());
					update();
					refresh();
				}
			}			
		});

		elementViewComposite = createElementView(sashForm);
		sashForm.setWeights(new int[] { 1, 3 });
	}
	
	protected boolean fillContextMenu(IMenuManager manager) {
		manager.add(new ChangeJobViewAction(this));
		return true;
	}
	protected boolean createToolBarActions(IToolBarManager toolBarMgr) {
		changeJobViewAction = new ChangeJobViewAction(this);
		toolBarMgr.add(changeJobViewAction);
		return true;
	}
	protected boolean createMenuActions(IMenuManager menuMgr) {
		return false;
	}
	protected void setActionEnable() {}
	
	protected void doubleClickAction(int element_num) {
		IElement element = cur_element_set.get(element_num);
		if (element != null) {
			openProcessViewer(jobManager.findProcess(cur_job_id, element.getID()));
		}
	}
	
	protected String getToolTipText(int element_num) {
		ISetManager setManager = getCurrentSetManager();
		if (setManager == null)
			return "Unknown element";

		IElement element = cur_element_set.get(element_num);
		if (element == null)
			return "Unknown element";
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("ID: " + element.getID());
		IElementSet[] groups = setManager.getSetsWithElement(element.getID());
		if (groups.length > 1)
			buffer.append("\nGroup: ");
		for (int i = 1; i < groups.length; i++) {
			buffer.append(groups[i].getID());
			if (i < groups.length - 1)
				buffer.append(",");
		}
		buffer.append("\nStatus: " + jobManager.getProcessStatusText(cur_job_id, element.getID()));
		return buffer.toString();
	}

	protected Image getStatusIcon(IElement element) {
		int status = jobManager.getProcessStatus(cur_job_id, element.getID());
		return procImages[status][element.isSelected() ? 1 : 0];
	}
	
	public String getCurrentJobID() {
		return cur_job_id;
	}
	public void selectJob(String job_id) {
		cur_job_id = job_id;
		updateJob();
	}
	public void updateJob() {
		ISetManager setManager = getCurrentSetManager();
		if (setManager != null) {			
			selectSet(setManager.getSetRoot());
		}
	}
	public void updateTitle() {
		if (cur_element_set != null) {
			changeTitle(jobManager.getName(cur_job_id), cur_element_set.getID(), cur_set_size);
		}
	}	

	public void run() {
		System.out.println("------------ run");		
		initialView();
		refresh();
	}

	public void start() {
		System.out.println("------------ start");
		refresh();
	}

	public void stopped() {
		System.out.println("------------ stop");
		refresh();
	}

	public void exit() {
		System.out.println("------------ exit");
		refresh();
	}

	public void abort() {
		System.out.println("------------ abort");
		refresh();
	}

	public void monitoringSystemChangeEvent(Object object) {
		System.out.println("------------ monitoringSystemChangeEvent");
		refresh();
	}

	public void execStatusChangeEvent(Object object) {
		System.out.println("------------ execStatusChangeEvent");
		refresh();
	}

	public void sysStatusChangeEvent(Object object) {
		System.out.println("------------ sysStatusChangeEvent");
		refresh();
	}

	public void processOutputEvent(Object object) {
		System.out.println("------------ processOutputEvent");
		refresh();
	}

	public void errorEvent(Object object) {
		System.out.println("------------ errorEvent");
		refresh();
	}

	public void updatedStatusEvent() {
		System.out.println("------------ updatedStatusEvent");
		refresh();
	}
}
