/*
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.ptp.rm.ibm.pe.core.rtsystem;

import org.eclipse.ptp.rm.core.proxy.AbstractRemoteProxyRuntimeClient;
import org.eclipse.ptp.rm.ibm.pe.core.rmsystem.PEResourceManagerConfiguration;

public class PEProxyRuntimeClient extends AbstractRemoteProxyRuntimeClient {
	public PEProxyRuntimeClient(PEResourceManagerConfiguration config, int baseModelId) {
		super(config, baseModelId);
	}
}
