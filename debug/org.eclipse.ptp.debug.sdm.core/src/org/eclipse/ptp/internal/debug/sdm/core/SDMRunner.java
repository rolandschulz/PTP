package org.eclipse.ptp.internal.debug.sdm.core;

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
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.internal.debug.core.PDebugOptions;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.internal.debug.sdm.core.messages.Messages;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;

public class SDMRunner extends Job {
	public enum SDMMasterState {
		UNKNOWN, STARTING, RUNNING, FINISHED, ERROR
	};

	private List<String> command;
	private String workDir = null;
	private SDMMasterState sdmState = SDMMasterState.STARTING;

	private IRemoteConnection connection = null;
	private IRemoteProcess sdmProcess;
	private IPLaunch launch;

	/**
	 * @since 6.0
	 */
	public SDMRunner(IRemoteConnection conn) {
		super(Messages.SDMRunner_0);
		this.setPriority(Job.LONG);
		this.setSystem(true);
		this.connection = conn;
		PDebugOptions.trace(PDebugOptions.DEBUG_MASTER_MORE, Messages.SDMRunner_4);
	}

	public void setCommand(List<String> command) {
		PDebugOptions.trace(PDebugOptions.DEBUG_MASTER_MORE, Messages.SDMRunner_5, command.toString());
		this.command = command;
	}

	public void setWorkDir(String workDir) {
		PDebugOptions.trace(PDebugOptions.DEBUG_MASTER_MORE, Messages.SDMRunner_6, workDir);
		this.workDir = workDir;
	}

	public synchronized SDMMasterState getSdmState() {
		return sdmState;
	}

	protected synchronized void setSdmState(SDMMasterState sdmState) {
		PDebugOptions.trace(PDebugOptions.DEBUG_MASTER_MORE, Messages.SDMRunner_7, sdmState.toString());
		this.sdmState = sdmState;
		this.notifyAll();
	}

	/**
	 * @since 6.0
	 */
	public void setLaunch(IPLaunch launch) {
		PDebugOptions.trace(PDebugOptions.DEBUG_MASTER_MORE, Messages.SDMRunner_8, launch.getJobId());
		this.launch = launch;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		assert command != null;
		assert sdmProcess == null;

		PDebugOptions.trace(PDebugOptions.DEBUG_MASTER, Messages.SDMRunner_9);
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
			IRemoteFileManager fileManager = connection.getFileManager();

			IRemoteProcessBuilder sdmProcessBuilder = connection.getProcessBuilder(command);
			if (workDir != null) {
				sdmProcessBuilder.directory(fileManager.getResource(workDir));
			}

			/*
			 * Wait some time to assure that SDM servers and front end have started.
			 */
			PDebugOptions.trace(PDebugOptions.DEBUG_MASTER, Messages.SDMRunner_10);
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			synchronized (this) {
				wait(3000);
			}

			/*
			 * Create process.
			 */
			PDebugOptions.trace(PDebugOptions.DEBUG_MASTER, Messages.SDMRunner_11);
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
			synchronized (this) {
				sdmProcess = sdmProcessBuilder.start();
			}
			final BufferedReader err_reader = new BufferedReader(new InputStreamReader(sdmProcess.getErrorStream()));
			final BufferedReader out_reader = new BufferedReader(new InputStreamReader(sdmProcess.getInputStream()));

			if (PDebugOptions.isDebugging(PDebugOptions.DEBUG_MASTER_OUTPUT)) {
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
						try {
							out_reader.close();
						} catch (IOException e) {
							// Ignore
						}
					}
				}, Messages.SDMRunner_13).start();
			}

			if (PDebugOptions.isDebugging(PDebugOptions.DEBUG_MASTER_OUTPUT)) {
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
						try {
							err_reader.close();
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
			PDebugOptions.trace(PDebugOptions.DEBUG_MASTER_MORE, Messages.SDMRunner_16);
			while (!sdmProcess.isCompleted()) {
				synchronized (this) {
					wait(500);
				}
				if (monitor.isCanceled()) {
					out_reader.close();
					err_reader.close();
					throw new InterruptedException();
				}
			}

			/*
			 * Check if process terminated successfully (if not canceled).
			 */
			PDebugOptions.trace(PDebugOptions.DEBUG_MASTER, Messages.SDMRunner_17, Integer.toString(sdmProcess.exitValue()));
			if (sdmProcess.exitValue() != 0) {
				if (!monitor.isCanceled()) {
					throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(), NLS.bind(
							Messages.SDMRunner_2, sdmProcess.exitValue())));
				} else {
					PDebugOptions.trace(PDebugOptions.DEBUG_MASTER, Messages.SDMRunner_18);
				}
			}
			setSdmState(SDMMasterState.FINISHED);
			return Status.OK_STATUS;
		} catch (Exception e) {
			/*
			 * Terminate the job, handling the error.
			 */
			PDebugOptions.trace(PDebugOptions.DEBUG_MASTER, Messages.SDMRunner_19, e.toString());
			synchronized (this) {
				PDebugOptions.trace(PDebugOptions.DEBUG_MASTER, Messages.SDMRunner_20, e.toString());
				sdmProcess.destroy();
			}
			try {
				if (launch != null) {
					PDebugOptions.trace(PDebugOptions.DEBUG_MASTER, Messages.SDMRunner_21, launch.getJobId());
					launch.getJobControl().control(launch.getJobId(), IJobControl.TERMINATE_OPERATION, null);
				}
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
			PDebugOptions.trace(PDebugOptions.DEBUG_MASTER, Messages.SDMRunner_22);
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
