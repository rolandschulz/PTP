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

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.attributes.MachineAttributes;
import org.eclipse.ptp.core.elements.events.IChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IChangedNodeEvent;
import org.eclipse.ptp.core.elements.events.IChangedProcessEvent;
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewNodeEvent;
import org.eclipse.ptp.core.elements.events.INewProcessEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveNodeEvent;
import org.eclipse.ptp.core.elements.events.IRemoveProcessEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;
import org.eclipse.ptp.core.elements.listeners.IMachineChildListener;
import org.eclipse.ptp.core.elements.listeners.INodeChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.events.IChangedResourceManagerEvent;
import org.eclipse.ptp.core.events.INewResourceManagerEvent;
import org.eclipse.ptp.core.events.IRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerChildListener;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.internal.ui.model.PProcessUI;
import org.eclipse.ptp.ui.IElementManager;
import org.eclipse.ptp.ui.IMachineManager;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.utils.core.BitSetIterable;
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
 *         Based on original work by Clement Chu
 * 
 */
public class ParallelMachinesView extends AbstractParallelSetView implements ISelectionProvider {
	private final class MachineChildListener implements IMachineChildListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IMachineNodeListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IMachineChangedNodeEvent)
		 */
		public void handleEvent(final IChangedNodeEvent e) {
			refresh(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IMachineNodeListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IMachineNewNodeEvent)
		 */
		public void handleEvent(final INewNodeEvent e) {
			final boolean isCurrent = e.getSource().equals(getCurrentMachine());

			UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() {
					for (IPNode node : e.getNodes()) {
						if (isCurrent) {
							/*
							 * Add node child listener so that we get notified
							 * when new processes are added to the node and can
							 * update the node icons.
							 */
							node.addChildListener(nodeListener);
						}

						getMachineManager().addNode(node);
					}

					if (isCurrent) {
						updateMachineSet();
						repaint(true);
					}
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IMachineNodeListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IMachineRemoveNodeEvent)
		 */
		public void handleEvent(final IRemoveNodeEvent e) {
			final boolean isCurrent = e.getSource().equals(getCurrentMachine());

			UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() {
					for (IPNode node : e.getNodes()) {
						if (isCurrent) {
							/*
							 * Remove node child listener when node is removed
							 * (if ever)
							 */
							node.removeChildListener(nodeListener);

						}

						getMachineManager().removeNode(node);
					}

					if (isCurrent) {
						updateMachineSet();
						repaint(true);
						nodeAttrTableViewer.refresh();
						processTableViewer.refresh();
					}
				}
			});
		}
	}

	private final class MMChildListener implements IModelManagerChildListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent
		 * (org.eclipse.ptp.core.events.IChangedResourceManagerEvent)
		 */
		public void handleEvent(IChangedResourceManagerEvent e) {
			// Don't need to do anything
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent
		 * (org.eclipse.ptp.core.events.INewResourceManagerEvent)
		 */
		public void handleEvent(INewResourceManagerEvent e) {
			/*
			 * Add resource manager child listener so we get notified when new
			 * machines are added to the model.
			 */
			final IPResourceManager rm = e.getResourceManager();
			rm.addChildListener(resourceManagerListener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent
		 * (org.eclipse.ptp.core.events.IRemoveResourceManagerEvent)
		 */
		public void handleEvent(IRemoveResourceManagerEvent e) {
			/*
			 * Removed resource manager child listener when resource manager is
			 * removed.
			 */
			e.getResourceManager().removeChildListener(resourceManagerListener);
		}
	}

	private final class NodeChildListener implements INodeChildListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.INodeChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IChangedProcessEvent)
		 */
		public void handleEvent(final IChangedProcessEvent e) {
			/*
			 * Update views if any node's processes status changes
			 */
			if (e.getSource() instanceof IPNode) {
				if ((IPNode) e.getSource() == getRegisteredNode()) {
					UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
						public void run() {
							processTableViewer.refresh();
						}
					});
				}
				refresh(false);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.INodeProcessListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.INodeNewProcessEvent)
		 */
		public void handleEvent(final INewProcessEvent e) {
			/*
			 * Update node icons when a process is added to a node.
			 */
			if (e.getSource() instanceof IPNode) {
				if ((IPNode) e.getSource() == getRegisteredNode()) {
					UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
						public void run() {
							processTableViewer.refresh();
						}
					});
					refresh(false);
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.INodeChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IRemoveProcessEvent)
		 */
		public void handleEvent(final IRemoveProcessEvent e) {
			/*
			 * Update node icons when a process is removed from a node.
			 */
			if (e.getSource() instanceof IPNode) {
				UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
					public void run() {
						IPNode node = (IPNode) e.getSource();
						if (node == getRegisteredNode()) {
							processTableViewer.refresh();
						}
					}
				});
				refresh(false);
			}
		}
	}

	private final class RMChildListener implements IResourceManagerChildListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.IChangedJobEvent)
		 */
		public void handleEvent(IChangedJobEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.
		 * IResourceManagerChangedMachineEvent)
		 */
		public void handleEvent(IChangedMachineEvent e) {
			UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() {
					if (!elementViewComposite.isDisposed()) {
						machineTableViewer.refresh(true);
					}
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.
		 * IResourceManagerChangedQueueEvent)
		 */
		public void handleEvent(IChangedQueueEvent e) {
			// Can safely ignore
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.INewJobEvent)
		 */
		public void handleEvent(INewJobEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.
		 * IResourceManagerNewMachineEvent)
		 */
		public void handleEvent(final INewMachineEvent e) {
			for (IPMachine machine : e.getMachines()) {
				/*
				 * Add us as a child listener so we get notified of node events
				 */
				machine.addChildListener(machineListener);

				/*
				 * Update views when a new machine is added
				 */
				changeMachineRefresh(machine);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.INewQueueEvent)
		 */
		public void handleEvent(INewQueueEvent e) {
			// Can safely ignore
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.IRemoveJobEvent)
		 */
		public void handleEvent(IRemoveJobEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.
		 * IResourceManagerRemoveMachineEvent)
		 */
		public void handleEvent(final IRemoveMachineEvent e) {
			/*
			 * Update views when a machine is removed.
			 */
			UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() {
					for (IPMachine machine : e.getMachines()) {
						/*
						 * Remove child listener
						 */
						machine.removeChildListener(machineListener);

						/*
						 * remove from machine manager
						 */
						getMachineManager().removeMachine(machine);
					}

					changeMachineRefresh(null);
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.
		 * IResourceManagerRemoveQueueEvent)
		 */
		public void handleEvent(IRemoveQueueEvent e) {
			// Can safely ignore
		}
	}

	// view flag
	public static final String BOTH_VIEW = "0"; //$NON-NLS-1$

	public static final String MACHINE_VIEW = "1"; //$NON-NLS-1$

	public static final String INFO_VIEW = "2"; //$NON-NLS-1$

	private final ListenerList listeners = new ListenerList();

	private final IModelManagerChildListener modelManagerListener = new MMChildListener();

	private final IResourceManagerChildListener resourceManagerListener = new RMChildListener();

	private final IMachineChildListener machineListener = new MachineChildListener();

	private final INodeChildListener nodeListener = new NodeChildListener();

	private ISelection selection = null;

	// selected element
	protected String cur_selected_element_id = IElementManager.EMPTY_ID;

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

	public ParallelMachinesView(IElementManager manager) {
		super(manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener
	 * (org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/**
	 * Change machine
	 * 
	 * @param id
	 *            Machine ID
	 */
	public void changeMachine(final String id) {
		IPMachine machine = ((IMachineManager) manager).findMachineById(id);
		changeMachineRefresh(machine);
	}

	public void changeMachineRefresh(final IPMachine machine) {
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
			public void run() {
				if (!elementViewComposite.isDisposed()) {
					changeMachine(machine);
					machineTableViewer.refresh(true);
					machineTableViewer.setSelection(machine == null ? new StructuredSelection() : new StructuredSelection(machine),
							true);
					nodeAttrTableViewer.refresh();
					processTableViewer.refresh();
				}
			}
		});
	}

	/**
	 * Change view
	 * 
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
			// sashForm.setWeights(new int[] { 3, 1 });
		}
	}

	@Override
	public void dispose() {
		elementViewComposite.dispose();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.views.AbstractParallelElementView#doubleClick(org.
	 * eclipse.ptp.ui.model.IElement)
	 */
	@Override
	public void doubleClick(IElement element) {
		boolean isElementRegistered = element.isRegistered();

		/*
		 * Unregister any registered elements
		 */
		unregister();

		/*
		 * Register the new element only if we're not trying to register the
		 * same one.
		 */
		if (!isElementRegistered) {
			/*
			 * Now register the new element
			 */
			getCurrentElementHandler().addToRegister(new IElement[] { element });
		}

		/*
		 * Refresh the attribute and process panes.
		 */
		nodeAttrTableViewer.refresh();
		processTableViewer.refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getCurrentID()
	 */
	@Override
	public String getCurrentID() {
		IPMachine machine = getMachineManager().getCurrentMachine();
		if (machine != null) {
			return machine.getID();
		}
		return IElementManager.EMPTY_ID;
	}

	/**
	 * Get selected machine
	 * 
	 * @return selected machine
	 */
	public synchronized IPMachine getCurrentMachine() {
		return getMachineManager().getCurrentMachine();
	}

	/**
	 * Get current view flag
	 * 
	 * @return flag of view
	 */
	public String getCurrentView() {
		return current_view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.views.IContentProvider#getRulerIndex(java.lang.Object,
	 * int)
	 */
	@Override
	public String getRulerIndex(Object obj, int index) {
		if (obj instanceof IElement) {
			Object nodeObj = convertElementObject((IElement) obj);
			if (nodeObj instanceof IPNode) {
				return ((IPNode) nodeObj).getNodeNumber();
			}
		}
		return super.getRulerIndex(obj, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		if (selection == null) {
			return StructuredSelection.EMPTY;
		}
		return selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.views.AbstractParallelElementView#getToolTipText(java
	 * .lang.Object)
	 */
	@Override
	public String[] getToolTipText(Object obj) {
		IElementHandler setManager = getCurrentElementHandler();
		if (obj == null || !(obj instanceof IPNode) || setManager == null || cur_element_set == null)
			return IToolTipProvider.NO_TOOLTIP;

		IPNode node = (IPNode) obj;
		StringBuffer buffer = new StringBuffer();
		buffer.append(node.getName());
		IElementSet[] sets = setManager.getSetsWithElement(node.getID());
		if (sets.length > 1)
			buffer.append(Messages.ParallelMachinesView_0);
		for (int i = 1; i < sets.length; i++) {
			buffer.append(sets[i].getID());
			if (i < sets.length - 1)
				buffer.append(","); //$NON-NLS-1$
		}
		// buffer.append("\nStatus: " +
		// getMachineManager().getNodeStatusText(node));
		return new String[] { buffer.toString() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener
	 * (org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.views.AbstractParallelElementView#updateView(java.
	 * lang.Object)
	 */
	@Override
	public void repaint(boolean all) {
		if (all) {
			if (!machineTableViewer.getTable().isDisposed()) {
				machineTableViewer.refresh(true);
			}
		}
		update();
		// updateLowerRegions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(
	 * org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		// Selection change could come from either the machineTableViewer of the
		// elementViewComposite
		selection = event.getSelection();
		setSelection(selection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#setFocus()
	 */
	@Override
	public void setFocus() {
		super.setFocus();
		IPMachine machine = getCurrentMachine();
		if (machine == null) {
			changeMachine((String) null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse
	 * .jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		final SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
		Object[] array = listeners.getListeners();
		for (int i = 0; i < array.length; i++) {
			final ISelectionChangedListener l = (ISelectionChangedListener) array[i];
			SafeRunnable.run(new SafeRunnable() {
				public void run() {
					l.selectionChanged(e);
				}
			});
		}
	}

	/**
	 * Unregister all registered elements
	 */
	public void unregister() {
		IElementHandler elementHandler = getCurrentElementHandler();
		elementHandler.removeFromRegister(elementHandler.getRegistered());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelSetView#updateAction()
	 */
	@Override
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
	private IMachineManager getMachineManager() {
		return ((IMachineManager) manager);
	}

	private IPNode getRegisteredNode() {
		cur_selected_element_id = ""; //$NON-NLS-1$
		IElementHandler elementHandler = getCurrentElementHandler();
		if (elementHandler == null || cur_element_set == null || elementHandler.totalRegistered() == 0)
			return null;
		String firstRegisteredElementID = elementHandler.getRegistered()[0].getID();
		if (!cur_element_set.contains(firstRegisteredElementID))
			return null;
		cur_selected_element_id = firstRegisteredElementID;
		return getMachineManager().findNode(cur_selected_element_id);
	}

	/**
	 * Change machine
	 * 
	 * @param machine
	 */
	protected void changeMachine(final IPMachine machine) {
		selectMachine(machine);
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.views.AbstractParallelElementView#convertElementObject
	 * (org.eclipse.ptp.ui.model.IElement)
	 */
	@Override
	protected Object convertElementObject(IElement element) {
		if (element == null)
			return null;

		return getMachineManager().findNode(element.getID());
	}

	/**
	 * Create lower text region layout
	 * 
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
		nodeGroup.setText(Messages.ParallelMachinesView_1);

		Group procGroup = new Group(composite, SWT.BORDER);
		procGroup.setLayout(new FillLayout());
		GridData gdlist = new GridData(GridData.FILL_BOTH);
		gdlist.grabExcessVerticalSpace = true;
		gdlist.grabExcessHorizontalSpace = true;
		gdlist.horizontalAlignment = GridData.FILL;
		gdlist.verticalAlignment = GridData.FILL;
		procGroup.setLayoutData(gdlist);
		procGroup.setText(Messages.ParallelMachinesView_2);

		nodeAttrTableViewer = new TableViewer(nodeGroup, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		Table nodeAttrTable = nodeAttrTableViewer.getTable();
		TableColumn col = new TableColumn(nodeAttrTable, SWT.LEFT);
		col.setText(Messages.ParallelMachinesView_3);
		col.setWidth(80);
		col.setResizable(true);
		col = new TableColumn(nodeAttrTable, SWT.LEFT);
		col.setText(Messages.ParallelMachinesView_4);
		col.setWidth(200);
		col.setResizable(false);
		nodeAttrTable.setHeaderVisible(true);
		nodeAttrTable.setLinesVisible(true);
		nodeAttrTable.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				Table table = (Table) e.getSource();
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
			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				if (element instanceof IAttribute) {
					IAttribute<?, ?, ?> attr = (IAttribute<?, ?, ?>) element;
					switch (columnIndex) {
					case 0:
						return attr.getDefinition().getName();
					case 1:
						return attr.getValueAsString();
					default:
						return Messages.ParallelMachinesView_5 + columnIndex;
					}
				}
				return ""; //$NON-NLS-1$
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}
		});
		nodeAttrTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof IElementManager) {
					IPNode node = getRegisteredNode();
					if (node != null) {
						return node.getDisplayAttributes();
					}
				}
				return new Object[0];
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		nodeAttrTableViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object a1, Object a2) {
				final IAttribute<?, ?, ?> attr1 = (IAttribute<?, ?, ?>) a1;
				final IAttribute<?, ?, ?> attr2 = (IAttribute<?, ?, ?>) a2;
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
			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				Table table = (Table) e.getSource();
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
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				// FIXME PProcessUI goes away when we address UI scalability.
				// See Bug 311057
				openProcessViewer((PProcessUI) sel.getFirstElement());
			}
		});
		processTableViewer.setLabelProvider(new LabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof PProcessUI) {
					// FIXME PProcessUI goes away when we address UI
					// scalability. See Bug 311057
					PProcessUI process = (PProcessUI) element;
					return ParallelImages.procImages[process.getState().ordinal()][0];
				}
				return null;
			}

			@Override
			public String getText(Object element) {
				if (element instanceof PProcessUI) {
					// FIXME PProcessUI goes away when we address UI
					// scalability. See Bug 311057
					PProcessUI process = (PProcessUI) element;
					return process.getJob().getName() + ":" + process.getName(); //$NON-NLS-1$
				}
				return ""; //$NON-NLS-1$
			}
		});
		processTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof IElementManager) {
					IPNode node = getRegisteredNode();
					if (node != null) {
						// FIXME PProcessUI goes away when we address UI
						// scalability. See Bug 311057
						final List<PProcessUI> procUIs = new LinkedList<PProcessUI>();
						final Set<? extends IPJob> jobs = node.getJobs();
						for (IPJob job : jobs) {
							final BitSet procJobRanks = node.getJobProcessRanks(job);
							for (Integer procJobRank : new BitSetIterable(procJobRanks)) {
								procUIs.add(new PProcessUI(job, procJobRank));
							}
						}
						return procUIs.toArray(new PProcessUI[0]);
					}
				}
				return new Object[0];
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		processTableViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object p1, Object p2) {
				// FIXME PProcessUI goes away when we address UI scalability.
				// See Bug 311057
				PProcessUI proc1 = (PProcessUI) p1;
				PProcessUI proc2 = (PProcessUI) p2;
				String name1 = proc1.getJob().getName() + ":" + proc1.getName(); //$NON-NLS-1$
				String name2 = proc2.getJob().getName() + ":" + proc2.getName(); //$NON-NLS-1$
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
		MenuManager menuMgr = new MenuManager("#machinepopupmenu"); //$NON-NLS-1$
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.views.AbstractParallelSetView#createToolBarActions
	 * (org.eclipse.jface.action.IToolBarManager)
	 */
	@Override
	protected void createToolBarActions(IToolBarManager toolBarMgr) {
		super.buildInToolBarActions(toolBarMgr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.views.AbstractParallelElementView#createView(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
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
			@Override
			public Image getImage(Object element) {
				if (element instanceof IPMachine) {
					IPMachine machine = (IPMachine) element;
					EnumeratedAttribute<MachineAttributes.State> attr = machine.getAttribute(MachineAttributes
							.getStateAttributeDefinition());
					if (attr != null) {
						return ParallelImages.machineImages[attr.getValueIndex()];
					}
				}
				return null;
			}

			@Override
			public String getText(Object element) {
				if (element instanceof IPMachine) {
					return ((IPMachine) element).getName();
				}
				return ""; //$NON-NLS-1$
			}
		});
		machineTableViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof IMachineManager)
					return ((IMachineManager) inputElement).getMachines();
				return new Object[0];
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		machineTableViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object j1, Object j2) {
				return ((IPMachine) j1).getName().compareTo(((IPMachine) j2).getName());
			}
		});
		machineTableViewer.setInput(manager);
		machineTableViewer.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				ISelection selection = machineTableViewer.getSelection();
				TableItem item = machineTableViewer.getTable().getItem(new Point(e.x, e.y));
				if (item == null && !selection.isEmpty()) {
					machineTableViewer.getTable().deselectAll();
					changeMachineRefresh((IPMachine) null);
				} else if (item != null) {
					IPMachine machine = (IPMachine) item.getData();
					if (machine == null) {
						changeMachineRefresh((IPMachine) null);
					} else if (selection.isEmpty()) {
						changeMachineRefresh(machine);
					} else {
						String cur_id = getCurrentID();
						if (cur_id == null || !cur_id.equals(machine.getID())) {
							changeMachineRefresh(machine);
						}
					}
				}
			}
		});
		createMachineContextMenu();
		elementViewComposite = createElementView(upperSashForm);

		// ----------------------------------------------------------------------
		// Enable property sheet updates when table items are selected.
		// Note for this to work each item in the table must either implement
		// IPropertySource, or support IPropertySource.class as an adapter type
		// in its AdapterFactory.
		// ----------------------------------------------------------------------
		machineTableViewer.addSelectionChangedListener(this);
		getSite().setSelectionProvider(this);

		infoComposite = createLowerRegions(sashForm);
		changeView(current_view);

		/*
		 * Wait until the view has been created before registering for events
		 */
		IModelManager mm = PTPCorePlugin.getDefault().getModelManager();

		synchronized (mm) {
			/*
			 * Add us to any existing RM's. I guess it's possible we could miss
			 * a RM if a new event arrives while we're doing this, but is it a
			 * problem?
			 */
			for (IPResourceManager rm : mm.getUniverse().getResourceManagers()) {
				rm.addChildListener(resourceManagerListener);
			}

			mm.addListener(modelManagerListener);
		}
	}

	/**
	 * Fill the context menu
	 * 
	 * @param menuManager
	 */
	protected void fillMachineContextMenu(IMenuManager menuManager) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.views.AbstractParallelElementView#initialElement()
	 */
	@Override
	protected void initialElement() {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();

		/*
		 * Add us as a child listener to any existing machines
		 */
		for (IPResourceManager rm : universe.getResourceManagers()) {
			for (IPMachine machine : rm.getMachines()) {
				machine.addChildListener(machineListener);
			}
		}

		/*
		 * Select initial machine
		 */
		changeMachineRefresh((IPMachine) manager.initial(universe));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.views.AbstractParallelElementView#initialView()
	 */
	@Override
	protected void initialView() {
		initialElement();
		update();
	}

	/**
	 * Change machine
	 * 
	 * @param machine
	 */
	protected void selectMachine(IPMachine machine) {
		IPMachine old = getMachineManager().getCurrentMachine();
		if (old != null) {
			for (IPNode node : old.getNodes()) {
				node.removeChildListener(nodeListener);
			}
		}
		getMachineManager().setMachine(machine);
		if (machine != null) {
			for (IPNode node : machine.getNodes()) {
				node.addChildListener(nodeListener);
			}
		}
		updateMachineSet();
	}
}
