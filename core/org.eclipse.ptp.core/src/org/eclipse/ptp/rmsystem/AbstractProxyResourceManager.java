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
package org.eclipse.ptp.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

public abstract class AbstractProxyResourceManager extends
		AbstractResourceManager {

	public AbstractProxyResourceManager(IPUniverseControl universe,
			IResourceManagerConfiguration config) {
		super(universe, config);
		// TODO Auto-generated constructor stub
	}

	public boolean abortJob(String name) throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeJob(IPJob job) {
		// TODO Auto-generated method stub

	}

	public IPJob run(ILaunch launch, JobRunConfiguration jobRunConfig,
			IProgressMonitor pm) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	private void closeConnection() {
		// TODO Auto-generated method stub
		
	}

	private void openConnection() {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 */
	protected abstract void doAfterCloseConnection();

	/**
	 * 
	 */
	protected abstract void doAfterOpenConnection();

	/**
	 * 
	 */
	protected abstract void doBeforeCloseConnection();

	/**
	 * 
	 */
	protected abstract void doBeforeOpenConnection();

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doDisableEvents()
	 */
	protected void doDisableEvents() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doEnableEvents()
	 */
	protected void doEnableEvents() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doShutdown()
	 */
	protected void doShutdown() throws CoreException {
		doBeforeCloseConnection();
		closeConnection();
		doAfterCloseConnection();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doStartup(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		doBeforeOpenConnection();
		openConnection();
		doAfterOpenConnection();
	}

}
