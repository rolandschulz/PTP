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
package org.eclipse.ptp.debug.core;

/**
 * Constant definitions for C/C++ debug plug-in.
 */
public interface IPDebugConstants {

	/**
	 * C/C++ debug plug-in identifier (value
	 * <code>"org.eclipse.ptp.debug.core"</code>).
	 */
	public static final String PLUGIN_ID = PTPDebugCorePlugin.getUniqueIdentifier();

	/**
	 * The identifier of the default variable format to use in the variables
	 * view
	 */
	public static final String PREF_DEFAULT_VARIABLE_FORMAT = PLUGIN_ID + "pDebug.default_variable_format"; //$NON-NLS-1$

	/**
	 * The identifier of the common source locations list
	 */
	public static final String PREF_SOURCE_LOCATIONS = PLUGIN_ID + "pDebug.Source.source_locations"; //$NON-NLS-1$
}
