package org.eclipse.ptp.debug.external;

import java.io.File;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.ICDIDebugger;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;

public class PTPDebugger implements ICDIDebugger {
	public ICDISession createDebuggerSession(ILaunch launch, IBinaryObject exe, IProgressMonitor monitor) {
		return createDebuggerSession(1, launch, exe, monitor);
	}
	
	public ICDISession createDebuggerSession(int nprocs, ILaunch launch, IBinaryObject exe, IProgressMonitor monitor) {
		System.out.println("PTPdebugger.createDebuggerSession(" + nprocs + ")");
		
		try {
			File cwd = new File("/tmp/");
			File prog = exe.getPath().toFile();
			//MISession[] miSessions = new MISession[nprocs];
			
			//for (int i = 0; i < nprocs; i++) {
			//	miSessions[i] = GDBSession.getDefault().createSession(null, prog, cwd, null);
			//}
			
			//return new Session(miSessions);
			
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}		 
		
		return null;
	}	
}