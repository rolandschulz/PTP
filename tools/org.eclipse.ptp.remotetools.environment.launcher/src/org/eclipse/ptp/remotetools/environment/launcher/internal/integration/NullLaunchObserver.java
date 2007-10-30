/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.launcher.internal.integration;

import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchObserver;
import org.eclipse.ptp.remotetools.environment.launcher.core.NullLaunchIntegration;

public class NullLaunchObserver extends NullLaunchIntegration implements ILaunchObserver {
	public void receiveOutput(String text) {
		// TODO Auto-generated method stub
		
	}
}
