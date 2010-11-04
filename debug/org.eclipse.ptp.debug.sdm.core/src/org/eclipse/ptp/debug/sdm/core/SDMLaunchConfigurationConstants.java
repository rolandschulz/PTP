/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.sdm.core;

import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;

/**
 * @since 5.0
 * 
 */
public interface SDMLaunchConfigurationConstants {
	/**
	 * Launch configuration attribute key to specify the SDM debugger backend
	 */
	public static final String ATTR_DEBUGGER_SDM_BACKEND = IPTPLaunchConfigurationConstants.PTP_LAUNCH_ID + ".DEBUGGER_SDM_BACKEND"; //$NON-NLS-1$
}
