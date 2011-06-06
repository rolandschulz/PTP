/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.lml_jaxb.actions;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.ui.views.TableView;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

/**
 * Executes various control and status calls on the resource manager initiated
 * by actions.
 * 
 * @author arossi
 * 
 */
public class ActionUtils {
	private static final int UNDEFINED = -1;
	private static final int COPY_BUFFER_SIZE = 64 * 1024;

	/**
	 * Exercises a control operation on the remote job.
	 * 
	 * @param job
	 * @param autoStart
	 * @param operation
	 * @throws CoreException
	 */
	public static void callDoControl(JobStatusData job, String operation, TableView view, IProgressMonitor monitor)
			throws CoreException {
		IResourceManager rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(job.getRmId());
		IResourceManagerControl control = rm.getControl();
		control.control(job.getJobId(), operation, monitor);
		maybeUpdateJobState(job, view, monitor);
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
	public static String doRead(final String rmId, final String path, final boolean autoStart) throws CoreException {
		if (path == null) {
			return ""; //$NON-NLS-1$
		}
		final StringBuffer sb = new StringBuffer();
		final Job j = new Job(path) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IResourceManager rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmId);
				IResourceManagerControl control = rm.getControl();
				SubMonitor progress = SubMonitor.convert(monitor, 100);
				try {
					IFileStore lres = getRemoteFile(path, control, progress);
					if (lres != null) {
						BufferedInputStream is = new BufferedInputStream(lres.openInputStream(EFS.NONE, progress.newChild(25)));
						byte[] buffer = new byte[COPY_BUFFER_SIZE];
						int rcvd = 0;
						try {
							while (true) {
								try {
									rcvd = is.read(buffer, 0, COPY_BUFFER_SIZE);
								} catch (final EOFException eof) {
									break;
								}

								if (rcvd == UNDEFINED) {
									break;
								}
								if (progress.isCanceled()) {
									break;
								}
								sb.append(new String(buffer, 0, rcvd));
							}
						} finally {
							try {
								is.close();
							} catch (final IOException ioe) {
								ioe.printStackTrace();
							}
							monitor.done();
						}
					}
				} catch (Throwable t) {
					return CoreExceptionUtils.getErrorStatus(t.getMessage(), t);
				}
				return Status.OK_STATUS;
			}
		};

		j.schedule();

		try {
			j.join();
		} catch (final InterruptedException ignored) {
		}

		return sb.toString();
	}

	/**
	 * 
	 * @param job
	 * @param autoStart
	 * @param monitor
	 * @throws CoreException
	 */
	public static void maybeUpdateJobState(JobStatusData job, TableView view, IProgressMonitor monitor) throws CoreException {
		IResourceManager rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(job.getRmId());
		if (!rm.getState().equals(IResourceManager.STARTED_STATE)) {
			return;
		}
		IResourceManagerControl control = rm.getControl();
		IJobStatus refreshed = control.getJobStatus(job.getJobId(), true, monitor);
		job.updateState(refreshed.getState(), refreshed.getStateDetail());
		maybeCheckFiles(job);
		view.refresh();
	}

	/**
	 * Delete stdout or stderr file on remote host.
	 * 
	 * @param path
	 *            to file
	 */
	public static void removeFile(final String path, final String rmId) {
		new Job(path) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IResourceManager rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmId);
				IResourceManagerControl control = rm.getControl();
				SubMonitor progress = SubMonitor.convert(monitor, 70);
				try {
					IFileStore lres = getRemoteFile(path, control, progress);
					if (lres != null) {
						if (lres.fetchInfo(EFS.NONE, progress.newChild(25)).exists()) {
							lres.delete(EFS.NONE, progress.newChild(25));
						}
					}
				} catch (Throwable t) {
					return CoreExceptionUtils.getErrorStatus(t.getMessage(), t);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/**
	 * Similar to {@link RemoteServicesDelegate#initialize(IProgressMonitor)}
	 * 
	 * @param path
	 * @param control
	 * @param progress
	 * @return file, if retrieval was successful
	 */
	private static IFileStore getRemoteFile(String path, IResourceManagerControl control, SubMonitor progress) {
		String remoteServicesId = control.getControlConfiguration().getRemoteServicesId();
		if (remoteServicesId != null) {
			if (PTPRemoteCorePlugin.getDefault() == null) {
				return null;
			}
			IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(remoteServicesId,
					progress.newChild(25));
			if (remoteServices == null) {
				return null;
			}
			IRemoteConnectionManager remoteConnectionManager = remoteServices.getConnectionManager();
			if (remoteConnectionManager == null) {
				return null;
			}
			String remoteConnectionName = control.getControlConfiguration().getConnectionName();
			if (remoteConnectionName == null) {
				return null;
			}
			IRemoteConnection remoteConnection = remoteConnectionManager.getConnection(remoteConnectionName);
			if (remoteConnection == null) {
				return null;
			}
			IRemoteFileManager remoteFileManager = remoteServices.getFileManager(remoteConnection);
			if (remoteFileManager == null) {
				return null;
			}
			return remoteFileManager.getResource(path);
		}
		return null;
	}

	/**
	 * Set the flags if this update carries ready info for the output files.
	 * 
	 * @param job
	 */
	private static void maybeCheckFiles(JobStatusData job) {
		if (IJobStatus.JOB_OUTERR_READY.equals(job.getStateDetail())) {
			if (job.getOutputPath() != null) {
				job.setOutReady(true);
			}
			if (job.getErrorPath() != null) {
				job.setErrReady(true);
			}
		}
	}

	/*
	 * static only
	 */
	private ActionUtils() {
	}
}
