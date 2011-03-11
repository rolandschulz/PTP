package org.eclipse.ptp.rm.jaxb.core.runnable.command;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStatus;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamsProxy;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rmsystem.IJobStatus;

public class CommandJobStatus implements ICommandJobStatus {

	private String jobId;
	private ILaunchConfiguration launchConfig;
	private String state;
	private ICommandJobStreamsProxy proxy;
	private boolean waitEnabled;

	public CommandJobStatus() {
		jobId = null;
		state = IJobStatus.UNDETERMINED;
		waitEnabled = true;
	}

	public CommandJobStatus(String jobId, String state) {
		this.jobId = jobId;
		this.state = state;
		waitEnabled = false;
	}

	public void cancelWait() {
		synchronized (this) {
			waitEnabled = false;
			notifyAll();
		}
	}

	public String getJobId() {
		synchronized (this) {
			return jobId;
		}
	}

	public ILaunchConfiguration getLaunchConfiguration() {
		return launchConfig;
	}

	public String getState() {
		return state;
	}

	public String getStateDetail() {
		return state;
	}

	public IStreamsProxy getStreamsProxy() {
		return proxy;
	}

	public void setLaunchConfig(ILaunchConfiguration launchConfig) {
		this.launchConfig = launchConfig;
	}

	public void setProxy(ICommandJobStreamsProxy proxy) {
		this.proxy = proxy;
	}

	public void waitForJobId(String uuid) {
		synchronized (this) {
			while (waitEnabled && jobId == null) {
				try {
					wait(1000);
				} catch (InterruptedException ignored) {
				}
				Property p = (Property) RMVariableMap.getActiveInstance().getVariables().get(uuid);
				if (p != null) {
					jobId = p.getName();
					String v = (String) p.getValue();
					if (v != null) {
						state = v;
					}
				}
			}
		}
	}
}
