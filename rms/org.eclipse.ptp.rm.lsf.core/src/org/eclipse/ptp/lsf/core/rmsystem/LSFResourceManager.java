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
/**
 * 
 */
package org.eclipse.ptp.lsf.core.rmsystem;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.rmsystem.AbstractProxyResourceManager;

/**
 * @author rsqrd
 *
 */
public class LSFResourceManager extends AbstractProxyResourceManager {

	/**
	 * @param universe
	 * @param config
	 */
	public LSFResourceManager(IPUniverseControl universe, LSFResourceManagerConfiguration config) {
		super(universe, config);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doAfterCloseConnection()
	 */
	protected void doAfterCloseConnection() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doAfterOpenConnection()
	 */
	protected void doAfterOpenConnection() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doBeforeCloseConnection()
	 */
	protected void doBeforeCloseConnection() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doBeforeOpenConnection()
	 */
	protected void doBeforeOpenConnection() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doDispose()
	 */
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

	public IAttribute[] getLaunchAttributes(IPMachineControl machine, IPQueueControl queue) {
		// TODO Auto-generated method stub
		return null;
	}

}
