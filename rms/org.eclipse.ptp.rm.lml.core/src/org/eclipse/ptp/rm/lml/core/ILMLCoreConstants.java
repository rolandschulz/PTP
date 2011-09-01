/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 ******************************************************************************/
package org.eclipse.ptp.rm.lml.core;

public interface ILMLCoreConstants {
	public static final String PLUGIN_ID = LMLCorePlugin.getUniqueIdentifier();
	public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$

	public static final int UNDEFINED = -1;

	/*
	 * Element keywords
	 */
	public static String INCLUDE_ELEMENT = "include"; //$NON-NLS-1$
	public static String TABLE_ELEMENT = "table"; //$NON-NLS-1$
	public static String TITLE_PREFIX = "title_"; //$NON-NLS-1$
}
