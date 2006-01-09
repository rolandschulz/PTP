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
import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.internal.ui.MachineManager;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.internal.ui.actions.ChangeMachineAction;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
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
	// composite
	protected SashForm sashForm = null;
	protected Composite elementViewComposite = null;
	protected Composite infoComposite = null;
	protected Table BLtable = null;
	protected Table BRtable = null;
	// view flag
	public static final String BOTH_VIEW = "0";
	public static final String MACHINE_VIEW = "1";
	public static final String INFO_VIEW = "2";
	protected String current_view = BOTH_VIEW;

	public ParallelMachineView() {
		instance = this;
		manager = PTPUIPlugin.getDefault().getMachineManager();
	}
	public void changeView(String view_flag) {
		current_view = view_flag;
		if (current_view.equals(ParallelMachineView.MACHINE_VIEW)) {
			elementViewComposite.setVisible(true);
			infoComposite.setVisible(false);
			sashForm.setWeights(new int[] { 1, 0 });
		} else if (current_view.equals(ParallelMachineView.INFO_VIEW)) {
			elementViewComposite.setVisible(false);
			infoComposite.setVisible(true);
			sashForm.setWeights(new int[] { 0, 1 });
		} else {
			elementViewComposite.setVisible(true);
			infoComposite.setVisible(true);
			sashForm.setWeights(new int[] { 3, 1 });
		}
	}
	public Image getImage(int index1, int index2) {
		return ParallelImages.nodeImages[index1][index2];
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
	public static ParallelMachineView getMachineViewInstance() {
		if (instance == null)
			instance = new ParallelMachineView();
		return instance;
	}
	protected void createView(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayout(new FillLayout(SWT.HORIZONTAL));
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		elementViewComposite = createElementView(sashForm);
		infoComposite = createLowerTextRegions(sashForm);
		changeView(current_view);
	}
	protected Composite createLowerTextRegions(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, true);
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		/* inner bottom composite - this one uses a grid layout */
		Group bleft = new Group(composite, SWT.BORDER);
		bleft.setLayout(new FillLayout());
		GridData gdtext = new GridData(GridData.FILL_BOTH);
		gdtext.grabExcessVerticalSpace = true;
		gdtext.grabExcessHorizontalSpace = true;
		gdtext.horizontalAlignment = GridData.FILL;
		gdtext.verticalAlignment = GridData.FILL;
		bleft.setLayoutData(gdtext);
		bleft.setText("Node Info");
		Group bright = new Group(composite, SWT.BORDER);
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
		new TableColumn(BLtable, SWT.LEFT).setWidth(200);
		BRtable = new Table(bright, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		BRtable.setLayout(new FillLayout());
		BRtable.setHeaderVisible(false);
		BRtable.setLinesVisible(true);
		new TableColumn(BRtable, SWT.LEFT).setWidth(300);
		BRtable.addSelectionListener(new SelectionAdapter() {
			/* double click - throw up an editor to look at the process */
			public void widgetDefaultSelected(SelectionEvent e) {
				IPNode node = ((MachineManager) manager).findNode(getCurrentID(), cur_selected_element_id);
				if (node != null) {
					int idx = BRtable.getSelectionIndex();
					IPProcess[] procs = node.getSortedProcesses();
					if (idx >= 0 && idx < procs.length) {
						openProcessViewer(procs[idx]);
					}
				}
			}
		});
		return composite;
	}
	protected void createToolBarActions(IToolBarManager toolBarMgr) {
		changeMachineAction = new ChangeMachineAction(this);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUINAVIGATORGROUP, changeMachineAction);
		super.buildInToolBarActions(toolBarMgr);
	}
	protected void setActionEnable() {}
	public void doubleClick(IElement element) {
		boolean isElementRegistered = element.isRegistered();
		unregister();
		if (!isElementRegistered) {
			register(element);
			getCurrentElementHandler().addRegisterElement(element);
		}
		updateLowerTextRegions();
	}
	public void register(IElement element) {
		element.setRegistered(true);
	}
	public void unregister() {
		IElementHandler elementHandler = getCurrentElementHandler();
		IElementSet rootSet = elementHandler.getSetRoot();
		IElement[] registerElements = elementHandler.getRegisteredElements();
		for (int i = 0; i < registerElements.length; i++) {
			IElement pE = rootSet.get(registerElements[i].getID());
			if (pE != null)
				pE.setRegistered(false);
		}
		elementHandler.removeAllRegisterElements();
	}
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * IToolTipProvider
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	public String getToolTipText(int index) {
		IElementHandler setManager = getCurrentElementHandler();
		if (setManager == null || cur_element_set == null)
			return " Unknown element";
		IElement element = cur_element_set.get(index);
		if (element == null)
			return " Unknown element";
		IPNode node = ((MachineManager) manager).findNode(getCurrentID(), element.getID());
		if (node == null)
			return " Unknown node";
		StringBuffer buffer = new StringBuffer();
		buffer.append(" Node ID: " + node.getNodeNumber());
		buffer.append("\n");
		buffer.append(" Node name: " + node.getElementName());
		IElementSet[] groups = setManager.getSetsWithElement(element.getID());
		if (groups.length > 1)
			buffer.append("\n Group: ");
		for (int i = 1; i < groups.length; i++) {
			buffer.append(groups[i].getID());
			if (i < groups.length - 1)
				buffer.append(",");
		}
		// buffer.append("\nStatus: " + getMachineManager().getNodeStatusText(node));
		return buffer.toString();
	}
	public String getCurrentID() {
		return ((MachineManager) manager).getCurrentMachineId();
	}
	public void selectMachine(String machine_id) {
		((MachineManager) manager).setCurrentMachineId(machine_id);
		updateMachine();
	}
	public void updateMachine() {
		IElementHandler setManager = getCurrentElementHandler();
		if (setManager != null) {
			selectSet(setManager.getSetRoot());
		}
	}
	protected void updateAction() {
		super.updateAction();
		changeMachineAction.setEnabled(((MachineManager) manager).getMachines().length > 0);
	}
	public void clearLowerTextRegions() {
		BLtable.removeAll();
		BRtable.removeAll();
	}
	public void updateLowerTextRegions() {
		clearLowerTextRegions();
		cur_selected_element_id = "";
		IElementHandler elementHandler = getCurrentElementHandler();
		if (elementHandler == null || cur_element_set == null || elementHandler.totalRegisterElements() == 0)
			return;
		String firstRegisteredElementID = elementHandler.getRegisteredElements()[0].getID();
		if (!cur_element_set.contains(firstRegisteredElementID))
			return;
		cur_selected_element_id = firstRegisteredElementID;
		IPNode node = ((MachineManager) manager).findNode(getCurrentID(), cur_selected_element_id);
		if (node == null) {
			return;
		}
		new TableItem(BLtable, SWT.NULL).setText(new String[] { "Node #", node.getNodeNumber() });
		new TableItem(BLtable, SWT.NULL).setText(new String[] { "Name", (String) node.getAttrib(AttributeConstants.ATTRIB_NODE_NAME) });
		new TableItem(BLtable, SWT.NULL).setText(new String[] { "State", (String) node.getAttrib(AttributeConstants.ATTRIB_NODE_STATE) });
		new TableItem(BLtable, SWT.NULL).setText(new String[] { "User", (String) node.getAttrib(AttributeConstants.ATTRIB_NODE_USER) });
		new TableItem(BLtable, SWT.NULL).setText(new String[] { "Group", (String) node.getAttrib(AttributeConstants.ATTRIB_NODE_GROUP) });
		new TableItem(BLtable, SWT.NULL).setText(new String[] { "Mode", (String) node.getAttrib(AttributeConstants.ATTRIB_NODE_MODE) });
		IPProcess procs[] = node.getSortedProcesses();
		if (procs != null) {
			TableItem item = null;
			for (int i = 0; i < procs.length; i++) {
				int proc_state = ((MachineManager) manager).getProcStatus(procs[i].getStatus());
				item = new TableItem(BRtable, SWT.NULL);
				item.setImage(ParallelImages.procImages[proc_state][0]);
				item.setText("Process " + procs[i].getProcessNumber() + ", Job " + procs[i].getJob().getJobNumber());
			}
		}
	}
	public void run(String arg) {
		System.out.println("------------ machine run - job " + arg);
		IPJob job = ((MachineManager) manager).findJob(arg);
		if (job != null) {
			IPMachine[] machines = job.getMachines();
			if (machines.length > 0) {
				selectMachine(machines[0].getIDString());
			}
		}
		update();
		refresh();
	}
	public void updateView(Object condition) {
		updateLowerTextRegions();
	}
	public void start() {
		System.out.println("------------ machine start");
		refresh();
	}
	public void stopped() {
		System.out.println("------------ machine stop");
		refresh();
	}
	public void exit() {
		System.out.println("------------ machine exit");
		refresh();
	}
	public void abort() {
		System.out.println("------------ machine abort");
		refresh();
	}
	public void monitoringSystemChangeEvent(Object object) {
		System.out.println("------------ machine monitoringSystemChangeEvent");
		manager.clear();
		initialView();
		refresh();
	}
	public void execStatusChangeEvent(Object object) {
		System.out.println("------------ machine execStatusChangeEvent");
		refresh();
	}
	public void sysStatusChangeEvent(Object object) {
		System.out.println("------------ machine sysStatusChangeEvent");
		refresh();
	}
	public void processOutputEvent(Object object) {
		System.out.println("------------ machine processOutputEvent");
		refresh();
	}
	public void errorEvent(Object object) {
		System.out.println("------------ machine errorEvent");
		refresh();
	}
	public void updatedStatusEvent() {
		System.out.println("------------ machine updatedStatusEvent");
		refresh();
	}
}
