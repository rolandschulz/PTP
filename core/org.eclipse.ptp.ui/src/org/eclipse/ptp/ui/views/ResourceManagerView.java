package org.eclipse.ptp.ui.views;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.events.IMachineChangedEvent;
import org.eclipse.ptp.core.elements.events.IMachineChangedNodeEvent;
import org.eclipse.ptp.core.elements.events.IMachineNewNodeEvent;
import org.eclipse.ptp.core.elements.events.IMachineRemoveNodeEvent;
import org.eclipse.ptp.core.elements.events.IQueueChangedEvent;
import org.eclipse.ptp.core.elements.events.IQueueChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IQueueNewJobEvent;
import org.eclipse.ptp.core.elements.events.IQueueRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerChangedEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerNewMachineEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerNewQueueEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerRemoveQueueEvent;
import org.eclipse.ptp.core.elements.listeners.IMachineListener;
import org.eclipse.ptp.core.elements.listeners.IMachineNodeListener;
import org.eclipse.ptp.core.elements.listeners.IQueueJobListener;
import org.eclipse.ptp.core.elements.listeners.IQueueListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerQueueListener;
import org.eclipse.ptp.core.events.IModelManagerChangedResourceManagerEvent;
import org.eclipse.ptp.core.events.IModelManagerNewResourceManagerEvent;
import org.eclipse.ptp.core.events.IModelManagerRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerResourceManagerListener;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.actions.AddResourceManagerAction;
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

public class ResourceManagerView extends ViewPart implements
		IModelManagerResourceManagerListener, IResourceManagerListener {
	
	public class QueueListener implements IQueueListener, IQueueJobListener {

		public void handleEvent(IQueueChangedEvent e) {
			updateViewer(e.getSource());
		}

		public void handleEvent(IQueueChangedJobEvent e) {
			updateViewer(e.getJob());
		}

		public void handleEvent(IQueueNewJobEvent e) {
			refreshViewer(e.getSource());
		}

		public void handleEvent(IQueueRemoveJobEvent e) {
			refreshViewer(e.getSource());
		}

	}

	private final class MachineListener implements IMachineNodeListener, IMachineListener {
		public void handleEvent(IMachineChangedEvent e) {
			updateViewer(e.getSource());
		}

		public void handleEvent(IMachineChangedNodeEvent e) {
			updateViewer(e.getNode());
		}

		public void handleEvent(IMachineNewNodeEvent e) {
			refreshViewer(e.getSource());
		}

		public void handleEvent(IMachineRemoveNodeEvent e) {
			refreshViewer(e.getSource());
		}
	}

	private final class RMMachineListener implements
			IResourceManagerMachineListener {
		private final MachineListener machineListener = new MachineListener();
		private final Set<IPMachine> machines = new HashSet<IPMachine>();

		public synchronized void dispose() {
			for (IPMachine machine : machines) {
				machine.removeElementListener(machineListener);
				machine.removeChildListener(machineListener);
			}
			machines.clear();
		}

		public void handleEvent(IResourceManagerChangedMachineEvent e) {
			updateViewer(e.getSource());
		}

		public synchronized void handleEvent(IResourceManagerNewMachineEvent e) {
			final IPMachine machine = e.getMachine();
			machines.add(machine);
			machine.addElementListener(machineListener);
			machine.addChildListener(machineListener);
			refreshViewer(e.getSource());
		}
		
		public synchronized void handleEvent(IResourceManagerRemoveMachineEvent e) {
			final IPMachine machine = e.getMachine();
			machines.remove(machine);
			machine.removeElementListener(machineListener);
			machine.removeChildListener(machineListener);
			refreshViewer(e.getSource());
		}
	}

	private final class RMQueueListener implements
			IResourceManagerQueueListener {
		private QueueListener queueListener = new QueueListener();
		private final Set<IPQueue> queues = new HashSet<IPQueue>();

		public synchronized void dispose() {
			for (IPQueue queue : queues) {
				queue.removeElementListener(queueListener);
				queue.removeChildListener(queueListener);
			}
			queues.clear();
		}

		public void handleEvent(IResourceManagerChangedQueueEvent e) {
			updateViewer(e.getSource());
		}

		public synchronized void handleEvent(IResourceManagerNewQueueEvent e) {
			final IPQueue queue = e.getQueue();
			queues.add(queue);
			queue.addElementListener(queueListener);
			queue.addChildListener(queueListener);
			refreshViewer(e.getSource());
		}
		
		public synchronized void handleEvent(IResourceManagerRemoveQueueEvent e) {
			final IPQueue queue = e.getQueue();
			queues.remove(queue);
			queue.removeElementListener(queueListener);
			queue.removeChildListener(queueListener);
			refreshViewer(e.getSource());
		}
	}

	private final Set<IResourceManager> resourceManagers = new HashSet<IResourceManager>();

	private TreeViewer viewer;

	private RemoveResourceManagersAction removeResourceManagerAction;

	private AddResourceManagerAction addResourceManagerAction;

	private final RMMachineListener rmMachineListener = new RMMachineListener();

	private final RMQueueListener rmQueueListener = new RMQueueListener();

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

		PTPCorePlugin.getDefault().getModelManager().addListener(this);
	}

	public synchronized void dispose() {
		PTPCorePlugin.getDefault().getModelManager().removeListener(this);
		for (IResourceManager resourceManager : resourceManagers) {
			resourceManager.removeElementListener(this);
			resourceManager.removeChildListener(rmMachineListener);
			resourceManager.removeChildListener(rmQueueListener);
		}
		resourceManagers.clear();
		rmMachineListener.dispose();
		rmQueueListener.dispose();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.events.IModelManagerResourceManagerListener#handleEvent(org.eclipse.ptp.core.events.IModelManagerChangedResourceManagerEvent)
	 */
	public void handleEvent(IModelManagerChangedResourceManagerEvent e) {
        updateViewer(e.getResourceManager());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.events.IModelManagerResourceManagerListener#handleEvent(org.eclipse.ptp.core.events.IModelManagerNewResourceManagerEvent)
	 */
	public synchronized void handleEvent(IModelManagerNewResourceManagerEvent e) {
		final IResourceManager resourceManager = e.getResourceManager();
		resourceManagers.add(resourceManager);
		resourceManager.addElementListener(this);
		resourceManager.addChildListener(rmMachineListener);
		resourceManager.addChildListener(rmQueueListener);
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable(){
			public void run() {
				refreshViewer(PTPCorePlugin.getDefault().getUniverse());
			}});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.events.IModelManagerResourceManagerListener#handleEvent(org.eclipse.ptp.core.events.IModelManagerRemoveResourceManagerEvent)
	 */
	public synchronized void handleEvent(IModelManagerRemoveResourceManagerEvent e) {
		final IResourceManager resourceManager = e.getResourceManager();
		resourceManagers.remove(resourceManager);
		resourceManager.removeElementListener(this);
		resourceManager.removeChildListener(rmMachineListener);
		resourceManager.removeChildListener(rmQueueListener);
		UIUtils.safeRunAsyncInUIThread(new SafeRunnable(){
			public void run() {
				refreshViewer(PTPCorePlugin.getDefault().getUniverse());
			}});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedEvent)
	 */
	public void handleEvent(IResourceManagerChangedEvent e) {
		refreshViewer(e.getSource());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent)
	 */
	public void handleEvent(IResourceManagerErrorEvent e) {
		refreshViewer(e.getSource());
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void createContextMenu() {
		final Shell shell = getSite().getShell();
		addResourceManagerAction = new AddResourceManagerAction(shell);
		removeResourceManagerAction = new RemoveResourceManagersAction(shell);

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
		boolean inContextForRemoveRM = inContextForRM;
		for (int i = 0; i < selectedObjects.length; ++i) {
			if (!(selectedObjects[i] instanceof IResourceManagerMenuContribution)) {
				// Not all of the selected are RMs
				inContextForRM = false;
				inContextForRemoveRM = false;
				break;
			}
			else {
                final IResourceManagerMenuContribution menuContrib = (IResourceManagerMenuContribution) selectedObjects[i];
			    IResourceManagerControl rm = (IResourceManagerControl) menuContrib.getAdapter(IResourceManagerControl.class);
			    if (rm.getState() != ResourceManagerAttributes.State.STOPPED) {
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
