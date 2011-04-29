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

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
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
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.jaxb.ui.JAXBMonitorPlugin;
import org.eclipse.ptp.rm.jaxb.ui.data.JobStatusData;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.providers.CommandJobStatusContentProvider;
import org.eclipse.ptp.rm.jaxb.ui.providers.CommandJobStatusLabelProvider;
import org.eclipse.ptp.rm.jaxb.ui.sorters.JobListViewerSorter;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
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
public class MonitorJobListView extends ViewPart implements IResourceManagerListener, IJobListener {
	private static final int UNDEFINED = -1;
	private static final int COPY_BUFFER_SIZE = 64 * 1024;

	private TableViewer viewer;
	private final Map<String, JobStatusData> jobs = Collections.synchronizedMap(new TreeMap<String, JobStatusData>());

	/**
	 * Exercises a control operation on the remote job.
	 * 
	 * @param job
	 * @param autoStart
	 * @param operation
	 * @throws CoreException
	 */
	public void callDoControl(JobStatusData job, boolean autoStart, String operation) throws CoreException {
		IResourceManager rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(job.getRmId());
		IResourceManagerControl control = rm.getControl();
		if (checkControl(rm, control, autoStart)) {
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
		addTableColumn(viewer, Messages.JOB_ID, SWT.LEFT, 100, getSelectionAdapter(viewer, 0));
		addTableColumn(viewer, Messages.STATE, SWT.LEFT, 100, getSelectionAdapter(viewer, 1));
		addTableColumn(viewer, Messages.STATE_DETAIL, SWT.LEFT, 100, getSelectionAdapter(viewer, 2));
		addTableColumn(viewer, Messages.STDOUT_PATH, SWT.LEFT, 100, getSelectionAdapter(viewer, 3));
		addTableColumn(viewer, Messages.STDOUT_READY, SWT.LEFT, 100, getSelectionAdapter(viewer, 4));
		addTableColumn(viewer, Messages.STDERR_PATH, SWT.LEFT, 100, getSelectionAdapter(viewer, 5));
		addTableColumn(viewer, Messages.STDERR_READY, SWT.LEFT, 100, getSelectionAdapter(viewer, 6));
		getSite().setSelectionProvider(viewer);
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);
		getSite().registerContextMenu(contextMenu, viewer);
		Control control = viewer.getControl();
		Menu menu = contextMenu.createContextMenu(control);
		control.setMenu(menu);
		refresh();
	}

	/**
	 * Fetches the remote stdout/stderr contents. This is functionality imported
	 * from JAXB core to avoid dependencies.
	 * 
	 * @param rmId
	 *            resource manager unique name
	 * @param path
	 *            of remote file.
	 * @param autoStart
	 *            start the resource manager if it is not started
	 * @return contents of the file, or empty string if path is undefined.
	 */
	public String doRead(String rmId, String path, boolean autoStart) throws CoreException {

		if (path == null) {
			return JobStatusData.ZEROSTR;
		}
		IResourceManager rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmId);
		IResourceManagerControl control = rm.getControl();
		if (checkControl(rm, control, autoStart)) {
			String remoteServicesId = control.getControlConfiguration().getRemoteServicesId();
			if (remoteServicesId != null) {
				IProgressMonitor monitor = new NullProgressMonitor();
				IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(remoteServicesId, monitor);
				IRemoteConnectionManager remoteConnectionManager = remoteServices.getConnectionManager();
				String remoteConnectionName = control.getControlConfiguration().getConnectionName();
				IRemoteConnection remoteConnection = remoteConnectionManager.getConnection(remoteConnectionName);
				IRemoteFileManager remoteFileManager = remoteServices.getFileManager(remoteConnection);
				IFileStore lres = remoteFileManager.getResource(path);
				BufferedInputStream is = new BufferedInputStream(lres.openInputStream(EFS.NONE, monitor));
				StringBuffer sb = new StringBuffer();
				byte[] buffer = new byte[COPY_BUFFER_SIZE];
				int rcvd = 0;
				try {
					while (true) {
						try {
							rcvd = is.read(buffer, 0, COPY_BUFFER_SIZE);
						} catch (EOFException eof) {
							break;
						}

						if (rcvd == UNDEFINED) {
							break;
						}
						sb.append(new String(buffer, 0, rcvd));
					}
				} catch (IOException ioe) {
					throw new CoreException(new Status(IStatus.ERROR, JAXBMonitorPlugin.PLUGIN_ID, ioe.getMessage(), ioe));
				} finally {
					try {
						is.close();
					} catch (IOException ioe) {
						JAXBMonitorPlugin.log(ioe);
					}
				}
				return sb.toString();
			}
		}
		return JobStatusData.ZEROSTR;
	}

	public TableViewer getViewer() {
		return viewer;
	}

	/*
	 * Calls {@link #maybeUpdateJobState(JobStatusData, boolean)} (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.listeners.IJobListener#handleEvent(org.eclipse.ptp
	 * .core.events.IJobChangedEvent)
	 */
	public void handleEvent(IJobChangedEvent e) {
		String jobId = e.getJobId();
		IResourceManager rm = e.getSource();
		IJobStatus status = rm.getControl().getJobStatus(jobId);
		JobStatusData data = jobs.get(jobId);
		if (data == null) {
			data = new JobStatusData(status);
			jobs.put(jobId, data);
		} else {
			data.updateState(status);
		}
		try {
			maybeUpdateJobState(data, false);
		} catch (Throwable t) {
			JAXBMonitorPlugin.log(t);
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
		if (rm != null) {
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
		if (rm != null) {
			rm.removeJobListener(this);
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		registerInitial();
		List<JobStatusData> jobs = JobStatusData.reload(memento);
		for (JobStatusData job : jobs) {
			this.jobs.put(job.getJobId(), job);
			try {
				maybeUpdateJobState(job, false);
			} catch (Throwable t) {
				throw new PartInitException(JAXBMonitorPlugin.PLUGIN_ID, t);
			}
		}
	}

	/**
	 * 
	 * @param job
	 * @param autoStart
	 * @throws CoreException
	 */
	public void maybeUpdateJobState(JobStatusData job, boolean autoStart) throws CoreException {
		IResourceManager rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(job.getRmId());
		IResourceManagerControl control = rm.getControl();
		if (checkControl(rm, control, autoStart)) {
			IJobStatus refreshed = control.getJobStatus(job.getJobId());
			job.updateState(refreshed);
			maybeCheckFiles(job);
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
		for (JobStatusData job : jobs.values()) {
			job.save(memento);
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

	private boolean checkControl(IResourceManager manager, IResourceManagerControl control, boolean autoStart) throws CoreException {
		boolean ok = false;
		if (control != null) {
			if (manager.getState().equals(IResourceManager.STARTED_STATE)) {
				ok = true;
			} else if (autoStart) {
				control.start(new NullProgressMonitor());
				ok = true;
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

	/**
	 * Set the flags if this update carries ready info for the output files.
	 * 
	 * @param job
	 */
	private void maybeCheckFiles(JobStatusData job) {
		if (IJobStatus.JOB_OUTERR_READY.equals(job.getStateDetail())) {
			if (job.getOutputPath() != null) {
				job.setOutReady(true);
			}
			if (job.getErrorPath() != null) {
				job.setErrReady(true);
			}
		}
	}

	/**
	 * Add this view as listeners to all existing RMs.
	 */
	private void registerInitial() {
		IModelManager manager = PTPCorePlugin.getDefault().getModelManager();
		manager.addListener(this);
		IResourceManager[] rms = manager.getResourceManagers();
		for (IResourceManager rm : rms) {
			if (rm != null) {
				rm.addJobListener(this);
			}
		}
	}

	private static TableColumn addTableColumn(final TableViewer viewer, String columnName, int style, int width,
			final SelectionAdapter s) {
		Table t = viewer.getTable();
		TableColumn c = new TableColumn(t, style);
		c.setText(columnName);
		c.setWidth(width);
		c.addSelectionListener(s);
		return c;
	}
}
