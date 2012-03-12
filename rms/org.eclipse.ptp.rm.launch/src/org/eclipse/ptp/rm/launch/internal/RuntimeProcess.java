package org.eclipse.ptp.rm.launch.internal;

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
import org.eclipse.ptp.core.events.IJobChangedEvent;
import org.eclipse.ptp.core.listeners.IJobListener;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.rm.launch.internal.messages.Messages;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

public class RuntimeProcess implements IProcess, IJobListener {
	private IPLaunch fLaunch = null;
	private final IResourceManager fResourceManager;
	private String fJobId = null;
	private Map<String, String> fAttributes;
	private int fExitValue = -1;
	private boolean fTerminated = false;

	public RuntimeProcess(IPLaunch launch, IResourceManager rm, String jobId, Map<String, String> attributes) {
		fLaunch = launch;
		fResourceManager = rm;
		fJobId = jobId;
		rm.addJobListener(this);
		initializeAttributes(attributes);
		fTerminated = rm.getJobStatus(jobId, null).getState().equals(IJobStatus.COMPLETED);
		launch.addProcess(this);
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

	/***************************************************************************************************************************************************************************************************
	 * IProcess interface
	 **************************************************************************************************************************************************************************************************/
	@Override
	public String getLabel() {
		return fResourceManager.getName() + ": job_" + fJobId; //$NON-NLS-1$
	}

	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}

	@Override
	public IStreamsProxy getStreamsProxy() {
		return fResourceManager.getJobStatus(fJobId, null).getStreamsProxy();
	}

	@Override
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

	@Override
	public String getAttribute(String key) {
		if (fAttributes == null) {
			return null;
		}
		return fAttributes.get(key);
	}

	@Override
	public int getExitValue() throws DebugException {
		if (isTerminated()) {
			return fExitValue;
		}
		throw new DebugException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(), IStatus.ERROR,
				Messages.RuntimeProcess_Exit_value_not_available, null));
	}

	protected void terminated() {
		synchronized (this) {
			fTerminated = true;
		}
		fExitValue = 0;
		fireTerminateEvent();
	}

	/***************************************************************************************************************************************************************************************************
	 * ITerminate interface
	 **************************************************************************************************************************************************************************************************/
	@Override
	public synchronized boolean canTerminate() {
		return !fTerminated;
	}

	@Override
	public synchronized boolean isTerminated() {
		return fTerminated;
	}

	@Override
	public void terminate() throws DebugException {
		if (!isTerminated()) {
			try {
				fResourceManager.control(fJobId, IResourceManagerControl.TERMINATE_OPERATION, null);
			} catch (CoreException e) {
				throw new DebugException(e.getStatus());
			}
		}
	}

	/***************************************************************************************************************************************************************************************************
	 * Adapter interface
	 **************************************************************************************************************************************************************************************************/
	@Override
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

	/***************************************************************************************************************************************************************************************************
	 * IResourceManagerListener interface
	 **************************************************************************************************************************************************************************************************/

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.listeners.IJobListener#handleEvent(org.eclipse.ptp .core.events.IJobChangeEvent)
	 */
	@Override
	public void handleEvent(IJobChangedEvent e) {
		if (!isTerminated()) {
			IResourceManager rm = e.getSource();
			IJobStatus status = rm.getJobStatus(e.getJobId(), null);
			if (status.getState().equals(IJobStatus.COMPLETED)) {
				terminated();
			}
		}
	}

	/***************************************************************************************************************************************************************************************************
	 * Debug Event
	 **************************************************************************************************************************************************************************************************/
	protected void fireEvent(DebugEvent event) {
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[] { event });
		}
	}

	protected void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}
}
