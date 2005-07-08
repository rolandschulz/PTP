package org.eclipse.ptp.debug.external;

import java.io.File;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.IPCDIDebugger;
import org.eclipse.ptp.debug.external.cdi.Session;

public class PTPDebugger implements IPCDIDebugger {
	public ICDISession createDebuggerSession(ILaunch launch, IBinaryObject exe, IProgressMonitor monitor) {
		return null;
	}

	public ICDISession createDebuggerSession(IPJob[] jobs, ILaunch launch, IBinaryObject exe, IProgressMonitor monitor) {
		return null;
	}
	
	public ICDISession createDebuggerSession(IPJob[] jobs, ILaunch launch, File exe, IProgressMonitor monitor) {
		
		try {
			/* Currently, we ignore the executable
			File cwd = new File("/tmp/");
			File prog = exe;
			*/

			/* Currently, we only use jobs[0] */
			
			DebugSession debug = new DebugSession();
			debug.initPTP(jobs[0]);

			Session session = new Session(debug, jobs[0]);
			
			Process debugger = session.getSessionProcess();
			
			if (debugger != null) {
				IProcess debuggerProcess = DebugPlugin.newProcess(launch, debugger, "Debugger");
				launch.addProcess(debuggerProcess);
			}
			
			return session;
			
		} catch (Exception e) {
			e.printStackTrace();
		}		 
		
		return null;
	}	
}