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
package org.eclipse.ptp.debug.ui.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ptp.debug.core.DebugManager;
import org.eclipse.ptp.debug.core.IDebugParallelModelListener;
import org.eclipse.ptp.debug.ui.UIDebugManager;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.actions.RegisterAction;
import org.eclipse.ptp.debug.ui.actions.ResumeAction;
import org.eclipse.ptp.debug.ui.actions.StepOverAction;
import org.eclipse.ptp.debug.ui.actions.StepReturnAction;
import org.eclipse.ptp.debug.ui.actions.SuspendAction;
import org.eclipse.ptp.debug.ui.actions.TerminateAction;
import org.eclipse.ptp.debug.ui.actions.UnregisterAction;
import org.eclipse.ptp.ui.MachineManager;
import org.eclipse.ptp.ui.ParallelImages;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.ISetManager;
import org.eclipse.ptp.ui.views.AbstractParallelSetView;
import org.eclipse.swt.graphics.Image;

/**
 * @author clement chu
 * 
 */
public class DebugParallelView extends AbstractParallelSetView implements IDebugParallelModelListener {
	public static final String VIEW_ID = "org.eclipse.ptp.debug.ui.views.debugParallelView";

	private static DebugParallelView instance = null;
	private UIDebugManager uiDebugManager = null;

	//machine
	protected String cur_job_name = "dummy";

	// actions
	protected ParallelAction resumeAction = null;
	protected ParallelAction suspendAction = null;
	protected ParallelAction terminateAction = null;
	protected ParallelAction stepIntoAction = null;
	protected ParallelAction stepOverAction = null;
	protected ParallelAction stepReturnAction = null;
	protected ParallelAction registerAction = null;
	protected ParallelAction unregisterAction = null;

	private Image[][] statusImages = {
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

	public DebugParallelView() {
		uiDebugManager = PTPDebugUIPlugin.getDefault().getUIDebugManager();
		//FIXME dummy
		DebugManager.getInstance().addListener(this);		
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
		uiDebugManager.initialJobs();
	}
	protected void initialView() {
		initialElement();
		update();
	}
	public ISetManager getCurrentSetManager() {
		return uiDebugManager.getSetManager(cur_job_name);
	}

	public static DebugParallelView getInstance() {
		if (instance == null)
			instance = new DebugParallelView();
		return instance;
	}
	
	protected boolean fillContextMenu(IMenuManager manager) {
		manager.add(resumeAction);
		manager.add(suspendAction);
		manager.add(terminateAction);
		manager.add(new Separator());
		manager.add(stepIntoAction);
		manager.add(stepOverAction);
		manager.add(stepReturnAction);
		return true;
	}
	protected boolean createToolBarActions(IToolBarManager toolBarMgr) {
		resumeAction = new ResumeAction(this);
		suspendAction = new SuspendAction(this);
		terminateAction = new TerminateAction(this);

		stepIntoAction = new StepReturnAction(this);
		stepOverAction = new StepOverAction(this);
		stepReturnAction = new StepReturnAction(this);
		
		registerAction = new RegisterAction(this);
		unregisterAction = new UnregisterAction(this);

		toolBarMgr.add(resumeAction);
		toolBarMgr.add(suspendAction);
		toolBarMgr.add(terminateAction);
		toolBarMgr.add(new Separator());
		toolBarMgr.add(stepIntoAction);
		toolBarMgr.add(stepOverAction);
		toolBarMgr.add(stepReturnAction);
		toolBarMgr.add(new Separator());
		toolBarMgr.add(registerAction);
		toolBarMgr.add(unregisterAction);
		return true;
	}
	protected boolean createMenuActions(IMenuManager menuMgr) {
		return false;
	}
	
	protected void setActionEnable() {
		
	}

	protected void doubleClickAction(int element_num) {
		IElement element = cur_element_set.get(element_num);
		if (element != null)
			registerElement(element);
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
		buffer.append("\nStatus: " + uiDebugManager.getProcessStatus(element.getID()));
		return buffer.toString();
	}

	protected Image getStatusIcon(IElement element) {
		int status = uiDebugManager.getProcessStatus(element.getID()) - MachineManager.NODE_UP;
		return statusImages[status][element.isSelected() ? 1 : 0];
	}
	
	public void dispose() {
		super.dispose();
		//FIME dummy
		DebugManager.getInstance().removeListener(this);		
	}

	public String getCurrentMachineName() {
		return cur_job_name;
	}
	public void selectMachine(String job_name) {
		cur_job_name = job_name;
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
			changeTitle(cur_job_name, cur_element_set.getID(), cur_set_size);
		}
	}	
	
	public void registerElement(IElement element) {
		if (element.isRegistered())
			uiDebugManager.unregisterElements(new IElement[] { element });
		else
			uiDebugManager.registerElements(new IElement[] { element });

		element.setRegistered(!element.isRegistered());
	}

	public void registerSelectedElements() {
		if (cur_element_set != null) {
			IElement[] elements = cur_element_set.getSelectedElements();
			for (int i = 0; i < elements.length; i++) {
				elements[i].setRegistered(true);
			}
			uiDebugManager.registerElements(elements);
		}
	}

	public void unregisterSelectedElements() {
		if (cur_element_set != null) {
			IElement[] elements = cur_element_set.getSelectedElements();
			for (int i = 0; i < elements.length; i++) {
				elements[i].setRegistered(false);
			}
			uiDebugManager.unregisterElements(elements);
		}
	}
	
	/*
	 * FIXME Should implemented IParallelModelListener
	 */
	public void run() {
		initialView();
		redraw();
	}

	public void start() {
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
	}

	public void stopped() {
	}

	public void monitoringSystemChangeEvent(Object object) {
	}

	public void execStatusChangeEvent(Object object) {
	}

	public void sysStatusChangeEvent(Object object) {
	}

	public void processOutputEvent(Object object) {
	}

	public void errorEvent(Object object) {
	}

	public void updatedStatusEvent() {
	}
}
