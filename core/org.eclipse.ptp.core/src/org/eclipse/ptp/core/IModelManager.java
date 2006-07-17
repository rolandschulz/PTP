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
package org.eclipse.ptp.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.rtsystem.IControlSystem;
import org.eclipse.ptp.rtsystem.IMonitoringSystem;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

public interface IModelManager extends IModelPresentation {
	/*
	public static final int STATE_ERROR = -1;
	public static final int EVENT_ALL_PROCESSES_STOPPED = 6;
	public static final int EVENT_PROCESS_OUTPUT = 1;
	public static final int EVENT_EXEC_STATUS_CHANGE = 2;
	
	public static final int EVENT_RUNNING = 0;
	public static final int EVENT_STARTING = 1;
	public static final int EVENT_ABORTED = 2;
	public static final int EVENT_STOPPED = 3;
	public static final int EVENT_EXITED = 4;
	public static final int EVENT_ERROR = 5;
	public static final int EVENT_SYS_STATUS_CHANGE = 6;
	public static final int EVENT_UPDATED_STATUS = 7;
	public static final int EVENT_MONITORING_SYSTEM_CHANGE = 8;
	*/

	public IControlSystem getControlSystem();
	public IMonitoringSystem getMonitoringSystem();
	public int getControlSystemID();
	public int getMonitoringSystemID();
	//public boolean isParallelPerspectiveOpen();
	public void shutdown();
	public IPJob run(ILaunch launch, JobRunConfiguration jobRunConfig, IProgressMonitor pm) throws CoreException;
	public void setPTPConfiguration(ILaunchConfiguration config);
	public ILaunchConfiguration getPTPConfiguration();
}
