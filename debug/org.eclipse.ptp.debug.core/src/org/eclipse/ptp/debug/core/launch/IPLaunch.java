package org.eclipse.ptp.debug.core.launch;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;

public interface IPLaunch extends ILaunch {
	IPJob getPJob();
	void setPJob(IPJob job);
	IPDebugTarget getDebugTarget(BitList tasks);
	IPDebugTarget getDebugTarget(int task_id);
}
