package org.eclipse.ptp.debug.core;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPFunctionBreakpoint;
import org.eclipse.ptp.debug.core.model.IPLineBreakpoint;
import org.eclipse.ptp.debug.internal.core.sourcelookup.CSourceLookupDirector;

/**
 * @deprecated 
 */
public class PSession implements IPSession, IBreakpointsListener {
	private IPCDISession pCDISession;
	private IPLaunch pLaunch;

	public PSession(IPCDISession session, IPLaunch launch) {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);

		pCDISession = session;
		pLaunch = launch;

		//initializeBreakpoints();

		/* Initially we only create process/target 0 */
		//session.registerTarget(0, true);
		//session.goAction(session.createBitList());
	}

	public IPCDISession getPCDISession() {
		return pCDISession;
	}

	public void breakpointsAdded(IBreakpoint[] bpts) {
		PTPDebugCorePlugin.getDefault().getLogger().finer("");
		String job_id = pLaunch.getPJob().getIDString();
		for (int i = 0; i < bpts.length; ++i) {
			if (bpts[i] instanceof IPBreakpoint) {
				try {
					String bp_job_id = ((IPBreakpoint)bpts[i]).getJobId(); 
					if (bp_job_id.equals(job_id) || bp_job_id.equals(IPBreakpoint.GLOBAL)) {
						//setBreakpoint(job_id, (IPBreakpoint)bpts[i]);
					}
				} catch (CoreException e) {
				}
			}
		}
	}

	public void breakpointsRemoved(IBreakpoint[] bpts, IMarkerDelta[] deltas) {
		PTPDebugCorePlugin.getDefault().getLogger().finer("");
	}

	public void breakpointsChanged(IBreakpoint[] bpts, IMarkerDelta[] deltas) {
		PTPDebugCorePlugin.getDefault().getLogger().finer("");
	}

	/*
	private void initializeBreakpoints() {
		try {
			String job_id = pLaunch.getPJob().getIDString();
			IPBreakpoint[] bpts = PCDIDebugModel.getDefault().findPBreakpointsByJob(job_id, true);
			for (int i = 0; i < bpts.length; i++) {
				setBreakpoint(job_id, bpts[i]);
			}
		} catch (CoreException e) {
		}
	}
	private void setBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException {
		BitList tasks = PCDIDebugModel.getDefault().getTasks(job_id, bpt.getSetId());
		ICDILocation location = getLocation(bpt);
		setLocationBreakpointOnSession(bpt, location, null, bpt.isEnabled(), tasks);				
	}

	private ICDILocation getLocation(IPBreakpoint bpt) throws CoreException {
		IPath path = convertPath(bpt.getSourceHandle());
		if (bpt instanceof IPLineBreakpoint) {
			return pCDISession.createLineLocation(path.lastSegment(), ((IPLineBreakpoint)bpt).getLineNumber());
		}
		else if (bpt instanceof IPFunctionBreakpoint) {
			return pCDISession.createFunctionLocation(path.lastSegment(), ((IPFunctionBreakpoint)bpt).getFunction());
		}
		return null;
	}

	private void setLocationBreakpointOnSession(final IPBreakpoint breakpoint, final ICDILocation location, final ICDICondition condition, final boolean enabled, final BitList tasks) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					if (breakpoint instanceof IPLineBreakpoint) {
						pCDISession.setLineBreakpoint(tasks, ICDIBreakpoint.REGULAR, (ICDILineLocation) location, condition, true);
					} else if (breakpoint instanceof IPFunctionBreakpoint) {
						pCDISession.setFunctionBreakpoint(tasks, ICDIBreakpoint.REGULAR, (ICDIFunctionLocation) location, condition, true);
					}
				} catch (CDIException e) {
				}
			}
		});
	}
	*/

	private IPath convertPath(String sourceHandle) {
		IPath path = null;
		if (Path.EMPTY.isValidPath(sourceHandle)) {
			ISourceLocator sl = pLaunch.getSourceLocator();
			if (sl instanceof CSourceLookupDirector) {
				path = ((CSourceLookupDirector) sl).getCompilationPath(sourceHandle);
			}
			if (path == null) {
				path = new Path(sourceHandle);
			}
		}
		return path;
	}
}
