package org.eclipse.ptp.ui.views;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.events.IChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IChangedNodeEvent;
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.IMachineChangeEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewNodeEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IQueueChangeEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveNodeEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerChangeEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.elements.listeners.IMachineChildListener;
import org.eclipse.ptp.core.elements.listeners.IMachineListener;
import org.eclipse.ptp.core.elements.listeners.IQueueChildListener;
import org.eclipse.ptp.core.elements.listeners.IQueueListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerListener;
import org.eclipse.ptp.core.events.IChangedResourceManagerEvent;
import org.eclipse.ptp.core.events.INewResourceManagerEvent;
import org.eclipse.ptp.core.events.IRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerChildListener;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.actions.AddResourceManagerAction;
import org.eclipse.ptp.ui.actions.EditResourceManagerAction;
import org.eclipse.ptp.ui.actions.RemoveResourceManagersAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

public class ResourceManagerView extends ViewPart {
	
	private final class QueueListener implements IQueueListener, IQueueChildListener {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent(org.eclipse.ptp.core.elements.events.INewJobEvent)
		 */
		public void handleEvent(INewJobEvent e) {
			refreshViewer(e.getSource());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IChangedJobEvent)
		 */
		public void handleEvent(IChangedJobEvent e) {
			updateViewer(e.getSource());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IQueueListener#handleEvent(org.eclipse.ptp.core.elements.events.IQueueChangeEvent)
		 */
		public void handleEvent(IQueueChangeEvent e) {
			updateViewer(e.getSource());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IRemoveJobEvent)
		 */
		public void handleEvent(IRemoveJobEvent e) {
			refreshViewer(e.getSource());
		}

	}

	private final class MachineListener implements IMachineChildListener, IMachineListener {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IMachineChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IChangedNodeEvent)
		 */
		public void handleEvent(IChangedNodeEvent e) {
			updateViewer(e.getSource());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IMachineChangeEvent)
		 */
		public void handleEvent(IMachineChangeEvent e) {
			updateViewer(e.getSource());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IMachineChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IRemoveNodeEvent)
		 */
		public void handleEvent(IRemoveNodeEvent e) {
			refreshViewer(e.getSource());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IMachineChildListener#handleEvent(org.eclipse.ptp.core.elements.events.INewNodeEvent)
		 */
		public void handleEvent(INewNodeEvent e) {
			refreshViewer(e.getSource());
		}
	}

	private final class RMChildListener implements
			IResourceManagerChildListener {
		private final MachineListener machineListener = new MachineListener();
		private QueueListener queueListener = new QueueListener();		
		private final Set<IPMachine> machines = new HashSet<IPMachine>();
		private final Set<IPQueue> queues = new HashSet<IPQueue>();

		public synchronized void dispose() {
			for (IPMachine machine : machines) {
				removeListeners(machine);
			}
			machines.clear();
			
			for (IPQueue queue : queues) {
				removeListeners(queue);
			}
			queues.clear();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.INewMachineEvent)
		 */
		public synchronized void handleEvent(INewMachineEvent e) {
			for (IPMachine machine : e.getMachines()) {
				machines.add(machine);
				addListeners(machine);
			}
			refreshViewer(e.getSource());
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.INewQueueEvent)
		 */
		public void handleEvent(INewQueueEvent e) {
			for (IPQueue queue : e.getQueues()) {
				queues.add(queue);
				addListeners(queue);
			}
			refreshViewer(e.getSource());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IChangedMachineEvent)
		 */
		public void handleEvent(IChangedMachineEvent e) {
			updateViewer(e.getSource());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedQueueEvent)
		 */
		public void handleEvent(IChangedQueueEvent e) {
			updateViewer(e.getSource());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IRemoveMachineEvent)
		 */
		public synchronized void handleEvent(IRemoveMachineEvent e) {
			for (IPMachine machine : e.getMachines()) {
				machines.remove(machine);
				removeListeners(machine);
			}
			refreshViewer(e.getSource());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerRemoveQueueEvent)
		 */
		public void handleEvent(IRemoveQueueEvent e) {
			for (IPQueue queue : e.getQueues()) {
				queues.remove(queue);
				removeListeners(queue);
			}
			refreshViewer(e.getSource());
		}

		/**
		 * @param resourceManager
		 */
		public synchronized void removeListeners(IResourceManager resourceManager) {
			List<IPMachine> removeMachines = Arrays.asList(resourceManager.getMachines());
			removeMachines.retainAll(machines);
			for (IPMachine machine : removeMachines) {
				removeListeners(machine);
			}
			machines.removeAll(removeMachines);
			
			List<IPQueue> removeQueues = Arrays.asList(resourceManager.getQueues());
			removeQueues.retainAll(queues);
			for (IPQueue queue : removeQueues) {
				removeListeners(queue);
			}
			queues.removeAll(removeQueues);
		}
		
		/**
		 * @param machine
		 */
		private void addListeners(final IPMachine machine) {
			machine.addElementListener(machineListener);
			machine.addChildListener(machineListener);
		}

		/**
		 * @param machine
		 */
		private void removeListeners(final IPMachine machine) {
			machine.removeElementListener(machineListener);
			machine.removeChildListener(machineListener);
		}
		
		/**
		 * @param queue
		 */
		private void addListeners(final IPQueue queue) {
			queue.addElementListener(queueListener);
			queue.addChildListener(queueListener);
		}

		/**
		 * @param queue
		 */
		private void removeListeners(IPQueue queue) {
			queue.removeElementListener(queueListener);
			queue.removeChildListener(queueListener);
		}	
	}
	
	private final class RMListener implements IResourceManagerListener {

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedEvent)
		 */
		public void handleEvent(IResourceManagerChangeEvent e) {
			refreshViewer(e.getSource());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent)
		 */
		public void handleEvent(final IResourceManagerErrorEvent e) {
			refreshViewer(e.getSource());
			UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					IStatus status = new Status(IStatus.ERROR, PTPUIPlugin.PLUGIN_ID, e.getMessage());
					ErrorDialog.openError(PTPUIPlugin.getDisplay().getActiveShell(), "Resource Manager Error", "The \"" + e.getSource().getName() + "\" resource manager reported the following error", status);
				}
			});
		}
	}

	private final class MMChildListener implements IModelManagerChildListener {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.events.IModelManagerResourceManagerListener#handleEvent(org.eclipse.ptp.core.events.IChangedResourceManagerEvent)
		 */
		public void handleEvent(IChangedResourceManagerEvent e) {
			// No need to do anything
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.events.IModelManagerResourceManagerListener#handleEvent(org.eclipse.ptp.core.events.INewResourceManagerEvent)
		 */
		public synchronized void handleEvent(INewResourceManagerEvent e) {
			final IResourceManager resourceManager = e.getResourceManager();
			resourceManagers.add(resourceManager);
			resourceManager.addElementListener(rmListener);
			resourceManager.addChildListener(rmChildListener);
			UIUtils.safeRunAsyncInUIThread(new SafeRunnable(){
				public void run() {
					refreshViewer(PTPCorePlugin.getDefault().getUniverse());
				}});
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.events.IModelManagerResourceManagerListener#handleEvent(org.eclipse.ptp.core.events.IRemoveResourceManagerEvent)
		 */
		public synchronized void handleEvent(IRemoveResourceManagerEvent e) {
			final IResourceManager resourceManager = e.getResourceManager();
			resourceManagers.remove(resourceManager);
			resourceManager.removeElementListener(rmListener);
			resourceManager.removeChildListener(rmChildListener);
			rmChildListener.removeListeners(resourceManager);
			UIUtils.safeRunAsyncInUIThread(new SafeRunnable(){
				public void run() {
					refreshViewer(PTPCorePlugin.getDefault().getUniverse());
				}});
		}
	}

	private final Set<IResourceManager> resourceManagers = new HashSet<IResourceManager>();

	private TreeViewer viewer;

	private RemoveResourceManagersAction removeResourceManagerAction;
	private AddResourceManagerAction addResourceManagerAction;
	private EditResourceManagerAction editResourceManagerAction;

	private final MMChildListener mmChildListener = new MMChildListener();
	private final RMListener rmListener = new RMListener();
	private final RMChildListener rmChildListener = new RMChildListener();

	public ResourceManagerView() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI);
		viewer.setContentProvider(new WorkbenchContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());

		viewer.setInput(PTPCorePlugin.getDefault().getUniverse());

		// -----------------------------
		// Enable right-click popup menu
		// -----------------------------
		createContextMenu();

		// ----------------------------------------------------------------------
		// Enable property sheet updates when tree items are selected.
		// Note for this to work each item in the tree must either implement
		// IPropertySource, or support IPropertySource.class as an adapter type
		// in its AdapterFactory.
		// ----------------------------------------------------------------------
		getSite().setSelectionProvider(viewer);

		viewer.getTree().addMouseListener(new MouseAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				ITreeSelection selection = (ITreeSelection)viewer.getSelection();
				if (!selection.isEmpty()) {
					IResourceManagerControl rm = (IResourceManagerControl)selection.getFirstElement();
					if (rm.getState() == ResourceManagerAttributes.State.STOPPED) {
						editResourceManagerAction.setResourceManager(rm);
						editResourceManagerAction.run();
					}
				}
			}

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDown(MouseEvent e) {
				ISelection selection = viewer.getSelection();
				TreeItem item = viewer.getTree().getItem(new Point(e.x, e.y));
				if (item == null && !selection.isEmpty()) {
					viewer.getTree().deselectAll();
				}
				else if (item != null) {
				}
			}
		});

		IModelManager mm = PTPCorePlugin.getDefault().getModelManager();
		
	    /*
	     * Add us to any existing RM's. I guess it's possible we could
	     * miss a RM if a new event arrives while we're doing this, but is 
	     * it a problem?
	     */
	    for (IResourceManager rm : mm.getUniverse().getResourceManagers()) {
	        rm.addElementListener(rmListener);
	    }
		mm.addListener(mmChildListener);
	}

	public synchronized void dispose() {
		PTPCorePlugin.getDefault().getModelManager().removeListener(mmChildListener);
		for (IResourceManager resourceManager : resourceManagers) {
			resourceManager.removeElementListener(rmListener);
			resourceManager.removeChildListener(rmChildListener);
		}
		resourceManagers.clear();
		rmChildListener.dispose();
		super.dispose();
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void createContextMenu() {
		final Shell shell = getSite().getShell();
		addResourceManagerAction = new AddResourceManagerAction(shell);
		removeResourceManagerAction = new RemoveResourceManagersAction(shell);
		editResourceManagerAction = new EditResourceManagerAction(shell);

		MenuManager menuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});

		Menu menu = menuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, viewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		manager.add(addResourceManagerAction);
		Object[] selectedObjects = selection.toArray();
		boolean inContextForRM = selection.size() > 0;
		boolean inContextForEditRM = inContextForRM;
		boolean inContextForRemoveRM = inContextForRM;
		for (int i = 0; i < selectedObjects.length; ++i) {
			if (!(selectedObjects[i] instanceof IResourceManagerMenuContribution)) {
				// Not all of the selected are RMs
				inContextForRM = false;
				inContextForEditRM = false;
				inContextForRemoveRM = false;
				break;
			}
			else {
                final IResourceManagerMenuContribution menuContrib = (IResourceManagerMenuContribution) selectedObjects[i];
			    IResourceManagerControl rm = (IResourceManagerControl) menuContrib.getAdapter(IResourceManagerControl.class);
			    if (rm.getState() != ResourceManagerAttributes.State.STOPPED) {
			    	inContextForEditRM = false;
			        inContextForRemoveRM = false;
			    }
			}
		}
		manager.add(removeResourceManagerAction);
		removeResourceManagerAction.setEnabled(inContextForRemoveRM);
		if (inContextForRemoveRM) {
			IResourceManagerControl[] rmManagers = new IResourceManagerControl[selection.size()];
			for (int i = 0; i < rmManagers.length; ++i) {
				final IResourceManagerMenuContribution menuContrib = (IResourceManagerMenuContribution) selectedObjects[i];
				rmManagers[i] = (IResourceManagerControl) menuContrib.getAdapter(IResourceManagerControl.class);
			}
			removeResourceManagerAction.setResourceManager(rmManagers);
		}
		manager.add(editResourceManagerAction);
		editResourceManagerAction.setEnabled(inContextForEditRM);
		if (inContextForEditRM) {
			final IResourceManagerMenuContribution menuContrib = (IResourceManagerMenuContribution) selectedObjects[0];
			IResourceManagerControl rmManager = (IResourceManagerControl) menuContrib.getAdapter(IResourceManagerControl.class);
			editResourceManagerAction.setResourceManager(rmManager);
		}
		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
				+ "-end")); //$NON-NLS-1$
	}

	private void refreshViewer(final IPElement element) {
		ISafeRunnable safeRunnable = new SafeRunnable(){

			public void run() throws Exception {
				viewer.refresh(element);
			}
		};
		UIUtils.safeRunAsyncInUIThread(safeRunnable);
	}

	private void updateViewer(final IPElement element) {
		ISafeRunnable safeRunnable = new SafeRunnable(){

			public void run() throws Exception {
				viewer.update(element, null);
			}
		};
		UIUtils.safeRunAsyncInUIThread(safeRunnable);
	}

}
