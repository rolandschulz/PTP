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
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes.State;
import org.eclipse.ptp.core.elements.events.IJobChangeEvent;
import org.eclipse.ptp.core.elements.listeners.IJobListener;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.messages.Messages;

public class RuntimeProcess implements IProcess, IJobListener {
	private IPLaunch launch = null;
	private IPJob job = null;
	private Map<String, String> fAttributes;
	private int fExitValue = -1;
	private boolean fTerminated = false;

	public RuntimeProcess(IPLaunch launch, IPJob job, Map<String, String> attributes) {
		this.launch = launch;
		this.job = job;
		job.addElementListener(this);
		initializeAttributes(attributes);
		fTerminated = job.getState() == JobAttributes.State.COMPLETED;
		launch.addProcess(this);
		fireCreationEvent();
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
	public String getLabel() {
		return job.getName();
	}

	public ILaunch getLaunch() {
		return launch;
	}

	public IStreamsProxy getStreamsProxy() {
		return null;
	}

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

	public String getAttribute(String key) {
		if (fAttributes == null) {
			return null;
		}
		return fAttributes.get(key);
	}

	public int getExitValue() throws DebugException {
		if (isTerminated()) {
			return fExitValue;
		}
		throw new DebugException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(), IStatus.ERROR,
				Messages.RuntimeProcess_0, null));
	}

	protected void terminated() {
		synchronized (this) {
			fTerminated = true;
		}
		fExitValue = 0;
		job.removeElementListener(this);
		fireTerminateEvent();
	}

	/***************************************************************************************************************************************************************************************************
	 * ITerminate interface
	 **************************************************************************************************************************************************************************************************/
	public synchronized boolean canTerminate() {
		return !fTerminated;
	}

	public synchronized boolean isTerminated() {
		return fTerminated;
	}

	public void terminate() throws DebugException {
		if (!isTerminated()) {
			try {
				IPResourceManager rm = job.getResourceManager();
				rm.terminateJob(job);
			} catch (CoreException e) {
				throw new DebugException(e.getStatus());
			}
		}
	}

	/***************************************************************************************************************************************************************************************************
	 * Adapter interface
	 **************************************************************************************************************************************************************************************************/
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
	 * IJobListener interface
	 **************************************************************************************************************************************************************************************************/

	public void handleEvent(IJobChangeEvent e) {
		EnumeratedAttribute<JobAttributes.State> attr = e.getAttributes().getAttribute(JobAttributes.getStateAttributeDefinition());
		if (attr != null) {
			JobAttributes.State state = attr.getValue();
			if (state == State.COMPLETED) {
				terminated();
			}
		}
	}

	/***************************************************************************************************************************************************************************************************
	 * Debug Event
	 **************************************************************************************************************************************************************************************************/
	protected void fireCreationEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
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
}
