package org.eclipse.ptp.debug.sdm.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.sdm.core.messages.Messages;
import org.eclipse.ptp.debug.sdm.core.utils.DebugUtil;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

public class SDMRunner extends Job {
	public enum SDMMasterState {
		UNKNOWN, STARTING, RUNNING, FINISHED, ERROR
	};

	private List<String> command;
	private String workDir = null;
	private SDMMasterState sdmState = SDMMasterState.STARTING;

	private String jobId = null;
	private IResourceManagerControl rmControl = null;
	private IRemoteProcess sdmProcess;

	/**
	 * @since 5.0
	 */
	public SDMRunner(IResourceManagerControl rmControl) {
		super(Messages.SDMRunner_0);
		this.setPriority(Job.LONG);
		this.setSystem(true);
		this.rmControl = rmControl;
		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, Messages.SDMRunner_4);
	}

	public void setCommand(List<String> command) {
		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, Messages.SDMRunner_5, command.toString());
		this.command = command;
	}

	public void setWorkDir(String workDir) {
		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, Messages.SDMRunner_6, workDir);
		this.workDir = workDir;
	}

	public synchronized SDMMasterState getSdmState() {
		return sdmState;
	}

	protected synchronized void setSdmState(SDMMasterState sdmState) {
		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, Messages.SDMRunner_7, sdmState.toString());
		this.sdmState = sdmState;
		this.notifyAll();
	}

	/**
	 * @since 5.0
	 */
	public void setJob(String jobId) {
		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, Messages.SDMRunner_8, jobId);
		this.jobId = jobId;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		assert command != null;
		assert sdmProcess == null;

		DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMRunner_9);
		/*
		 * Catch all try...catch
		 */
		try {
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			/*
			 * Prepare remote connection.
			 */
			IResourceManagerConfiguration configuration = rmControl.getConfiguration();
			IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(
					configuration.getRemoteServicesId(), monitor);
			IRemoteConnectionManager connectionManager = remoteServices.getConnectionManager();
			IRemoteConnection connection = connectionManager.getConnection(configuration.getConnectionName());
			IRemoteFileManager fileManager = remoteServices.getFileManager(connection);

			IRemoteProcessBuilder sdmProcessBuilder = remoteServices.getProcessBuilder(connection, command);
			if (workDir != null) {
				sdmProcessBuilder.directory(fileManager.getResource(workDir));
			}

			/*
			 * Wait some time to assure that SDM servers and front end have
			 * started.
			 */
			DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMRunner_10);
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			synchronized (this) {
				wait(3000);
			}

			/*
			 * Create process.
			 */
			DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMRunner_11);
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			synchronized (this) {
				sdmProcess = sdmProcessBuilder.start();
			}
			final BufferedReader err_reader = new BufferedReader(new InputStreamReader(sdmProcess.getErrorStream()));
			final BufferedReader out_reader = new BufferedReader(new InputStreamReader(sdmProcess.getInputStream()));

			if (DebugUtil.SDM_MASTER_OUTPUT_TRACING) {
				new Thread(new Runnable() {
					public void run() {
						try {
							String output;
							while ((output = out_reader.readLine()) != null) {
								System.out.println(Messages.SDMRunner_12 + output);
							}
						} catch (IOException e) {
							// Ignore
						}
					}
				}, Messages.SDMRunner_13).start();
			}

			if (DebugUtil.SDM_MASTER_OUTPUT_TRACING) {
				new Thread(new Runnable() {
					public void run() {
						try {
							String line;
							while ((line = err_reader.readLine()) != null) {
								System.err.println(Messages.SDMRunner_14 + line);
							}
						} catch (IOException e) {
							// Ignore
						}
					}
				}, Messages.SDMRunner_15).start();
			}

			/*
			 * Wait while running but not canceled.
			 */
			setSdmState(SDMMasterState.RUNNING);
			DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING_MORE, Messages.SDMRunner_16);
			while (!sdmProcess.isCompleted()) {
				synchronized (this) {
					wait(500);
				}
				if (monitor.isCanceled()) {
					throw new InterruptedException();
				}
			}

			/*
			 * Check if process terminated successfully (if not canceled).
			 */
			DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMRunner_17, sdmProcess.exitValue());
			if (sdmProcess.exitValue() != 0) {
				if (!monitor.isCanceled()) {
					throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(), NLS.bind(
							Messages.SDMRunner_2, sdmProcess.exitValue())));
				} else {
					DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMRunner_18);
				}
			}
			setSdmState(SDMMasterState.FINISHED);
			return Status.OK_STATUS;
		} catch (Exception e) {
			/*
			 * Terminate the job, handling the error. Also terminates the ipjob
			 * since it does not make sense to the ipjob running without
			 * debugger.
			 */
			DebugUtil.error(DebugUtil.SDM_MASTER_TRACING, Messages.SDMRunner_19, e);
			synchronized (this) {
				DebugUtil.error(DebugUtil.SDM_MASTER_TRACING, Messages.SDMRunner_20, e);
				sdmProcess.destroy();
			}
			try {
				DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMRunner_21, jobId);
				rmControl.control(jobId, IResourceManagerControl.TERMINATE_OPERATION, null);
			} catch (CoreException e1) {
				PTPDebugCorePlugin.log(e1);
			}
			if (e instanceof InterruptedException) {
				setSdmState(SDMMasterState.FINISHED);
				return Status.CANCEL_STATUS;
			} else if (e instanceof CoreException) {
				setSdmState(SDMMasterState.ERROR);
				return ((CoreException) e).getStatus();
			} else {
				setSdmState(SDMMasterState.ERROR);
				return new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(), Messages.SDMRunner_3, e);
			}
		} finally {
			DebugUtil.trace(DebugUtil.SDM_MASTER_TRACING, Messages.SDMRunner_22);
			synchronized (this) {
				sdmProcess = null;
			}
		}
	}

	@Override
	protected void canceling() {
		synchronized (this) {
			if (sdmProcess != null) {
				sdmProcess.destroy();
			}
		}
	}
}
