/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.launcher.macros;

import org.eclipse.cdt.internal.core.cdtvariables.ICoreVariableContextInfo;

/**
 * @author laggarcia
 *
 */
public interface ILaunchVariableContextInfo extends ICoreVariableContextInfo {

	// The contexts defined by this interface must be consistent with the
	// contexts defined in @see
	// org.eclipse.cdt.core.cdtvariables.ICoreVariableContextInfo
	public final static int CONTEXT_WORKSPACE = 5;

	public final static int CONTEXT_LAUNCH = 9;
	
}
