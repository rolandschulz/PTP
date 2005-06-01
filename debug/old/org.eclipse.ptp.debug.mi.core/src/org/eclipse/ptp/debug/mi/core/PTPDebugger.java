package org.eclipse.ptp.debug.mi.core;

import java.io.File;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.ICDIDebugger;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.debug.mi.core.cdi.Session;
import org.eclipse.ptp.debug.mi.core.gdb.GDBSession;
import org.eclipse.ptp.debug.mi.core.gdb.MISession;

public class PTPDebugger implements ICDIDebugger {
	public ICDISession createDebuggerSession(ILaunch launch, IBinaryObject exe, IProgressMonitor monitor) {
		return createDebuggerSession(1, launch, exe, monitor);
	}
	
	public ICDISession createDebuggerSession(int nprocs, ILaunch launch, IBinaryObject exe, IProgressMonitor monitor) {
		System.out.println("PTPdebugger.createDebuggerSession(" + nprocs + ")");
		
		try {
			File cwd = new File("/tmp/");
			File prog = exe.getPath().toFile();
			MISession[] miSessions = new MISession[nprocs];
			
			for (int i = 0; i < nprocs; i++) {
				miSessions[i] = GDBSession.getDefault().createSession(null, prog, cwd, null);
			}
			
			return new Session(miSessions);
			
		} catch (Exception e) {
			e.printStackTrace();
		}		 
		
		return null;
	}	
}