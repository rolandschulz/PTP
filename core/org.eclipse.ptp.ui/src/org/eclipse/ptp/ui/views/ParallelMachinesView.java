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
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
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
import org.eclipse.ptp.core.events.IModelManagerChangedResourceManagerEvent;
import org.eclipse.ptp.core.events.IModelManagerNewResourceManagerEvent;
import org.eclipse.ptp.core.events.IModelManagerRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerResourceManagerListener;
import org.eclipse.ptp.internal.ui.ParallelImages;
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
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Greg Watson
 * 
 * Based on original work by Clement Chu
 * 
 */
public class ParallelMachinesView extends AbstractParallelSetView implements
	IModelManagerResourceManagerListener, IResourceManagerMachineListener, 
	IMachineNodeListener, INodeProcessListener {
	
	// view flag
	public static final String BOTH_VIEW = "0";
	public static final String MACHINE_VIEW = "1";
	public static final String INFO_VIEW = "2";
	
	// selected element
	protected String cur_selected_element_id = IManager.EMPTY_ID;
	
	protected Menu jobPopupMenu = null;
	protected SashForm upperSashForm = null;
	protected SashForm sashForm = null;
	protected TableViewer machineTableViewer = null;
	protected TableViewer processTableViewer = null;
	protected TableViewer nodeAttrTableViewer = null;
	protected Composite elementViewComposite = null;
	protected Composite infoComposite = null;
	protected ParallelAction terminateAllAction = null;
	protected String current_view = BOTH_VIEW;

	public ParallelMachinesView() {
		this(PTPUIPlugin.getDefault().getMachineManager());
	}
	
	/** Constructor
	 * 
	 */
	public ParallelMachinesView(IManager manager) {
		super(manager);
		
		IModelManager mm = PTPCorePlugin.getDefault().getModelManager();
		
		synchronized (mm) {
		    /*
		     * Add us to any existing RM's. I guess it's possible we could
		     * miss a RM if a new event arrives while we're doing this, but is 
		     * it a problem?
		     */
		    for (IResourceManager rm : mm.getUniverse().getResourceManagers()) {
		        rm.addChildListener(this);
		    }
		    mm.addListener(this);
		}
	}
	
	/** 
	 * Change machine
	 * @param id Machine ID
	 */
	public void changeMachine(final String id) {
		IPMachine machine = ((MachineManager)manager).findMachineById(id);
		changeMachineRefresh(machine);
	}
	
	public void changeMachineRefresh(final IPMachine machine) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				if (!elementViewComposite.isDisposed()) {
					changeMachine(machine);
					machineTableViewer.refresh(true);
					machineTableViewer.setSelection(machine == null ? new StructuredSelection() : new StructuredSelection(machine), true);
					nodeAttrTableViewer.refresh();
					processTableViewer.refresh();
				}
			}
		});
	}
	
	/** Change view
	 * @param view_flag
	 */
	public void changeView(String view_flag) {
		current_view = view_flag;
		if (current_view.equals(ParallelMachinesView.MACHINE_VIEW)) {
			machineTableViewer.getTable().setVisible(true);
			elementViewComposite.setVisible(false);
			infoComposite.setVisible(false);
			sashForm.setWeights(new int[] { 1, 0 });
		} else if (current_view.equals(ParallelMachinesView.INFO_VIEW)) {
			machineTableViewer.getTable().setVisible(false);
			elementViewComposite.setVisible(true);
			infoComposite.setVisible(true);
			sashForm.setWeights(new int[] { 0, 1 });
		} else {
			machineTableViewer.getTable().setVisible(true);
			elementViewComposite.setVisible(true);
			infoComposite.setVisible(true);
			//sashForm.setWeights(new int[] { 3, 1 });
		}
	}
	
	public void dispose() {
		PTPCorePlugin.getDefault().getModelManager().removeListener(this);
		elementViewComposite.dispose();
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#doubleClick(org.eclipse.ptp.ui.model.IElement)
	 */
	public void doubleClick(IElement element) {
		boolean isElementRegistered = element.isRegistered();
		
		/*
		 * Unregister any registered elements
		 */
		unregister();

		/*
		 * Register the new element only if we're not trying
		 * to register the same one.
		 */
		if (!isElementRegistered) {
			/*
			 * Now register the new element
			 */
			register(element);
			getCurrentElementHandler().addRegisterElement(element);
		}
		
		/*
		 * Refresh the attribute and process panes.
		 */
		nodeAttrTableViewer.refresh();
		processTableViewer.refresh();
	}
	
	/** 
	 * Get selected machine
	 * @return selected machine
	 */
	public synchronized IPMachine getCurrentMachine() {
		return getMachineManager().getCurrentMachine();
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
	
	/** Get current view flag
	 * @return flag of view
	 */
	public String getCurrentView() {
		return current_view;
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
	public void handleEvent(final IMachineChangedNodeEvent e) {
		refresh(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IMachineNodeListener#handleEvent(org.eclipse.ptp.core.elements.events.IMachineNewNodeEvent)
	 */
	public void handleEvent(final IMachineNewNodeEvent e) {
		final IPNode node = e.getNode();
		final boolean isCurrent = node.getMachine().equals(getCurrentMachine());

		if (isCurrent) {
			/*
			 * Add node child listener so that we get notified when new processes
			 * are added to the node and can update the node icons.
			 */
			node.addChildListener(this);
		}
		
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				getMachineManager().addNode(node);
				if (isCurrent) {
					updateMachineSet();
					update();
					machineTableViewer.refresh(true);
				}
			}
		});	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IMachineNodeListener#handleEvent(org.eclipse.ptp.core.elements.events.IMachineRemoveNodeEvent)
	 */
	public void handleEvent(final IMachineRemoveNodeEvent e) {
		final IPNode node = e.getNode();
		final boolean isCurrent = node.getMachine().equals(getCurrentMachine());

		if (isCurrent) {
			/*
			 * Remove node child listener when node is removed (if ever)
			 */
			node.removeChildListener(this);
		}
		
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				getMachineManager().removeNode(node);
				if (isCurrent) {
					updateMachineSet();
					update();
					machineTableViewer.refresh(true);
					nodeAttrTableViewer.refresh();
					processTableViewer.refresh();
				}
			}
		});	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.listeners.IModelManagerResourceManagerListener#handleEvent(org.eclipse.ptp.core.events.IModelManagerChangedResourceManagerEvent)
	 */
	public void handleEvent(IModelManagerChangedResourceManagerEvent e) {
		// Don't need to do anything
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.listeners.IModelManagerResourceManagerListener#handleEvent(org.eclipse.ptp.core.events.IModelManagerNewResourceManagerEvent)
	 */
	public void handleEvent(IModelManagerNewResourceManagerEvent e) {
		/*
		 * Add resource manager child listener so we get notified when new
		 * machines are added to the model.
		 */
		final IResourceManager rm = e.getResourceManager();
        rm.addChildListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.listeners.IModelManagerResourceManagerListener#handleEvent(org.eclipse.ptp.core.events.IModelManagerRemoveResourceManagerEvent)
	 */
	public void handleEvent(IModelManagerRemoveResourceManagerEvent e) {
		/*
		 * Removed resource manager child listener when resource manager is removed.
		 */
		e.getResourceManager().removeChildListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.INodeProcessListener#handleEvent(org.eclipse.ptp.core.elements.events.INodeChangedProcessEvent)
	 */
	public void handleEvent(final INodeChangedProcessEvent e) {
		/*
		 * Update views if any node's status changes 
		 */
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				IPNode node = e.getSource();
				if (node == getRegisteredNode()) {
					processTableViewer.refresh();
				}
				refresh(false);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.INodeProcessListener#handleEvent(org.eclipse.ptp.core.elements.events.INodeNewProcessEvent)
	 */
	public void handleEvent(final INodeNewProcessEvent e) {
		/*
		 * Update node icons when a process is added to a node. 
		 */
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				IPNode node = e.getSource();
				if (node == getRegisteredNode()) {
					processTableViewer.refresh();
				}
				refresh(false);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.INodeProcessListener#handleEvent(org.eclipse.ptp.core.elements.events.INodeRemoveProcessEvent)
	 */
	public void handleEvent(final INodeRemoveProcessEvent e) {
		/*
		 * Update node icons when a process is removed from a node. 
		 */
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				IPNode node = e.getSource();
				if (node == getRegisteredNode()) {
					processTableViewer.refresh();
				}
				refresh(false);
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedMachineEvent)
	 */
	public void handleEvent(IResourceManagerChangedMachineEvent e) {
		// Don't need to do anything
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerNewMachineEvent)
	 */
	public void handleEvent(final IResourceManagerNewMachineEvent e) {
		/*
		 * Add us as a child listener so we get notified of node events
		 */
		e.getMachine().addChildListener(this);
		
		/*
		 * Update views when a new machine is added
		 */
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				changeMachineRefresh(e.getMachine());
			}
		});	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerRemoveMachineEvent)
	 */
	public void handleEvent(final IResourceManagerRemoveMachineEvent e) {
		/*
		 * Remove child listener
		 */
		e.getMachine().removeChildListener(this);

		/*
		 * Update views when a machine is removed.
		 */
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				getMachineManager().removeMachine(e.getMachine());
				changeMachineRefresh(null);
			}
		});	
	}

	/** 
	 * Register element
	 * @param element Target element
	 */
	public void register(IElement element) {
		element.setRegistered(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#updateView(java.lang.Object)
	 */
	public void repaint(boolean all) {
		if (all) {
			if (!machineTableViewer.getTable().isDisposed()) {
				machineTableViewer.refresh(true);
			}
		}
		update();
		//updateLowerRegions();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#setFocus()
	 */
	public void setFocus() {
		super.setFocus();
		IPMachine machine = getCurrentMachine();
		if (machine == null) {
			changeMachine((String)null);
		}
	}
	
	/** 
	 * Unregister all registered elements
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
	}
	
	/** 
	 * Update machine set
	 * 
	 */
	public void updateMachineSet() {
		IElementHandler setManager = getCurrentElementHandler();
		selectSet(setManager == null ? null : setManager.getSetRoot());
	}
	
	/**
	 * @return
	 */
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
	
	/** 
	 * Change machine
	 * @param machine
	 */
	protected void changeMachine(final IPMachine machine) {
		selectMachine(machine);
		updateAction();
		update();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#convertElementObject(org.eclipse.ptp.ui.model.IElement)
	 */
	protected Object convertElementObject(IElement element) {
		if (element == null)
			return null;
		
		return getMachineManager().findNode(element.getID());
	}
	
	/** 
	 * Create lower text region layout
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
		Group nodeGroup = new Group(composite, SWT.BORDER);
		nodeGroup.setLayout(new FillLayout());
		GridData gdtext = new GridData(GridData.FILL_BOTH);
		gdtext.grabExcessVerticalSpace = true;
		gdtext.grabExcessHorizontalSpace = true;
		gdtext.horizontalAlignment = GridData.FILL;
		gdtext.verticalAlignment = GridData.FILL;
		nodeGroup.setLayoutData(gdtext);
		nodeGroup.setText("Node Attributes");
		
		Group procGroup = new Group(composite, SWT.BORDER);
		procGroup.setLayout(new FillLayout());
		GridData gdlist = new GridData(GridData.FILL_BOTH);
		gdlist.grabExcessVerticalSpace = true;
		gdlist.grabExcessHorizontalSpace = true;
		gdlist.horizontalAlignment = GridData.FILL;
		gdlist.verticalAlignment = GridData.FILL;
		procGroup.setLayoutData(gdlist);
		procGroup.setText("Process Info");
		
		nodeAttrTableViewer = new TableViewer(nodeGroup, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		Table nodeAttrTable = nodeAttrTableViewer.getTable();
		TableColumn col = new TableColumn(nodeAttrTable, SWT.LEFT);
		col.setText("Attribute");
		col.setWidth(80);
		col.setResizable(true);
		col = new TableColumn(nodeAttrTable, SWT.LEFT);
		col.setText("Value");
		col.setWidth(200);
		col.setResizable(false);
		nodeAttrTable.setHeaderVisible(true);
		nodeAttrTable.setLinesVisible(true);
		nodeAttrTable.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {}
			public void controlResized(ControlEvent e) {
				Table table = (Table)e.getSource();
				int width = table.getClientArea().width;
				switch (table.getColumnCount()) {
				case 1:
					table.getColumn(0).setWidth(width);
					break;
				case 2:
					TableColumn col0 = table.getColumn(0);
					TableColumn col1 = table.getColumn(1);
					if (col0.getWidth() + col1.getWidth() < width) {
						col1.setWidth(width - col0.getWidth());
					}
				}
			}
		});
		nodeAttrTableViewer.setLabelProvider(new ITableLabelProvider() {
			public String getColumnText(Object element, int columnIndex) {
				if (element instanceof IAttribute) {
					IAttribute<?,?,?> attr = (IAttribute<?,?,?>) element;
					switch (columnIndex) {
					case 0:
						return attr.getDefinition().getName();
					case 1:
						return attr.getValueAsString();
					default:
						return "unknown " + columnIndex;
					}
				}
				return "";
			}
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
			public void addListener(ILabelProviderListener listener) {}
			public void dispose() {}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener listener) {}
		});
		nodeAttrTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {}
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof AbstractUIManager) {
					IPNode node = getRegisteredNode();
					if (node != null) {
						return node.getAttributes();
					}
				}
				return new Object[0];
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		nodeAttrTableViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object a1, Object a2) {
				final IAttribute<?,?,?> attr1 = (IAttribute<?,?,?>)a1;
				final IAttribute<?,?,?> attr2 = (IAttribute<?,?,?>)a2;
				final IAttributeDefinition<?, ?, ?> def1 = attr1.getDefinition();
				final IAttributeDefinition<?, ?, ?> def2 = attr2.getDefinition();
				return def1.compareTo(def2);
			}
		});
		nodeAttrTableViewer.setInput(manager);
		
		processTableViewer = new TableViewer(procGroup, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		Table processTable = processTableViewer.getTable();
		new TableColumn(processTable, SWT.LEFT).setWidth(300);
		processTable.setLinesVisible(true);
		processTable.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {}
			public void controlResized(ControlEvent e) {
				Table table = (Table)e.getSource();
				int width = table.getClientArea().width;
				switch (table.getColumnCount()) {
				case 1:
					table.getColumn(0).setWidth(width);
					break;
				case 2:
					TableColumn col0 = table.getColumn(0);
					TableColumn col1 = table.getColumn(1);
					if (col0.getWidth() + col1.getWidth() < width) {
						col1.setWidth(width - col0.getWidth());
					}
				}
			}
		});
		processTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection)event.getSelection();
				openProcessViewer((IPProcess)sel.getFirstElement());
			}
		});
		processTableViewer.setLabelProvider(new LabelProvider() {
			@SuppressWarnings("unchecked")
			public Image getImage(Object element) {
				if (element instanceof IPProcess) {
					IPProcess process = (IPProcess) element;
					int proc_state = getMachineManager().getProcStatus(process.getState());
					return ParallelImages.procImages[proc_state][0];
				}
				return null;
			}
			public String getText(Object element) {
				if (element instanceof IPProcess) {
					IPProcess process = (IPProcess) element;
					return process.getJob().getName() + ":" + process.getName();
				}
				return "";
			}
		});
		processTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {}
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof AbstractUIManager) {
					IPNode node = getRegisteredNode();
					if (node != null) {
						return node.getProcesses();
					}
				}
				return new Object[0];
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		processTableViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object p1, Object p2) {
				IPProcess proc1 = (IPProcess)p1;
				IPProcess proc2 = (IPProcess)p2;
				String name1 = proc1.getJob().getName() + ":" + proc1.getName();
				String name2 = proc2.getJob().getName() + ":" + proc2.getName();
				return name1.compareTo(name2);
			}
		});
		processTableViewer.setInput(manager);

		return composite;
	}
	
	/** 
	 * Create context menu
	 */
	protected void createMachineContextMenu() {
		MenuManager menuMgr = new MenuManager("#machinepopupmenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(IPTPUIConstants.IUIACTIONGROUP));
				manager.add(new Separator(IPTPUIConstants.IUIEMPTYGROUP));
				fillMachineContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(machineTableViewer.getTable());
		machineTableViewer.getTable().setMenu(menu);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#createToolBarActions(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void createToolBarActions(IToolBarManager toolBarMgr) {
		super.buildInToolBarActions(toolBarMgr);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#createView(org.eclipse.swt.widgets.Composite)
	 */
	protected void createView(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		upperSashForm = new SashForm(sashForm, SWT.HORIZONTAL);
		upperSashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		machineTableViewer = new TableViewer(upperSashForm, SWT.SINGLE | SWT.BORDER);
		machineTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		machineTableViewer.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				if (element instanceof IPMachine) {
					IPMachine machine = (IPMachine) element;
					EnumeratedAttribute<MachineAttributes.State> attr = 
						machine.getAttribute(MachineAttributes.getStateAttributeDefinition());
					if (attr != null) {
						return ParallelImages.machineImages[attr.getValueIndex()];
					}
				}
				return null;
			}
			public String getText(Object element) {
				if (element instanceof IPMachine) {
					return ((IPMachine) element).getName();
				}
				return "";
			}
		});
		machineTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {}
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof AbstractUIManager)
					return ((MachineManager) inputElement).getMachines();
				return new Object[0];
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		machineTableViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object j1, Object j2) {
				return ((IPMachine)j1).getName().compareTo(((IPMachine)j2).getName());
			}
		});
		machineTableViewer.setInput(manager);
		machineTableViewer.getTable().addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				ISelection selection = machineTableViewer.getSelection();
				TableItem item = machineTableViewer.getTable().getItem(new Point(e.x, e.y));
				if (item == null && !selection.isEmpty()) {
					machineTableViewer.getTable().deselectAll();
					changeMachine((IPMachine) null);
				}
				else if (item != null) {
					IPMachine machine = (IPMachine)item.getData();
					if (machine == null) {
						changeMachine((IPMachine) null);
					}
					else if (selection.isEmpty()) {
						changeMachine(machine);
					}
					else {
						String cur_id = getCurrentID();
						if (cur_id == null || !cur_id.equals(machine.getID())) {
							changeMachine(machine);
						}
					}
				}
			}
		});
		createMachineContextMenu();
		elementViewComposite = createElementView(upperSashForm);
		
		// ----------------------------------------------------------------------
		// Enable property sheet updates when tree items are selected.
		// Note for this to work each item in the tree must either implement
		// IPropertySource, or support IPropertySource.class as an adapter type
		// in its AdapterFactory.
		// ----------------------------------------------------------------------
		getSite().setSelectionProvider(machineTableViewer);
		
		infoComposite = createLowerRegions(sashForm);
		changeView(current_view);
	}
	
	/** 
	 * Fill the context menu
	 * @param menuManager
	 */
	protected void fillMachineContextMenu(IMenuManager menuManager) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#initialElement()
	 */
	protected void initialElement() {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		
		/*
		 * Add us as a child listener to any existing machines
		 */
		for (IResourceManager rm : universe.getResourceManagers()) {
			for (IPMachine machine : rm.getMachines()) {
				machine.addChildListener(this);
			}
		}
		
		/*
		 * Select initial machine
		 */
		changeMachineRefresh((IPMachine) manager.initial(universe));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#initialView()
	 */
	protected void initialView() {
		initialElement();
		update();
	}
	
	/**
	 * Change machine
	 * @param machine
	 */
	protected void selectMachine(IPMachine machine) {
		IPMachine old = getMachineManager().getCurrentMachine();
		if (old != null) {
			for (IPNode node : old.getNodes()) {
				node.removeChildListener(this);
			}
		}
		getMachineManager().setMachine(machine);
		if (machine != null) {
			for (IPNode node : machine.getNodes()) {
				node.addChildListener(this);
			}
		}
		updateMachineSet();
	}
}
