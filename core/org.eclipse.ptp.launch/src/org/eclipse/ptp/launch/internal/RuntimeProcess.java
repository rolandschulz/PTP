package org.eclipse.ptp.launch.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.core.jobs.IJobListener;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.messages.Messages;

public class RuntimeProcess implements IProcess, IJobListener {
	private IPLaunch fLaunch = null;
	private Map<String, String> fAttributes;
	private int fExitValue = -1;
	private boolean fTerminated = false;

	public RuntimeProcess(IPLaunch launch, Map<String, String> attributes) {
		fLaunch = launch;
		JobManager.getInstance().addListener(launch.getJobControl().getControlId(), this);
		initializeAttributes(attributes);
		try {
			fTerminated = launch.getJobControl().getJobStatus(launch.getJobId(), null).getState().equals(IJobStatus.COMPLETED);
		} catch (CoreException e) {
			fTerminated = true;
		}
		launch.addProcess(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public synchronized boolean canTerminate() {
		return !fTerminated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IProcess.class)) {
			return this;
		}
		if (adapter.equals(ILaunch.class)) {
			return getLaunch();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IProcess#getAttribute(java.lang.String)
	 */
	public String getAttribute(String key) {
		if (fAttributes == null) {
			return null;
		}
		return fAttributes.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IProcess#getExitValue()
	 */
	public int getExitValue() throws DebugException {
		if (isTerminated()) {
			return fExitValue;
		}
		throw new DebugException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(), IStatus.ERROR,
				Messages.RuntimeProcess_Exit_value_not_available, null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IProcess#getLabel()
	 */
	public String getLabel() {
		return "Runtime process " + fLaunch.getJobId(); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IProcess#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fLaunch;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IProcess#getStreamsProxy()
	 */
	public IStreamsProxy getStreamsProxy() {
		try {
			return fLaunch.getJobControl().getJobStatus(fLaunch.getJobId(), null).getStreamsProxy();
		} catch (CoreException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IJobListener#jobAdded(org.eclipse.ptp.core.jobs.IJobStatus)
	 */
	public void jobAdded(IJobStatus status) {
		// nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IJobListener#jobChanged(org.eclipse.ptp.core.jobs.IJobStatus)
	 */
	public void jobChanged(IJobStatus status) {
		if (!isTerminated() && fLaunch.getJobId().equals(status.getJobId())) {
			if (status.getState().equals(IJobStatus.COMPLETED)) {
				terminated();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public synchronized boolean isTerminated() {
		return fTerminated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IProcess#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		if (fAttributes == null) {
			fAttributes = new HashMap<String, String>(5);
		}
		Object origVal = fAttributes.get(key);
		if (origVal != null && origVal.equals(value)) {
			return;
		}
		fAttributes.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		if (!isTerminated()) {
			try {
				fLaunch.getJobControl().control(fLaunch.getJobId(), IJobControl.TERMINATE_OPERATION, null);
			} catch (CoreException e) {
				throw new DebugException(e.getStatus());
			}
		}
	}

	private void initializeAttributes(Map<String, String> attributes) {
		if (attributes != null) {
			Iterator<String> keys = attributes.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				setAttribute(key, attributes.get(key));
			}
		}
	}

	protected void fireEvent(DebugEvent event) {
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[] { event });
		}
	}

	protected void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}

	protected void terminated() {
		synchronized (this) {
			fTerminated = true;
		}
		fExitValue = 0;
		fireTerminateEvent();
		JobManager.getInstance().removeListener(this);
	}
}
