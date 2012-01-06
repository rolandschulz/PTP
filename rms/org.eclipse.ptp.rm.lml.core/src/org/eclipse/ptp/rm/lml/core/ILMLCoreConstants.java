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

	public static final String ZEROSTR = "";//$NON-NLS-1$
	public static final String XMLSchema = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$
	public static final String DATA = "data/"; //$NON-NLS-1$
	public static final String RM_XSD = DATA + "resource_manager_type.xsd";//$NON-NLS-1$

	/*
	 * Element keywords
	 */
	public static String INCLUDE_ELEMENT = "include"; //$NON-NLS-1$
	public static String TABLE_ELEMENT = "table"; //$NON-NLS-1$
	public static String USAGEBAR_ELEMENT = "usagebar"; //$NON-NLS-1$
	public static String TEXT_ELEMENT = "text"; //$NON-NLS-1$
	public static String INFOBOX_ELEMENT = "infobox"; //$NON-NLS-1$
	public static String CHART_ELEMENT = "chart"; //$NON-NLS-1$
	public static String CHARTGROUP_ELEMENT = "chartgroup"; //$NON-NLS-1$
	public static String NODEDISPLAY_ELEMENT = "nodedisplay"; //$NON-NLS-1$
	public static String ABSLAYOUT_ELEMENT = "abslayout"; //$NON-NLS-1$
	public static String SPLITLAYOUT_ELEMENT = "splitlayout"; //$NON-NLS-1$
	public static String TABLELAYOUT_ELEMENT = "tablelayout"; //$NON-NLS-1$
	public static String COMPONENTLAYOUT_ELEMENT = "componentlayout"; //$NON-NLS-1$
	public static String NODEDISPLAYLAYOUT_ELEMENT = "nodedisplaylayout"; //$NON-NLS-1$
	public static String TITLE_PREFIX = "title_"; //$NON-NLS-1$
}
