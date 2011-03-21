/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.smoa.core.rtsystem;

import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.rm.smoa.core.SMOAConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;
import org.eclipse.ptp.rtsystem.IRuntimeSystemFactory;

public class SMOARuntimeSystemFactory implements IRuntimeSystemFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeSystemFactory#create(org.eclipse.ptp
	 * .rmsystem.IResourceManager)
	 */
	public IRuntimeSystem create(IResourceManager rm) {
		SMOAConfiguration config = (SMOAConfiguration) rm.getConfiguration();
		IPResourceManager prm = (IPResourceManager) rm.getAdapter(IPResourceManager.class);
		return new SMOARuntimeSystem(config, prm.getID());
	}
}
