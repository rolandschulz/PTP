package org.eclipse.ptp.rm.jaxb.core;

import org.eclipse.ptp.rmsystem.IJobStatus;

public interface ICommandJobStatus extends IJobStatus {
	void cancelWait();

	void waitForJobId(String uuid);
}
