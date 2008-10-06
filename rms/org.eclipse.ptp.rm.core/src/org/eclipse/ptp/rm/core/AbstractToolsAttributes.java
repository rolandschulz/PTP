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
 * 
 * @author Daniel Felix Ferber
 */
public class AbstractToolsAttributes {

	/*
	 * I have disabled getLaunchArgumentsAttributeDefinition since
	 * the launch command will contain all arguments and a variable substitution
	 * to be replaced with the program executable path.
	 */

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[] {};
	}
}
