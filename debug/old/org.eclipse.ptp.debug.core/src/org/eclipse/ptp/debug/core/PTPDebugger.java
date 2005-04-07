package org.eclipse.ptp.debug.core;

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.ICDIDebugger;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.debug.core.cdi.Session;

/**
 * Implementing cdebugger extension point
 */
public class PTPDebugger implements ICDIDebugger {

	public ICDISession createDebuggerSession(ILaunch launch, IBinaryObject exe, IProgressMonitor monitor) {
		return createDebuggerSession(1, launch, exe, monitor);
	}
	
	public ICDISession createDebuggerSession(int nprocs, ILaunch launch, IBinaryObject exe, IProgressMonitor monitor) {
		System.out.println("PTPdebugger.createDebuggerSession(" + nprocs + ")");
		return new Session(nprocs);
	}
}