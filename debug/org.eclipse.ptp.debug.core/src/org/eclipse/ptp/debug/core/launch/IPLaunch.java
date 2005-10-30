package org.eclipse.ptp.debug.core.launch;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPseudoProcess;

public interface IPLaunch extends ILaunch {
	public IPJob getPJob();
	public void setPJob(IPJob job);
	public void addDebugTargets(IPDebugTarget[] debugTargets, BitList tasks, boolean sendEvent);
	public void removeDebugTargets(BitList tasks, boolean sendEvent);

	public void removeDebugProcess(int target_id);
	public IPDebugTarget getDebugTarget(int target_id);
	public IPseudoProcess getDebugProcess(int target_id);
	
	public void launchedStarted();
}
