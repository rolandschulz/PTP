/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.views;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.events.IJobChangedEvent;
import org.eclipse.ptp.core.events.IResourceManagerAddedEvent;
import org.eclipse.ptp.core.events.IResourceManagerChangedEvent;
import org.eclipse.ptp.core.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.events.IResourceManagerRemovedEvent;
import org.eclipse.ptp.core.listeners.IJobListener;
import org.eclipse.ptp.core.listeners.IResourceManagerListener;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus;
import org.eclipse.ptp.rm.jaxb.core.IFileReadyListener;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.data.PersistentCommandJobStatus;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.providers.CommandJobStatusContentProvider;
import org.eclipse.ptp.rm.jaxb.ui.providers.CommandJobStatusLabelProvider;
import org.eclipse.ptp.rm.jaxb.ui.sorters.JobListViewerSorter;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

/**
 * A proof-of concept view of JAXB jobs. Will be replaced by monitor views.
 * 
 * @author arossi
 * 
 */
public class MonitorJobListView extends ViewPart implements IResourceManagerListener, IJobListener, IFileReadyListener,
		IJAXBUINonNLSConstants {

	private TableViewer viewer;
	private final Map<String, PersistentCommandJobStatus> jobs = Collections
			.synchronizedMap(new TreeMap<String, PersistentCommandJobStatus>());

	public void callDoControl(PersistentCommandJobStatus job, boolean autoStart, String operation) throws CoreException {
		IJAXBResourceManagerControl control = job.getStatus().getControl();
		if (checkControl(control, autoStart)) {
			control.control(job.getJobId(), operation, new NullProgressMonitor());
			maybeUpdateJobState(job, autoStart);
		}
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
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new CommandJobStatusContentProvider());
		viewer.setLabelProvider(new CommandJobStatusLabelProvider());
		WidgetBuilderUtils.addTableColumn(viewer, Messages.JOB_ID, SWT.LEFT, 100, getSelectionAdapter(viewer, 0));
		WidgetBuilderUtils.addTableColumn(viewer, Messages.STATE, SWT.LEFT, 100, getSelectionAdapter(viewer, 1));
		WidgetBuilderUtils.addTableColumn(viewer, Messages.STATE_DETAIL, SWT.LEFT, 100, getSelectionAdapter(viewer, 2));
		WidgetBuilderUtils.addTableColumn(viewer, Messages.STDOUT_PATH, SWT.LEFT, 100, getSelectionAdapter(viewer, 3));
		WidgetBuilderUtils.addTableColumn(viewer, Messages.STDOUT_READY, SWT.LEFT, 100, getSelectionAdapter(viewer, 4));
		WidgetBuilderUtils.addTableColumn(viewer, Messages.STDERR_PATH, SWT.LEFT, 100, getSelectionAdapter(viewer, 5));
		WidgetBuilderUtils.addTableColumn(viewer, Messages.STDERR_READY, SWT.LEFT, 100, getSelectionAdapter(viewer, 6));
		getSite().setSelectionProvider(viewer);
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);
		getSite().registerContextMenu(contextMenu, viewer);
		Control control = viewer.getControl();
		Menu menu = contextMenu.createContextMenu(control);
		control.setMenu(menu);
		refresh();
	}

	public TableViewer getViewer() {
		return viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.listeners.IJobListener#handleEvent(org.eclipse.ptp
	 * .core.events.IJobChangedEvent)
	 */
	public void handleEvent(IJobChangedEvent e) {
		IResourceManager rm = e.getSource();
		if (rm instanceof IJAXBResourceManager) {
			IJAXBResourceManager jaxbRm = (IJAXBResourceManager) rm;
			IJAXBResourceManagerControl control = jaxbRm.getControl();
			String jobId = e.getJobId();
			ICommandJobStatus status = (ICommandJobStatus) control.getJobStatus(jobId);
			PersistentCommandJobStatus job = new PersistentCommandJobStatus(status);
			jobs.put(job.getJobId(), job);
			refresh();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.listeners.IResourceManagerListener#handleEvent(org
	 * .eclipse.ptp.core.events.IResourceManagerAddedEvent)
	 */
	public void handleEvent(IResourceManagerAddedEvent e) {
		IResourceManager rm = e.getResourceManager();
		if (rm instanceof IJAXBResourceManager) {
			rm.addJobListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.listeners.IResourceManagerListener#handleEvent(org
	 * .eclipse.ptp.core.events.IResourceManagerChangedEvent)
	 */
	public void handleEvent(IResourceManagerChangedEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.listeners.IResourceManagerListener#handleEvent(org
	 * .eclipse.ptp.core.events.IResourceManagerErrorEvent)
	 */
	public void handleEvent(IResourceManagerErrorEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.listeners.IResourceManagerListener#handleEvent(org
	 * .eclipse.ptp.core.events.IResourceManagerRemovedEvent)
	 */
	public void handleEvent(IResourceManagerRemovedEvent e) {
		IResourceManager rm = e.getResourceManager();
		if (rm instanceof IJAXBResourceManager) {
			rm.removeJobListener(this);
		}
	}

	/*
	 * Updates the persistent status object; maybe enables the actions.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.IFileReadyListener#handleReadyFile(java.
	 * lang.String, boolean)
	 */
	public void handleReadyFile(String jobId, String remoteFile, boolean ready) {
		PersistentCommandJobStatus job = jobs.get(jobId);
		ICommandJobRemoteOutputHandler h = job.getOutputHandler();
		if (h != null && remoteFile.equals(h.getRemoteFilePath())) {
			job.setOutReady(ready);
		} else {
			h = job.getErrorHandler();
			if (h != null && remoteFile.equals(h.getRemoteFilePath())) {
				job.setErrReady(ready);
			}
		}
		refresh();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		registerInitial();
		List<PersistentCommandJobStatus> jobs = PersistentCommandJobStatus.reload(memento);
		for (PersistentCommandJobStatus job : jobs) {
			job.maybeAddListener(this);
			this.jobs.put(job.getJobId(), job);
			maybeUpdateJobState(job, false);
		}
	}

	public void maybeUpdateJobState(PersistentCommandJobStatus job, boolean autoStart) {
		IJAXBResourceManagerControl control = job.getStatus().getControl();
		if (checkControl(control, autoStart)) {
			IJobStatus refreshed = control.getJobStatus(job.getJobId());
			job.getStatus().setState(refreshed.getState());
			refresh();
		}
	}

	/**
	 * Refresh the viewer.
	 */
	public void refresh() {
		new UIJob(Messages.JobListUpdate) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				viewer.setInput(jobs.values());
				viewer.refresh();
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/**
	 * Delete job status entry and refresh.
	 * 
	 * @param jobId
	 */
	public void removeJob(String jobId) {
		jobs.remove(jobId);
		refresh();
	}

	@Override
	public void saveState(IMemento memento) {
		for (PersistentCommandJobStatus job : jobs.values()) {
			job.save(memento);
			job.maybeRemoveListener(this);
		}
		super.saveState(memento);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}

	private boolean checkControl(IJAXBResourceManagerControl control, boolean autoStart) {
		boolean ok = false;
		if (control != null) {
			if (control.getState().equals(IResourceManager.STARTED_STATE)) {
				ok = true;
			} else if (autoStart) {
				try {
					control.start(new NullProgressMonitor());
					ok = true;
				} catch (CoreException t) {
					JAXBUIPlugin.log(t);
				}
			}
		}
		return ok;
	}

	private SelectionAdapter getSelectionAdapter(final TableViewer viewer, final int column) {
		SelectionAdapter sa = new SelectionAdapter() {
			private boolean toggle = false;

			@Override
			public void widgetSelected(SelectionEvent e) {
				JobListViewerSorter sorter = new JobListViewerSorter(column);
				if (toggle) {
					sorter.toggle();
				}
				viewer.setSorter(sorter);
				toggle = !toggle;
			}
		};
		return sa;
	}

	private void registerInitial() {
		IModelManager manager = PTPCorePlugin.getDefault().getModelManager();
		manager.addListener(this);
		IResourceManager[] rms = manager.getResourceManagers();
		for (IResourceManager rm : rms) {
			if (rm instanceof IJAXBResourceManager) {
				rm.addJobListener(this);
			}
		}
	}
}
