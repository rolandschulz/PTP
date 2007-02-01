package org.eclipse.ptp.debug.core.launch;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;

public class PLaunch extends Launch implements IPLaunch {
	private IPJob pJob;
	private Comparator targetComparator;
	//private Comparator processComparator;

	public PLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
		super(launchConfiguration, mode, locator);
		targetComparator = new Comparator() {
			public int compare(Object arg0, Object arg1) {
				IPDebugTarget t0 = (IPDebugTarget) arg0;
				IPDebugTarget t1 = (IPDebugTarget) arg1;
				return t0.getTargetID() - t1.getTargetID();
			}
		};
		/*
		processComparator = new Comparator() {
			public int compare(Object arg0, Object arg1) {
				IPseudoProcess t0 = (IPseudoProcess) arg0;
				IPseudoProcess t1 = (IPseudoProcess) arg1;
				return t0.getTargetID() - t1.getTargetID();
			}
		};
		*/
	}
	public void notifyTerminate() {
		fireTerminate();
	}
	public IPJob getPJob() {
		return pJob;
	}
	public void setPJob(IPJob job) {
		pJob = job;
	}
	protected List getDebugTargets0() {
		List list = super.getDebugTargets0();
		Collections.sort(list, targetComparator);
		return list;
	}
	/*
	protected List getProcesses0() {
		List list = super.getProcesses0();
		Collections.sort(list, processComparator);
		return list;
	}
	*/
	public IPDebugTarget getDebugTarget(int target_id) {
		for (Iterator i=getDebugTargets0().iterator(); i.hasNext();) {
			IPDebugTarget debugTarget = (IPDebugTarget)i.next();
			if (debugTarget.getTargetID() == target_id)
				return debugTarget;
		}
		return null;
	}
	/*
	public IPseudoProcess getDebugProcess(int target_id) {
		for (Iterator i=getProcesses0().iterator(); i.hasNext();) {
			IPseudoProcess proc = (IPseudoProcess)i.next();
			if (proc.getTargetID() == target_id)
				return proc;
		}
		return null;
	}
	public void removeDebugProcess(int target_id) {
		removeProcess(getDebugProcess(target_id));
	}
	*/
	public boolean isTerminated() {
		if (pJob != null)
			return pJob.isAllStop();
		return super.isTerminated();
	}
}
