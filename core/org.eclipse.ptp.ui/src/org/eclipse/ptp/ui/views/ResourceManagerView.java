package org.eclipse.ptp.ui.views;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPElement;
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
import org.eclipse.ptp.ui.actions.AddResourceManagerAction;
import org.eclipse.ptp.ui.actions.RemoveResourceManagersAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
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
			refreshViewer(e.getSource());
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
			updateViewer(e.getSource());
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

		public void handleEvent(IResourceManagerChangedMachineEvent e) {
			updateViewer(e.getSource());
		}

		public void handleEvent(IResourceManagerNewMachineEvent e) {
			e.getMachine().addElementListener(machineListener);
			e.getMachine().addChildListener(machineListener);
			refreshViewer(e.getSource());
		}

		public void handleEvent(IResourceManagerRemoveMachineEvent e) {
			e.getMachine().removeElementListener(machineListener);
			e.getMachine().removeChildListener(machineListener);
			refreshViewer(e.getSource());
		}
	}

	private final class RMQueueListener implements
			IResourceManagerQueueListener {
		private QueueListener queueListener = new QueueListener();

		public void handleEvent(IResourceManagerChangedQueueEvent e) {
			updateViewer(e.getSource());
		}

		public void handleEvent(IResourceManagerNewQueueEvent e) {
			e.getQueue().addElementListener(queueListener);
			e.getQueue().addChildListener(queueListener);
			refreshViewer(e.getSource());
		}

		public void handleEvent(IResourceManagerRemoveQueueEvent e) {
			e.getQueue().removeElementListener(queueListener);
			e.getQueue().removeChildListener(queueListener);
			refreshViewer(e.getSource());
		}
	}

	private TreeViewer viewer;

	private RemoveResourceManagersAction removeResourceManagerAction;

	private AddResourceManagerAction addResourceManagerAction;

	private final IResourceManagerMachineListener rmMachineListener = new RMMachineListener();

	private final IResourceManagerQueueListener rmQueueListener = new RMQueueListener();

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

		PTPCorePlugin.getDefault().getModelManager().addListener(this);
	}

	public void dispose() {
		PTPCorePlugin.getDefault().getModelManager().removeListener(this);
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
	public void handleEvent(IModelManagerNewResourceManagerEvent e) {
		e.getResourceManager().addElementListener(this);
		e.getResourceManager().addChildListener(rmMachineListener);
		e.getResourceManager().addChildListener(rmQueueListener);
		Display display = viewer.getControl().getDisplay();
		display.asyncExec(new Runnable(){
			public void run() {
				// Let the content provider register and unregister
				// to the added and removed resource managers
				viewer.setInput(PTPCorePlugin.getDefault().getUniverse());
			}});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.events.IModelManagerResourceManagerListener#handleEvent(org.eclipse.ptp.core.events.IModelManagerRemoveResourceManagerEvent)
	 */
	public void handleEvent(IModelManagerRemoveResourceManagerEvent e) {
		e.getResourceManager().removeElementListener(this);
		e.getResourceManager().removeChildListener(rmMachineListener);
		e.getResourceManager().removeChildListener(rmQueueListener);
		Display display = viewer.getControl().getDisplay();
		display.asyncExec(new Runnable(){
			public void run() {
				// Let the content provider register and unregister
				// to the added and removed resource managers
				viewer.setInput(PTPCorePlugin.getDefault().getUniverse());
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
		// TODO An error from the resource manager is a non-fatal error message generated
		// by the runtime. Should this be handled by an error dialog? is the
		// resource manager view the correct place?
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
		Display display = viewer.getControl().getDisplay();
		display.asyncExec(new Runnable(){
			public void run() {
				viewer.refresh(element);
			}});
	}

	private void updateViewer(final IPElement element) {
		Display display = viewer.getControl().getDisplay();
		display.asyncExec(new Runnable(){
			public void run() {
				viewer.update(element, null);
			}});
	}

}
