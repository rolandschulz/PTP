/*
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.ptp.ibmpe.core.rtsystem;

import org.eclipse.ptp.ibmpe.core.rmsystem.PEResourceManagerConfiguration;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteProxyRuntimeClient;

public class PEProxyRuntimeClient extends AbstractRemoteProxyRuntimeClient {
	public PEProxyRuntimeClient(PEResourceManagerConfiguration config, 
			int baseModelId) {
		super(config, baseModelId);
	}
}
