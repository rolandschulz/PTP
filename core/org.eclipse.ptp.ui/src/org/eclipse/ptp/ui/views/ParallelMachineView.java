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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
	private static ParallelMachineView instance = null;

	// actions
	protected ParallelAction changeMachineAction = null;
	
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
		manager = PTPUIPlugin.getDefault().getMachineManager();
	}
	
	public MachineManager getMachineManager() {
		return (MachineManager)manager;
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
		selectMachine(manager.initial());
	}
	protected void initialView() {
		initialElement();
		if (manager.size() > 0) {
			refresh();
		}
		update();
	}
	public ISetManager getCurrentSetManager() {
		return getMachineManager().getSetManager(getCurrentMachineID());
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
		GridData gdtext = new GridData(GridData.FILL_BOTH);
		gdtext.grabExcessVerticalSpace = true;
		gdtext.grabExcessHorizontalSpace = true;
		gdtext.horizontalAlignment = GridData.FILL;
		gdtext.verticalAlignment = GridData.FILL;
		bleft.setLayoutData(gdtext);
		bleft.setText("Node Info");

		Group bright = new Group(bottom, SWT.BORDER);
		bright.setLayout(new FillLayout());
		GridData gdlist = new GridData(GridData.FILL_BOTH);
		gdlist.grabExcessVerticalSpace = true;
		gdlist.grabExcessHorizontalSpace = true;
		gdlist.horizontalAlignment = GridData.FILL;
		gdlist.verticalAlignment = GridData.FILL;
		bright.setLayoutData(gdlist);
		bright.setText("Process Info");

		BLtable = new Table(bleft, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		BLtable.setLayout(new FillLayout());
		BLtable.setHeaderVisible(false);
		BLtable.setLinesVisible(true);
		new TableColumn(BLtable, SWT.LEFT).setWidth(60);
		new TableColumn(BLtable, SWT.LEFT).setWidth(80);

		BRtable = new Table(bright, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		BRtable.setLayout(new FillLayout());
		BRtable.setHeaderVisible(false);
		BRtable.setLinesVisible(true);
		new TableColumn(BRtable, SWT.LEFT).setWidth(140);
		
		BRtable.addSelectionListener(new SelectionAdapter() {
			/* double click - throw up an editor to look at the process */
			public void widgetDefaultSelected(SelectionEvent e) {
				IPNode node = getMachineManager().findNode(getCurrentMachineID(), cur_selected_element_id);
				if (node != null) {
					int idx = BRtable.getSelectionIndex();
					IPProcess[] procs = node.getSortedProcesses();
					if (idx >= 0 && idx < procs.length) {
						openProcessViewer(procs[idx]);
					}
				}
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
		buffer.append("\nStatus: " + getMachineManager().getNodeStatusText(getCurrentMachineID(), element.getID()));
		return buffer.toString();
	}

	protected Image getStatusIcon(IElement element) {
		int status = getMachineManager().getNodeStatus(getCurrentMachineID(), element.getID());
		return nodeImages[status][element.isSelected() ? 1 : 0];
	}

	public String getCurrentMachineID() {
		return getMachineManager().getCurrentMachineId();
	}
	public void selectMachine(String machine_id) {
		getMachineManager().setCurrentMachineId(machine_id);
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
			changeTitle(manager.getName(getCurrentMachineID()), cur_element_set.getID(), cur_set_size);
		}
	}
	public void deSelectSet() {
		super.deSelectSet();
		cur_selected_element_id = "";
		clearLowerTextRegions();
	}
	protected void paintCanvas(GC g) {
		super.paintCanvas(g);
		updateLowerTextRegions();
	}
	
	protected void drawingRegisterElement(IElement element, GC g, int x_loc, int y_loc, int width, int height) {
		super.drawingRegisterElement(element, g, x_loc, y_loc, width, height);
		if (element.isRegistered()) {
			cur_selected_element_id = element.getID();
		}
	}
	
	public void clearLowerTextRegions() {
		BLtable.removeAll();
		BRtable.removeAll();		
	}

	public void updateLowerTextRegions() {
		clearLowerTextRegions();
		if (cur_selected_element_id.length() == 0)
			return;
			
		IPNode node = getMachineManager().findNode(getCurrentMachineID(), cur_selected_element_id);
		if (node == null)
			return;
		
		new TableItem(BLtable, SWT.NULL).setText(new String[] { "Node #", cur_selected_element_id });
		new TableItem(BLtable, SWT.NULL).setText(new String[] { "State", (String)node.getAttrib("state") });
		new TableItem(BLtable, SWT.NULL).setText(new String[] { "User", (String)node.getAttrib("user") });
		new TableItem(BLtable, SWT.NULL).setText(new String[] { "Group", (String)node.getAttrib("group") });
		new TableItem(BLtable, SWT.NULL).setText(new String[] { "Mode", (String)node.getAttrib("mode") });

		if (node.hasChildren()) {
			IPProcess procs[] = node.getSortedProcesses();
			TableItem item = null;
			for (int i = 0; i < procs.length; i++) {
				int proc_state = getMachineManager().getProcStatus(procs[i].getStatus());
				item = new TableItem(BRtable, SWT.NULL);
				item.setImage(procImages[proc_state][0]);
				item.setText("Process " + procs[i].getProcessNumber() + ", Job " + procs[i].getJob().getJobNumber());
			}
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
