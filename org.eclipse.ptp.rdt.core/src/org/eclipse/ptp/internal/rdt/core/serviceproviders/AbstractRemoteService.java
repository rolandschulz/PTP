/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.serviceproviders;

import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;

public class AbstractRemoteService {

	protected IHost fHost;
	protected IConnectorService fConnectorService;
	protected ICIndexSubsystem fIndexSubsystem;

	public AbstractRemoteService() {
		super();
	}

	protected ICIndexSubsystem getSubSystem() {
		if (fIndexSubsystem == null) {
	
			ISubSystem[] subSystems = fConnectorService.getSubSystems();
	
			for (int k = 0; k < subSystems.length; k++) {
				if (subSystems[k] instanceof ICIndexSubsystem)
	
					fIndexSubsystem = (ICIndexSubsystem) subSystems[k];
			}
		}
		
		return fIndexSubsystem;
	}

}