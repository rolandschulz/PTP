/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *     Chris Navarro (Illinois/NCSA) - Design and implementation
 ******************************************************************************/
package org.eclipse.ptp.etfw.jaxb;

import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;

/**
 * ETFw constants
 * 
 * @author "Chris Navarro"
 * 
 */
public class ETFWCoreConstants {

	/* JAXB */
	public static final String DATA = "data/"; //$NON-NLS-1$
	public static final String ETFW_XSD = JAXBCoreConstants.DATA + "etfw_tool_type.xsd";//$NON-NLS-1$
	public static final String JAXB_CONTEXT = "org.eclipse.ptp.etfw.jaxb.data";//$NON-NLS-1$

	/* ETFW Workflow Extension */
	public static final String WORKFLOW_EXT_PT = "org.eclipse.ptp.etfw.jaxb.workflows"; //$NON-NLS-1$

	/* Other Constants */
	public static final String PAIRED_ATTRIBUTE_SAVED = "_SAVED"; //$NON-NLS-1$
	public static final String RM_NAME = "org.eclipse.ptp.launch.RESOURCE_MANAGER_NAME"; //$NON-NLS-1$
}
