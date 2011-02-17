/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.core.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.rm.pbs.core.Activator;
import org.eclipse.ptp.rm.pbs.core.rtsystem.PBSProxyRuntimeClient;
import org.eclipse.ptp.rm.pbs.core.rtsystem.PBSRuntimeSystem;
import org.eclipse.ptp.rm.pbs.core.templates.PBSBatchScriptTemplateManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManager;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

public class PBSResourceManager extends AbstractRuntimeResourceManager {
	private PBSBatchScriptTemplateManager fTemplateManager = null;

	/**
	 * @since 5.0
	 */
	public PBSResourceManager(IPUniverse universe, IResourceManagerConfiguration config) {
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
		IPBSResourceManagerConfiguration config = (IPBSResourceManagerConfiguration) getConfiguration();
		IPResourceManager rm = (IPResourceManager) getAdapter(IPResourceManager.class);
		int baseId;
		try {
			baseId = Integer.parseInt(rm.getID());
		} catch (NumberFormatException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getLocalizedMessage()));
		}
		PBSProxyRuntimeClient runtimeProxy = new PBSProxyRuntimeClient(config, baseId);
		return new PBSRuntimeSystem(this, runtimeProxy);
	}

	/**
	 * @since 5.0
	 */
	public PBSBatchScriptTemplateManager getTemplateManager() {
		if (fTemplateManager == null) {
			try {
				fTemplateManager = new PBSBatchScriptTemplateManager(this);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return fTemplateManager;
	}
}