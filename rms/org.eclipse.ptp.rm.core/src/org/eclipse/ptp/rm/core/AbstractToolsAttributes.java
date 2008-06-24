/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.core;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;


/**
 * OMPI-specific attributes
 */
public class AbstractToolsAttributes {
	
	/*
	 * I have disabled getLaunchArgumentsAttributeDefinition since
	 * the launch command will contain all arguments and a variable substitution
	 * to be replaced with the program executable path.
	 */
	
//	private static final String LAUNCH_ARGS_ATTR_ID = "launchArgs";

//	private final static StringAttributeDefinition launchArgs =
//		new StringAttributeDefinition(LAUNCH_ARGS_ATTR_ID, LAUNCH_ARGS_ATTR_ID,
//				"Launch Arguments", true, "");


	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
//				launchArgs,
			};
	}

//	public static StringAttributeDefinition getLaunchArgumentsAttributeDefinition() {
//		return launchArgs;
//	}
}
