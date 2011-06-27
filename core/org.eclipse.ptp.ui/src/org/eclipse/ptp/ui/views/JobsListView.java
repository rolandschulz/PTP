/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.events.IChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.events.IJobChangedEvent;
import org.eclipse.ptp.core.events.IResourceManagerAddedEvent;
import org.eclipse.ptp.core.events.IResourceManagerChangedEvent;
import org.eclipse.ptp.core.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.events.IResourceManagerRemovedEvent;
import org.eclipse.ptp.core.listeners.IJobListener;
import org.eclipse.ptp.core.listeners.IResourceManagerListener;
import org.eclipse.ptp.internal.ui.actions.TerminateJobFromListAction;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.IRMSelectionListener;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.part.ViewPart;

public class JobsListView extends ViewPart {
	private final class JobListener implements IJobListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.listeners.IJobListener#handleEvent(org.eclipse
		 * .ptp.core.events.IJobChangeEvent)
		 */
		public void handleEvent(IJobChangedEvent e) {
			IPResourceManager rm = (IPResourceManager) e.getSource().getAdapter(IPResourceManager.class);
			if (rm != null) {
				update(rm.getJobs());
			}

			// Refresh the terminate job button
			// TODO: scalability bottleneck
			PTPUIPlugin.getDisplay().syncExec(new Runnable() {
				public void run() {
					terminateAllAction.updateTerminateJobState();
				}
			});

		}

	}

	private final class ResourceManagerListener implements IResourceManagerListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.listeners.IModelManagerListener#handleEvent
		 * (org.eclipse.ptp.core.events.IResourceManagerAddedEvent)
		 */
		public void handleEvent(IResourceManagerAddedEvent e) {
			/*
			 * Add resource manager child listener so we get notified when new
			 * machines are added to the model.
			 */
			final IPResourceManager rm = (IPResourceManager) e.getResourceManager().getAdapter(IPResourceManager.class);
			rm.addChildListener(resourceManagerChildListener);
			rm.getResourceManager().addJobListener(jobListener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.listeners.IResourceManagerListener#handleEvent
		 * (org.eclipse.ptp.core.events.IResourceManagerChangedEvent)
		 */
		public void handleEvent(IResourceManagerChangedEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.listeners.IResourceManagerListener#handleEvent
		 * (org.eclipse.ptp.core.events.IResourceManagerErrorEvent)
		 */
		public void handleEvent(IResourceManagerErrorEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.listeners.IModelManagerListener#handleEvent
		 * (org.eclipse.ptp.core.events.IResourceManagerRemovedEvent)
		 */
		// Update the button here.
		public void handleEvent(IResourceManagerRemovedEvent e) {
			/*
			 * Removed resource manager child listener when resource manager is
			 * removed.
			 */
			final IPResourceManager rm = (IPResourceManager) e.getResourceManager().getAdapter(IPResourceManager.class);
			rm.removeChildListener(resourceManagerChildListener);
			rm.getResourceManager().removeJobListener(jobListener);
		}
	}

	private final class RMChildListener implements IResourceManagerChildListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.
		 * IResourceManagerChangedMachineEvent)
		 */
		public void handleEvent(IChangedMachineEvent e) {
			// Don't need to do anything
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
		public void handleEvent(final INewJobEvent e) {
			if (fColumnsNeedUpdating) {
				PTPUIPlugin.getDisplay().syncExec(new Runnable() {
					public void run() {
						addColumns(viewer, e.getJobs().iterator().next());
					}
				});
			}
			refresh();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.
		 * IResourceManagerNewMachineEvent)
		 */
		public void handleEvent(INewMachineEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.INewQueueEvent)
		 */
		public void handleEvent(INewQueueEvent e) {
			// Don't need to do anything
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.IRemoveJobEvent)
		 */
		public void handleEvent(IRemoveJobEvent e) {
			refresh();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.
		 * IResourceManagerRemoveMachineEvent)
		 */
		public void handleEvent(IRemoveMachineEvent e) {
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
			// Don't need to do anything
		}
	}

	private TableViewer viewer;
	private TerminateJobFromListAction terminateAllAction;
	private IPResourceManager fSelectedRM = null;
	private IPQueue fSelectedQueue = null;
	private boolean fColumnsNeedUpdating = false;

	/*
	 * Model listeners
	 */
	private final IResourceManagerListener resourceManagerListener = new ResourceManagerListener();
	private final IResourceManagerChildListener resourceManagerChildListener = new RMChildListener();
	private final IJobListener jobListener = new JobListener();

	private final Set<IAttributeDefinition<?, ?, ?>> colDefs = Collections
			.synchronizedSet(new HashSet<IAttributeDefinition<?, ?, ?>>());

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);

		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				/*
				 * Just get jobs from the queue if one is selected
				 */
				if (fSelectedQueue != null) {
					return fSelectedQueue.getJobs();
				}
				/*
				 * Otherwise get jobs from all queues the RM knows about
				 */
				if (fSelectedRM != null) {
					return getAllJobs(fSelectedRM).toArray(new IPJob[0]);
				}
				/*
				 * Otherwise get all jobs from all queues TODO: should probably
				 * not do this!
				 */
				Set<IPJob> jobs = new HashSet<IPJob>();
				for (IPResourceManager rm : PTPCorePlugin.getDefault().getModelManager().getUniverse().getResourceManagers()) {
					jobs.addAll(getAllJobs(rm));
				}
				return jobs.toArray(new IPJob[0]);
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		viewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object j1, Object j2) {
				return ((IPJob) j1).getName().compareTo(((IPJob) j2).getName());
			}
		});

		createColumns(viewer);

		getSite().setSelectionProvider(viewer);

		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);
		getSite().registerContextMenu(contextMenu, viewer);
		Control control = viewer.getControl();
		Menu menu = contextMenu.createContextMenu(control);
		control.setMenu(menu);

		// Use view toolbar
		IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();

		terminateAllAction = new TerminateJobFromListAction(this);
		toolBarMgr.add(new Separator(IPTPUIConstants.IUIACTIONGROUP));
		toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP, terminateAllAction);

		/*
		 * terminateAllAction = new TerminateJobAction(viewer);
		 * toolBarMgr.appendToGroup(IPTPUIConstants.IUIACTIONGROUP,
		 * terminateAllAction);
		 */

		IModelManager mm = PTPCorePlugin.getDefault().getModelManager();
		viewer.setInput(mm.getUniverse());

		/*
		 * Add us to any existing RM's. I guess it's possible we could miss a RM
		 * if a new event arrives while we're doing this, but is it a problem?
		 */
		for (IPResourceManager rm : mm.getUniverse().getResourceManagers()) {
			rm.addChildListener(resourceManagerChildListener);
			rm.getResourceManager().addJobListener(jobListener);
		}
		mm.addListener(resourceManagerListener);

		/*
		 * Link this view to the ResourceManagerView
		 */
		PTPUIPlugin.getDefault().getRMManager().addRMSelectionListener(new IRMSelectionListener() {
			public void selectionChanged(ISelection selection) {
				IPResourceManager oldRM = fSelectedRM;
				fSelectedQueue = null;
				if (selection.isEmpty()) {
					fSelectedRM = null;
				} else {
					TreePath path = ((ITreeSelection) selection).getPaths()[0];
					Object segment = path.getFirstSegment();
					if (segment instanceof IPResourceManager) {
						fSelectedRM = (IPResourceManager) segment;
						if (path.getLastSegment() instanceof IPQueue) {
							fSelectedQueue = (IPQueue) path.getLastSegment();
						}
					}
				}
				if (oldRM != fSelectedRM) {
					createColumns(viewer);
				}
				refresh();
			}

			public void setDefault(Object rm) {
				// Ignore
			}
		});
	}

	public TableViewer getViewer() {
		return viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}

	/**
	 * @param tableViewer
	 * @param fontMetrics
	 * @param attrDef
	 */
	private void addColumn(final IAttributeDefinition<?, ?, ?> attrDef, boolean resizable) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);

		column.setLabelProvider(new ColumnLabelProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang
			 * .Object)
			 */
			@Override
			public Image getImage(Object element) {
				/*
				 * If this is the state column, get the image, otherwise return
				 * null and just use the text.
				 */
				if (attrDef == JobAttributes.getStateAttributeDefinition()) {
					IWorkbenchAdapter adapter = (IWorkbenchAdapter) Platform.getAdapterManager().getAdapter(element,
							IWorkbenchAdapter.class);
					if (adapter != null) {
						return adapter.getImageDescriptor(element).createImage();
					}
				}
				return null;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang
			 * .Object)
			 */
			@Override
			public String getText(Object element) {
				/*
				 * If this is not the state column, then use the attribute text.
				 * If it is the state column, just return null an use the icon.
				 */
				IPJob job = (IPJob) element;
				if (attrDef != JobAttributes.getStateAttributeDefinition()) {
					IAttribute<?, ?, ?> attr = job.getAttribute(attrDef.getId());
					if (attr != null) {
						return attr.getValueAsString();
					}
				}
				return null;
			}

		});
		column.getColumn().setResizable(resizable);
		column.getColumn().setMoveable(true);
		// this will need to sort by the col
		// column.getColumn().addSelectionListener(getHeaderListener());
		String name = attrDef.getName();
		column.getColumn().setText(name);
		PixelConverter converter = new PixelConverter(viewer.getControl());
		int colWidth = converter.convertWidthInCharsToPixels(resizable ? name.length() + 5 : name.length());
		column.getColumn().setWidth(colWidth);
		colDefs.add(attrDef);
	}

	/**
	 * @param tableViewer
	 * @param job
	 */
	private void addColumns(TableViewer tableViewer, IPJob job) {
		if (job != null) {
			for (IAttribute<?, ?, ?> attr : job.getAttributes()) {
				IAttributeDefinition<?, ?, ?> attrDef = attr.getDefinition();
				if (!colDefs.contains(attrDef) && attrDef.getDisplay()) {
					addColumn(attrDef, true);
				}
			}
		}
	}

	/**
	 * Create the columns for the table. This is done whenever the selection in
	 * the RM view changes.
	 * 
	 * 1. Always create the state and name columns. 2. Assume that job
	 * attributes from a single RM are always constant, so only look at the
	 * first job from any queue. 3. Do this for each RM (if no RM selected)
	 * 
	 * @param tableViewer
	 */
	private void createColumns(TableViewer tableViewer) {
		for (TableColumn column : tableViewer.getTable().getColumns()) {
			column.dispose();
		}
		colDefs.clear();

		addColumn(JobAttributes.getStateAttributeDefinition(), false);
		addColumn(ElementAttributes.getNameAttributeDefinition(), true);

		IPJob[] jobs = getFirstJobs();

		if (jobs.length > 0) {
			for (IPJob job : jobs) {
				addColumns(tableViewer, job);
			}
			fColumnsNeedUpdating = false;
		} else {
			fColumnsNeedUpdating = true;
		}
	}

	/**
	 * Find all jobs from all queues belonging to a RM
	 * 
	 * @param rm
	 * @return set of jobs
	 */
	private Set<IPJob> getAllJobs(IPResourceManager rm) {
		Set<IPJob> jobList = new HashSet<IPJob>();
		for (IPQueue queue : rm.getQueues()) {
			for (IPJob job : queue.getJobs()) {
				jobList.add(job);
			}
		}
		return jobList;
	}

	/**
	 * Get the first job from the queue
	 * 
	 * @param queue
	 * @return first job in the queue or null if there are no jobs
	 */
	private IPJob getFirstJob(IPQueue queue) {
		IPJob[] jobs = queue.getJobs();
		if (jobs.length > 0) {
			return jobs[0];
		}
		return null;
	}

	/**
	 * Finds the first few jobs to use to create the table columns If an RM
	 * queue is selected, just return the first job If an RM is selected, return
	 * the first job from one of the queues If no RM is selected, return the
	 * first job from one of the queues in each RM
	 * 
	 * @return first job or null if there are no jobs
	 */
	private IPJob[] getFirstJobs() {
		List<IPJob> jobsList = new ArrayList<IPJob>();

		if (fSelectedQueue != null) {
			IPJob job = getFirstJob(fSelectedQueue);
			if (job != null) {
				jobsList.add(job);
			}
		} else if (fSelectedRM != null) {
			for (IPQueue queue : fSelectedRM.getQueues()) {
				IPJob job = getFirstJob(queue);
				if (job != null) {
					jobsList.add(job);
					break;
				}
			}
		} else {
			for (IPResourceManager rm : PTPCorePlugin.getDefault().getModelManager().getUniverse().getResourceManagers()) {
				for (IPQueue queue : rm.getQueues()) {
					IPJob job = getFirstJob(queue);
					if (job != null) {
						jobsList.add(job);
						break;
					}
				}
			}
		}
		return jobsList.toArray(new IPJob[0]);
	}

	/**
	 * Refresh the viewer from the model.
	 */
	private void refresh() {
		if (!viewer.getTable().isDisposed()) {
			PTPUIPlugin.getDisplay().asyncExec(new Runnable() {
				public void run() {
					viewer.refresh();
				}
			});
		}
	}

	/**
	 * Update the viewer if the supplied jobs have changed
	 * 
	 * @param jobs
	 *            array of jobs that have changed
	 */
	private void update(final IPJob[] jobs) {
		if (!viewer.getTable().isDisposed()) {
			PTPUIPlugin.getDisplay().asyncExec(new Runnable() {
				public void run() {
					viewer.update(jobs, null);
				}
			});
		}
	}

}
