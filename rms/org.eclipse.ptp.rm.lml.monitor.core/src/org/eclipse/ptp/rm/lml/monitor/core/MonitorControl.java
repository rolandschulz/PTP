/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.lml.monitor.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.jobs.IJobAddedEvent;
import org.eclipse.ptp.core.jobs.IJobChangedEvent;
import org.eclipse.ptp.core.jobs.IJobListener;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.server.RemoteServerManager;
import org.eclipse.ptp.rm.jaxb.control.LaunchController;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.model.IPattern;
import org.eclipse.ptp.rm.lml.da.server.core.LMLDAServer;
import org.eclipse.ptp.rm.lml.internal.core.elements.DriverType;
import org.eclipse.ptp.rm.lml.internal.core.elements.RequestType;
import org.eclipse.ptp.rm.lml.internal.core.model.Pattern;
import org.eclipse.ptp.rm.lml.monitor.LMLMonitorCorePlugin;
import org.eclipse.ptp.rm.lml.monitor.core.messages.Messages;
import org.eclipse.ui.IMemento;

/**
 * LML JAXB resource manager monitor
 */
@SuppressWarnings("restriction")
public class MonitorControl extends LaunchController implements IMonitorControl {
	/**
	 * Job for running the LML DA server. This job gets run periodically based on the JOB_SCHEDULE_FREQUENCY.
	 */
	private class MonitorJob extends Job {
		private final LMLDAServer fServer;

		public MonitorJob(IRemoteConnection conn) {
			super(Messages.LMLResourceManagerMonitor_LMLMonitorJob);
			setSystem(true);
			fServer = (LMLDAServer) RemoteServerManager.getServer(LMLDAServer.SERVER_ID, conn);
			fServer.setWorkDir(new Path(conn.getWorkingDirectory()).append(".eclipsesettings").toString()); //$NON-NLS-1$
		}

		/**
		 * Schedule an immediate refresh
		 */
		public void refresh() {
			wakeUp();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final SubMonitor subMon = SubMonitor.convert(monitor, 100);
			try {
				try {
					fServer.startServer(subMon.newChild(20));
					if (!subMon.isCanceled()) {
						fServer.waitForServerStart(subMon.newChild(20));
						if (!subMon.isCanceled()) {
							LMLManager.getInstance().update(getMonitorId(), fServer.getInputStream(), fServer.getOutputStream());
						}
					}
				} catch (final Exception e) {
					fActive = false;
					MonitorControlManager.getInstance().fireMonitorUpdated(new IMonitorControl[] { MonitorControl.this });
					return new Status(IStatus.ERROR, LMLMonitorCorePlugin.PLUGIN_ID, e.getLocalizedMessage());
				}
				IStatus status = fServer.waitForServerFinish(subMon.newChild(40));
				if (status == Status.OK_STATUS) {
					schedule(JOB_SCHEDULE_FREQUENCY);
				} else {
					fActive = false;
					MonitorControlManager.getInstance().fireMonitorUpdated(new IMonitorControl[] { MonitorControl.this });
				}
				return status;
			} finally {
				if (monitor != null) {
					monitor.done();
				}
			}
		}
	}

	private class JobListener implements IJobListener {
		public void handleEvent(IJobAddedEvent e) {
			addJob(e.getJobStatus());
		}

		public void handleEvent(IJobChangedEvent e) {
			updateJob(e.getJobStatus());
		}
	}

	/*
	 * needs to be parameter
	 */
	private static final int JOB_SCHEDULE_FREQUENCY = 60000;

	private MonitorJob fMonitorJob;

	private final String fMonitorId;
	private final LMLManager fLMLManager = LMLManager.getInstance();
	private final JobListener fJobListener = new JobListener();
	private String fSavedLayout = new String();
	private JobStatusData[] fSavedJobs;
	private Map<String, List<IPattern>> fSavedPattern;
	private String fSystemType;
	private boolean fActive;

	private static final String JOBS_ATTR = "jobs";//$NON-NLS-1$ 
	private static final String LAYOUT_ATTR = "layout";//$NON-NLS-1$
	private static final String LAYOUT_STRING_ATTR = "layoutString";//$NON-NLS-1$
	private static final String PATTERNS_ATTR = "patterns";//$NON-NLS-1$
	private static final String PATTERN_GID_ATTR = "gid";//$NON-NLS-1$
	private static final String FILTER_TITLE_ATTR = "columnTitle";//$NON-NLS-1$
	private static final String FILTER_TYPE_ATTR = "type";//$NON-NLS-1$
	private static final String FILTER_RANGE_ATTR = "range";//$NON-NLS-1$
	private static final String FILTER_RELATION_ATTR = "relation";//$NON-NLS-1$
	private static final String FILTER_MAX_VALUE_RANGE_ATTR = "maxValueRange";//$NON-NLS-1$
	private static final String FILTER_MIN_VALUE_RANGE_ATTR = "minValueRange";//$NON-NLS-1$
	private static final String FILTER_RELATION_OPERATOR_ATTR = "relationOperartor";//$NON-NLS-1$
	private static final String FILTER_RELATION_VALUE_ATTR = "relationValue";//$NON-NLS-1$
	private static final String JOB_ID_ATTR = "job_id";//$NON-NLS-1$
	private static final String CONTROL_ID_ATTR = "control_id";//$NON-NLS-1$
	private static final String STDOUT_REMOTE_FILE_ATTR = "stdout_remote_path";//$NON-NLS-1$
	private static final String STDERR_REMOTE_FILE_ATTR = "stderr_remote_path";//$NON-NLS-1$
	private static final String INTERACTIVE_ATTR = "interactive";//$NON-NLS-1$;
	private static final String STATE_ATTR = "state";//$NON-NLS-1$;
	private static final String STATE_DETAIL_ATTR = "state_detail";//$NON-NLS-1$;
	private static final String OID_ATTR = "oid";//$NON-NLS-1$;
	private static final String QUEUE_NAME_ATTR = "queue_name";//$NON-NLS-1$;
	private static final String OWNER_ATTR = "owner";//$NON-NLS-1$;
	private static final String REMOTE_SERVICES_ID_ATTR = "remoteServicesId";//$NON-NLS-1$;
	private static final String CONNECTION_NAME_ATTR = "connectionName";//$NON-NLS-1$;
	private static final String SYSTEM_TYPE_ATTR = "systemType";//$NON-NLS-1$;
	private static final String MONITOR_STATE = "monitorState";//$NON-NLS-1$;

	public MonitorControl(String monitorId) {
		fMonitorId = monitorId;
	}

	public String getMonitorId() {
		return fMonitorId;
	}

	public String getSystemType() {
		return fSystemType;
	}

	public boolean isActive() {
		return fActive;
	}

	public boolean load(IMemento memento) {
		IMemento childLayout = memento.getChild(LAYOUT_ATTR);
		if (childLayout != null) {
			fSavedLayout = childLayout.getString(LAYOUT_STRING_ATTR);
		}

		childLayout = memento.getChild(JOBS_ATTR);
		if (childLayout != null) {
			fSavedJobs = loadJobs(memento);
		}

		childLayout = memento.getChild(PATTERNS_ATTR);
		if (childLayout != null) {
			fSavedPattern = loadPattern(memento);
		}

		return loadState(memento);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#refresh()
	 */
	public void refresh() {
		fMonitorJob.refresh();
	}

	public void save(IMemento memento) {
		final String layout = fLMLManager.getCurrentLayout(getMonitorId());
		final JobStatusData[] jobs = fLMLManager.getUserJobs(getMonitorId());
		final Map<String, List<IPattern>> patternMap = fLMLManager.getCurrentPattern(getMonitorId());

		if (layout != null) {
			final IMemento layoutMemento = memento.createChild(LAYOUT_ATTR);
			layoutMemento.putString(LAYOUT_STRING_ATTR, layout);
		}

		if (jobs != null && jobs.length > 0) {
			final IMemento jobsMemento = memento.createChild(JOBS_ATTR);
			for (final JobStatusData status : jobs) {
				if (!status.isRemoved()) {
					saveJob(status, jobsMemento);
				}
			}
		}

		if (patternMap != null && patternMap.keySet().size() > 0) {
			final IMemento patternMemento = memento.createChild(PATTERNS_ATTR);
			for (final Entry<String, List<IPattern>> pattern : patternMap.entrySet()) {
				savePattern(pattern.getKey(), pattern.getValue(), patternMemento);
			}
		}

		saveState(memento);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl#setSystemType(java.lang.String)
	 */
	public void setSystemType(String type) {
		fSystemType = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.monitors.IMonitorControl#start(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void start(IProgressMonitor monitor) throws CoreException {
		if (!isActive()) {
			SubMonitor progress = SubMonitor.convert(monitor, 30);
			try {
				final IRemoteConnection conn = getRemoteConnection(progress.newChild(10));
				if (progress.isCanceled()) {
					return;
				}

				if (!conn.isOpen()) {
					try {
						conn.open(progress.newChild(10));
					} catch (final RemoteConnectionException e) {
						throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(),
								e.getMessage()));
					}
					if (!conn.isOpen()) {
						throw new CoreException(new Status(IStatus.ERROR, LMLMonitorCorePlugin.getUniqueIdentifier(),
								Messages.LMLResourceManagerMonitor_unableToOpenConnection));
					}
				}

				/*
				 * Initialize LML classes
				 */
				fLMLManager.openLgui(getMonitorId(), conn.getUsername(), getMonitorConfigurationRequestType(), fSavedLayout,
						fSavedJobs, fSavedPattern);

				fActive = true;

				MonitorControlManager.getInstance().fireMonitorUpdated(new IMonitorControl[] { this });

				/*
				 * Start monitoring job. Note that the monitoring job can fail, in which case the monitor is considered to be
				 * stopped and the active flag set appropriately.
				 */
				synchronized (this) {
					if (fMonitorJob == null) {
						fMonitorJob = new MonitorJob(conn);
					}
					fMonitorJob.schedule();
				}

				JobManager.getInstance().addListener(fJobListener);
			} finally {
				if (monitor != null) {
					monitor.done();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.monitors.IMonitorControl#stop()
	 */
	@Override
	public void stop() throws CoreException {
		if (isActive()) {
			JobManager.getInstance().removeListener(fJobListener);

			fLMLManager.closeLgui(getMonitorId());

			synchronized (this) {
				if (fMonitorJob != null) {
					fMonitorJob.cancel();
					fMonitorJob = null;
				}
			}

			fActive = false;

			MonitorControlManager.getInstance().fireMonitorUpdated(new IMonitorControl[] { this });
		}
	}

	private RequestType getMonitorConfigurationRequestType() {
		RequestType request = new RequestType();
		final DriverType driver = new DriverType();
		driver.setName(getSystemType());
		request.getDriver().add(driver);
		return request;
	}

	/**
	 * Get the remote connection specified by the monitor configuration.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @return connection for the monitor
	 */
	private IRemoteConnection getRemoteConnection(IProgressMonitor monitor) throws CoreException {
		final IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(getRemoteServicesId(), monitor);
		if (services != null) {
			final IRemoteConnectionManager connMgr = services.getConnectionManager();
			return connMgr.getConnection(getConnectionName());
		}
		throw CoreExceptionUtils.newException("Unable to obtain remote connection", null);
	}

	private JobStatusData[] loadJobs(IMemento memento) {
		final List<JobStatusData> jobs = new ArrayList<JobStatusData>();
		final IMemento[] children = memento.getChildren(JOB_ID_ATTR);
		for (final IMemento child : children) {
			jobs.add(new JobStatusData(child.getID(), child.getString(CONTROL_ID_ATTR), child.getString(STATE_ATTR), child
					.getString(STATE_DETAIL_ATTR), child.getString(STDOUT_REMOTE_FILE_ATTR), child
					.getString(STDERR_REMOTE_FILE_ATTR), child.getBoolean(INTERACTIVE_ATTR), child.getString(QUEUE_NAME_ATTR),
					child.getString(OWNER_ATTR), child.getString(OID_ATTR)));
		}
		return jobs.toArray(new JobStatusData[jobs.size()]);
	}

	private Map<String, List<IPattern>> loadPattern(IMemento memento) {
		final Map<String, List<IPattern>> pattern = new HashMap<String, List<IPattern>>();
		if (memento != null) {
			final IMemento[] childrenPattern = memento.getChildren(PATTERN_GID_ATTR);
			for (final IMemento childPattern : childrenPattern) {
				final List<IPattern> filters = new LinkedList<IPattern>();
				final IMemento[] childrenFilter = childPattern.getChildren(FILTER_TITLE_ATTR);
				for (final IMemento childFilter : childrenFilter) {
					final IPattern filter = new Pattern(childFilter.getID(), childFilter.getString(FILTER_TYPE_ATTR));
					if (childFilter.getBoolean(FILTER_RANGE_ATTR)) {
						filter.setRange(childFilter.getString(FILTER_MIN_VALUE_RANGE_ATTR),
								childFilter.getString(FILTER_MAX_VALUE_RANGE_ATTR));
					} else if (childFilter.getBoolean(FILTER_RELATION_ATTR)) {
						filter.setRelation(childFilter.getString(FILTER_RELATION_OPERATOR_ATTR),
								childFilter.getString(FILTER_RELATION_VALUE_ATTR));
					}
					filters.add(filter);
				}

				if (filters.size() > 0) {
					pattern.put(childPattern.getID(), filters);
				}
			}
		}
		return pattern;
	}

	private boolean loadState(IMemento memento) {
		setRemoteServicesId(memento.getString(REMOTE_SERVICES_ID_ATTR));
		setConnectionName(memento.getString(CONNECTION_NAME_ATTR));
		fSystemType = memento.getString(SYSTEM_TYPE_ATTR);
		return memento.getBoolean(MONITOR_STATE);
	}

	private void saveJob(JobStatusData job, IMemento memento) {
		final IMemento jobMemento = memento.createChild(JOB_ID_ATTR, job.getJobId());
		jobMemento.putString(CONTROL_ID_ATTR, job.getControlId());
		jobMemento.putString(STATE_ATTR, job.getState());
		jobMemento.putString(STATE_DETAIL_ATTR, job.getStateDetail());
		jobMemento.putString(STDOUT_REMOTE_FILE_ATTR, job.getOutputPath());
		jobMemento.putString(STDERR_REMOTE_FILE_ATTR, job.getErrorPath());
		jobMemento.putBoolean(INTERACTIVE_ATTR, job.isInteractive());
		jobMemento.putString(QUEUE_NAME_ATTR, job.getQueueName());
		jobMemento.putString(OWNER_ATTR, job.getOwner());
		jobMemento.putString(OID_ATTR, job.getOid());

	}

	private void savePattern(String key, List<IPattern> value, IMemento memento) {
		final IMemento patternMemento = memento.createChild(PATTERN_GID_ATTR, key);
		for (final IPattern filterValue : value) {
			final IMemento filterMemento = patternMemento.createChild(FILTER_TITLE_ATTR, filterValue.getColumnTitle());
			filterMemento.putString(FILTER_TYPE_ATTR, filterValue.getType());
			filterMemento.putBoolean(FILTER_RANGE_ATTR, filterValue.isRange());
			filterMemento.putBoolean(FILTER_RELATION_ATTR, filterValue.isRelation());
			if (filterValue.isRange()) {
				filterMemento.putString(FILTER_MIN_VALUE_RANGE_ATTR, filterValue.getMinValueRange());
				filterMemento.putString(FILTER_MAX_VALUE_RANGE_ATTR, filterValue.getMaxValueRange());
			} else if (filterValue.isRelation()) {
				filterMemento.putString(FILTER_RELATION_OPERATOR_ATTR, filterValue.getRelationOperator());
				filterMemento.putString(FILTER_RELATION_VALUE_ATTR, filterValue.getRelationValue());
			}
		}
	}

	private void saveState(IMemento memento) {
		memento.putString(REMOTE_SERVICES_ID_ATTR, getRemoteServicesId());
		memento.putString(CONNECTION_NAME_ATTR, getConnectionName());
		memento.putString(SYSTEM_TYPE_ATTR, getSystemType());
		memento.putBoolean(MONITOR_STATE, isActive());
	}

	private void addJob(IJobStatus status) {
		String monitorId = getMonitorId(status);
		if (monitorId != null && monitorId.equals(getMonitorId())) {
			final JobStatusData data = new JobStatusData(status.getJobId(), status.getControlId(), status.getQueueName(),
					status.getOwner(), status.getOutputPath(), status.getErrorPath(), status.isInteractive());
			data.setState(status.getState());
			data.setStateDetail(status.getStateDetail());
			fLMLManager.addUserJob(getMonitorId(), status.getJobId(), data);
		}
	}

	private void updateJob(IJobStatus status) {
		String monitorId = getMonitorId(status);
		if (monitorId != null && monitorId.equals(getMonitorId())) {
			fLMLManager.updateUserJob(getMonitorId(), status.getJobId(), status.getState(), status.getStateDetail());
		}
	}

	private String getMonitorId(IJobStatus status) {
		ILaunchConfiguration configuration = status.getLaunchConfiguration();
		if (configuration != null) {
			String connectionName = LaunchUtils.getConnectionName(configuration);
			String remoteServicesId = LaunchUtils.getRemoteServicesId(configuration);
			String monitorType = LaunchUtils.getSystemType(configuration);
			if (connectionName != null && remoteServicesId != null && monitorType != null) {
				return MonitorControlManager.generateMonitorId(remoteServicesId, connectionName, monitorType);
			}
		}
		return null;
	}
}