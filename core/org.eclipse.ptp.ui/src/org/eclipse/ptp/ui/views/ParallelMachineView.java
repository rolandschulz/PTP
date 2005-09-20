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
import org.eclipse.swt.graphics.GC;
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
	public static Image[][] nodeImages = { { ParallelImages.getImage(ParallelImages.IMG_NODE_USER_ALLOC_EXCL), ParallelImages.getImage(ParallelImages.IMG_NODE_USER_ALLOC_EXCL_SEL) },
			{ ParallelImages.getImage(ParallelImages.IMG_NODE_USER_ALLOC_SHARED), ParallelImages.getImage(ParallelImages.IMG_NODE_USER_ALLOC_SHARED_SEL) }, { ParallelImages.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_EXCL), ParallelImages.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_EXCL_SEL) },
			{ ParallelImages.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_SHARED), ParallelImages.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_SHARED_SEL) }, { ParallelImages.getImage(ParallelImages.IMG_NODE_DOWN), ParallelImages.getImage(ParallelImages.IMG_NODE_DOWN_SEL) },
			{ ParallelImages.getImage(ParallelImages.IMG_NODE_ERROR), ParallelImages.getImage(ParallelImages.IMG_NODE_ERROR_SEL) }, { ParallelImages.getImage(ParallelImages.IMG_NODE_EXITED), ParallelImages.getImage(ParallelImages.IMG_NODE_EXITED_SEL) },
			{ ParallelImages.getImage(ParallelImages.IMG_NODE_RUNNING), ParallelImages.getImage(ParallelImages.IMG_NODE_RUNNING_SEL) }, { ParallelImages.getImage(ParallelImages.IMG_NODE_UNKNOWN), ParallelImages.getImage(ParallelImages.IMG_NODE_UNKNOWN_SEL) },
			{ ParallelImages.getImage(ParallelImages.IMG_NODE_UP), ParallelImages.getImage(ParallelImages.IMG_NODE_UP_SEL) } };
	public static Image[][] procImages = { { ParallelImages.getImage(ParallelImages.IMG_PROC_ERROR), ParallelImages.getImage(ParallelImages.IMG_PROC_ERROR_SEL) }, { ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED), ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED_SEL) },
			{ ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED_SIGNAL), ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED_SIGNAL_SEL) }, { ParallelImages.getImage(ParallelImages.IMG_PROC_RUNNING), ParallelImages.getImage(ParallelImages.IMG_PROC_RUNNING_SEL) },
			{ ParallelImages.getImage(ParallelImages.IMG_PROC_STARTING), ParallelImages.getImage(ParallelImages.IMG_PROC_STARTING_SEL) }, { ParallelImages.getImage(ParallelImages.IMG_PROC_STOPPED), ParallelImages.getImage(ParallelImages.IMG_PROC_STOPPED_SEL) } };

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
	protected void doubleClickAction(int element_num) {
		if (cur_element_set !=  null) {
			IElement element = cur_element_set.get(element_num);
			if (element != null) {
				boolean isElementRegistered = element.isRegistered();
				unregister();
				if (!isElementRegistered) {
					register(element);
					getCurrentElementHandler().addRegisterElement(element.getID());
				}
			}
		}
	}
	public void register(IElement element) {
		element.setRegistered(true);
	}
	public void unregister() {
		IElementHandler elementHandler = getCurrentElementHandler();
		IElementSet rootSet = elementHandler.getSetRoot();
		String[] registerElements = elementHandler.getRegisteredElementsID();
		for (int i = 0; i < registerElements.length; i++) {
			IElement pE = rootSet.get(registerElements[i]);
			if (pE != null)
				pE.setRegistered(false);
		}
		elementHandler.removeAllRegisterElements();
	}
	protected String getToolTipText(int element_num) {
		IElementHandler setManager = getCurrentElementHandler();
		if (setManager == null || cur_element_set == null)
			return "Unknown element";
		IElement element = cur_element_set.get(element_num);
		if (element == null)
			return "Unknown element";
		IPNode node = ((MachineManager) manager).findNode(getCurrentID(), element.getID());
		if (node == null)
			return "Unknown node";
		StringBuffer buffer = new StringBuffer();
		buffer.append("Node ID: " + node.getNodeNumber());
		buffer.append("\n");
		buffer.append("Node name: " + node.getElementName());
		IElementSet[] groups = setManager.getSetsWithElement(element.getID());
		if (groups.length > 1)
			buffer.append("\nGroup: ");
		for (int i = 1; i < groups.length; i++) {
			buffer.append(groups[i].getID());
			if (i < groups.length - 1)
				buffer.append(",");
		}
		// buffer.append("\nStatus: " + getMachineManager().getNodeStatusText(node));
		return buffer.toString();
	}
	protected Image getStatusIcon(IElement element) {
		int status = ((MachineManager) manager).getNodeStatus(getCurrentID(), element.getID());
		return nodeImages[status][element.isSelected() ? 1 : 0];
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
	public void deSelectSet() {
		super.deSelectSet();
		cur_selected_element_id = "";
	}
	protected void paintCanvas(GC g) {
		super.paintCanvas(g);
		updateLowerTextRegions();
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
		String firstRegisteredElementID = elementHandler.getRegisteredElementsID()[0];
		if (!cur_element_set.contains(firstRegisteredElementID))
			return;
		cur_selected_element_id = firstRegisteredElementID;
		IPNode node = ((MachineManager) manager).findNode(getCurrentID(), cur_selected_element_id);
		if (node == null) {
			return;
		}
		new TableItem(BLtable, SWT.NULL).setText(new String[] { "Node #", node.getNodeNumber() });
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
				item.setImage(procImages[proc_state][0]);
				item.setText("Process " + procs[i].getProcessNumber() + ", Job " + procs[i].getJob().getJobNumber());
			}
		}
	}
	public void run() {
		System.out.println("------------ machine run");
		refresh();
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
