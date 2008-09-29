/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.environment.launcher.cellbe.internal;

import org.eclipse.ptp.cell.environment.launcher.cellbe.AbstractCellRemoteLaunchDelegate;
import org.eclipse.ptp.remotetools.environment.launcher.RemoteLauncherPlugin;



public class TargetLaunchDelegate extends AbstractCellRemoteLaunchDelegate {

//	static int counter = 0;
	
	public String getPluginID() {
		return RemoteLauncherPlugin.getUniqueIdentifier();
	}
}
