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
 * 
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.core.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.rm.ibm.pe.core.PECorePlugin;
import org.eclipse.ptp.rm.ibm.pe.core.rtsystem.PEProxyRuntimeClient;
import org.eclipse.ptp.rm.ibm.pe.core.rtsystem.PERuntimeSystem;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManager;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

public class PEResourceManager extends AbstractRuntimeResourceManager {

	/**
	 * @since 5.0
	 */
	public PEResourceManager(IPUniverse universe, IResourceManagerConfiguration config) {
		super(universe, config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doAfterCloseConnection
	 * ()
	 */
	@Override
	protected void doAfterCloseConnection() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doAfterOpenConnection
	 * ()
	 */
	@Override
	protected void doAfterOpenConnection() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doBeforeCloseConnection
	 * ()
	 */
	@Override
	protected void doBeforeCloseConnection() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractProxyResourceManager#doBeforeOpenConnection
	 * ()
	 */
	@Override
	protected void doBeforeOpenConnection() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doCreateRuntimeSystem
	 * ()
	 */
	@Override
	protected IRuntimeSystem doCreateRuntimeSystem() throws CoreException {
		IPEResourceManagerConfiguration config = (IPEResourceManagerConfiguration) getConfiguration();
		IPResourceManager rm = (IPResourceManager) getAdapter(IPResourceManager.class);
		int baseId;
		try {
			baseId = Integer.parseInt(rm.getID());
		} catch (NumberFormatException e) {
			throw new CoreException(new Status(IStatus.ERROR, PECorePlugin.getUniqueIdentifier(), e.getLocalizedMessage()));
		}
		PEProxyRuntimeClient runtimeProxy = new PEProxyRuntimeClient(config, baseId);
		return new PERuntimeSystem(runtimeProxy);
	}
}