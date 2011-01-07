/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
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
import org.eclipse.ptp.core.elements.events.IResourceManagerSubmitJobErrorEvent;
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
import org.eclipse.ptp.core.rm.IRMChangeListener;
import org.eclipse.ptp.core.rm.IRMJobChangeEvent;
import org.eclipse.ptp.core.rm.IRMModelChangeEvent;
import org.eclipse.ptp.core.rm.IRMModelChangeListener;
import org.eclipse.ptp.core.rm.IRMSessionChangeEvent;
import org.eclipse.ptp.core.rm.IResourceManager;
import org.eclipse.ptp.core.rm.RMModelManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.actions.AddResourceManagerAction;
import org.eclipse.ptp.ui.actions.EditResourceManagerAction;
import org.eclipse.ptp.ui.actions.RemoveResourceManagersAction;
import org.eclipse.ptp.ui.actions.SelectDefaultResourceManagerAction;
import org.eclipse.ptp.ui.managers.RMManager;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

public class ResourceManagerView extends ViewPart {
	private final class MachineListener implements IMachineChildListener, IMachineListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IMachineChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IChangedNodeEvent)
		 */
		public void handleEvent(IChangedNodeEvent e) {
			updateViewer(e.getSource());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IMachineListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IMachineChangeEvent)
		 */
		public void handleEvent(IMachineChangeEvent e) {
			updateViewer(e.getSource());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IMachineChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.INewNodeEvent)
		 */
		public void handleEvent(INewNodeEvent e) {
			refreshViewer(e.getSource());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IMachineChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IRemoveNodeEvent)
		 */
		public void handleEvent(IRemoveNodeEvent e) {
			refreshViewer(e.getSource());
		}
	}

	private final class MMChildListener implements IModelManagerChildListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.events.IModelManagerResourceManagerListener#
		 * handleEvent(org.eclipse.ptp.core.events.IChangedResourceManagerEvent)
		 */
		public void handleEvent(IChangedResourceManagerEvent e) {
			updateViewer(e.getResourceManagers().toArray(new IPResourceManager[0]));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.events.IModelManagerResourceManagerListener#
		 * handleEvent(org.eclipse.ptp.core.events.INewResourceManagerEvent)
		 */
		public synchronized void handleEvent(INewResourceManagerEvent e) {
			final IPResourceManager resourceManager = e.getResourceManager();
			resourceManagers.add(resourceManager);
			resourceManager.addElementListener(rmListener);
			resourceManager.addChildListener(rmChildListener);
			refreshViewer(PTPCorePlugin.getDefault().getUniverse());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.events.IModelManagerResourceManagerListener#
		 * handleEvent(org.eclipse.ptp.core.events.IRemoveResourceManagerEvent)
		 */
		public synchronized void handleEvent(IRemoveResourceManagerEvent e) {
			final IPResourceManager resourceManager = e.getResourceManager();
			resourceManagers.remove(resourceManager);
			resourceManager.removeElementListener(rmListener);
			resourceManager.removeChildListener(rmChildListener);
			rmChildListener.removeListeners(resourceManager);
			refreshViewer(PTPCorePlugin.getDefault().getUniverse());
		}
	}

	private final class QueueListener implements IQueueListener, IQueueChildListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IChangedJobEvent)
		 */
		public void handleEvent(IChangedJobEvent e) {
			updateViewer(e.getSource());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.INewJobEvent)
		 */
		public void handleEvent(INewJobEvent e) {
			refreshViewer(e.getSource());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IQueueListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IQueueChangeEvent)
		 */
		public void handleEvent(IQueueChangeEvent e) {
			updateViewer(e.getSource());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IRemoveJobEvent)
		 */
		public void handleEvent(IRemoveJobEvent e) {
			refreshViewer(e.getSource());
		}

	}

	private class ResourceManagerLabelProvider extends WorkbenchLabelProvider {

		private final Font selectedFont;
		private final Font unSelectedFont;

		public ResourceManagerLabelProvider(Font font) {
			unSelectedFont = font;
			FontData fd = font.getFontData()[0];
			FontData selectedFontData = new FontData(fd.getName(), fd.getHeight(), SWT.BOLD);
			selectedFont = (Font) new LocalResourceManager(JFaceResources.getResources()).get(FontDescriptor
					.createFrom(selectedFontData));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ui.model.WorkbenchLabelProvider#getFont(java.lang.Object)
		 */
		@Override
		public Font getFont(Object element) {
			IPResourceManager rm = getResourceManager(element);
			RMManager rmManager = PTPUIPlugin.getDefault().getRMManager();
			if (rm != null && rmManager != null && rm.getUniqueName().equals(rmManager.getSelected())) {
				return selectedFont;
			}
			return unSelectedFont;
		}

		private IPResourceManager getResourceManager(Object parentElement) {
			IPResourceManager rm = null;
			if (parentElement instanceof IAdaptable) {
				rm = (IPResourceManager) ((IAdaptable) parentElement).getAdapter(IPResourceManager.class);
			}
			return rm;
		}
	}

	private class ResourceManagerSorter extends ViewerComparator {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface
		 * .viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			String name1 = null;
			String name2 = null;
			if (e1 instanceof IPElement) {
				name1 = ((IPElement) e1).getName();
			} else if (e1 instanceof IResourceManager) {
				name1 = ((IResourceManager) e1).getName();
			}
			if (e2 instanceof IPElement) {
				name2 = ((IPElement) e2).getName();
			} else if (e2 instanceof IResourceManager) {
				name2 = ((IResourceManager) e2).getName();
			}
			if (name1 != null && name2 != null) {
				return name1.compareTo(name2);
			}
			return super.compare(viewer, e1, e2);
		}

	}

	private final class RMChildListener implements IResourceManagerChildListener {
		private final MachineListener machineListener = new MachineListener();
		private final QueueListener queueListener = new QueueListener();
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #
		 * handleEvent(org.eclipse.ptp.core.elements.events.IChangedMachineEvent
		 * )
		 */
		public void handleEvent(IChangedMachineEvent e) {
			updateViewer(e.getSource());
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
			updateViewer(e.getSource());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.INewMachineEvent)
		 */
		public synchronized void handleEvent(INewMachineEvent e) {
			for (IPMachine machine : e.getMachines()) {
				machines.add(machine);
				addListeners(machine);
			}
			refreshViewer(e.getSource());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.INewQueueEvent)
		 */
		public void handleEvent(INewQueueEvent e) {
			for (IPQueue queue : e.getQueues()) {
				queues.add(queue);
				addListeners(queue);
			}
			refreshViewer(e.getSource());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #
		 * handleEvent(org.eclipse.ptp.core.elements.events.IRemoveMachineEvent)
		 */
		public synchronized void handleEvent(IRemoveMachineEvent e) {
			for (IPMachine machine : e.getMachines()) {
				machines.remove(machine);
				removeListeners(machine);
			}
			refreshViewer(e.getSource());
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
			for (IPQueue queue : e.getQueues()) {
				queues.remove(queue);
				removeListeners(queue);
			}
			refreshViewer(e.getSource());
		}

		/**
		 * @param resourceManager
		 */
		public synchronized void removeListeners(IPResourceManager resourceManager) {
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
		 * @param queue
		 */
		private void addListeners(final IPQueue queue) {
			queue.addElementListener(queueListener);
			queue.addChildListener(queueListener);
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
		private void removeListeners(IPQueue queue) {
			queue.removeElementListener(queueListener);
			queue.removeChildListener(queueListener);
		}
	}

	private final class RMListener implements IResourceManagerListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerListener#
		 * handleEvent
		 * (org.eclipse.ptp.core.elements.events.IResourceManagerChangedEvent)
		 */
		public void handleEvent(IResourceManagerChangeEvent e) {
			IPResourceManager rm = e.getSource();
			if (rmManager != null && rm.getState() == ResourceManagerAttributes.State.STOPPED
					&& rm.getUniqueName().equals(rmManager.getSelected())) {
				rmManager.fireSetDefaultRMEvent(null);
			}
			refreshViewer(rm);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerListener#
		 * handleEvent
		 * (org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent)
		 */
		public void handleEvent(final IResourceManagerErrorEvent e) {
			refreshViewer(e.getSource());
			UIUtils.safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					IStatus status = new Status(IStatus.ERROR, PTPUIPlugin.PLUGIN_ID, e.getMessage());
					ErrorDialog.openError(PTPUIPlugin.getDisplay().getActiveShell(), Messages.ResourceManagerView_0,
							NLS.bind(Messages.ResourceManagerView_1, e.getSource().getName()), status);
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerListener#
		 * handleEvent
		 * (org.eclipse.ptp.core.elements.events.IResourceManagerSubmitJobErrorEvent
		 * )
		 */
		public void handleEvent(IResourceManagerSubmitJobErrorEvent e) {
			// Handled by launch framework
		}
	}

	private final class RMChangeListener implements IRMChangeListener {

		public void jobStatusChange(IRMJobChangeEvent event) {
			// ignore
		}

		public void sessionStatusChange(IRMSessionChangeEvent event) {
			IResourceManager rm = event.getResourceManager();
			if (rmManager != null && rm.getSessionStatus() == IResourceManager.SessionStatus.STOPPED
					&& rm.getUniqueName().equals(rmManager.getSelected())) {
				rmManager.fireSetDefaultRMEvent(null);
			}
			refreshViewer(rm);
		}

	}

	private final class RMModelChangeListener implements IRMModelChangeListener {

		public void added(IRMModelChangeEvent event) {
			final IResourceManager rm = event.getResourceManager();
			rm.addListener(rmChangeListener);
			refreshViewer(PTPCorePlugin.getDefault().getUniverse());
		}

		public void changed(IRMModelChangeEvent event) {
			updateViewer(event.getResourceManager());
		}

		public void removed(IRMModelChangeEvent event) {
			final IResourceManager rm = event.getResourceManager();
			rm.removeListener(rmChangeListener);
			refreshViewer(PTPCorePlugin.getDefault().getUniverse());
		}

	}

	private TreeViewer viewer;
	private RemoveResourceManagersAction removeResourceManagerAction;
	private AddResourceManagerAction addResourceManagerAction;
	private EditResourceManagerAction editResourceManagerAction;
	private SelectDefaultResourceManagerAction selectResourceManagerAction;
	private final Set<IPResourceManager> resourceManagers = new HashSet<IPResourceManager>();
	private final MMChildListener mmChildListener = new MMChildListener();
	private final RMListener rmListener = new RMListener();
	private final RMChildListener rmChildListener = new RMChildListener();
	private final RMChangeListener rmChangeListener = new RMChangeListener();
	private final RMModelChangeListener rmModelChangeListener = new RMModelChangeListener();
	private final RMManager rmManager = PTPUIPlugin.getDefault().getRMManager();

	public ResourceManagerView() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI);
		viewer.setContentProvider(new WorkbenchContentProvider());
		viewer.setLabelProvider(new ResourceManagerLabelProvider(viewer.getTree().getFont()));
		viewer.setComparator(new ResourceManagerSorter());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (rmManager != null) {
					rmManager.fireSelectedEvent(event.getSelection());
				}
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ITreeSelection selection = (ITreeSelection) event.getSelection();
				if (!selection.isEmpty()) {
					if (selection.getFirstElement() instanceof IResourceManagerControl) {
						final IResourceManagerControl rm = (IResourceManagerControl) selection.getFirstElement();
						if (rm.getState() == ResourceManagerAttributes.State.STOPPED
								|| rm.getState() == ResourceManagerAttributes.State.ERROR) {
							IRunnableWithProgress runnable = new IRunnableWithProgress() {
								public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
									try {
										rm.startUp(monitor);
									} catch (CoreException e) {
										throw new InvocationTargetException(e);
									}
									if (monitor.isCanceled()) {
										throw new InterruptedException();
									}
								}
							};
							try {
								PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
							} catch (InvocationTargetException e) {
								Throwable t = e.getCause();
								IStatus status = null;
								if (t != null && t instanceof CoreException) {
									status = ((CoreException) t).getStatus();
								}
								UIUtils.showErrorDialog(Messages.ResourceManagerView_Startup,
										Messages.ResourceManagerView_FailedToStart, status);
							} catch (InterruptedException e) {
								// Do nothing. Operation has been canceled.
							}
							return;
						} else {
							boolean shutdown = true;
							if (rm.getState() == ResourceManagerAttributes.State.STARTED) {
								shutdown = MessageDialog.openConfirm(viewer.getControl().getShell(),
										Messages.ResourceManagerView_Shutdown,
										NLS.bind(Messages.ResourceManagerView_AreYouSure, rm.getName()));
							}
							if (shutdown) {
								try {
									rm.shutdown();
								} catch (CoreException e) {
									final String message = NLS.bind(Messages.ResourceManagerView_UnableToStop, rm.getName());
									Status status = new Status(Status.ERROR, PTPUIPlugin.PLUGIN_ID, 1, message, e);
									ErrorDialog dlg = new ErrorDialog(viewer.getControl().getShell(),
											Messages.ResourceManagerView_ErrorStopping, message, status, IStatus.ERROR);
									dlg.open();
									PTPUIPlugin.log(status);
								}
							}
						}
					}

					TreeItem item = viewer.getTree().getSelection()[0];
					item.setExpanded(!item.getExpanded());
					viewer.refresh(selection.getFirstElement());
				}
			}
		});
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

		IModelManager mm = PTPCorePlugin.getDefault().getModelManager();

		/*
		 * Add us to any existing RM's. I guess it's possible we could miss a RM
		 * if a new event arrives while we're doing this, but is it a problem?
		 */
		for (IPResourceManager rm : mm.getUniverse().getResourceManagers()) {
			rm.addElementListener(rmListener);
		}
		mm.addListener(mmChildListener);

		RMModelManager.getInstance().addListener(rmModelChangeListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public synchronized void dispose() {
		PTPCorePlugin.getDefault().getModelManager().removeListener(mmChildListener);
		for (IPResourceManager resourceManager : resourceManagers) {
			resourceManager.removeElementListener(rmListener);
			resourceManager.removeChildListener(rmChildListener);
		}
		resourceManagers.clear();
		rmChildListener.dispose();
		super.dispose();
	}

	public Font getFont() {
		if (viewer == null) {
			return null;
		}
		return viewer.getTree().getFont();
	}

	public void refreshViewer() {
		ISafeRunnable safeRunnable = new SafeRunnable() {
			public void run() throws Exception {
				viewer.refresh();
			}
		};
		UIUtils.safeRunAsyncInUIThread(safeRunnable);
	}

	private void refreshViewer(final IPElement element) {
		ISafeRunnable safeRunnable = new SafeRunnable() {
			public void run() throws Exception {
				viewer.refresh(element);
			}
		};
		UIUtils.safeRunAsyncInUIThread(safeRunnable);
	}

	private void refreshViewer(final IResourceManager rm) {
		ISafeRunnable safeRunnable = new SafeRunnable() {
			public void run() throws Exception {
				viewer.refresh(rm);
			}
		};
		UIUtils.safeRunAsyncInUIThread(safeRunnable);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void updateViewer(final IPElement element) {
		ISafeRunnable safeRunnable = new SafeRunnable() {
			public void run() throws Exception {
				viewer.update(element, null);
			}
		};
		UIUtils.safeRunAsyncInUIThread(safeRunnable);
	}

	private void updateViewer(final IPElement[] elements) {
		ISafeRunnable safeRunnable = new SafeRunnable() {
			public void run() throws Exception {
				viewer.update(elements, null);
			}
		};
		UIUtils.safeRunAsyncInUIThread(safeRunnable);
	}

	private void updateViewer(final IResourceManager rm) {
		ISafeRunnable safeRunnable = new SafeRunnable() {
			public void run() throws Exception {
				viewer.update(rm, null);
			}
		};
		UIUtils.safeRunAsyncInUIThread(safeRunnable);
	}

	private void createContextMenu() {
		final Shell shell = getSite().getShell();
		addResourceManagerAction = new AddResourceManagerAction(shell);
		removeResourceManagerAction = new RemoveResourceManagersAction(shell);
		editResourceManagerAction = new EditResourceManagerAction(shell);
		selectResourceManagerAction = new SelectDefaultResourceManagerAction(this);

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
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
		manager.add(new Separator());

		final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		manager.add(addResourceManagerAction);
		Object[] selectedObjects = selection.toArray();
		boolean inContextForRM = selection.size() > 0;
		boolean inContextForEditRM = inContextForRM;
		boolean inContextForRemoveRM = inContextForRM;
		boolean inContextForSelectRM = inContextForRM;
		for (int i = 0; i < selectedObjects.length; ++i) {
			if (!(selectedObjects[i] instanceof IResourceManagerMenuContribution)) {
				// Not all of the selected are RMs
				inContextForRM = false;
				inContextForEditRM = false;
				inContextForRemoveRM = false;
				inContextForSelectRM = false;
				break;
			} else {
				final IResourceManagerMenuContribution menuContrib = (IResourceManagerMenuContribution) selectedObjects[i];
				IResourceManagerControl rmc = (IResourceManagerControl) menuContrib.getAdapter(IResourceManagerControl.class);
				if (rmc != null) {
					if (rmc.getState() != ResourceManagerAttributes.State.STOPPED) {
						inContextForEditRM = false;
						inContextForRemoveRM = false;
					} else {
						inContextForSelectRM = false;
					}
				} else {
					IResourceManager rm = (IResourceManager) menuContrib.getAdapter(IResourceManager.class);
					if (rm != null) {
						if (rm.getSessionStatus() != IResourceManager.SessionStatus.STOPPED) {
							inContextForEditRM = false;
							inContextForRemoveRM = false;
						} else {
							inContextForSelectRM = false;
						}
					}
				}
			}
		}
		manager.add(removeResourceManagerAction);
		removeResourceManagerAction.setEnabled(inContextForRemoveRM);
		if (inContextForRemoveRM) {
			IResourceManagerConfiguration[] configs = new IResourceManagerConfiguration[selection.size()];
			for (int i = 0; i < configs.length; ++i) {
				final IResourceManagerMenuContribution menuContrib = (IResourceManagerMenuContribution) selectedObjects[i];
				configs[i] = (IResourceManagerConfiguration) menuContrib.getAdapter(IResourceManagerConfiguration.class);
			}
			removeResourceManagerAction.setResourceManagers(configs);
		}
		manager.add(editResourceManagerAction);
		editResourceManagerAction.setEnabled(inContextForEditRM);
		if (inContextForEditRM) {
			final IResourceManagerMenuContribution menuContrib = (IResourceManagerMenuContribution) selectedObjects[0];
			IResourceManagerConfiguration config = (IResourceManagerConfiguration) menuContrib
					.getAdapter(IResourceManagerConfiguration.class);
			if (config != null) {
				editResourceManagerAction.setResourceManager(config);
			}
		}
		manager.add(new Separator());
		manager.add(selectResourceManagerAction);
		selectResourceManagerAction.setEnabled(inContextForSelectRM);
		if (inContextForSelectRM) {
			final IResourceManagerMenuContribution menuContrib = (IResourceManagerMenuContribution) selectedObjects[0];
			IResourceManagerConfiguration config = (IResourceManagerConfiguration) menuContrib
					.getAdapter(IResourceManagerConfiguration.class);
			selectResourceManagerAction.setResourceManager(config.getUniqueName());
		}
	}

}
