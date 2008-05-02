/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rm.ompi.core;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


/**
 * OMPI-specific attributes
 */
public class OMPIAttributes {
	private static final String LAUNCH_ARGS_ATTR_ID = "launchArgs";

	private final static StringAttributeDefinition launchArgs = 
		new StringAttributeDefinition(LAUNCH_ARGS_ATTR_ID, LAUNCH_ARGS_ATTR_ID,
				"Launch Arguments", true, "");


	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
				launchArgs,
			};
	}
	
	public static StringAttributeDefinition getLaunchArgumentsAttributeDefinition() {
		return launchArgs;
	}
}
