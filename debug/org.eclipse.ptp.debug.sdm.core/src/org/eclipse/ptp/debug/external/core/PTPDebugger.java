/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.external.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.debug.core.IPTPDebugger;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.external.core.debugger.PDIDebugger;
import org.eclipse.ptp.debug.internal.core.pdi.Session;

/**
 * @author clement
 *
 */
public class PTPDebugger implements IPTPDebugger {
	private IPDIDebugger pdiDebugger = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPTPDebugger#createDebugSession(long, org.eclipse.ptp.debug.core.launch.IPLaunch, org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IPDISession createDebugSession(long timeout, IPLaunch launch, IPath corefile, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		return createSession(timeout, launch, corefile, monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPTPDebugger#getDebuggerPort(int)
	 */
	public int getDebuggerPort(int timeout) throws CoreException {
		if (pdiDebugger == null) {
			pdiDebugger = new PDIDebugger();
		}
		try {
			return pdiDebugger.getDebuggerPort(timeout);
		}
		catch (PDIException e) {
			pdiDebugger = null;
			throw newCoreException(e);
		}
	}
	
	/**
	 * @return
	 */
	private IPDIDebugger getDebugger() {
		if (pdiDebugger == null) {
			pdiDebugger = new PDIDebugger();
		}
		return pdiDebugger;
	}
	
	/**
	 * @param timeout
	 * @param launch
	 * @param corefile
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	protected Session createSession(long timeout, IPLaunch launch, IPath corefile, IProgressMonitor monitor) throws CoreException {
		IPJob job = launch.getPJob();
		int job_size = getJobSize(job);
		try {
			return new Session(launch.getLaunchConfiguration(), timeout, getDebugger(), job.getID(), job_size);
		}
		catch (PDIException e) {
			throw newCoreException(e);
		}
	}
	
	/**
	 * @param job
	 * @return
	 */
	protected int getJobSize(IPJob job) {
		IntegerAttribute numProcAttr = job.getAttribute(JobAttributes.getNumberOfProcessesAttributeDefinition());
		if (numProcAttr != null) {
			return numProcAttr.getValue();
		}
		return 1;
	}
	
	/**
	 * @param exception
	 * @return
	 */
	protected CoreException newCoreException(Throwable exception) {
		MultiStatus status = new MultiStatus(PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, "Cannot start debugging", exception);
		status.add(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, exception == null ? new String() : exception.getLocalizedMessage(), exception));
		return new CoreException(status);
	}
}
