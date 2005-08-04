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
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.ui.MachineManager;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.ParallelImages;
import org.eclipse.ptp.ui.actions.ChangeMachineAction;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.ISetManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author clement chu
 * 
 */
public class ParallelMachineView extends AbstractParallelSetView {
	public static final String VIEW_ID = "org.eclipse.ptp.ui.views.parallelMachineView";

	private static ParallelMachineView instance = null;
	protected MachineManager machineManager = null;

	// actions
	protected ParallelAction changeMachineAction = null;
	
	//machine
	protected String cur_machine_id = "0";
	
	//selected element
	protected String cur_selected_element_id = "";
	
	//table
	protected Table BLtable = null;
	protected Table BRtable = null;

	public static Image[][] nodeImages = {
		{
			ParallelImages.getImage(ParallelImages.IMG_NODE_USER_ALLOC_EXCL),
			ParallelImages.getImage(ParallelImages.IMG_NODE_USER_ALLOC_EXCL_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_NODE_USER_ALLOC_SHARED),
			ParallelImages.getImage(ParallelImages.IMG_NODE_USER_ALLOC_SHARED_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_EXCL),
			ParallelImages.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_EXCL_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_SHARED),
			ParallelImages.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_SHARED_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_NODE_DOWN),
			ParallelImages.getImage(ParallelImages.IMG_NODE_DOWN_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_NODE_ERROR),
			ParallelImages.getImage(ParallelImages.IMG_NODE_ERROR_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_NODE_EXITED),
			ParallelImages.getImage(ParallelImages.IMG_NODE_EXITED_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_NODE_RUNNING),
			ParallelImages.getImage(ParallelImages.IMG_NODE_RUNNING_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_NODE_UNKNOWN),
			ParallelImages.getImage(ParallelImages.IMG_NODE_UNKNOWN_SEL) },
		{
			ParallelImages.getImage(ParallelImages.IMG_NODE_UP),
			ParallelImages.getImage(ParallelImages.IMG_NODE_UP_SEL) }
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

	public ParallelMachineView() {
		machineManager = PTPUIPlugin.getDefault().getMachineManager();
	}
	
	public MachineManager getMachineManager() {
		return machineManager;
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
		cur_machine_id = machineManager.initial();
	}
	protected void initialView() {
		initialElement();
		if (machineManager.size() > 0) {
			updateMachine();
			refresh();
		}
		update();
	}
	public ISetManager getCurrentSetManager() {
		return machineManager.getSetManager(cur_machine_id);
	}

	public static ParallelMachineView getInstance() {
		if (instance == null)
			instance = new ParallelMachineView();
		return instance;
	}
	
	protected void createView(Composite parent) {
		Composite composite = createElementView(parent);
		createLowerTextRegions(composite);
	}
	
	protected void createLowerTextRegions(Composite parent) {
		FormLayout layout = new FormLayout();
		parent.setLayout(layout);

		/* setup the form layout for the top 'node area' box */
		FormData compositeData = new FormData();
		compositeData.top = new FormAttachment(0);
		compositeData.left = new FormAttachment(0);
		compositeData.right = new FormAttachment(100);
		compositeData.bottom = new FormAttachment(65);
		sc.setLayoutData(compositeData);

		/* setup the form data for the text area */
		FormData bottomData = new FormData();
		bottomData.left = new FormAttachment(0);
		bottomData.right = new FormAttachment(100);
		bottomData.bottom = new FormAttachment(100);
		bottomData.top = new FormAttachment(sc, 20);

		Composite bottomOut = new Composite(parent, SWT.BORDER);
		bottomOut.setLayout(new FillLayout());
		bottomOut.setLayoutData(bottomData);

		/* inner bottom composite - this one uses a grid layout */
		Composite bottom = new Composite(bottomOut, SWT.NONE);
		bottom.setLayout(new GridLayout(2, true));

		Group bleft = new Group(bottom, SWT.BORDER);
		bleft.setLayout(new FillLayout());
		bleft.setText("Node Info");
		Group bright = new Group(bottom, SWT.BORDER);
		bright.setLayout(new FillLayout());
		bright.setText("Process Info");

		BLtable = new Table(bleft, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		BLtable.setLayout(new FillLayout());
		BLtable.setHeaderVisible(false);
		BLtable.setLinesVisible(true);
		TableColumn col1 = new TableColumn(BLtable, SWT.LEFT);
		col1.setWidth(65);
		TableColumn col2 = new TableColumn(BLtable, SWT.LEFT);
		col2.setWidth(90);

		BRtable = new Table(bright, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		BRtable.setLayout(new FillLayout());
		BRtable.setHeaderVisible(false);
		BRtable.setLinesVisible(true);
		col1 = new TableColumn(BRtable, SWT.LEFT);
		col1.setWidth(25);
		col2 = new TableColumn(BRtable, SWT.LEFT);
		col2.setWidth(130);

		GridData gdtext = new GridData(GridData.FILL_BOTH);
		gdtext.grabExcessVerticalSpace = true;
		gdtext.grabExcessHorizontalSpace = true;
		gdtext.horizontalAlignment = GridData.FILL;
		gdtext.verticalAlignment = GridData.FILL;
		bleft.setLayoutData(gdtext);

		GridData gdlist = new GridData(GridData.FILL_BOTH);
		gdlist.grabExcessVerticalSpace = true;
		gdlist.grabExcessHorizontalSpace = true;
		gdlist.horizontalAlignment = GridData.FILL;
		gdlist.verticalAlignment = GridData.FILL;
		bright.setLayoutData(gdlist);
		
		BRtable.addSelectionListener(new SelectionListener() {
			/* single click - do nothing */
			public void widgetSelected(SelectionEvent e) {
			}

			/* double click - throw up an editor to look at the process */
			public void widgetDefaultSelected(SelectionEvent e) {
				int idx = BRtable.getSelectionIndex();

				//if (mode == NODES) {
					IPNode node = machineManager.findNode(cur_machine_id, cur_selected_element_id);
					if (node != null) {
						IPProcess[] procs = node.getSortedProcesses();
						if (idx >= 0 && idx < procs.length) {
							//launchProcessViewer(procs[idx]);
						}
					}
				//} else if (mode == PROCESSES) {
				//	launchProcessViewer(processList[selected_node_num]);
				//}
			}
		});
	}
	
	protected boolean fillContextMenu(IMenuManager manager) {
		manager.add(new ChangeMachineAction(this));
		return true;
	}
	protected boolean createToolBarActions(IToolBarManager toolBarMgr) {
		changeMachineAction = new ChangeMachineAction(this);
		toolBarMgr.add(changeMachineAction);
		return true;
	}
	protected boolean createMenuActions(IMenuManager menuMgr) {
		return false;
	}
	
	protected void setActionEnable() {}

	protected void doubleClickAction(int element_num) {
		IElement element = cur_element_set.get(element_num);
		if (element != null) {
			String tmp_selected_element_id = cur_selected_element_id;
			unregister();
			if (!element.getID().equals(tmp_selected_element_id))
				register(element);
		}
	}
	public void register(IElement element) {
		element.setRegistered(true);
		cur_selected_element_id = element.getID();
	}
	
	public void unregister() {
		IElement pE = cur_element_set.get(cur_selected_element_id);
		if (pE != null) {
			pE.setRegistered(false);
			cur_selected_element_id = "";
			clearLowerTextRegions();
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
		buffer.append("\nStatus: " + machineManager.getNodeStatusText(cur_machine_id, element.getID()));
		return buffer.toString();
	}

	protected Image getStatusIcon(IElement element) {
		int status = machineManager.getNodeStatus(cur_machine_id, element.getID());
		return nodeImages[status][element.isSelected() ? 1 : 0];
	}

	public String getCurrentMachineID() {
		return cur_machine_id;
	}
	public void selectMachine(String machine_id) {
		cur_machine_id = machine_id;
		updateMachine();
	}
	public void updateMachine() {
		ISetManager setManager = getCurrentSetManager();
		if (setManager != null) {			
			selectSet(setManager.getSetRoot());
		}
	}
	
	public void updateTitle() {
		if (cur_element_set != null) {
			changeTitle(machineManager.getName(cur_machine_id), cur_element_set.getID(), cur_set_size);
		}
	}
	public void deSelectSet() {
		super.deSelectSet();
		cur_selected_element_id = "";
		clearLowerTextRegions();
	}
	protected void drawingRegisterElement(IElement element, GC g, int x_loc, int y_loc, int width, int height) {
		super.drawingRegisterElement(element, g, x_loc, y_loc, width, height);
		if (element.isRegistered()) {
			cur_selected_element_id = element.getID();
			updateLowerTextRegions();
		}
	}
	
	public void clearLowerTextRegions() {
		BLtable.removeAll();
		BRtable.removeAll();		
	}
	//Nathan's code
	public void updateLowerTextRegions() {
		if (cur_selected_element_id.length() == 0) {
			clearLowerTextRegions();
			return;
		}
			
		//int idx = BRtable.getSelectionIndex();
		BLtable.removeAll();
		BRtable.removeAll();
		TableItem item;

		//if (mode == NODES) {
			item = new TableItem(BLtable, 0);
			IPNode node = machineManager.findNode(cur_machine_id, cur_selected_element_id);
			
			item.setText(1, cur_selected_element_id);
			item.setText(new String[] { "Node #", cur_selected_element_id });
			item = new TableItem(BLtable, 0);
			item.setText(new String[] { "State", (String)node.getAttrib("state") });
			item = new TableItem(BLtable, 0);
			item.setText(new String[] { "User", (String)node.getAttrib("user") });
			item = new TableItem(BLtable, 0);
			item.setText(new String[] { "Group", (String)node.getAttrib("group") });
			item = new TableItem(BLtable, 0);
			item.setText(new String[] { "Mode", (String)node.getAttrib("mode") });

			if (node.hasChildren()) {
				IPProcess procs[] = node.getSortedProcesses();
				for (int i = 0; i < procs.length; i++) {
					item = new TableItem(BRtable, 0);
					int proc_state = machineManager.getProcStatus(procs[i].getStatus());
					item.setImage(0, procImages[proc_state][0]);
					item.setText(1, "Process " + procs[i].getProcessNumber() + ", Job " + procs[i].getJob().getJobNumber());
				}
			}
		//}
		/*
		else if (mode == PROCESSES) {
			item = new TableItem(BLtable, 0);
			item.setText(new String[] { "Rank",
					processList[selected_node_num].getProcessNumber() });
			item = new TableItem(BLtable, 0);
			item.setText(new String[] { "PID",
					processList[selected_node_num].getPid() });

			String procnum = processList[selected_node_num].getProcessNumber();
			Integer procnumint = null;
			try {
				procnumint = new Integer(procnum);
			} catch (NumberFormatException e) {
				procnumint = null;
			}
			if (procnumint != null) {
				int node_idx = findNodeFromProcess(procnumint.intValue());
				if (node_idx >= 0 && node_idx < displayElements.length) {
					item = new TableItem(BLtable, 0);
					item.setText(new String[] {
							"On node",
							((IPNode) displayElements[node_idx])
									.getNodeNumber() });
				}
			}

			item = new TableItem(BLtable, 0);
			item.setText(new String[] { "Status",
					processList[selected_node_num].getStatus() });
			if (processList[selected_node_num].getStatus().equals(
					IPProcess.EXITED)) {
				item = new TableItem(BLtable, 0);
				item.setText(new String[] { "Exit code",
						processList[selected_node_num].getExitCode() });
			} else if (processList[selected_node_num].getStatus().equals(
					IPProcess.EXITED_SIGNALLED)) {
				item = new TableItem(BLtable, 0);
				item.setText(new String[] { "Signal name",
						processList[selected_node_num].getSignalName() });
			}

			item = new TableItem(BRtable, 0);

			if (processList[selected_node_num].getStatus().equals(
					IPProcess.STARTING))
				item.setImage(0, statusImages[PROC_STARTING][NOT_SELECTED]);
			else if (processList[selected_node_num].getStatus().equals(
					IPProcess.RUNNING))
				item.setImage(0, statusImages[PROC_RUNNING][NOT_SELECTED]);
			else if (processList[selected_node_num].getStatus().equals(
					IPProcess.EXITED))
				item.setImage(0, statusImages[PROC_EXITED][NOT_SELECTED]);
			else if (processList[selected_node_num].getStatus().equals(
					IPProcess.EXITED_SIGNALLED))
				item
						.setImage(0,
								statusImages[PROC_EXITED_SIGNAL][NOT_SELECTED]);
			else if (processList[selected_node_num].getStatus().equals(
					IPProcess.STOPPED))
				item.setImage(0, statusImages[PROC_STOPPED][NOT_SELECTED]);
			else if (processList[selected_node_num].getStatus().equals(
					IPProcess.ERROR))
				item.setImage(0, statusImages[PROC_ERROR][NOT_SELECTED]);
			item.setText(1, "Process "
					+ processList[selected_node_num].getProcessNumber());
		}
		*/
	}	
	
	/*
	 * FIXME Should implemented IParallelModelListener
	 */
	public void run() {
		System.out.println("monitoringSystemChangeEvent");		
		refresh();
	}

	public void start() {
		System.out.println("start");
		initialView();
		refresh();
	}

	public void stop() {
		refresh();
	}

	public void suspend() {
		refresh();
	}

	public void exit() {
		refresh();
	}

	public void error() {
		refresh();
	}

	public void abort() {
		refresh();
	}

	public void stopped() {
		refresh();
	}

	public void monitoringSystemChangeEvent(Object object) {
		System.out.println("monitoringSystemChangeEvent");
		refresh();
	}

	public void execStatusChangeEvent(Object object) {
		System.out.println("execStatusChangeEvent");
		refresh();
	}

	public void sysStatusChangeEvent(Object object) {
		System.out.println("sysStatusChangeEvent");
		refresh();
	}

	public void processOutputEvent(Object object) {
		System.out.println("processOutputEvent");
		refresh();
	}

	public void errorEvent(Object object) {
		System.out.println("errorEvent");
		refresh();
	}

	public void updatedStatusEvent() {
		System.out.println("updatedStatusEvent");
		refresh();
	}
}
