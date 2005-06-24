/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core;

import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;

/**
 * Definitions of the internal constants for C/C++ Debug plug-in.
 */
public class IPDebugInternalConstants {

	/**
	 * Status handler codes.
	 */
	public static final int STATUS_CODE_QUESTION = 10000;
	public static final int STATUS_CODE_INFO = 10001;
	public static final int STATUS_CODE_ERROR = 10002;

	/**
	 * String preference for the common source containers.
	 */
	public static final String PREF_COMMON_SOURCE_CONTAINERS = PTPDebugCorePlugin.getUniqueIdentifier() + ".cDebug.common_source_containers"; //$NON-NLS-1$
}