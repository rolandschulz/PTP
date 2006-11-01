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
package org.eclipse.ptp.internal.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IModelListener;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.INodeListener;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.core.IProcessListener;
import org.eclipse.ptp.rtsystem.IControlSystem;
import org.eclipse.ptp.rtsystem.IMonitoringSystem;
import org.eclipse.ptp.rtsystem.IRuntimeListener;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

public class NullManager implements IModelManager {

	public static class NullMonitoringSystem implements IMonitoringSystem {

		public void addRuntimeListener(IRuntimeListener listener) {
			// no-op
		}

		public void removeRuntimeListener(IRuntimeListener listener) {
			// no-op
		}

		public void shutdown() {
			// no-op
		}

		public void startup() {
			// no-op
		}

		public void initiateDiscovery() throws CoreException {
			// TODO Auto-generated method stub
			
		}

	}

	public static class NullControlSystem implements IControlSystem {

		public void addRuntimeListener(IRuntimeListener listener) {
			// no-op
		}

		public String[] getAllProcessesAttributes(IPJob job, String[] attribs)
				throws CoreException {
			return new String[0];
		}

		public String[] getJobs() throws CoreException {
			return new String[0];
		}

		public String[] getProcessAttributes(IPProcess proc, String[] attribs)
				throws CoreException {
			return new String[0];
		}

		public String[] getProcesses(IPJob job) throws CoreException {
			return new String[0];
		}

		public boolean isHealthy() {
			// TODO Auto-generated method stub
			return false;
		}

		public void removeRuntimeListener(IRuntimeListener listener) {
			// no-op
		}

		public void run(int jobID, JobRunConfiguration jobRunConfig) throws CoreException {
			// no-op
		}

		public void shutdown() {
			// no-op
		}

		public void startup() {
			// no-op
		}

		public void terminateJob(IPJob job) throws CoreException {
			// no-op
		}

	}

	public IControlSystem getControlSystem() {
		return new NullControlSystem();
	}

	public IMonitoringSystem getMonitoringSystem() {
		return new NullMonitoringSystem();
	}

	public ILaunchConfiguration getPTPConfiguration() {
		// no-op
		return null;
	}

	public IPJob run(ILaunch launch, JobRunConfiguration jobRunConfig,
			IProgressMonitor pm) throws CoreException {
		// no-op
		return null;
	}

	public void shutdown() {
		// no-op
	}

	public void abortJob(String jobName) throws CoreException {
		// no-op
	}

	public IPUniverse getUniverse() {
		return new NullUniverse();
	}

	public void refreshRuntimeSystems(IProgressMonitor monitor, boolean force)
			throws CoreException {
		// no-op
	}

	public int getControlSystemID() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMonitoringSystemID() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setPTPConfiguration(ILaunchConfiguration config) {
		// TODO Auto-generated method stub
		
	}

	public void addModelListener(IModelListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void addNodeListener(INodeListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void addProcessListener(IProcessListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void refreshRuntimeSystems(int controlSystemID, int monitoringSystemID, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	public void removeModelListener(IModelListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void removeNodeListener(INodeListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void removeProcessListener(IProcessListener listener) {
		// TODO Auto-generated method stub
		
	}

}
