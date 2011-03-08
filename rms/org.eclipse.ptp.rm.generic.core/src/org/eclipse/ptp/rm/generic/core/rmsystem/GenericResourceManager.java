/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.generic.core.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolResourceManager;
import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;
import org.eclipse.ptp.rm.generic.core.rtsystem.GenericRMRuntimeSystem;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerControl;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerMonitor;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class GenericResourceManager extends AbstractToolResourceManager {

	public GenericResourceManager(IResourceManagerConfiguration config, AbstractRuntimeResourceManagerControl control,
			AbstractRuntimeResourceManagerMonitor monitor) {
		super(config, control, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManager#doCreateRuntimeSystem
	 * ()
	 */
	@Override
	protected IRuntimeSystem doCreateRuntimeSystem() throws CoreException {
		IToolRMConfiguration config = (IToolRMConfiguration) getConfiguration();
		return new GenericRMRuntimeSystem(this, config);
	}
}
