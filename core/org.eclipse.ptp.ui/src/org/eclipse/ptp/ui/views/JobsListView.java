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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.events.IChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;
import org.eclipse.ptp.core.elements.listeners.IQueueChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.events.IChangedResourceManagerEvent;
import org.eclipse.ptp.core.events.INewResourceManagerEvent;
import org.eclipse.ptp.core.events.IRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerChildListener;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.utils.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class JobsListView extends ViewPart {
	private final class MMChildListener implements IModelManagerChildListener {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent(org.eclipse.ptp.core.events.IChangedResourceManagerEvent)
		 */
		public void handleEvent(IChangedResourceManagerEvent e) {
			// Don't need to do anything
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent(org.eclipse.ptp.core.events.INewResourceManagerEvent)
		 */
		public void handleEvent(INewResourceManagerEvent e) {
			/*
			 * Add resource manager child listener so we get notified when new
			 * machines are added to the model.
			 */
			final IResourceManager rm = e.getResourceManager();
	        rm.addChildListener(resourceManagerListener);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent(org.eclipse.ptp.core.events.IRemoveResourceManagerEvent)
		 */
		public void handleEvent(IRemoveResourceManagerEvent e) {
			/*
			 * Removed resource manager child listener when resource manager is removed.
			 */
			e.getResourceManager().removeChildListener(resourceManagerListener);
		}		
	}
	
	private final class QueueChildListener implements IQueueChildListener {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IChangedJobEvent)
		 */
		public void handleEvent(IChangedJobEvent e) {
			for (IPJob job : e.getJobs()) {
				refresh(job);
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent(org.eclipse.ptp.core.elements.events.INewJobEvent)
		 */
		public void handleEvent(final INewJobEvent e) {
			PTPUIPlugin.getDisplay().syncExec(new Runnable() {
				public void run() {
					updateColumns(e.getJobs().iterator().next());
				}
			});
			refresh(null);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IRemoveJobEvent)
		 */
		public void handleEvent(IRemoveJobEvent e) {
			refresh(null);
		}
	}
	
	private final class RMChildListener implements IResourceManagerChildListener {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedMachineEvent)
		 */
		public void handleEvent(IChangedMachineEvent e) {
			// Don't need to do anything
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedQueueEvent)
		 */
		public void handleEvent(IChangedQueueEvent e) {
			// Can safely ignore
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerNewMachineEvent)
		 */
		public void handleEvent(INewMachineEvent e) {
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.INewQueueEvent)
		 */
		public void handleEvent(INewQueueEvent e) {
			for (IPQueue queue : e.getQueues()) {
				queue.addChildListener(queueChildListener);
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerRemoveMachineEvent)
		 */
		public void handleEvent(IRemoveMachineEvent e) {
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerRemoveQueueEvent)
		 */
		public void handleEvent(IRemoveQueueEvent e) {
			for (IPQueue queue : e.getQueues()) {
				queue.removeChildListener(queueChildListener);
			}
		}
	}

	private static final String TAG_COLUMN_WIDTHS = "columnWidths"; //$NON-NLS-1$
	private TableViewer viewer;
	private IMemento memento;

	/*
	 * Model listeners
	 */
	private final IModelManagerChildListener modelManagerListener = new MMChildListener();
	private final IResourceManagerChildListener resourceManagerListener = new RMChildListener();
	private final IQueueChildListener queueChildListener = new QueueChildListener();

	private final Set<IAttributeDefinition<?,?,?>> colDefs = Collections.synchronizedSet(new HashSet<IAttributeDefinition<?,?,?>>());
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		
		viewer = new TableViewer(parent, SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);

		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.setUseHashlookup(true);

		createColumns(viewer);

		viewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof IPUniverse) {
					IPUniverse universe = ((IPUniverse)inputElement);
					List<IPJob> jobList = new ArrayList<IPJob>();
					for (IResourceManager rm : universe.getResourceManagers()) {
						for (IPQueue queue : rm.getQueues()) {
							for (IPJob job : queue.getJobs()) {
								jobList.add(job);
							}
						}
					}
					return jobList.toArray(new IPJob[jobList.size()]);
				}
				return new Object[0];
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		viewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object j1, Object j2) {
				return ((IPJob)j1).getName().compareTo(((IPJob)j2).getName());
			}
		});
		getSite().setSelectionProvider(viewer);
		
		IModelManager mm = PTPCorePlugin.getDefault().getModelManager();
		viewer.setInput(mm.getUniverse());

		/*
	     * Add us to any existing RM's. I guess it's possible we could
	     * miss a RM if a new event arrives while we're doing this, but is 
	     * it a problem?
	     */
	    for (IResourceManager rm : mm.getUniverse().getResourceManagers()) {
	        rm.addChildListener(resourceManagerListener);
	        for (IPQueue queue : rm.getQueues()) {
	        	queue.addChildListener(queueChildListener);
	        }
	    }
	    mm.addListener(modelManagerListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;
	}

	/**
	 * @param job
	 */
	private void refresh(final IPJob job) {
		PTPUIPlugin.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!viewer.getTable().isDisposed()) {
					if (job != null) {
						updateColumns(job);
						viewer.refresh(job);
					} else {
						viewer.refresh();
					}
				}
			}
		});
	}
	
	/* (non-Javadoc)
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
	private void addColumn(final IAttributeDefinition<?, ?, ?> attrDef) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		
		column.setLabelProvider(new ColumnLabelProvider(){

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
			 */
			@Override
			public Image getImage(Object element) {
				if (element instanceof IPJob) {
					if (attrDef.equals(JobAttributes.getStateAttributeDefinition())) {
						IPJob job = (IPJob)element;
						return ParallelImages.jobImages[job.getState().ordinal()][job.isDebug() ? 1 : 0];
					}
				}
				return null;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
			 */
			@Override
			public String getText(Object element) {
				if (element instanceof IPJob) {
					IPJob job = (IPJob) element;
					if (!attrDef.equals(JobAttributes.getStateAttributeDefinition())) {
						IAttribute<?,?,?> attr = job.getAttribute(attrDef.getId());
						if (attr != null) {
							return attr.getValueAsString();
						}
					}
				}
				return null;
			}
			
		});
		column.getColumn().setData(attrDef);
		column.getColumn().setResizable(true);
		column.getColumn().setMoveable(true);
		// this will need to sort by the col
		//column.getColumn().addSelectionListener(getHeaderListener());
		String name = attrDef.getName();
		column.getColumn().setText(name);
		PixelConverter converter = new PixelConverter(viewer.getControl());
		int width = Math.max(converter.convertWidthInCharsToPixels(name.length()+2),
								converter.convertWidthInCharsToPixels(5));
		column.getColumn().setWidth(width);
		colDefs.add(attrDef);
	}
	
	/**
	 * @param tableViewer
	 * @param job
	 */
	private void addColumns(TableViewer tableViewer, IPJob job) {
		addColumn(JobAttributes.getStateAttributeDefinition());
		addColumn(ElementAttributes.getNameAttributeDefinition());

		if (job != null) {
			for (IAttribute<?,?,?> attr : job.getAttributes()) {
				IAttributeDefinition<?,?,?> attrDef = attr.getDefinition();
				if (!colDefs.contains(attrDef) && attrDef.getDisplay()) {
					addColumn(attrDef);
				}
			}
		}
	}
	
	/**
	 * @param tableViewer
	 */
	private void createColumns(TableViewer tableViewer) {
		addColumns(tableViewer, null);
	}
	
	/**
	 * @param job
	 */
	private void updateColumns(IPJob job) {
		for (IAttribute<?,?,?> attr : job.getAttributes()) {
			IAttributeDefinition<?,?,?> attrDef = attr.getDefinition();
			if (!colDefs.contains(attrDef) && attrDef.getDisplay()) {
				addColumn(attrDef);
			}
		}
		viewer.getTable().layout(true);
	}

}
