/*******************************************************************************
 * Copyright (c) 2013 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *     Chris Navarro (Illinois/NCSA) - Design and implementation
 ******************************************************************************/
package org.eclipse.ptp.etfw;

import org.eclipse.ptp.internal.etfw.Activator;

/**
 * Preference constants for the external tools framework
 * 
 * @since 7.0
 * 
 */
public class PreferenceConstants {
	public static final String ETFW_VERSION = "ETFW_VERSION"; //$NON-NLS-1$
	/**
	 * @since 7.1
	 */
	public static final String ETFW_WORKFLOW = "ETFW_WORKFLOW"; //$NON-NLS-1$

	/**
	 * @return the etfw version stored in preferences
	 */
	public static String getVersion() {
		return Preferences.getString(Activator.PLUGIN_ID, ETFW_VERSION);
	}

	/**
	 * Get the default workflow, or null if no default workflow has been set
	 * 
	 * @return default workflow or null
	 * @since 7.1
	 */
	public static String getWorkflow() {
		return Preferences.getString(Activator.PLUGIN_ID, ETFW_WORKFLOW);
	}

	/**
	 * Set the default workflow
	 * 
	 * @since 7.1
	 */
	public static void setWorkflow(String workflow) {
		Preferences.setString(Activator.PLUGIN_ID, ETFW_WORKFLOW, workflow);
	}
}
