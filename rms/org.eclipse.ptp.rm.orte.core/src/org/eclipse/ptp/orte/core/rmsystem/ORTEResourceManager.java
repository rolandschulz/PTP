/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.orte.core.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.orte.core.rtsystem.OMPIControlSystem;
import org.eclipse.ptp.orte.core.rtsystem.OMPIMonitoringSystem;
import org.eclipse.ptp.orte.core.rtsystem.OMPIProxyRuntimeClient;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.RuntimeResourceManager;

public class ORTEResourceManager extends RuntimeResourceManager {

	public ORTEResourceManager(IPUniverseControl universe,
			IResourceManagerConfiguration config) {
		super(universe, config);
		if (config instanceof ORTEResourceManagerConfiguration) {
			ORTEResourceManagerConfiguration oConfig = (ORTEResourceManagerConfiguration) config;
			System.out.println("ctor ORTEResourceManager: " + oConfig.getOrteServerFile());
		}
	}

	protected void doStartRuntime(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Starting OMPI proxy runtime...", 30);
		try {
			ORTEResourceManagerConfiguration config = (ORTEResourceManagerConfiguration) getConfiguration();
			String serverFile = config.getOrteServerFile();
			boolean launchManually = config.isLaunchManually();
			/* load up the control and monitoring systems for OMPI */
			OMPIProxyRuntimeClient runtimeProxy = new OMPIProxyRuntimeClient(serverFile,
					launchManually);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			setRuntimeProxy(runtimeProxy);
			monitor.worked(10);

			if(!runtimeProxy.startup(monitor)) {
				System.err.println("Failed to start up the proxy runtime.");
				runtimeProxy = null;
				setRuntimeProxy(runtimeProxy);
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, 
						"There was an error starting the OMPI proxy runtime.  The path to 'ptp_orte_proxy' or 'orted' "+
						"may have been incorrect.  The 'orted' binary MUST be in your PATH to be found by 'ptp_orte_proxy'.  "+
						"Try checking the console log or error logs for more detailed information.",
						null));
			}
			monitor.subTask("Starting OMPI monitoring system...");
			setMonitoringSystem(new OMPIMonitoringSystem(runtimeProxy));
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(10);
			monitor.subTask("Starting OMPI control system...");
			setControlSystem(new OMPIControlSystem(runtimeProxy));
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.worked(10);
		}
		finally {
			monitor.done();
		}
	}

	protected void doDispose() {
		// do nothing
	}

	protected void doStop() throws CoreException {
		// do nothing
	}

}
