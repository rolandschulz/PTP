package org.eclipse.ptp.debug.external;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.IPTPDebugger;
import org.eclipse.ptp.debug.external.cdi.Session;

public class PTPDebugger implements IPTPDebugger {
	public ICDISession createDebuggerSession(IPJob job, ILaunch launch, IBinaryObject exe, IProgressMonitor monitor) {
		try {
			DebugSession debug = new DebugSession(job);

			Session session = new Session(debug, launch, exe);
						
			return session;
			
		} catch (Exception e) {
			e.printStackTrace();
		}		 
		
		return null;
	}
}