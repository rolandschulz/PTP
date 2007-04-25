package org.eclipse.ptp.ui.views;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.rmsystem.AbstractResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerChangedListener;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerListener;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;
import org.eclipse.ptp.rmsystem.ResourceManagerState;
import org.eclipse.ptp.rmsystem.events.IResourceManagerAddedRemovedEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedJobsEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedMachinesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedNodesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedProcessesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerChangedQueuesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewJobsEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewMachinesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewNodesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewProcessesEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerNewQueuesEvent;
import org.eclipse.ptp.ui.UIMessage;
import org.eclipse.ptp.ui.actions.AddResourceManagerAction;
import org.eclipse.ptp.ui.actions.RemoveResourceManagersAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

public class ResourceManagerView extends ViewPart implements
		IResourceManagerChangedListener, IResourceManagerListener {

	public class ChildContainer {

		private final Object parent;

		private final String name;

		private final Object[] children;

		public ChildContainer(Object parent, String name, Object[] children) {
			this.parent = parent;
			this.name = name;
			this.children = children;
		}

		public Object[] getChildren() {
			return children;
		}

		public String getName() {
			return name;
		}

		public Object getParent() {
			return parent;
		}

		public boolean hasChildren() {
			return children != null && children.length > 0;
		}

	}

	public class TheContentProvider implements ITreeContentProvider {

		private final HashMap parents = new HashMap();

		public void dispose() {
			// no-op
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ChildContainer) {
				ChildContainer childContainer = (ChildContainer) parentElement;
				return childContainer.getChildren();
			}

			IResourceManager rm = null;
			if (parentElement instanceof IAdaptable) {
				rm = (IResourceManager) ((IAdaptable) parentElement).getAdapter(IResourceManager.class);
			}
			if (rm != null) {
//				if (!rm.getState().equals(ResourceManagerState.STARTED)) {
//					return new Object[0];
//				}
				IPMachine[] machines = rm.getMachines();
				IPQueue[] queues = rm.getQueues();
				return new Object[] {
						makeChildContainer(parentElement,
								UIMessage.getResourceString("ResourceManagerView.Machines"), machines), //$NON-NLS-1$
						makeChildContainer(parentElement,
								UIMessage.getResourceString("ResourceManagerView.Queues"), queues) }; //$NON-NLS-1$
			}
			return null;
		}

		public Object[] getElements(Object inputElement) {
			final IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();

			final IResourceManager[] resourceManagers = universe.getResourceManagers();
			return resourceManagers;
		}

		public Object getParent(Object element) {
			if (element instanceof ChildContainer) {
				ChildContainer childContainer = (ChildContainer) element;
				return childContainer.getParent();
			}
			return parents.get(element);
		}

		public boolean hasChildren(Object element) {
			if (element instanceof ChildContainer) {
				return ((ChildContainer) element).hasChildren();
			}
			if (element instanceof IAdaptable) {
				IResourceManager rm = (IResourceManager) ((IAdaptable) element).getAdapter(IResourceManager.class);
				if (rm != null)
					return rm.getState().equals(ResourceManagerState.State.STARTED);
			}
			return false;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			System.out.println("inputChanged: viewer = " + viewer); //$NON-NLS-1$

			parents.clear();

			HashSet oldRMs = new HashSet();
			HashSet newRMs = new HashSet();

			if (oldInput != null) {
				IResourceManager[] oldManagers = (IResourceManager[]) oldInput;
				oldRMs.addAll(Arrays.asList(oldManagers));
			}

			if (newInput != null) {
				IResourceManager[] newManagers = (IResourceManager[]) newInput;
				newRMs.addAll(Arrays.asList(newManagers));
			}

			// We only want to add/remove listenership to added or removed
			// resource managers, not unaffected ones.
			HashSet unaffectedRMs = new HashSet(oldRMs);
			unaffectedRMs.retainAll(newRMs);

			newRMs.removeAll(unaffectedRMs);
			oldRMs.removeAll(unaffectedRMs);

			for (Iterator oit = oldRMs.iterator(); oit.hasNext();) {
				IResourceManager rm = (IResourceManager) oit.next();
				rm.removeResourceManagerListener(ResourceManagerView.this);
			}
			for (Iterator nit = newRMs.iterator(); nit.hasNext();) {
				IResourceManager rm = (IResourceManager) nit.next();
				rm.addResourceManagerListener(ResourceManagerView.this);
			}
		}

		private ChildContainer makeChildContainer(Object parent, String name,
				final Object[] children) {
			final ChildContainer container = new ChildContainer(parent, name,
					children);
			for (int i = 0; i < children.length; ++i) {
				parents.put(children[i], container);
			}
			return container;
		}

	}

	public class TheLabelProvider extends LabelProvider {

		public void dispose() {
			super.dispose();
		}

		public Image getImage(Object element) {
			if (element instanceof IResourceManager) {
				ResourceManagerState.State status = ((IResourceManager) element).getState();
                if (status.equals(ResourceManagerState.State.STARTED))
                    return ParallelImages.rmImages[1];
                if (status.equals(ResourceManagerState.State.SUSPENDED))
                    return ParallelImages.rmImages[1];
				if (status.equals(ResourceManagerState.State.STOPPED))
					return ParallelImages.rmImages[0];
				if (status.equals(ResourceManagerState.State.ERROR))
					return ParallelImages.rmImages[3];
			}
			return super.getImage(element);
		}

		public String getText(Object element) {
			if (element instanceof IResourceManager) {
				final IResourceManager resourceManager = (IResourceManager) element;
				final IModelManager modelManager = PTPCorePlugin.getDefault().getModelManager();
				final String resourceManagerId = resourceManager.getIDString();
				if (resourceManagerId == null)
					return resourceManager.getName();
				IResourceManagerFactory factory = modelManager.getResourceManagerFactory(
						resourceManagerId);
				return resourceManager.getName() + " (" + factory.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (element instanceof ChildContainer) {
				return ((ChildContainer) element).getName();
			}
			if (element instanceof IPElement) {
				IPElement ipelement = (IPElement) element;
				return ipelement.getElementName();
			}
			return super.getText(element);
		}

		public boolean isLabelProperty(Object element, String property) {
			return super.isLabelProperty(element, property);
		}

	}

	private TreeViewer viewer;

	private RemoveResourceManagersAction removeResourceManagerAction;

	private AddResourceManagerAction addResourceManagerAction;

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI);
		viewer.setContentProvider(new TheContentProvider());
		viewer.setLabelProvider(new TheLabelProvider());

		viewer.setInput(PTPCorePlugin.getDefault().getUniverse().getResourceManagers());

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

		PTPCorePlugin.getDefault().getModelManager().addResourceManagerChangedListener(this);
	}

	public void dispose() {
		PTPCorePlugin.getDefault().getModelManager().removeResourceManagerChangedListener(this);
		super.dispose();
	}

	public void handleChangedJobsEvent(IResourceManagerChangedJobsEvent e) {
		// This does not display these items
	}

	public void handleChangedMachinesEvent(IResourceManagerChangedMachinesEvent e) {
		updateViewer(e.getSource());
	}

	public void handleChangedNodesEvent(IResourceManagerChangedNodesEvent e) {
		// This does not display these items
	}

	public void handleChangedProcessesEvent(IResourceManagerChangedProcessesEvent e) {
		// This does not display these items
	}

	public void handleChangedQueuesEvent(IResourceManagerChangedQueuesEvent e) {
		updateViewer(e.getSource());
	}

	public void handleErrorStateEvent(IResourceManagerErrorEvent e) {
		updateViewer(e.getSource());        
	}

	public void handleNewJobsEvent(IResourceManagerNewJobsEvent e) {
		// This does not display these items
	}

    public void handleNewMachinesEvent(IResourceManagerNewMachinesEvent e) {
		refreshViewer(e.getSource());
	}

    public void handleNewNodesEvent(IResourceManagerNewNodesEvent e) {
		// This does not display these items
	}

	public void handleNewProcessesEvent(IResourceManagerNewProcessesEvent e) {
		// This does not display these items
	}

	public void handleNewQueuesEvent(IResourceManagerNewQueuesEvent e) {
		refreshViewer(e.getSource());
	}

	public void handleResourceManagersAddedRemoved(IResourceManagerAddedRemovedEvent event) {
		Display display = viewer.getControl().getDisplay();
		display.asyncExec(new Runnable(){
			public void run() {
				// Let the content provider register and unregister
				// to the added and removed resource managers
				viewer.setInput(PTPCorePlugin.getDefault().getUniverse().getResourceManagers());
			}});
	}

	public void handleShutdownStateEvent(IResourceManager resourceManager) {
		refreshViewer(resourceManager);
	}

	public void handleStartupStateEvent(IResourceManager resourceManager) {
		refreshViewer(resourceManager);
	}

	public void handleSuspendedStateEvent(AbstractResourceManager manager) {
        updateViewer(manager);
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
		boolean inContext = selection.size() > 0;
		for (int i = 0; i < selectedObjects.length; ++i) {
			if (!(selectedObjects[i] instanceof IResourceManagerMenuContribution)) {
				// Not all of the selected can be removed;
				inContext = false;
			}
		}
		manager.add(removeResourceManagerAction);
		removeResourceManagerAction.setEnabled(inContext);
		if (inContext) {
			IResourceManager[] rmManagers = new IResourceManager[selection.size()];
			for (int i = 0; i < rmManagers.length; ++i) {
				final IResourceManagerMenuContribution menuContrib = (IResourceManagerMenuContribution) selectedObjects[i];
				rmManagers[i] = (IResourceManager) menuContrib.getAdapter(IResourceManager.class);
			}
			removeResourceManagerAction.setResourceManager(rmManagers);
		}

		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
				+ "-end")); //$NON-NLS-1$
	}

	private void refreshViewer(final IResourceManager resourceManager) {
		Display display = viewer.getControl().getDisplay();
		display.asyncExec(new Runnable(){
			public void run() {
				viewer.refresh(resourceManager);
			}});
	}

	private void updateViewer(final IResourceManager resourceManager) {
		Display display = viewer.getControl().getDisplay();
		display.asyncExec(new Runnable(){
			public void run() {
				viewer.update(resourceManager, null);
			}});
	}

}
