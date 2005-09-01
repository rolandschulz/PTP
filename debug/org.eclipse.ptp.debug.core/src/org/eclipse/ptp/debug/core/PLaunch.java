package org.eclipse.ptp.debug.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.core.IPJob;

public class PLaunch extends Launch implements IPLaunch {
	private IPJob pJob;
	private IPSession pSession;
	private Comparator targetComparator;
	private Comparator processComparator;
	
	public PLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
		super(launchConfiguration, mode, locator);
		
		targetComparator = new Comparator(){
			public int compare(Object arg0, Object arg1) {
				IDebugTarget t0 = (IDebugTarget) arg0;
				IDebugTarget t1 = (IDebugTarget) arg1;
				int retVal = 0;
				try {
					retVal = t0.getName().compareTo(t1.getName());
				} catch (DebugException e) {
				}
				return retVal; 
			}
		};
		
		processComparator = new Comparator(){
			public int compare(Object arg0, Object arg1) {
				IProcess t0 = (IProcess) arg0;
				IProcess t1 = (IProcess) arg1;
				return t0.getLabel().compareTo(t1.getLabel());
			}
		};

	}

	public IPJob getPJob() {
		return pJob;
	}

	public void setPJob(IPJob job) {
		pJob = job;
	}

	public IPSession getPSession() {
		return pSession;
	}

	public void setPSession(IPSession session) {
		pSession = session;
	}

	protected List getDebugTargets0() {
		List list = super.getDebugTargets0();
		Collections.sort(list, targetComparator);
		return list;
	}
	
	protected List getProcesses0() {
		List list = super.getProcesses0();
		Collections.sort(list, processComparator);
		return list;
	}	
}
