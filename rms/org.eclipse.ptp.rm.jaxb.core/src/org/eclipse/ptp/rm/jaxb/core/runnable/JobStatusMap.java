package org.eclipse.ptp.rm.jaxb.core.runnable;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus;
import org.eclipse.ptp.rm.jaxb.core.JAXBRMConstants;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

/**
 * Class for handling status of submitted jobs.
 * 
 * @author arossi
 * 
 */
public class JobStatusMap extends Thread {

	private final IResourceManagerControl control;
	private final Map<String, ICommandJobStatus> map;
	private Thread t;
	private boolean running = false;

	public JobStatusMap(IResourceManagerControl control) {
		this.control = control;
		map = new HashMap<String, ICommandJobStatus>();
	}

	/**
	 * Also unpins the id if it is pinned.
	 * 
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @param status
	 *            object containing status info and stream proxy
	 */
	public void addJobStatus(String jobId, ICommandJobStatus status) {
		synchronized (map) {
			map.put(jobId, status);
		}
	}

	/**
	 * Synchronized cancel. External calls are premature and thus should not
	 * block waiting for the remote files if any.
	 * 
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 */
	public ICommandJobStatus cancel(String jobId) {
		ICommandJobStatus status = null;
		synchronized (map) {
			status = map.get(jobId);
			if (status != null) {
				status.cancel();
				status.setState(IJobStatus.CANCELED);
			}
		}
		return status;
	}

	/**
	 * 
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @return object containing status info and stream proxy
	 */
	public ICommandJobStatus getStatus(String jobId) {
		ICommandJobStatus status = null;
		synchronized (map) {
			status = map.get(jobId);
		}
		return status;
	}

	/**
	 * shuts down the daemon
	 */
	public void halt() {
		synchronized (map) {
			running = false;
			t.interrupt();
			map.notifyAll();
		}
	}

	/**
	 * Thread daemon for cleanup on the map. Eliminates stray completed state
	 * information, and also starts the stream proxies on jobs which have been
	 * submitted to a scheduler and have become active.
	 */
	@Override
	public void run() {
		t = Thread.currentThread();
		Map<String, String> toPrune = new HashMap<String, String>();

		synchronized (map) {
			running = true;
		}

		while (isRunning()) {
			synchronized (map) {
				try {
					map.wait(2 * JAXBRMConstants.MINUTE_IN_MS);
				} catch (InterruptedException ignored) {
				}

				for (String jobId : map.keySet()) {
					IJobStatus status = control.getJobStatus(jobId);
					String state = status.getState();
					if (IJobStatus.COMPLETED.equals(state)) {
						toPrune.put(jobId, jobId);
					}
				}

				for (String jobId : toPrune.keySet()) {
					remove(jobId, true);
				}
				toPrune.clear();
			}
		}

		synchronized (map) {
			for (String jobId : map.keySet()) {
				doTerminated(jobId, false);
			}
			map.clear();
		}
	}

	/**
	 * Synchronized terminate.
	 * 
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 */
	public ICommandJobStatus terminated(String jobId) {
		ICommandJobStatus status = null;
		synchronized (map) {
			status = doTerminated(jobId, true);
		}
		return status;
	}

	/**
	 * Must be called under synchronization.
	 * 
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @param block
	 *            wait for the remote files
	 */
	private ICommandJobStatus doTerminated(String jobId, boolean block) {
		ICommandJobStatus status = map.get(jobId);
		if (status != null) {
			String d = status.getStateDetail();
			block = block && !IJobStatus.CANCELED.equals(d) && !IJobStatus.FAILED.equals(d);
			if (!status.isInteractive()) {
				if (block) {
					status.maybeWaitForHandlerFiles(JAXBRMConstants.READY_FILE_BLOCK);
				} else {
					status.maybeWaitForHandlerFiles(0);
				}
			}
			status.cancel();
		}
		return status;
	}

	/**
	 * @return whether the daemon is running
	 */
	private boolean isRunning() {
		boolean b = false;
		synchronized (map) {
			b = running;
		}
		return b;
	}

	/**
	 * Must be called under synchronization. Also unpins the id.
	 * 
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @param block
	 *            wait for the remote files
	 */
	private ICommandJobStatus remove(String jobId, boolean block) {
		ICommandJobStatus status = doTerminated(jobId, block);
		map.remove(jobId);
		return status;
	}
}
