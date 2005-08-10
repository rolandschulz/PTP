package org.eclipse.ptp.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDISession;

public interface IPCDISession extends ICDISession {
	
	public void registerTarget(int procNum);
	public void registerTargets(int[] procNums);
	public void unregisterTarget(int procNum);
	public void unregisterTargets(int[] targets);
	public int[] getRegisteredTargetIds();
	public boolean isRegistered(int i);

}
