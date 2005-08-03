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
import org.eclipse.ptp.ui.MachineManager;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.ParallelImages;
import org.eclipse.ptp.ui.actions.ChangeMachineAction;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.ISetManager;
import org.eclipse.swt.graphics.Image;

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
	protected String cur_machine_name = "";

	private Image[][] statusImages = {
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
			ParallelImages.getImage(ParallelImages.IMG_NODE_UP_SEL) },
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
	
	protected void initElementAttribute() {
		e_offset_x = 5;
		e_spacing_x = 4;
		e_offset_y = 5;
		e_spacing_y = 4;
		e_width = 16;
		e_height = 16;
	}
	
	protected void initialElement() {
		machineManager.initialMachines();
	}
	protected void initialView() {
		initialElement();
		update();
	}
	public ISetManager getCurrentSetManager() {
		return machineManager.getSetManager(cur_machine_name);
	}

	public static ParallelMachineView getInstance() {
		if (instance == null)
			instance = new ParallelMachineView();
		return instance;
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
	
	protected void setActionEnable() {
		
	}

	protected void doubleClickAction(int element_num) {
		IElement element = cur_element_set.get(element_num);
		if (element != null) {
			//TODO
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
		buffer.append("\nStatus: " + machineManager.getNodeStatusText(element.getID()));
		return buffer.toString();
	}

	protected Image getStatusIcon(IElement element) {
		int status = machineManager.getNodeStatus(element.getID());
		return statusImages[status][element.isSelected() ? 1 : 0];
	}

	public void dispose() {
		super.dispose();
	}

	public String getCurrentMachineName() {
		return cur_machine_name;
	}
	public void selectMachine(String machine_name) {
		cur_machine_name = machine_name;
		updateMachine();
	}
	public void updateMachine() {
		ISetManager setManager = getCurrentSetManager();
		if (setManager != null) {			
			selectSet(setManager.getSetRoot());
			setManager.getSetRoot().setAllSelect(false);
		}
	}
	
	public void updateTitle() {
		if (cur_element_set != null) {
			changeTitle(cur_machine_name, cur_element_set.getID(), cur_set_size);
		}
	}
		
	/*
	 * FIXME Should implemented IParallelModelListener
	 */
	public void run() {
		System.out.println("run");
		initialView();
		redraw();
	}

	public void start() {
		System.out.println("start");
		initialView();
		redraw();
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
