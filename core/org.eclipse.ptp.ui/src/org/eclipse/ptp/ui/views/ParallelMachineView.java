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

import java.util.Map;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.events.IMachineChangedNodeEvent;
import org.eclipse.ptp.core.elements.events.IMachineNewNodeEvent;
import org.eclipse.ptp.core.elements.events.IMachineRemoveNodeEvent;
import org.eclipse.ptp.core.elements.events.INodeChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.INodeNewProcessEvent;
import org.eclipse.ptp.core.elements.events.INodeRemoveProcessEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerNewMachineEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerRemoveMachineEvent;
import org.eclipse.ptp.core.elements.listeners.IMachineNodeListener;
import org.eclipse.ptp.core.elements.listeners.INodeProcessListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.internal.ui.actions.ChangeMachineAction;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.managers.AbstractUIManager;
import org.eclipse.ptp.ui.managers.MachineManager;
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
public class ParallelMachineView extends AbstractParallelSetView implements IResourceManagerMachineListener, IMachineNodeListener, INodeProcessListener {

	// view flag
	public static final String BOTH_VIEW = "0";

	public static final String MACHINE_VIEW = "1";
	
	public static final String INFO_VIEW = "2";

	// actions
	protected ParallelAction changeMachineAction = null;

	// composite
	protected SashForm sashForm = null;

	protected Composite elementViewComposite = null;

	protected Composite infoComposite = null;
	protected Table BLtable = null;
	protected Table BRtable = null;
	protected String current_view = BOTH_VIEW;
	
	public ParallelMachineView() {
		this(PTPUIPlugin.getDefault().getMachineManager());
	}
	
	public ParallelMachineView(IManager manager) {
		super(manager);
		//PTPCorePlugin.getDefault().getModelPresentation().addNodeListener(this);
		//manager.addJobChangedListener(this);
	}
	
	/** Change view flag
	 * @param view_flag view flag
	 */
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
	
	public void dispose() {
		//manager.removeJobChangedListener(this);
		//PTPCorePlugin.getDefault().getModelPresentation().removeNodeListener(this);
		IPMachine machine = getMachineManager().getCurrentMachine();
		if (machine != null) {
			machine.removeChildListener(this);
		}
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#doubleClick(org.eclipse.ptp.ui.model.IElement)
	 */
	public void doubleClick(IElement element) {
		/*
		 * Remove handler from previously registered node, if any
		 */
		IPNode node = getRegisteredNode();
		if (node != null) {
			node.removeChildListener(this);
		}
		
		/*
		 * Unregister any registered elements
		 */
		boolean isElementRegistered = element.isRegistered();
		unregister();
		
		/*
		 * Now register the new element if we're not trying
		 * to register the same one (toggle action).
		 */
		if (!isElementRegistered) {
			register(element);
			getCurrentElementHandler().addRegisterElement(element);
			if (element instanceof IPNode) {
				node = (IPNode)element;
				node.addChildListener(this);
			}
			updateLowerRegions();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getCurrentID()
	 */
	public String getCurrentID() {
		IPMachine machine = getMachineManager().getCurrentMachine();
		if (machine != null) {
			return machine.getID();
		}
		return IManager.EMPTY_ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getImage(int, int)
	 */
	public Image getImage(int index1, int index2) {
		return ParallelImages.nodeImages[index1][index2];
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.IContentProvider#getRulerIndex(java.lang.Object, int)
	 */
	public String getRulerIndex(Object obj, int index) {
		if (obj instanceof IElement) {
			Object nodeObj = convertElementObject((IElement)obj);
			if (nodeObj instanceof IPNode) {
				return ((IPNode)nodeObj).getNodeNumber();
			}
		}
		return super.getRulerIndex(obj, index);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getToolTipText(java.lang.Object)
	 */
	public String[] getToolTipText(Object obj) {
		IElementHandler setManager = getCurrentElementHandler();
		if (obj == null || !(obj instanceof IPNode) || setManager == null || cur_element_set == null)
			return IToolTipProvider.NO_TOOLTIP;

		IPNode node = (IPNode)obj;
		StringBuffer buffer = new StringBuffer();
		buffer.append(node.getName());
		IElementSet[] sets = setManager.getSetsWithElement(node.getID());
		if (sets.length > 1)
			buffer.append("\n Set: ");
		for (int i = 1; i < sets.length; i++) {
			buffer.append(sets[i].getID());
			if (i < sets.length - 1)
				buffer.append(",");
		}
		// buffer.append("\nStatus: " + getMachineManager().getNodeStatusText(node));
		return new String[] { buffer.toString() };
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IMachineNodeListener#handleEvent(org.eclipse.ptp.core.elements.events.IMachineChangedNodeEvent)
	 */
	public void handleEvent(IMachineChangedNodeEvent e) {
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				refresh(false);
			}
		});	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IMachineNodeListener#handleEvent(org.eclipse.ptp.core.elements.events.IMachineNewNodeEvent)
	 */
	public void handleEvent(final IMachineNewNodeEvent e) {
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				IPMachine machine = e.getSource();
				IPNode node = e.getNode();
				// only redraw if the current set contain the node
				final String idString;
				if (machine == null) {
					idString = "none";
				}
				else {
					idString = machine.getID();
				}
				getMachineManager().addNode(node);
				if (getMachineManager().isCurrentSetContainNode(idString,
						node.getID())) {
					refresh(false);
				}		
			}
		});	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IMachineNodeListener#handleEvent(org.eclipse.ptp.core.elements.events.IMachineRemoveNodeEvent)
	 */
	public void handleEvent(IMachineRemoveNodeEvent e) {
		// TODO need to implement node removal
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.INodeProcessListener#handleEvent(org.eclipse.ptp.core.elements.events.INodeChangedProcessEvent)
	 */
	public void handleEvent(final INodeChangedProcessEvent e) {
		/*
		 * Only the currently registered node will receive these events 
		 */
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				updateProcessInfoRegion(e.getSource());
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.INodeProcessListener#handleEvent(org.eclipse.ptp.core.elements.events.INodeNewProcessEvent)
	 */
	public void handleEvent(final INodeNewProcessEvent e) {
		/*
		 * Only the currently registered node will receive these events 
		 */
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				updateProcessInfoRegion(e.getSource());
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.INodeProcessListener#handleEvent(org.eclipse.ptp.core.elements.events.INodeRemoveProcessEvent)
	 */
	public void handleEvent(final INodeRemoveProcessEvent e) {
		/*
		 * Only the currently registered node will receive these events 
		 */
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				updateProcessInfoRegion(e.getSource());
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedMachineEvent)
	 */
	public void handleEvent(IResourceManagerChangedMachineEvent e) {
		// Doesn't matter
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerNewMachineEvent)
	 */
	public void handleEvent(final IResourceManagerNewMachineEvent e) {
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				IPMachine machine = e.getMachine();
				getMachineManager().addMachine(machine);
				updateAction();
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerRemoveMachineEvent)
	 */
	public void handleEvent(IResourceManagerRemoveMachineEvent e) {
		// TODO implement remote machine
	}
	
	/** Register element
	 * @param element Target element
	 */
	public void register(IElement element) {
		element.setRegistered(true);
	}
		
	public void repaint(boolean all) {
		updateLowerRegions();
	}
	
	/** Change machine
	 * @param machine_id machine ID
	 */
	public void selectMachine(IPMachine machine) {
		IPMachine old = getMachineManager().getCurrentMachine();
		if (old != null) {
			old.removeChildListener(this);
		}
		if (machine != null) {
			getMachineManager().setCurrentMachine(machine);
			machine.addChildListener(this);
		}
		updateMachineSet();
	}
	
	/** Unregister all registered elements
	 * 
	 */
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#updateAction()
	 */
	public void updateAction() {
		super.updateAction();
		if (changeMachineAction != null) {
			boolean enabled = ((AbstractUIManager) manager).getResourceManagers().length > 0;
			changeMachineAction.setEnabled(enabled);
		}
	}	
	
	/** Update emachine
	 * 
	 */
	public void updateMachineSet() {
		IElementHandler setManager = getCurrentElementHandler();
		selectSet(setManager == null ? null : setManager.getSetRoot());
	}
	
	private MachineManager getMachineManager() {
		return ((MachineManager) manager);
	}
	
	private IPNode getRegisteredNode() {
		cur_selected_element_id = "";
		IElementHandler elementHandler = getCurrentElementHandler();
		if (elementHandler == null || cur_element_set == null || elementHandler.totalRegisterElements() == 0)
			return null;
		String firstRegisteredElementID = elementHandler.getRegisteredElements()[0].getID();
		if (!cur_element_set.contains(firstRegisteredElementID))
			return null;
		cur_selected_element_id = firstRegisteredElementID;
		return getMachineManager().findNode(cur_selected_element_id);
	}
	
	/** Update lower regions
	 * 
	 */
	private void updateLowerRegions() {
		IPNode node = getRegisteredNode();
		if (node != null) {
			updateNodeInfoRegion(node);
			updateProcessInfoRegion(node);
		}
	}
	
	private void updateNodeInfoRegion(IPNode node) {
		BLtable.removeAll();
		for (Map.Entry<String, IAttribute> entry : node.getAttributeEntrySet()) {
			String key = entry.getValue().getDefinition().getName();
			String value = entry.getValue().getValueAsString();
			new TableItem(BLtable, SWT.NULL).setText(new String[] { key, value });
		}
	}
	
	private void updateProcessInfoRegion(IPNode node) {
		IPProcess procs[] = node.getSortedProcesses();
		if (procs != null) {
			BRtable.removeAll();
			TableItem item = null;
			for (int i = 0; i < procs.length; i++) {
				int proc_state = getMachineManager().getProcStatus(procs[i].getState());
				item = new TableItem(BRtable, SWT.NULL);
				item.setImage(ParallelImages.procImages[proc_state][0]);
				final IPJob job = procs[i].getJob();
				String jobName = "none";
				if (job != null) {
					jobName = job.getName();
				}
				item.setText("Process " + procs[i].getProcessNumber() + ", Job " + jobName);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#convertElementObject(org.eclipse.ptp.ui.model.IElement)
	 */
	protected Object convertElementObject(IElement element) {
		if (element == null)
			return null;
		
		return getMachineManager().findNode(element.getID());
	}
	
	/** Create lower text region layout
	 * @param parent
	 * @return
	 */
	protected Composite createLowerRegions(Composite parent) {
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
		new TableColumn(BLtable, SWT.LEFT).setWidth(80);
		new TableColumn(BLtable, SWT.LEFT).setWidth(200);
		BRtable = new Table(bright, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		BRtable.setLayout(new FillLayout());
		BRtable.setHeaderVisible(false);
		BRtable.setLinesVisible(true);
		new TableColumn(BRtable, SWT.LEFT).setWidth(300);
		BRtable.addSelectionListener(new SelectionAdapter() {
			/* double click - throw up an editor to look at the process */
			public void widgetDefaultSelected(SelectionEvent e) {
				IPNode node = getMachineManager().findNode(cur_selected_element_id);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#createToolBarActions(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void createToolBarActions(IToolBarManager toolBarMgr) {
		changeMachineAction = new ChangeMachineAction(this);
		toolBarMgr.appendToGroup(IPTPUIConstants.IUINAVIGATORGROUP, changeMachineAction);
		super.buildInToolBarActions(toolBarMgr);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#createView(org.eclipse.swt.widgets.Composite)
	 */
	protected void createView(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayout(new FillLayout(SWT.HORIZONTAL));
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		elementViewComposite = createElementView(sashForm);
		infoComposite = createLowerRegions(sashForm);
		changeView(current_view);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#initialElement()
	 */
	protected void initialElement() {
		selectMachine((IPMachine)manager.initial());
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#initialView()
	 */
	protected void initialView() {
		initialElement();
		if (manager.size() > 0) {
			refresh(false);
		}
		update();
	}
}
