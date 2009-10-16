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

package org.eclipse.ptp.rm.pbs.core.rtsystem;

import org.eclipse.ptp.rm.core.rtsystem.AbstractRemoteProxyRuntimeClient;
import org.eclipse.ptp.rm.pbs.core.rmsystem.IPBSResourceManagerConfiguration;

public class PBSProxyRuntimeClient extends AbstractRemoteProxyRuntimeClient {
	public PBSProxyRuntimeClient(IPBSResourceManagerConfiguration config, 
			int baseModelId) {
		super(config, baseModelId);
	}
}
