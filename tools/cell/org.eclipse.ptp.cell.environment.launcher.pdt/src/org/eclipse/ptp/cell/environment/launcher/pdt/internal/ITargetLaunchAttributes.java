/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.environment.launcher.pdt.internal;

/**
 * Definition of key names and default values that are used by the launcher.
 * @author Daniel Felix Ferber
 */
public interface ITargetLaunchAttributes {
	public static final String TARGET_LAUNCH_ID = "org.eclipse.ptp.remotetools.environment.launcher.cellbe"; //$NON-NLS-1$
	public static final String ATTR_TARGET_ID = TARGET_LAUNCH_ID + ".CELL_TARGET";  //$NON-NLS-1$
}
